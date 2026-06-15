# Deploy Backend Len Render

Muc tieu:

```text
Android APK -> Render backend online -> Neon PostgreSQL
```

## 1. Chuan bi truoc khi deploy

Backend da co:

```text
backend/Dockerfile
render.yaml
backend/.env.example
```

Vi connection string Neon da tung duoc dan vao chat, nen sau khi test deploy xong nen rotate password trong Neon de an toan hon.

## 2. Dua code len GitHub

Render can lay code tu GitHub/GitLab/Bitbucket. Cach don gian nhat:

1. Tao repository moi tren GitHub, vi du `gymrank`.
2. Push toan bo folder `C:\Users\Admin\Documents\prj` len repository do.
3. Dam bao cac file nay co tren GitHub:

```text
backend/Dockerfile
render.yaml
backend/build.gradle
backend/src/main/resources/application.yml
```

## 3. Tao service tren Render bang Blueprint

1. Vao Render Dashboard.
2. Chon `New`.
3. Chon `Blueprint`.
4. Connect repository GitHub vua tao.
5. Render se doc file `render.yaml`.
6. Tao service `gymrank-api`.

Neu Render yeu cau nhap environment variables, dien:

```text
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://YOUR_NEON_HOST/neondb?sslmode=require
SPRING_DATASOURCE_USERNAME=neondb_owner
SPRING_DATASOURCE_PASSWORD=YOUR_NEON_PASSWORD
SPRING_DATASOURCE_MAX_POOL_SIZE=5
```

Luu y: URL phai bat dau bang `jdbc:postgresql://`, khong phai `postgresql://`.

## 4. Kiem tra backend online

Sau khi deploy thanh cong, Render se cho URL dang:

```text
https://gymrank-api.onrender.com
```

Kiem tra:

```text
https://gymrank-api.onrender.com/api/v1/health
```

Neu thay `status: ok`, backend online da chay.

## 5. Doi Android app sang backend online

Mo file:

```text
android-app/app/src/main/java/com/gymrank/app/network/ApiConfig.java
```

Doi:

```text
http://192.168.1.3:8080/api/v1
```

Thanh URL Render cua ban:

```text
https://gymrank-api.onrender.com/api/v1
```

Sau do build APK lai.

## 6. Test sau khi deploy

1. Cai APK moi tren dien thoai.
2. Dang ky tai khoan moi.
3. Lam onboarding.
4. Vao Neon SQL Editor chay:

```sql
select * from app_users order by id desc;
select * from user_profiles;
select * from onboarding_answers order by user_id desc;
select * from user_stats;
```

Neu co du lieu moi, luong APK -> Render -> Neon da thanh cong.

## 7. Loi thuong gap

### Render deploy fail khi build Docker

Xem tab `Logs` tren Render. Thuong gap:

- Sai Gradle wrapper.
- Khong push du file `gradle/`.
- Loi compile Java.

### Backend online `status: ok` nhung app khong dang ky duoc

Kiem tra `ApiConfig.java` da dung URL `https://...onrender.com/api/v1` chua, sau do build lai APK.

### Neon khong co du lieu

Kiem tra Render environment variables:

```text
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://...
SPRING_DATASOURCE_USERNAME=...
SPRING_DATASOURCE_PASSWORD=...
```
