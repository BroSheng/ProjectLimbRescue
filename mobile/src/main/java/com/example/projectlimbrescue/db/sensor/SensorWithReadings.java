package com.example.projectlimbrescue.db.sensor;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.example.projectlimbrescue.db.reading.Reading;

import java.util.List;

/*
Room class modeling one-to-many relationship between Sensor and Reading entities.
 */

public class SensorWithReadings {
    @Embedded
    public Sensor sensor;
    @Relation(
            parentColumn = "sensor_id",
            entityColumn = "sensor_id"
    )
    public List<Reading> readings;
}
