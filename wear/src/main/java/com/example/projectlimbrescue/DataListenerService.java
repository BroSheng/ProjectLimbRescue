package com.example.projectlimbrescue;

import android.content.Intent;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class DataListenerService extends WearableListenerService {

    private static final String TAG = "DataService";

    private static final String START_ACTIVITY_PATH = "/start-activity";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(START_ACTIVITY_PATH)) {
            Intent startIntent = new Intent(this, MainActivity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startIntent);
        } else {
            super.onMessageReceived(messageEvent);
        }
    }
}
