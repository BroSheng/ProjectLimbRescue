package com.example.projectlimbrescue;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.wearable.activity.WearableActivity;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Chronometer;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.shared.Device;
import com.example.shared.Limb;
import com.example.shared.ReadingSession;
import com.example.shared.SensorReading;
import com.example.shared.SensorReadingList;
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

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/*
 * TODO: Wearable activity is deprecated. We can transition away from it, but it will take some
 *  work.
 */
public class MainActivity extends WearableActivity implements DataClient.OnDataChangedListener, MessageClient.OnMessageReceivedListener, CapabilityClient.OnCapabilityChangedListener, SensorEventListener, AdapterView.OnItemSelectedListener {
    private static final String TAG = "MainActivity";

    private static final String START_ACTIVITY_PATH = "/start-activity";
    private static final String SENSOR_PATH = "/sensor";
    private static final String SESSION_KEY = "session";
    private static final int SENSOR_REFRESH_RATE = 30000;

    private boolean isLogging = false;
    private SensorManager mSensorManager;
    private int ppgSensor = 0;
    private long startTime = 0L;
    private Chronometer timer;
    private SensorReadingList ppgReadings = null;

    private long calibrationOffset = 0L;

    private Limb limb = Limb.LEFT_ARM;

    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        timer = findViewById(R.id.timer);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        List<Sensor> sensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor : sensorList) {
            Log.d("List sensors", "Name: ${currentSensor.name} /Type_String: ${currentSensor.stringType} /Type_number: ${currentSensor.type}");
            if(sensor.getStringType().equals("com.google.wear.sensor.ppg"))
            {
                ppgSensor = sensor.getType();
                Log.d("Sensor", "Using of type ${currentSensor.type}");
                break;
            }
        }

        status = findViewById(R.id.status);

        Spinner spinner = findViewById(R.id.limb_choice);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.limbs_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
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

        if (messageEvent.getPath().equals(START_ACTIVITY_PATH)) {
            if (isLogging) {
                stopRecording();
            } else {
                startRecording(bytesToLong(messageEvent.getData()));
            }

            isLogging = !isLogging;
        }
    }

    private void startRecording(long startTime) {
        timer.setBase(SystemClock.elapsedRealtime());
        timer.start();
        this.startTime = startTime;
        this.calibrationOffset = -1L;
        this.ppgReadings = new SensorReadingList(com.example.shared.Sensor.PPG);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(ppgSensor),
                                        SENSOR_REFRESH_RATE);
        status.setText("Recording");
    }

    private void stopRecording() {
        timer.stop();
        status.setText("Sending data...");
        // TODO: Programmatically get device and limb
        ReadingSession session = new ReadingSession(Device.FOSSIL_GEN_5, this.limb);
        session.addSensor(this.ppgReadings);
        sendSensorData(session.toString().getBytes());
        status.setText("Waiting for phone...");
    }

//    private byte[] serializeSensorData(ReadingSession readingSession) {
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        ObjectOutputStream oos = null;
//        try {
//            oos = new ObjectOutputStream(bos);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        try {
//            oos.writeObject(readingSession);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return bos.toByteArray();
//    }

    private void sendSensorData(byte[] serializedReadingSession) {
        PutDataMapRequest dataMap = PutDataMapRequest.create(SENSOR_PATH);
        dataMap.getDataMap().putByteArray(SESSION_KEY, serializedReadingSession);
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

    private long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip();
        return buffer.getLong();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == ppgSensor) {
            if(this.calibrationOffset < 0) {
                this.calibrationOffset = event.timestamp;
            }
            // Convert to a 5V analog reading.
            float reading = (event.values[0] / event.sensor.getMaximumRange()) * 5.0f;
            Instant now = Instant.now();
            long sensorTime = now.getEpochSecond() * 1000000000 + now.getNano() + ((event.timestamp - calibrationOffset) % 1000000);
            long timestamp = sensorTime - startTime;
            JSONObject sensorReading = new JSONObject();
            try {
                sensorReading.put("time", timestamp);
                sensorReading.put("value", reading);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            this.ppgReadings.addReading(sensorReading);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
        switch(adapterView.getItemAtPosition(pos).toString()) {
            case "Left Arm":
                this.limb = Limb.LEFT_ARM;
                break;
            case "Right Arm":
                this.limb = Limb.RIGHT_ARM;
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}