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
            // Fallback to standard SharedPreferences if encryption fails (e.g., Keystore issue on some devices)
            pref = appContext.getSharedPreferences("SLAS_UserSession_Fallback", Context.MODE_PRIVATE);
            editor = pref.edit();
        }
    }

    public void saveSession(String userId, String token, String role, String name) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_TOKEN, token);
        editor.putString(KEY_USER_ROLE, role);
        editor.putString(KEY_USER_NAME, name);
        boolean saved = editor.commit(); // Synchronous commit to disk
        Log.d("SLAS_AUTH", "SLAS: Session saved = " + saved + " for " + userId + " (" + role + ")");
    }

    public boolean isLoggedIn() {
        boolean loggedIn = pref.getBoolean(KEY_IS_LOGGED_IN, false);
        Log.d("SLAS_AUTH", "SLAS: Session exists = " + loggedIn);
        return loggedIn;
    }

    public String getUserId() {
        return pref.getString(KEY_USER_ID, "");
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

    public String getToken() {
        return pref.getString(KEY_USER_TOKEN, "");
    }

    public void clearSession() {
        Log.d("SLAS_AUTH", "SLAS: Logout started - Clearing session data");
        editor.clear();
        boolean cleared = editor.commit(); // Synchronous commit to disk
        Log.d("SLAS_AUTH", "SLAS: Session cleared = " + cleared);
    }
}
