package com.blog.controller;

import com.blog.dto.LoginDto;
import com.blog.dto.RegisterDto;
import com.blog.entity.User;
import com.blog.service.UserService;
import com.blog.utils.CurrentUser;
import com.blog.utils.JwtUtils;
import com.blog.vo.UserVo;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final CurrentUser currentUser;

    public AuthController(UserService userService, PasswordEncoder passwordEncoder, JwtUtils jwtUtils, CurrentUser currentUser) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.currentUser = currentUser;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDto dto) {
        if (userService.getByUsername(dto.getUsername()) != null) {
            return ResponseEntity.badRequest().body(Map.of("message", "用户名已存在"));
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setRole(1);
        userService.save(user);
        return ResponseEntity.ok(Map.of("message", "注册成功"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDto dto) {
        User user = userService.getByUsername(dto.getUsername());
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("message", "用户名或密码错误"));
        }
        if (user.getStatus() == 0) {
            return ResponseEntity.status(403).body(Map.of("message", "账号已被禁用"));
        }
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
        return ResponseEntity.ok(Map.of(
            "token", token,
            "user", UserVo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .createTime(user.getCreateTime())
                .build()
        ));
    }

    @GetMapping("/info")
    public ResponseEntity<?> info() {
        Long userId = currentUser.getUserId();
        User user = userService.getById(userId);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("message", "用户不存在"));
        }
        return ResponseEntity.ok(UserVo.builder()
            .id(user.getId()).username(user.getUsername())
            .nickname(user.getNickname()).avatar(user.getAvatar())
            .role(user.getRole()).createTime(user.getCreateTime()).build());
    }
}
