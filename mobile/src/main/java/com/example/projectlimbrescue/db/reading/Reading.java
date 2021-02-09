package com.example.projectlimbrescue.db.reading;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import com.example.projectlimbrescue.db.sensor.Sensor;
import com.example.projectlimbrescue.db.session.Session;
import com.example.projectlimbrescue.db.device.Device;

import java.sql.Timestamp;

/*
A reading is a single instance of raw data collected from a type of sensor,
on a particular device, about a given limb, during a single session.
It numerically represents some output recorded by the sensor.
 */

@Entity ( foreignKeys = {
    @ForeignKey(
        entity = Session.class,
        parentColumns = "id",
        childColumns = "session_id",
        onDelete = ForeignKey.CASCADE
    ),
    @ForeignKey (
            entity = Device.class,
            parentColumns = "id",
            childColumns = "device_id",
            onDelete = ForeignKey.CASCADE
    ),
    @ForeignKey (
            entity = Sensor.class,
            parentColumns = "id",
            childColumns = "sensor_id",
            onDelete = ForeignKey.CASCADE
    )})
public class Reading {
    @PrimaryKey
    public long id;

    @ColumnInfo(name = "session_id")
    public long sessionId;
    @ColumnInfo(name = "device_id")
    public long deviceId;
    @ColumnInfo(name = "sensor_id")
    public long sensorId;

    // The timestamp when the reading was taken, e.g. 2021-02-03 12:01:37.5500
    public Timestamp timestamp;

    // The raw data value recorded in the reading, e.g. 12034.035
    public float value;

    // The body limb or combination of limbs being measured, e.g. LEFT_ARM, enumerated in ReadingLimb.
    public ReadingLimb limb;
}
