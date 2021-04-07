package com.example.projectlimbrescue;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.room.Database;
import androidx.room.Room;

import com.example.projectlimbrescue.db.AppDatabase;
import com.example.projectlimbrescue.db.DatabaseSingleton;
import com.example.projectlimbrescue.db.device.Device;
import com.example.projectlimbrescue.db.device.DeviceDao;
import com.example.projectlimbrescue.db.reading.Reading;
import com.example.projectlimbrescue.db.reading.ReadingDao;
import com.example.projectlimbrescue.db.sensor.Sensor;
import com.example.projectlimbrescue.db.sensor.SensorDao;
import com.example.projectlimbrescue.db.session.Session;
import com.example.projectlimbrescue.db.session.SessionDao;
import com.example.shared.DeviceDesc;
import com.example.shared.ReadingLimb;
import com.example.shared.SensorDesc;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.material.bottomnavigation.BottomNavigationMenu;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView mNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* -------------------- BEGIN DB INSERTION -------------------- */
        AppDatabase db = DatabaseSingleton.getInstance(getBaseContext());

        // put data into db
        SessionDao sessionDao = db.sessionDao();
        SensorDao sensorDao = db.sensorDao();
        DeviceDao deviceDao = db.deviceDao();
        ReadingDao readingDao = db.readingDao();

        // create valid entities for reading foreign keys
        Device device = new Device();
        device.deviceId = 123;
        device.desc = DeviceDesc.FOSSIL_GEN_5;
        deviceDao.insert(device);

        Sensor sensor = new Sensor();
        sensor.sensorId = 321;
        sensor.desc = SensorDesc.PPG;
        sensorDao.insert(sensor);

        // create valid entities for reading foreign keys
        Device device1 = new Device();
        device1.deviceId = 456;
        device1.desc = DeviceDesc.FOSSIL_GEN_5;
        deviceDao.insert(device1);

        Sensor sensor1 = new Sensor();
        sensor1.sensorId = 654;
        sensor1.desc = SensorDesc.PPG;
        sensorDao.insert(sensor1);

        // keeps track of used IDs do we don't repeat them
        List<Integer> sessionIDs = new LinkedList<>();

        Random rand = new Random();
        // randomly generate sessions
        for (int i = 0; i < 100; i++) {
            Session session = new Session();
            int id;
            do {
                id = rand.nextInt(10000);
            } while (sessionIDs.contains(id));
            session.sessionId = id;
            sessionIDs.add(id);
            long time = 1609477200000L;    // 03/05/2021 in UNIX time (ms)
            long start = time + rand.nextInt(947720000);
            session.startTime = new Timestamp(start);
            // add 30 seconds to start time go get end time
            session.endTime = new Timestamp(start + 30000);
            sessionDao.insert(session);

            List<Reading> readings = new ArrayList<>();

            long currentTime = start;
            boolean generateBoth = rand.nextBoolean();
            Log.d("GENERATE", "generateBoth = " + generateBoth);
            boolean generateLeft = rand.nextBoolean();

            // generate readings for each session
            for (int j = 0; j < 800; j++) {

                // insert the reading itself
                // left limb
                if (generateBoth || generateLeft) {
                    Reading reading = new Reading();
                    reading.deviceId = device.deviceId;
                    reading.sensorId = sensor.sensorId;
                    reading.sessionId = session.sessionId;
                    reading.time = currentTime;
                    reading.value = rand.nextDouble();
                    reading.limb = ReadingLimb.LEFT_ARM;
                    readingDao.insert(reading);
                }

                // right limb
                if (generateBoth || !generateLeft) {
                    Reading reading1 = new Reading();
                    reading1.deviceId = device1.deviceId;
                    reading1.sensorId = sensor1.sensorId;
                    reading1.sessionId = session.sessionId;
                    reading1.time = currentTime;
                    reading1.value = rand.nextDouble();
                    reading1.limb = ReadingLimb.RIGHT_ARM;
                    readingDao.insert(reading1);
                }

                // increment time by 33 ms to maintain 30hz
                currentTime += 33;
            }
        }
        /* -------------------- END DB INSERTION -------------------- */

        mNavigation = findViewById(R.id.bottom_navigation);

        // initialize fragment
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container_view);

        if (fragment == null) {
            fragment = new ReadingsFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container_view, fragment)
                    .commit();
        }

        // set up onclick listener for navigation view
        mNavigation.setOnNavigationItemSelectedListener(
                item -> {
                    switch (item.getItemId()) {
                        case R.id.action_readings:
                            fm.beginTransaction()
                                    .replace(R.id.fragment_container_view, ReadingsFragment.class, null)
                                    .commit();
                            break;
                        case R.id.action_history:
                            fm.beginTransaction()
                                    .replace(R.id.fragment_container_view, HistoryFragment.class, null)
                                    .commit();
                            break;
                        case R.id.action_settings:
                            // TODO create settings fragment
                            break;
                        default:
                            break;
                    }
                    return true;
                });
    }

}