package com.alejandro_castilla.heartratetest.data.pref;

import java.util.Map;

/**
 * Created by yasar on 22/3/18.
 */

public interface Pref {


    boolean isLoggedIn();

    void saveSession(String email, String password, String username, String studentID, String usertype, boolean status, String userimage);

    String getUserType();

    Map getUserDetails();

    String getEmailOrUsername();

    String getPassword();

    String getKeyStudentId();

    String getProfileName();

    String getProfileImage();

    String getUpdateTime();

    void saveUpdateTime(String s);

    void clear();


}
