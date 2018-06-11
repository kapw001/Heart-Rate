package com.alejandro_castilla.heartratetest.data;

import android.content.Context;

import com.alejandro_castilla.heartratetest.data.helper.NetworkHelper;
import com.alejandro_castilla.heartratetest.data.listener.DataListener;
import com.alejandro_castilla.heartratetest.data.pref.Pref;
import com.alejandro_castilla.heartratetest.data.remote.RemoteDataSource;

import java.util.Map;

/**
 * Created by yasar on 6/3/18.
 */

public class DataRepository implements DataSource {

    private static final String TAG = "DataRepository";
    private Context context;
    private RemoteDataSource remoteDataSource;
    private Pref preferences;

    private TypeRequest typeRequest;

    private enum TypeRequest {

        LOGIN, PROFILE, ALL_PROFILE, OTHERS;

    }


    public DataRepository(Context context, RemoteDataSource remoteDataSource, Pref preferences) {
        this.context = context;
        this.remoteDataSource = remoteDataSource;
        this.preferences = preferences;
    }


    public void login(String json, final DataListener dataListener) {

        if (NetworkHelper.isNetworkAvailable(context)) {

            typeRequest = TypeRequest.LOGIN;
            remoteDataSource.login(json, setDataListener(dataListener, typeRequest));

        } else {

            dataListener.onNetworkFailure();

        }

    }

    public void updateHeartRate(String json, final DataListener dataListener) {

        if (NetworkHelper.isNetworkAvailable(context)) {

            typeRequest = TypeRequest.OTHERS;
            remoteDataSource.updateHeartRate(json, setDataListener(dataListener, typeRequest));

        } else {

            dataListener.onNetworkFailure();

        }

    }

    @Override
    public void updateStudentLocation(String json, DataListener dataListener) {

//        if (NetworkHelper.isNetworkAvailable(context)) {

//            typeRequest = TypeRequest.OTHERS;
            remoteDataSource.updateStudentLocation(json, setDataListener(dataListener, typeRequest));

//        } else {
//
//            dataListener.onNetworkFailure();
//
//        }
    }


    private DataListener setDataListener(final DataListener dataListener, final TypeRequest typeRequest) {

        return new DataListener() {
            @Override
            public void onSuccess(Object object) {


                parserData(object, dataListener, typeRequest);


            }

            @Override
            public void onFail(Throwable s) {
                dataListener.onFail(s);
            }

            @Override
            public void onNetworkFailure() {
                dataListener.onNetworkFailure();
            }
        };
    }


    private <T> void parserData(T o, DataListener dataListener, TypeRequest typeRequest) {

//        switch (typeRequest) {
//
//            case LOGIN:
//
//                Parser.loginParse(o.toString(), dataListener, this);
//
//                break;
//
//            case PROFILE:
//
//                Parser.profileParse(o.toString(), dataListener, this);
//
//                break;
//
//            case ALL_PROFILE:
//
//                Parser.allProfileParse(o.toString(), dataListener, this);
//
//                break;
//
//            case OTHERS:

        dataListener.onSuccess(o);

//                break;
//        }


    }


    @Override
    public boolean isLoggedIn() {

        return preferences.isLoggedIn();

    }

    @Override
    public void saveSession(String email, String password, String username, String userid, String usertype, boolean status, String userimage) {

        preferences.saveSession(email, password, username, userid, usertype, status, userimage);

    }

    @Override
    public String getUserType() {
        return preferences.getUserType();
    }

    @Override
    public Map getUserDetails() {
        return preferences.getUserDetails();
    }

    @Override
    public String getEmailOrUsername() {
        return preferences.getEmailOrUsername();
    }

    @Override
    public String getPassword() {
        return preferences.getPassword();
    }

    @Override
    public String getKeyStudentId() {
        return preferences.getKeyStudentId();
    }

    @Override
    public String getProfileName() {
        return preferences.getProfileName();
    }

    @Override
    public String getProfileImage() {
        return preferences.getProfileImage();
    }

    @Override
    public String getUpdateTime() {

        String time = preferences.getUpdateTime();


        return time;
    }

    @Override
    public void saveUpdateTime(String s) {
        preferences.saveUpdateTime(s);
    }

    @Override
    public void clear() {
        preferences.clear();
    }


}

