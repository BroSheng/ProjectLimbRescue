package com.example.projectlimbrescue.db.session;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

/*
Data access object for the SessionMeasuresSensor relationship. Contains only insert and delete methods.
 */

@Dao
public interface SessionMeasuresSensorDao {

    @Insert
    void insert(SessionMeasuresSensor... sessionsMeasuringSensors);

    @Delete
    void delete(SessionMeasuresSensor sessionMeasuringSensor);
}
