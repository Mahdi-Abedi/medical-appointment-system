# Medical Appointment System

A distributed **microservices-based** medical appointment system built with **Spring Boot 4.1.1**, **Spring Cloud**, **Java 25**, and **Oracle/PostgreSQL** databases. The system provides user authentication, doctor management, appointment booking, and service discovery, all orchestrated via Docker Compose and monitored with Prometheus.

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Services](#services)
- [Technologies Used](#technologies-used)
- [Prerequisites](#prerequisites)
- [How to Run](#how-to-run)
- [Configuration](#configuration)
- [API Endpoints](#api-endpoints)
- [Monitoring](#monitoring)
- [Troubleshooting](#troubleshooting)
- [License](#license)

---

## Overview

This project demonstrates a **production-like microservices architecture** for a medical appointment domain. It includes:

- **Service Discovery** with Eureka
- **Centralized Configuration** using Spring Cloud Config backed by a Gitea Git repository
- **API Gateway** for routing and cross-cutting concerns
- **Domain services** for users, doctors, and appointments
- **Event-driven communication** via RabbitMQ
- **Polyglot persistence** using Oracle DB (doctors) and PostgreSQL (users)
- **Observability** with Prometheus and Spring Boot Actuator
- **UI service** for the frontend layer

Each service is an independent Spring Boot application with its own `pom.xml`, and can be built and run separately.

---

## Architecture

```text
┌──────────────────────────────────────────────────────────────────┐
│                        Client / Browser                         │
└───────────────────────────────┬──────────────────────────────────┘
                                │
                                ▼
                     ┌─────────────────────┐
                     │    API Gateway      │
                     │    (port 8080)      │
                     └──────────┬──────────┘
                                │
        ┌───────────────────────┼───────────────────────┐
        │                       │                       │
        ▼                       ▼                       ▼
┌───────────────┐      ┌───────────────┐      ┌───────────────┐
│ user-service  │      │doctor-service │      │appointment-   │
│ (port 8081)   │      │ (port 8082)   │      │service        │
│               │      │               │      │ (port 8083)   │
└───────┬───────┘      └───────┬───────┘      └───────┬───────┘
        │                      │                      │
        │                      │                      │
        ▼                      ▼                      ▼
┌───────────────┐      ┌───────────────┐      ┌───────────────┐
│  PostgreSQL   │      │   Oracle DB   │      │  PostgreSQL   │
│   (userdb)    │      │   (doctors)   │      │ (appointments)│
└───────────────┘      └───────────────┘      └───────────────┘

        ┌───────────────────────────────────────────────┐
        │              Infrastructure                    │
        │  ┌─────────┐  ┌─────────────┐  ┌───────────┐  │
        │  │ Eureka  │  │Config Server│  │ RabbitMQ  │  │
        │  │ Server  │  │  (Gitea)    │  │           │  │
        │  └─────────┘  └─────────────┘  └───────────┘  │
        └───────────────────────────────────────────────┘
```

- **Eureka Server** – Service registry for all microservices.
- **Config Server** – Fetches configuration from a Gitea repository (`medapp-config`).
- **API Gateway** – Single entry point, routes requests to downstream services.
- **user-service** – Manages users, authentication, and authorization (JWT).
- **doctor-service** – Manages doctor profiles, specializations, and availability.
- **appointment-service** – Handles appointment booking, status updates, and cancellation.
- **ui-service** – Frontend service (server-side rendered UI).
- **evaluate-prometheus** – A helper service for evaluating Prometheus metrics.

---

## Services

| Service | Port | Description | Database |
|---------|------|-------------|----------|
| `eureka-server` | 8761 | Service discovery | – |
| `config-server` | 8888 | Centralized configuration | Gitea (Git) |
| `api-gateway` | 8080 | API routing | – |
| `user-service` | 8081 | Users & authentication | PostgreSQL |
| `doctor-service` | 8082 | Doctor management | Oracle DB |
| `appointment-service` | 8083 | Appointment management | PostgreSQL |
| `ui-service` | 8084 | Frontend UI | – |
| `evaluate-prometheus` | 9090 | Prometheus evaluation | – |

> **Note:** Ports are indicative. Check each service's `application.yml` for exact values.

---

## Technologies Used

| Technology | Version |
|------------|---------|
| Java | 25 |
| Spring Boot | 4.1.1 |
| Spring Cloud | 2025.1.3 |
| Spring Cloud Netflix Eureka | – |
| Spring Cloud Config | – |
| Spring Cloud Gateway | – |
| Spring Security | – |
| Spring Data JPA | – |
| Oracle JDBC (ojdbc11) | 23.26.3.0.0 |
| PostgreSQL | (via Docker) |
| RabbitMQ | (via Docker) |
| Micrometer / Prometheus | – |
| Lombok | – |
| Maven | – |
| Docker & Docker Compose | – |
| Gitea | (for config repo) |

---

## Prerequisites

- **Java 25** installed
- **Maven** (or use Maven wrapper if present)
- **Docker** and **Docker Compose** installed
- An IDE (IntelliJ IDEA recommended)

---

## How to Run

### 1. Start Infrastructure

From the project root:

```bash
docker-compose up -d
```

This starts:

- Gitea (port 3000) + PostgreSQL for Gitea (port 5432)
- Oracle DB (port 1521)
- RabbitMQ (ports 5672, 15672)
- PostgreSQL for user-service (port 5433)

Wait for all containers to become healthy. You can check with:

```bash
docker ps
```

### 2. Build All Services

Because each service is a standalone Maven project, build them individually or use a loop:

```bash
# From the project root
for dir in eureka-server config-server api-gateway user-service doctor-service appointment-service ui-service evaluate-prometheus; do
  (cd "$dir" && mvn clean package -DskipTests)
done
```

### 3. Start Services in Order

Start the infrastructure services first, then the domain services:

```bash
# 1. Eureka Server
cd eureka-server && mvn spring-boot:run

# 2. Config Server (in a new terminal)
cd config-server && mvn spring-boot:run

# 3. API Gateway (in a new terminal)
cd api-gateway && mvn spring-boot:run

# 4. Domain services (each in a new terminal)
cd user-service && mvn spring-boot:run
cd doctor-service && mvn spring-boot:run
cd appointment-service && mvn spring-boot:run

# 5. UI Service
cd ui-service && mvn spring-boot:run
```

Once all services are up, the Eureka dashboard will be available at:

```
http://localhost:8761
```

---

## Configuration

Configuration is managed centrally via **Spring Cloud Config Server**, which reads from a Gitea repository (`medapp-config`). Each service has a minimal `application.yml` that points to the Config Server.

Example `application.yml` for `appointment-service`:

```yaml
spring:
  application:
    name: appointment-service
  config:
    import: optional:configserver:http://localhost:8888
  cloud:
    config:
      uri: http://localhost:8888
      fail-fast: true

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka
```

The actual datasource, RabbitMQ, and other environment-specific properties live in the Gitea config repository.

---

## API Endpoints

All endpoints are exposed through the API Gateway at `http://localhost:8080`. The paths below are the downstream service paths.

### User Service

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Authenticate and receive a JWT |
| GET | `/api/users/me` | Get current user profile |
| GET | `/api/users/{id}` | Get user by ID |
| GET | `/api/users` | List all users (ADMIN only) |
| PUT | `/api/users/{id}` | Update user |
| DELETE | `/api/users/{id}` | Delete user (ADMIN only) |

### Doctor Service

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/doctors` | Create a doctor |
| GET | `/api/doctors/{id}` | Get doctor by ID |
| GET | `/api/doctors` | List all doctors |
| GET | `/api/doctors/available` | List available doctors |
| GET | `/api/doctors/specialization/{specialization}` | Filter by specialization |
| PUT | `/api/doctors/{id}` | Update doctor |
| DELETE | `/api/doctors/{id}` | Delete doctor |

### Appointment Service

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/appointments` | Create an appointment |
| GET | `/api/appointments/{id}` | Get appointment by ID |
| GET | `/api/appointments` | List all appointments |
| GET | `/api/appointments/user/{userId}` | Appointments of a user |
| GET | `/api/appointments/doctor/{doctorId}` | Appointments of a doctor |
| GET | `/api/appointments/user/{userId}/upcoming` | Upcoming appointments for a user |
| PATCH | `/api/appointments/{id}/status` | Update appointment status |
| DELETE | `/api/appointments/{id}` | Cancel appointment |

---

## Monitoring

Each service exposes Actuator endpoints and Micrometer metrics. Prometheus can scrape them at:

```
/actuator/prometheus
```

The `evaluate-prometheus` module is provided as a helper for evaluating and testing Prometheus metrics.

---

## Troubleshooting

### Service does not register with Eureka

- Ensure `eureka-server` is running before starting other services.
- Check the `eureka.client.service-url.defaultZone` property in each service.

### Config Server cannot fetch configuration

- Verify that the Gitea container is up and the `medapp-config` repository exists.
- Check the Config Server logs for connection issues to Gitea.

### Database connection errors

- Make sure Oracle DB and PostgreSQL containers are healthy (`docker ps`).
- Verify credentials and ports in the Gitea config repository.

### RabbitMQ connection refused

- Confirm RabbitMQ is running on `localhost:5672`.
- Check the `spring.rabbitmq` properties in the config repository.

### Port conflicts

If a port is already in use, either stop the conflicting process or change the port in the service's configuration.

---

## License

This project is open source and available under the [MIT License](LICENSE).

---

---

# سیستم نوبت‌دهی پزشکی

یک سیستم نوبت‌دهی پزشکی **میکروسرویسی** و توزیع‌شده که با **Spring Boot 4.1.1**، **Spring Cloud**، **Java 25** و پایگاه‌داده‌های **Oracle/PostgreSQL** ساخته شده است. این سیستم احراز هویت کاربران، مدیریت پزشکان، رزرو نوبت و کشف سرویس را فراهم می‌کند و از طریق Docker Compose اجرا و با Prometheus مانیتور می‌شود.

---

## فهرست مطالب

- [معرفی](#معرفی)
- [معماری](#معماری)
- [سرویس‌ها](#سرویس‌ها)
- [تکنولوژی‌های استفاده‌شده](#تکنولوژی‌های-استفاده‌شده)
- [پیش‌نیازها](#پیش‌نیازها)
- [نحوه‌ی اجرا](#نحوه‌ی-اجرا)
- [تنظیمات](#تنظیمات)
- [APIها](#apiها)
- [مانیتورینگ](#مانیتورینگ)
- [رفع اشکال](#رفع-اشکال)
- [مجوز](#مجوز)

---

## معرفی

این پروژه یک **معماری میکروسرویسی شبیه به محیط production** برای حوزه‌ی نوبت‌دهی پزشکی ارائه می‌دهد. شامل موارد زیر است:

- **کشف سرویس** با Eureka
- **پیکربندی متمرکز** با Spring Cloud Config و مخزن Git در Gitea
- **API Gateway** برای مسیریابی و مدیریت درخواست‌ها
- **سرویس‌های دامنه** برای کاربران، پزشکان و نوبت‌ها
- **ارتباط رویدادمحور** از طریق RabbitMQ
- **ذخیره‌سازی چندگانه** با Oracle DB (پزشکان) و PostgreSQL (کاربران)
- **قابلیت مشاهده‌پذیری** با Prometheus و Spring Boot Actuator
- **سرویس UI** برای لایه‌ی فرانت‌اند

هر سرویس یک اپلیکیشن مستقل Spring Boot با `pom.xml` جداگانه است و می‌تواند به صورت جداگانه ساخته و اجرا شود.

---

## معماری

```text
┌──────────────────────────────────────────────────────────────────┐
│                        کلاینت / مرورگر                          │
└───────────────────────────────┬──────────────────────────────────┘
                                │
                                ▼
                     ┌─────────────────────┐
                     │    API Gateway      │
                     │    (پورت 8080)      │
                     └──────────┬──────────┘
                                │
        ┌───────────────────────┼───────────────────────┐
        │                       │                       │
        ▼                       ▼                       ▼
┌───────────────┐      ┌───────────────┐      ┌───────────────┐
│ user-service  │      │doctor-service │      │appointment-   │
│ (پورت 8081)   │      │ (پورت 8082)   │      │service        │
│               │      │               │      │ (پورت 8083)   │
└───────┬───────┘      └───────┬───────┘      └───────┬───────┘
        │                      │                      │
        │                      │                      │
        ▼                      ▼                      ▼
┌───────────────┐      ┌───────────────┐      ┌───────────────┐
│  PostgreSQL   │      │   Oracle DB   │      │  PostgreSQL   │
│   (userdb)    │      │   (doctors)   │      │ (appointments)│
└───────────────┘      └───────────────┘      └───────────────┘

        ┌───────────────────────────────────────────────┐
        │                 زیرساخت                        │
        │  ┌─────────┐  ┌─────────────┐  ┌───────────┐  │
        │  │ Eureka  │  │Config Server│  │ RabbitMQ  │  │
        │  │ Server  │  │  (Gitea)    │  │           │  │
        │  └─────────┘  └─────────────┘  └───────────┘  │
        └───────────────────────────────────────────────┘
```

- **Eureka Server** – رجیستری سرویس‌ها.
- **Config Server** – پیکربندی را از مخزن Gitea (`medapp-config`) می‌خواند.
- **API Gateway** – نقطه‌ی ورود واحد و مسیریابی درخواست‌ها.
- **user-service** – مدیریت کاربران، احراز هویت و مجوزدهی (JWT).
- **doctor-service** – مدیریت پروفایل پزشکان، تخصص‌ها و زمان‌های در دسترس.
- **appointment-service** – مدیریت رزرو، تغییر وضعیت و لغو نوبت‌ها.
- **ui-service** – سرویس رابط کاربری (Server-side rendered).
- **evaluate-prometheus** – سرویس کمکی برای ارزیابی متریک‌های Prometheus.

---

## سرویس‌ها

| سرویس | پورت | توضیح | پایگاه‌داده |
|-------|------|-------|-------------|
| `eureka-server` | 8761 | کشف سرویس | – |
| `config-server` | 8888 | پیکربندی متمرکز | Gitea (Git) |
| `api-gateway` | 8080 | مسیریابی API | – |
| `user-service` | 8081 | کاربران و احراز هویت | PostgreSQL |
| `doctor-service` | 8082 | مدیریت پزشکان | Oracle DB |
| `appointment-service` | 8083 | مدیریت نوبت‌ها | PostgreSQL |
| `ui-service` | 8084 | رابط کاربری | – |
| `evaluate-prometheus` | 9090 | ارزیابی Prometheus | – |

> **توجه:** پورت‌ها تقریبی هستند. مقادیر دقیق را در `application.yml` هر سرویس بررسی کنید.

---

## تکنولوژی‌های استفاده‌شده

| تکنولوژی | نسخه |
|----------|------|
| Java | 25 |
| Spring Boot | 4.1.1 |
| Spring Cloud | 2025.1.3 |
| Spring Cloud Netflix Eureka | – |
| Spring Cloud Config | – |
| Spring Cloud Gateway | – |
| Spring Security | – |
| Spring Data JPA | – |
| Oracle JDBC (ojdbc11) | 23.26.3.0.0 |
| PostgreSQL | (با Docker) |
| RabbitMQ | (با Docker) |
| Micrometer / Prometheus | – |
| Lombok | – |
| Maven | – |
| Docker & Docker Compose | – |
| Gitea | (برای مخزن کانفیگ) |

---

## پیش‌نیازها

- **Java 25**
- **Maven** (یا استفاده از wrapper در صورت وجود)
- **Docker** و **Docker Compose**
- یک IDE (پیشنهاد: IntelliJ IDEA)

---

## نحوه‌ی اجرا

### ۱. راه‌اندازی زیرساخت

از ریشه‌ی پروژه:

```bash
docker-compose up -d
```

این دستور موارد زیر را راه‌اندازی می‌کند:

- Gitea (پورت 3000) + PostgreSQL برای Gitea (پورت 5432)
- Oracle DB (پورت 1521)
- RabbitMQ (پورت‌های 5672 و 15672)
- PostgreSQL برای user-service (پورت 5433)

منتظر بمانید تا همه‌ی کانتینرها سالم شوند. بررسی با:

```bash
docker ps
```

### ۲. ساخت همه‌ی سرویس‌ها

چون هر سرویس یک پروژه‌ی Maven مستقل است، آن‌ها را جداگانه یا با یک حلقه بسازید:

```bash
# از ریشه‌ی پروژه
for dir in eureka-server config-server api-gateway user-service doctor-service appointment-service ui-service evaluate-prometheus; do
  (cd "$dir" && mvn clean package -DskipTests)
done
```

### ۳. اجرای سرویس‌ها به ترتیب

ابتدا سرویس‌های زیرساختی و سپس سرویس‌های دامنه را اجرا کنید:

```bash
# ۱. Eureka Server
cd eureka-server && mvn spring-boot:run

# ۲. Config Server (در ترمینال جدید)
cd config-server && mvn spring-boot:run

# ۳. API Gateway (در ترمینال جدید)
cd api-gateway && mvn spring-boot:run

# ۴. سرویس‌های دامنه (هر کدام در ترمینال جدید)
cd user-service && mvn spring-boot:run
cd doctor-service && mvn spring-boot:run
cd appointment-service && mvn spring-boot:run

# ۵. UI Service
cd ui-service && mvn spring-boot:run
```

پس از بالا آمدن همه‌ی سرویس‌ها، داشبورد Eureka در آدرس زیر در دسترس است:

```
http://localhost:8761
```

---

## تنظیمات

پیکربندی به صورت متمرکز از طریق **Spring Cloud Config Server** مدیریت می‌شود که از مخزن Gitea (`medapp-config`) می‌خواند. هر سرویس یک `application.yml` حداقلی دارد که به Config Server اشاره می‌کند.

نمونه `application.yml` برای `appointment-service`:

```yaml
spring:
  application:
    name: appointment-service
  config:
    import: optional:configserver:http://localhost:8888
  cloud:
    config:
      uri: http://localhost:8888
      fail-fast: true

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka
```

تنظیمات واقعی دیتاسورس، RabbitMQ و سایر مقادیر محیطی در مخزن Gitea قرار دارند.

---

## APIها

همه‌ی endpointها از طریق API Gateway در `http://localhost:8080` در دسترس هستند. مسیرهای زیر مربوط به سرویس‌های داخلی هستند.

### User Service

| متد | Endpoint | توضیح |
|------|----------|-------|
| POST | `/api/auth/register` | ثبت‌نام کاربر جدید |
| POST | `/api/auth/login` | احراز هویت و دریافت JWT |
| GET | `/api/users/me` | دریافت پروفایل کاربر جاری |
| GET | `/api/users/{id}` | دریافت کاربر با شناسه |
| GET | `/api/users` | لیست همه‌ی کاربران (فقط ADMIN) |
| PUT | `/api/users/{id}` | به‌روزرسانی کاربر |
| DELETE | `/api/users/{id}` | حذف کاربر (فقط ADMIN) |

### Doctor Service

| متد | Endpoint | توضیح |
|------|----------|-------|
| POST | `/api/doctors` | ایجاد پزشک |
| GET | `/api/doctors/{id}` | دریافت پزشک با شناسه |
| GET | `/api/doctors` | لیست همه‌ی پزشکان |
| GET | `/api/doctors/available` | لیست پزشکان در دسترس |
| GET | `/api/doctors/specialization/{specialization}` | فیلتر بر اساس تخصص |
| PUT | `/api/doctors/{id}` | به‌روزرسانی پزشک |
| DELETE | `/api/doctors/{id}` | حذف پزشک |

### Appointment Service

| متد | Endpoint | توضیح |
|------|----------|-------|
| POST | `/api/appointments` | ایجاد نوبت |
| GET | `/api/appointments/{id}` | دریافت نوبت با شناسه |
| GET | `/api/appointments` | لیست همه‌ی نوبت‌ها |
| GET | `/api/appointments/user/{userId}` | نوبت‌های یک کاربر |
| GET | `/api/appointments/doctor/{doctorId}` | نوبت‌های یک پزشک |
| GET | `/api/appointments/user/{userId}/upcoming` | نوبت‌های آینده‌ی کاربر |
| PATCH | `/api/appointments/{id}/status` | تغییر وضعیت نوبت |
| DELETE | `/api/appointments/{id}` | لغو نوبت |

---

## مانیتورینگ

هر سرویس endpointهای Actuator و متریک‌های Micrometer را ارائه می‌دهد. Prometheus می‌تواند آن‌ها را در آدرس زیر scrape کند:

```
/actuator/prometheus
```

ماژول `evaluate-prometheus` به عنوان یک ابزار کمکی برای ارزیابی و تست متریک‌های Prometheus ارائه شده است.

---

## رفع اشکال

### سرویس در Eureka ثبت نمی‌شود

- مطمئن شوید `eureka-server` قبل از سایر سرویس‌ها اجرا شده است.
- مقدار `eureka.client.service-url.defaultZone` را در هر سرویس بررسی کنید.

### Config Server نمی‌تواند پیکربندی را دریافت کند

- بررسی کنید که کانتینر Gitea بالا باشد و مخزن `medapp-config` وجود داشته باشد.
- لاگ‌های Config Server را برای مشکلات اتصال به Gitea بررسی کنید.

### خطاهای اتصال به پایگاه‌داده

- از سالم بودن کانتینرهای Oracle DB و PostgreSQL مطمئن شوید (`docker ps`).
- اطلاعات ورود و پورت‌ها را در مخزن کانفیگ Gitea بررسی کنید.

### اتصال RabbitMQ برقرار نمی‌شود

- مطمئن شوید RabbitMQ روی `localhost:5672` در حال اجراست.
- ویژگی‌های `spring.rabbitmq` را در مخزن کانفیگ بررسی کنید.

### تضاد پورت

اگر پورتی اشغال است، فرآیند متضاد را متوقف کنید یا پورت را در پیکربندی سرویس تغییر دهید.

---

## مجوز

این پروژه به صورت open source و تحت [مجوز MIT](LICENSE) منتشر شده است.