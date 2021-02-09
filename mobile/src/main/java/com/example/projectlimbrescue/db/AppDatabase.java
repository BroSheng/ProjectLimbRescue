package com.example.projectlimbrescue.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.projectlimbrescue.db.device.Device;
import com.example.projectlimbrescue.db.device.DeviceDao;
import com.example.projectlimbrescue.db.reading.Reading;
import com.example.projectlimbrescue.db.reading.ReadingDao;
import com.example.projectlimbrescue.db.sensor.Sensor;
import com.example.projectlimbrescue.db.sensor.SensorDao;
import com.example.projectlimbrescue.db.session.Session;
import com.example.projectlimbrescue.db.session.SessionDao;

@Database(
        entities = {
            Device.class,
            Reading.class,
            Sensor.class,
            Session.class
        },
        version = 1
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract DeviceDao deviceDao();
    public abstract ReadingDao readingDao();
    public abstract SensorDao sensorDao();
    public abstract SessionDao sessionDao();
}
