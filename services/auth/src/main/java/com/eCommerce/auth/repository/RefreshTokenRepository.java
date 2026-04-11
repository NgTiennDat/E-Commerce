package com.eCommerce.auth.repository;

import com.eCommerce.auth.model.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    // Lấy tất cả token chưa bị revoke của 1 user — dùng cho revokeAll()
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.isRevoked = false")
    List<RefreshToken> findActiveByUserId(Long userId);

    // Bulk revoke — hiệu quả hơn load từng entity rồi save
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true WHERE rt.user.id = :userId AND rt.isRevoked = false")
    void revokeAllByUserId(Long userId);
}
