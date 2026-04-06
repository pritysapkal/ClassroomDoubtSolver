package com.example.classroomdoubt.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.classroomdoubt.R;
import com.example.classroomdoubt.models.User;
import com.example.classroomdoubt.utils.FirebaseHelper;
import com.example.classroomdoubt.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class StudentRegisterActivity extends AppCompatActivity {

    private TextInputLayout tilName, tilEnrollment, tilEmail, tilPassword;
    private TextInputEditText etName, etEnrollment, etEmail, etPassword;
    private MaterialButton btnRegister;
    private ProgressBar progressBar;
    private TextView tvLogin;
    private FirebaseAuth mAuth;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_student);

        mAuth = FirebaseAuth.getInstance();
        session = new SessionManager(this);

        tilName = findViewById(R.id.tilName);
        tilEnrollment = findViewById(R.id.tilEnrollment);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etName = findViewById(R.id.etName);
        etEnrollment = findViewById(R.id.etEnrollment);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
        tvLogin = findViewById(R.id.tvLogin);

        btnRegister.setOnClickListener(v -> attemptRegister());
        tvLogin.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String enrollment = etEnrollment.getText() != null ? etEnrollment.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        // Clear errors
        tilName.setError(null); tilEnrollment.setError(null);
        tilEmail.setError(null); tilPassword.setError(null);

        boolean valid = true;
        if (TextUtils.isEmpty(name)) { tilName.setError("Name required"); valid = false; }
        if (TextUtils.isEmpty(enrollment)) { tilEnrollment.setError("Enrollment required"); valid = false; }
        if (TextUtils.isEmpty(email)) { tilEmail.setError("Email required"); valid = false; }
        if (password.length() < 6) { tilPassword.setError("Min 6 characters"); valid = false; }
        if (!valid) return;

        setLoading(true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    User user = new User(uid, name, email, "student");
                    user.setEnrollmentId(enrollment);

                    FirebaseHelper.getUsersRef().child(uid).setValue(user)
                            .addOnSuccessListener(unused -> {
                                setLoading(false);
                                session.saveSession(uid, name, "student", "");
                                Intent intent = new Intent(this, StudentDashboardActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                Toast.makeText(this, "Welcome, " + name + "!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                setLoading(false);
                                Toast.makeText(this, "Failed to save data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    tilEmail.setError(e.getMessage());
                });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!loading);
    }
}