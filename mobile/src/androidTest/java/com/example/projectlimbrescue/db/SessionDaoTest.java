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
import com.example.projectlimbrescue.db.session.SessionMeasuresSensor;
import com.example.projectlimbrescue.db.session.SessionMeasuresSensorDao;
import com.example.projectlimbrescue.db.session.SessionReadsFromDevice;
import com.example.projectlimbrescue.db.session.SessionReadsFromDeviceDao;
import com.example.projectlimbrescue.db.session.SessionWithDevices;
import com.example.projectlimbrescue.db.session.SessionWithReadings;
import com.example.projectlimbrescue.db.session.SessionWithSensors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.Assert.assertEquals;

/*
Test class for SessionDao.
 */

@RunWith(AndroidJUnit4.class)
public class SessionDaoTest {
    private SessionDao sessionDao;
    private SessionMeasuresSensorDao sessionMeasuresSensorDao;
    private SessionReadsFromDeviceDao sessionReadsFromDeviceDao;
    private DeviceDao deviceDao;
    private ReadingDao readingDao;
    private SensorDao sensorDao;
    private AppDatabase db;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        sessionDao = db.sessionDao();
        sessionMeasuresSensorDao = db.sessionMeasuresSensorDao();
        sessionReadsFromDeviceDao = db.sessionReadsFromDeviceDao();
        deviceDao = db.deviceDao();
        readingDao = db.readingDao();
        sensorDao = db.sensorDao();
    }

    @After
    public void closeDb() {
        db.close();
    }

    /*
    Standard tests -- insert, get by id, insert and remove
     */

    @Test
    public void insertAndGetSession() throws Exception {
        Session session = new Session();
        session.sessionId = 123;
        session.startTime = new Timestamp(1000);
        session.endTime = new Timestamp(2000);

        sessionDao.insert(session);
        List<Session> sessions = sessionDao.getSessions();

        DbTestUtils.assertSessionEquals(session, sessions.get(0));
    }

    @Test
    public void insertAndGetSessionById() throws Exception {
        Session session = new Session();
        session.sessionId = 123;
        session.startTime = new Timestamp(1000);
        session.endTime = new Timestamp(2000);

        sessionDao.insert(session);
        List<Session> sessions = sessionDao.getSessionsByIds(new long[]{123});

        DbTestUtils.assertSessionEquals(session, sessions.get(0));
    }

    @Test
    public void insertAndGetMultipleSessionsById() throws Exception {
        Session session1 = new Session();
        session1.sessionId = 123;
        session1.startTime = new Timestamp(1000);
        session1.endTime = new Timestamp(2000);

        Session session2 = new Session();
        session2.sessionId = 456;
        session2.startTime = new Timestamp(1000);
        session2.endTime = new Timestamp(2000);

        sessionDao.insert(session1, session2);
        List<Session> sessions = sessionDao.getSessionsByIds(new long[]{123, 456});

        DbTestUtils.assertSessionEquals(session1, sessions.get(0));
        DbTestUtils.assertSessionEquals(session2, sessions.get(1));
    }

    @Test
    public void insertAndDeleteSession() throws Exception {
        Session session = new Session();
        session.sessionId = 123;
        session.startTime = new Timestamp(1000);
        session.endTime = new Timestamp(2000);

        sessionDao.insert(session);
        sessionDao.delete(session);

        List<Session> sessions = sessionDao.getSessions();

        assertEquals(sessions.size(), 0);
    }

    /*
    SessionWithDevices tests -- insert, get by id, insert multiple, and insert with multiple devices
     */

    @Test
    public void insertAndGetSessionWithDevices() throws Exception {
        Session session = new Session();
        session.sessionId = 123;
        session.startTime = new Timestamp(1000);
        session.endTime = new Timestamp(2000);

        Device device = new Device();
        device.deviceId = 123;
        device.desc = DeviceDesc.FOSSIL_GEN_5;

        sessionDao.insert(session);
        deviceDao.insert(device);

        SessionReadsFromDevice sessionReadsFromDevice = new SessionReadsFromDevice();
        sessionReadsFromDevice.sessionId = session.sessionId;
        sessionReadsFromDevice.deviceId = device.deviceId;
        sessionReadsFromDeviceDao.insert(sessionReadsFromDevice);

        List<SessionWithDevices> sessionsWithDevices = sessionDao.getSessionsWithDevices();
        Session foundSession = sessionsWithDevices.get(0).session;
        List<Device> foundDevices = sessionsWithDevices.get(0).devices;

        DbTestUtils.assertSessionEquals(session, foundSession);
        DbTestUtils.assertDeviceEquals(device, foundDevices.get(0));
    }

    @Test
    public void insertAndGetSessionWithDevicesById() throws Exception {
        Session session = new Session();
        session.sessionId = 123;
        session.startTime = new Timestamp(1000);
        session.endTime = new Timestamp(2000);

        Device device = new Device();
        device.deviceId = 123;
        device.desc = DeviceDesc.FOSSIL_GEN_5;

        sessionDao.insert(session);
        deviceDao.insert(device);

        SessionReadsFromDevice sessionReadsFromDevice = new SessionReadsFromDevice();
        sessionReadsFromDevice.sessionId = session.sessionId;
        sessionReadsFromDevice.deviceId = device.deviceId;
        sessionReadsFromDeviceDao.insert(sessionReadsFromDevice);

        List<SessionWithDevices> sessionsWithDevices = sessionDao.getSessionsWithDevicesByIds(new long[]{123});
        Session foundSession = sessionsWithDevices.get(0).session;
        List<Device> foundDevices = sessionsWithDevices.get(0).devices;

        DbTestUtils.assertSessionEquals(session, foundSession);
        DbTestUtils.assertDeviceEquals(device, foundDevices.get(0));
    }

    @Test
    public void insertAndGetMultipleSessionsWithDevicesById() throws Exception {
        Session session1 = new Session();
        session1.sessionId = 123;
        session1.startTime = new Timestamp(1000);
        session1.endTime = new Timestamp(2000);

        Session session2 = new Session();
        session2.sessionId = 456;
        session2.startTime = new Timestamp(1000);
        session2.endTime = new Timestamp(2000);

        Device device = new Device();
        device.deviceId = 123;
        device.desc = DeviceDesc.FOSSIL_GEN_5;

        sessionDao.insert(session1, session2);
        deviceDao.insert(device);

        SessionReadsFromDevice session1ReadsFromDevice = new SessionReadsFromDevice();
        session1ReadsFromDevice.sessionId = session1.sessionId;
        session1ReadsFromDevice.deviceId = device.deviceId;

        SessionReadsFromDevice session2ReadsFromDevice = new SessionReadsFromDevice();
        session2ReadsFromDevice.sessionId = session2.sessionId;
        session2ReadsFromDevice.deviceId = device.deviceId;
        sessionReadsFromDeviceDao.insert(session1ReadsFromDevice, session2ReadsFromDevice);

        List<SessionWithDevices> sessionsWithDevices = sessionDao.getSessionsWithDevicesByIds(new long[]{123, 456});
        Session foundSession1 = sessionsWithDevices.get(0).session;
        Session foundSession2 = sessionsWithDevices.get(1).session;
        List<Device> foundDevices1 = sessionsWithDevices.get(0).devices;
        List<Device> foundDevices2 = sessionsWithDevices.get(1).devices;

        DbTestUtils.assertSessionEquals(session1, foundSession1);
        DbTestUtils.assertSessionEquals(session2, foundSession2);
        DbTestUtils.assertDeviceEquals(device, foundDevices1.get(0));
        DbTestUtils.assertDeviceEquals(device, foundDevices2.get(0));
    }

    @Test
    public void insertAndGetSessionWithMultipleDevicesById() throws Exception {
        Session session = new Session();
        session.sessionId = 123;
        session.startTime = new Timestamp(1000);
        session.endTime = new Timestamp(2000);

        Device device1 = new Device();
        device1.deviceId = 123;
        device1.desc = DeviceDesc.FOSSIL_GEN_5;

        Device device2 = new Device();
        device2.deviceId = 456;
        device2.desc = DeviceDesc.FOSSIL_GEN_5;

        sessionDao.insert(session);
        deviceDao.insert(device1, device2);

        SessionReadsFromDevice sessionReadsFromDevice1 = new SessionReadsFromDevice();
        sessionReadsFromDevice1.sessionId = session.sessionId;
        sessionReadsFromDevice1.deviceId = device1.deviceId;

        SessionReadsFromDevice sessionReadsFromDevice2 = new SessionReadsFromDevice();
        sessionReadsFromDevice2.sessionId = session.sessionId;
        sessionReadsFromDevice2.deviceId = device2.deviceId;
        sessionReadsFromDeviceDao.insert(sessionReadsFromDevice1, sessionReadsFromDevice2);

        List<SessionWithDevices> sessionsWithDevices = sessionDao.getSessionsWithDevicesByIds(new long[]{123, 456});
        Session foundSession = sessionsWithDevices.get(0).session;
        List<Device> foundDevices = sessionsWithDevices.get(0).devices;


        DbTestUtils.assertSessionEquals(session, foundSession);
        DbTestUtils.assertDeviceEquals(device1, foundDevices.get(0));
        DbTestUtils.assertDeviceEquals(device2, foundDevices.get(1));
    }

        /*
    SessionWithReadings tests -- insert, get by id, insert multiple, and insert with multiple readings
     */

    @Test
    public void insertAndGetSessionWithReadings() throws Exception {
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

        // insert the reading itself
        Reading reading = new Reading();
        reading.deviceId = 123;
        reading.readingId = 456;
        reading.sensorId = 123;
        reading.sessionId = session.sessionId;
        reading.time = 1000;
        reading.value = 123.456f;
        reading.limb = ReadingLimb.LEFT_ARM;

        sessionDao.insert(session);
        readingDao.insert(reading);

        List<SessionWithReadings> sessionsWithReadings = sessionDao.getSessionsWithReadings();
        Session foundSession = sessionsWithReadings.get(0).session;
        List<Reading> foundReadings = sessionsWithReadings.get(0).readings;

        DbTestUtils.assertSessionEquals(session, foundSession);
        DbTestUtils.assertReadingEquals(reading, foundReadings.get(0));
    }

    @Test
    public void insertAndGetSessionWithReadingsById() throws Exception {
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

        // insert the reading itself
        Reading reading = new Reading();
        reading.deviceId = 123;
        reading.readingId = 456;
        reading.sensorId = 123;
        reading.sessionId = session.sessionId;
        reading.time = 1000;
        reading.value = 123.456f;
        reading.limb = ReadingLimb.LEFT_ARM;

        sessionDao.insert(session);
        readingDao.insert(reading);

        List<SessionWithReadings> sessionsWithReadings = sessionDao.getSessionsWithReadingsByIds(new long[]{123});
        Session foundSession = sessionsWithReadings.get(0).session;
        List<Reading> foundReadings = sessionsWithReadings.get(0).readings;

        DbTestUtils.assertSessionEquals(session, foundSession);
        DbTestUtils.assertReadingEquals(reading, foundReadings.get(0));
    }

    @Test
    public void insertAndGetMultipleSessionsWithReadingsById() throws Exception {
        Session session1 = new Session();
        session1.sessionId = 123;
        session1.startTime = new Timestamp(1000);
        session1.endTime = new Timestamp(2000);

        Session session2 = new Session();
        session2.sessionId = 456;
        session2.startTime = new Timestamp(1000);
        session2.endTime = new Timestamp(2000);

        // create valid entities for reading foreign keys
        Device device = new Device();
        device.deviceId = 123;
        device.desc = DeviceDesc.FOSSIL_GEN_5;
        deviceDao.insert(device);

        Sensor sensor = new Sensor();
        sensor.sensorId = 123;
        sensor.desc = SensorDesc.PPG;
        sensorDao.insert(sensor);

        // insert the readings themselves
        Reading reading1 = new Reading();
        reading1.deviceId = 123;
        reading1.readingId = 123;
        reading1.sensorId = 123;
        reading1.sessionId = session1.sessionId;
        reading1.time = 1000;
        reading1.value = 123.456f;
        reading1.limb = ReadingLimb.LEFT_ARM;

        Reading reading2 = new Reading();
        reading2.deviceId = 123;
        reading2.readingId = 456;
        reading2.sensorId = 123;
        reading2.sessionId = session2.sessionId;
        reading2.time = 1000;
        reading2.value = 123.456f;
        reading2.limb = ReadingLimb.LEFT_ARM;

        sessionDao.insert(session1, session2);
        readingDao.insert(reading1, reading2);

        List<SessionWithReadings> sessionsWithReadings = sessionDao.getSessionsWithReadingsByIds(new long[]{123, 456});
        Session foundSession1 = sessionsWithReadings.get(0).session;
        Session foundSession2 = sessionsWithReadings.get(1).session;
        List<Reading> foundReadings1 = sessionsWithReadings.get(0).readings;
        List<Reading> foundReadings2 = sessionsWithReadings.get(1).readings;

        DbTestUtils.assertSessionEquals(session1, foundSession1);
        DbTestUtils.assertSessionEquals(session2, foundSession2);
        DbTestUtils.assertReadingEquals(reading1, foundReadings1.get(0));
        DbTestUtils.assertReadingEquals(reading2, foundReadings2.get(0));
    }

    @Test
    public void insertAndGetSessionWithMultipleReadingsById() throws Exception {
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

        // insert the reading itself
        Reading reading1 = new Reading();
        reading1.deviceId = 123;
        reading1.readingId = 123;
        reading1.sensorId = 123;
        reading1.sessionId = session.sessionId;
        reading1.time = 1000;
        reading1.value = 123.456f;
        reading1.limb = ReadingLimb.LEFT_ARM;

        Reading reading2 = new Reading();
        reading2.deviceId = 123;
        reading2.readingId = 456;
        reading2.sensorId = 123;
        reading2.sessionId = session.sessionId;
        reading2.time = 1000;
        reading2.value = 123.456f;
        reading2.limb = ReadingLimb.LEFT_ARM;

        sessionDao.insert(session);
        readingDao.insert(reading1, reading2);

        List<SessionWithReadings> sessionsWithReadings = sessionDao.getSessionsWithReadingsByIds(new long[]{123});
        Session foundSession = sessionsWithReadings.get(0).session;
        List<Reading> foundReadings = sessionsWithReadings.get(0).readings;

        DbTestUtils.assertSessionEquals(session, foundSession);
        DbTestUtils.assertReadingEquals(reading1, foundReadings.get(0));
        DbTestUtils.assertReadingEquals(reading2, foundReadings.get(1));
    }

        /*
    SessionWithSensors tests -- insert, get by id, insert multiple, and insert with multiple sensors
     */

    @Test
    public void insertAndGetSessionWithSensors() throws Exception {
        Session session = new Session();
        session.sessionId = 123;
        session.startTime = new Timestamp(1000);
        session.endTime = new Timestamp(2000);

        Sensor sensor = new Sensor();
        sensor.sensorId = 123;
        sensor.desc = SensorDesc.PPG;

        sessionDao.insert(session);
        sensorDao.insert(sensor);

        SessionMeasuresSensor sessionMeasuresSensor = new SessionMeasuresSensor();
        sessionMeasuresSensor.sessionId = session.sessionId;
        sessionMeasuresSensor.sensorId = sensor.sensorId;
        sessionMeasuresSensorDao.insert(sessionMeasuresSensor);

        List<SessionWithSensors> sessionsWithSensors = sessionDao.getSessionsWithSensors();
        Session foundSession = sessionsWithSensors.get(0).session;
        List<Sensor> foundSensors = sessionsWithSensors.get(0).sensors;

        DbTestUtils.assertSessionEquals(session, foundSession);
        DbTestUtils.assertSensorEquals(sensor, foundSensors.get(0));
    }

    @Test
    public void insertAndGetSessionWithSensorsById() throws Exception {
        Session session = new Session();
        session.sessionId = 123;
        session.startTime = new Timestamp(1000);
        session.endTime = new Timestamp(2000);

        Sensor sensor = new Sensor();
        sensor.sensorId = 123;
        sensor.desc = SensorDesc.PPG;

        sessionDao.insert(session);
        sensorDao.insert(sensor);

        SessionMeasuresSensor sessionMeasuresSensor = new SessionMeasuresSensor();
        sessionMeasuresSensor.sessionId = session.sessionId;
        sessionMeasuresSensor.sensorId = sensor.sensorId;
        sessionMeasuresSensorDao.insert(sessionMeasuresSensor);

        List<SessionWithSensors> sessionsWithSensors = sessionDao.getSessionsWithSensorsByIds(new long[]{123});
        Session foundSession = sessionsWithSensors.get(0).session;
        List<Sensor> foundSensors = sessionsWithSensors.get(0).sensors;

        DbTestUtils.assertSessionEquals(session, foundSession);
        DbTestUtils.assertSensorEquals(sensor, foundSensors.get(0));
    }

    @Test
    public void insertAndGetMultipleSessionsWithSensorsById() throws Exception {
        Session session1 = new Session();
        session1.sessionId = 123;
        session1.startTime = new Timestamp(1000);
        session1.endTime = new Timestamp(2000);

        Session session2 = new Session();
        session2.sessionId = 456;
        session2.startTime = new Timestamp(1000);
        session2.endTime = new Timestamp(2000);

        Sensor sensor = new Sensor();
        sensor.sensorId = 123;
        sensor.desc = SensorDesc.PPG;

        sessionDao.insert(session1, session2);
        sensorDao.insert(sensor);

        SessionMeasuresSensor session1MeasuresSensor = new SessionMeasuresSensor();
        session1MeasuresSensor.sessionId = session1.sessionId;
        session1MeasuresSensor.sensorId = sensor.sensorId;

        SessionMeasuresSensor session2MeasuresSensor = new SessionMeasuresSensor();
        session2MeasuresSensor.sessionId = session2.sessionId;
        session2MeasuresSensor.sensorId = sensor.sensorId;
        sessionMeasuresSensorDao.insert(session1MeasuresSensor, session2MeasuresSensor);

        List<SessionWithSensors> sessionsWithSensors = sessionDao.getSessionsWithSensorsByIds(new long[]{123, 456});
        Session foundSession1 = sessionsWithSensors.get(0).session;
        Session foundSession2 = sessionsWithSensors.get(1).session;
        List<Sensor> foundSensors1 = sessionsWithSensors.get(0).sensors;
        List<Sensor> foundSensors2 = sessionsWithSensors.get(1).sensors;

        DbTestUtils.assertSessionEquals(session1, foundSession1);
        DbTestUtils.assertSessionEquals(session2, foundSession2);
        DbTestUtils.assertSensorEquals(sensor, foundSensors1.get(0));
        DbTestUtils.assertSensorEquals(sensor, foundSensors2.get(0));
    }

    @Test
    public void insertAndGetSessionWithMultipleSensorsById() throws Exception {
        Session session = new Session();
        session.sessionId = 123;
        session.startTime = new Timestamp(1000);
        session.endTime = new Timestamp(2000);

        Sensor sensor1 = new Sensor();
        sensor1.sensorId = 123;
        sensor1.desc = SensorDesc.PPG;

        Sensor sensor2 = new Sensor();
        sensor2.sensorId = 456;
        sensor2.desc = SensorDesc.PPG;

        sessionDao.insert(session);
        sensorDao.insert(sensor1, sensor2);

        SessionMeasuresSensor sessionMeasuresSensor1 = new SessionMeasuresSensor();
        sessionMeasuresSensor1.sessionId = session.sessionId;
        sessionMeasuresSensor1.sensorId = sensor1.sensorId;

        SessionMeasuresSensor sessionMeasuresSensor2 = new SessionMeasuresSensor();
        sessionMeasuresSensor2.sessionId = session.sessionId;
        sessionMeasuresSensor2.sensorId = sensor2.sensorId;
        sessionMeasuresSensorDao.insert(sessionMeasuresSensor1, sessionMeasuresSensor2);

        List<SessionWithSensors> sessionsWithSensors = sessionDao.getSessionsWithSensorsByIds(new long[]{123});
        Session foundSession = sessionsWithSensors.get(0).session;
        List<Sensor> foundSensors = sessionsWithSensors.get(0).sensors;

        DbTestUtils.assertSessionEquals(session, foundSession);
        DbTestUtils.assertSensorEquals(sensor1, foundSensors.get(0));
        DbTestUtils.assertSensorEquals(sensor2, foundSensors.get(1));
    }
}
