package com.example.projectlimbrescue.db.device;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import com.example.projectlimbrescue.db.sensor.Sensor;

/*
Many-to-many association between a device and sensor.
A device can contain any number of sensor types,
and a sensor type can be found in any number of devices.
 */

@Entity (primaryKeys = {"device_id", "sensor_id"},
    foreignKeys = {
        @ForeignKey(
            entity = Device.class,
            parentColumns = "device_id",
            childColumns = "device_id",
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
            @Index("device_id"),
            @Index("sensor_id")
    }
)
public class DeviceContainsSensor {
    @ColumnInfo(name = "device_id")
    public long deviceId;
    @ColumnInfo(name = "sensor_id")
    public long sensorId;
}
