package com.lhh.hbluetooth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WriteActivity extends BaseActivity {

    @BindView(R.id.read_text_view)
    public TextView readTextView;

    @BindView(R.id.write_text)
    public EditText writeText;

    @OnClick(R.id.test_write_button) void testWrite() {
        String content = writeText.getText().toString();
        mConnection.write(content.getBytes());
    }

    @OnClick(R.id.disconnect_button) void disConnect() {
        HBUtil.getInstance().disconnectDevice(mConnection.getDevcieAddress());
        finish();
    }

    private HBConnection mConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        String address = intent.getStringExtra("address");
        mConnection = HBUtil.getInstance().getConnection(address);

        mConnection.registerListener(WriteActivity.class.getName(), new HBConnectionListener() {
            @Override
            public void onDisconnected(String address) {
                showToast("Connction is Disconnected!");
                finish();
            }

            @Override
            public void onRead(byte[] cache) {
                runOnUiThread(() -> {
                   readTextView.append(new String(cache) + "\n");
                });
            }

            @Override
            public void onError(int code) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mConnection.unregisterListener(WriteActivity.class.getName());
    }

    @Override
    protected void afterRequestPermission(int requestCode, boolean isAllGranted) {

    }
}