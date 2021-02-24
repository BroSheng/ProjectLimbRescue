package com.example.projectlimbrescue.db.device;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

/*
Data access object for the DeviceContainsSensor relationship. Contains only insert and delete methods.
 */

@Dao
public interface DeviceContainsSensorDao {

    @Insert
    void insert(DeviceContainsSensor... devicesContainingSensors);

    @Delete
    void delete(DeviceContainsSensor deviceContainsSensor);
}
