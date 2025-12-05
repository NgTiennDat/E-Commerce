package com.eCommerce.product.service.impl;

import com.eCommerce.common.exception.CustomException;
import com.eCommerce.common.payload.ResponseCode;
import com.eCommerce.product.model.entity.Category;
import com.eCommerce.product.model.entity.Product;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    /**
     * Tạo mới một sản phẩm.
     *
     * Flow:
     *  - Lấy Category theo categoryId từ request
     *  - Map ProductRequest -> Product entity
     *  - Set các field như sku, brand, price, discountPercent,...
     *  - Lưu vào database
     *  - Trả về id của sản phẩm vừa tạo
     */
    @Override
    public Long addProduct(ProductRequest request) {
        try {
            // Lấy category, nếu không tồn tại thì throw
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new CustomException(ResponseCode.CATEGORY_NOT_FOUND));

            Product product = productMapper.toEntity(request, category);
            Product saved = productRepository.save(product);
            return saved.getId();

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("[addProduct] Error: {}", e.getMessage(), e);
            throw new CustomException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Mua nhiều sản phẩm một lúc.
     *
     * Business logic:
     *  - Nhận danh sách ProductPurchaseRequest (productId + quantity)
     *  - Lấy tất cả product từ DB theo list id
     *  - Nếu số lượng record DB != số lượng request => có productId không tồn tại -> throw PRODUCT_NOT_FOUND
     *  - Sort request & product theo productId để map 1-1
     *  - Với mỗi product:
     *      + Check tồn kho (availableQuantity >= quantity)
     *      + Trừ tồn kho
     *      + Lưu product
     *      + Build ProductPurchaseResponse:
     *          * giá gốc
     *          * giá sau giảm (finalPrice)
     *          * quantity mua
     *          * availableQuantity còn lại
     *          * totalPrice = finalPrice * quantity
     */
    @Override
    @Transactional(rollbackFor = CustomException.class)
    public List<ProductPurchaseResponse> purchaseProduct(List<ProductPurchaseRequest> request) {
        try {
            if (request == null || request.isEmpty()) {
                return List.of();
            }

            // Lấy list id cần mua
            List<Long> productIds = request.stream()
                    .map(ProductPurchaseRequest::getProductId)
                    .toList();

            // Lấy sản phẩm từ DB, đã sort theo id
            List<Product> storedProducts = productRepository.findAllByIdInOrderById(productIds);

            // Nếu số record không khớp => có productId không tồn tại
            if (productIds.size() != storedProducts.size()) {
                throw new CustomException(ResponseCode.PRODUCT_NOT_FOUND);
            }

            // Sort lại request theo productId để map 1-1 với storedProducts
            List<ProductPurchaseRequest> sortedRequest = request.stream()
                    .sorted(Comparator.comparing(ProductPurchaseRequest::getProductId))
                    .toList();

            List<ProductPurchaseResponse> purchasedProducts = new ArrayList<>();

            for (int i = 0; i < storedProducts.size(); i++) {
                Product product = storedProducts.get(i);
                ProductPurchaseRequest productRequest = sortedRequest.get(i);

                // Kiểm tra tồn kho
                if (product.getAvailableQuantity() < productRequest.getQuantity()) {
                    throw new CustomException(ResponseCode.PRODUCT_QUANTITY_NOT_ENOUGH);
                }

                int newAvailableQuantity = product.getAvailableQuantity() - productRequest.getQuantity();
                product.setAvailableQuantity(newAvailableQuantity);

                productRepository.save(product);

                // Build response chi tiết cho từng sản phẩm đã mua
                ProductPurchaseResponse response = productMapper.toPurchaseResponse(product, productRequest.getQuantity());
                purchasedProducts.add(response);
            }

            return purchasedProducts;
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("[purchaseProduct] Error: {}", e.getMessage(), e);
            throw new CustomException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Lấy chi tiết 1 sản phẩm theo id.
     *
     * - Nếu không tồn tại -> throw PRODUCT_NOT_FOUND
     * - Map entity -> ProductResponse (bao gồm:
     *      giá gốc, giá sau giảm, inStock, category info,...)
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

    /**
     * Lấy tất cả sản phẩm trong 1 category (có phân trang).
     *
     * - Dùng khi FE click vào 1 category để xem danh sách sản phẩm
     * - Hỗ trợ page, size
     */
    @Override
    public Page<ProductResponse> getAllProductInCategory(Long categoryId, int page, int size) {
        try {
            int pageIndex = Math.max(page, 0);

            Pageable pageable = PageRequest.of(pageIndex, size);

            Page<Product> products = productRepository.findAllByCategoryId(categoryId, pageable);
            log.info("Total products in category {}: {}", categoryId, products.getTotalElements());

            return products.map(productMapper::toResponse);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("[getAllProductInCategory] Error: {}", e.getMessage(), e);
            throw new CustomException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Lấy tất cả sản phẩm trong hệ thống.
     *
     * - Dùng cho admin hoặc trang home (có thể filter/pagination bên ngoài)
     */
    @Override
    public Page<ProductResponse> getAllProducts(int page, int size) {
        try {
            // page từ FE thường truyền 0-based hoặc 1-based
            // Nếu bạn muốn 1-based (page=1 là trang đầu) thì:
            int pageIndex = Math.max(page, 0); // nếu FE đã 0-based thì bỏ đoạn này

            Pageable pageable = PageRequest.of(pageIndex, size);

            Page<Product> products = productRepository.findAll(pageable);
            log.info("Total products found: {}", products.getTotalElements());

            return products.map(productMapper::toResponse);

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("[getAllProducts] Error: {}", e.getMessage(), e);
            throw new CustomException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }
}
