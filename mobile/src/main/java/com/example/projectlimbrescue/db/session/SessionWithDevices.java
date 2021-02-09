package com.example.projectlimbrescue.db.session;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.example.projectlimbrescue.db.device.Device;

import java.util.List;

/*
Room class modeling many-to-many relationship between Session and Device entities.
 */

public class SessionWithDevices {
    @Embedded
    public Session session;
    @Relation(
        parentColumn = "id",
        entityColumn = "device_id",
        associateBy = @Junction(SessionReadsFromDevice.class)
    )
    public List<Device> devices;
}
