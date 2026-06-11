package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.PostTag;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PostTagMapper extends BaseMapper<PostTag> {

    @Delete("DELETE FROM post_tag WHERE post_id = #{postId}")
    void deleteByPostId(@Param("postId") Long postId);

    @Insert("INSERT IGNORE INTO post_tag (post_id, tag_id) VALUES (#{postId}, #{tagId})")
    void insert(@Param("postId") Long postId, @Param("tagId") Long tagId);
}
