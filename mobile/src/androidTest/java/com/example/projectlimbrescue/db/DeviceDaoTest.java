package com.example.projectlimbrescue.db;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.projectlimbrescue.db.device.Device;
import com.example.projectlimbrescue.db.device.DeviceDao;
import com.example.projectlimbrescue.db.device.DeviceDesc;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import java.util.List;

/*
Test class for DeviceDao.
TODO: add tests for DeviceWithReadings, DeviceWithSensors, DeviceWithSessions
 */

@RunWith(AndroidJUnit4.class)
public class DeviceDaoTest {
    private DeviceDao deviceDao;
    private AppDatabase db;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        deviceDao = db.deviceDao();
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void insertAndGetDevice() throws Exception {
        Device device = new Device();
        device.deviceId = 123;
        device.desc = DeviceDesc.FOSSIL_GEN_5;

        deviceDao.insert(device);
        List<Device> devices = deviceDao.getDevices();

        assertEquals(devices.get(0).deviceId, device.deviceId);
        assertEquals(devices.get(0).desc, device.desc);
    }

    @Test
    public void insertAndGetDeviceById() throws Exception {
        Device device = new Device();
        device.deviceId = 123;
        device.desc = DeviceDesc.FOSSIL_GEN_5;

        deviceDao.insert(device);
        List<Device> devices = deviceDao.getDevicesByIds(new int[]{123});

        assertEquals(devices.get(0).deviceId, device.deviceId);
        assertEquals(devices.get(0).desc, device.desc);
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

        assertEquals(devices.get(0).deviceId, device1.deviceId);
        assertEquals(devices.get(0).desc, device1.desc);
        assertEquals(devices.get(1).deviceId, device2.deviceId);
        assertEquals(devices.get(1).desc, device2.desc);
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
}
