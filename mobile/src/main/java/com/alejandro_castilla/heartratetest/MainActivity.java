package com.alejandro_castilla.heartratetest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alejandro_castilla.heartratetest.base.BaseActivity;
import com.alejandro_castilla.heartratetest.floatingview.FloatingViewService;
import com.alejandro_castilla.heartratetest.location.GPSTracker;
import com.github.ybq.android.spinkit.SpinKitView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import pl.tajchert.waitingdots.DotsTextView;

public class MainActivity extends BaseActivity {

    private static final int DRAW_OVER_OTHER_APP_PERMISSION = 123;
    private static final String TAG = "MainActivity";

    private TextView heartRate;
    private Button panicAlert, start, stop;
    private SpinKitView spin_kit;

    private GPSTracker gpsTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        gpsTracker = new GPSTracker(this);

//        dataSource.saveSession("hemandh.kumar@gmail.com", "a", "Hemandh", "12", "Parent", true, null);
//        dataSource.saveSession("shiv", "shiv", "shiv", "247", "Parent", true, null);


        dataSource.saveUpdateTime("1 Min");

        heartRate = findViewById(R.id.heartRate);
        start = findViewById(R.id.start);
        stop = findViewById(R.id.stop);
        panicAlert = findViewById(R.id.panic_alert);

        spin_kit = findViewById(R.id.spin_kit);
//        spin_kit.setVisibility(View.VISIBLE);

        Intent intent = new Intent(getApplicationContext(), MyHeartSensorService.class);

        intent.setAction(MyHeartSensorService.START_HEART_RATE);

        startService(intent);


        panicAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (gpsTracker.canGetLocation()) {

                    gpsTracker.getLocation();

                    Double lat = gpsTracker.getLatitude();
                    Double lon = gpsTracker.getLongitude();

                    Log.e(TAG, "onClick: lat " + lat + "  long  " + lon);

                    Toast.makeText(MainActivity.this, "" + lat + "   " + lon, Toast.LENGTH_SHORT).show();


                    Log.e(TAG, "onClick: " + String.format("The address is  %S", getCompleteAddressString(lat, lon)));


                }

            }
        });


//        start.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                heartRate.setText("Starting");
//
//                Intent intent = new Intent(getApplicationContext(), MyHeartSensorService.class);
//
//                intent.setAction(MyHeartSensorService.START_HEART_RATE);
//
//                startService(intent);
//
//            }
//        });
//
//        stop.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
////                heartRate.setText("Stoped");
////
////                Intent intent = new Intent(getApplicationContext(), MyHeartSensorService.class);
////
////                intent.setAction(MyHeartSensorService.STOP_HEART_RATE);
////
////                stopService(intent);
//
//            }
//        });

        LocalBroadcastManager.getInstance(this).

                registerReceiver(mMessageReceiver,
                        new IntentFilter("HeartRateUpdating"));

        callFloatingView();
    }

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "No Geodecode";
        if (Geocoder.isPresent()) {


            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
                if (addresses != null) {
                    Address returnedAddress = addresses.get(0);
                    StringBuilder strReturnedAddress = new StringBuilder("");

                    for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                        strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                    }
                    strAdd = strReturnedAddress.toString();
                    Log.w("My Current loction address", strReturnedAddress.toString());
                } else {
                    Log.w("My Current loction address", "No Address returned!");
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.w("My Current loction address", "Canont get Address!");
            }
        }

        return strAdd;
    }

    private void askForSystemOverlayPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {

            //If the draw over permission is not available open the settings screen
            //to grant the permission.
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, DRAW_OVER_OTHER_APP_PERMISSION);
        }
    }

    private void callFloatingView() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            startService(new Intent(this, FloatingViewService.class));
//            finish();
        } else if (Settings.canDrawOverlays(getApplicationContext())) {
            startService(new Intent(this, FloatingViewService.class));
//            finish();
        } else {
            askForSystemOverlayPermission();
            Toast.makeText(getApplicationContext(), "You need System Alert Window Permission to do this", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == DRAW_OVER_OTHER_APP_PERMISSION) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    //Permission is not available. Display error text.
                    errorToast();
//                    finish();
                } else {

                    callFloatingView();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void errorToast() {
        Toast.makeText(this, "Draw over other app permission not available. Can't start panic alert without the permission.", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            Intent intent = new Intent(this, HeartRateSettingsActivity.class);
            startActivity(intent);

            return true;
        } else if (id == R.id.logout) {

            dataSource.clear();

            heartRate.setText("Stoped");

            Intent intent1 = new Intent(this, MyHeartSensorService.class);

            intent1.setAction(MyHeartSensorService.STOP_HEART_RATE);

            stopService(intent1);

            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);

            finish();

        }

        return super.onOptionsItemSelected(item);
    }


    // Our handler for received Intents. This will be called whenever an Intent
// with an action named "custom-event-name" is broadcasted.
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            Log.d("receiver", "Got message: " + message);


            int rate = Integer.parseInt(message);

//            if (rate > 0) {

            spin_kit.setVisibility(View.GONE);
            heartRate.setText("Your heart rate is : " + message);

//            } else {
//
//                spin_kit.setVisibility(View.VISIBLE);
//                heartRate.setText("Heart rate checking...");
//
//            }
        }
    };

    @Override
    protected void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

}
