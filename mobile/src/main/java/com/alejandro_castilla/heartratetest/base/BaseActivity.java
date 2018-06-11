package com.alejandro_castilla.heartratetest.base;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.alejandro_castilla.heartratetest.R;
import com.alejandro_castilla.heartratetest.data.DataRepository;
import com.alejandro_castilla.heartratetest.data.DataSource;
import com.alejandro_castilla.heartratetest.data.pref.Pref;
import com.alejandro_castilla.heartratetest.data.pref.PreferencesHelper;
import com.alejandro_castilla.heartratetest.data.remote.ApiService;
import com.alejandro_castilla.heartratetest.data.remote.RemoteDataSource;
import com.alejandro_castilla.heartratetest.data.remote.RemoteDataSourceHelper;
import com.alejandro_castilla.heartratetest.data.retrofitclient.ApiEndPoint;
import com.alejandro_castilla.heartratetest.data.retrofitclient.RetrofitClient;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by yasar on 7/5/18.
 */

public class BaseActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks,
        EasyPermissions.RationaleCallbacks {

    private static final String TAG = "BaseActivity";

    public static final int RC_LOCATION_PERM = 124;
    public static final String[] LOCATION =
            {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private Pref pref;
    public DataSource dataSource;
    private RemoteDataSource remoteDataSource;
    private ApiService apiService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pref = PreferencesHelper.getPreferencesInstance(this);

        apiService = RetrofitClient.getRetrofitClientInstance(ApiEndPoint.BASE_URL).getRetrofit().create(ApiService.class);

        remoteDataSource = new RemoteDataSourceHelper(apiService);

        dataSource = new DataRepository(this, remoteDataSource, pref);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size());
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());

        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
//            String yes = getString(R.string.yes);
//            String no = getString(R.string.no);
//
//            // Do something after user returned from app settings screen, like showing a Toast.
//            Toast.makeText(
//                    this,
//                    getString(R.string.returned_from_app_settings_to_activity,
//                            hasCameraPermission() ? yes : no,
//                            hasLocationAndContactsPermissions() ? yes : no,
//                            hasSmsPermission() ? yes : no),
//                    Toast.LENGTH_LONG)
//                    .show();
        }
    }

    @Override
    public void onRationaleAccepted(int requestCode) {
        Log.d(TAG, "onRationaleAccepted:" + requestCode);
    }

    @Override
    public void onRationaleDenied(int requestCode) {
        Log.d(TAG, "onRationaleDenied:" + requestCode);
    }



}
