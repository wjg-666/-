package com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterDto {
    @NotBlank @Size(min = 3, max = 50)
    private String username;
    @NotBlank @Size(min = 6)
    private String password;
    private String nickname;
}
