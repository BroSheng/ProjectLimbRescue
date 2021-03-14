package com.example.projectlimbrescue.db.sensor;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.shared.SensorDesc;

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
    List<Sensor> getSensorsByIds(long[] ids);
    @Transaction
    @Query("SELECT * FROM Sensor WHERE sensor_id IN (:ids)")
    List<SensorWithDevices> getSensorsWithDevicesByIds(long[] ids);
    @Transaction
    @Query("SELECT * FROM Sensor WHERE sensor_id IN (:ids)")
    List<SensorWithReadings> getSensorsWithReadingsByIds(long[] ids);
    @Transaction
    @Query("SELECT * FROM Sensor WHERE sensor_id IN (:ids)")
    List<SensorWithSessions> getSensorsWithSessionsByIds(long[] ids);

    @Query("SELECT * FROM Sensor WHERE `desc` = :desc")
    List<Sensor> getSensorsByDesc(SensorDesc desc);

    @Insert
    long[] insert(Sensor... sensors);

    @Delete
    void delete(Sensor sensor);
}
