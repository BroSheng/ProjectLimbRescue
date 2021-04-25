package com.example.projectlimbrescue.db.session;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.google.common.util.concurrent.ListenableFuture;

/*
Data access object for the SessionMeasuresSensor relationship. Contains only insert and delete methods.
 */

@Dao
public interface SessionMeasuresSensorDao {

    @Insert
    ListenableFuture<long[]> insert(SessionMeasuresSensor... sessionsMeasuringSensors);

    @Delete
    ListenableFuture<Integer> delete(SessionMeasuresSensor sessionMeasuringSensor);

    @Query("DELETE FROM SessionMeasuresSensor")
    ListenableFuture<Integer> deleteAll();
}
