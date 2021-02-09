package com.example.projectlimbrescue.db.session;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import com.example.projectlimbrescue.db.device.Device;

/*
Many-to-many association between a session and device.
A session can record data from any number of devices,
and a device can be recorded from during any number of sessions.
 */

@Entity (primaryKeys = {"session_id", "device_id"},
    foreignKeys = {
        @ForeignKey(
            entity = Session.class,
            parentColumns = "id",
            childColumns = "session_id",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = Device.class,
            parentColumns = "id",
            childColumns = "device_id",
            onDelete = ForeignKey.CASCADE
        )
    }
)
public class SessionReadsFromDevice {
    @ColumnInfo(name = "session_id")
    long sessionId;
    @ColumnInfo(name = "device_id")
    long deviceId;
}
