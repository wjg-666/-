package com.blog.controller;

import com.blog.dto.PostDto;
import com.blog.entity.Post;
import com.blog.service.PostService;
import com.blog.utils.CurrentUser;
import com.blog.vo.PostVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final CurrentUser currentUser;

    public PostController(PostService postService, CurrentUser currentUser) {
        this.postService = postService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public ResponseEntity<IPage<PostVo>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(postService.pageWithAuthor(page, size, keyword));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detail(@PathVariable Long id) {
        PostVo vo = postService.getDetail(id);
        if (vo == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(vo);
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody PostDto dto) {
        Post post = new Post();
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setCoverImage(dto.getCoverImage());
        post.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        PostVo vo = postService.createPost(post, currentUser.getUserId());
        return ResponseEntity.ok(vo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody PostDto dto) {
        Post post = new Post();
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setCoverImage(dto.getCoverImage());
        post.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        PostVo vo = postService.updatePost(id, post, currentUser.getUserId());
        if (vo == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(vo);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        postService.deletePost(id, currentUser.getUserId(), currentUser.isAdmin());
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }
}
