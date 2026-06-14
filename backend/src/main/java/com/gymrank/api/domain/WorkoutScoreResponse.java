package com.gymrank.api.domain;

public record WorkoutScoreResponse(
        int expGained,
        int rankPointsGained,
        boolean streakKept,
        String note
) {
}
