package com.eCommerce.order.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign client gọi Customer Service để lookup customerId theo email.
 *
 * Tại sao lookup theo email?
 * JWT chứa username và email — email là field duy nhất
 * có thể dùng để tìm Customer trong MongoDB.
 *
 * name = "customer-service" phải khớp với spring.application.name
 * trong customer-service/application.yml để Eureka resolve đúng.
 */
@FeignClient(name = "customer-service", path = "/api/v1/customer")
public interface CustomerClient {

    @GetMapping("/by-email")
    CustomerResponse findByEmail(@RequestParam("email") String email);

    @Getter
    @Setter
    class CustomerResponse {
        private String id;
        private String firstName;
        private String lastName;
        private String email;
    }
}
