package com.example.projectlimbrescue.db.device;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.shared.DeviceDesc;

import java.util.List;

/*
Data access object for the Device entity, providing the methods used to query the device table.
 */

@Dao
public interface DeviceDao {
    // Simple "placeholder" methods for now; add more in as functionality or testing requires
    @Query("SELECT * FROM Device")
    List<Device> getDevices();

    @Transaction
    @Query("SELECT * FROM Device")
    List<DeviceWithReadings> getDevicesWithReadings();

    @Transaction
    @Query("SELECT * FROM Device")
    List<DeviceWithSensors> getDevicesWithSensors();

    @Transaction
    @Query("SELECT * FROM Device")
    List<DeviceWithSessions> getDevicesWithSessions();

    @Query("SELECT * FROM Device WHERE device_id IN (:ids)")
    List<Device> getDevicesByIds(int[] ids);

    @Transaction
    @Query("SELECT * FROM Device WHERE device_id IN (:ids)")
    List<DeviceWithReadings> getDevicesWithReadingsByIds(long[] ids);
    @Transaction
    @Query("SELECT * FROM Device WHERE device_id IN (:ids)")
    List<DeviceWithSensors> getDevicesWithSensorsByIds(long[] ids);
    @Transaction
    @Query("SELECT * FROM Device WHERE device_id IN (:ids)")
    List<DeviceWithSessions> getDevicesWithSessionsByIds(long[] ids);

    @Query("SELECT * FROM Device WHERE `desc` = :desc")
    List<Device> getDevicesByDesc (DeviceDesc desc);

    @Insert
    long[] insert(Device... devices);

    @Delete
    void delete(Device device);
}
