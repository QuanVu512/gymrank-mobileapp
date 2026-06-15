package com.gymrank.app.ui.onboarding;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
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

    private MediaPlayer answerPlayer;
    private final Map<String, String> draftProfile = new HashMap<>();
    private int onboardingStep = 0;
    private String selectedValue = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        answerPlayer = MediaPlayer.create(this, R.raw.freesound_community_ding2_89720);
        if (answerPlayer != null) {
            answerPlayer.setVolume(2f, 2f);
        }

        backButton = findViewById(R.id.back_button);
        onboardingProgress = findViewById(R.id.onboarding_progress);
        coachText = findViewById(R.id.coach_text);
        questionTitle = findViewById(R.id.question_title);
        nameInput = findViewById(R.id.name_input);
        optionsContainer = findViewById(R.id.options_container);
        helperText = findViewById(R.id.helper_text);
        nextButton = findViewById(R.id.next_button);

        backButton.setOnClickListener(v -> UiFeedback.animatePress(v, this::goBack));
        nextButton.setOnClickListener(v -> UiFeedback.animateContinue(v, () -> {
            playAnswerTing();
            goNext();
        }));
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
        if (answerPlayer != null) {
            answerPlayer.release();
            answerPlayer = null;
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
                coachText.setText("Chào bạn, mình sẽ hỏi thật ngắn để tạo lịch tập phù hợp.");
                questionTitle.setText("Mình nên gọi bạn là gì?");
                helperText.setText("Tên này sẽ hiển thị ở trang chính. Bạn có thể đổi sau.");
                nameInput.setVisibility(View.VISIBLE);
                nameInput.setText(draftProfile.getOrDefault(ProfileStore.KEY_NAME, ""));
                nameInput.requestFocus();
                setNextEnabled(nameInput.getText().toString().trim().length() >= 2);
                break;
            case 1:
                hideKeyboard();
                coachText.setText("Tốt rồi. Mục này giúp app gợi ý bài tập an toàn hơn.");
                questionTitle.setText("Kinh nghiệm tập của bạn?");
                helperText.setText("Hãy chọn mức gần với hiện tại nhất.");
                addOption("Chưa từng tập", "Cần lịch thật nhẹ, ưu tiên an toàn.", "NEVER");
                addOption("Mới bắt đầu", "Đã thử tập một vài lần.", "BEGINNER");
                addOption("Tập đều", "Tập khá thường xuyên.", "INTERMEDIATE");
                addOption("Nhiều kinh nghiệm", "Biết rõ bài tập và kỹ thuật.", "ADVANCED");
                break;
            case 2:
                coachText.setText("Mục tiêu sẽ quyết định app ưu tiên EXP hay Rank Point.");
                questionTitle.setText("Mục tiêu chính của bạn?");
                helperText.setText("Chỉ cần chọn một mục chính cho giai đoạn đầu.");
                addOption("Tăng cơ", "Ưu tiên rank nhóm cơ và sức mạnh.", "BUILD_MUSCLE");
                addOption("Giảm mỡ", "Ưu tiên vận động đều và cardio.", "LOSE_WEIGHT");
                addOption("Khỏe hơn", "Cân bằng cơ, tim mạch và thói quen.", "GET_STRONGER");
                addOption("Giữ thói quen", "Tập đủ 3 buổi mỗi tuần.", "CONSISTENT");
                addOption("Cày rank", "Tập theo hệ rank và bodygraph.", "RANK");
                break;
            case 3:
                coachText.setText("Đừng ép quá sức. Lịch tốt là lịch bạn giữ được.");
                questionTitle.setText("Bạn có thể tập mấy buổi mỗi tuần?");
                helperText.setText("Chuỗi MVP sẽ giữ khi đủ 3 buổi trong tuần.");
                addOption("2 buổi", "Nhẹ, hợp với người rất bận.", "2");
                addOption("3 buổi", "Khuyến nghị để giữ chuỗi.", "3");
                addOption("4 buổi", "Tốt nếu bạn đã quen vận động.", "4");
                addOption("5+ buổi", "Cần lịch chia nhóm cơ cẩn thận.", "5");
                break;
            case 4:
                coachText.setText("Bước cuối để app hiển thị bodygraph phù hợp hơn.");
                questionTitle.setText("Mẫu bodygraph bạn muốn dùng?");
                helperText.setText("Lựa chọn này chỉ để hiển thị cơ thể.");
                addOption("Nam", "Dùng bodygraph nam.", "MALE");
                addOption("Nữ", "Dùng bodygraph nữ.", "FEMALE");
                addOption("Chọn sau", "Vào app trước, cập nhật sau.", "SKIP");
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
        if (onboardingStep > 0) {
            onboardingStep--;
            showStep();
        }
    }

    private void goNext() {
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

    private void playAnswerTing() {
        if (answerPlayer == null) {
            return;
        }
        try {
            if (answerPlayer.isPlaying()) {
                answerPlayer.pause();
            }
            answerPlayer.seekTo(0);
            answerPlayer.start();
        } catch (IllegalStateException ignored) {
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
