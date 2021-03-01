package com.example.projectlimbrescue.db.sensor;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.example.projectlimbrescue.db.device.Device;
import com.example.projectlimbrescue.db.device.DeviceContainsSensor;

import java.util.List;

/*
Room class modeling many-to-many relationship between Sensor and Device entities.
 */

public class SensorWithDevices {
    @Embedded
    public Sensor sensor;
    @Relation(
            parentColumn = "sensor_id",
            entityColumn = "device_id",
            associateBy = @Junction(DeviceContainsSensor.class)
    )
    public List<Device> devices;
}
