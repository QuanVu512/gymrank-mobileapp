# Database Design v2

Database production du kien dung PostgreSQL online free, uu tien Neon. Ban v2 nay dua tren y tuong schema ban dau, nhung sua de mo rong tot hon cho Android app, backend API va web admin.

## Danh gia schema ban dau

Schema ban dau da dung huong o cac diem:

- Da tach core data, user/auth, news, stats, streak va workout.
- Co role `USER` / `ADMIN`, phu hop voi y tuong them web admin CRUD.
- Co `muscle_groups.svg_path_id`, rat hop voi bodygraph tren Android.
- Co `user_bodygraph`, phu hop voi co che rank theo nhom co.

Nhung nen sua cac diem sau truoc khi noi database that:

- `exercises.primary_muscle_id` chi luu duoc 1 nhom co chinh. Bai tap thuc te tac dong nhieu nhom co, nen can bang trung gian `exercise_muscle_targets`.
- `workout_logs` dang qua don gian. Can tach thanh `workout_sessions` va `workout_sets` de luu 1 buoi tap co nhieu bai, moi bai co nhieu set.
- `user_profiles` chi luu trang thai hien tai, chua luu lich su cau tra loi onboarding. Neu admin muon xem nguoi dung quan tam gi, can bang `onboarding_answers`.
- `news_articles` nen co `status`, `slug`, `published_at` de admin viet nhap, dang bai va an bai.
- Nen them `created_at`, `updated_at`, `active/status`, indexes va CHECK constraints.
- Nen dung `BIGSERIAL` thay cho `SERIAL` de an toan hon khi du lieu lon dan.

## Nhom Auth va User

### app_users

```text
id
public_id
email
password_hash
auth_provider
full_name
role
status
created_at
updated_at
last_login_at
```

Ghi chu:

- `role`: `USER` hoac `ADMIN` trong MVP.
- `auth_provider`: `LOCAL`, sau nay them `GOOGLE`.
- `public_id`: UUID de app/API dung ben ngoai, tranh lo id tang dan.
- `password_hash`: MVP co the luu dang `salt:hash`; production nen doi sang BCrypt.

### user_profiles

```text
user_id
gender
height_cm
weight_kg
fitness_goal
experience_level
training_days_per_week
bodygraph_type
created_at
updated_at
```

### onboarding_answers

```text
id
user_id
question_key
question_version
answer_value
answer_text
answered_at
```

Bang nay quan trong cho web admin. Vi du admin co the xem:

- Bao nhieu nguoi chon `LOSE_WEIGHT`.
- Bao nhieu nguoi la `BEGINNER`.
- Trung binh nguoi dung muon tap may buoi/tuang.
- Cau hoi nao nen sua vi it nguoi tra loi ro.

## Nhom Core Data

### muscle_groups

```text
id
code
name_vi
name_en
body_region
svg_path_id
sort_order
active
created_at
updated_at
```

### exercises

```text
id
code
name_vi
name_en
description
instruction
exercise_type
equipment
difficulty_level
image_url
video_url
is_beginner_safe
active
created_by
updated_by
created_at
updated_at
```

Ghi chu:

- `exercise_type`: `STRENGTH`, `CARDIO`, `MOBILITY`, `SPORT`.
- `difficulty_level`: `BEGINNER`, `INTERMEDIATE`, `ADVANCED`.

### exercise_muscle_targets

```text
exercise_id
muscle_group_id
target_role
impact_ratio
```

Bang nay thay cho `primary_muscle_id` don le. Vi du squat:

- Quads: PRIMARY, 0.50
- Glutes: PRIMARY, 0.30
- Core: SECONDARY, 0.20

## Nhom News/Admin Content

### news_articles

```text
id
slug
title
summary
content
image_url
status
created_by
updated_by
published_at
created_at
updated_at
```

`status`: `DRAFT`, `PUBLISHED`, `ARCHIVED`.

## Nhom Gamification

### user_stats

```text
user_id
level
exp_points
rank_points
current_streak_weeks
best_streak_weeks
total_workouts
updated_at
```

### weekly_streaks

```text
id
user_id
week_start_date
workouts_this_week
streak_kept
updated_at
```

Quy tac MVP: mot tuan co tu 3 buoi tro len thi `streak_kept = true`.

### user_muscle_stats

```text
user_id
muscle_group_id
rank_points
rank_tier
last_trained_at
recovery_score
updated_at
```

Bang nay dung cho bodygraph: to mau nhom co, xep rank tung nhom co, va sau nay hien nhom co bi bo quen.

## Nhom Planning va Workout Logging

### workout_routines

```text
id
user_id
name
goal
is_auto_generated
active
created_at
updated_at
```

### routine_schedules

```text
id
routine_id
day_of_week
```

`day_of_week`: nen quy uoc 1-7, trong do 1 la thu 2 va 7 la chu nhat.

### routine_exercises

```text
id
routine_id
exercise_id
position
target_sets
target_reps_min
target_reps_max
target_duration_seconds
target_weight_kg
rest_seconds
```

### workout_sessions

```text
id
user_id
routine_id
started_at
ended_at
duration_minutes
session_type
note
exp_gained
rank_points_gained
created_at
```

### workout_sets

```text
id
workout_session_id
exercise_id
set_number
weight_kg
reps
duration_seconds
distance_meters
completed
created_at
```

Bang nay co the luu ca gym va cardio:

- Gym: `weight_kg`, `reps`.
- Cardio: `duration_seconds`, `distance_meters`.

### workout_muscle_rewards

```text
workout_session_id
muscle_group_id
exp_gained
rank_points_gained
```

Bang nay luu diem da cong theo nhom co tai thoi diem hoan thanh buoi tap. Neu sau nay admin sua bai tap/impact ratio, lich su diem cu van khong bi doi sai.

## Nhom Admin va Audit

### admin_audit_logs

```text
id
actor_user_id
action
entity_type
entity_id
before_data
after_data
created_at
```

Dung de web admin xem ai da them/sua/xoa bai tap, nhom co, news, routine mau.

## Thu tu nen lam

Khong can tao tat ca bang ngay trong ngay dau. Thu tu hop ly:

1. `app_users`, `user_profiles`, `onboarding_answers`, `user_stats`.
2. `muscle_groups`, `exercises`, `exercise_muscle_targets`.
3. `workout_routines`, `routine_schedules`, `routine_exercises`.
4. `workout_sessions`, `workout_sets`, `weekly_streaks`, `user_muscle_stats`.
5. `news_articles`, `admin_audit_logs`.

Voi tien do 1-2 thang, nen noi database that bat dau tu nhom 1 truoc.
