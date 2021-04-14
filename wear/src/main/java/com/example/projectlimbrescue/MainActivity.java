package com.example.projectlimbrescue;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Chronometer;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.wear.ambient.AmbientModeSupport;

import com.example.shared.DeviceDesc;
import com.example.shared.ReadingLimb;
import com.example.shared.ReadingSession;
import com.example.shared.SensorDesc;
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

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends FragmentActivity implements
        DataClient.OnDataChangedListener,
        MessageClient.OnMessageReceivedListener,
        CapabilityClient.OnCapabilityChangedListener,
        SensorEventListener,
        AdapterView.OnItemSelectedListener,
        AmbientModeSupport.AmbientCallbackProvider {

    /** Tag used for debug messages. */
    private static final String TAG = "MainActivity";

    /** Path received to indicate starting/stopping the session.  */
    private static final String START_ACTIVITY_PATH = "/start-activity";

    /** Path for sending sensor reading information. */
    private static final String SENSOR_PATH = "/sensor";

    /** Key for data transfer between mobile and wear apps. */
    private static final String SESSION_KEY = "session";

    /** Refresh the PPG sensor at 30Hz. (1/30s = 33333ms) */
    private static final int SENSOR_REFRESH_RATE = 33333;

    /** Maximum time the watch can record in seconds. Used for timing out on communication error. */
    private static final int RECORD_TIME = 35000;

    /** Indicates if the watch is recording data or not. */
    private boolean isLogging = false;

    /** System-level manager that provides sensor I/O. */
    private SensorManager mSensorManager;

    /** Id of the PPG sensor. */
    private int ppgSensor = 0;

    /** Start time in nanoseconds from the phone. */
    private long startTime = 0L;

    /** Timer view */
    private Chronometer timer;

    /** List of readings from the PPG sensor. */
    private SensorReadingList ppgReadings = null;

    /**
     * The starting timestamp when the PPG sensor starts taking readings.
     * 
     * This allows the readings to start from a timestamp of zero.
     */
    private long calibrationOffset = 0L;

    /** Which limb the watch is on. */
    private ReadingLimb limb = ReadingLimb.LEFT_ARM;

    /** Status text at the bottom of the screen. */
    private TextView status;

    /** A timeout for stopping the watch is it doesn't receive a stop signal from the phone. */
    private Timer stopFailsafe;

    /**
     * Update alarm and intent for ambient mode.
     * 
     * TODO: ambient mode is not currently working. It should show the time ticking still
     *       however, the chronometer simply sleeps instead. A short term fix was keeping
     *       the screen always on, but this kills battery fast.
     */
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                new Intent(getApplicationContext(), MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        timer = findViewById(R.id.timer);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        List<Sensor> sensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor : sensorList) {
            Log.d("List sensors", "Name: ${currentSensor.name} /Type_String: ${currentSensor.stringType} /Type_number: ${currentSensor.type}");
            if (sensor.getStringType().equals("com.google.wear.sensor.ppg")) {
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

        AmbientModeSupport.attach(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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

    /**
     * Start the reading session.
     * 
     * @param startTime Initial UTC time from the phone in nanoseconds since epoch.
     */
    private void startRecording(long startTime) {
        // Reset the chronometer on start of reading.
        timer.setBase(SystemClock.elapsedRealtime());
        timer.start();
        this.startTime = startTime;
        this.calibrationOffset = -1L;
        this.ppgReadings = new SensorReadingList(SensorDesc.PPG);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(ppgSensor),
                SENSOR_REFRESH_RATE);
        status.setText(R.string.recording_status);

        // Set a timeout for if communication is lost with the phone.
        stopFailsafe = new Timer();
        stopFailsafe.schedule(new TimerTask() {
            @Override
            public void run() {
                stopRecording();
            }
        }, RECORD_TIME);
    }

    /**
     * Stops reading session and sends data.
     */
    private void stopRecording() {
        stopFailsafe.cancel();
        runOnUiThread(() -> {
            timer.stop();
            status.setText(R.string.sending_data_status);
        });
        // TODO: Programmatically get device and limb
        ReadingSession session = new ReadingSession(DeviceDesc.FOSSIL_GEN_5, this.limb);
        session.addSensor(this.ppgReadings);
        sendSensorData(session.toJson().toString().getBytes());
        runOnUiThread(() -> status.setText(R.string.waiting_for_phone_status));
    }

    /**
     * Sends the sensor data as a flat byte array.
     *  
     * TODO: If the message couldn't be sent, it should be retried instead of giving up.
     * 
     * @param serializedReadingSession String to send as a byte array.
     */
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

    /**
     * Converts byte array to long integer.
     * 
     * @param bytes Byte array to convert.
     * @return Long integer of the byte array in little endian.
     */
    private long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip();
        return buffer.getLong();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == ppgSensor) {
            // event.timestamp is not 0 from the start so grab the timestamp of the first reading.
            if(this.calibrationOffset < 0) {
                this.calibrationOffset = event.timestamp;
            }
            float reading = event.values[0];
            long timestamp = event.timestamp - this.calibrationOffset;
            JSONObject sensorReading = new JSONObject();
            try {
                sensorReading.put("time", startTime + timestamp);
                sensorReading.put("value", reading);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            this.ppgReadings.addReading(sensorReading);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Necessary override.
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
        switch (adapterView.getItemAtPosition(pos).toString()) {
            case "Left Arm":
                this.limb = ReadingLimb.LEFT_ARM;
                break;
            case "Right Arm":
                this.limb = ReadingLimb.RIGHT_ARM;
                break;
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 1000,
                pendingIntent);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public AmbientModeSupport.AmbientCallback getAmbientCallback() {
        return new TimerAmbientCallback();
    }

    private class TimerAmbientCallback extends AmbientModeSupport.AmbientCallback {
        @Override
        public void onEnterAmbient(Bundle ambientDetails) {
            super.onEnterAmbient(ambientDetails);

            TextView status = findViewById(R.id.status);
            status.setVisibility(View.INVISIBLE);

            Spinner limbChooser = findViewById(R.id.limb_choice);
            limbChooser.setVisibility(View.INVISIBLE);
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + 1000,
                    pendingIntent);
        }

        @Override
        public void onExitAmbient() {
            super.onExitAmbient();

            TextView status = findViewById(R.id.status);
            status.setVisibility(View.VISIBLE);

            Spinner limbChooser = findViewById(R.id.limb_choice);
            limbChooser.setVisibility(View.VISIBLE);

            alarmManager.cancel(pendingIntent);
        }

        @Override
        public void onUpdateAmbient() {
        }
    }
}