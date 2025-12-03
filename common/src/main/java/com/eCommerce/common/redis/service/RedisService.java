package com.eCommerce.common.redis.service;


import java.util.concurrent.TimeUnit;

public interface RedisService {
    void setValue(String key, Object value, long timeout, TimeUnit unit);

    Object getValue(String key);

    void delete(String key);

    boolean exists(String s);

    Long getTtl(String key);
}
