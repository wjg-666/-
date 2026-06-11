package com.blog.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
        String ext = getExtension(file.getOriginalFilename());
        if (!ext.matches("jpg|jpeg|png|gif|webp")) {
            return ResponseEntity.badRequest().body(Map.of("message", "不支持的图片格式"));
        }
        Files.createDirectories(Paths.get(uploadPath));
        String filename = UUID.randomUUID() + "." + ext;
        Path dest = Paths.get(uploadPath, filename);
        file.transferTo(dest.toFile());
        return ResponseEntity.ok(Map.of("url", "/uploads/" + filename));
    }

    private String getExtension(String name) {
        if (name == null) return "";
        int i = name.lastIndexOf('.');
        return i == -1 ? "" : name.substring(i + 1).toLowerCase();
    }
}
