package com.collab.controller;

import com.collab.common.Result;
import com.collab.model.dto.AddCategoryRequest;
import com.collab.model.entity.Category;
import com.collab.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * 获取分类列表
     */
    @GetMapping
    public Result<List<Category>> getCategories(@RequestParam(required = false) String type) {
        List<Category> categories = categoryService.getCategories(type);
        return Result.success(categories);
    }

    /**
     * 添加分类
     */
    @PostMapping
    public Result<Map<String, String>> addCategory(@Valid @RequestBody AddCategoryRequest body) {
        String categoryId = categoryService.addCategory(body.getName(), body.getType(), body.getIcon());
        Map<String, String> data = new HashMap<>();
        data.put("categoryId", categoryId);
        return Result.success(data, "添加成功");
    }
}
