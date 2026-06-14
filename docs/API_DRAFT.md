# API Draft

Base URL local:

```text
http://localhost:8080/api/v1
```

## Public/dev

```text
GET /health
```

## User

```text
POST /auth/register
POST /auth/login
GET /me
PUT /me/profile
```

## Exercise

```text
GET /exercises
GET /exercises/{id}
GET /muscle-groups
```

## Routine

```text
GET /routines
POST /routines
PUT /routines/{id}
DELETE /routines/{id}
POST /routines/auto-plan
```

## Workout

```text
POST /workouts/start
POST /workouts/{id}/sets
POST /workouts/{id}/finish
GET /workouts/history
```

## Progress

```text
GET /progress/summary
GET /progress/bodygraph
GET /progress/streak
POST /score/preview
```

## API mau trong skeleton

Skeleton hien co:

```text
GET  /api/v1/health
GET  /api/v1/exercises
POST /api/v1/auth/register
POST /api/v1/auth/login
POST /api/v1/score/preview
POST /api/v1/onboarding/profile
GET  /api/v1/me/summary
```

## Auth MVP

Dang ky tai khoan bang email va mat khau:

```text
POST /api/v1/auth/register
```

Body:

```json
{
  "displayName": "Quan",
  "email": "quan@example.com",
  "password": "123456"
}
```

Dang nhap:

```text
POST /api/v1/auth/login
```

Body:

```json
{
  "email": "quan@example.com",
  "password": "123456"
}
```

Response:

```json
{
  "userId": "uuid",
  "displayName": "Quan",
  "email": "quan@example.com",
  "token": "session-token-tam-thoi",
  "newUser": true
}
```

Luu y: Auth MVP hien tai luu trong memory cua backend. Khi restart backend, tai khoan se mat. Buoc database online se giai quyet viec nay.

## Onboarding profile

Android gui profile sau khi nguoi dung hoan thanh cau hoi:

```text
POST /api/v1/onboarding/profile
```

Body:

```json
{
  "userId": "uuid",
  "displayName": "Quan",
  "experienceLevel": "BEGINNER",
  "mainGoal": "BUILD_MUSCLE",
  "trainingDaysPerWeek": 3,
  "bodygraphType": "MALE"
}
```

Response:

```json
{
  "displayName": "Quan",
  "experienceLevel": "BEGINNER",
  "mainGoal": "BUILD_MUSCLE",
  "trainingDaysPerWeek": 3,
  "bodygraphType": "MALE",
  "level": 1,
  "exp": 0,
  "streak": 0,
  "rankPoints": 0,
  "synced": true,
  "updatedAt": "2026-06-13T00:00:00Z"
}
```

MVP hien tai luu tam trong memory cua backend. Khi restart backend, du lieu se mat. Buoc sau moi noi PostgreSQL online.

## Summary

```text
GET /api/v1/me/summary?userId=uuid
```
