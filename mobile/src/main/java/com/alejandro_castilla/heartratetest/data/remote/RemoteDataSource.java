package com.alejandro_castilla.heartratetest.data.remote;

import com.alejandro_castilla.heartratetest.data.listener.DataListener;

/**
 * Created by yasar on 22/3/18.
 */

public interface RemoteDataSource {


    void login(String json, DataListener dataListener);


    void updateHeartRate(String json, DataListener dataListener);

    void updateStudentLocation(String json, DataListener dataListener);


}
