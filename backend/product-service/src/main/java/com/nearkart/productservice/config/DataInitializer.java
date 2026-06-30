package com.nearkart.productservice.config;

import com.nearkart.productservice.model.Category;
import com.nearkart.productservice.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        if (categoryRepository.count() == 0) {
            List<Category> categories = List.of(
                Category.builder().name("Groceries").description("Fresh groceries and daily essentials").build(),
                Category.builder().name("Fruits & Vegetables").description("Fresh fruits and vegetables").build(),
                Category.builder().name("Dairy & Eggs").description("Milk, cheese, butter, eggs").build(),
                Category.builder().name("Bakery").description("Fresh bread, cakes, pastries").build(),
                Category.builder().name("Beverages").description("Juices, sodas, water, tea, coffee").build(),
                Category.builder().name("Snacks").description("Chips, biscuits, nuts").build(),
                Category.builder().name("Personal Care").description("Soap, shampoo, toothpaste").build(),
                Category.builder().name("Household").description("Cleaning and household supplies").build()
            );
            categoryRepository.saveAll(categories);
            log.info("Seeded {} default categories", categories.size());
        }
    }
}
