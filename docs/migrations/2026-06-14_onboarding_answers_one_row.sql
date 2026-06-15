-- Convert onboarding_answers from one-row-per-question to one-row-per-user.
-- Run this on Neon before deploying the backend version that uses the new entity.
-- This migration keeps existing answers when possible.

CREATE TABLE IF NOT EXISTS onboarding_answers_new (
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

INSERT INTO onboarding_answers_new (
    user_id,
    display_name,
    experience_level,
    main_goal,
    training_days_per_week,
    bodygraph_type,
    answered_at,
    updated_at
)
SELECT
    user_id,
    COALESCE(MAX(answer_value) FILTER (WHERE question_key = 'display_name'), 'Ban') AS display_name,
    COALESCE(MAX(answer_value) FILTER (WHERE question_key = 'experience_level'), 'BEGINNER') AS experience_level,
    COALESCE(MAX(answer_value) FILTER (WHERE question_key = 'main_goal'), 'CONSISTENT') AS main_goal,
    COALESCE(MAX(answer_value) FILTER (WHERE question_key = 'training_days_per_week'), '3')::SMALLINT AS training_days_per_week,
    COALESCE(MAX(answer_value) FILTER (WHERE question_key = 'bodygraph_type'), 'SKIP') AS bodygraph_type,
    MIN(answered_at) AS answered_at,
    NOW() AS updated_at
FROM onboarding_answers
GROUP BY user_id
ON CONFLICT (user_id) DO UPDATE SET
    display_name = EXCLUDED.display_name,
    experience_level = EXCLUDED.experience_level,
    main_goal = EXCLUDED.main_goal,
    training_days_per_week = EXCLUDED.training_days_per_week,
    bodygraph_type = EXCLUDED.bodygraph_type,
    updated_at = NOW();

DROP TABLE onboarding_answers;

ALTER TABLE onboarding_answers_new RENAME TO onboarding_answers;

CREATE INDEX IF NOT EXISTS idx_onboarding_answers_goal ON onboarding_answers(main_goal);
CREATE INDEX IF NOT EXISTS idx_onboarding_answers_experience ON onboarding_answers(experience_level);
