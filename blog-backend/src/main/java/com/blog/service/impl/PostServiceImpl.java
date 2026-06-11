package com.blog.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.entity.Post;
import com.blog.entity.User;
import com.blog.mapper.PostMapper;
import com.blog.mapper.UserMapper;
import com.blog.service.PostService;
import com.blog.vo.PostVo;
import org.springframework.stereotype.Service;

@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    private final PostMapper postMapper;
    private final UserMapper userMapper;

    public PostServiceImpl(PostMapper postMapper, UserMapper userMapper) {
        this.postMapper = postMapper;
        this.userMapper = userMapper;
    }

    @Override
    public IPage<PostVo> pageWithAuthor(int page, int size, String keyword) {
        return postMapper.selectPageWithAuthor(new Page<>(page, size), keyword);
    }

    @Override
    public PostVo getDetail(Long id) {
        Post post = getById(id);
        if (post == null || post.getStatus() == 0) return null;
        post.setViewCount(post.getViewCount() + 1);
        updateById(post);
        User author = userMapper.selectById(post.getAuthorId());
        return toVo(post, author);
    }

    @Override
    public PostVo createPost(Post post, Long userId) {
        post.setAuthorId(userId);
        post.setViewCount(0);
        save(post);
        User author = userMapper.selectById(userId);
        return toVo(post, author);
    }

    @Override
    public PostVo updatePost(Long id, Post updated, Long userId) {
        Post post = getById(id);
        if (post == null) return null;
        post.setTitle(updated.getTitle());
        post.setContent(updated.getContent());
        post.setCoverImage(updated.getCoverImage());
        post.setStatus(updated.getStatus());
        updateById(post);
        User author = userMapper.selectById(post.getAuthorId());
        return toVo(post, author);
    }

    @Override
    public void deletePost(Long id, Long userId, boolean isAdmin) {
        Post post = getById(id);
        if (post != null && (isAdmin || post.getAuthorId().equals(userId))) {
            removeById(id);
        }
    }

    @Override
    public IPage<PostVo> adminPage(int page, int size, String keyword, Long userId) {
        return postMapper.selectAdminPage(new Page<>(page, size), keyword, userId);
    }

    @Override
    public void adminDelete(Long id) {
        removeById(id);
    }

    private PostVo toVo(Post p, User u) {
        return PostVo.builder()
            .id(p.getId()).title(p.getTitle()).content(p.getContent())
            .authorId(p.getAuthorId())
            .authorName(u != null ? u.getNickname() : "Unknown")
            .authorAvatar(u != null ? u.getAvatar() : null)
            .coverImage(p.getCoverImage()).viewCount(p.getViewCount())
            .status(p.getStatus())
            .createTime(p.getCreateTime()).updateTime(p.getUpdateTime())
            .build();
    }
}
