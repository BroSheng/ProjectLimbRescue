package com.example.projectlimbrescue.db;

/*
Contains type converters for Timestamp and all enums used in database.
 */

import androidx.room.TypeConverter;

import com.example.shared.DeviceDesc;
import com.example.projectlimbrescue.db.reading.ReadingLimb;
import com.example.shared.SensorDesc;

import java.sql.Timestamp;

public class Converters {
    // Timestamp
    @TypeConverter
    public static long fromTimestamp(Timestamp ts) {
        return ts.getTime();
    }

    @TypeConverter
    public static Timestamp toTimestamp(long ms) {
        return new Timestamp(ms);
    }

    // DeviceDesc
    @TypeConverter
    public static int fromDeviceDesc(DeviceDesc desc) {
        return desc.ordinal();
    }

    @TypeConverter
    public static DeviceDesc toDeviceDesc(int val) {
        return (DeviceDesc.values()[val]);
    }

    // ReadingLimb
    @TypeConverter
    public static int fromReadingLimb(ReadingLimb limb) {
        return limb.ordinal();
    }

    @TypeConverter
    public static ReadingLimb toReadingLimb(int val) {
        return (ReadingLimb.values()[val]);
    }

    // SensorDesc
    @TypeConverter
    public static int fromSensorDesc(SensorDesc desc) {
        return desc.ordinal();
    }

    @TypeConverter
    public static SensorDesc toSensorDesc(int val) {
        return (SensorDesc.values()[val]);
    }
}
