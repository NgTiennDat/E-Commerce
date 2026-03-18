package com.eCommerce.product.service.impl;

import com.eCommerce.common.exception.CustomException;
import com.eCommerce.common.payload.ResponseCode;
import com.eCommerce.product.model.entity.Category;
import com.eCommerce.product.model.entity.Product;
import com.eCommerce.product.model.enumn.ProductStatus;
import com.eCommerce.product.model.projection.ProductListProjection;
import com.eCommerce.product.model.projection.RelatedProductProjection;
import com.eCommerce.product.model.request.ProductPurchaseRequest;
import com.eCommerce.product.model.request.ProductRequest;
import com.eCommerce.product.model.request.ProductSearchRequest;
import com.eCommerce.product.model.response.ProductPurchaseResponse;
import com.eCommerce.product.model.response.ProductResponse;
import com.eCommerce.product.repository.CategoryRepository;
import com.eCommerce.product.repository.ProductRepository;
import com.eCommerce.product.service.ProductService;
import com.eCommerce.product.service.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Core product orchestration.
 * Notes for future maintainers:
 *  - Concurrency: purchase flow is still vulnerable to oversell without DB locking/reservation.
 *  - Auditing: manual for now; migrate to Spring Data auditing when enabled.
 *  - Pricing: kept read-only here; calculation lives in mapper to centralize discount math.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private static final String SYSTEM_ACTOR = "SYSTEM";
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_RELATED_LIMIT = 20;
    private static final int DEFAULT_RELATED_LIMIT = 10;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    /**
     * Admin creates a product.
     * - Validates category existence and SKU uniqueness up front to fail fast.
     * - Defaults status to ACTIVE when missing to keep reads consistent.
     * - Sets audit fields explicitly (until JPA auditing is enabled).
     */
    @Override
    public Long addProduct(ProductRequest request) {
        Category category = getCategoryOrThrow(request.getCategoryId());

        if (productRepository.existsBySkuAndIsDeletedFalse(request.getSku())) {
            throw new CustomException(ResponseCode.INVALID_REQUEST);
        }

        Product product = productMapper.toEntity(request, category);
        if (product.getStatus() == null) {
            product.setStatus(ProductStatus.ACTIVE);
        }

        stampAuditOnCreate(product);

        Long id = productRepository.save(product).getId();
        log.info("Product created sku={}, id={}", product.getSku(), id);
        return id;
    }

    /**
     * Bulk purchase flow (cart checkout).
     * Transactional to keep stock deductions atomic within the service boundary.
     * Concurrency caveat: still susceptible to oversell during flash sales.
     * Replace with optimistic/pessimistic lock or a reservation service for high contention.
     */
    @Override
    @Transactional(rollbackFor = CustomException.class)
    public List<ProductPurchaseResponse> purchaseProduct(List<ProductPurchaseRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }

        List<ProductPurchaseRequest> sorted = requests.stream()
                .sorted(Comparator.comparing(ProductPurchaseRequest::getProductId))
                .toList();

        List<Long> ids = sorted.stream().map(ProductPurchaseRequest::getProductId).toList();

        // Single fetch to reduce DB round trips; order-by-id to align with sorted list.
        List<Product> products = productRepository.findAllByIdInAndIsDeletedFalseOrderById(ids);
        if (products.size() != ids.size()) {
            throw new CustomException(ResponseCode.PRODUCT_NOT_FOUND);
        }

        List<ProductPurchaseResponse> responses = new ArrayList<>(products.size());

        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            int quantityRequested = sorted.get(i).getQuantity();

            assertPositiveQuantity(quantityRequested);
            assertStockSufficient(product, quantityRequested);

            product.setAvailableQuantity(product.getAvailableQuantity() - quantityRequested);
            responses.add(productMapper.toPurchaseResponse(product, quantityRequested));
        }

        productRepository.saveAll(products);
        log.info("Completed purchase for {} items", products.size());
        return responses;
    }

    @Override
    public ProductResponse getProductDetail(Long productId) {
        Product product = getProductOrThrow(productId);
        return productMapper.toResponse(product);
    }

    @Override
    public Page<ProductResponse> getAllProductInCategory(Long categoryId, int page, int size) {
        Pageable pageable = buildPageable(page, size);
        Page<Product> products = productRepository.findAllByCategoryIdAndIsDeletedFalse(categoryId, pageable);

        log.debug("Category {} contains {} products", categoryId, products.getTotalElements());
        return products.map(productMapper::toResponse);
    }

    @Override
    public Page<ProductResponse> getProducts(ProductSearchRequest request) {
        Pageable pageable = buildPageable(request.getPage(), request.getSize());

        Page<ProductListProjection> projectionPage = productRepository.searchProducts(
                request.getKeyword(),
                request.getCategoryName(),
                request.getStatus() != null ? request.getStatus().name() : null,
                request.getMinPrice(),
                request.getMaxPrice(),
                request.getBrand(),
                request.getIsFeatured(),
                request.getIsNew(),
                pageable
        );

        return projectionPage.map(productMapper::mapToProductResponse);
    }

    /**
     * Admin updates product attributes (no price rules enforced here).
     * Note: consider moving price-change policies and audit logging to a dedicated domain service.
     */
    @Override
    public void updateProduct(Long productId, ProductRequest request) {
        Product product = getProductOrThrow(productId);

        if (!product.getSku().equals(request.getSku()) &&
                productRepository.existsBySkuAndIsDeletedFalse(request.getSku())) {
            throw new CustomException(ResponseCode.INVALID_REQUEST);
        }

        product.setSku(request.getSku());
        product.setName(request.getName());
        product.setShortDescription(request.getShortDescription());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setDiscountPercent(request.getDiscountPercent());
        product.setAvailableQuantity(request.getAvailableQuantity());
        product.setImageUrl(request.getImageUrl());
        product.setBrand(request.getBrand());
        product.setIsFeatured(request.getIsFeatured());
        product.setIsNew(request.getIsNew());

        stampAuditOnUpdate(product);
        productRepository.save(product);
        log.info("Product updated id={}", productId);
    }

    @Override
    public void deleteProduct(Long productId) {
        Product product = getProductOrThrow(productId);
        product.setIsDeleted(true);

        stampAuditOnUpdate(product);
        productRepository.save(product);
        log.info("Product soft-deleted id={}", productId);
    }

    @Override
    public List<ProductResponse> getRelatedProducts(Long productId, int limit) {
        Product product = getProductOrThrow(productId);

        Long categoryId = product.getCategory().getId();
        int size = normalizeLimit(limit);

        List<RelatedProductProjection> projections =
                productRepository.findRelatedProductsNative(categoryId, productId, size);

        return projections.stream()
                .map(productMapper::fromRelatedProjection)
                .toList();
    }

    @Override
    public void updateProductStatus(Long productId, ProductStatus status) {
        Product product = getProductOrThrow(productId);
        product.setStatus(status);

        stampAuditOnUpdate(product);
        productRepository.save(product);
        log.info("Product status updated id={} status={}", productId, status);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private Product getProductOrThrow(Long id) {
        return productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException(ResponseCode.PRODUCT_NOT_FOUND));
    }

    private Category getCategoryOrThrow(Long id) {
        return categoryRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException(ResponseCode.CATEGORY_NOT_FOUND));
    }

    private Pageable buildPageable(int page, int size) {
        int safePage = Math.max(page, DEFAULT_PAGE);
        int safeSize = (size <= 0) ? DEFAULT_SIZE : size;
        return PageRequest.of(safePage, safeSize);
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_RELATED_LIMIT;
        }
        return Math.min(limit, MAX_RELATED_LIMIT);
    }

    private void assertPositiveQuantity(int quantity) {
        if (quantity <= 0) {
            throw new CustomException(ResponseCode.INVALID_REQUEST);
        }
    }

    private void assertStockSufficient(Product product, int quantityRequested) {
        if (product.getAvailableQuantity() < quantityRequested) {
            throw new CustomException(ResponseCode.PRODUCT_QUANTITY_NOT_ENOUGH);
        }
    }

    private void stampAuditOnCreate(Product product) {
        product.setCreatedAt(LocalDateTime.now());
        product.setCreatedBy(SYSTEM_ACTOR);
        stampAuditOnUpdate(product);
    }

    private void stampAuditOnUpdate(Product product) {
        product.setUpdatedAt(LocalDateTime.now());
        product.setUpdatedBy(SYSTEM_ACTOR);
    }
}
