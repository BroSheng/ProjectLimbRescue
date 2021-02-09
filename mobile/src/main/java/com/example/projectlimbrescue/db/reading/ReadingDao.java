package com.example.projectlimbrescue.db.reading;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.projectlimbrescue.db.device.Device;
import com.example.projectlimbrescue.db.device.DeviceWithReadings;
import com.example.projectlimbrescue.db.device.DeviceWithSensors;
import com.example.projectlimbrescue.db.device.DeviceWithSessions;

import java.util.List;

/*
Data access object for the Reading entity, providing the methods used to query the reading table.
 */

@Dao
public interface ReadingDao {
    // Simple "placeholder" methods for now; add more in as functionality or testing requires
    @Query("SELECT * FROM Reading")
    List<Device> getReadings();

    @Query("SELECT * FROM Reading WHERE id IN (:ids)")
    List<Device> getReadingsByIds(int[] ids);

    @Insert
    void insert(Reading... readings);

    @Delete
    void delete(Reading reading);
}
