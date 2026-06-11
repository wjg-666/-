package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.entity.Post;
import com.blog.vo.PostVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PostMapper extends BaseMapper<Post> {

    @Select("""
        <script>
        SELECT p.*, u.nickname as author_name, u.avatar as author_avatar,
               c.name as category_name
        FROM post p
        LEFT JOIN user u ON p.author_id = u.id
        LEFT JOIN category c ON p.category_id = c.id
        WHERE p.status = 1
        <if test='keyword != null and keyword != \"\"'>
            AND p.title LIKE CONCAT('%', #{keyword}, '%')
        </if>
        <if test='categoryId != null'>
            AND p.category_id = #{categoryId}
        </if>
        ORDER BY p.create_time DESC
        </script>
    """)
    IPage<PostVo> selectPageWithAuthor(Page<PostVo> page,
                                       @Param("keyword") String keyword,
                                       @Param("categoryId") Long categoryId);

    @Select("""
        SELECT p.*, u.nickname as author_name, u.avatar as author_avatar,
               c.name as category_name
        FROM post p
        LEFT JOIN user u ON p.author_id = u.id
        LEFT JOIN category c ON p.category_id = c.id
        WHERE p.status = 1
        ORDER BY p.view_count DESC
        LIMIT #{limit}
    """)
    List<PostVo> selectPopular(@Param("limit") int limit);

    @Select("""
        SELECT t.name FROM tag t
        INNER JOIN post_tag pt ON t.id = pt.tag_id
        WHERE pt.post_id = #{postId}
    """)
    List<String> selectTagsByPostId(@Param("postId") Long postId);

    @Select("""
        <script>
        SELECT pt.post_id, t.name FROM tag t
        INNER JOIN post_tag pt ON t.id = pt.tag_id
        WHERE pt.post_id IN
        <foreach collection='postIds' item='id' open='(' separator=',' close=')'>
            #{id}
        </foreach>
        </script>
    """)
    List<java.util.Map<String, Object>> selectTagsByPostIds(@Param("postIds") java.util.List<Long> postIds);

    @Select("""
        <script>
        SELECT p.*, u.nickname as author_name, u.avatar as author_avatar,
               c.name as category_name
        FROM post p
        LEFT JOIN user u ON p.author_id = u.id
        LEFT JOIN category c ON p.category_id = c.id
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
