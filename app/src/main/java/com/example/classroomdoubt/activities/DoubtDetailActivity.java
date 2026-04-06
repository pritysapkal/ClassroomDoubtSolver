package com.example.classroomdoubt.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.classroomdoubt.R;
import com.example.classroomdoubt.adapters.AnswerAdapter;
import com.example.classroomdoubt.models.Answer;
import com.example.classroomdoubt.models.Doubt;
import com.example.classroomdoubt.utils.FirebaseHelper;
import com.example.classroomdoubt.utils.SessionManager;
import com.example.classroomdoubt.utils.TimeUtil;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DoubtDetailActivity extends AppCompatActivity {

    public static final String EXTRA_DOUBT_ID = "doubt_id";
    public static final String EXTRA_DOUBT_TITLE = "doubt_title";

    private TextView tvTitle, tvDescription, tvStatus, tvPriority, tvAuthor, tvTime, tvIsPrivate;
    private ImageView ivAttachment;
    private MaterialButton btnMarkResolved, btnAnswerDoubt, btnUpvote;
    private RecyclerView recyclerAnswers;
    private AnswerAdapter answerAdapter;
    private SessionManager session;
    private String doubtId;
    private ValueEventListener doubtListener, upvoteListener;
    private int currentUpvotes = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doubt_detail);

        session = new SessionManager(this);
        doubtId = getIntent().getStringExtra(EXTRA_DOUBT_ID);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvTitle = findViewById(R.id.tvTitle);
        tvDescription = findViewById(R.id.tvDescription);
        tvStatus = findViewById(R.id.tvStatus);
        tvPriority = findViewById(R.id.tvPriority);
        tvAuthor = findViewById(R.id.tvAuthor);
        tvTime = findViewById(R.id.tvTime);
        tvIsPrivate = findViewById(R.id.tvIsPrivate);
        ivAttachment = findViewById(R.id.ivAttachment);
        btnMarkResolved = findViewById(R.id.btnMarkResolved);
        btnAnswerDoubt = findViewById(R.id.btnAnswerDoubt);
        btnUpvote = findViewById(R.id.btnUpvote);
        recyclerAnswers = findViewById(R.id.recyclerAnswers);

        answerAdapter = new AnswerAdapter(this, new ArrayList<>(), session.getRole(), session.getUserId());
        recyclerAnswers.setLayoutManager(new LinearLayoutManager(this));
        recyclerAnswers.setAdapter(answerAdapter);

        if ("faculty".equals(session.getRole())) {
            btnAnswerDoubt.setVisibility(View.VISIBLE);
        }

        btnAnswerDoubt.setOnClickListener(v -> {
            Intent intent = new Intent(this, AnswerDoubtActivity.class);
            intent.putExtra(AnswerDoubtActivity.EXTRA_DOUBT_ID, doubtId);
            intent.putExtra(AnswerDoubtActivity.EXTRA_DOUBT_TITLE, tvTitle.getText().toString());
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        btnMarkResolved.setOnClickListener(v -> MarkAsResolved());
        btnUpvote.setOnClickListener(v -> toggleUpvote());

        loadDoubtDetail();
        loadAnswers();
        listenToUpvoteStatus();
    }

    private void toggleUpvote() {
        String path = doubtId + "/" + session.getUserId();
        FirebaseHelper.getDatabaseRef().child(FirebaseHelper.NODE_UPVOTES).child(path).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        FirebaseHelper.getDatabaseRef().child(FirebaseHelper.NODE_UPVOTES).child(path).removeValue();
                        FirebaseHelper.getDoubtsRef().child(doubtId).child("upvotes").setValue(Math.max(0, currentUpvotes - 1));
                    } else {
                        FirebaseHelper.getDatabaseRef().child(FirebaseHelper.NODE_UPVOTES).child(path).setValue(true);
                        FirebaseHelper.getDoubtsRef().child(doubtId).child("upvotes").setValue(currentUpvotes + 1);
                    }
                });
    }

    private void listenToUpvoteStatus() {
        upvoteListener = FirebaseHelper.getDatabaseRef().child(FirebaseHelper.NODE_UPVOTES)
                .child(doubtId).child(session.getUserId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            btnUpvote.setTextColor(getResources().getColor(R.color.faculty_primary));
                            btnUpvote.setText("👍 " + currentUpvotes + " (Upvoted)");
                        } else {
                            btnUpvote.setTextColor(getResources().getColor(R.color.primary));
                            btnUpvote.setText("👍 " + currentUpvotes);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void loadDoubtDetail() {
        doubtListener = FirebaseHelper.getDoubtsRef().child(doubtId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Doubt doubt = snapshot.getValue(Doubt.class);
                        if (doubt == null) return;

                        currentUpvotes = doubt.getUpvotes();
                        tvTitle.setText(doubt.getTitle());
                        tvDescription.setText(doubt.getDescription());
                        tvPriority.setText(capitalize(doubt.getPriority()));
                        tvAuthor.setText("👤 " + (doubt.isAnonymous() ? "Anonymous" : doubt.getAuthorName()));
                        tvTime.setText(TimeUtil.getTimeAgo(doubt.getCreatedAt()));

                        // Privacy Indicator
                        tvIsPrivate.setVisibility(doubt.isPrivate() ? View.VISIBLE : View.GONE);

                        // Attachment Loading
                        if (doubt.getAttachmentUrl() != null && !doubt.getAttachmentUrl().isEmpty()) {
                            ivAttachment.setVisibility(View.VISIBLE);
                            Glide.with(DoubtDetailActivity.this).load(doubt.getAttachmentUrl()).placeholder(R.color.divider).into(ivAttachment);
                        } else {
                            ivAttachment.setVisibility(View.GONE);
                        }

                        updateStatusUI(doubt.getStatus(), doubt.getAuthorId());
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
    }

    private void updateStatusUI(String status, String authorId) {
        if ("resolved".equals(status)) {
            tvStatus.setText("RESOLVED");
            tvStatus.setBackgroundResource(R.drawable.badge_green);
            tvStatus.setTextColor(getResources().getColor(R.color.faculty_primary));
            btnMarkResolved.setVisibility(View.GONE);
        } else if ("answered".equals(status)) {
            tvStatus.setText("ANSWERED");
            tvStatus.setBackgroundResource(R.drawable.badge_green);
            tvStatus.setTextColor(getResources().getColor(R.color.faculty_primary));
            btnMarkResolved.setVisibility(session.getUserId().equals(authorId) ? View.VISIBLE : View.GONE);
        } else {
            tvStatus.setText("PENDING");
            tvStatus.setBackgroundResource(R.drawable.badge_orange);
            tvStatus.setTextColor(getResources().getColor(R.color.status_open));
            btnMarkResolved.setVisibility(session.getUserId().equals(authorId) ? View.VISIBLE : View.GONE);
        }
    }

    private void MarkAsResolved() {
        new AlertDialog.Builder(this).setTitle("Mark Resolved?").setMessage("Mark this doubt as fixed?").setPositiveButton("Yes", (d, w) -> {
            FirebaseHelper.getDoubtsRef().child(doubtId).child("status").setValue("resolved");
        }).setNegativeButton("Cancel", null).show();
    }

    private void loadAnswers() {
        FirebaseHelper.getAnswersRef().orderByChild("doubtId").equalTo(doubtId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Answer> answers = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Answer a = ds.getValue(Answer.class);
                    if (a != null) { a.setAnswerId(ds.getKey()); answers.add(a); }
                }
                answerAdapter.updateList(answers);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { getMenuInflater().inflate(R.menu.menu_doubt_detail, menu); return true; }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_delete) {
            new AlertDialog.Builder(this).setTitle("Delete Doubt").setMessage("Are you sure?").setPositiveButton("Delete", (d, w) -> {
                FirebaseHelper.getDoubtsRef().child(doubtId).removeValue().addOnSuccessListener(u -> finish());
            }).setNegativeButton("Cancel", null).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (doubtListener != null) FirebaseHelper.getDoubtsRef().child(doubtId).removeEventListener(doubtListener);
        if (upvoteListener != null) FirebaseHelper.getDatabaseRef().child(FirebaseHelper.NODE_UPVOTES).child(doubtId).child(session.getUserId()).removeEventListener(upvoteListener);
    }

    private String capitalize(String s) { if (s == null || s.isEmpty()) return s; return Character.toUpperCase(s.charAt(0)) + s.substring(1); }
}