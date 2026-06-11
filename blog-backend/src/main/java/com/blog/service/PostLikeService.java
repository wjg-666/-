package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.entity.PostLike;

public interface PostLikeService extends IService<PostLike> {
    boolean toggleLike(Long postId, Long userId);
    boolean isLiked(Long postId, Long userId);
    int likeCount(Long postId);
}
