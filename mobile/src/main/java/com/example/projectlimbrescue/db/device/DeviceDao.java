package com.example.projectlimbrescue.db.device;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.shared.DeviceDesc;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

/*
Data access object for the Device entity, providing the methods used to query the device table.
 */

@Dao
public interface DeviceDao {
    // Simple "placeholder" methods for now; add more in as functionality or testing requires
    @Query("SELECT * FROM Device")
    ListenableFuture<List<Device>> getDevices();

    @Transaction
    @Query("SELECT * FROM Device")
    ListenableFuture<List<DeviceWithReadings>> getDevicesWithReadings();

    @Transaction
    @Query("SELECT * FROM Device")
    ListenableFuture<List<DeviceWithSensors>> getDevicesWithSensors();

    @Transaction
    @Query("SELECT * FROM Device")
    ListenableFuture<List<DeviceWithSessions>> getDevicesWithSessions();

    @Query("SELECT * FROM Device WHERE device_id IN (:ids)")
    ListenableFuture<List<Device>> getDevicesByIds(int[] ids);

    @Transaction
    @Query("SELECT * FROM Device WHERE device_id IN (:ids)")
    ListenableFuture<List<DeviceWithReadings>> getDevicesWithReadingsByIds(long[] ids);
    @Transaction
    @Query("SELECT * FROM Device WHERE device_id IN (:ids)")
    ListenableFuture<List<DeviceWithSensors>> getDevicesWithSensorsByIds(long[] ids);
    @Transaction
    @Query("SELECT * FROM Device WHERE device_id IN (:ids)")
    ListenableFuture<List<DeviceWithSessions>> getDevicesWithSessionsByIds(long[] ids);

    @Query("SELECT * FROM Device WHERE `desc` = :desc")
    ListenableFuture<List<Device>> getDevicesByDesc (DeviceDesc desc);

    @Insert
    ListenableFuture<long[]> insert(Device... devices);

    @Delete
    ListenableFuture<Integer> delete(Device device);

    @Query("DELETE FROM Device")
    ListenableFuture<Integer> deleteAll();
}
