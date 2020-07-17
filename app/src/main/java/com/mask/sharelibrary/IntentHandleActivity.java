package com.mask.sharelibrary;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class IntentHandleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intent_handle);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String action = intent.getAction();
        LogUtil.i("Intent Action: " + action);
        LogUtil.i("Intent Type: " + intent.getType());
        LogUtil.i("Intent Data: " + intent.getData());
        LogUtil.i("Intent ClipData: " + intent.getClipData());
        if (extras != null) {
            if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
                LogUtil.i("Intent EXTRA_STREAM Multiple: " + extras.getParcelableArrayList(Intent.EXTRA_STREAM));
            } else {
                LogUtil.i("Intent EXTRA_STREAM: " + extras.getParcelable(Intent.EXTRA_STREAM));
            }
        }
        LogUtil.i("Intent: " + intent);
    }
}