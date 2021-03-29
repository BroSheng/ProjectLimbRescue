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

        List<Long> xVals = new ArrayList<>();
        List<Double> yVals = new ArrayList<>();

        AppDatabase db = DatabaseSingleton.getInstance(getBaseContext());

        // put data into db
        SessionDao sessionDao = db.sessionDao();
        SensorDao sensorDao = db.sensorDao();
        DeviceDao deviceDao = db.deviceDao();
        ReadingDao readingDao = db.readingDao();

        Session session = new Session();
        session.sessionId = 123;
        session.startTime = new Timestamp(1000);
        session.endTime = new Timestamp(2000);

        // create valid entities for reading foreign keys
        Device device = new Device();
        device.deviceId = 123;
        device.desc = DeviceDesc.FOSSIL_GEN_5;
        deviceDao.insert(device);

        Sensor sensor = new Sensor();
        sensor.sensorId = 123;
        sensor.desc = SensorDesc.PPG;
        sensorDao.insert(sensor);

        sessionDao.insert(session);

        // get data from CSV file
        // turn CSV file into readings
        List<Reading> readings = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(getAssets().open("data.csv")));
            Scanner myReader = new Scanner(reader);
            String data;

            while (myReader.hasNextLine()) {
                data = myReader.nextLine();
                String[] nums = data.split(",");
                xVals.add(Long.parseLong(nums[0]));
                yVals.add(Double.parseDouble(nums[1]));

                // insert the reading itself
                // left limb
                Reading reading = new Reading();
                reading.deviceId = 123;
                reading.sensorId = 123;
                reading.sessionId = session.sessionId;
                reading.time = Long.parseLong(nums[0]);
                reading.value = Float.parseFloat(nums[1]);
                reading.limb = ReadingLimb.LEFT_ARM;
                readingDao.insert(reading);

                // right limb
                // for right limb make up values for testing purposes
                Random random = new Random();

                Reading reading1 = new Reading();
                reading1.deviceId = 123;
                reading1.sensorId = 123;
                reading1.sessionId = session.sessionId;
                reading1.time = Long.parseLong(nums[0]);
                reading1.value = Double.parseDouble(nums[1]);
                reading1.value += random.nextDouble();
                reading1.limb = ReadingLimb.RIGHT_ARM;
                readingDao.insert(reading1);
            }
            myReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // convert since I have no easy way of doing this
        long[] xValues = new long[xVals.size()];
        double[] yValues = new double[yVals.size()];
        for (int i = 0; i < xVals.size(); i++) {
            xValues[i] = xVals.get(i);
            yValues[i] = yVals.get(i);
        }
        // insert start and end time into session
        // hardcoding this because proof of concept
        session.startTime = new Timestamp(53986952);
        session.startTime = new Timestamp(54015985);

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
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch(item.getItemId()) {
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
                    }

                });
    }

}