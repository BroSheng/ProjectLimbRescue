package com.example.projectlimbrescue.db;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.projectlimbrescue.db.device.Device;
import com.example.projectlimbrescue.db.device.DeviceContainsSensor;
import com.example.projectlimbrescue.db.device.DeviceContainsSensorDao;
import com.example.projectlimbrescue.db.device.DeviceDao;
import com.example.shared.DeviceDesc;
import com.example.projectlimbrescue.db.reading.Reading;
import com.example.projectlimbrescue.db.reading.ReadingDao;
import com.example.shared.ReadingLimb;
import com.example.projectlimbrescue.db.sensor.Sensor;
import com.example.projectlimbrescue.db.sensor.SensorDao;
import com.example.shared.SensorDesc;
import com.example.projectlimbrescue.db.sensor.SensorWithDevices;
import com.example.projectlimbrescue.db.sensor.SensorWithReadings;
import com.example.projectlimbrescue.db.sensor.SensorWithSessions;
import com.example.projectlimbrescue.db.session.Session;
import com.example.projectlimbrescue.db.session.SessionDao;
import com.example.projectlimbrescue.db.session.SessionMeasuresSensor;
import com.example.projectlimbrescue.db.session.SessionMeasuresSensorDao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.Assert.assertEquals;

/*
Test class for SensorDao.
 */

@RunWith(AndroidJUnit4.class)
public class SensorDaoTest {
    private DeviceDao deviceDao;
    private DeviceContainsSensorDao deviceContainsSensorDao;
    private ReadingDao readingDao;
    private SensorDao sensorDao;
    private SessionDao sessionDao;
    private SessionMeasuresSensorDao sessionMeasuresSensorDao;
    private AppDatabase db;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        deviceDao = db.deviceDao();
        sensorDao = db.sensorDao();
        deviceContainsSensorDao = db.deviceContainsSensorDao();
        readingDao = db.readingDao();
        sessionDao = db.sessionDao();
        sessionMeasuresSensorDao = db.sessionMeasuresSensorDao();
    }

    @After
    public void closeDb() {
        db.close();
    }

    /*
    Standard tests -- insert, get by id/desc, insert and remove
     */

    @Test
    public void insertAndGetSensor() throws Exception {
        Sensor sensor = new Sensor();
        sensor.sensorId = 123;
        sensor.desc = SensorDesc.PPG;

        sensorDao.insert(sensor).get();
        List<Sensor> sensors = sensorDao.getSensors().get();

        DbTestUtils.assertSensorEquals(sensor, sensors.get(0));
    }

    @Test
    public void insertAndGetSensorById() throws Exception {
        Sensor sensor = new Sensor();
        sensor.sensorId = 123;
        sensor.desc = SensorDesc.PPG;

        sensorDao.insert(sensor).get();
        List<Sensor> sensors = sensorDao.getSensorsByIds(new long[]{123}).get();

        DbTestUtils.assertSensorEquals(sensor, sensors.get(0));
    }

    @Test
    public void insertAndGetSensorByDesc() throws Exception {
        Sensor sensor = new Sensor();
        sensor.sensorId = 123;
        sensor.desc = SensorDesc.PPG;

        sensorDao.insert(sensor).get();
        List<Sensor> sensors = sensorDao.getSensorsByDesc(SensorDesc.PPG).get();

        DbTestUtils.assertSensorEquals(sensor, sensors.get(0));
    }

    @Test
    public void insertAndGetMultipleSensorsById() throws Exception {
        Sensor sensor1 = new Sensor();
        sensor1.sensorId = 123;
        sensor1.desc = SensorDesc.PPG;

        Sensor sensor2 = new Sensor();
        sensor2.sensorId = 456;
        sensor2.desc = SensorDesc.PPG;

        sensorDao.insert(sensor1, sensor2).get();
        List<Sensor> sensors = sensorDao.getSensorsByIds(new long[]{123, 456}).get();

        DbTestUtils.assertSensorEquals(sensor1, sensors.get(0));
        DbTestUtils.assertSensorEquals(sensor2, sensors.get(1));
    }

    @Test
    public void insertAndDeleteSensor() throws Exception {
        Sensor sensor = new Sensor();
        sensor.sensorId = 456;
        sensor.desc = SensorDesc.PPG;

        sensorDao.insert(sensor).get();
        sensorDao.delete(sensor).get();
        List<Sensor> sensors = sensorDao.getSensors().get();

        assertEquals(sensors.size(), 0);
    }

    /*
    SensorWithDevices tests -- insert, get by id, insert multiple, and insert with multiple devices
     */

    @Test
    public void insertAndGetSensorWithDevices() throws Exception {
        Sensor sensor = new Sensor();
        sensor.sensorId = 123;
        sensor.desc = SensorDesc.PPG;

        Device device = new Device();
        device.deviceId = 123;
        device.desc = DeviceDesc.FOSSIL_GEN_5;

        sensorDao.insert(sensor).get();
        deviceDao.insert(device).get();

        DeviceContainsSensor deviceContainsSensor = new DeviceContainsSensor();
        deviceContainsSensor.deviceId = device.deviceId;
        deviceContainsSensor.sensorId = sensor.sensorId;
        deviceContainsSensorDao.insert(deviceContainsSensor).get();

        List<SensorWithDevices> sensorWithDevices = sensorDao.getSensorsWithDevices().get();
        Sensor foundSensor = sensorWithDevices.get(0).sensor;
        List<Device> foundDevices = sensorWithDevices.get(0).devices;

        DbTestUtils.assertSensorEquals(sensor, foundSensor);
        DbTestUtils.assertDeviceEquals(device, foundDevices.get(0));
    }

    @Test
    public void insertAndGetSensorWithDevicesById() throws Exception {
        Sensor sensor = new Sensor();
        sensor.sensorId = 123;
        sensor.desc = SensorDesc.PPG;

        Device device = new Device();
        device.deviceId = 123;
        device.desc = DeviceDesc.FOSSIL_GEN_5;

        sensorDao.insert(sensor).get();
        deviceDao.insert(device).get();

        DeviceContainsSensor deviceContainsSensor = new DeviceContainsSensor();
        deviceContainsSensor.deviceId = device.deviceId;
        deviceContainsSensor.sensorId = sensor.sensorId;
        deviceContainsSensorDao.insert(deviceContainsSensor).get();

        List<SensorWithDevices> sensorWithDevices = sensorDao.getSensorsWithDevicesByIds(new long[]{123}).get();
        Sensor foundSensor = sensorWithDevices.get(0).sensor;
        List<Device> foundDevices = sensorWithDevices.get(0).devices;

        DbTestUtils.assertSensorEquals(sensor, foundSensor);
        DbTestUtils.assertDeviceEquals(device, foundDevices.get(0));
    }

    @Test
    public void insertAndGetMultipleSensorsWithDevicesById() throws Exception {
        Sensor sensor1 = new Sensor();
        sensor1.sensorId = 123;
        sensor1.desc = SensorDesc.PPG;

        Sensor sensor2 = new Sensor();
        sensor2.sensorId = 456;
        sensor2.desc = SensorDesc.PPG;

        Device device = new Device();
        device.deviceId = 123;
        device.desc = DeviceDesc.FOSSIL_GEN_5;

        sensorDao.insert(sensor1, sensor2).get();
        deviceDao.insert(device).get();

        DeviceContainsSensor deviceContainsSensor1 = new DeviceContainsSensor();
        deviceContainsSensor1.deviceId = device.deviceId;
        deviceContainsSensor1.sensorId = sensor1.sensorId;

        DeviceContainsSensor deviceContainsSensor2 = new DeviceContainsSensor();
        deviceContainsSensor2.deviceId = device.deviceId;
        deviceContainsSensor2.sensorId = sensor2.sensorId;
        deviceContainsSensorDao.insert(deviceContainsSensor1, deviceContainsSensor2).get();

        List<SensorWithDevices> sensorsWithDevices = sensorDao.getSensorsWithDevicesByIds(new long[]{123, 456}).get();
        Sensor foundSensor1 = sensorsWithDevices.get(0).sensor;
        Sensor foundSensor2 = sensorsWithDevices.get(1).sensor;
        List<Device> foundDevices1 = sensorsWithDevices.get(0).devices;
        List<Device> foundDevices2 = sensorsWithDevices.get(1).devices;


        DbTestUtils.assertSensorEquals(sensor1, foundSensor1);
        DbTestUtils.assertSensorEquals(sensor2, foundSensor2);
        DbTestUtils.assertDeviceEquals(device, foundDevices1.get(0));
        DbTestUtils.assertDeviceEquals(device, foundDevices2.get(0));
    }

    @Test
    public void insertAndGetSensorWithMultipleDevicesById() throws Exception {
        Sensor sensor = new Sensor();
        sensor.sensorId = 123;
        sensor.desc = SensorDesc.PPG;

        Device device1 = new Device();
        device1.deviceId = 123;
        device1.desc = DeviceDesc.FOSSIL_GEN_5;

        Device device2 = new Device();
        device2.deviceId = 456;
        device2.desc = DeviceDesc.FOSSIL_GEN_5;

        sensorDao.insert(sensor).get();
        deviceDao.insert(device1, device2).get();

        DeviceContainsSensor device1ContainsSensor = new DeviceContainsSensor();
        device1ContainsSensor.deviceId = device1.deviceId;
        device1ContainsSensor.sensorId = sensor.sensorId;

        DeviceContainsSensor device2ContainsSensor = new DeviceContainsSensor();
        device2ContainsSensor.deviceId = device2.deviceId;
        device2ContainsSensor.sensorId = sensor.sensorId;
        deviceContainsSensorDao.insert(device1ContainsSensor, device2ContainsSensor).get();

        List<SensorWithDevices> sensorWithDevices = sensorDao.getSensorsWithDevicesByIds(new long[]{123, 456}).get();
        Sensor foundSensor = sensorWithDevices.get(0).sensor;
        List<Device> foundDevices = sensorWithDevices.get(0).devices;


        DbTestUtils.assertSensorEquals(sensor, foundSensor);
        DbTestUtils.assertDeviceEquals(device1, foundDevices.get(0));
        DbTestUtils.assertDeviceEquals(device2, foundDevices.get(1));
    }

    /*
    SensorWithReadings tests -- insert, get by id, insert multiple, insert with multiple readings
     */

    @Test
    public void insertAndGetSensorWithReadings() throws Exception {
        Sensor sensor = new Sensor();
        sensor.sensorId = 123;
        sensor.desc = SensorDesc.PPG;

        // create valid entities for reading foreign keys
        Device device = new Device();
        device.deviceId = 123;
        device.desc = DeviceDesc.FOSSIL_GEN_5;
        deviceDao.insert(device).get();

        Session session = new Session();
        session.sessionId = 012;
        session.startTime = new Timestamp(1000);
        session.endTime = new Timestamp(2000);
        sessionDao.insert(session).get();

        // insert the reading itself
        Reading reading = new Reading();
        reading.deviceId = 123;
        reading.readingId = 456;
        reading.sensorId = sensor.sensorId;
        reading.sessionId = 012;
        reading.time = 1000;
        reading.value = 123.456f;
        reading.limb = ReadingLimb.LEFT_ARM;

        sensorDao.insert(sensor).get();
        readingDao.insert(reading).get();

        // test validity
        List<SensorWithReadings> sensorsWithReadings = sensorDao.getSensorsWithReadings().get();
        Sensor foundSensor = sensorsWithReadings.get(0).sensor;
        List<Reading> foundReadings = sensorsWithReadings.get(0).readings;

        DbTestUtils.assertSensorEquals(sensor, foundSensor);
        DbTestUtils.assertReadingEquals(reading, foundReadings.get(0));
    }

    @Test
    public void insertAndGetSensorWithReadingsById() throws Exception {
        Sensor sensor = new Sensor();
        sensor.sensorId = 123;
        sensor.desc = SensorDesc.PPG;

        // create valid entities for reading foreign keys
        Device device = new Device();
        device.deviceId = 123;
        device.desc = DeviceDesc.FOSSIL_GEN_5;
        deviceDao.insert(device).get();

        Session session = new Session();
        session.sessionId = 012;
        session.startTime = new Timestamp(1000);
        session.endTime = new Timestamp(2000);
        sessionDao.insert(session).get();

        // insert the reading itself
        Reading reading = new Reading();
        reading.deviceId = 123;
        reading.readingId = 456;
        reading.sensorId = sensor.sensorId;
        reading.sessionId = 012;
        reading.time = 1000;
        reading.value = 123.456f;
        reading.limb = ReadingLimb.LEFT_ARM;

        sensorDao.insert(sensor).get();
        readingDao.insert(reading).get();

        // test validity
        List<SensorWithReadings> sensorsWithReadings = sensorDao.getSensorsWithReadingsByIds(new long[]{123}).get();
        Sensor foundSensor = sensorsWithReadings.get(0).sensor;
        List<Reading> foundReadings = sensorsWithReadings.get(0).readings;

        DbTestUtils.assertSensorEquals(sensor, foundSensor);
        DbTestUtils.assertReadingEquals(reading, foundReadings.get(0));
    }

    @Test
    public void insertAndGetMultipleSensorsWithReadingsById() throws Exception {
        Sensor sensor1 = new Sensor();
        sensor1.sensorId = 123;
        sensor1.desc = SensorDesc.PPG;

        Sensor sensor2 = new Sensor();
        sensor2.sensorId = 456;
        sensor2.desc = SensorDesc.PPG;

        // create valid entities for reading foreign keys
        Device device = new Device();
        device.deviceId = 123;
        device.desc = DeviceDesc.FOSSIL_GEN_5;
        deviceDao.insert(device).get();

        Session session = new Session();
        session.sessionId = 012;
        session.startTime = new Timestamp(1000);
        session.endTime = new Timestamp(2000);
        sessionDao.insert(session).get();

        // insert the reading itself
        Reading reading1 = new Reading();
        reading1.deviceId = 123;
        reading1.readingId = 123;
        reading1.sensorId = sensor1.sensorId;
        reading1.sessionId = 012;
        reading1.time = 1000;
        reading1.value = 123.456f;
        reading1.limb = ReadingLimb.LEFT_ARM;

        Reading reading2 = new Reading();
        reading2.deviceId = 123;
        reading2.readingId = 456;
        reading2.sensorId = sensor2.sensorId;
        reading2.sessionId = 012;
        reading2.time = 1000;
        reading2.value = 123.456f;
        reading2.limb = ReadingLimb.LEFT_ARM;

        sensorDao.insert(sensor1, sensor2).get();
        readingDao.insert(reading1, reading2).get();

        // test validity
        List<SensorWithReadings> sensorsWithReadings = sensorDao.getSensorsWithReadingsByIds(new long[]{123, 456}).get();
        Sensor foundSensor1 = sensorsWithReadings.get(0).sensor;
        Sensor foundSensor2 = sensorsWithReadings.get(1).sensor;
        List<Reading> foundReadings1 = sensorsWithReadings.get(0).readings;
        List<Reading> foundReadings2 = sensorsWithReadings.get(1).readings;

        DbTestUtils.assertSensorEquals(sensor1, foundSensor1);
        DbTestUtils.assertSensorEquals(sensor2, foundSensor2);
        DbTestUtils.assertReadingEquals(reading1, foundReadings1.get(0));
        DbTestUtils.assertReadingEquals(reading2, foundReadings2.get(0));
    }

    @Test
    public void insertAndGetSensorWithMultipleReadingsById() throws Exception {
        Sensor sensor = new Sensor();
        sensor.sensorId = 123;
        sensor.desc = SensorDesc.PPG;

        // create valid entities for reading foreign keys
        Device device = new Device();
        device.deviceId = 123;
        device.desc = DeviceDesc.FOSSIL_GEN_5;
        deviceDao.insert(device).get();

        Session session = new Session();
        session.sessionId = 012;
        session.startTime = new Timestamp(1000);
        session.endTime = new Timestamp(2000);
        sessionDao.insert(session).get();

        // insert the reading itself
        Reading reading1 = new Reading();
        reading1.deviceId = 123;
        reading1.readingId = 123;
        reading1.sensorId = sensor.sensorId;
        reading1.sessionId = 012;
        reading1.time = 1000;
        reading1.value = 123.456f;
        reading1.limb = ReadingLimb.LEFT_ARM;

        Reading reading2 = new Reading();
        reading2.deviceId = 123;
        reading2.readingId = 456;
        reading2.sensorId = sensor.sensorId;
        reading2.sessionId = 012;
        reading2.time = 1000;
        reading2.value = 123.456f;
        reading2.limb = ReadingLimb.LEFT_ARM;

        sensorDao.insert(sensor).get();
        readingDao.insert(reading1, reading2).get();

        // test validity
        List<SensorWithReadings> sensorsWithReadings = sensorDao.getSensorsWithReadingsByIds(new long[]{123}).get();
        Sensor foundSensor = sensorsWithReadings.get(0).sensor;
        List<Reading> foundReadings = sensorsWithReadings.get(0).readings;

        DbTestUtils.assertSensorEquals(sensor, foundSensor);
        DbTestUtils.assertReadingEquals(reading1, foundReadings.get(0));
        DbTestUtils.assertReadingEquals(reading2, foundReadings.get(1));
    }

    /*
    SensorWithSessions tests -- insert, get by id, insert multiple, insert with multiple sessions
     */

    @Test
    public void insertAndGetSensorWithSessions() throws Exception {
        Sensor sensor = new Sensor();
        sensor.sensorId = 123;
        sensor.desc = SensorDesc.PPG;

        Session session = new Session();
        session.sessionId = 123;
        session.startTime = new Timestamp(1000);
        session.endTime = new Timestamp(2000);

        sensorDao.insert(sensor).get();
        sessionDao.insert(session).get();

        SessionMeasuresSensor sessionMeasuresSensor = new SessionMeasuresSensor();
        sessionMeasuresSensor.sessionId = session.sessionId;
        sessionMeasuresSensor.sensorId = sensor.sensorId;
        sessionMeasuresSensorDao.insert(sessionMeasuresSensor).get();

        List<SensorWithSessions> sensorsWithSessions = sensorDao.getSensorWithSessions().get();
        Sensor foundSensor = sensorsWithSessions.get(0).sensor;
        List<Session> foundSessions = sensorsWithSessions.get(0).sessions;

        DbTestUtils.assertSensorEquals(sensor, foundSensor);
        DbTestUtils.assertSessionEquals(session, foundSessions.get(0));
    }

    @Test
    public void insertAndGetSensorWithSessionsById() throws Exception {
        Sensor sensor = new Sensor();
        sensor.sensorId = 123;
        sensor.desc = SensorDesc.PPG;

        Session session = new Session();
        session.sessionId = 123;
        session.startTime = new Timestamp(1000);
        session.endTime = new Timestamp(2000);

        sensorDao.insert(sensor).get();
        sessionDao.insert(session).get();

        SessionMeasuresSensor sessionMeasuresSensor = new SessionMeasuresSensor();
        sessionMeasuresSensor.sessionId = session.sessionId;
        sessionMeasuresSensor.sensorId = sensor.sensorId;
        sessionMeasuresSensorDao.insert(sessionMeasuresSensor).get();

        List<SensorWithSessions> sensorsWithSessions = sensorDao.getSensorsWithSessionsByIds(new long[]{123}).get();
        Sensor foundSensor = sensorsWithSessions.get(0).sensor;
        List<Session> foundSessions = sensorsWithSessions.get(0).sessions;

        DbTestUtils.assertSensorEquals(sensor, foundSensor);
        DbTestUtils.assertSessionEquals(session, foundSessions.get(0));
    }

    @Test
    public void insertAndGetMultipleSensorsWithSessionsById() throws Exception {
        Sensor sensor1 = new Sensor();
        sensor1.sensorId = 123;
        sensor1.desc = SensorDesc.PPG;

        Sensor sensor2 = new Sensor();
        sensor2.sensorId = 456;
        sensor2.desc = SensorDesc.PPG;

        Session session = new Session();
        session.sessionId = 123;
        session.startTime = new Timestamp(1000);
        session.endTime = new Timestamp(2000);

        sensorDao.insert(sensor1, sensor2).get();
        sessionDao.insert(session).get();

        SessionMeasuresSensor sessionMeasuresSensor1 = new SessionMeasuresSensor();
        sessionMeasuresSensor1.sessionId = session.sessionId;
        sessionMeasuresSensor1.sensorId = sensor1.sensorId;

        SessionMeasuresSensor sessionMeasuresSensor2 = new SessionMeasuresSensor();
        sessionMeasuresSensor2.sessionId = session.sessionId;
        sessionMeasuresSensor2.sensorId = sensor2.sensorId;
        sessionMeasuresSensorDao.insert(sessionMeasuresSensor1, sessionMeasuresSensor2).get();

        List<SensorWithSessions> sensorsWithSessions = sensorDao.getSensorsWithSessionsByIds(new long[]{123, 456}).get();
        Sensor foundSensor1 = sensorsWithSessions.get(0).sensor;
        Sensor foundSensor2 = sensorsWithSessions.get(1).sensor;
        List<Session> foundSessions1 = sensorsWithSessions.get(0).sessions;
        List<Session> foundSessions2 = sensorsWithSessions.get(1).sessions;


        DbTestUtils.assertSensorEquals(sensor1, foundSensor1);
        DbTestUtils.assertSensorEquals(sensor2, foundSensor2);
        DbTestUtils.assertSessionEquals(session, foundSessions1.get(0));
        DbTestUtils.assertSessionEquals(session, foundSessions2.get(0));
    }

    @Test
    public void insertAndGetSensorWithMultipleSessionsById() throws Exception {
        Sensor sensor = new Sensor();
        sensor.sensorId = 123;
        sensor.desc = SensorDesc.PPG;

        Session session1 = new Session();
        session1.sessionId = 123;
        session1.startTime = new Timestamp(1000);
        session1.endTime = new Timestamp(2000);

        Session session2 = new Session();
        session2.sessionId = 456;
        session2.startTime = new Timestamp(1000);
        session2.endTime = new Timestamp(2000);

        sensorDao.insert(sensor).get();
        sessionDao.insert(session1, session2).get();

        SessionMeasuresSensor session1MeasuresSensor = new SessionMeasuresSensor();
        session1MeasuresSensor.sessionId = session1.sessionId;
        session1MeasuresSensor.sensorId = sensor.sensorId;

        SessionMeasuresSensor session2MeasuresSensor = new SessionMeasuresSensor();
        session2MeasuresSensor.sessionId = session2.sessionId;
        session2MeasuresSensor.sensorId = sensor.sensorId;
        sessionMeasuresSensorDao.insert(session1MeasuresSensor, session2MeasuresSensor).get();

        List<SensorWithSessions> sensorsWithSessions = sensorDao.getSensorsWithSessionsByIds(new long[]{123}).get();
        Sensor foundSensor = sensorsWithSessions.get(0).sensor;
        List<Session> foundSessions = sensorsWithSessions.get(0).sessions;

        DbTestUtils.assertSensorEquals(sensor, foundSensor);
        DbTestUtils.assertSessionEquals(session1, foundSessions.get(0));
        DbTestUtils.assertSessionEquals(session2, foundSessions.get(1));
    }
}
