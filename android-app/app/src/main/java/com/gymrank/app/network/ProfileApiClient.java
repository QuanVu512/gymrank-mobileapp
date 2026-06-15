package com.gymrank.app.network;

import android.content.Context;
import android.util.Log;

import com.gymrank.app.data.AuthStore;
import com.gymrank.app.data.ProfileStore;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

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

    public static void fetchSummary(Context context, SummaryCallback callback) {
        Context appContext = context.getApplicationContext();
        Thread thread = new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(ApiConfig.BASE_URL + "/me/summary");
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(ApiConfig.NETWORK_TIMEOUT_MS);
                connection.setReadTimeout(ApiConfig.NETWORK_TIMEOUT_MS);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");
                addAuthorizationHeader(appContext, connection);

                int statusCode = connection.getResponseCode();
                if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    SessionExpiredHandler.handle(appContext);
                    callback.onError("Phiên đăng nhập đã hết hạn.");
                    return;
                }
                String responseText = readResponse(statusCode >= 400
                        ? connection.getErrorStream()
                        : connection.getInputStream());
                if (statusCode < 200 || statusCode >= 300) {
                    callback.onError("Không tải được hồ sơ. Mã lỗi: " + statusCode);
                    return;
                }

                JSONObject response = new JSONObject(responseText);
                JSONObject muscleJson = response.optJSONObject("muscleRankPoints");
                Map<String, Float> muscleRankPoints = new LinkedHashMap<>();
                if (muscleJson != null) {
                    Iterator<String> keys = muscleJson.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        muscleRankPoints.put(key, (float) muscleJson.optDouble(key));
                    }
                }
                callback.onSuccess(new Summary(
                        response.optString("displayName"),
                        response.optString("experienceLevel"),
                        response.optString("mainGoal"),
                        response.optInt("trainingDaysPerWeek", 3),
                        response.optString("bodygraphType"),
                        response.optInt("exp", 0),
                        response.optInt("rankPoints", 0),
                        muscleRankPoints,
                        response.optBoolean("synced")
                ));
            } catch (Exception exception) {
                callback.onError("Không tải được hồ sơ. Hãy thử lại.");
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
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
            connection.setConnectTimeout(ApiConfig.NETWORK_TIMEOUT_MS);
            connection.setReadTimeout(ApiConfig.NETWORK_TIMEOUT_MS);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");
            addAuthorizationHeader(context, connection);
            connection.setDoOutput(true);

            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(payload);
            }

            int statusCode = connection.getResponseCode();
            if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                SessionExpiredHandler.handle(context);
                return;
            }
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

    private static void addAuthorizationHeader(Context context, HttpURLConnection connection) {
        String token = AuthStore.getToken(context);
        if (!token.isEmpty()) {
            connection.setRequestProperty("Authorization", "Bearer " + token);
        }
    }

    private static String readResponse(InputStream inputStream) throws Exception {
        if (inputStream == null) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }

    public interface SummaryCallback {
        void onSuccess(Summary summary);

        void onError(String message);
    }

    public static final class Summary {
        private final String displayName;
        private final String experienceLevel;
        private final String mainGoal;
        private final int trainingDaysPerWeek;
        private final String bodygraphType;
        private final int exp;
        private final int rankPoints;
        private final Map<String, Float> muscleRankPoints;
        private final boolean synced;

        private Summary(String displayName, String experienceLevel, String mainGoal, int trainingDaysPerWeek, String bodygraphType, int exp, int rankPoints, Map<String, Float> muscleRankPoints, boolean synced) {
            this.displayName = displayName;
            this.experienceLevel = experienceLevel;
            this.mainGoal = mainGoal;
            this.trainingDaysPerWeek = trainingDaysPerWeek;
            this.bodygraphType = bodygraphType;
            this.exp = exp;
            this.rankPoints = rankPoints;
            this.muscleRankPoints = muscleRankPoints;
            this.synced = synced;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getExperienceLevel() {
            return experienceLevel;
        }

        public String getMainGoal() {
            return mainGoal;
        }

        public int getTrainingDaysPerWeek() {
            return trainingDaysPerWeek;
        }

        public String getBodygraphType() {
            return bodygraphType;
        }

        public int getExp() {
            return exp;
        }

        public int getRankPoints() {
            return rankPoints;
        }

        public Map<String, Float> getMuscleRankPoints() {
            return muscleRankPoints;
        }

        public boolean isSynced() {
            return synced;
        }
    }
}
