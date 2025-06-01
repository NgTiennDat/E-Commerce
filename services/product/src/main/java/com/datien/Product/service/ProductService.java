package com.datien.Product.service;

import com.datien.Product.exception.ProductPurchaseException;
import com.datien.Product.model.Category;
import com.datien.Product.model.Product;
import com.datien.Product.model.dto.CategoryDto;
import com.datien.Product.model.request.ProductPurchaseRequest;
import com.datien.Product.model.request.ProductRequest;
import com.datien.Product.model.response.ProductPurchaseResponse;
import com.datien.Product.model.response.ProductResponse;
import com.datien.Product.repository.ProductRepository;
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
public class ProductService {

    private final ProductRepository productRepository;

    public Integer addProduct(ProductRequest request) {
        var product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setAvailableQuantity(request.getAvailableQuantity());
        product.setPrice(request.getPrice());
        product.setCategory(request.getCategory());
        return null;
    }

    @Transactional(rollbackFor = ProductPurchaseException.class)
    public List<ProductPurchaseResponse> purchaseProduct(List<ProductPurchaseRequest> request) {
        var productIds = request
                .stream()
                .map(ProductPurchaseRequest::getProductId)
                .toList();
        var storedProducts = productRepository.findAllByIdInOrderById(productIds);
        if(productIds.size() != storedProducts.size()) {
            throw new ProductPurchaseException("One or more products does not exist");
        }

        var storedRequest = request
                .stream()
                .sorted(Comparator.comparing(ProductPurchaseRequest::getProductId))
                .toList();

        var purchasedProducts = new ArrayList<ProductPurchaseResponse>();
        for(int i = 0; i < storedProducts.size(); i++) {
            var product = storedProducts.get(i);
            var productRequest = storedRequest.get(i);
            if(product.getAvailableQuantity() < productRequest.getQuantity()) {
                throw new ProductPurchaseException(
                        "Insufficient stock quantity for product with ID:: " + productRequest.getProductId()
                );
            }
            var newAvailableQuantity = product.getAvailableQuantity() - productRequest.getQuantity();
            product.setAvailableQuantity(newAvailableQuantity);
            productRepository.save(product);

            var response = new ProductPurchaseResponse();
            response.setName(product.getName());
            response.setDescription(product.getDescription());
            response.setQuantity(product.getAvailableQuantity());
            response.setPrice(product.getPrice());
            purchasedProducts.add(response);
        }
        return purchasedProducts;
    }

    public ProductResponse getProductDetail(Integer productId) {
        var product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("No product with Id: " + productId));
        ProductResponse response = new ProductResponse();
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setAvailableQuantity(product.getAvailableQuantity());
        Category category = product.getCategory();
        if (category != null) {
            CategoryDto categoryDto = new CategoryDto();
            categoryDto.setId(category.getId());
            categoryDto.setName(category.getName());
            categoryDto.setDescription(category.getDescription());
            response.setCategoryDto(categoryDto);
        }
        return response;
    }

    public List<ProductResponse> getAllProductInCategory(Integer categoryId) {
        return null;
    }

    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream().map(product -> {
            ProductResponse dto = new ProductResponse();
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
        }).toList();
    }

}
