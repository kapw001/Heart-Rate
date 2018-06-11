package com.alejandro_castilla.heartratetest;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.alejandro_castilla.heartratetest.base.BaseActivity;


public class HeartRateSettingsActivity extends BaseActivity implements View.OnClickListener {


    private Spinner mSpinner;
    private Button mUpdateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate_settings);

        if (getSupportActionBar() != null) {

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initView();


    }

    private void initView() {
        mSpinner = (Spinner) findViewById(R.id.spinner);
        mUpdateTime = (Button) findViewById(R.id.updateTime);

        mUpdateTime.setOnClickListener(this);

        String times = dataSource.getUpdateTime();

        setSpinnerToValue(mSpinner, times);
    }


    public void setSpinnerToValue(Spinner spinner, String value) {
        int index = 0;
        SpinnerAdapter adapter = spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).equals(value)) {
                index = i;
                break; // terminate loop
            }
        }
        spinner.setSelection(index);
    }

    @Override
    public void onClick(View v) {

        String time = (String) mSpinner.getSelectedItem();

        dataSource.saveUpdateTime(time);

        Intent intent = new Intent(getApplicationContext(), MyHeartSensorService.class);

        intent.setAction(MyHeartSensorService.START_HEART_RATE);

        startService(intent);

        finish();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                break;
        }
        return true;
    }
}
