package com.example.classroomdoubt.activities;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.classroomdoubt.R;
import com.example.classroomdoubt.adapters.NotificationAdapter;
import com.example.classroomdoubt.models.Notification;
import com.example.classroomdoubt.utils.FirebaseHelper;
import com.example.classroomdoubt.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView recyclerNotifications;
    private NotificationAdapter notificationAdapter;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        session = new SessionManager(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerNotifications = findViewById(R.id.recyclerNotifications);
        notificationAdapter = new NotificationAdapter(this, new ArrayList<>());
        recyclerNotifications.setLayoutManager(new LinearLayoutManager(this));
        recyclerNotifications.setAdapter(notificationAdapter);

        loadNotifications();
    }

    private void loadNotifications() {
        String userId = session.getUserId();
        FirebaseHelper.getNotificationsRef().child(userId)
                .orderByChild("createdAt")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Notification> notifications = new ArrayList<>();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Notification notif = ds.getValue(Notification.class);
                            if (notif != null) {
                                notif.setNotifId(ds.getKey());
                                notifications.add(0, notif); // newest first
                                // Mark as read
                                ds.getRef().child("isRead").setValue(true);
                            }
                        }
                        notificationAdapter.updateList(notifications);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}