package com.blog.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVo {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private Integer role;
    private LocalDateTime createTime;
}
