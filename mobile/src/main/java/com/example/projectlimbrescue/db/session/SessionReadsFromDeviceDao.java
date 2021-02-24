package com.example.projectlimbrescue.db.session;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;

/*
Data access object for the SessionReadsFromDevice relationship. Contains only insert and delete methods.
 */

@Dao
public interface SessionReadsFromDeviceDao {

    @Insert
    void insert(SessionReadsFromDevice... sessionsReadingFromDevices);

    @Delete
    void delete(SessionReadsFromDevice sessionReadingFromDevice);
}
