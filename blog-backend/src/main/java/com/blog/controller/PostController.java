package com.blog.controller;

import com.blog.dto.PostDto;
import com.blog.entity.Post;
import com.blog.service.PostLikeService;
import com.blog.service.PostService;
import com.blog.utils.CurrentUser;
import com.blog.utils.Result;
import com.blog.vo.PostVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final PostLikeService postLikeService;
    private final CurrentUser currentUser;

    public PostController(PostService postService, PostLikeService postLikeService,
                          CurrentUser currentUser) {
        this.postService = postService;
        this.postLikeService = postLikeService;
        this.currentUser = currentUser;
    }

    @GetMapping("/popular")
    public ResponseEntity<Result> popular() {
        List<PostVo> list = postService.listPopular(5);
        return ResponseEntity.ok(Result.ok(list));
    }

    @GetMapping
    public ResponseEntity<Result> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId) {
        IPage<PostVo> result = postService.pageWithAuthor(page, size, keyword, categoryId);
        return ResponseEntity.ok(Result.ok(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Result> detail(@PathVariable Long id) {
        Long userId = null;
        try { userId = currentUser.getUserId(); } catch (Exception ignored) {}
        PostVo vo = postService.getDetail(id, userId);
        if (vo == null) {
            return ResponseEntity.ok(Result.notFound("文章不存在"));
        }
        return ResponseEntity.ok(Result.ok(vo));
    }

    @PostMapping
    public ResponseEntity<Result> create(@Valid @RequestBody PostDto dto) {
        Post post = new Post();
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setCategoryId(dto.getCategoryId());
        post.setCoverImage(dto.getCoverImage());
        post.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        PostVo vo = postService.createPost(post, dto.getTagNames(), currentUser.getUserId());
        return ResponseEntity.ok(Result.ok("发布成功", vo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Result> update(@PathVariable Long id, @Valid @RequestBody PostDto dto) {
        Post post = new Post();
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setCategoryId(dto.getCategoryId());
        post.setCoverImage(dto.getCoverImage());
        post.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        PostVo vo = postService.updatePost(id, post, dto.getTagNames(),
                currentUser.getUserId(), currentUser.isAdmin());
        if (vo == null) {
            return ResponseEntity.ok(Result.notFound("文章不存在"));
        }
        return ResponseEntity.ok(Result.ok("更新成功", vo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Result> delete(@PathVariable Long id) {
        postService.deletePost(id, currentUser.getUserId(), currentUser.isAdmin());
        return ResponseEntity.ok(Result.ok("删除成功"));
    }

    // ===== 点赞 =====

    @PostMapping("/{id}/like")
    public ResponseEntity<Result> like(@PathVariable Long id) {
        boolean liked = postLikeService.toggleLike(id, currentUser.getUserId());
        return ResponseEntity.ok(Result.ok(liked ? "点赞成功" : "取消点赞",
                Map.of("liked", liked)));
    }
}
