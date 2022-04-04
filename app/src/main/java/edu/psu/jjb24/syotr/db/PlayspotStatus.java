package edu.psu.jjb24.syotr.db;

public class PlayspotStatus {
    public PlayspotStatus(int rowid, String playspot, String river, Double lowLevel,
                          Double idealLevel, Double highLevel, Double tempC, Double heightFt,
                          Boolean ideal, Boolean inPlay, Boolean liked) {
        this.rowid = rowid;
        this.playspot = playspot;
        this.river = river;
        this.lowLevel = lowLevel;
        this.idealLevel = idealLevel;
        this.highLevel = highLevel;
        this.tempC = tempC;
        this.heightFt = heightFt;
        this.ideal = ideal;
        this.inPlay = inPlay;
        this.liked = liked;
    }

    public int rowid;
    public String playspot;
    public String river;
    public Double lowLevel;
    public Double idealLevel;
    public Double highLevel;
    public Double tempC;
    public Double heightFt;
    public Boolean ideal;
    public Boolean inPlay;
    public Boolean liked;
}
