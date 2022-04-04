package edu.psu.jjb24.syotr.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName="gauge")
public class Gauge {

    public Gauge(@NonNull String site, String measureDate, Double latitude,
                 Double longitude, Double heightFt, Double flowCFPS,
                 Double tempC) {
        this.site = site;
        this.measureDate = measureDate;
        this.latitude = latitude;
        this.longitude = longitude;
        this.heightFt = heightFt;
        this.flowCFPS = flowCFPS;
        this.tempC = tempC;
    }

    @PrimaryKey
    @ColumnInfo(name = "site")
    @NonNull
    public String site;

    @ColumnInfo(name = "measureDate")
    public String measureDate;

    @ColumnInfo(name = "latitude")
    public Double latitude;

    @ColumnInfo(name = "longitude")
    public Double longitude;

    @ColumnInfo(name = "heightFt")
    public Double heightFt;

    @ColumnInfo(name = "flowCFPS")
    public Double flowCFPS;

    @ColumnInfo(name = "tempC")
    public Double tempC;
}
