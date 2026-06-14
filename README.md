# GymRank Workspace

GymRank la du an Android + Spring Boot cho app tap gym huong den nguoi Viet: ghi lai buoi tap, giu chuoi luyen tap, tinh EXP/Level, tinh Rank theo nhom co va hien Bodygraph.

## Cau truc

```text
GymRank/
  android-app/   Android app Java + XML, xuat APK mien phi
  backend/       Spring Boot REST API
  docs/          Tai lieu y tuong, MVP, database, API, scoring
```

## Huong di da chot

- Lam trong 1 workspace IntelliJ.
- Android app dung Java + XML.
- Backend dung Spring Boot + Java.
- Database online free: uu tien Neon PostgreSQL khi can dung that.
- Trien khai backend len Render/host khac khi muon nguoi khac test online.
- Khong publish Google Play luc dau, chi xuat APK de demo/thuc tap.

## Mo du an

1. Mo thu muc goc `C:\Users\Admin\Documents\prj` bang IntelliJ IDEA.
2. Mo rieng `android-app` bang Android Studio/IntelliJ de sync Android Gradle project.
3. Mo rieng `backend` bang IntelliJ de sync Spring Boot project.

## Xuat APK

Trong Android Studio:

1. Mo `android-app`.
2. Chon `Build`.
3. Chon `Build Bundle(s) / APK(s)`.
4. Chon `Build APK(s)`.

APK debug se nam trong:

```text
android-app/app/build/outputs/apk/debug/
```

Neu build bang terminal trong thu muc `android-app`:

```powershell
.\gradlew.bat :app:assembleDebug
```

## Chay backend

Trong IntelliJ:

1. Mo `backend`.
2. Sync Gradle.
3. Chay class `GymRankApiApplication`.
4. Kiem tra API mau: `http://localhost:8080/api/v1/health`.

Khi Android app chay tren emulator, app goi backend local qua:

```text
http://10.0.2.2:8080/api/v1
```

Khi Android app chay tren dien thoai that, dien thoai phai cung mang voi laptop va app goi backend qua IP LAN cua laptop, vi du:

```text
http://192.168.1.3:8080/api/v1
```

Neu chay tren dien thoai that cung Wi-Fi, can doi base URL trong file nay sang IP cua may tinh dang chay backend:

```text
android-app/app/src/main/java/com/gymrank/app/network/ApiConfig.java
```

## Test luong dang nhap MVP

1. Restart backend de nap code moi.
2. Chay Android app tren dien thoai.
3. Chon `Dang ky`, nhap ten, email va mat khau it nhat 6 ky tu.
4. Hoan thanh onboarding.
5. Mo tren trinh duyet may tinh:

```text
http://localhost:8080/api/v1/me/summary
```

Neu thay `"synced": true` la Android da gui profile len backend thanh cong.

Backend hien da luu cac bang dau tien bang JPA:

```text
app_users
user_profiles
onboarding_answers
user_stats
```

Neu build bang terminal trong thu muc `backend`:

```powershell
.\gradlew.bat bootJar
```

File backend sau khi build:

```text
backend/build/libs/gymrank-backend-0.1.0.jar
```

Trong profile `dev`, backend dung H2 file database:

```text
backend/build/gymrank-dev.mv.db
```

Nghia la khi restart backend, tai khoan test van con. Neu muon reset database dev, tat backend roi xoa file tren.

Khi muon dung database online PostgreSQL, xem:

```text
docs/ONLINE_DATABASE_SETUP.md
```

Khi muon share APK cho nguoi khac dung, can deploy backend online truoc, xem:

```text
docs/DEPLOY_RENDER.md
```

Backend online hien tai:

```text
https://gymrank-api.onrender.com/api/v1
```

## Giai doan hien tai

Day la skeleton dau tien de bat dau coding. Chua phai san pham hoan chinh.
