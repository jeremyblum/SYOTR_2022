package edu.psu.jjb24.syotr;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class WhiteWaterDB extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "kayak.db";

    private static WhiteWaterDB theDb;

    private WhiteWaterDB(Context context) {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    public static synchronized WhiteWaterDB getInstance(Context context) {
        if (theDb == null) {
            // Make sure that we do not leak Activity's context
            theDb = new WhiteWaterDB(context.getApplicationContext());
        }

        return theDb;
    }

    private static final String[] SQL_CREATE_TABLES = {
            "CREATE TABLE playspots (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "playspot TEXT, " +
                    "river TEXT, " +
                    "latitude REAL," +
                    "longitude REAL," +
                    "lowLevel TEXT," +
                    "idealLevel TEXT," +
                    "highLevel TEXT," +
                    "gaugeSite TEXT, " +
                    "liked TEXT)",
            "CREATE TABLE gauges (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "site TEXT UNIQUE," +
                    "measureDate TEXT," +
                    "latitude REAL," +
                    "longitude REAL," +
                    "heightFt REAL," +
                    "flowCFPS REAL," +
                    "tempC REAL)",
            "CREATE VIEW IF NOT EXISTS playspotGauge AS SELECT playspots._id as _id, playspot, " +
                    "river, lowLevel, idealLevel, highLevel, tempC, heightFt, " +
                    "abs(heightFt - idealLevel) < 0.05 as ideal, (heightFt <= highLevel AND heightFt >= lowLevel) as inPlay, liked " +
                    " FROM playspots INNER JOIN gauges on (playspots.gaugeSite = gauges.site)"
    };

    private static final String[] SQL_DELETE_TABLES = {
            "DROP TABLE IF EXISTS playspots",
            "DROP VIEW IF EXISTS playspotGauge",
            "DROP TABLE IF EXISTS gauges"};

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (String command: SQL_CREATE_TABLES) {
            db.execSQL(command);
        }

        String[] playspots = {"Wet Bottom","Horseshoe Wave", "Maryland Chute", "Virginia Chute", "Rocky Island Waves", "Center Chute Wave", "Sweetie-pie Wave", "Offut Island Waves", "Skull Island Wave"};
        double[] lowLevels = {2.5,2.6,2.7,2.7,3.9,5.4,7,4.2,6};
        double[] idealLevels = {4,2.8,3.7,3.9,4.2,5.9,7.1,4.9,6.3};
        double[] highLevels = {4.2,2.8,4,4.2,4.8,6.2,7.5,5.4,7.6};

        db.beginTransaction();
        ContentValues values = new ContentValues();
        values.put("liked","?");
        for (int i = 0; i < playspots.length; i++) {
            values.put("playspot", playspots[i]);
            values.put("river", "Potomac: Mather Gorge");
            values.put("lowLevel", lowLevels[i]);
            values.put("idealLevel", idealLevels[i]);
            values.put("highLevel", highLevels[i]);
            values.put("gaugeSite", "01646500");
            values.put("liked", "?");
            db.insert("playspots", null, values);
        }
        values.clear();
        values.put("site", "01646500");
        db.insert("gauges", null, values);

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (String command: SQL_DELETE_TABLES) {
            db.execSQL(command);
        }
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}
