package com.example.projectlimbrescue.db.reading;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

/*
Data access object for the Reading entity, providing the methods used to query the reading table.
 */

@Dao
public interface ReadingDao {
    // Simple "placeholder" methods for now; add more in as functionality or testing requires
    @Query("SELECT * FROM Reading")
    List<Reading> getReadings();

    @Query("SELECT * FROM Reading WHERE reading_id IN (:ids)")
    List<Reading> getReadingsByIds(long[] ids);

    @Insert
    long[] insert(Reading... readings);

    @Delete
    void delete(Reading reading);
}
