package com.example.classroomdoubt.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseHelper {
    // Firebase Realtime Database node paths
    public static final String NODE_USERS = "users";
    public static final String NODE_DOUBTS = "doubts";
    public static final String NODE_ANSWERS = "answers";
    public static final String NODE_NOTIFICATIONS = "notifications";
    public static final String NODE_UPVOTES = "upvotes";

    private static DatabaseReference dbRef;
    private static FirebaseAuth auth;

    public static DatabaseReference getDatabaseRef() {
        if (dbRef == null) {
            dbRef = FirebaseDatabase.getInstance().getReference();
        }
        return dbRef;
    }

    public static FirebaseAuth getAuth() {
        if (auth == null) {
            auth = FirebaseAuth.getInstance();
        }
        return auth;
    }

    public static DatabaseReference getUsersRef() {
        return getDatabaseRef().child(NODE_USERS);
    }

    public static DatabaseReference getDoubtsRef() {
        return getDatabaseRef().child(NODE_DOUBTS);
    }

    public static DatabaseReference getAnswersRef() {
        return getDatabaseRef().child(NODE_ANSWERS);
    }

    public static DatabaseReference getNotificationsRef() {
        return getDatabaseRef().child(NODE_NOTIFICATIONS);
    }

    public static String getCurrentUserId() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            return auth.getCurrentUser().getUid();
        }
        return null;
    }

    public static StorageReference getStorageRef() {
        return FirebaseStorage.getInstance().getReference();
    }
}