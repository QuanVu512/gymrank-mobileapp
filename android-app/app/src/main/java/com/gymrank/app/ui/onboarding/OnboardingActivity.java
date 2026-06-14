package com.gymrank.app.ui.onboarding;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gymrank.app.R;
import com.gymrank.app.data.ProfileStore;
import com.gymrank.app.network.ProfileApiClient;
import com.gymrank.app.ui.common.UiFeedback;
import com.gymrank.app.ui.home.HomeActivity;

import java.util.HashMap;
import java.util.Map;

public class OnboardingActivity extends Activity {

    private TextView backButton;
    private ProgressBar onboardingProgress;
    private TextView coachText;
    private TextView questionTitle;
    private EditText nameInput;
    private LinearLayout optionsContainer;
    private TextView helperText;
    private TextView nextButton;

    private ToneGenerator toneGenerator;
    private final Map<String, String> draftProfile = new HashMap<>();
    private int onboardingStep = 0;
    private String selectedValue = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);

        backButton = findViewById(R.id.back_button);
        onboardingProgress = findViewById(R.id.onboarding_progress);
        coachText = findViewById(R.id.coach_text);
        questionTitle = findViewById(R.id.question_title);
        nameInput = findViewById(R.id.name_input);
        optionsContainer = findViewById(R.id.options_container);
        helperText = findViewById(R.id.helper_text);
        nextButton = findViewById(R.id.next_button);

        backButton.setOnClickListener(v -> UiFeedback.animatePress(v, this::goBack));
        nextButton.setOnClickListener(v -> UiFeedback.animateContinue(v, this::goNext));
        nameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setNextEnabled(s.toString().trim().length() >= 2);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        showStep();
    }

    @Override
    protected void onDestroy() {
        if (toneGenerator != null) {
            toneGenerator.release();
        }
        super.onDestroy();
    }

    private void showStep() {
        selectedValue = "";
        optionsContainer.removeAllViews();
        nameInput.setVisibility(View.GONE);
        backButton.setVisibility(onboardingStep == 0 ? View.INVISIBLE : View.VISIBLE);
        onboardingProgress.setProgress((onboardingStep + 1) * 20);
        setNextEnabled(false);

        View root = findViewById(R.id.onboarding_root);
        root.setAlpha(0.88f);
        root.animate().alpha(1f).setDuration(150).start();

        switch (onboardingStep) {
            case 0:
                coachText.setText("Chao ban, minh se hoi that ngan de tao lich tap phu hop.");
                questionTitle.setText("Minh nen goi ban la gi?");
                helperText.setText("Ten nay se hien o trang Home. Ban co the doi sau.");
                nameInput.setVisibility(View.VISIBLE);
                nameInput.setText(draftProfile.getOrDefault(ProfileStore.KEY_NAME, ""));
                nameInput.requestFocus();
                setNextEnabled(nameInput.getText().toString().trim().length() >= 2);
                break;
            case 1:
                hideKeyboard();
                coachText.setText("Tot roi. Muc nay giup app goi y bai tap an toan hon.");
                questionTitle.setText("Kinh nghiem tap cua ban?");
                helperText.setText("Hay chon muc gan voi hien tai nhat.");
                addOption("Chua tung tap", "Can lich that nhe, uu tien an toan.", "NEVER");
                addOption("Moi bat dau", "Da thu tap mot vai lan.", "BEGINNER");
                addOption("Tap deu", "Tap kha thuong xuyen.", "INTERMEDIATE");
                addOption("Nhieu kinh nghiem", "Biet ro bai tap va ky thuat.", "ADVANCED");
                break;
            case 2:
                coachText.setText("Muc tieu se quyet dinh app uu tien EXP hay Rank Point.");
                questionTitle.setText("Muc tieu chinh cua ban?");
                helperText.setText("Chi can chon mot muc chinh cho giai doan dau.");
                addOption("Tang co", "Uu tien rank nhom co va suc manh.", "BUILD_MUSCLE");
                addOption("Giam mo", "Uu tien van dong deu va cardio.", "LOSE_WEIGHT");
                addOption("Khoe hon", "Can bang co, tim mach va thoi quen.", "GET_STRONGER");
                addOption("Giu thoi quen", "Tap du 3 buoi moi tuan.", "CONSISTENT");
                addOption("Cay rank", "Tap theo he rank va bodygraph.", "RANK");
                break;
            case 3:
                coachText.setText("Dung ep qua suc. Lich tot la lich ban giu duoc.");
                questionTitle.setText("Ban co the tap may buoi moi tuan?");
                helperText.setText("Streak MVP se giu khi du 3 buoi trong tuan.");
                addOption("2 buoi", "Nhe, hop voi nguoi rat ban.", "2");
                addOption("3 buoi", "Khuyen nghi de giu streak.", "3");
                addOption("4 buoi", "Tot neu ban da quen van dong.", "4");
                addOption("5+ buoi", "Can lich chia nhom co can than.", "5");
                break;
            case 4:
                coachText.setText("Buoc cuoi de app hien bodygraph phu hop hon.");
                questionTitle.setText("Mau bodygraph ban muon dung?");
                helperText.setText("Lua chon nay chi de hien thi co the.");
                addOption("Nam", "Dung bodygraph nam.", "MALE");
                addOption("Nu", "Dung bodygraph nu.", "FEMALE");
                addOption("Chon sau", "Vao app truoc, cap nhat sau.", "SKIP");
                break;
            default:
                finishOnboarding();
                break;
        }
    }

    private void addOption(String title, String subtitle, String value) {
        TextView option = new TextView(this);
        option.setText(title + "\n" + subtitle);
        option.setTextColor(getColor(R.color.gr_text));
        option.setTextSize(16);
        option.setLineSpacing(5, 1f);
        option.setPadding(dp(18), dp(13), dp(18), dp(13));
        option.setBackgroundResource(R.drawable.bg_option_normal);
        option.setTag(value);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dp(12));
        option.setLayoutParams(params);

        option.setOnClickListener(v -> selectOption((TextView) v, true));
        optionsContainer.addView(option);

        if (getSavedDraftValueForStep().equals(value)) {
            selectOption(option, false);
        }
    }

    private void selectOption(TextView option, boolean playFeedback) {
        for (int i = 0; i < optionsContainer.getChildCount(); i++) {
            View child = optionsContainer.getChildAt(i);
            child.setBackgroundResource(R.drawable.bg_option_normal);
            child.setScaleX(1f);
            child.setScaleY(1f);
            child.setAlpha(1f);
        }

        selectedValue = String.valueOf(option.getTag());
        option.setBackgroundResource(R.drawable.bg_option_selected);
        setNextEnabled(true);

        if (playFeedback) {
            UiFeedback.playTing(toneGenerator);
            UiFeedback.animatePress(option, () -> {
            });
        }
    }

    private String getSavedDraftValueForStep() {
        return switch (onboardingStep) {
            case 1 -> draftProfile.getOrDefault(ProfileStore.KEY_EXPERIENCE, "");
            case 2 -> draftProfile.getOrDefault(ProfileStore.KEY_GOAL, "");
            case 3 -> draftProfile.getOrDefault(ProfileStore.KEY_DAYS, "");
            case 4 -> draftProfile.getOrDefault(ProfileStore.KEY_BODYGRAPH, "");
            default -> "";
        };
    }

    private void goBack() {
        UiFeedback.playTing(toneGenerator);
        if (onboardingStep > 0) {
            onboardingStep--;
            showStep();
        }
    }

    private void goNext() {
        UiFeedback.playTing(toneGenerator);
        if (onboardingStep == 0) {
            String name = nameInput.getText().toString().trim();
            if (name.length() < 2) {
                setNextEnabled(false);
                return;
            }
            draftProfile.put(ProfileStore.KEY_NAME, name);
        } else {
            if (selectedValue.isEmpty()) {
                setNextEnabled(false);
                return;
            }
            switch (onboardingStep) {
                case 1 -> draftProfile.put(ProfileStore.KEY_EXPERIENCE, selectedValue);
                case 2 -> draftProfile.put(ProfileStore.KEY_GOAL, selectedValue);
                case 3 -> draftProfile.put(ProfileStore.KEY_DAYS, selectedValue);
                case 4 -> draftProfile.put(ProfileStore.KEY_BODYGRAPH, selectedValue);
                default -> {
                }
            }
        }

        onboardingStep++;
        if (onboardingStep >= 5) {
            finishOnboarding();
        } else {
            showStep();
        }
    }

    private void finishOnboarding() {
        ProfileStore.saveProfile(this, draftProfile);
        ProfileApiClient.syncOnboardingProfile(this);
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    private void setNextEnabled(boolean enabled) {
        nextButton.setEnabled(enabled);
        nextButton.setAlpha(enabled ? 1f : 0.48f);
        nextButton.setBackgroundResource(enabled ? R.drawable.bg_card_blue : R.drawable.bg_button_disabled);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View current = getCurrentFocus();
        if (imm != null && current != null) {
            imm.hideSoftInputFromWindow(current.getWindowToken(), 0);
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
