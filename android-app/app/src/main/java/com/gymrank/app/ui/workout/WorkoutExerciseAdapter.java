package com.gymrank.app.ui.workout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gymrank.app.R;
import com.gymrank.app.ui.common.UiFeedback;

import java.util.List;

public final class WorkoutExerciseAdapter extends BaseAdapter {

    private final Context context;
    private final List<WorkoutExercise> exercises;
    private final OnWorkoutSaved listener;

    public WorkoutExerciseAdapter(Context context, List<WorkoutExercise> exercises, OnWorkoutSaved listener) {
        this.context = context;
        this.exercises = exercises;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return exercises.size();
    }

    @Override
    public WorkoutExercise getItem(int position) {
        return exercises.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_workout_exercise, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        WorkoutExercise exercise = getItem(position);
        holder.name.setText(exercise.name);
        holder.muscles.setText(exercise.musclesText);
        holder.hint.setText(exercise.note);
        holder.detail.setVisibility(exercise.expanded ? View.VISIBLE : View.GONE);
        holder.arrow.setText(exercise.expanded ? "⌃" : "⌄");
        holder.result.setText(exercise.lastResult);

        holder.header.setOnClickListener(v -> UiFeedback.animatePress(v, () -> {
            exercise.expanded = !exercise.expanded;
            notifyDataSetChanged();
            listener.onListHeightChanged();
        }));

        holder.save.setOnClickListener(v -> UiFeedback.animatePress(v, () -> saveExercise(exercise, holder)));
        return convertView;
    }

    private void saveExercise(WorkoutExercise exercise, ViewHolder holder) {
        int reps = parseInt(holder.reps.getText().toString());
        int sets = parseInt(holder.sets.getText().toString());
        float weight = parseFloat(holder.weight.getText().toString());

        if (reps <= 0 || sets <= 0 || weight <= 0f) {
            Toast.makeText(context, "Nhập đủ reps, set và kg trước khi lưu.", Toast.LENGTH_SHORT).show();
            return;
        }

        ScoreResult score = calculateScore(reps, sets, weight);
        listener.onWorkoutSaved(exercise, score.rankGain, score.expGain, score.cheatLike);
        exercise.lastResult = score.cheatLike
                ? "Bài này nghiêng cardio/cheat: +" + score.expGain + " EXP, không cộng rank."
                : "Đã lưu: +" + format(score.rankGain) + " điểm rank nền, +" + score.expGain + " EXP.";
        holder.result.setText(exercise.lastResult);
        listener.onListHeightChanged();
        hideKeyboard(holder.weight);
    }

    private ScoreResult calculateScore(int reps, int sets, float weight) {
        boolean tooMuchLowWeightVolume = reps >= 15 && sets >= 10 && weight <= 10f;
        boolean suspiciousVolume = sets > 15 || reps > 30;
        if (tooMuchLowWeightVolume || suspiciousVolume) {
            int expGain = Math.max(1, Math.round((reps * sets) / 60f));
            return new ScoreResult(0f, expGain, true);
        }

        float volume = reps * sets * weight;
        float rankGain = (float) Math.sqrt(volume) / 8f;
        int expGain = Math.max(2, Math.round((reps * sets) / 8f));
        return new ScoreResult(rankGain, expGain, false);
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private float parseFloat(String value) {
        try {
            return Float.parseFloat(value.trim());
        } catch (NumberFormatException exception) {
            return 0f;
        }
    }

    private String format(float value) {
        return String.format(java.util.Locale.US, "%.1f", value);
    }

    private void hideKeyboard(View view) {
        InputMethodManager manager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (manager != null) {
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public interface OnWorkoutSaved {
        void onWorkoutSaved(WorkoutExercise exercise, float rankGain, int expGain, boolean cheatLike);

        void onListHeightChanged();
    }

    private static final class ScoreResult {
        private final float rankGain;
        private final int expGain;
        private final boolean cheatLike;

        private ScoreResult(float rankGain, int expGain, boolean cheatLike) {
            this.rankGain = rankGain;
            this.expGain = expGain;
            this.cheatLike = cheatLike;
        }
    }

    private static final class ViewHolder {
        private final View header;
        private final View detail;
        private final TextView name;
        private final TextView muscles;
        private final TextView arrow;
        private final TextView hint;
        private final TextView save;
        private final TextView result;
        private final EditText reps;
        private final EditText sets;
        private final EditText weight;

        private ViewHolder(View root) {
            header = root.findViewById(R.id.exercise_header);
            detail = root.findViewById(R.id.exercise_detail);
            name = root.findViewById(R.id.exercise_name);
            muscles = root.findViewById(R.id.exercise_muscles);
            arrow = root.findViewById(R.id.exercise_arrow);
            hint = root.findViewById(R.id.exercise_hint);
            save = root.findViewById(R.id.save_progress);
            result = root.findViewById(R.id.save_result);
            reps = root.findViewById(R.id.input_reps);
            sets = root.findViewById(R.id.input_sets);
            weight = root.findViewById(R.id.input_weight);
        }
    }
}
