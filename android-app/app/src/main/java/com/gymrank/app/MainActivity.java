package com.gymrank.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.gymrank.app.data.AuthStore;
import com.gymrank.app.data.ProfileStore;
import com.gymrank.app.ui.auth.AuthActivity;
import com.gymrank.app.ui.home.HomeActivity;
import com.gymrank.app.ui.onboarding.OnboardingActivity;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Class<?> nextScreen;
        if (!AuthStore.isLoggedIn(this)) {
            nextScreen = AuthActivity.class;
        } else if (ProfileStore.isCompleted(this)) {
            nextScreen = HomeActivity.class;
        } else {
            nextScreen = OnboardingActivity.class;
        }

        startActivity(new Intent(this, nextScreen));
        finish();
    }
}
