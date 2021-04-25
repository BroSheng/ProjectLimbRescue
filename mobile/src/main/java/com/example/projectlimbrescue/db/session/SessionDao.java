package com.example.projectlimbrescue.db.session;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

/*
Data access object for the Session entity, providing the methods used to query the session table.
 */

@Dao
public interface SessionDao {
    // Simple "placeholder" methods for now; add more in as functionality or testing requires
    @Query("SELECT * FROM Session")
    ListenableFuture<List<Session>> getSessions();

    @Transaction
    @Query("SELECT * FROM Session")
    ListenableFuture<List<SessionWithDevices>> getSessionsWithDevices();

    @Transaction
    @Query("SELECT * FROM Session")
    ListenableFuture<List<SessionWithReadings>> getSessionsWithReadings();

    @Transaction
    @Query("SELECT * FROM Session")
    ListenableFuture<List<SessionWithSensors>> getSessionsWithSensors();

    @Query("SELECT * FROM Session WHERE session_id IN (:ids)")
    ListenableFuture<List<Session>> getSessionsByIds(long[] ids);
    @Transaction
    @Query("SELECT * FROM Session WHERE session_id IN (:ids)")
    ListenableFuture<List<SessionWithDevices>> getSessionsWithDevicesByIds(long[] ids);
    @Transaction
    @Query("SELECT * FROM Session WHERE session_id IN (:ids)")
    ListenableFuture<List<SessionWithReadings>> getSessionsWithReadingsByIds(long[] ids);
    @Transaction
    @Query("SELECT * FROM Session WHERE session_id IN (:ids)")
    ListenableFuture<List<SessionWithSensors>> getSessionsWithSensorsByIds(long[] ids);

    @Insert
    ListenableFuture<long[]> insert(Session... sessions);

    @Delete
    ListenableFuture<Integer> delete(Session session);

    @Query("DELETE FROM Session")
    ListenableFuture<Integer> deleteAll();
}
