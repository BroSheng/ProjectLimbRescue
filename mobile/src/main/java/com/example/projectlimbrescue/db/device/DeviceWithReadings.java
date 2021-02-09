package com.example.projectlimbrescue.db.device;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.example.projectlimbrescue.db.reading.Reading;

import java.util.List;

/*
Room class modeling one-to-many relationship between Device and Reading entities.
 */

public class DeviceWithReadings {
    @Embedded
    public Device device;
    @Relation(
        parentColumn = "id",
        entityColumn = "device_id"
    )
    public List<Reading> readings;
}
