package com.eCommerce.order.customer;

import com.eCommerce.order.client.CustomerClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Cache customerId (MongoDB ObjectId) theo email trong Redis.
 *
 * Tại sao cần cache?
 * Mỗi lần tạo order, Order Service cần customerId từ Customer Service.
 * Nếu gọi Feign mỗi lần → thêm latency + tăng load cho Customer Service.
 * customerId của 1 user không bao giờ thay đổi → cache vĩnh viễn (TTL dài).
 *
 * Key schema: order:customer:{email} → customerId
 * TTL: 24 giờ (customer profile thay đổi rất hiếm)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerCacheService {

    private static final String KEY_PREFIX = "order:customer:";
    private static final Duration TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;
    private final CustomerClient customerClient;

    /**
     * Lấy customerId theo email.
     * Cache hit → trả về ngay.
     * Cache miss → gọi Customer Service → cache kết quả → trả về.
     *
     * @throws IllegalStateException nếu không tìm thấy customer
     */
    public String getCustomerId(String email) {
        String key = KEY_PREFIX + email;

        // Cache hit
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            log.debug("Customer cache hit for email={}", email);
            return cached;
        }

        // Cache miss → gọi Customer Service
        log.debug("Customer cache miss for email={}, calling Customer Service", email);
        CustomerClient.CustomerResponse customer = customerClient.findByEmail(email);

        if (customer == null || customer.getId() == null) {
            throw new IllegalStateException("Customer not found for email: " + email);
        }

        // Lưu vào cache
        redisTemplate.opsForValue().set(key, customer.getId(), TTL);
        log.debug("Cached customerId={} for email={}", customer.getId(), email);

        return customer.getId();
    }
}
