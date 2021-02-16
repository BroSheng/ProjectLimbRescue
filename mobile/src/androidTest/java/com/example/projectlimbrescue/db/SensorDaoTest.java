package com.example.projectlimbrescue.db;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.projectlimbrescue.db.device.Device;
import com.example.projectlimbrescue.db.device.DeviceDao;
import com.example.projectlimbrescue.db.device.DeviceDesc;
import com.example.projectlimbrescue.db.sensor.Sensor;
import com.example.projectlimbrescue.db.sensor.SensorDao;
import com.example.projectlimbrescue.db.sensor.SensorDesc;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;

/*
Test class for SensorDao.
TODO: add tests for SensorWithDevices, SensorWithReadings, SensorWithSessions
 */

@RunWith(AndroidJUnit4.class)
public class SensorDaoTest {
    private SensorDao sensorDao;
    private AppDatabase db;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        sensorDao = db.sensorDao();
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void insertAndGetSensor() throws Exception {
        Sensor sensor = new Sensor();
        sensor.sensorId = 123;
        sensor.desc = SensorDesc.PPG;

        sensorDao.insert(sensor);
        List<Sensor> sensors = sensorDao.getSensors();

        assertEquals(sensors.get(0).sensorId, sensor.sensorId);
        assertEquals(sensors.get(0).desc, sensor.desc);
    }

    @Test
    public void insertAndGetSensorById() throws Exception {
        Sensor sensor = new Sensor();
        sensor.sensorId = 123;
        sensor.desc = SensorDesc.PPG;

        sensorDao.insert(sensor);
        List<Sensor> sensors = sensorDao.getSensors();

        assertEquals(sensors.get(0).sensorId, sensor.sensorId);
        assertEquals(sensors.get(0).desc, sensor.desc);
    }

    @Test
    public void insertAndGetMultipleSensorsById() throws Exception {
        Sensor sensor1 = new Sensor();
        sensor1.sensorId = 123;
        sensor1.desc = SensorDesc.PPG;

        Sensor sensor2 = new Sensor();
        sensor2.sensorId = 456;
        sensor2.desc = SensorDesc.PPG;

        sensorDao.insert(sensor1, sensor2);
        List<Sensor> sensors = sensorDao.getSensorsByIds(new int[]{123, 456});

        assertEquals(sensors.get(0).sensorId, sensor1.sensorId);
        assertEquals(sensors.get(0).desc, sensor1.desc);
        assertEquals(sensors.get(1).sensorId, sensor2.sensorId);
        assertEquals(sensors.get(1).desc, sensor2.desc);
    }

    @Test
    public void insertAndDeleteSensor() throws Exception {
        Sensor sensor = new Sensor();
        sensor.sensorId = 456;
        sensor.desc = SensorDesc.PPG;

        sensorDao.insert(sensor);
        sensorDao.delete(sensor);
        List<Sensor> sensors = sensorDao.getSensors();

        assertEquals(sensors.size(), 0);
    }
}
