package com.alejandro_castilla.heartratetest.floatingview;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.alejandro_castilla.heartratetest.R;
import com.alejandro_castilla.heartratetest.data.DataRepository;
import com.alejandro_castilla.heartratetest.data.DataSource;
import com.alejandro_castilla.heartratetest.data.listener.DataListener;
import com.alejandro_castilla.heartratetest.data.pref.Pref;
import com.alejandro_castilla.heartratetest.data.pref.PreferencesHelper;
import com.alejandro_castilla.heartratetest.data.remote.ApiService;
import com.alejandro_castilla.heartratetest.data.remote.RemoteDataSource;
import com.alejandro_castilla.heartratetest.data.remote.RemoteDataSourceHelper;
import com.alejandro_castilla.heartratetest.data.retrofitclient.ApiEndPoint;
import com.alejandro_castilla.heartratetest.data.retrofitclient.RetrofitClient;
import com.alejandro_castilla.heartratetest.location.GPSTracker;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;


/**
 * Created by yasar on 12/5/18.
 */

public class FloatingViewService extends Service implements View.OnClickListener {

    private static final String TAG = "FloatingViewService";
    private WindowManager mWindowManager;
    private View mFloatingView;
    private View collapsedView;

    private Pref pref;
    private DataSource dataSource;
    private RemoteDataSource remoteDataSource;
    private ApiService apiService;

    private GPSTracker gpsTracker;


    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Represents a geographical location.
     */
    protected Location mLastLocation;


    private int clickCount = 0;

    public FloatingViewService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            mLastLocation = task.getResult();

//                            Log.e(TAG, "onComplete: " + String.format(Locale.ENGLISH, "%s: %f",
//                                    mLastLocation.getLatitude()));
//                            Log.e(TAG, "onComplete: " + String.format(Locale.ENGLISH, "%s: %f",
//                                    mLastLocation.getLongitude()));
                        } else {
//                            Log.w(TAG, "getLastLocation:exception", task.getException());

                        }
                    }
                });

        pref = PreferencesHelper.getPreferencesInstance(this);

        apiService = RetrofitClient.getRetrofitClientInstance(ApiEndPoint.BASE_URL).getRetrofit().create(ApiService.class);

        remoteDataSource = new RemoteDataSourceHelper(apiService);

        dataSource = new DataRepository(this, remoteDataSource, pref);
        gpsTracker = new GPSTracker(this);

        //getting the widget layout from xml using layout inflater
        mFloatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null);

        final WindowManager.LayoutParams params;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            //setting the layout parameters
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        } else {

            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);

        }


        //getting windows services and adding the floating view to it
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mFloatingView, params);


        //getting the collapsed and expanded view from the floating view
        collapsedView = mFloatingView.findViewById(R.id.layoutCollapsed);

        //adding click listener to close button and expanded view
//        mFloatingView.findViewById(R.id.collapsed_iv).setOnClickListener(this);

//        flipView = (FlipView) mFloatingView.findViewById(R.id.collapsed_iv);

        //adding an touchlistener to make drag movement of the floating widget
        mFloatingView.findViewById(R.id.relativeLayoutParent).setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            private boolean isMove = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_UP:
                        //when the drag is ended switching the state of the widget
//                        collapsedView.setVisibility(View.GONE);
//                        expandedView.setVisibility(View.VISIBLE);

                        Log.e(TAG, "onTouch: ");

//                        if (!isMove) {
                        Log.e(TAG, "onTouch: false");

//                            flipView.flip(true);

                        clickCount++;

                        changeColor(mFloatingView, clickCount);

//                            reInitializeCount();

//                        }

                        isMove = false;

                        return true;

                    case MotionEvent.ACTION_MOVE:
                        isMove = true;
                        //this code is helping the widget to move around the screen with fingers
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        mWindowManager.updateViewLayout(mFloatingView, params);
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatingView != null) mWindowManager.removeView(mFloatingView);
    }

    private void changeColor(final View mFlipView, final int clickCount1) {

        final View view = mFlipView.findViewById(R.id.collapsed_iv);
//        final View viewGreen = mFlipView.findViewById(R.id.collapsed_iv1);
        GradientDrawable bgShape = (GradientDrawable) view.getBackground();


        if (clickCount1 == 1) {
            reInitializeCount();
            bgShape.setColor(ContextCompat.getColor(this, R.color.yellow));

        } else if (clickCount1 == 2) {

            bgShape.setColor(ContextCompat.getColor(this, R.color.orange));

        } else if (clickCount1 == 3) {


            gpsTracker.getLocation();

            Double lat = gpsTracker.getLatitude();
            Double lng = gpsTracker.getLongitude();

            updateStudentLocation(lat, lng, true);

            if (handler != null) {
                handler.removeCallbacks(runnable);
            }

            bgShape.setColor(ContextCompat.getColor(this, R.color.green));
            clickCount = 0;
            view.setVisibility(View.GONE);
            mFlipView.findViewById(R.id.collapsed_iv1).setVisibility(View.VISIBLE);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    clickCount = 0;
                    view.setVisibility(View.VISIBLE);
                    mFlipView.findViewById(R.id.collapsed_iv1).setVisibility(View.GONE);
                    changeColor(mFlipView, clickCount);
                }
            }, 500);

        } else {

            bgShape.setColor(ContextCompat.getColor(this, R.color.colorAccent));

        }
    }

    private void reInitializeCount() {

        handler.postDelayed(runnable, 3000);


    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {

            clickCount = 0;

            changeColor(mFloatingView, clickCount);
        }
    };

    private Handler handler = new Handler();

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.collapsed_iv:

                clickCount++;

                changeColor(mFloatingView, clickCount);

                Log.e(TAG, "onClick: ");

                break;

//            case R.id.buttonClose:
//                //closing the widget
//                stopSelf();
//                break;
        }
    }


    private void updateStudentLocation(Double lat, Double lng, boolean is_notify) {

        Map<Object, Object> json = new HashMap<>();
        Map<Object, Object> params = new HashMap<>();
        params.put("parent_id", Integer.parseInt(dataSource.getEmailOrUsername()));
//        params.put("password", dataSource.getPassword());
        params.put("user_type", dataSource.getUserType());
        params.put("student_id", Integer.parseInt(dataSource.getKeyStudentId()));
        params.put("latitude", lat);
        params.put("longtitude", lng);
        params.put("is_notify", is_notify);

        json.put("params", params);


        final String js = new JSONObject(json).toString();

        Log.e(TAG, "updateHeartRateToServer: location " + js);
        dataSource.updateStudentLocation(js, new DataListener() {
            @Override
            public void onSuccess(Object object) {

                Log.e(TAG, "onSuccess: " + object.toString());

            }

            @Override
            public void onFail(Throwable throwable) {

                Log.e(TAG, "onFail: " + throwable.getMessage());
            }

            @Override
            public void onNetworkFailure() {

                Log.e(TAG, "onNetworkFailure: ");
            }
        });

    }
}