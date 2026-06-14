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
                .putString(KEY_NAME, profile.getOrDefault(KEY_NAME, "Ban"))
                .putString(KEY_EXPERIENCE, profile.getOrDefault(KEY_EXPERIENCE, "BEGINNER"))
                .putString(KEY_GOAL, profile.getOrDefault(KEY_GOAL, "CONSISTENT"))
                .putString(KEY_DAYS, profile.getOrDefault(KEY_DAYS, "3"))
                .putString(KEY_BODYGRAPH, profile.getOrDefault(KEY_BODYGRAPH, "SKIP"))
                .apply();
    }

    public static String getName(Context context) {
        return prefs(context).getString(KEY_NAME, "Ban");
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
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
