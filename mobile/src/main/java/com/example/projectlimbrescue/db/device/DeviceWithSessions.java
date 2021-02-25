package com.example.projectlimbrescue.db.device;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.example.projectlimbrescue.db.session.Session;
import com.example.projectlimbrescue.db.session.SessionReadsFromDevice;

import java.util.List;

/*
Room class modeling many-to-many relationship between Device and Session entities.
 */

public class DeviceWithSessions {
    @Embedded
    public Device device;
    @Relation(
        parentColumn = "device_id",
        entityColumn = "session_id",
        associateBy = @Junction(SessionReadsFromDevice.class)
    )
    public List<Session> sessions;
}
