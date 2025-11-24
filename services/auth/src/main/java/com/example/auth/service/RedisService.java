package com.example.auth.service;

import com.example.auth.entity.RefreshToken;

import java.util.concurrent.TimeUnit;

public interface RedisService {
    void setValue(String key, Object value, long timeout, TimeUnit unit);

    Object getValue(String key);

    void delete(String key);

    void cacheRefreshToken(RefreshToken refreshToken);

    RefreshToken getCacheRefreshToken(String refreshTokenId);

    void deleteCacheToken(String refreshTokenId);

}
