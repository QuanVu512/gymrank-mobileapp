package com.gymrank.app.network;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.gymrank.app.data.AuthStore;
import com.gymrank.app.ui.auth.AuthActivity;

public final class SessionExpiredHandler {

    private static boolean handlingSessionExpired = false;

    private SessionExpiredHandler() {
    }

    public static void handle(Context context) {
        synchronized (SessionExpiredHandler.class) {
            if (handlingSessionExpired) {
                return;
            }
            handlingSessionExpired = true;
        }

        Context appContext = context.getApplicationContext();
        AuthStore.clear(appContext);

        new Handler(Looper.getMainLooper()).post(() -> {
            Intent intent = new Intent(appContext, AuthActivity.class);
            intent.putExtra(AuthActivity.EXTRA_SESSION_EXPIRED, true);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            appContext.startActivity(intent);
        });
    }

    public static void reset() {
        synchronized (SessionExpiredHandler.class) {
            handlingSessionExpired = false;
        }
    }
}
