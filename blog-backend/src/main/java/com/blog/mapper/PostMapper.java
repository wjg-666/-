package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.entity.Post;
import com.blog.vo.PostVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PostMapper extends BaseMapper<Post> {

    @Select("""
        SELECT p.*, u.nickname as author_name, u.avatar as author_avatar
        FROM post p LEFT JOIN user u ON p.author_id = u.id
        WHERE p.status = 1
        AND (#{keyword} IS NULL OR p.title LIKE CONCAT('%', #{keyword}, '%'))
        ORDER BY p.create_time DESC
    """)
    IPage<PostVo> selectPageWithAuthor(Page<PostVo> page, @Param("keyword") String keyword);

    @Select("""
        <script>
        SELECT p.*, u.nickname as author_name, u.avatar as author_avatar
        FROM post p LEFT JOIN user u ON p.author_id = u.id
        WHERE 1=1
        <if test='keyword != null and keyword != \"\"'>
            AND p.title LIKE CONCAT('%', #{keyword}, '%')
        </if>
        <if test='userId != null'>
            AND p.author_id = #{userId}
        </if>
        ORDER BY p.create_time DESC
        </script>
    """)
    IPage<PostVo> selectAdminPage(Page<PostVo> page, @Param("keyword") String keyword, @Param("userId") Long userId);
}
