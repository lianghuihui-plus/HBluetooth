package com.lhh.hbluetooth;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    protected void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show());
    }

    protected void showLongToast(String msg) {
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show());
    }

    protected void delayFinish(final long delayTime) {
        new Thread(() -> {
            try {
                Thread.sleep(delayTime);
            } catch (InterruptedException e) {
                showToast(e.getMessage());
            } finally {
                runOnUiThread(this::finish);
            }
        }).start();
    }
}
