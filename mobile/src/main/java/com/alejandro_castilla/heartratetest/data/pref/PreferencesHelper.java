package com.alejandro_castilla.heartratetest.data.pref;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yasar on 7/3/18.
 */

public class PreferencesHelper implements Pref {

    private static PreferencesHelper preferences;

    private SharedPreferences sharedPreferences;

    private static final String KEY_EMAIL = "email";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_STATUS = "status";
    private static final String KEY_IMAGE = "userimage";
    private static final String KEY_URL = "serverurl";
    private static final String KEY_STUDENT_ID = "studentID";
    private static final String KEY_USERTYPE = "usertype";
    private static final String KEY_UPDATE_TIME = "updateTime";


    public String getKeyEmail() {
        return sharedPreferences.getString(KEY_EMAIL, null);
    }

    public String getKeyUserName() {
        return sharedPreferences.getString(KEY_USERNAME, null);
    }

    public String getKeyPassword() {
        return sharedPreferences.getString(KEY_PASSWORD, null);
    }

    public String getKeyStatus() {
        return sharedPreferences.getString(KEY_STATUS, null);
    }

    public String getKeyImage() {
        return sharedPreferences.getString(KEY_IMAGE, null);
    }

    public String getKeyUrl() {
        return sharedPreferences.getString(KEY_URL, null);
    }

    public String getKeyStudentId() {
        return sharedPreferences.getString(KEY_STUDENT_ID, null);
    }

    public String getKeyUserType() {
        return sharedPreferences.getString(KEY_USERTYPE, null);
    }

    @Override
    public String getUpdateTime() {
        return sharedPreferences.getString(KEY_UPDATE_TIME, null);
    }

    @Override
    public void saveUpdateTime(String s) {

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_UPDATE_TIME, s);
        editor.apply();
    }


    private PreferencesHelper(Context context) {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

    }

    public static PreferencesHelper getPreferencesInstance(Context context) {

        if (preferences == null) {
            preferences = new PreferencesHelper(context);
        }

        return preferences;
    }

    @Override
    public void saveSession(String email, String password, String username, String studentID, String usertype, boolean status, String userimage) {

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PASSWORD, password);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_STUDENT_ID, studentID);
        editor.putString(KEY_USERTYPE, usertype);
        editor.putBoolean(KEY_STATUS, status);
        editor.putString(KEY_IMAGE, userimage);
        editor.apply();

    }

    @Override
    public String getUserType() {
        return getKeyUserType();
    }

    @Override
    public Map<Object, Object> getUserDetails() {
        return getSession();
    }

    @Override
    public String getEmailOrUsername() {
        return getKeyEmail();
    }

    @Override
    public String getPassword() {
        return getKeyPassword();
    }

    @Override
    public String getProfileName() {
        return getKeyUserName();
    }

    @Override
    public String getProfileImage() {
        return getKeyImage();
    }

    public Map<Object, Object> getSession() {

        Map<Object, Object> hashMap = new HashMap<>();

        hashMap.put(KEY_EMAIL, sharedPreferences.getString(KEY_EMAIL, null));
        hashMap.put(KEY_PASSWORD, sharedPreferences.getString(KEY_PASSWORD, null));
        hashMap.put(KEY_STATUS, sharedPreferences.getBoolean(KEY_STATUS, false));
        hashMap.put(KEY_USERNAME, sharedPreferences.getString(KEY_USERNAME, ""));
        hashMap.put(KEY_IMAGE, sharedPreferences.getString(KEY_IMAGE, ""));
        hashMap.put(KEY_STUDENT_ID, sharedPreferences.getString(KEY_STUDENT_ID, ""));
        hashMap.put(KEY_USERTYPE, sharedPreferences.getString(KEY_USERTYPE, ""));

        return hashMap;
    }

    @Override
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_STATUS, false);
    }

    @Override
    public void clear() {

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

    }
}
