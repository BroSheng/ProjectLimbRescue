package com.example.projectlimbrescue.db.reading;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.shared.ReadingLimb;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

/*
Data access object for the Reading entity, providing the methods used to query the reading table.
 */

@Dao
public interface ReadingDao {
    // Simple "placeholder" methods for now; add more in as functionality or testing requires
    @Query("SELECT * FROM Reading")
    ListenableFuture<List<Reading>> getReadings();

    @Query("SELECT * FROM Reading WHERE reading_id IN (:ids)")
    ListenableFuture<List<Reading>> getReadingsByIds(long[] ids);

    @Query(("SELECT * FROM Reading WHERE Reading.session_id = (:id) AND Reading.limb = (:limb)"))
    ListenableFuture<List<Reading>> getReadingsForSessionIdAndLimb(long id, ReadingLimb limb);

    @Insert
    ListenableFuture<long[]> insert(Reading... readings);

    @Delete
    ListenableFuture<Integer> delete(Reading reading);
}
