package com.blog.controller;

import com.blog.entity.Category;
import com.blog.service.CategoryService;
import com.blog.utils.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/categories")
    public ResponseEntity<Result> list() {
        List<Category> list = categoryService.list();
        return ResponseEntity.ok(Result.ok(list));
    }

    // ---- 管理接口 ----

    @PostMapping("/admin/categories")
    public ResponseEntity<Result> create(@RequestBody Category category) {
        categoryService.save(category);
        return ResponseEntity.ok(Result.ok("创建成功", category));
    }

    @PutMapping("/admin/categories/{id}")
    public ResponseEntity<Result> update(@PathVariable Long id, @RequestBody Category category) {
        Category existing = categoryService.getById(id);
        if (existing == null) return ResponseEntity.ok(Result.notFound("分类不存在"));
        existing.setName(category.getName());
        categoryService.updateById(existing);
        return ResponseEntity.ok(Result.ok("更新成功"));
    }

    @DeleteMapping("/admin/categories/{id}")
    public ResponseEntity<Result> delete(@PathVariable Long id) {
        categoryService.removeById(id);
        return ResponseEntity.ok(Result.ok("删除成功"));
    }
}
