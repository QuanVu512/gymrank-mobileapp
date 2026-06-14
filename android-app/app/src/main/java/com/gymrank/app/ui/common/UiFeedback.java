package com.gymrank.app.ui.common;

import android.media.ToneGenerator;
import android.view.View;

public final class UiFeedback {

    private UiFeedback() {
    }

    public static void playTing(ToneGenerator toneGenerator) {
        if (toneGenerator != null) {
            toneGenerator.stopTone();
            toneGenerator.startTone(ToneGenerator.TONE_PROP_PROMPT, 180);
        }
    }

    public static void animatePress(View view, Runnable afterAnimation) {
        view.animate().cancel();
        view.setRotation(0f);
        view.setAlpha(1f);
        view.animate()
                .scaleX(0.96f)
                .scaleY(0.96f)
                .alpha(0.88f)
                .setDuration(70)
                .withEndAction(() -> view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .setDuration(120)
                        .withEndAction(afterAnimation)
                        .start())
                .start();
    }

    public static void animateContinue(View view, Runnable afterAnimation) {
        view.animate().cancel();
        view.setRotation(0f);
        view.setScaleX(1f);
        view.setScaleY(1f);
        view.setAlpha(1f);

        view.animate()
                .scaleX(1.12f)
                .scaleY(1.12f)
                .rotation(24f)
                .setDuration(130)
                .withEndAction(() -> view.animate()
                        .scaleX(0.98f)
                        .scaleY(0.98f)
                        .rotation(-8f)
                        .setDuration(90)
                        .withEndAction(() -> view.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .rotation(0f)
                                .setDuration(100)
                                .withEndAction(afterAnimation)
                                .start())
                        .start())
                .start();
    }
}
