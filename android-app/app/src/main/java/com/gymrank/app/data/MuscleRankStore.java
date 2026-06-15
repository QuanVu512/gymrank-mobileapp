package com.gymrank.app.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class MuscleRankStore {

    private static final String PREFS = "gymrank_muscle_rank";
    private static final String KEY_STARTER_WORKOUT = "starter_workout";
    private static final String KEY_EXP = "exp";
    private static final String KEY_RANK_POINTS = "rank_points";
    private static final String MUSCLE_PREFIX = "muscle_";

    private MuscleRankStore() {
    }

    public static boolean hasStarterWorkout(Context context) {
        return prefs(context).getBoolean(KEY_STARTER_WORKOUT, false);
    }

    public static void enableStarterWorkout(Context context) {
        prefs(context).edit().putBoolean(KEY_STARTER_WORKOUT, true).apply();
    }

    public static int getExp(Context context) {
        return prefs(context).getInt(KEY_EXP, 0);
    }

    public static float getRankPoints(Context context) {
        return prefs(context).getFloat(KEY_RANK_POINTS, 0f);
    }

    public static void addExp(Context context, int amount) {
        SharedPreferences preferences = prefs(context);
        preferences.edit().putInt(KEY_EXP, Math.max(0, preferences.getInt(KEY_EXP, 0) + amount)).apply();
    }

    public static void addMusclePoints(Context context, String muscle, float points) {
        if (points <= 0f) {
            return;
        }
        SharedPreferences preferences = prefs(context);
        String key = muscleKey(muscle);
        float nextMuscleScore = preferences.getFloat(key, 0f) + points;
        float nextRankPoints = preferences.getFloat(KEY_RANK_POINTS, 0f) + points;
        preferences.edit()
                .putFloat(key, nextMuscleScore)
                .putFloat(KEY_RANK_POINTS, nextRankPoints)
                .apply();
    }

    public static float getMusclePoints(Context context, String muscle) {
        return prefs(context).getFloat(muscleKey(muscle), 0f);
    }

    public static Rank rankFor(Context context, String muscle) {
        return Rank.fromPoints(getMusclePoints(context, muscle));
    }

    public static Map<String, Float> knownMusclePoints(Context context) {
        String[] muscles = {"chest", "deltoids", "triceps", "quadriceps", "biceps", "upper-back", "trapezius"};
        Map<String, Float> result = new LinkedHashMap<>();
        for (String muscle : muscles) {
            float points = getMusclePoints(context, muscle);
            if (points > 0f) {
                result.put(muscle, points);
            }
        }
        return result;
    }

    public static void clear(Context context) {
        prefs(context).edit().clear().apply();
    }

    private static String muscleKey(String muscle) {
        return MUSCLE_PREFIX + muscle.toLowerCase(Locale.ROOT);
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public enum Rank {
        NONE("#00000000"),
        BRONZE("#C8753D"),
        SILVER("#C9D3DC"),
        GOLD("#F7B733"),
        PLATINUM("#64D7FF");

        public final int color;

        Rank(String color) {
            this.color = Color.parseColor(color);
        }

        public static Rank fromPoints(float points) {
            if (points >= 40f) {
                return PLATINUM;
            }
            if (points >= 20f) {
                return GOLD;
            }
            if (points >= 8f) {
                return SILVER;
            }
            if (points > 0f) {
                return BRONZE;
            }
            return NONE;
        }
    }
}

