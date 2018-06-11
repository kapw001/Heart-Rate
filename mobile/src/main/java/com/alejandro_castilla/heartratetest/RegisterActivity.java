package com.alejandro_castilla.heartratetest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alejandro_castilla.heartratetest.base.BaseActivity;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;


public class RegisterActivity extends BaseActivity implements View.OnClickListener {

    private Button qrscan;

    //qr code scanner object
    private IntentIntegrator qrScan;
    private TextView resultTextview;
    private Button mLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initView();

        //intializing scan object
        qrScan = new IntentIntegrator(this);


        if (dataSource.isLoggedIn()) {

            moveMainActivity();
        }


    }


    //Getting the scan results
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            //if qrcode has nothing in it
            if (result.getContents() == null) {
                Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show();
                resultTextview.setText("  Result Not Found  ");
            } else {
                //if qr contains data
                try {
                    //converting the data to json
                    JSONObject obj = new JSONObject(result.getContents());

                    JSONObject jsonObject = obj.getJSONObject("params");
//
                    String parent_id = jsonObject.optString("parent_id");
//
//                    String password = jsonObject.optString("password");
//
                    String user_type = jsonObject.optString("user_type");
//
                    String student_id = jsonObject.optString("student_id");
//
//
                    dataSource.saveSession(parent_id, "", "", student_id, user_type, true, null);


                    moveMainActivity();


                    Toast.makeText(this, "Successfully registered...", Toast.LENGTH_SHORT).show();


//                    resultTextview.setText(obj.toString());

//                    //setting values to textviews
//                    textViewName.setText(obj.getString("name"));
//                    textViewAddress.setText(obj.getString("address"));
                } catch (JSONException e) {
                    e.printStackTrace();
                    //if control comes here
                    //that means the encoded format not matches
                    //in this case you can display whatever data is available on the qrcode
                    //to a toast
                    Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();
                    resultTextview.setText("Json Error  " + result.getContents());
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.qrscan:
//        initiating the qr code scan
                locationAndContactsTask();

                break;

            case R.id.login:


                login();

                break;

        }


    }


    @AfterPermissionGranted(RC_LOCATION_PERM)
    public void locationAndContactsTask() {
        if (EasyPermissions.hasPermissions(this, LOCATION)) {

            qrScan.initiateScan();

        } else {
            // Ask for both permissions
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.rationale_location_contacts),
                    RC_LOCATION_PERM,
                    LOCATION);
        }
    }


    private void login() {

        startActivity(new Intent(this, LoginActivity.class));

    }

    private void initView() {
        qrscan = (Button) findViewById(R.id.qrscan);
        resultTextview = (TextView) findViewById(R.id.values);


        qrscan.setOnClickListener(this);
        mLogin = (Button) findViewById(R.id.login);
        mLogin.setOnClickListener(this);
    }


    private void moveMainActivity() {

        Intent in = new Intent(getApplicationContext(), MainActivity.class);

        startActivity(in);

        finish();
    }
}
