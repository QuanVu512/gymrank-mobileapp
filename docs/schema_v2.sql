-- GymRank database schema v2
-- Target database: PostgreSQL

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ==========================================
-- 1. Auth and user profile
-- ==========================================

CREATE TABLE app_users (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255), -- MVP format: salt:hash. Production should use BCrypt.
    auth_provider VARCHAR(30) NOT NULL DEFAULT 'LOCAL',
    full_name VARCHAR(120) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_login_at TIMESTAMPTZ,
    CONSTRAINT chk_app_users_role CHECK (role IN ('USER', 'ADMIN')),
    CONSTRAINT chk_app_users_status CHECK (status IN ('ACTIVE', 'DISABLED')),
    CONSTRAINT chk_app_users_auth_provider CHECK (auth_provider IN ('LOCAL', 'GOOGLE'))
);

CREATE TABLE auth_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    token_hash CHAR(64) UNIQUE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ NOT NULL,
    last_used_at TIMESTAMPTZ,
    revoked_at TIMESTAMPTZ
);

CREATE INDEX idx_auth_sessions_user_id ON auth_sessions(user_id);
CREATE INDEX idx_auth_sessions_active_token ON auth_sessions(token_hash) WHERE revoked_at IS NULL;

CREATE TABLE user_profiles (
    user_id BIGINT PRIMARY KEY REFERENCES app_users(id) ON DELETE CASCADE,
    gender VARCHAR(20),
    height_cm NUMERIC(5,2),
    weight_kg NUMERIC(5,2),
    fitness_goal VARCHAR(50),
    experience_level VARCHAR(50),
    training_days_per_week SMALLINT,
    bodygraph_type VARCHAR(20),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_user_profiles_training_days CHECK (training_days_per_week IS NULL OR training_days_per_week BETWEEN 1 AND 7)
);

CREATE TABLE onboarding_answers (
    user_id BIGINT PRIMARY KEY REFERENCES app_users(id) ON DELETE CASCADE,
    display_name VARCHAR(120) NOT NULL,
    experience_level VARCHAR(50) NOT NULL,
    main_goal VARCHAR(50) NOT NULL,
    training_days_per_week SMALLINT NOT NULL,
    bodygraph_type VARCHAR(20) NOT NULL,
    answered_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_onboarding_answers_training_days CHECK (training_days_per_week BETWEEN 1 AND 7)
);

CREATE TABLE user_muscle_rank_stats (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    muscle_code VARCHAR(60) NOT NULL,
    rank_points DOUBLE PRECISION NOT NULL DEFAULT 0,
    last_trained_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_user_muscle_rank_stats_user_muscle UNIQUE (user_id, muscle_code)
);

CREATE INDEX idx_user_muscle_rank_stats_user_id ON user_muscle_rank_stats(user_id);

CREATE TABLE workout_rank_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    exercise_code VARCHAR(80) NOT NULL,
    reps INT NOT NULL,
    sets INT NOT NULL,
    weight_kg DOUBLE PRECISION NOT NULL,
    exp_gained INT NOT NULL,
    rank_gained DOUBLE PRECISION NOT NULL,
    cheat_like BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_workout_rank_logs_user_created ON workout_rank_logs(user_id, created_at);

-- ==========================================
-- 2. Core exercise data
-- ==========================================

CREATE TABLE muscle_groups (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name_vi VARCHAR(100) NOT NULL,
    name_en VARCHAR(100),
    body_region VARCHAR(50),
    svg_path_id VARCHAR(100) UNIQUE NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE exercises (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(80) UNIQUE NOT NULL,
    name_vi VARCHAR(255) NOT NULL,
    name_en VARCHAR(255),
    description TEXT,
    instruction TEXT,
    exercise_type VARCHAR(30) NOT NULL,
    equipment VARCHAR(80),
    difficulty_level VARCHAR(30),
    image_url VARCHAR(500),
    video_url VARCHAR(500),
    is_beginner_safe BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by BIGINT REFERENCES app_users(id),
    updated_by BIGINT REFERENCES app_users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_exercises_type CHECK (exercise_type IN ('STRENGTH', 'CARDIO', 'MOBILITY', 'SPORT')),
    CONSTRAINT chk_exercises_difficulty CHECK (difficulty_level IS NULL OR difficulty_level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED'))
);

CREATE TABLE exercise_muscle_targets (
    exercise_id BIGINT NOT NULL REFERENCES exercises(id) ON DELETE CASCADE,
    muscle_group_id BIGINT NOT NULL REFERENCES muscle_groups(id),
    target_role VARCHAR(20) NOT NULL,
    impact_ratio NUMERIC(4,3) NOT NULL DEFAULT 1.000,
    PRIMARY KEY (exercise_id, muscle_group_id),
    CONSTRAINT chk_exercise_muscle_role CHECK (target_role IN ('PRIMARY', 'SECONDARY')),
    CONSTRAINT chk_exercise_muscle_impact CHECK (impact_ratio > 0 AND impact_ratio <= 1)
);

-- ==========================================
-- 3. News and admin content
-- ==========================================

CREATE TABLE news_articles (
    id BIGSERIAL PRIMARY KEY,
    slug VARCHAR(180) UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    summary TEXT,
    content TEXT NOT NULL,
    image_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_by BIGINT REFERENCES app_users(id),
    updated_by BIGINT REFERENCES app_users(id),
    published_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_news_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'))
);

-- ==========================================
-- 4. Gamification and bodygraph
-- ==========================================

CREATE TABLE user_stats (
    user_id BIGINT PRIMARY KEY REFERENCES app_users(id) ON DELETE CASCADE,
    level INT NOT NULL DEFAULT 1,
    exp_points INT NOT NULL DEFAULT 0,
    rank_points INT NOT NULL DEFAULT 0,
    current_streak_weeks INT NOT NULL DEFAULT 0,
    best_streak_weeks INT NOT NULL DEFAULT 0,
    total_workouts INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_user_stats_non_negative CHECK (
        level >= 1
        AND exp_points >= 0
        AND rank_points >= 0
        AND current_streak_weeks >= 0
        AND best_streak_weeks >= 0
        AND total_workouts >= 0
    )
);

CREATE TABLE weekly_streaks (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    week_start_date DATE NOT NULL,
    workouts_this_week INT NOT NULL DEFAULT 0,
    streak_kept BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_weekly_streaks_user_week UNIQUE (user_id, week_start_date),
    CONSTRAINT chk_weekly_streaks_workouts CHECK (workouts_this_week >= 0)
);

CREATE TABLE user_muscle_stats (
    user_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    muscle_group_id BIGINT NOT NULL REFERENCES muscle_groups(id),
    rank_points INT NOT NULL DEFAULT 0,
    rank_tier VARCHAR(30) NOT NULL DEFAULT 'UNRANKED',
    last_trained_at TIMESTAMPTZ,
    recovery_score SMALLINT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, muscle_group_id),
    CONSTRAINT chk_user_muscle_rank_points CHECK (rank_points >= 0),
    CONSTRAINT chk_user_muscle_recovery CHECK (recovery_score IS NULL OR recovery_score BETWEEN 0 AND 100)
);

-- ==========================================
-- 5. Planning and workout logging
-- ==========================================

CREATE TABLE workout_routines (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    name VARCHAR(160) NOT NULL,
    goal VARCHAR(50),
    is_auto_generated BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE routine_schedules (
    id BIGSERIAL PRIMARY KEY,
    routine_id BIGINT NOT NULL REFERENCES workout_routines(id) ON DELETE CASCADE,
    day_of_week SMALLINT NOT NULL,
    CONSTRAINT uq_routine_schedules_day UNIQUE (routine_id, day_of_week),
    CONSTRAINT chk_routine_schedules_day CHECK (day_of_week BETWEEN 1 AND 7)
);

CREATE TABLE routine_exercises (
    id BIGSERIAL PRIMARY KEY,
    routine_id BIGINT NOT NULL REFERENCES workout_routines(id) ON DELETE CASCADE,
    exercise_id BIGINT NOT NULL REFERENCES exercises(id),
    position INT NOT NULL DEFAULT 1,
    target_sets INT,
    target_reps_min INT,
    target_reps_max INT,
    target_duration_seconds INT,
    target_weight_kg NUMERIC(6,2),
    rest_seconds INT,
    CONSTRAINT chk_routine_exercises_position CHECK (position >= 1),
    CONSTRAINT chk_routine_exercises_sets CHECK (target_sets IS NULL OR target_sets >= 1),
    CONSTRAINT chk_routine_exercises_reps CHECK (
        target_reps_min IS NULL
        OR target_reps_max IS NULL
        OR target_reps_max >= target_reps_min
    )
);

CREATE TABLE workout_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    routine_id BIGINT REFERENCES workout_routines(id),
    started_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ended_at TIMESTAMPTZ,
    duration_minutes INT,
    session_type VARCHAR(30) NOT NULL DEFAULT 'STRENGTH',
    note TEXT,
    exp_gained INT NOT NULL DEFAULT 0,
    rank_points_gained INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_workout_sessions_type CHECK (session_type IN ('STRENGTH', 'CARDIO', 'MIXED', 'SPORT')),
    CONSTRAINT chk_workout_sessions_duration CHECK (duration_minutes IS NULL OR duration_minutes >= 0)
);

CREATE TABLE workout_sets (
    id BIGSERIAL PRIMARY KEY,
    workout_session_id BIGINT NOT NULL REFERENCES workout_sessions(id) ON DELETE CASCADE,
    exercise_id BIGINT NOT NULL REFERENCES exercises(id),
    set_number INT NOT NULL,
    weight_kg NUMERIC(6,2),
    reps INT,
    duration_seconds INT,
    distance_meters INT,
    completed BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_workout_sets_number CHECK (set_number >= 1),
    CONSTRAINT chk_workout_sets_weight CHECK (weight_kg IS NULL OR weight_kg >= 0),
    CONSTRAINT chk_workout_sets_reps CHECK (reps IS NULL OR reps >= 0),
    CONSTRAINT chk_workout_sets_duration CHECK (duration_seconds IS NULL OR duration_seconds >= 0),
    CONSTRAINT chk_workout_sets_distance CHECK (distance_meters IS NULL OR distance_meters >= 0)
);

CREATE TABLE workout_muscle_rewards (
    workout_session_id BIGINT NOT NULL REFERENCES workout_sessions(id) ON DELETE CASCADE,
    muscle_group_id BIGINT NOT NULL REFERENCES muscle_groups(id),
    exp_gained INT NOT NULL DEFAULT 0,
    rank_points_gained INT NOT NULL DEFAULT 0,
    PRIMARY KEY (workout_session_id, muscle_group_id)
);

-- ==========================================
-- 6. Admin audit logs
-- ==========================================

CREATE TABLE admin_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    actor_user_id BIGINT REFERENCES app_users(id),
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(80) NOT NULL,
    entity_id VARCHAR(80),
    before_data JSONB,
    after_data JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ==========================================
-- Indexes
-- ==========================================

CREATE INDEX idx_onboarding_answers_goal ON onboarding_answers(main_goal);
CREATE INDEX idx_onboarding_answers_experience ON onboarding_answers(experience_level);
CREATE INDEX idx_exercises_type_active ON exercises(exercise_type, active);
CREATE INDEX idx_news_status_published ON news_articles(status, published_at DESC);
CREATE INDEX idx_workout_sessions_user_started ON workout_sessions(user_id, started_at DESC);
CREATE INDEX idx_workout_sets_session ON workout_sets(workout_session_id);
CREATE INDEX idx_admin_audit_actor_created ON admin_audit_logs(actor_user_id, created_at DESC);
