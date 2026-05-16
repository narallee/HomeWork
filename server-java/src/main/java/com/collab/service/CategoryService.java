package com.collab.service;

import com.collab.mapper.CategoryMapper;
import com.collab.model.entity.Category;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CategoryService {

    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    public List<Category> getCategories(String type) {
        return categoryMapper.findAll(type);
    }

    public String addCategory(String name, String type, String icon) {
        Category category = new Category();
        category.setId(UUID.randomUUID().toString());
        category.setName(name);
        category.setType(type);
        category.setIcon(icon != null ? icon : "default");
        category.setSortOrder(99);
        categoryMapper.insert(category);
        return category.getId();
    }
}
