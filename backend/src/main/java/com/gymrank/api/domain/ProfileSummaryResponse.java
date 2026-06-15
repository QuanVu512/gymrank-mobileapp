package com.gymrank.api.domain;

import java.time.Instant;
import java.util.Map;

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
        Map<String, Double> muscleRankPoints,
        boolean synced,
        Instant updatedAt
) {
}
