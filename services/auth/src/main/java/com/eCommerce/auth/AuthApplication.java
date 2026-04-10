package com.eCommerce.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(scanBasePackages = "com.eCommerce")
@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")
@EnableCaching  // Kích hoạt Spring Cache — cần thiết để @Cacheable/@CacheEvict hoạt động
public class AuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthApplication.class, args);
	}

}
