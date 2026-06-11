package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.entity.Comment;
import com.blog.vo.CommentVo;
import java.util.List;

public interface CommentService extends IService<Comment> {
    List<CommentVo> getByPostId(Long postId);
    CommentVo addComment(Comment comment);
    void deleteComment(Long id);
}
