package com.blog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.entity.User;
import com.blog.mapper.CommentMapper;
import com.blog.service.PostService;
import com.blog.service.UserService;
import com.blog.vo.PostVo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final PostService postService;
    private final CommentMapper commentMapper;

    public AdminController(UserService userService, PostService postService, CommentMapper commentMapper) {
        this.userService = userService;
        this.postService = postService;
        this.commentMapper = commentMapper;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> stats() {
        return ResponseEntity.ok(Map.of(
            "userCount", userService.count(),
            "postCount", postService.count(),
            "commentCount", commentMapper.selectCount(null)
        ));
    }

    @GetMapping("/users")
    public ResponseEntity<IPage<User>> userList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.page(new Page<>(page, size)));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) return ResponseEntity.notFound().build();
        user.setStatus(user.getStatus() == 1 ? 0 : 1);
        userService.updateById(user);
        return ResponseEntity.ok(Map.of("message", "状态更新成功"));
    }

    @GetMapping("/posts")
    public ResponseEntity<IPage<PostVo>> postList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(postService.adminPage(page, size, keyword, userId));
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id) {
        postService.adminDelete(id);
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }
}
