package com.gymrank.app.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gymrank.app.R;
import com.gymrank.app.data.AuthStore;
import com.gymrank.app.data.MuscleRankStore;
import com.gymrank.app.data.ProfileStore;
import com.gymrank.app.network.AuthApiClient;
import com.gymrank.app.network.WorkoutApiClient;
import com.gymrank.app.ui.auth.AuthActivity;
import com.gymrank.app.ui.common.UiFeedback;
import com.gymrank.app.ui.workout.WorkoutExercise;
import com.gymrank.app.ui.workout.WorkoutExerciseAdapter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeActivity extends Activity implements WorkoutExerciseAdapter.OnWorkoutSaved {

    private View homeScreen;
    private View workoutScreen;
    private View bodyScreen;
    private View profileScreen;
    private TextView homeName;
    private TextView homeGoal;
    private TextView homeHint;
    private TextView expValue;
    private TextView rankValue;
    private TextView homeAction;
    private TextView workoutAction;
    private TextView workoutIntro;
    private TextView bodygraphStatus;
    private TextView profileEmail;
    private TextView profileLogoutButton;
    private TextView tabHome;
    private TextView tabWorkout;
    private TextView tabBody;
    private TextView tabProfile;
    private ProgressBar expProgress;
    private ListView workoutList;
    private WorkoutExerciseAdapter workoutAdapter;
    private final Map<String, ImageView> muscleOverlays = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        homeScreen = findViewById(R.id.home_screen);
        workoutScreen = findViewById(R.id.workout_screen);
        bodyScreen = findViewById(R.id.body_screen);
        profileScreen = findViewById(R.id.profile_screen);

        homeName = findViewById(R.id.home_name);
        homeGoal = findViewById(R.id.home_goal);
        homeHint = findViewById(R.id.home_hint);
        expValue = findViewById(R.id.exp_value);
        rankValue = findViewById(R.id.rank_value);
        homeAction = findViewById(R.id.home_primary_action);
        workoutAction = findViewById(R.id.workout_primary_action);
        workoutIntro = findViewById(R.id.workout_intro);
        workoutList = findViewById(R.id.workout_list);
        bodygraphStatus = findViewById(R.id.bodygraph_status);
        profileEmail = findViewById(R.id.profile_email);
        profileLogoutButton = findViewById(R.id.profile_logout_button);
        tabHome = findViewById(R.id.tab_home);
        tabWorkout = findViewById(R.id.tab_workout);
        tabBody = findViewById(R.id.tab_body);
        tabProfile = findViewById(R.id.tab_profile);
        expProgress = findViewById(R.id.exp_progress);

        setupWorkoutList();
        setupBodygraphOverlays();

        tabHome.setOnClickListener(v -> UiFeedback.animatePress(v, this::showHome));
        tabWorkout.setOnClickListener(v -> UiFeedback.animatePress(v, this::showWorkout));
        tabBody.setOnClickListener(v -> UiFeedback.animatePress(v, this::showBody));
        tabProfile.setOnClickListener(v -> UiFeedback.animatePress(v, this::showProfile));
        homeAction.setOnClickListener(v -> UiFeedback.animatePress(v, this::createStarterWorkout));
        workoutAction.setOnClickListener(v -> UiFeedback.animatePress(v, this::createStarterWorkout));
        profileLogoutButton.setOnClickListener(v -> UiFeedback.animatePress(v, this::logout));

        refreshStats();
        updateWorkoutUi();
        updateBodygraph();
        showHome();
    }

    private void setupWorkoutList() {
        List<WorkoutExercise> exercises = new ArrayList<>();
        exercises.add(WorkoutExercise.chestPress());
        exercises.add(WorkoutExercise.legPress());
        exercises.add(WorkoutExercise.latPullDown());
        workoutAdapter = new WorkoutExerciseAdapter(this, exercises, this);
        workoutList.setAdapter(workoutAdapter);
    }

    private void setupBodygraphOverlays() {
        FrameLayout front = findViewById(R.id.bodygraph_front_container);
        FrameLayout back = findViewById(R.id.bodygraph_back_container);

        addOverlay(front, "chest", R.drawable.bodygraph_overlay_front_chest);
        addOverlay(front, "deltoids", R.drawable.bodygraph_overlay_front_deltoids);
        addOverlay(front, "triceps", R.drawable.bodygraph_overlay_front_triceps);
        addOverlay(front, "quadriceps", R.drawable.bodygraph_overlay_front_quadriceps);
        addOverlay(front, "biceps", R.drawable.bodygraph_overlay_front_biceps);
        addOverlay(back, "upper-back", R.drawable.bodygraph_overlay_back_upper_back);
        addOverlay(back, "trapezius", R.drawable.bodygraph_overlay_back_trapezius);
    }

    private void addOverlay(FrameLayout container, String muscle, int drawableRes) {
        ImageView overlay = new ImageView(this);
        overlay.setImageResource(drawableRes);
        overlay.setScaleType(ImageView.ScaleType.FIT_CENTER);
        overlay.setAdjustViewBounds(true);
        overlay.setVisibility(View.GONE);
        container.addView(overlay, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        overlay.bringToFront();
        muscleOverlays.put(muscle, overlay);
    }

    private void createStarterWorkout() {
        MuscleRankStore.enableStarterWorkout(this);
        updateWorkoutUi();
        showWorkout();
    }

    private void showHome() {
        showScreen(homeScreen, tabHome);
    }

    private void showWorkout() {
        showScreen(workoutScreen, tabWorkout);
    }

    private void showBody() {
        showScreen(bodyScreen, tabBody);
    }

    private void showProfile() {
        showScreen(profileScreen, tabProfile);
    }

    private void refreshStats() {
        String days = ProfileStore.getTrainingDays(this);
        int exp = MuscleRankStore.getExp(this);
        float rankPoint = MuscleRankStore.getRankPoints(this);
        homeName.setText("Chào, " + ProfileStore.getName(this));
        homeGoal.setText("Mục tiêu: " + readableGoal(ProfileStore.getGoal(this)));
        homeHint.setText("Bạn đặt mục tiêu " + days + " buổi mỗi tuần. Hãy tạo lịch tập đầu tiên để bắt đầu tính EXP, rank và bodygraph.");
        expValue.setText(exp + " / 1000");
        rankValue.setText(String.format(Locale.US, "%.1f RP", rankPoint));
        expProgress.setProgress(Math.min(exp, 1000));
        profileEmail.setText("Email: " + AuthStore.getEmail(this));
    }

    private void updateWorkoutUi() {
        boolean hasStarterWorkout = MuscleRankStore.hasStarterWorkout(this);
        workoutAction.setVisibility(hasStarterWorkout ? View.GONE : View.VISIBLE);
        workoutList.setVisibility(hasStarterWorkout ? View.VISIBLE : View.GONE);
        workoutIntro.setText(hasStarterWorkout
                ? "Nhập kết quả từng bài để kiểm tra cách EXP, rank và bodygraph đổi màu."
                : "Tạo lịch tập để mở 3 bài cơ bản và thử hệ thống tính rank theo nhóm cơ.");
        workoutList.post(this::updateWorkoutListHeight);
    }

    private void updateBodygraph() {
        for (Map.Entry<String, ImageView> entry : muscleOverlays.entrySet()) {
            MuscleRankStore.Rank rank = MuscleRankStore.rankFor(this, entry.getKey());
            ImageView overlay = entry.getValue();
            overlay.setVisibility(rank == MuscleRankStore.Rank.NONE ? View.GONE : View.VISIBLE);
            overlay.setColorFilter(rank.color);
        }

        Map<String, Float> activeMuscles = MuscleRankStore.knownMusclePoints(this);
        bodygraphStatus.setText(activeMuscles.isEmpty()
                ? "Chưa có dữ liệu rank"
                : "Đang có điểm: " + activeMuscleSummary(activeMuscles));
    }

    private void updateWorkoutListHeight() {
        ListAdapter adapter = workoutList.getAdapter();
        if (adapter == null || adapter.getCount() == 0 || workoutList.getVisibility() != View.VISIBLE) {
            return;
        }

        int width = getResources().getDisplayMetrics().widthPixels - dp(40);
        int widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.AT_MOST);
        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View item = adapter.getView(i, null, workoutList);
            item.measure(widthSpec, View.MeasureSpec.UNSPECIFIED);
            totalHeight += item.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = workoutList.getLayoutParams();
        params.height = totalHeight + workoutList.getDividerHeight() * Math.max(0, adapter.getCount() - 1);
        workoutList.setLayoutParams(params);
        workoutList.requestLayout();
    }

    @Override
    public void onWorkoutSaved(WorkoutExercise exercise, int reps, int sets, float weightKg, float rankGain, int expGain, boolean cheatLike) {
        MuscleRankStore.addExp(this, expGain);
        if (!cheatLike) {
            for (Map.Entry<String, Float> target : exercise.muscleWeights.entrySet()) {
                MuscleRankStore.addMusclePoints(this, target.getKey(), rankGain * target.getValue());
            }
        }

        refreshStats();
        updateBodygraph();
        syncWorkoutToBackend(exercise, reps, sets, weightKg);
        Toast.makeText(this, cheatLike ? "Đã cộng EXP nhẹ, không cộng rank." : "Đã cộng rank cho nhóm cơ.", Toast.LENGTH_SHORT).show();
    }

    private void syncWorkoutToBackend(WorkoutExercise exercise, int reps, int sets, float weightKg) {
        WorkoutApiClient.syncWorkout(this, exercise, reps, sets, weightKg, new WorkoutApiClient.Callback() {
            @Override
            public void onSuccess(WorkoutApiClient.Result result) {
                runOnUiThread(() -> Toast.makeText(HomeActivity.this, "Đã đồng bộ rank lên máy chủ.", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(HomeActivity.this, message, Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    public void onListHeightChanged() {
        workoutList.post(this::updateWorkoutListHeight);
    }

    private void showScreen(View selectedScreen, TextView selectedTab) {
        homeScreen.setVisibility(selectedScreen == homeScreen ? View.VISIBLE : View.GONE);
        workoutScreen.setVisibility(selectedScreen == workoutScreen ? View.VISIBLE : View.GONE);
        bodyScreen.setVisibility(selectedScreen == bodyScreen ? View.VISIBLE : View.GONE);
        profileScreen.setVisibility(selectedScreen == profileScreen ? View.VISIBLE : View.GONE);
        selectBottomTab(selectedTab);
    }

    private void selectBottomTab(TextView selectedTab) {
        TextView[] tabs = {tabHome, tabWorkout, tabBody, tabProfile};
        for (TextView tab : tabs) {
            boolean selected = tab == selectedTab;
            tab.setBackgroundResource(selected ? R.drawable.bg_bottom_nav_item_selected : 0);
            tab.setTextColor(getColor(selected ? R.color.gr_background : R.color.gr_text_muted));
            tab.setAlpha(selected ? 1f : 0.82f);
        }
    }

    private String readableGoal(String goal) {
        return switch (goal) {
            case "BUILD_MUSCLE" -> "Tăng cơ";
            case "LOSE_WEIGHT" -> "Giảm mỡ";
            case "GET_STRONGER" -> "Khỏe hơn";
            case "RANK" -> "Cày rank";
            default -> "Giữ thói quen";
        };
    }

    private String activeMuscleSummary(Map<String, Float> muscles) {
        StringBuilder summary = new StringBuilder();
        int count = 0;
        for (Map.Entry<String, Float> muscle : muscles.entrySet()) {
            if (count > 0) {
                summary.append(", ");
            }
            summary.append(readableMuscle(muscle.getKey()))
                    .append(" ")
                    .append(String.format(Locale.US, "%.1f", muscle.getValue()));
            count++;
        }
        return summary.toString();
    }

    private String readableMuscle(String muscle) {
        return switch (muscle) {
            case "chest" -> "Ngực";
            case "deltoids" -> "Vai";
            case "triceps" -> "Tay sau";
            case "quadriceps" -> "Đùi trước";
            case "biceps" -> "Tay trước";
            case "upper-back" -> "Lưng trên";
            case "trapezius" -> "Trap";
            default -> muscle;
        };
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void logout() {
        AuthApiClient.logout(this);
        AuthStore.clear(this);
        startActivity(new Intent(this, AuthActivity.class));
        finish();
    }
}
