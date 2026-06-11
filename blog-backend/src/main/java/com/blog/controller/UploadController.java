package com.blog.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Value("${app.upload.path}")
    private String uploadPath;

    @PostMapping
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "文件为空"));
        }
        String originalName = file.getOriginalFilename();
        String ext = getExtension(originalName);
        if (!ext.matches("jpg|jpeg|png|gif|webp")) {
            return ResponseEntity.badRequest().body(Map.of("message", "不支持的图片格式"));
        }

        // 转为绝对路径，解决启动目录不同导致的上传失败问题
        Path uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
        Files.createDirectories(uploadDir);

        String filename = UUID.randomUUID() + "." + ext;
        Path dest = uploadDir.resolve(filename);
        file.transferTo(dest.toFile());

        return ResponseEntity.ok(Map.of("url", "/uploads/" + filename));
    }

    private String getExtension(String name) {
        if (name == null) return "";
        int i = name.lastIndexOf('.');
        return i == -1 ? "" : name.substring(i + 1).toLowerCase();
    }
}
