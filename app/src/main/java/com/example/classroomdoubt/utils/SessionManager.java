package com.example.classroomdoubt.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "DoubtSolveSession";
    private static final String KEY_ROLE = "role";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_DEPT = "department";
    private static final String KEY_SUBJECT = "subject";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveSession(String userId, String name, String role, String subject) {
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_ROLE, role);
        editor.putString(KEY_SUBJECT, subject);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, "");
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, "");
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }

    public String getDepartment() {
        return prefs.getString(KEY_DEPT, "");
    }

    public void setDepartment(String dept) {
        editor.putString(KEY_DEPT, dept);
        editor.apply();
    }

    public String getSubject() {
        return prefs.getString(KEY_SUBJECT, "General");
    }

    public void setSubject(String subject) {
        editor.putString(KEY_SUBJECT, subject);
        editor.apply();
    }


    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}