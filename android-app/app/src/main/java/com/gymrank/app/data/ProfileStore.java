package com.gymrank.app.data;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

public final class ProfileStore {

    private static final String PREFS_NAME = "gymrank_profile";
    private static final String KEY_COMPLETED = "completed";
    public static final String KEY_NAME = "name";
    public static final String KEY_EXPERIENCE = "experience";
    public static final String KEY_GOAL = "goal";
    public static final String KEY_DAYS = "days";
    public static final String KEY_BODYGRAPH = "bodygraph";

    private ProfileStore() {
    }

    public static boolean isCompleted(Context context) {
        return prefs(context).getBoolean(KEY_COMPLETED, false);
    }

    public static void saveProfile(Context context, Map<String, String> profile) {
        prefs(context).edit()
                .putBoolean(KEY_COMPLETED, true)
                .putString(KEY_NAME, profile.getOrDefault(KEY_NAME, "Bạn"))
                .putString(KEY_EXPERIENCE, profile.getOrDefault(KEY_EXPERIENCE, "BEGINNER"))
                .putString(KEY_GOAL, profile.getOrDefault(KEY_GOAL, "CONSISTENT"))
                .putString(KEY_DAYS, profile.getOrDefault(KEY_DAYS, "3"))
                .putString(KEY_BODYGRAPH, profile.getOrDefault(KEY_BODYGRAPH, "SKIP"))
                .apply();
    }

    public static void saveProfile(Context context, String displayName, String experience, String goal, int trainingDays, String bodygraphType) {
        prefs(context).edit()
                .putBoolean(KEY_COMPLETED, true)
                .putString(KEY_NAME, displayName == null || displayName.isBlank() ? "Bạn" : displayName)
                .putString(KEY_EXPERIENCE, experience == null || experience.isBlank() ? "BEGINNER" : experience)
                .putString(KEY_GOAL, goal == null || goal.isBlank() ? "CONSISTENT" : goal)
                .putString(KEY_DAYS, String.valueOf(trainingDays <= 0 ? 3 : trainingDays))
                .putString(KEY_BODYGRAPH, bodygraphType == null || bodygraphType.isBlank() ? "SKIP" : bodygraphType)
                .apply();
    }

    public static String getName(Context context) {
        return prefs(context).getString(KEY_NAME, "Bạn");
    }

    public static String getGoal(Context context) {
        return prefs(context).getString(KEY_GOAL, "CONSISTENT");
    }

    public static String getExperience(Context context) {
        return prefs(context).getString(KEY_EXPERIENCE, "BEGINNER");
    }

    public static String getTrainingDays(Context context) {
        return prefs(context).getString(KEY_DAYS, "3");
    }

    public static String getBodygraph(Context context) {
        return prefs(context).getString(KEY_BODYGRAPH, "SKIP");
    }

    public static void clear(Context context) {
        prefs(context).edit().clear().apply();
    }

    private static SharedPreferences prefs(Context context) {
        String userId = AuthStore.getUserId(context);
        String prefsName = userId == null || userId.isEmpty() ? PREFS_NAME : PREFS_NAME + "_" + userId;
        return context.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
    }
}
