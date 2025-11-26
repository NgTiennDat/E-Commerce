package com.eCommerce.auth.service.impl;

import com.eCommerce.auth.model.entity.RefreshToken;
import com.eCommerce.auth.service.RedisService;
import com.eCommerce.common.exception.CustomException;
import com.eCommerce.common.payload.ResponseCode;
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
    private static final String REFRESH_TOKEN_PREFIX = "REDIS_KEY:";

    @Override
    public void setValue(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    @Override
    public Object getValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public void cacheRefreshToken(RefreshToken refreshToken) {
        try {
            String key = REFRESH_TOKEN_PREFIX + refreshToken.getId();
            long ttl = refreshToken.getExpiredAt().toEpochSecond(java.time.ZoneOffset.UTC)
                    - java.time.Instant.now().getEpochSecond();

            redisTemplate.opsForValue().set(key, refreshToken, ttl, TimeUnit.SECONDS);
        } catch (RuntimeException e) {
            throw new CustomException(ResponseCode.CACHE_FAILED);
        }
    }

    /**
     * Retrieves a cached RefreshToken by its ID.
     *
     * @param refreshTokenId the ID of the RefreshToken to retrieve
     * @return the cached RefreshToken, or null if not found
     */
    @Override
    public RefreshToken getCacheRefreshToken(String refreshTokenId) {
        try {
            String key = REFRESH_TOKEN_PREFIX + refreshTokenId;
            Object cachedValue = redisTemplate.opsForValue().get(key);
            return cachedValue instanceof RefreshToken ? (RefreshToken) cachedValue : null;
        }  catch (RuntimeException e) {
            log.error(e.getMessage());
            throw new CustomException(ResponseCode.CACHE_FAILED);
        }
    }

    /**
     * Delete a cached RefreshToken by its IDs
     * @param refreshTokenId the ID of the RefreshToken to retrieve
     */
    @Override
    public void deleteCacheToken(String refreshTokenId) {
        try {
            redisTemplate.delete(REFRESH_TOKEN_PREFIX + refreshTokenId);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CustomException(ResponseCode.CACHE_FAILED);
        }
    }
}
