package com.example.classroomdoubt.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.classroomdoubt.R;
import com.example.classroomdoubt.adapters.ChatAdapter;
import com.example.classroomdoubt.models.ChatMessage;
import com.example.classroomdoubt.utils.FirebaseHelper;
import com.example.classroomdoubt.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_CHAT_ROOM = "chat_room";
    public static final String ROOM_STUDENTS = "student_chat";
    public static final String ROOM_FACULTY = "faculty_chat";

    // Request codes
    private static final int REQUEST_DOCUMENT = 101;
    private static final int REQUEST_GALLERY = 102;
    private static final int REQUEST_CAMERA = 103;
    private static final int REQUEST_AUDIO = 104;
    private static final int REQUEST_PERMISSION = 200;

    private RecyclerView recyclerMessages;
    private ChatAdapter chatAdapter;
    private TextInputEditText etMessage;
    private FloatingActionButton btnSend;
    private MaterialButton btnAttach;
    private LinearLayout attachmentPanel;
    private LinearLayout btnAttachDocument, btnAttachGallery,
            btnAttachCamera, btnAttachAudio, btnAttachPoll;
    private TextView tvRoomName, tvMemberCount;
    private SessionManager session;
    private String chatRoom;
    private List<ChatMessage> messageList = new ArrayList<>();
    private boolean isPanelVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        session = new SessionManager(this);
        chatRoom = getIntent().getStringExtra(EXTRA_CHAT_ROOM);
        if (chatRoom == null) chatRoom = ROOM_STUDENTS;

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Init views
        tvRoomName = findViewById(R.id.tvRoomName);
        tvMemberCount = findViewById(R.id.tvMemberCount);
        recyclerMessages = findViewById(R.id.recyclerMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnAttach = findViewById(R.id.btnAttach);
        attachmentPanel = findViewById(R.id.attachmentPanel);
        btnAttachDocument = findViewById(R.id.btnAttachDocument);
        btnAttachGallery = findViewById(R.id.btnAttachGallery);
        btnAttachCamera = findViewById(R.id.btnAttachCamera);
        btnAttachAudio = findViewById(R.id.btnAttachAudio);
        btnAttachPoll = findViewById(R.id.btnAttachPoll);

        // Set room theme
        if (ROOM_FACULTY.equals(chatRoom)) {
            tvRoomName.setText("Faculty Chat Room");
            tvRoomName.setTextColor(
                    getResources().getColor(R.color.faculty_primary));
            toolbar.setBackgroundColor(
                    getResources().getColor(R.color.faculty_primary));
            btnSend.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            getResources().getColor(R.color.faculty_primary)));
        } else {
            tvRoomName.setText("Student Chat Room");
            tvRoomName.setTextColor(
                    getResources().getColor(R.color.student_primary));
        }

        // Setup RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerMessages.setLayoutManager(layoutManager);
        chatAdapter = new ChatAdapter(this, messageList, session.getUserId());
        recyclerMessages.setAdapter(chatAdapter);

        // + button click → show/hide attachment panel
        btnAttach.setOnClickListener(v -> toggleAttachmentPanel());

        // Attachment option clicks
        btnAttachDocument.setOnClickListener(v -> {
            hideAttachmentPanel();
            openDocument();
        });

        btnAttachGallery.setOnClickListener(v -> {
            hideAttachmentPanel();
            openGallery();
        });

        btnAttachCamera.setOnClickListener(v -> {
            hideAttachmentPanel();
            openCamera();
        });

        btnAttachAudio.setOnClickListener(v -> {
            hideAttachmentPanel();
            openAudio();
        });

        btnAttachPoll.setOnClickListener(v -> {
            hideAttachmentPanel();
            showPollDialog();
        });

        // Send button
        btnSend.setOnClickListener(v -> sendMessage());

        // Member count click → show member list
        tvMemberCount.setOnClickListener(v -> showMembersDialog());

        loadMemberCount();
        loadMessages();
        registerInChatRoom();
    }

    // Toggle attachment panel
    private void toggleAttachmentPanel() {
        if (isPanelVisible) {
            hideAttachmentPanel();
        } else {
            showAttachmentPanel();
        }
    }

    private void showAttachmentPanel() {
        attachmentPanel.setVisibility(View.VISIBLE);
        btnAttach.setText("✕");
        isPanelVisible = true;
    }

    private void hideAttachmentPanel() {
        attachmentPanel.setVisibility(View.GONE);
        btnAttach.setText("+");
        isPanelVisible = false;
    }

    // Open Document picker
    private void openDocument() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimeTypes = {
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-excel",
                "application/vnd.ms-powerpoint",
                "text/plain"
        };
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.putExtra(Intent.EXTRA_TITLE, "Select Document");
        startActivityForResult(
                Intent.createChooser(intent, "Select Document"),
                REQUEST_DOCUMENT);
    }

    // Open Gallery
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(
                Intent.createChooser(intent, "Select Image"),
                REQUEST_GALLERY);
    }

    // Open Camera
    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_PERMISSION);
        } else {
            Intent intent = new Intent(
                    MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_CAMERA);
            }
        }
    }

    // Open Audio picker
    private void openAudio() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(
                Intent.createChooser(intent, "Select Audio"),
                REQUEST_AUDIO);
    }

    // Show Poll Dialog
    private void showPollDialog() {
        android.view.View dialogView = getLayoutInflater()
                .inflate(android.R.layout.activity_list_item, null);

        final android.widget.EditText etQuestion =
                new android.widget.EditText(this);
        etQuestion.setHint("Enter poll question...");

        final android.widget.EditText etOption1 =
                new android.widget.EditText(this);
        etOption1.setHint("Option 1");

        final android.widget.EditText etOption2 =
                new android.widget.EditText(this);
        etOption2.setHint("Option 2");

        android.widget.LinearLayout layout =
                new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 0);
        layout.addView(etQuestion);
        layout.addView(etOption1);
        layout.addView(etOption2);

        new AlertDialog.Builder(this)
                .setTitle("Create Poll")
                .setView(layout)
                .setPositiveButton("Send Poll", (dialog, which) -> {
                    String question = etQuestion.getText()
                            .toString().trim();
                    String opt1 = etOption1.getText()
                            .toString().trim();
                    String opt2 = etOption2.getText()
                            .toString().trim();

                    if (question.isEmpty() || opt1.isEmpty()
                            || opt2.isEmpty()) {
                        Toast.makeText(this,
                                "Please fill all fields",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Send poll as a formatted message
                    String pollMessage = "📊 POLL\n"
                            + "❓ " + question + "\n"
                            + "1️⃣ " + opt1 + "\n"
                            + "2️⃣ " + opt2;
                    sendSpecialMessage(pollMessage);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Handle results from file pickers
    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK || data == null) return;

        Uri uri = data.getData();
        if (uri == null) return;

        switch (requestCode) {
            case REQUEST_DOCUMENT:
                String docName = getFileName(uri);
                sendSpecialMessage("📄 Document: " + docName);
                break;

            case REQUEST_GALLERY:
                sendSpecialMessage("🖼️ Image shared");
                break;

            case REQUEST_CAMERA:
                sendSpecialMessage("📷 Photo taken and shared");
                break;

            case REQUEST_AUDIO:
                String audioName = getFileName(uri);
                sendSpecialMessage("🎵 Audio: " + audioName);
                break;
        }
    }

    // Get file name from URI
    private String getFileName(Uri uri) {
        String result = "file";
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(
                    uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(
                            OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) {
                        result = cursor.getString(index);
                    }
                }
            }
        }
        if (result.equals("file")) {
            result = uri.getPath();
            int cut = result != null ? result.lastIndexOf('/') : -1;
            if (cut != -1) result = result.substring(cut + 1);
        }
        return result;
    }

    // Handle permission results
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            Toast.makeText(this,
                    "Camera permission denied",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void registerInChatRoom() {
        FirebaseHelper.getDatabaseRef()
                .child("chat_members")
                .child(chatRoom)
                .child(session.getUserId())
                .setValue(session.getUserName());
    }

    private void loadMemberCount() {
        FirebaseHelper.getDatabaseRef()
                .child("chat_members")
                .child(chatRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {
                        long count = snapshot.getChildrenCount();
                        tvMemberCount.setText("👥 " + count + " members");
                    }
                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError databaseError) {}
                });
    }

    private void showMembersDialog() {
        FirebaseHelper.getDatabaseRef()
                .child("chat_members")
                .child(chatRoom)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> names = new ArrayList<>();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String name = ds.getValue(String.class);
                            if (name != null) names.add(name);
                        }

                        if (names.isEmpty()) {
                            Toast.makeText(ChatActivity.this, "No members found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String[] nameArray = names.toArray(new String[0]);
                        new AlertDialog.Builder(ChatActivity.this)
                                .setTitle("Room Members (" + names.size() + ")")
                                .setItems(nameArray, null)
                                .setPositiveButton("Close", null)
                                .show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ChatActivity.this, "Failed to load members", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadMessages() {
        FirebaseHelper.getDatabaseRef()
                .child("chats")
                .child(chatRoom)
                .orderByChild("timestamp")
                .limitToLast(100)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {
                        messageList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ChatMessage message =
                                    ds.getValue(ChatMessage.class);
                            if (message != null) {
                                message.setMessageId(ds.getKey());
                                messageList.add(message);
                            }
                        }
                        chatAdapter.updateList(messageList);
                        if (!messageList.isEmpty()) {
                            recyclerMessages.smoothScrollToPosition(
                                    messageList.size() - 1);
                        }
                    }
                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError databaseError) {}
                });
    }

    private void sendMessage() {
        String messageText = etMessage.getText() != null
                ? etMessage.getText().toString().trim() : "";

        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(this,
                    "Please type a message",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        ChatMessage message = new ChatMessage(
                session.getUserId(),
                session.getUserName(),
                session.getRole(),
                messageText);

        DatabaseReference newRef = FirebaseHelper.getDatabaseRef()
                .child("chats")
                .child(chatRoom)
                .push();
        message.setMessageId(newRef.getKey());

        newRef.setValue(message)
                .addOnSuccessListener(unused ->
                        etMessage.setText(""))
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    // Send special messages (poll, file, photo etc)
    private void sendSpecialMessage(String text) {
        ChatMessage message = new ChatMessage(
                session.getUserId(),
                session.getUserName(),
                session.getRole(),
                text);

        DatabaseReference newRef = FirebaseHelper.getDatabaseRef()
                .child("chats")
                .child(chatRoom)
                .push();
        message.setMessageId(newRef.getKey());
        newRef.setValue(message);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseHelper.getDatabaseRef()
                .child("chat_members")
                .child(chatRoom)
                .child(session.getUserId())
                .removeValue();
    }
}