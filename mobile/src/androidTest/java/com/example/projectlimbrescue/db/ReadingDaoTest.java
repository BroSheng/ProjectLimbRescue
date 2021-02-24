package com.example.projectlimbrescue.db;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.Assert.assertEquals;

/*
Test class for ReadingDao.
 */

@RunWith(AndroidJUnit4.class)
public class ReadingDaoTest {
    private ReadingDao readingDao;
    private DeviceDao deviceDao;
    private SensorDao sensorDao;
    private SessionDao sessionDao;
    private AppDatabase db;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        readingDao = db.readingDao();
        deviceDao = db.deviceDao();
        sensorDao = db.sensorDao();
        sessionDao = db.sessionDao();
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void insertAndGetReading() throws Exception {
        // create and insert valid entities for foreign keys
        Session session = new Session();
        session.sessionId = 456;
        session.startTime = new Timestamp(1000);
        session.endTime = new Timestamp(2000);
        sessionDao.insert(session);

        Device device = new Device();
        device.deviceId = 789;
        device.desc = DeviceDesc.FOSSIL_GEN_5;
        deviceDao.insert(device);

        Sensor sensor = new Sensor();
        sensor.sensorId = 012;
        sensor.desc = SensorDesc.PPG;
        sensorDao.insert(sensor);

        // insert the reading itself
        Reading reading = new Reading();
        reading.readingId = 123;
        reading.sessionId = 456;
        reading.deviceId = 789;
        reading.sensorId = 012;
        reading.timestamp = new Timestamp(1000);
        reading.value = 123.456f;
        reading.limb = ReadingLimb.LEFT_ARM;

        readingDao.insert(reading);
        List<Reading> readings = readingDao.getReadings();

        DbTestUtils.assertReadingEquals(reading, readings.get(0));
    }

    @Test
    public void insertAndGetReadingById() throws Exception {
        // create and insert valid entities for foreign keys
        Session session = new Session();
        session.sessionId = 456;
        session.startTime = new Timestamp(1000);
        session.endTime = new Timestamp(2000);
        sessionDao.insert(session);

        Device device = new Device();
        device.deviceId = 789;
        device.desc = DeviceDesc.FOSSIL_GEN_5;
        deviceDao.insert(device);

        Sensor sensor = new Sensor();
        sensor.sensorId = 012;
        sensor.desc = SensorDesc.PPG;
        sensorDao.insert(sensor);

        // insert the reading itself
        Reading reading = new Reading();
        reading.readingId = 123;
        reading.sessionId = 456;
        reading.deviceId = 789;
        reading.sensorId = 012;
        reading.timestamp = new Timestamp(1000);
        reading.value = 123.456f;
        reading.limb = ReadingLimb.LEFT_ARM;

        readingDao.insert(reading);
        List<Reading> readings = readingDao.getReadingsByIds(new int[]{123});

        DbTestUtils.assertReadingEquals(reading, readings.get(0));
    }

    @Test
    public void insertAndGetMultipleReadingsById() throws Exception {
        // create and insert valid entities for foreign keys
        Session session = new Session();
        session.sessionId = 456;
        session.startTime = new Timestamp(1000);
        session.endTime = new Timestamp(2000);
        sessionDao.insert(session);

        Device device = new Device();
        device.deviceId = 789;
        device.desc = DeviceDesc.FOSSIL_GEN_5;
        deviceDao.insert(device);

        Sensor sensor = new Sensor();
        sensor.sensorId = 012;
        sensor.desc = SensorDesc.PPG;
        sensorDao.insert(sensor);

        // insert the reading itself
        Reading reading1 = new Reading();
        reading1.readingId = 123;
        reading1.sessionId = 456;
        reading1.deviceId = 789;
        reading1.sensorId = 012;
        reading1.timestamp = new Timestamp(1000);
        reading1.value = 123.456f;
        reading1.limb = ReadingLimb.LEFT_ARM;

        Reading reading2 = new Reading();
        reading2.readingId = 234;
        reading2.sessionId = 456;
        reading2.deviceId = 789;
        reading2.sensorId = 012;
        reading2.timestamp = new Timestamp(2000);
        reading2.value = 789.012f;
        reading2.limb = ReadingLimb.RIGHT_ARM;

        readingDao.insert(reading1);
        readingDao.insert(reading2);
        List<Reading> readings = readingDao.getReadingsByIds(new int[]{123, 234});

        DbTestUtils.assertReadingEquals(reading1, readings.get(0));
        DbTestUtils.assertReadingEquals(reading2, readings.get(1));
    }

    @Test
    public void insertAndDeleteReading() throws Exception {
        // create and insert valid entities for foreign keys
        Session session = new Session();
        session.sessionId = 456;
        session.startTime = new Timestamp(1000);
        session.endTime = new Timestamp(2000);
        sessionDao.insert(session);

        Device device = new Device();
        device.deviceId = 789;
        device.desc = DeviceDesc.FOSSIL_GEN_5;
        deviceDao.insert(device);

        Sensor sensor = new Sensor();
        sensor.sensorId = 012;
        sensor.desc = SensorDesc.PPG;
        sensorDao.insert(sensor);

        // insert the reading itself
        Reading reading = new Reading();
        reading.readingId = 123;
        reading.sessionId = 456;
        reading.deviceId = 789;
        reading.sensorId = 012;
        reading.timestamp = new Timestamp(1000);
        reading.value = 123.456f;
        reading.limb = ReadingLimb.LEFT_ARM;

        readingDao.insert(reading);
        readingDao.delete(reading);

        List<Reading> readings = readingDao.getReadings();

        assertEquals(readings.size(), 0);
    }
}
