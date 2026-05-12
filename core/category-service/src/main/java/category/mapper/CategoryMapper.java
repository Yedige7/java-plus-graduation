package category.mapper;

import category.dto.NewCategoryDto;
import category.model.Category;
import common.dto.CategoryDto;

public final class CategoryMapper {
    private CategoryMapper() {
    }

    public static Category toEntity(NewCategoryDto dto) {
        Category category = new Category();
        category.setName(dto.getName());
        return category;
    }

    public static CategoryDto toDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        return dto;
    }
}
