package com.blog.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentVo {
    private Long id;
    private Long postId;
    private Long userId;
    private String username;
    private String avatar;
    private String guestName;
    private String content;
    private Long parentId;
    private LocalDateTime createTime;
    private List<CommentVo> replies;
}
