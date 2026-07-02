package com.nearkart.productservice.config;

import com.nearkart.productservice.repository.CategoryRepository;
import com.nearkart.productservice.model.Category;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final CategoryRepository categoryRepository;

    @Bean
    @Profile("!test")
    public CommandLineRunner seedCategories() {
        return args -> {
            List<String> defaultCategories = List.of(
                "Fruits & Vegetables", "Dairy & Eggs", "Meat & Seafood",
                "Bakery", "Beverages", "Snacks", "Household", "Personal Care"
            );

            long existingCount = categoryRepository.count();
            if (existingCount > 0) {
                log.info("DataInitializer: {} categories already exist \u2014 skipping seed", existingCount);
                return;
            }

            defaultCategories.forEach(name -> {
                if (!categoryRepository.existsByNameIgnoreCase(name)) {
                    categoryRepository.save(Category.builder().name(name).build());
                    log.info("Seeded category: {}", name);
                }
            });

            log.info("DataInitializer: seeding complete");
        };
    }
}
