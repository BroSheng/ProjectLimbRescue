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
import com.example.projectlimbrescue.db.device.DeviceWithReadings;
import com.example.projectlimbrescue.db.device.DeviceWithSensors;
import com.example.projectlimbrescue.db.device.DeviceWithSessions;
import com.example.projectlimbrescue.db.reading.Reading;
import com.example.projectlimbrescue.db.reading.ReadingDao;
import com.example.shared.ReadingLimb;
import com.example.projectlimbrescue.db.sensor.Sensor;
import com.example.projectlimbrescue.db.sensor.SensorDao;
import com.example.shared.SensorDesc;
import com.example.projectlimbrescue.db.session.Session;
import com.example.projectlimbrescue.db.session.SessionDao;
import com.example.projectlimbrescue.db.session.SessionReadsFromDevice;
import com.example.projectlimbrescue.db.session.SessionReadsFromDeviceDao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.Assert.assertEquals;

/*
Test class for DeviceDao.
 */

@RunWith(AndroidJUnit4.class)
public class DeviceDaoTest {
    private DeviceDao deviceDao;
    private DeviceContainsSensorDao deviceContainsSensorDao;
    private ReadingDao readingDao;
    private SensorDao sensorDao;
    private SessionDao sessionDao;
    private SessionReadsFromDeviceDao sessionReadsFromDeviceDao;
    private AppDatabase db;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        deviceDao = db.deviceDao();
        deviceContainsSensorDao = db.deviceContainsSensorDao();
        readingDao = db.readingDao();
        sensorDao = db.sensorDao();
        sessionDao = db.sessionDao();
        sessionReadsFromDeviceDao = db.sessionReadsFromDeviceDao();
    }

    @After
    public void closeDb() {
        db.close();
    }

    /*
    Standard tests -- insert, get by id/desc, insert and remove
     */

    @Test
    public void insertAndGetDevice() throws Exception {
        Device device = new Device();
        device.deviceId = 123;
        device.desc = DeviceDesc.FOSSIL_GEN_5;

        deviceDao.insert(device);
        List<Device> devices = deviceDao.getDevices();

        DbTestUtils.assertDeviceEquals(device, devices.get(0));
    }

    @Test
    public void insertAndGetDeviceByDesc() throws Exception {
        Device device = new Device();
        device.deviceId = 123;
        device.desc = DeviceDesc.FOSSIL_GEN_5;

        deviceDao.insert(device);
        List<Device> devices = deviceDao.getDevicesByDesc(DeviceDesc.FOSSIL_GEN_5);

        DbTestUtils.assertDeviceEquals(device, devices.get(0));
    }

    @Test
    public void insertAndGetDeviceById() throws Exception {
        Device device = new Device();
        device.deviceId = 123;
        device.desc = DeviceDesc.FOSSIL_GEN_5;

        deviceDao.insert(device);
        List<Device> devices = deviceDao.getDevicesByIds(new int[]{123});

        DbTestUtils.assertDeviceEquals(device, devices.get(0));
    }

    @Test
    public void insertAndGetMultipleDevicesById() throws Exception {
        Device device1 = new Device();
        device1.deviceId = 123;
        device1.desc = DeviceDesc.FOSSIL_GEN_5;

        Device device2 = new Device();
        device2.deviceId = 456;
        device2.desc = DeviceDesc.FOSSIL_GEN_5;

        deviceDao.insert(device1);
        deviceDao.insert(device2);
        List<Device> devices = deviceDao.getDevicesByIds(new int[]{123, 456});

        DbTestUtils.assertDeviceEquals(device1, devices.get(0));
        DbTestUtils.assertDeviceEquals(device2, devices.get(1));
    }

    @Test
    public void insertAndDeleteDevice() throws Exception {
        Device device = new Device();
        device.deviceId = 123;
        device.desc = DeviceDesc.FOSSIL_GEN_5;

        deviceDao.insert(device);
        deviceDao.delete(device);
        List<Device> devices = deviceDao.getDevices();

        assertEquals(devices.size(), 0);
    }

    /*
    DeviceWithReadings tests -- insert, get by id, insert multiple, and insert with multiple readings
     */

    @Test
    public void insertAndGetDeviceWithReadings() throws Exception {
        Device device = new Device();
        device.deviceId = 123;
        device.desc = DeviceDesc.FOSSIL_GEN_5;

        // create valid entities for reading foreign keys
        Sensor sensor = new Sensor();
        sensor.sensorId = 789;
        sensor.desc = SensorDesc.PPG;
        sensorDao.insert(sensor);

        Session session = new Session();
        session.sessionId = 012;
        session.startTime = new Timestamp(1000);
        session.endTime = new Timestamp(2000);
        sessionDao.insert(session);


        // insert the reading itself
        Reading reading = new Reading();
        reading.deviceId = 123;
        reading.readingId = 456;
        reading.sensorId = 789;
        reading.sessionId = 012;
        reading.time = 1000;
        reading.value = 123.456f;
        reading.limb = ReadingLimb.LEFT_ARM;

        deviceDao.insert(device);
        readingDao.insert(reading);

        // test validity
        List<DeviceWithReadings> devicesWithReadings = deviceDao.getDevicesWithReadings();
        Device foundDevice = devicesWithReadings.get(0).device;
        List<Reading> foundReadings = devicesWithReadings.get(0).readings;

        DbTestUtils.assertDeviceEquals(device, foundDevice);
        DbTestUtils.assertReadingEquals(reading, foundReadings.get(0));
    }

    @Test
    public void insertAndGetDevicesWithReadingsById() throws Exception {
        Device device = new Device();
        device.deviceId = 123;
        device.desc = DeviceDesc.FOSSIL_GEN_5;

        // create valid entities for reading foreign keys
        Sensor sensor = new Sensor();
        sensor.sensorId = 789;
        sensor.desc = SensorDesc.PPG;
        sensorDao.insert(sensor);

        Session session = new Session();
        session.sessionId = 012;
        session.startTime = new Timestamp(1000);
        session.endTime = new Timestamp(2000);
        sessionDao.insert(session);


        // insert the reading itself
        Reading reading = new Reading();
        reading.deviceId = 123;
        reading.readingId = 456;
        reading.sensorId = 789;
        reading.sessionId = 012;
        reading.time = 1000;
        reading.value = 123.456f;
        reading.limb = ReadingLimb.LEFT_ARM;

        deviceDao.insert(device);
        readingDao.insert(reading);

        // test validity
        List<DeviceWithReadings> devicesWithReadings = deviceDao.getDevicesWithReadingsByIds(new long[]{123});
        Device foundDevice = devicesWithReadings.get(0).device;
        List<Reading> foundReadings = devicesWithReadings.get(0).readings;

        DbTestUtils.assertDeviceEquals(device, foundDevice);
        DbTestUtils.assertReadingEquals(reading, foundReadings.get(0));
    }

    @Test
    public void insertAndGetMultipleDevicesWithReadingsById() throws Exception {
        Device device1 = new Device();
        device1.deviceId = 123;
        device1.desc = DeviceDesc.FOSSIL_GEN_5;

        Device device2 = new Device();
        device2.deviceId = 456;
        device2.desc = DeviceDesc.FOSSIL_GEN_5;

        // create valid entities for reading foreign keys
        Sensor sensor = new Sensor();
        sensor.sensorId = 789;
        sensor.desc = SensorDesc.PPG;
        sensorDao.insert(sensor);

        Session session = new Session();
        session.sessionId = 012;
        session.startTime = new Timestamp(1000);
        session.endTime = new Timestamp(2000);
        sessionDao.insert(session);


        // insert the readings themselves
        Reading reading1 = new Reading();
        reading1.deviceId = 123;
        reading1.readingId = 123;
        reading1.sensorId = 789;
        reading1.sessionId = 012;
        reading1.time = 1000;
        reading1.value = 123.456f;
        reading1.limb = ReadingLimb.LEFT_ARM;

        Reading reading2 = new Reading();
        reading2.deviceId = 456;
        reading2.readingId = 456;
        reading2.sensorId = 789;
        reading2.sessionId = 012;
        reading2.time = 1000;
        reading2.value = 123.456f;
        reading2.limb = ReadingLimb.LEFT_ARM;

        deviceDao.insert(device1);
        deviceDao.insert(device2);
        readingDao.insert(reading1);
        readingDao.insert(reading2);

        // test validity
        List<DeviceWithReadings> devicesWithReadings = deviceDao.getDevicesWithReadingsByIds(new long[]{123, 456});

        Device foundDevice1 = devicesWithReadings.get(0).device;
        List<Reading> foundReadings1 = devicesWithReadings.get(0).readings;
        Device foundDevice2 = devicesWithReadings.get(1).device;
        List<Reading> foundReadings2 = devicesWithReadings.get(1).readings;

        DbTestUtils.assertDeviceEquals(device1, foundDevice1);
        DbTestUtils.assertReadingEquals(reading1, foundReadings1.get(0));
        DbTestUtils.assertDeviceEquals(device2, foundDevice2);
        DbTestUtils.assertReadingEquals(reading2, foundReadings2.get(0));
    }

    @Test
    public void insertAndGetDeviceWithMultipleReadingsById() throws Exception {
        Device device = new Device();
        device.deviceId = 123;
        device.desc = DeviceDesc.FOSSIL_GEN_5;

        // create valid entities for reading foreign keys
        Sensor sensor = new Sensor();
        sensor.sensorId = 789;
        sensor.desc = SensorDesc.PPG;
        sensorDao.insert(sensor);

        Session session = new Session();
        session.sessionId = 012;
        session.startTime = new Timestamp(1000);
        session.endTime = new Timestamp(2000);
        sessionDao.insert(session);


        // insert the readings themselves
        Reading reading1 = new Reading();
        reading1.deviceId = 123;
        reading1.readingId = 123;
        reading1.sensorId = 789;
        reading1.sessionId = 012;
        reading1.time = 1000;
        reading1.value = 123.456f;
        reading1.limb = ReadingLimb.LEFT_ARM;

        Reading reading2 = new Reading();
        reading2.deviceId = 123;
        reading2.readingId = 456;
        reading2.sensorId = 789;
        reading2.sessionId = 012;
        reading2.time = 1000;
        reading2.value = 123.456f;
        reading2.limb = ReadingLimb.LEFT_ARM;

        deviceDao.insert(device);
        readingDao.insert(reading1);
        readingDao.insert(reading2);

        // test validity
        List<DeviceWithReadings> devicesWithReadings = deviceDao.getDevicesWithReadingsByIds(new long[]{123});

        Device foundDevice = devicesWithReadings.get(0).device;
        List<Reading> foundReadings = devicesWithReadings.get(0).readings;

        DbTestUtils.assertDeviceEquals(device, foundDevice);
        DbTestUtils.assertReadingEquals(reading1, foundReadings.get(0));
        DbTestUtils.assertReadingEquals(reading2, foundReadings.get(1));
    }

    /*
    DeviceWithSensors tests -- insert, get by id, insert multiple, and insert with multiple sensors
     */

    @Test
    public void insertAndGetDeviceWithSensors() throws Exception {
        Device device = new Device();
        device.deviceId = 123;
        device.desc = DeviceDesc.FOSSIL_GEN_5;

        Sensor sensor = new Sensor();
        sensor.sensorId = 456;
        sensor.desc = SensorDesc.PPG;

        deviceDao.insert(device);
        sensorDao.insert(sensor);

        DeviceContainsSensor deviceContainsSensor = new DeviceContainsSensor();
        deviceContainsSensor.deviceId = device.deviceId;
        deviceContainsSensor.sensorId = sensor.sensorId;
        deviceContainsSensorDao.insert(deviceContainsSensor);

        List<DeviceWithSensors> deviceWithSensors = deviceDao.getDevicesWithSensors();
        Device foundDevice = deviceWithSensors.get(0).device;
        List<Sensor> foundSensors = deviceWithSensors.get(0).sensors;

        DbTestUtils.assertDeviceEquals(device, foundDevice);
        DbTestUtils.assertSensorEquals(sensor, foundSensors.get(0));
    }

    @Test
    public void insertAndGetDeviceWithSensorsById() throws Exception {
        Device device = new Device();
        device.deviceId = 123;
        device.desc = DeviceDesc.FOSSIL_GEN_5;

        Sensor sensor = new Sensor();
        sensor.sensorId = 456;
        sensor.desc = SensorDesc.PPG;

        deviceDao.insert(device);
        sensorDao.insert(sensor);

        DeviceContainsSensor deviceContainsSensor = new DeviceContainsSensor();
        deviceContainsSensor.deviceId = device.deviceId;
        deviceContainsSensor.sensorId = sensor.sensorId;
        deviceContainsSensorDao.insert(deviceContainsSensor);

        List<DeviceWithSensors> deviceWithSensors = deviceDao.getDevicesWithSensorsByIds(new long[]{123});
        Device foundDevice = deviceWithSensors.get(0).device;
        List<Sensor> foundSensors = deviceWithSensors.get(0).sensors;

        DbTestUtils.assertDeviceEquals(device, foundDevice);
        DbTestUtils.assertSensorEquals(sensor, foundSensors.get(0));
    }

    @Test
    public void insertAndGetMultipleDevicesWithSensorsById() throws Exception {
        Device device1 = new Device();
        device1.deviceId = 123;
        device1.desc = DeviceDesc.FOSSIL_GEN_5;

        Device device2 = new Device();
        device2.deviceId = 456;
        device2.desc = DeviceDesc.FOSSIL_GEN_5;

        Sensor sensor = new Sensor();
        sensor.sensorId = 456;
        sensor.desc = SensorDesc.PPG;

        deviceDao.insert(device1, device2);
        sensorDao.insert(sensor);

        DeviceContainsSensor device1ContainsSensor = new DeviceContainsSensor();
        device1ContainsSensor.deviceId = device1.deviceId;
        device1ContainsSensor.sensorId = sensor.sensorId;

        DeviceContainsSensor device2ContainsSensor = new DeviceContainsSensor();
        device2ContainsSensor.deviceId = device2.deviceId;
        device2ContainsSensor.sensorId = sensor.sensorId;

        deviceContainsSensorDao.insert(device1ContainsSensor, device2ContainsSensor);

        List<DeviceWithSensors> devicesWithSensors = deviceDao.getDevicesWithSensorsByIds(new long[]{123, 456});
        Device foundDevice1 = devicesWithSensors.get(0).device;
        List<Sensor> foundSensors1 = devicesWithSensors.get(0).sensors;
        Device foundDevice2 = devicesWithSensors.get(1).device;
        List<Sensor> foundSensors2 = devicesWithSensors.get(1).sensors;

        DbTestUtils.assertDeviceEquals(device1, foundDevice1);
        DbTestUtils.assertSensorEquals(sensor, foundSensors1.get(0));
        DbTestUtils.assertDeviceEquals(device2, foundDevice2);
        DbTestUtils.assertSensorEquals(sensor, foundSensors2.get(0));
    }

    @Test
    public void insertAndGetDeviceWithMultipleSensorsById() throws Exception {
        Device device = new Device();
        device.deviceId = 123;
        device.desc = DeviceDesc.FOSSIL_GEN_5;

        Sensor sensor1 = new Sensor();
        sensor1.sensorId = 123;
        sensor1.desc = SensorDesc.PPG;
        Sensor sensor2 = new Sensor();
        sensor2.sensorId = 456;
        sensor2.desc = SensorDesc.PPG;

        deviceDao.insert(device);
        sensorDao.insert(sensor1, sensor2);

        DeviceContainsSensor deviceContainsSensor1 = new DeviceContainsSensor();
        deviceContainsSensor1.deviceId = device.deviceId;
        deviceContainsSensor1.sensorId = sensor1.sensorId;

        DeviceContainsSensor deviceContainsSensor2 = new DeviceContainsSensor();
        deviceContainsSensor2.deviceId = device.deviceId;
        deviceContainsSensor2.sensorId = sensor2.sensorId;

        deviceContainsSensorDao.insert(deviceContainsSensor1, deviceContainsSensor2);

        List<DeviceWithSensors> deviceWithSensors = deviceDao.getDevicesWithSensorsByIds(new long[]{123});
        Device foundDevice = deviceWithSensors.get(0).device;
        List<Sensor> foundSensors = deviceWithSensors.get(0).sensors;

        DbTestUtils.assertDeviceEquals(device, foundDevice);
        DbTestUtils.assertSensorEquals(sensor1, foundSensors.get(0));
        DbTestUtils.assertSensorEquals(sensor2, foundSensors.get(1));
    }

    /*
    DeviceWithSessions tests -- insert, get by id, insert multiple, and insert with multiple sessions
     */

    @Test
    public void insertAndGetDeviceWithSessions() throws Exception {
        Device device = new Device();
        device.deviceId = 123;
        device.desc = DeviceDesc.FOSSIL_GEN_5;

        Session session = new Session();
        session.sessionId = 123;
        session.startTime = new Timestamp(1000);
        session.endTime = new Timestamp(2000);

        deviceDao.insert(device);
        sessionDao.insert(session);

        SessionReadsFromDevice sessionReadsFromDevice = new SessionReadsFromDevice();
        sessionReadsFromDevice.deviceId = device.deviceId;
        sessionReadsFromDevice.sessionId = session.sessionId;
        sessionReadsFromDeviceDao.insert(sessionReadsFromDevice);

        List<DeviceWithSessions> deviceWithSessions = deviceDao.getDevicesWithSessions();
        Device foundDevice = deviceWithSessions.get(0).device;
        List<Session> foundSessions = deviceWithSessions.get(0).sessions;

        DbTestUtils.assertDeviceEquals(device, foundDevice);
        DbTestUtils.assertSessionEquals(session, foundSessions.get(0));
    }

    @Test
    public void insertAndGetDeviceWithSessionsById() throws Exception {
        Device device = new Device();
        device.deviceId = 123;
        device.desc = DeviceDesc.FOSSIL_GEN_5;

        Session session = new Session();
        session.sessionId = 123;
        session.startTime = new Timestamp(1000);
        session.endTime = new Timestamp(2000);

        deviceDao.insert(device);
        sessionDao.insert(session);

        SessionReadsFromDevice sessionReadsFromDevice = new SessionReadsFromDevice();
        sessionReadsFromDevice.deviceId = device.deviceId;
        sessionReadsFromDevice.sessionId = session.sessionId;
        sessionReadsFromDeviceDao.insert(sessionReadsFromDevice);

        List<DeviceWithSessions> deviceWithSessions = deviceDao.getDevicesWithSessionsByIds(new long[]{123});
        Device foundDevice = deviceWithSessions.get(0).device;
        List<Session> foundSessions = deviceWithSessions.get(0).sessions;

        DbTestUtils.assertDeviceEquals(device, foundDevice);
        DbTestUtils.assertSessionEquals(session, foundSessions.get(0));
    }

    @Test
    public void insertAndGetMultipleDevicesWithSessionsById() throws Exception {
        Device device1 = new Device();
        device1.deviceId = 123;
        device1.desc = DeviceDesc.FOSSIL_GEN_5;

        Device device2 = new Device();
        device2.deviceId = 456;
        device2.desc = DeviceDesc.FOSSIL_GEN_5;

        Session session = new Session();
        session.sessionId = 123;
        session.startTime = new Timestamp(1000);
        session.endTime = new Timestamp(2000);

        deviceDao.insert(device1, device2);
        sessionDao.insert(session);

        SessionReadsFromDevice sessionReadsFromDevice1 = new SessionReadsFromDevice();
        sessionReadsFromDevice1.deviceId = device1.deviceId;
        sessionReadsFromDevice1.sessionId = session.sessionId;
        SessionReadsFromDevice sessionReadsFromDevice2 = new SessionReadsFromDevice();
        sessionReadsFromDevice2.deviceId = device2.deviceId;
        sessionReadsFromDevice2.sessionId = session.sessionId;

        sessionReadsFromDeviceDao.insert(sessionReadsFromDevice1, sessionReadsFromDevice2);

        List<DeviceWithSessions> devicesWithSessions = deviceDao.getDevicesWithSessionsByIds(new long[]{123, 456});
        Device foundDevice1 = devicesWithSessions.get(0).device;
        List<Session> foundSessions1 = devicesWithSessions.get(0).sessions;
        Device foundDevice2 = devicesWithSessions.get(1).device;
        List<Session> foundSessions2 = devicesWithSessions.get(1).sessions;

        DbTestUtils.assertDeviceEquals(device1, foundDevice1);
        DbTestUtils.assertSessionEquals(session, foundSessions1.get(0));
        DbTestUtils.assertDeviceEquals(device2, foundDevice2);
        DbTestUtils.assertSessionEquals(session, foundSessions2.get(0));
    }

    @Test
    public void insertAndGetDeviceWithMultipleSessionsById() throws Exception {
        Device device = new Device();
        device.deviceId = 123;
        device.desc = DeviceDesc.FOSSIL_GEN_5;

        Session session1 = new Session();
        session1.sessionId = 123;
        session1.startTime = new Timestamp(1000);
        session1.endTime = new Timestamp(2000);

        Session session2 = new Session();
        session2.sessionId = 456;
        session2.startTime = new Timestamp(1000);
        session2.endTime = new Timestamp(2000);

        SessionReadsFromDevice session1ReadsFromDevice = new SessionReadsFromDevice();
        session1ReadsFromDevice.deviceId = device.deviceId;
        session1ReadsFromDevice.sessionId = session1.sessionId;
        SessionReadsFromDevice session2ReadsFromDevice = new SessionReadsFromDevice();
        session2ReadsFromDevice.deviceId = device.deviceId;
        session2ReadsFromDevice.sessionId = session2.sessionId;

        deviceDao.insert(device);
        sessionDao.insert(session1, session2);

        sessionReadsFromDeviceDao.insert(session1ReadsFromDevice, session2ReadsFromDevice);

        List<DeviceWithSessions> deviceWithSessions = deviceDao.getDevicesWithSessionsByIds(new long[]{123});
        Device foundDevice = deviceWithSessions.get(0).device;
        List<Session> foundSessions = deviceWithSessions.get(0).sessions;

        DbTestUtils.assertDeviceEquals(device, foundDevice);
        DbTestUtils.assertSessionEquals(session1, foundSessions.get(0));
        DbTestUtils.assertSessionEquals(session2, foundSessions.get(1));
    }
}
