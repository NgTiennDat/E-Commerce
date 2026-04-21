package com.eCommerce.product.seeder;

import com.eCommerce.product.model.entity.Category;
import com.eCommerce.product.model.entity.Product;
import com.eCommerce.product.model.enums.ProductStatus;
import com.eCommerce.product.repository.CategoryRepository;
import com.eCommerce.product.repository.ProductRepository;
import com.eCommerce.product.seeder.client.DummyJsonClient;
import com.eCommerce.product.seeder.dto.DummyJsonResponse.DummyJsonProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "seeder.enabled", havingValue = "true")
public class DataSeeder implements ApplicationRunner {

    private final DummyJsonClient dummyJsonClient;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Value("${seeder.enabled:false}")
    private boolean enabled;

    @Value("${seeder.dummyjson.limit:194}")
    private int limit;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled) {
            log.info("DataSeeder is disabled. Set seeder.enabled=true to seed data.");
            return;
        }
        if (productRepository.count() > 0) {
            log.info("Database already has products. Skipping seeder.");
            return;
        }

        List<DummyJsonProduct> products = dummyJsonClient.fetchProducts(limit).products();
        if (products == null || products.isEmpty()) {
            log.warn("No products fetched from DummyJSON.");
            return;
        }

        Map<String, Category> categoryCache = new HashMap<>();
        int savedCount = 0;

        for (DummyJsonProduct dto : products) {
            try {
                Category category = categoryCache.computeIfAbsent(
                        dto.category(), this::findOrCreateCategory
                );
                if (!productRepository.existsBySkuAndIsDeletedFalse(buildSku(dto))) {
                    productRepository.save(toProduct(dto, category));
                    savedCount++;
                }
            } catch (Exception e) {
                log.warn("Failed to seed product '{}': {}", dto.title(), e.getMessage());
            }
        }

        log.info("DataSeeder completed. Saved {} products.", savedCount);
    }

    private Category findOrCreateCategory(String rawName) {
        String slug = toSlug(rawName);
        return categoryRepository.findBySlugAndIsDeletedFalse(slug)
                .orElseGet(() -> categoryRepository.save(
                        Category.builder()
                                .name(capitalize(rawName))
                                .slug(slug)
                                .description(capitalize(rawName) + " category")
                                .isActive(true)
                                .displayOrder(0)
                                .build()
                ));
    }

    private Product toProduct(DummyJsonProduct dto, Category category) {
        return Product.builder()
                .sku(buildSku(dto))
                .name(dto.title())
                .shortDescription(truncate(dto.description(), 255))
                .description(dto.description())
                .price(BigDecimal.valueOf(dto.price()).setScale(2, RoundingMode.HALF_UP))
                .discountPercent(dto.discountPercentage() != null ? dto.discountPercentage().intValue() : 0)
                .availableQuantity(dto.stock() != null ? dto.stock() : 0)
                .imageUrl(dto.thumbnail())
                .brand(dto.brand() != null ? dto.brand() : "Unknown")
                .rating(dto.rating())
                .ratingCount(0)
                .isFeatured(false)
                .isNew(true)
                .status(ProductStatus.ACTIVE)
                .category(category)
                .build();
    }

    private String buildSku(DummyJsonProduct dto) {
        return "DUMMY-" + dto.id();
    }

    private String toSlug(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return Pattern.compile("[^\\p{ASCII}]").matcher(normalized).replaceAll("")
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }

    private String capitalize(String input) {
        if (input == null || input.isBlank()) return input;
        return Character.toUpperCase(input.charAt(0)) + input.substring(1);
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return null;
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
