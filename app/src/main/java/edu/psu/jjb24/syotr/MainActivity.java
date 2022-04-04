package edu.psu.jjb24.syotr;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import edu.psu.jjb24.syotr.db.Gauge;
import edu.psu.jjb24.syotr.db.PlayspotStatus;
import edu.psu.jjb24.syotr.db.WaterDatabase;
import edu.psu.jjb24.syotr.db.WaterViewModel;

public class MainActivity extends AppCompatActivity {
    private boolean filtered = false;
    private WaterViewModel waterViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState != null) {
            filtered = savedInstanceState.getBoolean("filtered");
        }

        setSupportActionBar(findViewById(R.id.toolbar));

        RecyclerView recyclerView = findViewById(R.id.lstPlayspots);
        ListAdapter adapter = new ListAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        waterViewModel = new ViewModelProvider(this).get(WaterViewModel.class);
        waterViewModel.getAll().observe(this, adapter::setData);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);
        if (filtered) {
            menu.getItem(1).setIcon(R.drawable.ic_filter);
        } else {
            menu.getItem(1).setIcon(R.drawable.ic_clear_filter);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("Http-JSON", "" + item.getItemId());
        Log.d("Http-JSON", "" + R.id.menu_refresh);
        if (item.getItemId() == R.id.menu_refresh) {
            getLatestData();
            return true;
        }
        else if (item.getItemId() == R.id.menu_filter) {
            filtered = !filtered;
            if (filtered) {
                item.setIcon(R.drawable.ic_filter);
            } else {
                item.setIcon(R.drawable.ic_clear_filter);
            }
            RecyclerView recyclerView = findViewById(R.id.lstPlayspots);
            ListAdapter adapter = new ListAdapter(this);
            recyclerView.setAdapter(adapter);
            waterViewModel.filterSpots(filtered);
            waterViewModel.getAll().observe(this, adapter::setData);
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

    public void getLatestData() {
        Log.d("JSON-HTTP", "retrieving gauges ");
        WaterDatabase.getGauges(gauges -> {
            for (Gauge gauge: gauges) {
                Log.d("JSON-HTTP", "retrieving gauge " + gauge.site);
                getSiteData(gauge);
            }
        });
    }

    private void getSiteData(Gauge gauge) {
        Uri.Builder builder = new Uri.Builder() ;
        builder.scheme("https")
                .authority("waterservices.usgs.gov")
                .appendPath("nwis")
                .appendPath("iv")
                .appendQueryParameter("format","json")
                .appendQueryParameter("sites",gauge.site)
                .appendQueryParameter("parameterCd","00010,00060,00065");
        String url = builder.build().toString();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                url,null,
                response -> {
                    try {
                        gauge.measureDate = response.getJSONObject("value").getJSONArray("timeSeries").getJSONObject(0).getJSONArray("values").getJSONObject(0).getJSONArray("value").getJSONObject(0).getString("dateTime");
                        gauge.latitude = Double.valueOf(response.getJSONObject("value").getJSONArray("timeSeries").getJSONObject(0).getJSONObject("sourceInfo").getJSONObject("geoLocation").getJSONObject("geogLocation").getString("latitude"));
                        gauge.longitude = Double.valueOf(response.getJSONObject("value").getJSONArray("timeSeries").getJSONObject(0).getJSONObject("sourceInfo").getJSONObject("geoLocation").getJSONObject("geogLocation").getString("longitude"));
                        gauge.heightFt = Double.valueOf(response.getJSONObject("value").getJSONArray("timeSeries").getJSONObject(2).getJSONArray("values").getJSONObject(0).getJSONArray("value").getJSONObject(0).getString("value"));
                        gauge.flowCFPS = Double.valueOf(response.getJSONObject("value").getJSONArray("timeSeries").getJSONObject(1).getJSONArray("values").getJSONObject(0).getJSONArray("value").getJSONObject(0).getString("value"));
                        gauge.tempC = Double.valueOf(response.getJSONObject("value").getJSONArray("timeSeries").getJSONObject(0).getJSONArray("values").getJSONObject(0).getJSONArray("value").getJSONObject(0).getString("value"));
                        WaterDatabase.update(gauge);
                    }
                    catch (JSONException je) {
                        Log.d("JSON-Http-Request", "ERROR: " + je.getMessage());
                    }
                },
                error -> {
                    Log.d("JSON-Http-Request", "ERROR: " + error.getMessage());
                });

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    /**************************************************************************************
     * Adapter for the RecyclerView List
     **************************************************************************************/
    public static class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {
        // If the JokeListAdapter were an outer class, the JokeViewHolder could be
        // a static class.  We want to be able to get access to the MainActivity instance,
        // so we want it to be an inner class
        static class ViewHolder extends RecyclerView.ViewHolder {
            private final View viewGroup;
            private final TextView txtPlayspot;
            private final TextView txtLevel;
            private final TextView txtTemp;
            private final TextView txtRiver;
            private final ImageView likedView;
            private PlayspotStatus playspot;

            private ViewHolder(View itemView) {
                super(itemView);
                viewGroup = itemView;
                txtPlayspot = itemView.findViewById(R.id.txtPlayspot);
                txtLevel = itemView.findViewById(R.id.txtLevel);
                txtTemp = itemView.findViewById(R.id.txtTemp);
                txtRiver = itemView.findViewById(R.id.txtRiver);
                likedView = itemView.findViewById(R.id.imgLiked);

                likedView.setOnClickListener(view -> {
                    if (playspot.liked == null) playspot.liked = true;
                    else if (playspot.liked) playspot.liked = false;
                    else playspot.liked = null;
                    WaterDatabase.update(playspot.rowid, playspot.liked);
                });
            }
        }

        private final LayoutInflater layoutInflater;
        private List<PlayspotStatus> playspots;

        ListAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.list_item, parent, false);
            return new ViewHolder(itemView);
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (playspots != null) {
                PlayspotStatus current = playspots.get(position);
                holder.playspot = current;
                holder.txtPlayspot.setText(current.playspot);
                holder.txtLevel.setText(String.format("Current Level: %.2f    Ideal Level: %.1f\nRange: %.1f-%.1f ",
                        current.heightFt, current.idealLevel, current.lowLevel, current.highLevel));
                holder.txtTemp.setText(String.format("Temperature %.1f C",current.tempC));
                holder.txtRiver.setText(current.river);

                if (current.ideal != null && current.ideal) {
                    holder.viewGroup.setBackgroundColor(0xFF80C080);
                }
                else if (current.inPlay != null && current.inPlay) {
                    holder.viewGroup.setBackgroundColor(0xFFAFFFAF);
                }
                else {
                    holder.viewGroup.setBackgroundColor(Color.LTGRAY);
                }

                if (current.liked == null) {
                    holder.likedView.setImageResource(R.drawable.ic_question);
                }
                else if (current.liked) {
                    holder.likedView.setImageResource(R.drawable.ic_thumbs_up);
                }
                else {
                    holder.likedView.setImageResource(R.drawable.ic_thumbs_down);
                }
            } else {
                // Covers the case of data not being ready yet.
                holder.playspot = null;
                holder.txtPlayspot.setText("... initializing ...");
                holder.txtLevel.setText("");
                holder.txtTemp.setText("");
                holder.txtRiver.setText("");
                holder.likedView.setImageResource(R.drawable.ic_question);
            }
        }

        void setData(List<PlayspotStatus> playspots){
            this.playspots = playspots;
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            if (playspots != null)
                return playspots.size();
            else return 0;
        }
    }


}
