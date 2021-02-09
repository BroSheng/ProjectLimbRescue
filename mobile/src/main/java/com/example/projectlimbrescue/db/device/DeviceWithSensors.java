package com.example.projectlimbrescue.db.device;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.example.projectlimbrescue.db.sensor.Sensor;

import java.util.List;

/*
Room class modeling many-to-many relationship between Device and Sensor entities.
 */

public class DeviceWithSensors {
    @Embedded
    public Device device;
    @Relation(
        parentColumn = "id",
        entityColumn = "sensor_id",
        associateBy = @Junction(DeviceContainsSensor.class)
    )
    public List<Sensor> sensors;
}
