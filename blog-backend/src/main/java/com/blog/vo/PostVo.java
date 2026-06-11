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
public class PostVo {
    private Long id;
    private String title;
    private String content;
    private Long authorId;
    private String authorName;
    private String authorAvatar;
    private Long categoryId;
    private String categoryName;
    private String coverImage;
    private Integer viewCount;
    private Integer status;
    private Integer likeCount;
    private Boolean isLiked;
    private List<String> tagNames;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
