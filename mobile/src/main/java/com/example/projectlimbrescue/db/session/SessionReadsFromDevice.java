package com.example.projectlimbrescue.db.session;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import com.example.projectlimbrescue.db.device.Device;

/*
Many-to-many association between a session and device.
A session can record data from any number of devices,
and a device can be recorded from during any number of sessions.
 */

@Entity(primaryKeys = {"session_id", "device_id"},
        foreignKeys = {
                @ForeignKey(
                        entity = Session.class,
                        parentColumns = "session_id",
                        childColumns = "session_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Device.class,
                        parentColumns = "device_id",
                        childColumns = "device_id",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index("session_id"),
                @Index("device_id")
        }
)
public class SessionReadsFromDevice {
    @ColumnInfo(name = "session_id")
    public long sessionId;
    @ColumnInfo(name = "device_id")
    public long deviceId;
}
