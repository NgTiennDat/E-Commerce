package com.eCommerce.auth.repository;

import com.eCommerce.auth.model.entity.User;
import com.eCommerce.auth.model.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserInfoRepository extends JpaRepository<UserInfo,Long> {

    Optional<UserInfo> findByUser(User user);
}
