package com.gymrank.app.ui.workout;

import java.util.LinkedHashMap;
import java.util.Map;

public final class WorkoutExercise {

    public final String code;
    public final String name;
    public final String musclesText;
    public final String note;
    public final Map<String, Float> muscleWeights;
    public boolean expanded;
    public String lastResult = "";

    private WorkoutExercise(String code, String name, String musclesText, String note, Map<String, Float> muscleWeights) {
        this.code = code;
        this.name = name;
        this.musclesText = musclesText;
        this.note = note;
        this.muscleWeights = muscleWeights;
    }

    public static WorkoutExercise chestPress() {
        Map<String, Float> weights = new LinkedHashMap<>();
        weights.put("chest", 1f);
        weights.put("deltoids", 0.5f);
        weights.put("triceps", 0.5f);
        return new WorkoutExercise(
                "chest_press",
                "Chest Press",
                "Ngực, vai, tay sau",
                "Bài đẩy ngực máy/tạ. Rank chính vào ngực, phụ vào vai và tay sau.",
                weights
        );
    }

    public static WorkoutExercise legPress() {
        Map<String, Float> weights = new LinkedHashMap<>();
        weights.put("quadriceps", 1f);
        return new WorkoutExercise(
                "leg_press",
                "Leg Press",
                "Đùi trước",
                "Bài đạp chân. Bản demo cộng rank chính cho nhóm đùi trước.",
                weights
        );
    }

    public static WorkoutExercise latPullDown() {
        Map<String, Float> weights = new LinkedHashMap<>();
        weights.put("upper-back", 1f);
        weights.put("trapezius", 0.5f);
        weights.put("biceps", 0.35f);
        return new WorkoutExercise(
                "lat_pull_down",
                "Lat Pull Down",
                "Lưng trên, trap, tay trước",
                "Bài kéo xô. Rank chính vào lưng trên, phụ vào trap và tay trước.",
                weights
        );
    }
}
