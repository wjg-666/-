package com.blog.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileDto {
    @Size(min = 1, max = 50, message = "昵称长度1-50字符")
    private String nickname;
    private String avatar;
}
