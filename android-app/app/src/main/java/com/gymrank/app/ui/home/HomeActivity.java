package com.gymrank.app.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gymrank.app.R;
import com.gymrank.app.data.AuthStore;
import com.gymrank.app.data.ProfileStore;
import com.gymrank.app.ui.common.UiFeedback;
import com.gymrank.app.ui.auth.AuthActivity;

public class HomeActivity extends Activity {

    private TextView screenTitle;
    private TextView screenSubtitle;
    private TextView primaryAction;
    private TextView homeName;
    private TextView homeGoal;
    private TextView streakValue;
    private TextView expValue;
    private TextView rankValue;
    private ProgressBar expProgress;
    private ToneGenerator toneGenerator;

    private int streak = 2;
    private int exp = 620;
    private int rankPoint = 410;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);

        screenTitle = findViewById(R.id.screen_title);
        screenSubtitle = findViewById(R.id.screen_subtitle);
        primaryAction = findViewById(R.id.primary_action);
        homeName = findViewById(R.id.home_name);
        homeGoal = findViewById(R.id.home_goal);
        streakValue = findViewById(R.id.streak_value);
        expValue = findViewById(R.id.exp_value);
        rankValue = findViewById(R.id.rank_value);
        expProgress = findViewById(R.id.exp_progress);

        findViewById(R.id.tab_today).setOnClickListener(v -> UiFeedback.animatePress(v, this::showToday));
        findViewById(R.id.tab_workout).setOnClickListener(v -> UiFeedback.animatePress(v, this::showWorkout));
        findViewById(R.id.tab_body).setOnClickListener(v -> UiFeedback.animatePress(v, this::showBody));
        findViewById(R.id.tab_profile).setOnClickListener(v -> UiFeedback.animatePress(v, this::showProfile));

        refreshStats();
        showToday();
    }

    @Override
    protected void onDestroy() {
        if (toneGenerator != null) {
            toneGenerator.release();
        }
        super.onDestroy();
    }

    private void showToday() {
        UiFeedback.playTing(toneGenerator);
        primaryAction.setOnClickListener(v -> UiFeedback.animatePress(v, this::completeDemoWorkout));
        screenTitle.setText("Hom nay");
        String days = ProfileStore.getTrainingDays(this);
        screenSubtitle.setText("Lich goi y: " + days + " buoi moi tuan. Hom nay nen tap full body nhe de giu nhip.");
        primaryAction.setText("Bat dau buoi tap");
    }

    private void showWorkout() {
        UiFeedback.playTing(toneGenerator);
        primaryAction.setOnClickListener(v -> UiFeedback.animatePress(v, () -> UiFeedback.playTing(toneGenerator)));
        screenTitle.setText("Tap luyen");
        screenSubtitle.setText("Routine dau tien: hit dat, squat khong ta, plank. Se them tracker set/rep o buoc sau.");
        primaryAction.setText("Tao routine");
    }

    private void showBody() {
        UiFeedback.playTing(toneGenerator);
        primaryAction.setOnClickListener(v -> UiFeedback.animatePress(v, () -> UiFeedback.playTing(toneGenerator)));
        screenTitle.setText("Bodygraph");
        screenSubtitle.setText("Bodygraph se to mau theo rank nhom co. Hien tai dang o che do demo.");
        primaryAction.setText("Xem nhom co");
    }

    private void showProfile() {
        UiFeedback.playTing(toneGenerator);
        primaryAction.setOnClickListener(v -> UiFeedback.animatePress(v, this::logout));
        screenTitle.setText("Ho so");
        screenSubtitle.setText("Email: " + AuthStore.getEmail(this) + "\nHo so da dong bo theo userId local. Database online se lam o buoc sau.");
        primaryAction.setText("Dang xuat");
    }

    private void completeDemoWorkout() {
        UiFeedback.playTing(toneGenerator);
        streak += 1;
        exp += 80;
        rankPoint += 45;
        refreshStats();
        screenTitle.setText("Da ghi buoi tap");
        screenSubtitle.setText("Cong 80 EXP va 45 Rank Point. Tiep tuc giu nhip 3 buoi moi tuan.");
        primaryAction.setText("Ghi them buoi tap");
    }

    private void refreshStats() {
        homeName.setText("Chao, " + ProfileStore.getName(this));
        homeGoal.setText("Muc tieu: " + readableGoal(ProfileStore.getGoal(this)));
        streakValue.setText(String.valueOf(streak));
        expValue.setText(exp + " / 1000");
        rankValue.setText(rankPoint + " RP");
        expProgress.setProgress(Math.min(exp, 1000));
    }

    private String readableGoal(String goal) {
        return switch (goal) {
            case "BUILD_MUSCLE" -> "Tang co";
            case "LOSE_WEIGHT" -> "Giam mo";
            case "GET_STRONGER" -> "Khoe hon";
            case "RANK" -> "Cay rank";
            default -> "Giu thoi quen";
        };
    }

    private void logout() {
        UiFeedback.playTing(toneGenerator);
        AuthStore.clear(this);
        ProfileStore.clear(this);
        startActivity(new Intent(this, AuthActivity.class));
        finish();
    }
}
