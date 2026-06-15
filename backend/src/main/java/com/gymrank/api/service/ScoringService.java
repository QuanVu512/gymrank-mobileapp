package com.gymrank.api.service;

import com.gymrank.api.domain.ActivityType;
import com.gymrank.api.domain.WorkoutScoreRequest;
import com.gymrank.api.domain.WorkoutScoreResponse;
import org.springframework.stereotype.Service;

@Service
public class ScoringService {

    public WorkoutScoreResponse preview(WorkoutScoreRequest request) {
        int exp = calculateExp(request);
        int rankPoints = calculateRankPoints(request);
        boolean streakKept = request.sessionsThisWeek() >= 3;
        String note = streakKept
                ? "Tuần này đủ 3 buổi, chuỗi được giữ."
                : "Cần đủ 3 buổi trong tuần để giữ chuỗi.";

        return new WorkoutScoreResponse(exp, rankPoints, streakKept, note);
    }

    private int calculateExp(WorkoutScoreRequest request) {
        return switch (request.activityType()) {
            case STRENGTH -> request.durationMinutes() * 2 + request.totalVolumeKg() / 100;
            case CARDIO -> request.durationMinutes() * 4;
            case SPORT -> request.durationMinutes() * 3;
        };
    }

    private int calculateRankPoints(WorkoutScoreRequest request) {
        return switch (request.activityType()) {
            case STRENGTH -> request.durationMinutes() + request.totalVolumeKg() / 50;
            case CARDIO -> request.durationMinutes() / 3;
            case SPORT -> request.durationMinutes() / 4;
        };
    }
}
