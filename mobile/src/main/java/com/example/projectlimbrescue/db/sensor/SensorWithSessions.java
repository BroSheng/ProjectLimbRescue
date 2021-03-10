package com.example.projectlimbrescue.db.sensor;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.example.projectlimbrescue.db.session.Session;
import com.example.projectlimbrescue.db.session.SessionMeasuresSensor;

import java.util.List;

/*
Room class modeling many-to-many relationship between Sensor and Session entities.
 */

public class SensorWithSessions {
    @Embedded
    public Sensor sensor;
    @Relation(
            parentColumn = "sensor_id",
            entityColumn = "session_id",
            associateBy = @Junction(SessionMeasuresSensor.class)
    )
    public List<Session> sessions;
}
