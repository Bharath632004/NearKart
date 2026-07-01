package com.nearkart.productservice.service;

import com.nearkart.productservice.dto.CategoryRequest;
import com.nearkart.productservice.exception.CategoryNotFoundException;
import com.nearkart.productservice.model.Category;
import com.nearkart.productservice.repository.CategoryRepository;
import com.nearkart.productservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @InjectMocks private ProductServiceImpl productService;

    private Category sampleCategory;
    private CategoryRequest categoryRequest;

    @BeforeEach
    void setUp() {
        sampleCategory = Category.builder()
                .id(1L)
                .name("Groceries")
                .description("Daily essentials")
                .build();

        categoryRequest = new CategoryRequest();
        categoryRequest.setName("Groceries");
        categoryRequest.setDescription("Daily essentials");
    }

    @Test
    @DisplayName("createCategory - should save and return category")
    void createCategory_shouldReturnSavedCategory() {
        when(categoryRepository.existsByNameIgnoreCase("Groceries")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(sampleCategory);

        Category result = productService.createCategory(categoryRequest);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Groceries");
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("createCategory - should throw when duplicate name")
    void createCategory_shouldThrow_whenDuplicate() {
        when(categoryRepository.existsByNameIgnoreCase("Groceries")).thenReturn(true);

        assertThatThrownBy(() -> productService.createCategory(categoryRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Groceries");
    }

    @Test
    @DisplayName("getAllCategories - should return all categories")
    void getAllCategories_shouldReturnList() {
        when(categoryRepository.findAll()).thenReturn(List.of(sampleCategory));

        List<Category> categories = productService.getAllCategories();

        assertThat(categories).hasSize(1);
        assertThat(categories.get(0).getName()).isEqualTo("Groceries");
    }

    @Test
    @DisplayName("getCategoryById - should return category when exists")
    void getCategoryById_shouldReturnCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(sampleCategory));

        Category result = productService.getCategoryById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getCategoryById - should throw when not found")
    void getCategoryById_shouldThrow_whenNotFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getCategoryById(99L))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("deleteCategory - should delete when exists")
    void deleteCategory_shouldDelete_whenExists() {
        when(categoryRepository.existsById(1L)).thenReturn(true);

        productService.deleteCategory(1L);

        verify(categoryRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("deleteCategory - should throw when not found")
    void deleteCategory_shouldThrow_whenNotFound() {
        when(categoryRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> productService.deleteCategory(99L))
                .isInstanceOf(CategoryNotFoundException.class);
    }
}
