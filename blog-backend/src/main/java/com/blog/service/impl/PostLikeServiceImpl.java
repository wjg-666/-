package com.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.entity.PostLike;
import com.blog.mapper.PostLikeMapper;
import com.blog.service.PostLikeService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Service
public class PostLikeServiceImpl extends ServiceImpl<PostLikeMapper, PostLike> implements PostLikeService {

    @Override
    public boolean toggleLike(Long postId, Long userId) {
        PostLike existing = lambdaQuery()
                .eq(PostLike::getPostId, postId)
                .eq(PostLike::getUserId, userId)
                .one();
        if (existing != null) {
            removeById(existing.getId());
            return false; // 取消点赞
        }
        PostLike like = new PostLike();
        like.setPostId(postId);
        like.setUserId(userId);
        try {
            save(like);
        } catch (DuplicateKeyException e) {
            // 并发时重复点击，忽略
        }
        return true; // 点赞成功
    }

    @Override
    public boolean isLiked(Long postId, Long userId) {
        if (userId == null) return false;
        return lambdaQuery()
                .eq(PostLike::getPostId, postId)
                .eq(PostLike::getUserId, userId)
                .count() > 0;
    }

    @Override
    public int likeCount(Long postId) {
        return Math.toIntExact(lambdaQuery()
                .eq(PostLike::getPostId, postId)
                .count());
    }
}
