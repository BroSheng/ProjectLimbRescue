package com.example.projectlimbrescue.db.session;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.example.projectlimbrescue.db.reading.Reading;

import java.util.List;

/*
Room class modeling one-to-many relationship between Session and Reading entities.
 */

public class SessionWithReadings {
    @Embedded
    public Session session;
    @Relation(
        parentColumn = "id",
        entityColumn = "session_id"
    )
    public List<Reading> readings;
}
