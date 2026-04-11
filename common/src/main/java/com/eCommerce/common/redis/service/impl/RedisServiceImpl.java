package com.eCommerce.common.redis.service.impl;

import com.eCommerce.common.redis.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void setValue(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * Trả về null nếu key không tồn tại trong Redis.
     * Caller tự kiểm tra null trước khi dùng.
     *
     * Tại sao bỏ Objects.requireNonNull()?
     * requireNonNull() throw NPE ngay tại đây — caller không có cơ hội
     * xử lý trường hợp "key không tồn tại" theo business logic riêng.
     * Trả về null là contract rõ ràng hơn: "có thể không có giá trị".
     */
    @Override
    public Object getValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public boolean exists(String key) {
        Boolean result = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(result);
    }

    @Override
    public Long getTtl(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }
}
