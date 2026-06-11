package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.entity.Post;
import com.blog.vo.PostVo;
import com.baomidou.mybatisplus.core.metadata.IPage;

public interface PostService extends IService<Post> {
    IPage<PostVo> pageWithAuthor(int page, int size, String keyword);
    PostVo getDetail(Long id);
    PostVo createPost(Post post, Long userId);
    PostVo updatePost(Long id, Post post, Long userId);
    void deletePost(Long id, Long userId, boolean isAdmin);
    IPage<PostVo> adminPage(int page, int size, String keyword, Long userId);
    void adminDelete(Long id);
}
