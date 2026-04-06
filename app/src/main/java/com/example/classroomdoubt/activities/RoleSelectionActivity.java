package com.example.classroomdoubt.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.classroomdoubt.R;

public class RoleSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        LinearLayout logoSection = findViewById(R.id.logoSection);
        CardView cardStudent = findViewById(R.id.cardStudent);
        CardView cardFaculty = findViewById(R.id.cardFaculty);

        // Load animations
        Animation bounce = AnimationUtils.loadAnimation(this, R.anim.bounce);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        Animation slideUpDelay = AnimationUtils.loadAnimation(this,
                R.anim.slide_up_delay);

        // Start animations
        logoSection.startAnimation(bounce);
        cardStudent.startAnimation(slideUp);
        cardFaculty.startAnimation(slideUpDelay);

        // Student card click with scale animation
        cardStudent.setOnClickListener(v -> {
            cardStudent.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        cardStudent.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .withEndAction(() -> {
                                    Intent intent = new Intent(RoleSelectionActivity.this,
                                            StudentLoginActivity.class);
                                    startActivity(intent);
                                    overridePendingTransition(
                                            R.anim.slide_in_right,
                                            R.anim.slide_out_left);
                                }).start();
                    }).start();
        });

        // Faculty card click with scale animation
        cardFaculty.setOnClickListener(v -> {
            cardFaculty.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        cardFaculty.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .withEndAction(() -> {
                                    Intent intent = new Intent(RoleSelectionActivity.this,
                                            FacultyLoginActivity.class);
                                    startActivity(intent);
                                    overridePendingTransition(
                                            R.anim.slide_in_right,
                                            R.anim.slide_out_left);
                                }).start();
                    }).start();
        });
    }
}


