package com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("comment")
public class Comment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long postId;
    private Long userId;
    private String guestName;
    private String content;
    private Long parentId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
