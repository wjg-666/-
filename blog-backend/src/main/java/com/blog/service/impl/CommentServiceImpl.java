package com.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.entity.Comment;
import com.blog.mapper.CommentMapper;
import com.blog.service.CommentService;
import com.blog.vo.CommentVo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private final CommentMapper commentMapper;

    public CommentServiceImpl(CommentMapper commentMapper) {
        this.commentMapper = commentMapper;
    }

    @Override
    public List<CommentVo> getByPostId(Long postId) {
        List<CommentVo> all = commentMapper.selectByPostId(postId);
        Map<Long, List<CommentVo>> repliesMap = all.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(CommentVo::getParentId));
        List<CommentVo> roots = all.stream()
                .filter(c -> c.getParentId() == null)
                .collect(Collectors.toList());
        for (CommentVo root : roots) {
            setReplies(root, repliesMap);
        }
        return roots;
    }

    private void setReplies(CommentVo parent, Map<Long, List<CommentVo>> repliesMap) {
        List<CommentVo> replies = repliesMap.getOrDefault(parent.getId(), new ArrayList<>());
        parent.setReplies(replies);
        for (CommentVo reply : replies) {
            setReplies(reply, repliesMap);
        }
    }

    @Override
    public CommentVo addComment(Comment comment) {
        save(comment);
        return CommentVo.builder()
            .id(comment.getId()).postId(comment.getPostId())
            .userId(comment.getUserId()).guestName(comment.getGuestName())
            .content(comment.getContent()).parentId(comment.getParentId())
            .createTime(comment.getCreateTime()).replies(new ArrayList<>())
            .build();
    }

    @Override
    public void deleteComment(Long id) {
        List<Long> toDelete = new ArrayList<>();
        toDelete.add(id);
        collectChildren(id, toDelete);
        removeByIds(toDelete);
    }

    private void collectChildren(Long parentId, List<Long> ids) {
        List<Comment> children = lambdaQuery().eq(Comment::getParentId, parentId).list();
        for (Comment c : children) {
            ids.add(c.getId());
            collectChildren(c.getId(), ids);
        }
    }
}
