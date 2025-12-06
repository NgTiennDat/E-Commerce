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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
/*
 * Service xử lý toàn bộ business logic liên quan đến Product:
 *  - CRUD sản phẩm
 *  - Tìm kiếm + filter + pagination
 *  - Mua hàng (purchase batch)
 *  - Lấy sản phẩm theo category
 *  - Lấy sản phẩm liên quan (related items)
 *
 * NOTE:
 *  - Tất cả exception business đều dùng CustomException để đồng bộ format trả về.
 *  - Đã sử dụng Projection + Native Query để tối ưu hiệu năng.
 *  - Các trường audit (createdBy/updatedBy) đang set SYSTEM, nhưng khi tích hợp Auth thì sẽ thay bằng username thực.
 */
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    // ============================================================
    // 1. CREATE PRODUCT (ADMIN)
    // ============================================================

    /**
     * Tạo mới một product.
     *
     * Flow:
     *  1. Validate categoryId bằng CategoryRepository
     *  2. Map ProductRequest → Product entity (mapper lo)
     *  3. Set audit fields (createdBy, createdAt, ...)
     *  4. Save vào DB
     *  5. Return ID sản phẩm
     *
     * Business Rules:
     *  - SKU phải unique
     *  - Category phải tồn tại
     */
    @Override
    public Long addProduct(ProductRequest request) {
        try {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new CustomException(ResponseCode.CATEGORY_NOT_FOUND));

            Product product = productMapper.toEntity(request, category);

            product.setCreatedAt(LocalDateTime.now());
            product.setCreatedBy("SYSTEM");
            product.setUpdatedAt(LocalDateTime.now());
            product.setUpdatedBy("SYSTEM");

            Product saved = productRepository.save(product);

            return saved.getId();
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("[addProduct] Error: {}", e.getMessage(), e);
            throw new CustomException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ============================================================
    // 2. PURCHASE MULTIPLE PRODUCTS IN ONE REQUEST
    // ============================================================

    /**
     * Purchase sản phẩm hàng loạt.
     *
     * Flow:
     *  1. Validate list request != empty
     *  2. Extract list productId
     *  3. Query DB bằng findAllByIdInOrderById() (đã sorted để map 1-1)
     *  4. Validate số lượng record trả về phải == số lượng request
     *  5. Sort lại request theo productId để đảm bảo index mapping
     *  6. Với từng product:
     *        - Check tồn kho
     *        - Trừ tồn kho
     *        - Save lại vào DB
     *        - Build ProductPurchaseResponse (giá gốc, giá sau giảm, total price,…)
     *
     * Lợi ích:
     *  - Tránh gọi DB nhiều lần (1 query load tất cả)
     *  - Giảm race condition khi mua batch
     *  - Tối ưu performance
     */
    @Override
    @Transactional(rollbackFor = CustomException.class)
    public List<ProductPurchaseResponse> purchaseProduct(List<ProductPurchaseRequest> request) {
        try {
            if (request == null || request.isEmpty()) return List.of();

            List<Long> productIds = request.stream()
                    .map(ProductPurchaseRequest::getProductId)
                    .toList();

            List<Product> storedProducts = productRepository.findAllByIdInOrderById(productIds);

            if (storedProducts.size() != productIds.size()) {
                throw new CustomException(ResponseCode.PRODUCT_NOT_FOUND);
            }

            List<ProductPurchaseRequest> sortedRequest = request.stream()
                    .sorted(Comparator.comparing(ProductPurchaseRequest::getProductId))
                    .toList();

            List<ProductPurchaseResponse> purchasedProducts = new ArrayList<>();

            for (int i = 0; i < storedProducts.size(); i++) {
                Product product = storedProducts.get(i);
                ProductPurchaseRequest req = sortedRequest.get(i);

                if (product.getAvailableQuantity() < req.getQuantity()) {
                    throw new CustomException(ResponseCode.PRODUCT_QUANTITY_NOT_ENOUGH);
                }

                product.setAvailableQuantity(product.getAvailableQuantity() - req.getQuantity());
                productRepository.save(product);

                purchasedProducts.add(productMapper.toPurchaseResponse(product, req.getQuantity()));
            }

            return purchasedProducts;

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("[purchaseProduct] Error: {}", e.getMessage(), e);
            throw new CustomException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ============================================================
    // 3. PRODUCT DETAIL BY ID
    // ============================================================

    /**
     * Lấy chi tiết product theo ID.
     *
     * Bao gồm:
     *  - Category info
     *  - Final price (giá sau giảm)
     *  - inStock flag
     */
    @Override
    public ProductResponse getProductDetail(Long productId) {
        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new CustomException(ResponseCode.PRODUCT_NOT_FOUND));

            return productMapper.toResponse(product);

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("[getProductDetail] Error: {}", e.getMessage(), e);
            throw new CustomException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ============================================================
    // 4. GET PRODUCTS BY CATEGORY WITH PAGINATION
    // ============================================================

    /**
     * Lấy danh sách sản phẩm theo category kèm pagination.
     *
     * Use case:
     *  - FE click category sẽ vào đây
     *  - category listing pages
     */
    @Override
    public Page<ProductResponse> getAllProductInCategory(Long categoryId, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(Math.max(page, 0), size);

            Page<Product> products = productRepository.findAllByCategoryId(categoryId, pageable);

            log.info("Category {} contains {} products", categoryId, products.getTotalElements());

            return products.map(productMapper::toResponse);

        } catch (Exception e) {
            log.error("[getAllProductInCategory] Error: {}", e.getMessage(), e);
            throw new CustomException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ============================================================
    // 5. ADVANCED SEARCH + FILTER + PAGINATION WITH NATIVE QUERY
    // ============================================================

    /**
     * API tìm kiếm nâng cao (search + filter + sort).
     *
     * Parameters:
     *  - keyword (name, brand, description)
     *  - categoryId
     *  - status
     *  - minPrice & maxPrice
     *  - brand
     *  - isFeatured / isNew
     *
     * Notes:
     *  - Query sử dụng Projection + Native SQL => hiệu năng rất cao
     *  - Chỉ select 12 cột thay vì toàn bộ entity → nhẹ & nhanh
     */
    @Override
    public Page<ProductResponse> getProducts(
            int page,
            int size,
            String keyword,
            Long categoryId,
            ProductStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String brand,
            Boolean isFeatured,
            Boolean isNew
    ) {
        try {
            Pageable pageable = PageRequest.of(Math.max(page, 0), size);

            Page<ProductListProjection> projectionPage = productRepository.searchProducts(
                    keyword,
                    categoryId,
                    status != null ? status.name() : null,
                    minPrice,
                    maxPrice,
                    brand,
                    isFeatured,
                    isNew,
                    pageable
            );

            return projectionPage.map(productMapper::mapToProductResponse);

        } catch (Exception e) {
            log.error("[getProducts] Error: {}", e.getMessage(), e);
            throw new CustomException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ============================================================
    // 6. UPDATE PRODUCT
    // ============================================================

    /**
     * Update thông tin product.
     *
     * NOTE:
     *  - Chỉ update các trường được phép edit (không update category ở đây)
     *  - Audit updatedAt/updatedBy
     */
    @Override
    public void updateProduct(Long productId, ProductRequest request) {
        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new CustomException(ResponseCode.PRODUCT_NOT_FOUND));

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

            product.setUpdatedAt(LocalDateTime.now());
            product.setUpdatedBy("SYSTEM");

            productRepository.save(product);

        } catch (Exception e) {
            log.error("[updateProduct] Error: {}", e.getMessage(), e);
            throw new CustomException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ============================================================
    // 7. SOFT DELETE PRODUCT
    // ============================================================

    /**
     * Delete (soft delete) product.
     */
    @Override
    public void deleteProduct(Long productId) {
        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new CustomException(ResponseCode.PRODUCT_NOT_FOUND));

            product.setIsDeleted(true);
            productRepository.save(product);

        } catch (Exception e) {
            log.error("[deleteProduct] Error: {}", e.getMessage(), e);
            throw new CustomException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ============================================================
    // 8. RELATED PRODUCTS (NATIVE QUERY)
    // ============================================================

    /**
     * Lấy danh sách sản phẩm liên quan:
     *  - Cùng category
     *  - Có thể cùng brand (ưu tiên xếp hạng)
     *  - Không include chính product đang xem
     *
     * LIMIT:
     *  - FE truyền vào hoặc default = 10
     *
     * Query:
     *  - Native SQL trong ProductRepository
     *  - Select nhẹ bằng projection → siêu nhanh
     */
    @Override
    public List<ProductResponse> getRelatedProducts(Long productId, int limit) {
        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new CustomException(ResponseCode.PRODUCT_NOT_FOUND));

            Long categoryId = product.getCategory().getId();

            int size = (limit <= 0) ? 10 : Math.min(limit, 20);

            List<RelatedProductProjection> projections =
                    productRepository.findRelatedProductsNative(categoryId, productId, size);

            return projections.stream()
                    .map(productMapper::fromRelatedProjection)
                    .toList();

        } catch (Exception e) {
            log.error("[getRelatedProducts] Error: {}", e.getMessage(), e);
            throw new CustomException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }
}

