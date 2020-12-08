package com.lhh.hbluetooth;

import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public abstract class BaseActivity extends AppCompatActivity {

    protected void showToast(String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected void showLongToast(String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), text, Toast.LENGTH_LONG).show();
            }
        });
    }

    protected boolean checkPermissions(String[] neededPermissions) {
        boolean allGranted = true;
        for (String permission: neededPermissions)
            allGranted &= (ContextCompat.checkSelfPermission(getBaseContext(), permission)
                    == PackageManager.PERMISSION_GRANTED);
        return allGranted;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean allGranted = true;
        for (int grantResult: grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }
        afterRequestPermission(requestCode, allGranted);
    }

    protected void afterRequestPermission(int requestCode, boolean allGranted) {}
}
