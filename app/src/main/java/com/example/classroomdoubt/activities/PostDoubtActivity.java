package com.example.classroomdoubt.activities;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.classroomdoubt.R;
import com.example.classroomdoubt.models.Doubt;
import com.example.classroomdoubt.utils.FirebaseHelper;
import com.example.classroomdoubt.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class PostDoubtActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etDescription;
    private AutoCompleteTextView actvSubject;
    private RadioGroup rgPriority;
    private SwitchMaterial switchAnonymous, switchPrivate;
    private MaterialButton btnSubmit, btnAddImage;
    private ImageView ivPreview;
    private View layoutImagePreview;
    private ImageButton btnClearImage;
    private ProgressBar progressBar;
    private SessionManager session;
    private Uri selectedImageUri;

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

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            selectedImageUri = uri;
                            ivPreview.setImageURI(uri);
                            layoutImagePreview.setVisibility(View.VISIBLE);
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_doubt);

        session = new SessionManager(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        actvSubject = findViewById(R.id.actvSubject);
        rgPriority = findViewById(R.id.rgPriority);
        switchAnonymous = findViewById(R.id.switchAnonymous);
        switchPrivate = findViewById(R.id.switchPrivate);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnAddImage = findViewById(R.id.btnAddImage);
        ivPreview = findViewById(R.id.ivPreview);
        layoutImagePreview = findViewById(R.id.layoutImagePreview);
        btnClearImage = findViewById(R.id.btnClearImage);
        progressBar = findViewById(R.id.progressBar);

        // Setup Subject Dropdown
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, subjects);
        actvSubject.setAdapter(adapter);

        btnAddImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        btnClearImage.setOnClickListener(v -> {
            selectedImageUri = null;
            layoutImagePreview.setVisibility(View.GONE);
        });

        btnSubmit.setOnClickListener(v -> validateAndSubmit());
    }

    private void validateAndSubmit() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String subject = actvSubject.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || subject.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        if (selectedImageUri != null) {
            uploadImageAndSubmit(title, description, subject);
        } else {
            submitDoubt(title, description, subject, null);
        }
    }

    private void uploadImageAndSubmit(String title, String description, String subject) {
        String fileName = "doubts/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference ref = FirebaseHelper.getStorageRef().child(fileName);

        ref.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl()
                        .addOnSuccessListener(uri -> submitDoubt(title, description, subject, uri.toString())))
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnSubmit.setEnabled(true);
                    Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show();
                });
    }

    private void submitDoubt(String title, String description, String subject, String imageUrl) {
        int priorityId = rgPriority.getCheckedRadioButtonId();
        String priority = "medium";
        if (priorityId == R.id.rbLow) priority = "low";
        else if (priorityId == R.id.rbHigh) priority = "high";

        boolean isAnonymous = switchAnonymous.isChecked();
        boolean isPrivate = switchPrivate.isChecked();

        Doubt doubt = new Doubt();
        doubt.setTitle(title);
        doubt.setDescription(description);
        doubt.setSubject(subject);
        doubt.setPriority(priority);
        doubt.setAnonymous(isAnonymous);
        doubt.setPrivate(isPrivate);
        doubt.setAuthorId(session.getUserId());
        doubt.setAuthorName(isAnonymous ? "Anonymous" : session.getUserName());
        doubt.setCreatedAt(System.currentTimeMillis());
        doubt.setStatus("pending");
        doubt.setUpvotes(0);
        doubt.setAnswerCount(0);
        doubt.setAttachmentUrl(imageUrl);

        String doubtId = FirebaseHelper.getDoubtsRef().push().getKey();
        if (doubtId != null) {
            doubt.setDoubtId(doubtId);
            FirebaseHelper.getDoubtsRef().child(doubtId).setValue(doubt)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Doubt posted successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        btnSubmit.setEnabled(true);
                        Toast.makeText(this, "Failed to post", Toast.LENGTH_SHORT).show();
                    });
        }
    }

}