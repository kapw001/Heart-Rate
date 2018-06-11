package com.alejandro_castilla.heartratetest;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.IntDef;
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

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MyHeartSensorService extends Service implements SensorEventListener {


    private static final String TAG = "MyHeartSensorService";
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

    public MyHeartSensorService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

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
        notificationIntent.setAction(MyHeartSensorService.START_HEART_RATE);  // A string containing the action name
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

//        if (rate > 0) {

        if (isUpdated) {
            updateHeartRateToServer(msg);
            isUpdated = false;
        }

//        } else {

        Log.e(TAG, "sendMessage: no heart rate  , checking .......................................................................................................................................... ");
//        }

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
