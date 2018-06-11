package com.alejandro_castilla.heartratetest.broadcastrecivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.alejandro_castilla.heartratetest.MyHeartSensorService;
import com.alejandro_castilla.heartratetest.data.pref.Pref;
import com.alejandro_castilla.heartratetest.data.pref.PreferencesHelper;

/**
 * Created by yasar on 9/5/18.
 */

public class BootCompletedReceiver extends BroadcastReceiver {

    private static final String TAG = "BootCompletedReceiver";
    private Pref pref;

    @Override
    public void onReceive(Context context, Intent arg1) {
        // TODO Auto-generated method stub
        Log.w("boot_broadcast_poc", "starting service...");

        pref = PreferencesHelper.getPreferencesInstance(context);

        if (pref.isLoggedIn()) {

            Intent intent = new Intent(context, MyHeartSensorService.class);

            intent.setAction(MyHeartSensorService.START_HEART_RATE);

            context.startService(intent);

        } else {

            Log.e(TAG, "onReceive: ");

        }
    }

}