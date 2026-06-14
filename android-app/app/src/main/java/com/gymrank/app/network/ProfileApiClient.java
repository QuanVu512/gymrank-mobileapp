package com.gymrank.app.network;

import android.content.Context;
import android.util.Log;

import com.gymrank.app.data.AuthStore;
import com.gymrank.app.data.ProfileStore;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class ProfileApiClient {

    private static final String TAG = "ProfileApiClient";

    private ProfileApiClient() {
    }

    public static void syncOnboardingProfile(Context context) {
        Context appContext = context.getApplicationContext();
        Thread syncThread = new Thread(() -> postOnboardingProfile(appContext));
        syncThread.setDaemon(true);
        syncThread.start();
    }

    private static void postOnboardingProfile(Context context) {
        HttpURLConnection connection = null;
        try {
            JSONObject body = new JSONObject();
            body.put("userId", AuthStore.getUserId(context));
            body.put("displayName", ProfileStore.getName(context));
            body.put("experienceLevel", ProfileStore.getExperience(context));
            body.put("mainGoal", ProfileStore.getGoal(context));
            body.put("trainingDaysPerWeek", Integer.parseInt(ProfileStore.getTrainingDays(context)));
            body.put("bodygraphType", ProfileStore.getBodygraph(context));

            byte[] payload = body.toString().getBytes(StandardCharsets.UTF_8);
            URL url = new URL(ApiConfig.BASE_URL + "/onboarding/profile");
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(4000);
            connection.setReadTimeout(4000);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(payload);
            }

            int statusCode = connection.getResponseCode();
            if (statusCode < 200 || statusCode >= 300) {
                Log.w(TAG, "Profile sync failed with status: " + statusCode);
            } else {
                Log.i(TAG, "Profile synced to backend.");
            }
        } catch (Exception exception) {
            Log.w(TAG, "Profile sync skipped. Backend may be offline.", exception);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
