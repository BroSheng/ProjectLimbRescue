package com.example.projectlimbrescue.db.reading;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
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
        parentColumns = "session_id",
        childColumns = "session_id",
        onDelete = ForeignKey.CASCADE
    ),
    @ForeignKey (
            entity = Device.class,
            parentColumns = "device_id",
            childColumns = "device_id",
            onDelete = ForeignKey.CASCADE
    ),
    @ForeignKey (
            entity = Sensor.class,
            parentColumns = "sensor_id",
            childColumns = "sensor_id",
            onDelete = ForeignKey.CASCADE
    )},
    indices = {
        @Index("session_id"),
        @Index("device_id"),
        @Index("sensor_id")
    })
public class Reading {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "reading_id")
    public long readingId;

    @ColumnInfo(name = "session_id")
    public long sessionId;
    @ColumnInfo(name = "device_id")
    public long deviceId;
    @ColumnInfo(name = "sensor_id")
    public long sensorId;

    // The number of nanos since the start of the reading's session, for ordering readings
    public long time;

    // The raw data value recorded in the reading, e.g. 12034.035
    public double value;

    // The body limb or combination of limbs being measured, e.g. LEFT_ARM, enumerated in ReadingLimb.
    public ReadingLimb limb;
}
