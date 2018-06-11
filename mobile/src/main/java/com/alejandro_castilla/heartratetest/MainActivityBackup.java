package com.alejandro_castilla.heartratetest;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivityBackup extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "MainActivity";
//    private SensorManager mSensorManager;
//    private Sensor mHeartRateSensor;
//    private Sensor mTemp;

    private TextView heartRate;
    private Button start, stop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


//        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
//        mTemp=mSensorManager.getDefaultSensor(Sensor.TYPE_TEMPERATURE);

        heartRate = (TextView) findViewById(R.id.heartRate);
        start = (Button) findViewById(R.id.start);
        stop = (Button) findViewById(R.id.stop);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), MyHeartSensorService.class);

                intent.setAction(MyHeartSensorService.START_HEART_RATE);

                startService(intent);

//                heartRate.setText("Please wait...");
//                startMeasure();
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                heartRate.setText("--");
//                stopMeasure();

                Intent intent = new Intent(getApplicationContext(), MyHeartSensorService.class);

                intent.setAction(MyHeartSensorService.STOP_HEART_RATE);

                stopService(intent);

            }
        });
    }


    private void startMeasure() {
//        boolean sensorRegistered = mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_FASTEST);
//        Log.d("Sensor Status:", " Sensor registered: " + (sensorRegistered ? "yes" : "no"));
//        Log.e(TAG, "startMeasure:Sensor Status:" + " Sensor registered: " + (sensorRegistered ? "yes" : "no"));
    }

    private void stopMeasure() {
//        mSensorManager.unregisterListener(this);

        Log.e(TAG, "stopMeasure: ");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float mHeartRateFloat = event.values[0];

        int mHeartRate = Math.round(mHeartRateFloat);

        heartRate.setText("Heart Rate is : " + Integer.toString(mHeartRate));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        mSensorManager.unregisterListener(this);
    }
}
