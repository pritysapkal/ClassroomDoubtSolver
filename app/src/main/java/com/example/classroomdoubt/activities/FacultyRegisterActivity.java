package com.example.classroomdoubt.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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

public class FacultyRegisterActivity extends AppCompatActivity {

    private TextInputLayout tilName, tilFacultyId, tilSubject, tilEmail, tilPassword;
    private TextInputEditText etName, etFacultyId, etEmail, etPassword;
    private AutoCompleteTextView actvSubject;
    private MaterialButton btnRegister;
    private ProgressBar progressBar;
    private TextView tvLogin;
    private FirebaseAuth mAuth;
    private SessionManager session;

    private final String[] subjects = {
            "Compiler Design (CD)",
            "Structured Database (SDB)",
            "Scripting Concepts (SC)",
            "PEC-II: Data Visualization Techniques",
            "PEC-II: Introduction to Machine Learning",
            "PEC-II: Software Architecture",
            "PEC-III: Professional Elective-III",
            "PEC-III: Prompt Engineering",
            "PEC-III: Data Analytics",
            "PEC-III Lab",
            "Mobile Applications Development (MAD)",
            "Liberal Learning (LLC)"
    };

    private final String[] authorizedEmails = {
            "khopkar@gmail.com", "jitkar@gmail.com", "bhosale@gmail.com",
            "terdalkar@gmail.com", "rsjadhav@gmail.com", "nnpatil@gmail.com",
            "anjadhav@gmail.com", "prpatil@gmail.com", "mohite@gmail.com",
            "varadpatil@gmail.com", "yadav@gmail.com", "phadtare@gmail.com"
    };

    private final String[] facultyNames = {
            "Ms. P. V. Khopkar",
            "Dr. B. D. Jitkar",
            "Mrs. K. A. Bhosale",
            "Mr. O. A. Terdalkar",
            "Mrs. R. S. Jadhav",
            "Mrs. N. N. Patil",
            "Dr. A. N. Jadhav",
            "Mrs. P. R. Patil",
            "Dr. S. B. Mohite",
            "Mr. Varad S. Patil",
            "Dr. A. S. Yadav",
            "Ms. A. M. Phadtare"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_faculty);

        mAuth = FirebaseAuth.getInstance();
        session = new SessionManager(this);

        tilName = findViewById(R.id.tilName);
        tilFacultyId = findViewById(R.id.tilFacultyId);
        tilSubject = findViewById(R.id.tilSubject);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etName = findViewById(R.id.etName);
        etFacultyId = findViewById(R.id.etFacultyId);
        actvSubject = findViewById(R.id.actvSubject);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
        tvLogin = findViewById(R.id.tvLogin);

        // Setup Subject Dropdown
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, subjects);
        actvSubject.setAdapter(adapter);

        // Auto-fill Name based on selected Subject
        actvSubject.setOnItemClickListener((parent, view, position, id) -> {
            etName.setText(facultyNames[position]);
        });

        btnRegister.setOnClickListener(v -> attemptRegister());
        tvLogin.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String facultyId = etFacultyId.getText() != null ? etFacultyId.getText().toString().trim() : "";
        String subject = actvSubject.getText().toString().trim();
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        tilName.setError(null); tilFacultyId.setError(null); tilSubject.setError(null);
        tilEmail.setError(null); tilPassword.setError(null);

        boolean valid = true;
        if (TextUtils.isEmpty(name)) { tilName.setError("Name required"); valid = false; }
        if (TextUtils.isEmpty(facultyId)) { tilFacultyId.setError("Faculty ID required"); valid = false; }
        if (TextUtils.isEmpty(subject)) { tilSubject.setError("Subject required"); valid = false; }
        if (TextUtils.isEmpty(email)) { tilEmail.setError("Email required"); valid = false; }
        if (password.length() < 6) { tilPassword.setError("Min 6 characters"); valid = false; }
        if (!valid) return;

        setLoading(true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    User user = new User(uid, name, email, "faculty");
                    user.setFacultyId(facultyId);
                    user.setSubject(subject);

                    FirebaseHelper.getUsersRef().child(uid).setValue(user)
                            .addOnSuccessListener(unused -> {
                                setLoading(false);
                                session.saveSession(uid, name, "faculty", subject);
                                Intent intent = new Intent(this, FacultyDashboardActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                Toast.makeText(this, "Welcome, Prof. " + name + "!", Toast.LENGTH_SHORT).show();
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