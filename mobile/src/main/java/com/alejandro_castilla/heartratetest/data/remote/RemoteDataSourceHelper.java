package com.alejandro_castilla.heartratetest.data.remote;

import android.util.Log;


import com.alejandro_castilla.heartratetest.data.listener.DataListener;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

/**
 * Created by yasar on 6/3/18.
 */

public class RemoteDataSourceHelper implements RemoteDataSource {

    private static final String TAG = "RemoteDataSourceHelper";

    private ApiService apiService;

    public RemoteDataSourceHelper(ApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public void login(String json, final DataListener dataListener) {

        Log.e(TAG, "login: " + json);

        apiService.loginValidate(json).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(observer(dataListener));
    }

    @Override
    public void updateHeartRate(String json, DataListener dataListener) {

        apiService.upadateHeartRate(json).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(observer(dataListener));

    }

    @Override
    public void updateStudentLocation(String json, DataListener dataListener) {

        apiService.updateStudentLocation(json).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(observer(dataListener));

    }


    private static <T> Observer<T> observer(final DataListener dataListener) {

        return new Observer<T>() {

            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(T value) {

                dataListener.onSuccess(value);

            }

            @Override
            public void onError(Throwable e) {


                Log.e(TAG, "onError: Handle for testing purpose " + e.getMessage());


                dataListener.onFail(e);
            }

            @Override
            public void onComplete() {

//                Log.e(TAG, "onComplete:  qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq");

            }
        };

    }


}
