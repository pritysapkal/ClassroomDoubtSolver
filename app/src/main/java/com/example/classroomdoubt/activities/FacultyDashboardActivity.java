package com.example.classroomdoubt.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.classroomdoubt.R;
import com.example.classroomdoubt.adapters.DoubtAdapter;
import com.example.classroomdoubt.models.Doubt;
import com.example.classroomdoubt.utils.FirebaseHelper;
import com.example.classroomdoubt.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FacultyDashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerDoubts;
    private DoubtAdapter doubtAdapter;
    private SwipeRefreshLayout swipeRefresh;
    private TextView tvPendingCount, tvAnsweredCount, tvTotalCount;
    private ChipGroup chipGroupFilter;
    private SessionManager session;
    private List<Doubt> allDoubts = new ArrayList<>();
    private String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty_dashboard);

        session = new SessionManager(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setSubtitle("Subject: " + session.getSubject());

        recyclerDoubts = findViewById(R.id.recyclerDoubts);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        tvPendingCount = findViewById(R.id.tvPendingCount);
        tvAnsweredCount = findViewById(R.id.tvAnsweredCount);
        tvTotalCount = findViewById(R.id.tvTotalCount);
        chipGroupFilter = findViewById(R.id.chipGroupFilter);

        doubtAdapter = new DoubtAdapter(this, new ArrayList<>(),
                "faculty", session.getUserId());
        recyclerDoubts.setLayoutManager(new LinearLayoutManager(this));
        recyclerDoubts.setAdapter(doubtAdapter);

        swipeRefresh.setColorSchemeResources(R.color.faculty_primary);
        swipeRefresh.setOnRefreshListener(this::loadDoubts);

        chipGroupFilter.setOnCheckedStateChangeListener(
                (group, checkedIds) -> {
                    if (checkedIds.isEmpty()) return;
                    int id = checkedIds.get(0);
                    if (id == R.id.chipAll) currentFilter = "all";
                    else if (id == R.id.chipPending) currentFilter = "pending";
                    else if (id == R.id.chipMyAnswers) currentFilter = "mine";
                    filterDoubts();
                });

        loadDoubts();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_faculty, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_chat) {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra(ChatActivity.EXTRA_CHAT_ROOM,
                    ChatActivity.ROOM_FACULTY);
            startActivity(intent);
            overridePendingTransition(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left);
            return true;
        } else if (id == R.id.action_notifications) {
            startActivity(new Intent(this,
                    NotificationActivity.class));
            return true;
        } else if (id == R.id.action_profile) {
            startActivity(new Intent(this,
                    ProfileActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadDoubts() {
        swipeRefresh.setRefreshing(true);
        String facultySubject = session.getSubject();
        
        FirebaseHelper.getDoubtsRef()
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {
                        allDoubts.clear();
                        int pending = 0, answered = 0;
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Doubt doubt = ds.getValue(Doubt.class);
                            if (doubt != null) {
                                // Filter by faculty's subject
                                if (facultySubject.equals(doubt.getSubject())) {
                                    doubt.setDoubtId(ds.getKey());
                                    allDoubts.add(0, doubt);
                                    if ("pending".equals(doubt.getStatus())) {
                                        pending++;
                                    } else {
                                        answered++;
                                    }
                                }
                            }
                        }
                        swipeRefresh.setRefreshing(false);
                        tvPendingCount.setText(String.valueOf(pending));
                        tvAnsweredCount.setText(String.valueOf(answered));
                        tvTotalCount.setText(
                                String.valueOf(allDoubts.size()));
                        filterDoubts();
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError databaseError) {
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(FacultyDashboardActivity.this,
                                "Failed to load",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void filterDoubts() {
        List<Doubt> filtered = new ArrayList<>();
        for (Doubt d : allDoubts) {
            boolean match;
            switch (currentFilter) {
                case "pending":
                    match = "pending".equals(d.getStatus());
                    break;
                case "mine":
                    match = d.getAnswerCount() > 0;
                    break;
                default:
                    match = true;
            }
            if (match) filtered.add(d);
        }
        doubtAdapter.updateList(filtered);
    }
}