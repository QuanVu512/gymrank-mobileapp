package com.gymrank.app.network;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class AuthApiClient {

    private AuthApiClient() {
    }

    public static void register(String displayName, String email, String password, Callback callback) {
        sendAuthRequest("/auth/register", displayName, email, password, callback);
    }

    public static void login(String email, String password, Callback callback) {
        sendAuthRequest("/auth/login", "", email, password, callback);
    }

    private static void sendAuthRequest(String path, String displayName, String email, String password, Callback callback) {
        Thread thread = new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                JSONObject body = new JSONObject();
                body.put("displayName", displayName);
                body.put("email", email);
                body.put("password", password);

                URL url = new URL(ApiConfig.BASE_URL + path);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setRequestProperty("Accept", "application/json");
                connection.setDoOutput(true);

                byte[] payload = body.toString().getBytes(StandardCharsets.UTF_8);
                try (OutputStream outputStream = connection.getOutputStream()) {
                    outputStream.write(payload);
                }

                int statusCode = connection.getResponseCode();
                String responseText = readResponse(statusCode >= 400
                        ? connection.getErrorStream()
                        : connection.getInputStream());

                if (statusCode < 200 || statusCode >= 300) {
                    callback.onError(readErrorMessage(responseText, statusCode));
                    return;
                }

                JSONObject response = new JSONObject(responseText);
                callback.onSuccess(new Result(
                        response.optString("userId"),
                        response.optString("displayName"),
                        response.optString("email"),
                        response.optString("token"),
                        response.optBoolean("newUser")
                ));
            } catch (Exception exception) {
                callback.onError("Khong ket noi duoc backend. Hay kiem tra Wi-Fi, IP laptop va Spring Boot.");
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

    private static String readErrorMessage(String responseText, int statusCode) {
        try {
            JSONObject response = new JSONObject(responseText);
            String message = response.optString("message");
            if (!message.isEmpty()) {
                return message;
            }
        } catch (Exception ignored) {
        }
        return "Dang nhap that bai. Ma loi: " + statusCode;
    }

    public interface Callback {
        void onSuccess(Result result);

        void onError(String message);
    }

    public static final class Result {
        private final String userId;
        private final String displayName;
        private final String email;
        private final String token;
        private final boolean newUser;

        public Result(String userId, String displayName, String email, String token, boolean newUser) {
            this.userId = userId;
            this.displayName = displayName;
            this.email = email;
            this.token = token;
            this.newUser = newUser;
        }

        public String getUserId() {
            return userId;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getEmail() {
            return email;
        }

        public String getToken() {
            return token;
        }

        public boolean isNewUser() {
            return newUser;
        }
    }
}
