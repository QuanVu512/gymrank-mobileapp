package com.gymrank.app.network;

import android.content.Context;

import com.gymrank.app.data.AuthStore;
import com.gymrank.app.ui.workout.WorkoutExercise;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class WorkoutApiClient {

    private WorkoutApiClient() {
    }

    public static void syncWorkout(Context context, WorkoutExercise exercise, int reps, int sets, float weightKg, Callback callback) {
        Context appContext = context.getApplicationContext();
        Thread thread = new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                JSONObject body = new JSONObject();
                body.put("exerciseCode", exercise.code);
                body.put("reps", reps);
                body.put("sets", sets);
                body.put("weightKg", weightKg);

                URL url = new URL(ApiConfig.BASE_URL + "/workouts/log");
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(ApiConfig.NETWORK_TIMEOUT_MS);
                connection.setReadTimeout(ApiConfig.NETWORK_TIMEOUT_MS);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setRequestProperty("Accept", "application/json");

                String token = AuthStore.getToken(appContext);
                if (!token.isEmpty()) {
                    connection.setRequestProperty("Authorization", "Bearer " + token);
                }
                connection.setDoOutput(true);

                byte[] payload = body.toString().getBytes(StandardCharsets.UTF_8);
                try (OutputStream outputStream = connection.getOutputStream()) {
                    outputStream.write(payload);
                }

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
                    callback.onError("Chưa đồng bộ được bài tập. Mã lỗi: " + statusCode);
                    return;
                }

                JSONObject response = new JSONObject(responseText);
                callback.onSuccess(new Result(
                        response.optInt("expGained"),
                        (float) response.optDouble("rankGained"),
                        response.optBoolean("cheatLike"),
                        response.optInt("totalExp"),
                        response.optInt("totalRankPoints")
                ));
            } catch (Exception exception) {
                callback.onError("Đã lưu trên máy. Sẽ cần đồng bộ lại khi mạng ổn.");
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
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

    public interface Callback {
        void onSuccess(Result result);

        void onError(String message);
    }

    public static final class Result {
        private final int expGained;
        private final float rankGained;
        private final boolean cheatLike;
        private final int totalExp;
        private final int totalRankPoints;

        private Result(int expGained, float rankGained, boolean cheatLike, int totalExp, int totalRankPoints) {
            this.expGained = expGained;
            this.rankGained = rankGained;
            this.cheatLike = cheatLike;
            this.totalExp = totalExp;
            this.totalRankPoints = totalRankPoints;
        }

        public int getExpGained() {
            return expGained;
        }

        public float getRankGained() {
            return rankGained;
        }

        public boolean isCheatLike() {
            return cheatLike;
        }

        public int getTotalExp() {
            return totalExp;
        }

        public int getTotalRankPoints() {
            return totalRankPoints;
        }
    }
}
