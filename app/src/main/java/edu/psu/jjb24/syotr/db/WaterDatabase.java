package edu.psu.jjb24.syotr.db;

import android.content.Context;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Gauge.class, Playspot.class}, version = 1, exportSchema = false)
public abstract class WaterDatabase extends RoomDatabase {
    public interface OnGaugeResultListener {
        void onResult(List<Gauge> gauges);
    }
    public abstract WaterDAO waterDAO();

    private static WaterDatabase INSTANCE;

    public static WaterDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (WaterDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            WaterDatabase.class, "water_db")
                            .addCallback(createDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private final static RoomDatabase.Callback createDatabaseCallback =
            new RoomDatabase.Callback() {
                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                    super.onCreate(db);
                    for (int i = 0; i < DefaultValues.PLAYSPOTS.length; i++) {
                        insert(new Playspot(0, DefaultValues.PLAYSPOTS[i], "Potomac: Mather Gorge",
                                DefaultValues.LOW_LEVELS[i], DefaultValues.IDEAL_LEVELS[i],DefaultValues.HIGH_LEVELS[i],
                                "01646500",null));
                    }
                    insert(new Gauge("01646500", null,null, null, null, null, null));
            }};

    public static void insert(Gauge gauge) {
        (new Thread(()-> INSTANCE.waterDAO().insert(gauge))).start();
    }

    public static void insert(Playspot playspot) {
        (new Thread(()-> INSTANCE.waterDAO().insert(playspot))).start();
    }

    public static void update(Gauge gauge) {
        (new Thread(()-> INSTANCE.waterDAO().update(gauge))).start();
    }

    public static void update(int rowid, Boolean liked) {
        (new Thread(()-> INSTANCE.waterDAO().update(rowid, liked))).start();
    }

    public static void getGauges(OnGaugeResultListener listener) {
        (new Thread(()-> listener.onResult(INSTANCE.waterDAO().getGauges()))).start();
    }

}
