package com.alejandro_castilla.heartratetest;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

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
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MyHeartSensorServiceBackupLocation extends Service implements SensorEventListener {


    private static final String TAG = "MyHeartSensorService";
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    public static String START_HEART_RATE = "START_HEART_RATE";
    public static String STOP_HEART_RATE = "STOP_HEART_RATE";
    private String NOTIFICATION_CHANNEL_ID = "heartrate";
    private Pref pref;
    private DataSource dataSource;
    private RemoteDataSource remoteDataSource;
    private ApiService apiService;

    private SensorManager mSensorManager;
    private Sensor mHeartRateSensor;

    private Timer timer;
    private Timer timerLocationUpdate;

    private String heartRate = "0";
    private GPSTracker gpsTracker;

    private boolean isUpdated = true;

    private Notification notification;

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // Keys for storing activity state in the Bundle.
    private final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    private final static String KEY_LOCATION = "location";
    private final static String KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string";

    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Provides access to the Location Settings API.
     */
    private SettingsClient mSettingsClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest mLocationRequest;

    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    private LocationSettingsRequest mLocationSettingsRequest;

    /**
     * Callback for Location events.
     */
    private LocationCallback mLocationCallback;

    /**
     * Represents a geographical location.
     */
    private Location mCurrentLocation;

    public MyHeartSensorServiceBackupLocation() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);


        // Kick off the process of building the LocationCallback, LocationRequest, and
        // LocationSettingsRequest objects.
        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();
        startLocationUpdates();
        timer = new Timer();

        timerLocationUpdate = new Timer();

        gpsTracker = new GPSTracker(this);

        pref = PreferencesHelper.getPreferencesInstance(this);

        apiService = RetrofitClient.getRetrofitClientInstance(ApiEndPoint.BASE_URL).getRetrofit().create(ApiService.class);

        remoteDataSource = new RemoteDataSourceHelper(apiService);

        dataSource = new DataRepository(this, remoteDataSource, pref);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
    }


    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Creates a callback for receiving location events.
     */
    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                mCurrentLocation = locationResult.getLastLocation();

                Log.e(TAG, "onLocationResult: location " + locationResult.getLastLocation().getLatitude() + "  " + locationResult.getLastLocation().getLongitude());

            }
        };
    }

    private void startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                        //noinspection MissingPermission
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
//                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
//                                        "location settings ");
//                                try {
//                                    // Show the dialog by calling startResolutionForResult(), and check the
//                                    // result in onActivityResult().
//                                    ResolvableApiException rae = (ResolvableApiException) e;
//                                    rae.startResolutionForResult(MainActivity.class, REQUEST_CHECK_SETTINGS);
//                                } catch (IntentSender.SendIntentException sie) {
//                                    Log.i(TAG, "PendingIntent unable to execute request.");
//                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(MyHeartSensorServiceBackupLocation.this, errorMessage, Toast.LENGTH_LONG).show();

                        }

                    }
                });
    }


    /**
     * Uses a {@link LocationSettingsRequest.Builder} to build
     * a {@link LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        if (intent != null) {

            updateLocation();

            updateNotification(heartRate);

            String action = intent.getAction();


            if (action.equalsIgnoreCase(START_HEART_RATE)) {

                startMeasure();

            } else if (action.equalsIgnoreCase(STOP_HEART_RATE)) {


                stopMeasure();
            }


        }


        return START_STICKY;

    }

    private void updateLocation() {

        timerLocationUpdate.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                Double lat = gpsTracker.getLatitude();
                Double lng = gpsTracker.getLongitude();

                updateStudentLocation(lat, lng, false);

            }
        }, 1000, 60000);
    }


    private void updateNotification(String heartRate) {

        String msg = "";

        if (Integer.parseInt(heartRate) > 0) {

            msg = "Your hear rate is : " + heartRate;
        } else {

            msg = "Checking heart rate";
        }


        NotificationChannel notificationChannel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_MAX);

            // Configure the notification channel.
            notificationChannel.setDescription("Heart Rate");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(false);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);
        }

        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        notificationIntent.setAction(MyHeartSensorServiceBackupLocation.START_HEART_RATE);  // A string containing the action name
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

//            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.my_icon);

        notification = new NotificationCompat.Builder(this)
                .setChannelId(NOTIFICATION_CHANNEL_ID)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setTicker(getResources().getString(R.string.app_name))
                .setContentText(msg)
                .setSmallIcon(R.mipmap.ic_launcher)
//                    .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(contentPendingIntent)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
//                .setDeleteIntent(contentPendingIntent)  // if needed
                .build();
        notification.flags = notification.flags | Notification.FLAG_NO_CLEAR;     // NO_CLEAR makes the notification stay when the user performs a "delete all" command
        startForeground(999, notification);

    }


    private void startMeasure() {

        boolean sensorRegistered = mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_FASTEST);
        Log.d("Sensor Status:", " Sensor registered: " + (sensorRegistered ? "yes" : "no"));
        Log.e(TAG, "startMeasure:Sensor Status:" + " Sensor registered: " + (sensorRegistered ? "yes" : "no"));


        String times = dataSource.getUpdateTime();

        if (times != null) {

            long convertTime = 0;

            if (times.toLowerCase().contains("sec")) {

                times = getTime(times);

                convertTime = Long.parseLong(times);

            } else if (times.toLowerCase().contains("min")) {

                times = getTime(times);

                long milliseconds = Long.parseLong(times) * 60000;

                convertTime = milliseconds;

            } else if (times.toLowerCase().contains("Hour".toLowerCase())) {

                times = getTime(times);

                long milliseconds = Integer.parseInt(times) * 60000 * 60;

                convertTime = milliseconds;

            }

            if (timer != null) {

                timer.cancel();
                timer = null;

                timer = new Timer();

                timer.scheduleAtFixedRate(new TimeUpadate(), 0, convertTime);
            }

        } else {

            timer.scheduleAtFixedRate(new TimeUpadate(), 1000, 1000);
        }


    }

    private String getTime(String str) {

        int firstSpace = (str.indexOf(" ") >= 0) ? str.indexOf(" ") : str.length() - 1;
        return str.substring(0, firstSpace);
    }

    private void stopMeasure() {
        mSensorManager.unregisterListener(this);

        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        Log.e(TAG, "stopMeasure: ");
    }

    private class TimeUpadate extends TimerTask {

        @Override
        public void run() {

            sendMessage(heartRate);
//            Handler handler = new Handler(Looper.getMainLooper());
//            handler.post(new Runnable() {
//
//                @Override
//                public void run() {
//                    Toast.makeText(getApplicationContext(), heartRate + "",
//                            Toast.LENGTH_SHORT).show();
//                }
//            });
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float mHeartRateFloat = event.values[0];

        final int mHeartRate = Math.round(mHeartRateFloat);

        heartRate = mHeartRate + "";

        updateNotification(heartRate);
//        heartRate.setText("Heart Rate is : " + Integer.toString(mHeartRate));


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onDestroy() {
        stopMeasure();
        super.onDestroy();
    }


    // Send an Intent with an action named "custom-event-name". The Intent sent should
// be received by the ReceiverActivity.
    private void sendMessage(String msg) {
        Log.d("sender", "Broadcasting message");
        Intent intent = new Intent("HeartRateUpdating");
        // You can also include some extra data.
        intent.putExtra("message", msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);


        int rate = Integer.parseInt(msg);

        if (rate > 0) {

            if (isUpdated) {
                updateHeartRateToServer(msg);
                isUpdated = false;
            }

        } else {

            Log.e(TAG, "sendMessage: no heart rate  , checking .......................................................................................................................................... ");
        }

    }

    private void updateHeartRateToServer(String heartRate) {

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                isUpdated = true;
            }

        }, 4000);


//        Date date = new Date();
//
//        Date date1 = new SimpleDateFormat().parse(date);

        int rate = Integer.parseInt(heartRate);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        String date = simpleDateFormat.format(new Date());

        Map<Object, Object> json = new HashMap<>();
        Map<Object, Object> params = new HashMap<>();
        params.put("parent_id", dataSource.getEmailOrUsername());
//        params.put("password", dataSource.getPassword());
        params.put("user_type", dataSource.getUserType());
        params.put("student_id", Integer.parseInt(dataSource.getKeyStudentId()));
        params.put("time", date);
        params.put("heartrate", rate);

        json.put("params", params);


        final String js = new JSONObject(json).toString();

        Log.e(TAG, "updateHeartRateToServer: " + js);
        dataSource.updateHeartRate(js, new DataListener() {
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


    private void updateStudentLocation(Double lat, Double lng, boolean is_notify) {

        Map<Object, Object> json = new HashMap<>();
        Map<Object, Object> params = new HashMap<>();
        params.put("parent_id", dataSource.getEmailOrUsername());
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
