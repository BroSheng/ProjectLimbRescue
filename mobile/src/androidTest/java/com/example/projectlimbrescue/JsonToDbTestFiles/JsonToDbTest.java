package com.example.projectlimbrescue.JsonToDbTestFiles;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.projectlimbrescue.JsonToDb;
import com.example.projectlimbrescue.db.AppDatabase;
import com.example.projectlimbrescue.db.device.Device;
import com.example.projectlimbrescue.db.device.DeviceDao;
import com.example.shared.DeviceDesc;
import com.example.projectlimbrescue.db.device.DeviceWithReadings;
import com.example.projectlimbrescue.db.device.DeviceWithSensors;
import com.example.projectlimbrescue.db.reading.Reading;
import com.example.projectlimbrescue.db.reading.ReadingDao;
import com.example.projectlimbrescue.db.sensor.Sensor;
import com.example.projectlimbrescue.db.sensor.SensorDao;
import com.example.shared.SensorDesc;
import com.example.projectlimbrescue.db.sensor.SensorWithReadings;
import com.example.projectlimbrescue.db.session.Session;
import com.example.projectlimbrescue.db.session.SessionDao;
import com.example.projectlimbrescue.db.session.SessionWithDevices;
import com.example.projectlimbrescue.db.session.SessionWithReadings;
import com.example.projectlimbrescue.db.session.SessionWithSensors;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.Timestamp;

import static org.junit.Assert.assertEquals;

/**
 * Test class for JsonToDb.
 */

@RunWith(AndroidJUnit4.class)
public class JsonToDbTest {
    private AppDatabase db;
    private SessionDao sessionDao;
    private DeviceDao deviceDao;
    private SensorDao sensorDao;
    private ReadingDao readingDao;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        sessionDao = db.sessionDao();
        deviceDao = db.deviceDao();
        sensorDao = db.sensorDao();
        readingDao = db.readingDao();
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void insertSingleJsonOneReading() throws Exception {
        String json1Str = "{\"desc\":\"FOSSIL_GEN_5\",\"limb\":\"LEFT_ARM\",\"sensors\":[{\"desc\":\"PPG\",\"readings\":[{\"time\":12.34,\"value\":56.78}]}]}";
        JSONObject json1 = new JSONObject(json1Str);

        // Build up and insert a Session
        Session session = new Session();
        session.startTime = new Timestamp(1000);
        session.endTime = new Timestamp(2000);
        long sessionId = sessionDao.insert(session)[0];

        JsonToDb.InsertJson(json1, sessionId, db);

        // verify that all session connections exist
        SessionWithDevices sessionWithDevices = sessionDao.getSessionsWithDevicesByIds(new long[]{sessionId}).get(0);
        Device device = sessionWithDevices.devices.get(0);
        assertEquals(DeviceDesc.FOSSIL_GEN_5, device.desc);

        SessionWithSensors sessionWithSensors = sessionDao.getSessionsWithSensorsByIds(new long[]{sessionId}).get(0);
        Sensor sensor = sessionWithSensors.sensors.get(0);
        assertEquals(SensorDesc.PPG, sensor.desc);

        SessionWithReadings sessionWithReadings = sessionDao.getSessionsWithReadingsByIds(new long[]{sessionId}).get(0);
        Reading reading = sessionWithReadings.readings.get(0);
        assertEquals((long)12.34, reading.time);
        assertEquals(56.78, reading.value, 0.01f);

        // verify that all device connections exist
        long deviceId = device.deviceId;

        DeviceWithSensors deviceWithSensors = deviceDao.getDevicesWithSensorsByIds(new long[]{deviceId}).get(0);
        sensor = deviceWithSensors.sensors.get(0);
        assertEquals(SensorDesc.PPG, sensor.desc);

        DeviceWithReadings deviceWithReadings = deviceDao.getDevicesWithReadingsByIds(new long[]{deviceId}).get(0);
        reading = deviceWithReadings.readings.get(0);
        assertEquals((long)12.34, reading.time);
        assertEquals(56.78, reading.value, 0.01f);

        // verify that all sensor connections exist
        long sensorId = sensor.sensorId;

        SensorWithReadings sensorWithReadings = sensorDao.getSensorsWithReadingsByIds(new long[]{sensorId}).get(0);
        reading = deviceWithReadings.readings.get(0);
        assertEquals((long)12.34, reading.time);
        assertEquals(56.78, reading.value, 0.01f);
    }

    @Test
    public void insertOneJsonMultipleReadings() throws Exception {
        String json1Str = "{\"desc\":\"FOSSIL_GEN_5\",\"limb\":\"LEFT_ARM\",\"sensors\":[{\"desc\":\"PPG\",\"readings\":[{\"time\":12.34,\"value\":56.78},{\"time\":34.56,\"value\":78.90}]}]}";
        JSONObject json1 = new JSONObject(json1Str);

        // Build up and insert a Session
        Session session = new Session();
        session.startTime = new Timestamp(1000);
        session.endTime = new Timestamp(2000);
        long sessionId = sessionDao.insert(session)[0];

        JsonToDb.InsertJson(json1, sessionId, db);

        // verify both readings exist
        SessionWithReadings sessionWithReadings = sessionDao.getSessionsWithReadingsByIds(new long[]{sessionId}).get(0);
        Reading reading1 = sessionWithReadings.readings.get(0);
        assertEquals((long)12.34, reading1.time);
        assertEquals(56.78, reading1.value, 0.01f);

        Reading reading2 = sessionWithReadings.readings.get(1);
        assertEquals((long)34.56, reading2.time);
        assertEquals(78.90, reading2.value, 0.01f);
    }

    @Test
    public void insertMultipleJsonsOneReadingEach() throws Exception {
        String json1Str = "{\"desc\":\"FOSSIL_GEN_5\",\"limb\":\"LEFT_ARM\",\"sensors\":[{\"desc\":\"PPG\",\"readings\":[{\"time\":12.34,\"value\":56.78}]}]}";
        JSONObject json1 = new JSONObject(json1Str);
        String json2Str = "{\"desc\":\"FOSSIL_GEN_5\",\"limb\":\"LEFT_ARM\",\"sensors\":[{\"desc\":\"PPG\",\"readings\":[{\"time\":34.56,\"value\":78.90}]}]}";
        JSONObject json2 = new JSONObject(json2Str);

        // Build up and insert a Session
        Session session = new Session();
        session.startTime = new Timestamp(1000);
        session.endTime = new Timestamp(2000);
        long sessionId = sessionDao.insert(session)[0];

        JsonToDb.InsertJson(json1, sessionId, db);
        JsonToDb.InsertJson(json2, sessionId, db);

        // verify that both readings exist and no duplicates are logged
        SessionWithDevices sessionWithDevices = sessionDao.getSessionsWithDevicesByIds(new long[]{sessionId}).get(0);
        Device device = sessionWithDevices.devices.get(0);
        assertEquals(DeviceDesc.FOSSIL_GEN_5, device.desc);
        assertEquals(1, sessionWithDevices.devices.size());

        SessionWithSensors sessionWithSensors = sessionDao.getSessionsWithSensorsByIds(new long[]{sessionId}).get(0);
        Sensor sensor = sessionWithSensors.sensors.get(0);
        assertEquals(SensorDesc.PPG, sensor.desc);
        assertEquals(1, sessionWithSensors.sensors.size());

        SessionWithReadings sessionWithReadings = sessionDao.getSessionsWithReadingsByIds(new long[]{sessionId}).get(0);

        Reading reading1 = sessionWithReadings.readings.get(0);
        assertEquals((long)12.34, reading1.time);
        assertEquals(56.78, reading1.value, 0.01f);

        Reading reading2 = sessionWithReadings.readings.get(1);
        assertEquals((long)34.56, reading2.time);
        assertEquals(78.90, reading2.value, 0.01f);

    }
}
