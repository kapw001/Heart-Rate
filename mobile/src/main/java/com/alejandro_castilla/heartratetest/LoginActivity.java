package com.alejandro_castilla.heartratetest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.alejandro_castilla.heartratetest.base.BaseActivity;


public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private EditText mEmail;
    private EditText mPassword;
    private EditText mStudentid;
    private Button mSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (getSupportActionBar() != null) {

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initView();
    }

    private void initView() {

        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);
        mStudentid = (EditText) findViewById(R.id.studentid);
        mSubmit = (Button) findViewById(R.id.submit);

        mSubmit.setOnClickListener(this);
    }


    private void login() {

        String email = mEmail.getText().toString();
        String pass = mPassword.getText().toString();
        String stid = mStudentid.getText().toString();

        if (email.length() > 0 && pass.length() > 0 && stid.length() > 0) {


            dataSource.saveSession(email, pass, email, stid, "Parent", true, null);

            moveMainActivity();
        } else {

            Toast.makeText(this, "Please provide vaild credentials", Toast.LENGTH_SHORT).show();

        }


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

    private void moveMainActivity() {

        Intent in = new Intent(getApplicationContext(), MainActivity.class);

        in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(in);

        finish();
    }

    @Override
    public void onClick(View v) {

        login();
    }
}
