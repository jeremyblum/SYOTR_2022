package edu.psu.jjb24.syotr.db;

import android.app.Application;

import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class WaterViewModel extends AndroidViewModel {

    private LiveData<List<PlayspotStatus>> playspots;

    public WaterViewModel (Application application) {
        super(application);

        playspots = WaterDatabase.getDatabase(getApplication()).waterDAO().getAll();
    }

    public void filterSpots(boolean onlyLiked) {
        if (onlyLiked)
            playspots = WaterDatabase.getDatabase(getApplication()).waterDAO().getLiked();
        else
            playspots = WaterDatabase.getDatabase(getApplication()).waterDAO().getAll();
    }

    public LiveData<List<PlayspotStatus>> getAll() {
        return playspots;
    }
}
