package edu.psu.jjb24.syotr.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName="playspot")
public class Playspot {
    public Playspot(int id, String playspot, String river, double lowLevel, double idealLevel,
                 double highLevel, String gaugeSite, Boolean liked) {
        this.id = id;
        this.playspot = playspot;
        this.river = river;
        this.lowLevel = lowLevel;
        this.idealLevel = idealLevel;
        this.highLevel = highLevel;
        this.gaugeSite = gaugeSite;
        this.liked = liked;
    }

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rowid")
    public int id;

    @ColumnInfo(name = "playspot")
    public String playspot;

    @ColumnInfo(name = "river")
    public String river;

    @ColumnInfo(name = "lowLevel")
    public double lowLevel;

    @ColumnInfo(name = "idealLevel")
    public double idealLevel;

    @ColumnInfo(name = "highLevel")
    public double highLevel;

    @ColumnInfo(name = "gaugeSite")
    public String gaugeSite;

    @ColumnInfo(name = "liked")
    public Boolean liked;

}
