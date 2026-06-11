package com.blog.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.entity.Post;
import com.blog.entity.Tag;
import com.blog.entity.User;
import com.blog.mapper.PostMapper;
import com.blog.mapper.UserMapper;
import com.blog.service.PostLikeService;
import com.blog.service.PostService;
import com.blog.service.TagService;
import com.blog.vo.PostVo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final JdbcTemplate jdbcTemplate;
    private final TagService tagService;
    private final PostLikeService postLikeService;

    public PostServiceImpl(PostMapper postMapper, UserMapper userMapper,
                           JdbcTemplate jdbcTemplate,
                           TagService tagService, PostLikeService postLikeService) {
        this.postMapper = postMapper;
        this.userMapper = userMapper;
        this.jdbcTemplate = jdbcTemplate;
        this.tagService = tagService;
        this.postLikeService = postLikeService;
    }

    @Override
    public IPage<PostVo> pageWithAuthor(int page, int size, String keyword, Long categoryId) {
        IPage<PostVo> result = postMapper.selectPageWithAuthor(
                new Page<>(page, size), keyword, categoryId);
        populateTags(result.getRecords());
        return result;
    }

    @Override
    public List<PostVo> listPopular(int limit) {
        List<PostVo> list = postMapper.selectPopular(limit);
        populateTags(list);
        return list;
    }

    @Override
    public PostVo getDetail(Long id, Long currentUserId) {
        Post post = getById(id);
        if (post == null || post.getStatus() == 0) return null;
        post.setViewCount(post.getViewCount() + 1);
        updateById(post);
        User author = userMapper.selectById(post.getAuthorId());
        PostVo vo = toVo(post, author);
        vo.setTagNames(postMapper.selectTagsByPostId(id));
        vo.setLikeCount(postLikeService.likeCount(id));
        vo.setIsLiked(postLikeService.isLiked(id, currentUserId));
        return vo;
    }

    @Override
    public PostVo createPost(Post post, List<String> tagNames, Long userId) {
        post.setAuthorId(userId);
        post.setViewCount(0);
        save(post);
        syncTags(post.getId(), tagNames);
        User author = userMapper.selectById(userId);
        PostVo vo = toVo(post, author);
        vo.setTagNames(tagNames != null ? tagNames : List.of());
        return vo;
    }

    @Override
    public PostVo updatePost(Long id, Post updated, List<String> tagNames, Long userId, boolean isAdmin) {
        Post post = getById(id);
        if (post == null) return null;
        if (!isAdmin && !post.getAuthorId().equals(userId)) {
            throw new RuntimeException("无权修改此文章");
        }
        post.setTitle(updated.getTitle());
        post.setContent(updated.getContent());
        post.setCategoryId(updated.getCategoryId());
        post.setCoverImage(updated.getCoverImage());
        post.setStatus(updated.getStatus());
        updateById(post);
        syncTags(id, tagNames);
        User author = userMapper.selectById(post.getAuthorId());
        PostVo vo = toVo(post, author);
        vo.setTagNames(tagNames != null ? tagNames : List.of());
        return vo;
    }

    @Override
    public void deletePost(Long id, Long userId, boolean isAdmin) {
        Post post = getById(id);
        if (post != null && (isAdmin || post.getAuthorId().equals(userId))) {
            jdbcTemplate.update("DELETE FROM post_tag WHERE post_id = ?", id);
            removeById(id);
        }
    }

    @Override
    public IPage<PostVo> adminPage(int page, int size, String keyword, Long userId) {
        IPage<PostVo> result = postMapper.selectAdminPage(
                new Page<>(page, size), keyword, userId);
        populateTags(result.getRecords());
        return result;
    }

    @Override
    public void adminDelete(Long id) {
        jdbcTemplate.update("DELETE FROM post_tag WHERE post_id = ?", id);
        removeById(id);
    }

    // ===== 内部工具 =====

    private void syncTags(Long postId, List<String> tagNames) {
        jdbcTemplate.update("DELETE FROM post_tag WHERE post_id = ?", postId);
        if (tagNames == null || tagNames.isEmpty()) return;
        List<Tag> tags = tagService.listOrCreate(tagNames);
        for (Tag tag : tags) {
            jdbcTemplate.update("INSERT IGNORE INTO post_tag (post_id, tag_id) VALUES (?, ?)",
                    postId, tag.getId());
        }
    }

    private void populateTags(List<PostVo> posts) {
        if (posts == null || posts.isEmpty()) return;
        List<Long> postIds = posts.stream().map(PostVo::getId).collect(Collectors.toList());
        List<Map<String, Object>> rows = postMapper.selectTagsByPostIds(postIds);
        // rows: [{post_id=1, name=Vue}, {post_id=1, name=JavaScript}, ...]
        Map<Long, List<String>> tagMap = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Long pid = ((Number) row.get("post_id")).longValue();
            String name = (String) row.get("name");
            tagMap.computeIfAbsent(pid, k -> new ArrayList<>()).add(name);
        }
        for (PostVo vo : posts) {
            vo.setTagNames(tagMap.getOrDefault(vo.getId(), List.of()));
            vo.setLikeCount(postLikeService.likeCount(vo.getId()));
        }
    }

    private PostVo toVo(Post p, User u) {
        return PostVo.builder()
            .id(p.getId()).title(p.getTitle()).content(p.getContent())
            .authorId(p.getAuthorId())
            .authorName(u != null ? u.getNickname() : "Unknown")
            .authorAvatar(u != null ? u.getAvatar() : null)
            .categoryId(p.getCategoryId())
            .coverImage(p.getCoverImage()).viewCount(p.getViewCount())
            .status(p.getStatus())
            .createTime(p.getCreateTime()).updateTime(p.getUpdateTime())
            .build();
    }
}
