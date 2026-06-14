package com.gymrank.api.domain;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record WorkoutScoreRequest(
        @NotNull ActivityType activityType,
        @Min(1) @Max(300) int durationMinutes,
        @Min(0) int totalVolumeKg,
        @Min(0) @Max(14) int sessionsThisWeek
) {
}
