package com.blog.controller;

import com.blog.dto.CommentDto;
import com.blog.entity.Comment;
import com.blog.service.CommentService;
import com.blog.utils.CurrentUser;
import com.blog.vo.CommentVo;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CommentController {

    private final CommentService commentService;
    private final CurrentUser currentUser;

    public CommentController(CommentService commentService, CurrentUser currentUser) {
        this.commentService = commentService;
        this.currentUser = currentUser;
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<CommentVo>> list(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getByPostId(postId));
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentVo> create(@PathVariable Long postId,
                                            @Valid @RequestBody CommentDto dto) {
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setContent(dto.getContent());
        comment.setParentId(dto.getParentId());
        try {
            Long uid = currentUser.getUserId();
            comment.setUserId(uid);
        } catch (Exception e) {
            comment.setGuestName(dto.getGuestName() != null ? dto.getGuestName() : "匿名");
        }
        return ResponseEntity.ok(commentService.addComment(comment));
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        // 防御性检查：只有管理员能删除评论
        if (!currentUser.isAdmin()) {
            return ResponseEntity.status(403).body(Map.of("message", "无权删除评论"));
        }
        commentService.deleteComment(id);
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }
}
