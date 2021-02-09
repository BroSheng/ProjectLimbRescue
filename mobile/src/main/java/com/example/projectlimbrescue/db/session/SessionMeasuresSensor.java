package com.example.projectlimbrescue.db.session;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import com.example.projectlimbrescue.db.sensor.Sensor;

/*
Many-to-many association between a sensor and session.
A session can record data from any number of sensors,
and a sensor can be recorded during any number of sessions.
 */

@Entity (primaryKeys = {"session_id", "sensor_id"},
    foreignKeys = {
        @ForeignKey(
            entity = Session.class,
            parentColumns = "session_id",
            childColumns = "session_id",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = Sensor.class,
            parentColumns = "sensor_id",
            childColumns = "sensor_id",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {
            @Index("session_id"),
            @Index("sensor_id")
    }
)
public class SessionMeasuresSensor {
    @ColumnInfo(name = "session_id")
    long sessionId;
    @ColumnInfo(name = "sensor_id")
    long sensorId;
}
