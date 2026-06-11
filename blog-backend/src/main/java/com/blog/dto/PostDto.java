package com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PostDto {
    @NotBlank private String title;
    @NotBlank private String content;
    private String coverImage;
    private Integer status;
}
