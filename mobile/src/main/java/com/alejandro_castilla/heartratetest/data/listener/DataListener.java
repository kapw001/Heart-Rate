package com.alejandro_castilla.heartratetest.data.listener;

/**
 * Created by yasar on 28/2/18.
 */

public interface DataListener {

    void onSuccess(Object object);

    void onFail(Throwable throwable);

    void onNetworkFailure();

}
