package com.example.classroomdoubt.activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class StudentDashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerDoubts;
    private DoubtAdapter doubtAdapter;
    private SwipeRefreshLayout swipeRefresh;
    private ExtendedFloatingActionButton fabPostDoubt;
    private TextInputEditText etSearch;
    private ChipGroup chipGroupFilter;
    private SessionManager session;
    private List<Doubt> allDoubts = new ArrayList<>();
    private String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        session = new SessionManager(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerDoubts = findViewById(R.id.recyclerDoubts);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        fabPostDoubt = findViewById(R.id.fabPostDoubt);
        etSearch = findViewById(R.id.etSearch);
        chipGroupFilter = findViewById(R.id.chipGroupFilter);

        doubtAdapter = new DoubtAdapter(this, new ArrayList<>(),
                "student", session.getUserId());
        recyclerDoubts.setLayoutManager(new LinearLayoutManager(this));
        recyclerDoubts.setAdapter(doubtAdapter);

        fabPostDoubt.setOnClickListener(v -> {
            Intent intent = new Intent(this, PostDoubtActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        swipeRefresh.setColorSchemeResources(R.color.primary, R.color.accent);
        swipeRefresh.setOnRefreshListener(this::loadDoubts);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) { filterDoubts(); }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chipAll) currentFilter = "all";
            else if (id == R.id.chipPending) currentFilter = "pending";
            else if (id == R.id.chipAnswered) currentFilter = "answered";
            else if (id == R.id.chipMine) currentFilter = "mine";
            filterDoubts();
        });

        recyclerDoubts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                if (dy > 10) fabPostDoubt.shrink();
                else if (dy < -10) fabPostDoubt.extend();
            }
        });

        createNotificationChannel();
        loadDoubts();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_student, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_chat) {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra(ChatActivity.EXTRA_CHAT_ROOM, ChatActivity.ROOM_STUDENTS);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            return true;
        } else if (id == R.id.action_notifications) {
            startActivity(new Intent(this, NotificationActivity.class));
            return true;
        } else if (id == R.id.action_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadDoubts() {
        swipeRefresh.setRefreshing(true);
        FirebaseHelper.getDoubtsRef()
                .orderByChild("createdAt")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        allDoubts.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Doubt doubt = ds.getValue(Doubt.class);
                            if (doubt != null) {
                                doubt.setDoubtId(ds.getKey());
                                allDoubts.add(0, doubt);
                            }
                        }
                        swipeRefresh.setRefreshing(false);
                        filterDoubts();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(StudentDashboardActivity.this, "Failed to load doubts", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void filterDoubts() {
        String query = etSearch.getText() != null ? etSearch.getText().toString().toLowerCase().trim() : "";
        List<Doubt> filtered = new ArrayList<>();
        String myId = session.getUserId();

        for (Doubt d : allDoubts) {
            // Privacy Filtering: ONLY show if public OR if it's mine
            if (d.isPrivate() && !myId.equals(d.getAuthorId())) {
                continue;
            }

            boolean matchesSearch = query.isEmpty()
                    || d.getTitle().toLowerCase().contains(query)
                    || d.getSubject().toLowerCase().contains(query);

            boolean matchesFilter;
            switch (currentFilter) {
                case "pending": matchesFilter = "pending".equals(d.getStatus()); break;
                case "answered": matchesFilter = !"pending".equals(d.getStatus()); break;
                case "mine": matchesFilter = myId.equals(d.getAuthorId()); break;
                default: matchesFilter = true;
            }

            if (matchesSearch && matchesFilter) filtered.add(d);
        }
        doubtAdapter.updateList(filtered);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("doubt_channel", "Doubt Notifications", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }
}
