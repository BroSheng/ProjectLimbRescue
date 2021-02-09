package com.example.projectlimbrescue.db.session;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.example.projectlimbrescue.db.sensor.Sensor;

import java.util.List;

/*
Room class modeling many-to-many relationship between Session and Sensor entities.
 */

public class SessionWithSensors {
    @Embedded
    public Session session;
    @Relation(
        parentColumn = "id",
        entityColumn = "sensor_id",
        associateBy = @Junction(SessionMeasuresSensor.class)
    )
    public List<Sensor> sensors;
}
