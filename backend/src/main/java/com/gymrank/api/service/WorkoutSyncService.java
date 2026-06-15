package com.gymrank.api.service;

import com.gymrank.api.domain.WorkoutLogRequest;
import com.gymrank.api.domain.WorkoutLogResponse;
import com.gymrank.api.persistence.AppUser;
import com.gymrank.api.persistence.UserMuscleRankStat;
import com.gymrank.api.persistence.UserMuscleRankStatRepository;
import com.gymrank.api.persistence.UserStats;
import com.gymrank.api.persistence.UserStatsRepository;
import com.gymrank.api.persistence.WorkoutRankLog;
import com.gymrank.api.persistence.WorkoutRankLogRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class WorkoutSyncService {

    private final UserStatsRepository userStatsRepository;
    private final UserMuscleRankStatRepository muscleRankStatRepository;
    private final WorkoutRankLogRepository workoutRankLogRepository;

    public WorkoutSyncService(
            UserStatsRepository userStatsRepository,
            UserMuscleRankStatRepository muscleRankStatRepository,
            WorkoutRankLogRepository workoutRankLogRepository
    ) {
        this.userStatsRepository = userStatsRepository;
        this.muscleRankStatRepository = muscleRankStatRepository;
        this.workoutRankLogRepository = workoutRankLogRepository;
    }

    @Transactional
    public WorkoutLogResponse logWorkout(AppUser user, WorkoutLogRequest request) {
        ExerciseRule rule = exerciseRule(request.exerciseCode());
        Score score = calculateScore(request.reps(), request.sets(), request.weightKg());

        UserStats stats = userStatsRepository.findById(user.getId())
                .orElseGet(() -> userStatsRepository.save(new UserStats(user)));
        stats.addWorkoutResult(score.expGain(), score.rankGain());

        Map<String, Double> muscleTotals = new LinkedHashMap<>();
        if (!score.cheatLike()) {
            for (Map.Entry<String, Double> target : rule.muscleWeights().entrySet()) {
                double gain = score.rankGain() * target.getValue();
                UserMuscleRankStat muscleStat = muscleRankStatRepository
                        .findByUserIdAndMuscleCode(user.getId(), target.getKey())
                        .orElseGet(() -> new UserMuscleRankStat(user, target.getKey()));
                muscleStat.addRankPoints(gain);
                muscleRankStatRepository.save(muscleStat);
            }
        }

        workoutRankLogRepository.save(new WorkoutRankLog(
                user,
                request.exerciseCode(),
                request.reps(),
                request.sets(),
                request.weightKg(),
                score.expGain(),
                score.rankGain(),
                score.cheatLike()
        ));

        for (UserMuscleRankStat muscleStat : muscleRankStatRepository.findByUserIdOrderByMuscleCodeAsc(user.getId())) {
            muscleTotals.put(muscleStat.getMuscleCode(), muscleStat.getRankPoints());
        }

        return new WorkoutLogResponse(
                request.exerciseCode(),
                score.expGain(),
                score.rankGain(),
                score.cheatLike(),
                stats.getExpPoints(),
                stats.getRankPoints(),
                muscleTotals
        );
    }

    private ExerciseRule exerciseRule(String exerciseCode) {
        return switch (exerciseCode) {
            case "chest_press" -> new ExerciseRule(Map.of(
                    "chest", 1d,
                    "deltoids", 0.5d,
                    "triceps", 0.5d
            ));
            case "leg_press" -> new ExerciseRule(Map.of(
                    "quadriceps", 1d
            ));
            case "lat_pull_down" -> new ExerciseRule(Map.of(
                    "upper-back", 1d,
                    "trapezius", 0.5d,
                    "biceps", 0.35d
            ));
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bài tập chưa được hỗ trợ.");
        };
    }

    private Score calculateScore(int reps, int sets, double weightKg) {
        boolean tooMuchLowWeightVolume = reps >= 15 && sets >= 10 && weightKg <= 10d;
        boolean suspiciousVolume = sets > 15 || reps > 30;
        if (tooMuchLowWeightVolume || suspiciousVolume) {
            int expGain = Math.max(1, Math.round((reps * sets) / 60f));
            return new Score(expGain, 0d, true);
        }

        double volume = reps * sets * weightKg;
        double rankGain = Math.sqrt(volume) / 8d;
        int expGain = Math.max(2, Math.round((reps * sets) / 8f));
        return new Score(expGain, rankGain, false);
    }

    private record ExerciseRule(Map<String, Double> muscleWeights) {
    }

    private record Score(int expGain, double rankGain, boolean cheatLike) {
    }
}
