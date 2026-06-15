package com.gymrank.app.ui.auth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gymrank.app.R;
import com.gymrank.app.data.AuthStore;
import com.gymrank.app.data.ProfileStore;
import com.gymrank.app.network.ApiConfig;
import com.gymrank.app.network.AuthApiClient;
import com.gymrank.app.ui.common.UiFeedback;
import com.gymrank.app.ui.home.HomeActivity;
import com.gymrank.app.ui.onboarding.OnboardingActivity;

public class AuthActivity extends Activity {

    private TextView authTitle;
    private TextView authSubtitle;
    private TextView loginMode;
    private TextView registerMode;
    private EditText displayNameInput;
    private EditText emailInput;
    private EditText passwordInput;
    private TextView authMessage;
    private TextView primaryButton;
    private TextView googleButton;
    private boolean registerModeEnabled = false;
    private boolean loading = false;
    private final Handler authHandler = new Handler(Looper.getMainLooper());
    private int authRequestId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        authTitle = findViewById(R.id.auth_title);
        authSubtitle = findViewById(R.id.auth_subtitle);
        loginMode = findViewById(R.id.login_mode);
        registerMode = findViewById(R.id.register_mode);
        displayNameInput = findViewById(R.id.display_name_input);
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        authMessage = findViewById(R.id.auth_message);
        primaryButton = findViewById(R.id.auth_primary_button);
        googleButton = findViewById(R.id.google_button);

        loginMode.setOnClickListener(v -> UiFeedback.animatePress(v, () -> switchMode(false)));
        registerMode.setOnClickListener(v -> UiFeedback.animatePress(v, () -> switchMode(true)));
        primaryButton.setOnClickListener(v -> UiFeedback.animateContinue(v, this::submit));
        googleButton.setOnClickListener(v -> UiFeedback.animatePress(v, this::showGoogleNotice));

        switchMode(false);
    }

    private void switchMode(boolean register) {
        registerModeEnabled = register;
        displayNameInput.setVisibility(register ? View.VISIBLE : View.GONE);
        authTitle.setText(register ? "Tạo tài khoản GymRank" : "Chào mừng trở lại");
        authSubtitle.setText(register
                ? "Tạo tài khoản để bắt đầu lưu level, rank và chuỗi tập."
                : "Đăng nhập để tiếp tục hành trình tập luyện của bạn.");
        primaryButton.setText(register ? "Đăng ký" : "Đăng nhập");
        authMessage.setText(register
                ? "Bạn có thể dùng email thật để đồng bộ hồ sơ và tiến trình tập luyện."
                : "Hãy đăng nhập hoặc tạo tài khoản để tiếp tục.");

        loginMode.setBackgroundResource(register ? R.drawable.bg_segment_normal : R.drawable.bg_segment_selected);
        registerMode.setBackgroundResource(register ? R.drawable.bg_segment_selected : R.drawable.bg_segment_normal);
        loginMode.setTextColor(getColor(register ? R.color.gr_text_muted : R.color.gr_background));
        registerMode.setTextColor(getColor(register ? R.color.gr_background : R.color.gr_text_muted));
    }

    private void submit() {
        if (loading) {
            return;
        }

        String displayName = displayNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        if (registerModeEnabled && displayName.length() < 2) {
            showMessage("Tên hiển thị cần ít nhất 2 ký tự.");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showMessage("Email chưa đúng định dạng.");
            return;
        }
        if (password.length() < 6) {
            showMessage("Mật khẩu cần ít nhất 6 ký tự.");
            return;
        }

        int requestId = ++authRequestId;
        setLoading(true);
        showMessage("Đang kết nối máy chủ. Nếu server vừa ngủ, lần đầu có thể mất khoảng 1 phút.");
        scheduleAuthTimeout(requestId);

        AuthApiClient.Callback callback = new AuthApiClient.Callback() {
            @Override
            public void onSuccess(AuthApiClient.Result result) {
                runOnUiThread(() -> {
                    if (requestId == authRequestId) {
                        handleSuccess(result);
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    if (requestId == authRequestId) {
                        setLoading(false);
                        showMessage(message);
                    }
                });
            }
        };

        if (registerModeEnabled) {
            AuthApiClient.register(displayName, email, password, callback);
        } else {
            AuthApiClient.login(email, password, callback);
        }
    }

    private void handleSuccess(AuthApiClient.Result result) {
        setLoading(false);
        AuthStore.saveSession(
                this,
                result.getUserId(),
                result.getDisplayName(),
                result.getEmail(),
                result.getToken()
        );

        Class<?> nextScreen = result.isNewUser() || !ProfileStore.isCompleted(this)
                ? OnboardingActivity.class
                : HomeActivity.class;
        startActivity(new Intent(this, nextScreen));
        finish();
    }

    private void scheduleAuthTimeout(int requestId) {
        authHandler.postDelayed(() -> {
            if (loading && requestId == authRequestId) {
                authRequestId++;
                setLoading(false);
                showMessage("Kết nối quá lâu. Hãy kiểm tra mạng hoặc thử lại sau ít phút.");
            }
        }, ApiConfig.NETWORK_TIMEOUT_MS + 5000L);
    }

    private void setLoading(boolean value) {
        loading = value;
        primaryButton.setEnabled(!value);
        primaryButton.setAlpha(value ? 0.6f : 1f);
        primaryButton.setText(value
                ? "Đang xử lý..."
                : registerModeEnabled ? "Đăng ký" : "Đăng nhập");
    }

    private void showMessage(String message) {
        authMessage.setText(message);
    }

    private void showGoogleNotice() {
        Toast.makeText(this, "Đăng nhập bằng Google sẽ được cấu hình ở bước sau.", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        authHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
