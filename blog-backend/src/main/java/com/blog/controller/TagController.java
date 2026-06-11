package com.blog.controller;

import com.blog.entity.Tag;
import com.blog.service.TagService;
import com.blog.utils.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping("/tags")
    public ResponseEntity<Result> list() {
        List<Tag> list = tagService.list();
        return ResponseEntity.ok(Result.ok(list));
    }

    // ---- 管理接口 ----

    @PostMapping("/admin/tags")
    public ResponseEntity<Result> create(@RequestBody Tag tag) {
        tagService.save(tag);
        return ResponseEntity.ok(Result.ok("创建成功", tag));
    }

    @PutMapping("/admin/tags/{id}")
    public ResponseEntity<Result> update(@PathVariable Long id, @RequestBody Tag tag) {
        Tag existing = tagService.getById(id);
        if (existing == null) return ResponseEntity.ok(Result.notFound("标签不存在"));
        existing.setName(tag.getName());
        tagService.updateById(existing);
        return ResponseEntity.ok(Result.ok("更新成功"));
    }

    @DeleteMapping("/admin/tags/{id}")
    public ResponseEntity<Result> delete(@PathVariable Long id) {
        tagService.removeById(id);
        return ResponseEntity.ok(Result.ok("删除成功"));
    }
}
