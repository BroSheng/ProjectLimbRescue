package com.example.projectlimbrescue.db.sensor;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.shared.SensorDesc;

/*
A sensor is a device that takes readings of health data.
It is situated in devices and may produce readings. One ‘sensor’ entry refers to a type of sensor,
rather than a single particular one on a device.
 */

@Entity
public class Sensor {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "sensor_id")
    public long sensorId;

    // A short description of the sensor's type, e.g. PPG, enumerated in SensorDesc.
    @ColumnInfo(name = "desc")
    public SensorDesc desc;
}
