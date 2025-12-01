package com.eCommerce.product.service.impl;

import com.eCommerce.common.exception.CustomException;
import com.eCommerce.common.payload.ResponseCode;
import com.eCommerce.product.model.Category;
import com.eCommerce.product.model.Product;
import com.eCommerce.product.model.dto.CategoryDto;
import com.eCommerce.product.model.request.ProductPurchaseRequest;
import com.eCommerce.product.model.request.ProductRequest;
import com.eCommerce.product.model.response.ProductPurchaseResponse;
import com.eCommerce.product.model.response.ProductResponse;
import com.eCommerce.product.repository.ProductRepository;
import com.eCommerce.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    public Integer addProduct(ProductRequest request) {
        try {
            Product product = new Product();
            product.setName(request.getName());
            product.setDescription(request.getDescription());
            product.setAvailableQuantity(request.getAvailableQuantity());
            product.setPrice(request.getPrice());
            product.setCategory(request.getCategory());

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
     * - Kiểm tra tất cả productId có tồn tại không
     * - Kiểm tra tồn kho từng sản phẩm
     * - Trừ tồn kho và lưu lại
     * - Trả về danh sách sản phẩm đã mua (kèm số lượng mua, giá,...)
     */
    @Override
    @Transactional(rollbackFor = CustomException.class)
    public List<ProductPurchaseResponse> purchaseProduct(List<ProductPurchaseRequest> request) {
        try {
            if (request == null || request.isEmpty()) {
                return List.of();
            }

            // Lấy list id cần mua
            List<Integer> productIds = request.stream()
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

                // Build response: quantity = SỐ LƯỢNG MUA,
                // nếu bạn muốn quantity = tồn kho mới thì đổi thành newAvailableQuantity
                ProductPurchaseResponse response = new ProductPurchaseResponse();
                response.setProductId(product.getId());
                response.setName(product.getName());
                response.setDescription(product.getDescription());
                response.setQuantity(productRequest.getQuantity());
                response.setPrice(product.getPrice());

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

    @Override
    public ProductResponse getProductDetail(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ResponseCode.PRODUCT_NOT_FOUND));

        return mapToProductResponse(product);
    }

    @Override
    public List<ProductResponse> getAllProductInCategory(Integer categoryId) {
        List<Product> products = productRepository.findAllByCategoryId(categoryId);
        return products.stream()
                .map(this::mapToProductResponse)
                .toList();
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAll();
        log.info("Total products found: {}", products.size());
        return products.stream()
                .map(this::mapToProductResponse)
                .toList();
    }

    // ================== helper ==================

    private ProductResponse mapToProductResponse(Product product) {
        ProductResponse dto = new ProductResponse();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setAvailableQuantity(product.getAvailableQuantity());
        dto.setPrice(product.getPrice());

        Category category = product.getCategory();
        if (category != null) {
            CategoryDto categoryDto = new CategoryDto();
            categoryDto.setId(category.getId());
            categoryDto.setName(category.getName());
            categoryDto.setDescription(category.getDescription());
            dto.setCategoryDto(categoryDto);
        }

        return dto;
    }
}
