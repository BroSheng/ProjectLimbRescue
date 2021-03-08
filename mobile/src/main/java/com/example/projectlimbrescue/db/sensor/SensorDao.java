package com.example.projectlimbrescue.db.sensor;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

/*
Data access object for the Sensor entity, providing the methods used to query the sensor table.
 */

@Dao
public interface SensorDao {
    // Simple "placeholder" methods for now; add more in as functionality or testing requires
    @Query("SELECT * FROM Sensor")
    List<Sensor> getSensors();

    @Transaction
    @Query("SELECT * FROM Sensor")
    List<SensorWithDevices> getSensorsWithDevices();

    @Transaction
    @Query("SELECT * FROM Sensor")
    List<SensorWithReadings> getSensorsWithReadings();

    @Transaction
    @Query("SELECT * FROM Sensor")
    List<SensorWithSessions> getSensorWithSessions();

    @Query("SELECT * FROM Sensor WHERE sensor_id IN (:ids)")
    List<Sensor> getSensorsByIds(int[] ids);

    @Transaction
    @Query("SELECT * FROM Sensor WHERE sensor_id IN (:ids)")
    List<SensorWithDevices> getSensorsWithDevicesByIds(int[] ids);

    @Transaction
    @Query("SELECT * FROM Sensor WHERE sensor_id IN (:ids)")
    List<SensorWithReadings> getSensorsWithReadingsByIds(int[] ids);

    @Transaction
    @Query("SELECT * FROM Sensor WHERE sensor_id IN (:ids)")
    List<SensorWithSessions> getSensorsWithSessionsByIds(int[] ids);

    @Insert
    void insert(Sensor... sensors);

    @Delete
    void delete(Sensor sensor);
}
