package com.eCommerce.auth.service;

import com.eCommerce.auth.model.entity.RefreshToken;

public interface RefreshTokenService {

    void cacheRefreshToken(RefreshToken refreshToken);

    RefreshToken getCacheRefreshToken(String refreshTokenId);

    void deleteCacheToken(String refreshTokenId);

}
