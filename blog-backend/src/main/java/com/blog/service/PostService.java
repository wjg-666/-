package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.entity.Post;
import com.blog.vo.PostVo;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

public interface PostService extends IService<Post> {
    IPage<PostVo> pageWithAuthor(int page, int size, String keyword, Long categoryId);
    List<PostVo> listPopular(int limit);
    PostVo getDetail(Long id, Long currentUserId);
    PostVo createPost(Post post, List<String> tagNames, Long userId);
    PostVo updatePost(Long id, Post post, List<String> tagNames, Long userId, boolean isAdmin);
    void deletePost(Long id, Long userId, boolean isAdmin);
    IPage<PostVo> adminPage(int page, int size, String keyword, Long userId);
    void adminDelete(Long id);
}
