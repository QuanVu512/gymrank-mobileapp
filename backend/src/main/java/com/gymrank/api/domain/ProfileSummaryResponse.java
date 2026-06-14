package com.gymrank.api.domain;

import java.time.Instant;

public record ProfileSummaryResponse(
        String displayName,
        String experienceLevel,
        String mainGoal,
        int trainingDaysPerWeek,
        String bodygraphType,
        int level,
        int exp,
        int streak,
        int rankPoints,
        boolean synced,
        Instant updatedAt
) {
}
