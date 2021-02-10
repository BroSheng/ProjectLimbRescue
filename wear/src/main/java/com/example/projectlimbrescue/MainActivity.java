package com.example.projectlimbrescue;

import android.net.Uri;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.myapplication.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

/*
 * TODO: Wearable activity is deprecated. We can transition away from it, but it will take some
 *  work.
 */
public class MainActivity extends WearableActivity implements DataClient.OnDataChangedListener, MessageClient.OnMessageReceivedListener, CapabilityClient.OnCapabilityChangedListener {
    private static final String TAG = "MainActivity";

    private static final String SENSOR_PATH = "/sensor";
    private static final String PPG_KEY = "ppg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView mTextView = (TextView) findViewById(R.id.text);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Instantiates clients without member variables, as clients are inexpensive to create and
        // won't lose their listeners. (They are cached and shared between GoogleApi instances.)
        Wearable.getDataClient(this).addListener(this);
        Wearable.getMessageClient(this).addListener(this);
        Wearable.getCapabilityClient(this)
                .addListener(this, Uri.parse("wear://"), CapabilityClient.FILTER_REACHABLE);
    }

    @Override
    protected void onPause() {
        super.onPause();

        Wearable.getDataClient(this).removeListener(this);
        Wearable.getMessageClient(this).removeListener(this);
        Wearable.getCapabilityClient(this).removeListener(this);
    }

    @Override
    public void onCapabilityChanged(@NonNull CapabilityInfo capabilityInfo) {
        Log.d(TAG, "onCapabilityChanged: " + capabilityInfo);
    }

    @Override
    public void onDataChanged(@NonNull DataEventBuffer dataEventBuffer) {
        Log.d(TAG, "onDataChanged: " + dataEventBuffer);
    }

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        Log.d(TAG, "onMessageReceived() A message from mobile was received: "
        + messageEvent.getRequestId()
        + " "
        + messageEvent.getPath());
    }

    private void sendSensorData(byte[] serializedSensorData) {
        PutDataMapRequest dataMap = PutDataMapRequest.create(SENSOR_PATH);
        dataMap.getDataMap().putByteArray(PPG_KEY, serializedSensorData);
        dataMap.getDataMap().putLong("time", new Date().getTime());

        PutDataRequest putDataRequest = dataMap.asPutDataRequest();
        putDataRequest.setUrgent();

        Task<DataItem> dataItemTask = Wearable.getDataClient(this).putDataItem(putDataRequest);
        dataItemTask.addOnSuccessListener(
                new OnSuccessListener<DataItem>() {
                    @Override
                    public void onSuccess(DataItem dataItem) {
                        Log.d(TAG, "Sending data was successful: " + dataItem);
                    }
                });
    }
}