package com.gymrank.app.ui.auth;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gymrank.app.R;
import com.gymrank.app.data.AuthStore;
import com.gymrank.app.data.ProfileStore;
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
    private ToneGenerator toneGenerator;
    private boolean registerModeEnabled = false;
    private boolean loading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);

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

    @Override
    protected void onDestroy() {
        if (toneGenerator != null) {
            toneGenerator.release();
        }
        super.onDestroy();
    }

    private void switchMode(boolean register) {
        UiFeedback.playTing(toneGenerator);
        registerModeEnabled = register;
        displayNameInput.setVisibility(register ? View.VISIBLE : View.GONE);
        authTitle.setText(register ? "Tao tai khoan GymRank" : "Chao mung tro lai");
        authSubtitle.setText(register
                ? "Tao tai khoan de bat dau luu level, rank va streak."
                : "Dang nhap de tiep tuc hanh trinh tap luyen cua ban.");
        primaryButton.setText(register ? "Dang ky" : "Dang nhap");
        authMessage.setText(register
                ? "Ban co the dung email that de sau nay nang cap len database online va Google login."
                : "Backend local can dang chay tren laptop va dien thoai phai cung mang Wi-Fi.");

        loginMode.setBackgroundResource(register ? R.drawable.bg_segment_normal : R.drawable.bg_segment_selected);
        registerMode.setBackgroundResource(register ? R.drawable.bg_segment_selected : R.drawable.bg_segment_normal);
        loginMode.setTextColor(getColor(register ? R.color.gr_text_muted : R.color.gr_background));
        registerMode.setTextColor(getColor(register ? R.color.gr_background : R.color.gr_text_muted));
    }

    private void submit() {
        if (loading) {
            return;
        }

        UiFeedback.playTing(toneGenerator);
        String displayName = displayNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        if (registerModeEnabled && displayName.length() < 2) {
            showMessage("Ten hien thi can it nhat 2 ky tu.");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showMessage("Email chua dung dinh dang.");
            return;
        }
        if (password.length() < 6) {
            showMessage("Mat khau can it nhat 6 ky tu.");
            return;
        }

        setLoading(true);
        AuthApiClient.Callback callback = new AuthApiClient.Callback() {
            @Override
            public void onSuccess(AuthApiClient.Result result) {
                runOnUiThread(() -> handleSuccess(result));
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    setLoading(false);
                    showMessage(message);
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
        UiFeedback.playTing(toneGenerator);

        Class<?> nextScreen = result.isNewUser() || !ProfileStore.isCompleted(this)
                ? OnboardingActivity.class
                : HomeActivity.class;
        startActivity(new Intent(this, nextScreen));
        finish();
    }

    private void setLoading(boolean value) {
        loading = value;
        primaryButton.setEnabled(!value);
        primaryButton.setAlpha(value ? 0.6f : 1f);
        primaryButton.setText(value
                ? "Dang xu ly..."
                : registerModeEnabled ? "Dang ky" : "Dang nhap");
    }

    private void showMessage(String message) {
        authMessage.setText(message);
    }

    private void showGoogleNotice() {
        UiFeedback.playTing(toneGenerator);
        Toast.makeText(this, "Google login lam duoc, nhung can cau hinh OAuth/Firebase sau.", Toast.LENGTH_LONG).show();
    }
}
