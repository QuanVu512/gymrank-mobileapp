package com.gymrank.api.domain;

import java.util.Map;

public record WorkoutLogResponse(
        String exerciseCode,
        int expGained,
        double rankGained,
        boolean cheatLike,
        int totalExp,
        int totalRankPoints,
        Map<String, Double> muscleRankPoints
) {
}
