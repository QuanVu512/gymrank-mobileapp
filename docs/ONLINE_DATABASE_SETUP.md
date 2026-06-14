# Online Database Setup

Backend da san sang ket noi PostgreSQL online qua profile `prod`.

## Nen dung database nao?

MVP nen dung PostgreSQL online vi Spring Boot backend da co dependency PostgreSQL va schema v2 cung viet theo PostgreSQL.

Lua chon phu hop:

- Neon PostgreSQL
- Supabase PostgreSQL
- Render PostgreSQL neu deploy backend len Render va muon de quan ly chung

## Thong tin can lay tu database provider

Can 3 thong tin:

```text
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
```

Voi PostgreSQL, URL cho Spring Boot thuong co dang:

```text
jdbc:postgresql://host/database?sslmode=require
```

## Chay backend local voi database online

Trong IntelliJ, mo Run Configuration cua backend va them Environment variables:

```text
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://YOUR_HOST/YOUR_DATABASE?sslmode=require
SPRING_DATASOURCE_USERNAME=YOUR_USERNAME
SPRING_DATASOURCE_PASSWORD=YOUR_PASSWORD
```

Sau do restart backend va kiem tra:

```text
http://localhost:8080/api/v1/health
```

Neu backend khoi dong thanh cong, hay dang ky tai khoan tren Android app. Cac bang dau tien se duoc tao tu dong:

```text
app_users
user_profiles
onboarding_answers
user_stats
```

## Luu y bao mat

- Khong commit `.env` hoac password database len Git.
- File `backend/.env.example` chi la mau, khong chua mat khau that.
- `ddl-auto: update` dang phu hop cho MVP. Khi gan production that, nen chuyen sang migration bang Flyway/Liquibase.
