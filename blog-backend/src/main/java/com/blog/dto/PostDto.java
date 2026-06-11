package com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class PostDto {
    @NotBlank private String title;
    @NotBlank private String content;
    private Long categoryId;
    private String coverImage;
    private Integer status;
    private List<String> tagNames;
}
