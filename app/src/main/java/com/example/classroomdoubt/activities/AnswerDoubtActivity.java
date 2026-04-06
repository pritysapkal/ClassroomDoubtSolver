package com.example.classroomdoubt.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.classroomdoubt.R;
import com.example.classroomdoubt.models.Answer;
import com.example.classroomdoubt.models.Notification;
import com.example.classroomdoubt.utils.FirebaseHelper;
import com.example.classroomdoubt.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class AnswerDoubtActivity extends AppCompatActivity {

    public static final String EXTRA_DOUBT_ID = "doubt_id";
    public static final String EXTRA_DOUBT_TITLE = "doubt_title";

    private TextInputLayout tilAnswer;
    private TextInputEditText etAnswer;
    private MaterialButton btnSubmitAnswer;
    private ProgressBar progressBar;
    private TextView tvDoubtTitle;
    private SessionManager session;
    private String doubtId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer_doubt);

        session = new SessionManager(this);

        doubtId = getIntent().getStringExtra(EXTRA_DOUBT_ID);
        String doubtTitle = getIntent().getStringExtra(EXTRA_DOUBT_TITLE);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tilAnswer = findViewById(R.id.tilAnswer);
        etAnswer = findViewById(R.id.etAnswer);
        btnSubmitAnswer = findViewById(R.id.btnSubmitAnswer);
        progressBar = findViewById(R.id.progressBar);
        tvDoubtTitle = findViewById(R.id.tvDoubtTitle);

        tvDoubtTitle.setText(doubtTitle);

        btnSubmitAnswer.setOnClickListener(v -> showConfirmDialog());
    }

    private void showConfirmDialog() {
        String answerText = etAnswer.getText() != null
                ? etAnswer.getText().toString().trim() : "";
        tilAnswer.setError(null);

        if (answerText.length() < 20) {
            tilAnswer.setError("Answer must be at least 20 characters");
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Submit Answer")
                .setMessage("Are you sure you want to submit this answer?")
                .setPositiveButton("Submit", (dialog, which) -> submitAnswer(answerText))
                .setNegativeButton("Review", null)
                .show();
    }

    private void submitAnswer(String answerText) {
        String facultyName = session.getUserName();
        String facultyDept = session.getDepartment();

        setLoading(true);

        // confidence set to 0 - student will rate it
        Answer answer = new Answer(doubtId, session.getUserId(),
                facultyName, facultyDept, answerText);

        DatabaseReference newRef = FirebaseHelper.getAnswersRef().push();
        answer.setAnswerId(newRef.getKey());

        newRef.setValue(answer)
                .addOnSuccessListener(unused -> {
                    FirebaseHelper.getDoubtsRef()
                            .child(doubtId)
                            .child("status")
                            .setValue("answered");
                    createNotificationForStudent(facultyName);
                    setLoading(false);
                    Toast.makeText(this,
                            "Answer submitted successfully!",
                            Toast.LENGTH_SHORT).show();
                    finish();
                    overridePendingTransition(0, R.anim.slide_out_left);
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this,
                            "Failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void createNotificationForStudent(String facultyName) {
        FirebaseHelper.getDoubtsRef().child(doubtId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        String studentId = snapshot.child("authorId")
                                .getValue(String.class);
                        String title = snapshot.child("title")
                                .getValue(String.class);
                        if (studentId != null) {
                            Notification notif = new Notification(
                                    studentId,
                                    "Your doubt was answered!",
                                    "Prof. " + facultyName
                                            + " answered: " + title,
                                    doubtId);
                            DatabaseReference notifRef =
                                    FirebaseHelper.getNotificationsRef()
                                            .child(studentId).push();
                            notif.setNotifId(notifRef.getKey());
                            notifRef.setValue(notif);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSubmitAnswer.setEnabled(!loading);
    }
}