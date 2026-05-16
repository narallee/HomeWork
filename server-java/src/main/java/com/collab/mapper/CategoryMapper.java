package com.collab.mapper;

import com.collab.model.entity.Category;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CategoryMapper {

    List<Category> findAll(@Param("type") String type);

    @Insert("INSERT INTO categories (id, name, type, icon, sort_order) VALUES (#{id}, #{name}, #{type}, #{icon}, #{sortOrder})")
    void insert(Category category);
}
