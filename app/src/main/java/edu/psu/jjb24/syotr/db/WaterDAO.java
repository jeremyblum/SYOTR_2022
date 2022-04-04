package edu.psu.jjb24.syotr.db;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface WaterDAO {
    @Query("SELECT rowid, playspot, " +
            "river, lowLevel, idealLevel, highLevel, tempC, heightFt, " +
            "abs(heightFt - idealLevel) < 0.05 as ideal, (heightFt <= highLevel AND heightFt >= lowLevel) as inPlay, liked " +
            "FROM playspot INNER JOIN gauge on (playspot.gaugeSite = gauge.site) "+
            "ORDER BY ideal DESC, inPlay DESC, playspot")
    LiveData<List<PlayspotStatus>> getAll();

    @Query("SELECT rowid as rowid, playspot, " +
            "river, lowLevel, idealLevel, highLevel, tempC, heightFt, " +
            "abs(heightFt - idealLevel) < 0.05 as ideal, (heightFt <= highLevel AND heightFt >= lowLevel) as inPlay, liked " +
            "FROM playspot INNER JOIN gauge on (playspot.gaugeSite = gauge.site) " +
            "WHERE liked " +
            "ORDER BY ideal DESC, inPlay DESC, playspot")
    LiveData<List<PlayspotStatus>> getLiked();

    @Query("SELECT * FROM gauge")
    List<Gauge> getGauges();

    @Insert
    void insert(Playspot... playspots);

    @Insert
    void insert(Gauge... gauges);

    @Update
    void update(Gauge... gauges);

    @Query("UPDATE playspot SET liked = :likeValue WHERE rowid = :id")
    void update(int id, Boolean likeValue);
}
