package com.blog.controller;

import com.blog.dto.LoginDto;
import com.blog.dto.RegisterDto;
import com.blog.dto.UpdateProfileDto;
import com.blog.entity.User;
import com.blog.service.UserService;
import com.blog.utils.CurrentUser;
import com.blog.utils.JwtUtils;
import com.blog.utils.Result;
import com.blog.vo.UserVo;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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
    public ResponseEntity<Result> register(@Valid @RequestBody RegisterDto dto) {
        if (userService.getByUsername(dto.getUsername()) != null) {
            return ResponseEntity.ok(Result.error("用户名已存在"));
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setRole(1);
        user.setStatus(1);
        userService.save(user);
        return ResponseEntity.ok(Result.ok("注册成功"));
    }

    @PostMapping("/login")
    public ResponseEntity<Result> login(@Valid @RequestBody LoginDto dto) {
        User user = userService.getByUsername(dto.getUsername());
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            return ResponseEntity.ok(Result.unauthorized("用户名或密码错误"));
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            return ResponseEntity.ok(Result.forbidden("账号已被禁用"));
        }
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());

        UserVo userVo = UserVo.builder()
            .id(user.getId())
            .username(user.getUsername())
            .nickname(user.getNickname())
            .avatar(user.getAvatar())
            .role(user.getRole())
            .createTime(user.getCreateTime())
            .build();

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", userVo);
        return ResponseEntity.ok(Result.ok("登录成功", data));
    }

    @GetMapping("/info")
    public ResponseEntity<Result> info() {
        Long userId = currentUser.getUserId();
        User user = userService.getById(userId);
        if (user == null) {
            return ResponseEntity.ok(Result.notFound("用户不存在"));
        }
        UserVo userVo = UserVo.builder()
            .id(user.getId()).username(user.getUsername())
            .nickname(user.getNickname()).avatar(user.getAvatar())
            .role(user.getRole()).createTime(user.getCreateTime()).build();
        return ResponseEntity.ok(Result.ok(userVo));
    }

    @PutMapping("/profile")
    public ResponseEntity<Result> updateProfile(@Valid @RequestBody UpdateProfileDto dto) {
        Long userId = currentUser.getUserId();
        User user = userService.getById(userId);
        if (user == null) {
            return ResponseEntity.ok(Result.notFound("用户不存在"));
        }
        if (dto.getNickname() != null) {
            user.setNickname(dto.getNickname());
        }
        if (dto.getAvatar() != null) {
            user.setAvatar(dto.getAvatar());
        }
        userService.updateById(user);
        UserVo userVo = UserVo.builder()
            .id(user.getId()).username(user.getUsername())
            .nickname(user.getNickname()).avatar(user.getAvatar())
            .role(user.getRole()).createTime(user.getCreateTime()).build();
        return ResponseEntity.ok(Result.ok("更新成功", userVo));
    }
}
