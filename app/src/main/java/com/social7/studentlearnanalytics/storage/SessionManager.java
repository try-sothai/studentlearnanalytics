package com.social7.studentlearnanalytics.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class SessionManager {

    private static final String PREF_NAME = "SLAS_UserSession_Secure";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_NUMERIC_USER_ID = "numeric_user_id";
    private static final String KEY_STUDENT_DB_ID = "student_db_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_TOKEN = "user_token";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_USER_NAME = "user_name";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        Context appContext = context.getApplicationContext();
        try {
            MasterKey masterKey = new MasterKey.Builder(appContext)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            pref = EncryptedSharedPreferences.create(
                    appContext,
                    PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            editor = pref.edit();
        } catch (GeneralSecurityException | IOException e) {
            Log.e("SLAS_AUTH", "Error initializing EncryptedSharedPreferences", e);
            pref = appContext.getSharedPreferences("SLAS_UserSession_Fallback", Context.MODE_PRIVATE);
            editor = pref.edit();
        }
    }

    public void saveSession(String userId, String token, String role, String name) {
        saveSession(userId, "", "", "", token, role, name);
    }

    public void saveSession(String userId, String numericUserId, String studentDbId, String email, String token, String role, String name) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, userId != null ? userId : "");
        if (numericUserId != null && !numericUserId.isEmpty()) {
            editor.putString(KEY_NUMERIC_USER_ID, numericUserId);
        }
        if (studentDbId != null && !studentDbId.isEmpty()) {
            editor.putString(KEY_STUDENT_DB_ID, studentDbId);
        }
        if (email != null) {
            editor.putString(KEY_USER_EMAIL, email);
        }
        editor.putString(KEY_USER_TOKEN, token != null ? token : "");
        editor.putString(KEY_USER_ROLE, role != null ? role : "");
        editor.putString(KEY_USER_NAME, name != null ? name : "");
        boolean saved = editor.commit();
        Log.d("SLAS_AUTH", "SLAS: Session saved = " + saved + " for " + userId + " (" + role + ")");
    }

    public void updateUserProfile(String name, String email) {
        if (name != null && !name.isEmpty()) {
            editor.putString(KEY_USER_NAME, name);
        }
        if (email != null) {
            editor.putString(KEY_USER_EMAIL, email);
        }
        editor.commit();
    }

    public boolean isLoggedIn() {
        boolean loggedIn = pref.getBoolean(KEY_IS_LOGGED_IN, false);
        Log.d("SLAS_AUTH", "SLAS: Session exists = " + loggedIn);
        return loggedIn;
    }

    public String getUserId() {
        return pref.getString(KEY_USER_ID, "");
    }

    public String getNumericUserId() {
        return pref.getString(KEY_NUMERIC_USER_ID, "");
    }

    public String getStudentDbId() {
        return pref.getString(KEY_STUDENT_DB_ID, "");
    }

    public String getUserEmail() {
        return pref.getString(KEY_USER_EMAIL, "");
    }

    public String getStudentName() {
        return pref.getString(KEY_USER_NAME, "");
    }

    public String getUserName() {
        return pref.getString(KEY_USER_NAME, "");
    }

    public String getUserRole() {
        return pref.getString(KEY_USER_ROLE, "");
    }

    public String getRole() {
        return getUserRole();
    }

    public String getToken() {
        return pref.getString(KEY_USER_TOKEN, "");
    }

    public void clearSession() {
        Log.d("SLAS_AUTH", "SLAS: Logout started - Clearing session data");
        editor.clear();
        boolean cleared = editor.commit();
        Log.d("SLAS_AUTH", "SLAS: Session cleared = " + cleared);
    }
}
