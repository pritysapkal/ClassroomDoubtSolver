package com.example.classroomdoubt.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.classroomdoubt.R;
import com.example.classroomdoubt.utils.FirebaseHelper;
import com.example.classroomdoubt.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class FacultyLoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin, btnRegister;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private SessionManager session;

    private final String[] authorizedEmails = {
            "khopkar@gmail.com", "jitkar@gmail.com", "bhosale@gmail.com",
            "terdalkar@gmail.com", "rsjadhav@gmail.com", "nnpatil@gmail.com",
            "anjadhav@gmail.com", "prpatil@gmail.com", "mohite@gmail.com",
            "varadpatil@gmail.com", "yadav@gmail.com", "phadtare@gmail.com"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty_login);

        mAuth = FirebaseAuth.getInstance();
        session = new SessionManager(this);

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);

        btnLogin.setOnClickListener(v -> attemptLogin());

        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, FacultyRegisterActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    private void attemptLogin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim().toLowerCase() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        tilEmail.setError(null);
        tilPassword.setError(null);

        if (TextUtils.isEmpty(email)) { tilEmail.setError("Email required"); return; }
        if (TextUtils.isEmpty(password)) { tilPassword.setError("Password required"); return; }

        // Authorization Check
        boolean isAuthorized = false;
        for (String authEmail : authorizedEmails) {
            if (authEmail.equals(email)) {
                isAuthorized = true;
                break;
            }
        }

        if (!isAuthorized) {
            tilEmail.setError("This email is not authorized as faculty");
            return;
        }

        setLoading(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    FirebaseHelper.getUsersRef().child(uid)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    setLoading(false);
                                    if (snapshot.exists()) {
                                        String role = snapshot.child("role").getValue(String.class);
                                        if ("faculty".equals(role)) {
                                            String name = snapshot.child("name").getValue(String.class);
                                            String subject = snapshot.child("subject").getValue(String.class);
                                            session.saveSession(uid, name != null ? name : "", "faculty", subject != null ? subject : "General");
                                            Intent intent = new Intent(FacultyLoginActivity.this, FacultyDashboardActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                        } else {
                                            mAuth.signOut();
                                            new androidx.appcompat.app.AlertDialog.Builder(FacultyLoginActivity.this)
                                                    .setTitle("Access Denied")
                                                    .setMessage("This account is registered as a Student. Please use the Student Portal.")
                                                    .setPositiveButton("OK", null).show();
                                        }
                                    } else {
                                        mAuth.signOut();
                                        Toast.makeText(FacultyLoginActivity.this, "User profile not found in database.", Toast.LENGTH_LONG).show();
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError error) {
                                    setLoading(false);
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    tilPassword.setError("Invalid credentials");
                });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
    }
}