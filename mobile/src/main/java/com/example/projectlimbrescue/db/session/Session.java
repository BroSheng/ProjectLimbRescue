package com.example.projectlimbrescue.db.session;
import androidx.room.*;
import java.sql.Timestamp;

/*
A session is a continuous period of data collection begun and ended by user input.
It is made up of readings, and may read from multiple sensors
and/or multiple devices over the same time period.
 */

@Entity
public class Session {
    @PrimaryKey
    public long id;

    // The start timestamp of the session, e.g. 2021-02-03 12:01:36.0000
    @ColumnInfo(name = "start_time")
    Timestamp startTime;

    // The end timestamp of the session, e.g. 2021-02-03 12:02:54.0000
    @ColumnInfo(name = "end_time")
    Timestamp endTime;
}
