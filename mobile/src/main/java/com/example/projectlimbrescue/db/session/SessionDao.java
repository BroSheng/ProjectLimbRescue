package com.example.projectlimbrescue.db.session;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.shared.Limb;

import java.util.List;

/*
Data access object for the Session entity, providing the methods used to query the session table.
 */

@Dao
public interface SessionDao {
    // Simple "placeholder" methods for now; add more in as functionality or testing requires
    @Query("SELECT * FROM Session")
    List<Session> getSessions();

    @Transaction
    @Query("SELECT * FROM Session")
    List<SessionWithDevices> getSessionsWithDevices();

    @Transaction
    @Query("SELECT * FROM Session")
    List<SessionWithReadings> getSessionsWithReadings();

    @Transaction
    @Query("SELECT * FROM Session")
    List<SessionWithSensors> getSessionsWithSensors();

    @Query("SELECT * FROM Session WHERE session_id IN (:ids)")
    List<Session> getSessionsByIds(long[] ids);
    @Transaction
    @Query("SELECT * FROM Session WHERE session_id IN (:ids)")
    List<SessionWithDevices> getSessionsWithDevicesByIds(long[] ids);
    @Transaction
    @Query("SELECT * FROM Session WHERE session_id IN (:ids)")
    List<SessionWithReadings> getSessionsWithReadingsByIds(long[] ids);
    @Transaction
    @Query("SELECT * FROM Session WHERE session_id IN (:ids)")
    List<SessionWithSensors> getSessionsWithSensorsByIds(long[] ids);

    @Insert
    long[] insert(Session... sessions);

    @Delete
    void delete(Session session);
}
