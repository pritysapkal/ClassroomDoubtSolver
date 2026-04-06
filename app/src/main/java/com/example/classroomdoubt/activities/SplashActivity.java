package com.example.classroomdoubt.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.classroomdoubt.R;
import com.example.classroomdoubt.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            SessionManager session = new SessionManager(this);

            if (session.isLoggedIn()) {
                // Navigate based on role
                String role = session.getRole();
                Intent intent;

                if ("faculty".equals(role)) {
                    intent = new Intent(this, FacultyDashboardActivity.class);
                } else {
                    intent = new Intent(this, StudentDashboardActivity.class);
                }

                startActivity(intent);
            } else {
                // If not logged in → go to role selection
                startActivity(new Intent(this, RoleSelectionActivity.class));
            }

            finish();

        }, SPLASH_DELAY);
    }
}