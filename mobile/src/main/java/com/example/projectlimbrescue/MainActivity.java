package com.example.projectlimbrescue;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.room.Room;

import com.example.projectlimbrescue.db.AppDatabase;
import com.example.projectlimbrescue.db.device.Device;
import com.example.projectlimbrescue.db.device.DeviceDao;
import com.example.projectlimbrescue.db.device.DeviceDesc;
import com.example.projectlimbrescue.db.reading.Reading;
import com.example.projectlimbrescue.db.reading.ReadingDao;
import com.example.projectlimbrescue.db.reading.ReadingLimb;
import com.example.projectlimbrescue.db.sensor.Sensor;
import com.example.projectlimbrescue.db.sensor.SensorDao;
import com.example.projectlimbrescue.db.sensor.SensorDesc;
import com.example.projectlimbrescue.db.session.Session;
import com.example.projectlimbrescue.db.session.SessionDao;
import com.example.projectlimbrescue.db.session.SessionWithReadings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private static final String XVALUES = "xvalues";
    private static final String YVALUES = "yvalues";

    AppDatabase db;
    DeviceDao deviceDao;
    ReadingDao readingDao;
    SensorDao sensorDao;
    SessionDao sessionDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // POC constants
        final int DEVICE_ID = 123;
        final int SENSOR_ID = 123;
        final int READING_ID = 1870;
        final int SESSION_ID = 123;
        final double NANO_TO_MILLI = 0.000001;

        db = Room.inMemoryDatabaseBuilder(getApplicationContext(), AppDatabase.class).allowMainThreadQueries().build();

        sessionDao = db.sessionDao();
        sensorDao = db.sensorDao();
        deviceDao = db.deviceDao();
        readingDao = db.readingDao();

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

                // insert the reading itself
                Reading reading = new Reading();
                reading.deviceId = 123;
                reading.sensorId = 123;
                reading.sessionId = session.sessionId;
                reading.time = Long.parseLong(nums[0]);
                reading.value = Float.parseFloat(nums[1]);
                reading.limb = ReadingLimb.LEFT_ARM;
                readingDao.insert(reading);
            }
            myReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // insert start and end time into session
        // hardcoding this because proof of concept
        session.startTime = new Timestamp(53986952);
        session.startTime = new Timestamp(54015985);

        // get session with readings
        List<SessionWithReadings> sessions = sessionDao.getSessionsWithReadings();

        SessionWithReadings mostRecentSession = sessions.get(0);

        // turn readings into x and y arrays
        long[] xVals = new long[mostRecentSession.readings.size()];
        float[] yVals = new float[mostRecentSession.readings.size()];

        // fill the values
        for (int i = 0; i < xVals.length; i++) {
            xVals[i] = mostRecentSession.readings.get(i).time;
            yVals[i] = mostRecentSession.readings.get(i).value;
        }

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container_view);

        // initialize GraphFragment
        if (fragment == null) {
            Bundle bundle = new Bundle();
            bundle.putLongArray(XVALUES, xVals);
            bundle.putFloatArray(YVALUES, yVals);
            fragment = new GraphFragment();
            fragment.setArguments(bundle);
            fm.beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragment_container_view, fragment)
                    .commit();
        }
    }
}
