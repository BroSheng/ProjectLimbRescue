package com.example.projectlimbrescue.db.session;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.google.common.util.concurrent.ListenableFuture;

/*
Data access object for the SessionReadsFromDevice relationship. Contains only insert and delete methods.
 */

@Dao
public interface SessionReadsFromDeviceDao {

    @Insert
    ListenableFuture<long[]> insert(SessionReadsFromDevice... sessionsReadingFromDevices);

    @Delete
    ListenableFuture<Integer> delete(SessionReadsFromDevice sessionReadingFromDevice);

    @Query("DELETE FROM SessionReadsFromDevice")
    ListenableFuture<Integer> deleteAll();
}
