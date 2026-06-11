package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.entity.Comment;
import com.blog.vo.CommentVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    @Select("""
        SELECT c.*, u.nickname as username, u.avatar
        FROM comment c LEFT JOIN user u ON c.user_id = u.id
        WHERE c.post_id = #{postId}
        ORDER BY c.create_time ASC
    """)
    List<CommentVo> selectByPostId(Long postId);

    @Select("""
        SELECT c.*, u.nickname as username, u.avatar
        FROM comment c LEFT JOIN user u ON c.user_id = u.id
        ORDER BY c.create_time DESC
    """)
    IPage<CommentVo> selectAdminPage(Page<CommentVo> page);
}
