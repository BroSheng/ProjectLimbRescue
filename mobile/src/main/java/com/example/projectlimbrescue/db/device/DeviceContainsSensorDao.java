package com.example.projectlimbrescue.db.device;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.google.common.util.concurrent.ListenableFuture;

/*
Data access object for the DeviceContainsSensor relationship. Contains only insert and delete methods.
 */

@Dao
public interface DeviceContainsSensorDao {

    @Insert
    ListenableFuture<long[]> insert(DeviceContainsSensor... devicesContainingSensors);

    @Delete
    ListenableFuture<Integer> delete(DeviceContainsSensor deviceContainsSensor);

    @Query("DELETE FROM DeviceContainsSensor")
    ListenableFuture<Integer> deleteAll();
}
