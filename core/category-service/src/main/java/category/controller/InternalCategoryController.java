package category.controller;

import category.mapper.CategoryMapper;
import category.model.Category;
import category.repository.CategoryRepository;
import common.dto.CategoryDto;
import common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/categories")
public class InternalCategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping("/{id}")
    public CategoryDto get(@PathVariable Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found: " + id));

        return CategoryMapper.toDto(category);
    }

    @GetMapping("/{id}/exists")
    public boolean exists(@PathVariable Long id) {
        return categoryRepository.existsById(id);
    }
}