# E-Commerce Platform

Nền tảng thương mại điện tử xây dựng trên Spring Boot Microservices.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 3.5, Spring Cloud 2025 |
| API Gateway | Spring Cloud Gateway (WebFlux) |
| Service Discovery | Netflix Eureka |
| Config Server | Spring Cloud Config |
| Message Broker | Apache Kafka |
| Auth | JWT + Spring Security + RBAC |
| Database | MySQL 8, MongoDB 5 |
| Cache | Redis 7 |
| Tracing | Zipkin |
| Email | MailDev (dev), SMTP (prod) |
| Payment | VNPay |
| Frontend | Next.js 16 |

## Service Ports

| Service | Port |
|---------|------|
| API Gateway | 8222 |
| Auth Service | 8040 |
| Product Service | 8050 |
| Order Service | 8070 |
| Customer Service | 8090 |
| Payment Service | 8060 |
| Cart Service | 8055 |
| Inventory Service | 8045 |
| Delivery Service | 8065 |
| Notification Service | 8030 |
| Discovery (Eureka) | 8761 |
| Config Server | 8888 |

## Infrastructure Ports

| Service | Port |
|---------|------|
| MySQL | 3306 |
| phpMyAdmin | 8080 |
| MongoDB | 27017 |
| Mongo Express | 8081 |
| Redis | 6379 |
| Kafka | 9093 (host), 9092 (internal) |
| Zipkin | 9411 |
| MailDev UI | 1080 |
| MailDev SMTP | 1025 |

## Cách chạy

### 1. Chuẩn bị môi trường

```bash
# Clone repo
git clone https://github.com/NgTiennDat/E-Commerce.git
cd E-Commerce

# Tạo file .env từ template
cp .env.example .env
# Mở .env và điền các giá trị cần thiết
```

### 2. Khởi động infrastructure

```bash
docker-compose up -d
```

Chờ tất cả container healthy (khoảng 30-60 giây):

```bash
docker-compose ps
```

### 3. Chạy các service (theo thứ tự)

Chạy từng service bằng IDE hoặc Maven:

```bash
# Thứ tự bắt buộc:
# 1. Config Server (phải chạy trước tất cả)
cd services/config && mvn spring-boot:run

# 2. Discovery (Eureka)
cd services/discovery && mvn spring-boot:run

# 3. Các service còn lại (thứ tự không quan trọng)
cd services/auth && mvn spring-boot:run
cd services/product && mvn spring-boot:run
cd services/customer && mvn spring-boot:run
cd services/order && mvn spring-boot:run
cd services/payment && mvn spring-boot:run
cd services/cart && mvn spring-boot:run
cd services/notification && mvn spring-boot:run

# 4. Gateway (chạy sau cùng)
cd services/gateway && mvn spring-boot:run
```

### 4. Chạy Frontend

```bash
cd ../E-Commerce-Application-UI
cp .env.example .env.local
npm install
npm run dev
```

Truy cập: http://localhost:3000

### 5. Tài khoản mặc định

| Role | Username | Password |
|------|----------|----------|
| Admin | admin | Admin@2024 |

> **Quan trọng:** Đổi password admin ngay sau khi deploy lần đầu.

## API Documentation

Sau khi chạy, truy cập Swagger UI:
- Auth Service: http://localhost:8040/swagger-ui.html
- Product Service: http://localhost:8050/swagger-ui.html

## Monitoring

- Eureka Dashboard: http://localhost:8761
- Zipkin Tracing: http://localhost:9411
- MailDev: http://localhost:1080
- phpMyAdmin: http://localhost:8080
- Mongo Express: http://localhost:8081
