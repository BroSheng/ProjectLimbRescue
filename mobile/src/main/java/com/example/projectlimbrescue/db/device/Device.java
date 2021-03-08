package com.example.projectlimbrescue.db.device;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/*
A device is a type of smart technology, such as a smartwatch or smart scale,
that contains one or more sensors. One ‘device’ entry refers to a type of device,
rather than a single particular watch or scale.
 */

@Entity
public class Device {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "device_id")
    public long deviceId;

    // A short description of the device's type, e.g. FOSSIL_GEN_5, enumerated in DeviceDesc.
    @ColumnInfo(name = "desc")
    public DeviceDesc desc;
}