package com.example.classroomdoubt.activities;

import android.os.Bundle;
import android.content.Intent;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.classroomdoubt.R;
import com.example.classroomdoubt.utils.FirebaseHelper;
import com.example.classroomdoubt.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import android.widget.TextView;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvName, tvEmail, tvRole,
            tvEnrollmentOrFacultyId,
            tvTotalDoubts, tvAnsweredDoubts, tvInitial;
    private MaterialButton btnLogout, btnChangePassword, btnDepartment;
    private android.view.View rowDepartment;
    private SessionManager session;

    private final String[] DEPARTMENTS = {
            "CSE", "DS", "AIML", "CHEMICAL", "CIVIL", "MECHANICAL", "ELECTRICAL"
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        session = new SessionManager(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvRole = findViewById(R.id.tvRole);
        tvEnrollmentOrFacultyId = findViewById(R.id.tvEnrollmentOrFacultyId);
        tvTotalDoubts = findViewById(R.id.tvTotalDoubts);
        tvAnsweredDoubts = findViewById(R.id.tvAnsweredDoubts);
        tvInitial = findViewById(R.id.tvInitial);
        btnLogout = findViewById(R.id.btnLogout);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnDepartment = findViewById(R.id.btnDepartment);
        rowDepartment = findViewById(R.id.rowDepartment);


        btnDepartment.setOnClickListener(v -> showSelectionDialog("Department", DEPARTMENTS,
                (selected) -> {
                    btnDepartment.setText(selected);
                    saveUserField("department", selected, () -> session.setDepartment(selected));
                }));

        tvInitial.setOnClickListener(v ->
                Toast.makeText(this,
                        "Profile photo coming soon!",
                        Toast.LENGTH_SHORT).show());

        btnChangePassword.setOnClickListener(v -> {
            Toast.makeText(this, "Opening Change Password...", Toast.LENGTH_SHORT).show();
            showChangePasswordDialog();
        });

        btnLogout.setOnClickListener(v -> {

            new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Logout", (dialog, which) -> {
                        try {
                            FirebaseAuth.getInstance().signOut();
                            session.clearSession();
                            
                            // Clear all activities and start RoleSelection
                            Intent intent = new Intent(ProfileActivity.this, RoleSelectionActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } catch (Exception e) {
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        loadUserProfile();
        loadStats();
    }

    private void loadUserProfile() {
        String userId = session.getUserId();
        String role = session.getRole();

        FirebaseHelper.getUsersRef().child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = snapshot.child("name")
                                .getValue(String.class);
                        String email = snapshot.child("email")
                                .getValue(String.class);
                        String enrollment = snapshot.child("enrollmentId")
                                .getValue(String.class);
                        String facultyId = snapshot.child("facultyId")
                                .getValue(String.class);
                        String department = snapshot.child("department")
                                .getValue(String.class);

                        if (name != null) {
                            String cleanName = name.replaceAll(
                                    "[^\\p{L}\\p{N}\\s]", "").trim();
                            tvName.setText(cleanName);
                            if (!cleanName.isEmpty()) {
                                tvInitial.setText(String.valueOf(
                                        cleanName.charAt(0)).toUpperCase());
                            }
                        }

                        if (email != null) {
                            tvEmail.setText(email);
                        }
                        if ("faculty".equals(role)) {
                            tvRole.setText("Faculty Member");
                            tvRole.setTextColor(getResources().getColor(
                                    R.color.faculty_primary));
                            tvEnrollmentOrFacultyId.setText("Faculty ID: "
                                    + (facultyId != null ? facultyId : "N/A"));
                        } else {
                            tvRole.setText("Student");
                            tvRole.setTextColor(getResources().getColor(
                                    R.color.student_primary));
                            tvEnrollmentOrFacultyId.setText("Enrollment: "
                                    + (enrollment != null ? enrollment : "N/A"));
                        }

                        if (snapshot.hasChild("department")) {
                            String userDept = snapshot.child("department").getValue(String.class);
                            if (userDept != null && !userDept.isEmpty()) {
                                btnDepartment.setText(userDept);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
    }

    private void loadStats() {
        String userId = session.getUserId();
        String role = session.getRole();

        if ("student".equals(role)) {
            FirebaseHelper.getDoubtsRef()
                    .orderByChild("authorId").equalTo(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            int total = 0, resolved = 0;
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                total++;
                                String status = ds.child("status")
                                        .getValue(String.class);
                                if ("resolved".equals(status)
                                        || "answered".equals(status)) {
                                    resolved++;
                                }
                            }
                            tvTotalDoubts.setText(String.valueOf(total));
                            tvAnsweredDoubts.setText(String.valueOf(resolved));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {}
                    });
        } else {
            FirebaseHelper.getAnswersRef()
                    .orderByChild("facultyId").equalTo(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            long count = snapshot.getChildrenCount();
                            tvTotalDoubts.setText(String.valueOf(count));
                            tvAnsweredDoubts.setText(String.valueOf(count));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {}
                    });
        }
    }

    private void showSelectionDialog(String title, String[] items, SelectionCallback callback) {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Select " + title)
                .setItems(items, (dialog, which) -> callback.onSelected(items[which]))
                .setNegativeButton("Cancel", null)
                .show();
    }

    interface SelectionCallback {
        void onSelected(String selected);
    }

    private void saveUserField(String field, String value, Runnable onSessionUpdate) {
        FirebaseHelper.getUsersRef()
                .child(session.getUserId())
                .child(field)
                .setValue(value)
                .addOnSuccessListener(unused -> {
                    onSessionUpdate.run();
                    Toast.makeText(this, field.substring(0, 1).toUpperCase()
                            + field.substring(1) + " updated!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this,
                        "Failed to update " + field, Toast.LENGTH_SHORT).show());
    }

    private void showChangePasswordDialog() {
        android.view.View dialogView = getLayoutInflater().inflate(
                R.layout.dialog_change_password, null);

        TextInputEditText etCurrentPassword =
                dialogView.findViewById(R.id.etCurrentPassword);
        TextInputEditText etNewPassword =
                dialogView.findViewById(R.id.etNewPassword);
        com.google.android.material.textfield.TextInputEditText etConfirmPassword =
                dialogView.findViewById(R.id.etConfirmPassword);

        androidx.appcompat.app.AlertDialog dialog = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Change Password")
                .setView(dialogView)
                .setPositiveButton("Update Password", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {
                        String currentPass = etCurrentPassword.getText() != null
                                ? etCurrentPassword.getText().toString().trim() : "";
                        String newPass = etNewPassword.getText() != null
                                ? etNewPassword.getText().toString().trim() : "";
                        String confirmPass = etConfirmPassword.getText() != null
                                ? etConfirmPassword.getText().toString().trim() : "";

                        if (currentPass.isEmpty() || newPass.isEmpty()
                                || confirmPass.isEmpty()) {
                            Toast.makeText(this, "Please fill all fields",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (!newPass.equals(confirmPass)) {
                            Toast.makeText(this,
                                    "New passwords do not match!",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (newPass.length() < 6) {
                            Toast.makeText(this,
                                    "Password must be at least 6 characters",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        FirebaseUser user =
                                FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null && user.getEmail() != null) {
                            AuthCredential credential =
                                    EmailAuthProvider.getCredential(
                                            user.getEmail(), currentPass);

                            user.reauthenticate(credential)
                                    .addOnSuccessListener(unused ->
                                            user.updatePassword(newPass)
                                                    .addOnSuccessListener(unused2 -> {
                                                        Toast.makeText(this,
                                                                "Password changed!",
                                                                Toast.LENGTH_SHORT).show();
                                                        dialog.dismiss();
                                                    })
                                                    .addOnFailureListener(e ->
                                                            Toast.makeText(this,
                                                                    "Failed: " + e.getMessage(),
                                                                    Toast.LENGTH_SHORT).show()))
                                    .addOnFailureListener(e ->
                                            Toast.makeText(this,
                                                    "Current password is incorrect!",
                                                    Toast.LENGTH_SHORT).show());
                        }
                    });
        });

        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}