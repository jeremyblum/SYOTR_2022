package edu.psu.jjb24.syotr;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity  {
    private static final String TAG = "MainActivity";
    SQLiteDatabase theDB;
    private SimpleCursorAdapter mAdapter;
    boolean filtered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initListView();

        new AsyncTask<Void,Void,Void>() {
            @Override
            protected Void doInBackground(Void... args) {
                theDB = WhiteWaterDB.getInstance(MainActivity.this).getWritableDatabase();
                return null;
            }

            @Override
            public void onPostExecute(Void result) {
                reloadCursor();
            }
        }.execute();
    }

    /**
     * Initialize the list view, including setting up the simple cursor adapter,
     * and custom view
     */
    private void initListView() {
        ListView listView = findViewById(R.id.lstPlayspots);

        // Set the SimpleCursorAdapter that will bind views in the listview item to
        // database fields
        mAdapter = new SimpleCursorAdapter(this, R.layout.list_item, null,
                new String[]{"playspot", "river", "liked", "ideal", "heightFt", "tempC"},
                new int[]{R.id.txtPlayspotInList, R.id.txtRiverInList, R.id.imgLikedInList, R.id.listBackground, R.id.txtLevelInList, R.id.txtTempInList}, 0);

        // Customize the way that certain fields are displayed (in particular the text in the row,
        // the liked field as an icon, and whether the current level is within the ideal range)
        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (columnIndex == cursor.getColumnIndex("liked")) {
                    switch (cursor.getString(cursor.getColumnIndex("liked"))) {
                        case "Y":
                            ((ImageView) view).setImageResource(R.drawable.ic_thumbs_up);
                            view.setTag("Y");
                            break;
                        case "N":
                            ((ImageView) view).setImageResource(R.drawable.ic_thumbs_down);
                            view.setTag("N");
                            break;
                        default:
                            ((ImageView) view).setImageResource(R.drawable.ic_question);
                            view.setTag("?");
                    }

                    final long rowid = cursor.getLong(cursor.getColumnIndex("_id"));

                    view.setOnClickListener(new View.OnClickListener() {
                        long _rowid = rowid;

                        public void onClick(View v) {
                            toggleImage(_rowid, (ImageView) v);
                        }
                    });
                    return true;
                } else if (columnIndex == cursor.getColumnIndex("ideal")) {

                    if (cursor.getInt(cursor.getColumnIndex("ideal")) == 1) {
                        view.setBackgroundColor(0xFF80C080);
                    } else if (cursor.getDouble(cursor.getColumnIndex("inPlay")) == 1) {
                        view.setBackgroundColor(0xFFAFFFAF);
                    } else {
                        Log.d(TAG, "Not in play");
                        view.setBackgroundColor(Color.LTGRAY);
                    }
                    return true;
                } else if (columnIndex == cursor.getColumnIndex("heightFt")) {
                    ((TextView) view).setText("Current level " + cursor.getDouble(cursor.getColumnIndex("heightFt")) +
                            "    Ideal Level " + cursor.getDouble(cursor.getColumnIndex("idealLevel")) +
                            "\nRange " + cursor.getDouble(cursor.getColumnIndex("lowLevel")) + " - " +
                            +cursor.getDouble(cursor.getColumnIndex("highLevel")));
                    return true;
                } else if (columnIndex == cursor.getColumnIndex("tempC")) {
                    ((TextView) view).setText("Temperature " + cursor.getDouble(cursor.getColumnIndex("tempC")) + " C");
                    return true;
                }

                return false;
            }
        });
        listView.setAdapter(mAdapter);
    }

    /**
     * Toggle the image in response to a click on the Thumb icon, and update the
     * database
     *
     * @param rowid The _id field for the row that is changed
     * @param imageView The view holding the icon
     */
    private void toggleImage(long rowid, ImageView imageView) {
        String newVal;
        if (imageView.getTag().equals("Y")) {
            newVal = "N";
            imageView.setImageResource(R.drawable.ic_thumbs_down);
        }
        else if (imageView.getTag().equals("N")) {
            newVal = "?";
            imageView.setImageResource(R.drawable.ic_question);
        }
        else {
            newVal = "Y";
            imageView.setImageResource(R.drawable.ic_thumbs_up);
        }
        imageView.setTag(newVal);

        ContentValues values = new ContentValues();
        values.put("liked", newVal);

        try {
            theDB.update("playspots", values, "_id = " + rowid,null);
        }
        catch (SQLException e) {
            Toast.makeText(this, "Error inserting record.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                getLatestData();
                return true;
            case R.id.menu_filter:
                filtered = !filtered;
                reloadCursor();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressLint("StaticFieldLeak")
    public void reloadCursor() {
        AsyncTask<Void,Void,Cursor> asyncTask =
        new AsyncTask<Void,Void,Cursor>() {
            @Override
            public Cursor doInBackground(Void... params) {
                String[] projection = {"_id", "playspot", "river", "lowLevel", "idealLevel", "highLevel", "tempC", "heightFt", "ideal", "inPlay", "liked"};
                Cursor cursor;
                if (!filtered) {
                    cursor = theDB.query("playspotGauge", projection, null, null, null, null, "ideal DESC, inPlay DESC, playspot");
                } else {
                    cursor = theDB.query("playspotGauge", projection, "liked = 'Y'", null, null, null, "ideal DESC, inPlay DESC, playspot");
                }
                return cursor;
            }

            @Override
            public void onPostExecute(Cursor cursor) {
                mAdapter.swapCursor(cursor);
            }
        };

        asyncTask.execute();

    }

    public void getLatestData() {
        new AsyncTask<Void,Void,Void>() {
            @Override
            public Void doInBackground(Void... params) {
                Cursor c;
                try {
                    c = theDB.query("gauges", new String[]{"site"}, null, null, null, null, null);
                } catch (Exception e) {
                    Log.e(TAG, "Error opening database cursor for gauges: " + e.getMessage());
                    return null;
                }

                if (c == null) {
                    Log.e(TAG, "Error opening database cursor for gauges.");
                    return null;
                }

                try {
                    while (c.moveToNext()) {
                        URL url;

                        // Note that this call occurs on a background thread,
                        // but we have implemented the network call in the background
                        // to illustrate how that would be done, even if called from
                        // the UI thread
                        getSiteData(c.getString(0));
                    }
                    c.close();
                } catch (Exception e) {
                    Log.e(TAG, "Error trying to traverse query results: " + e.getMessage());
                }
                reloadCursor();
                return null;
            }
            @Override
            public void onPostExecute(Void result) {
            }
        }.execute();
    }

    private void getSiteData(String siteCode) {
        JSONviaHTTP.QueryStringParams params = new JSONviaHTTP.QueryStringParams();
        params.add("format","json");
        params.add("sites", siteCode);
        params.add("parameterCd","00010,00060,00065");

        new AsyncTask<JSONviaHTTP.QueryStringParams, Void, JSONObject>() {
            public JSONObject doInBackground(JSONviaHTTP.QueryStringParams... params) {
                return JSONviaHTTP.get("GET","https://waterservices.usgs.gov/nwis/iv/", params[0],"");
            }
            public void onPostExecute(JSONObject result) {
                try {
                    String site = result.getJSONObject("value").getJSONArray("timeSeries").getJSONObject(0).getJSONObject("sourceInfo").getJSONArray("siteCode").getJSONObject(0).getString("value");
                    String measureDate = result.getJSONObject("value").getJSONArray("timeSeries").getJSONObject(0).getJSONArray("values").getJSONObject(0).getJSONArray("value").getJSONObject(0).getString("dateTime");
                    double latitude = Double.valueOf(result.getJSONObject("value").getJSONArray("timeSeries").getJSONObject(0).getJSONObject("sourceInfo").getJSONObject("geoLocation").getJSONObject("geogLocation").getString("latitude"));
                    double longitude = Double.valueOf(result.getJSONObject("value").getJSONArray("timeSeries").getJSONObject(0).getJSONObject("sourceInfo").getJSONObject("geoLocation").getJSONObject("geogLocation").getString("longitude"));
                    double tempC = Double.valueOf(result.getJSONObject("value").getJSONArray("timeSeries").getJSONObject(0).getJSONArray("values").getJSONObject(0).getJSONArray("value").getJSONObject(0).getString("value"));
                    double flowCFPS = Double.valueOf(result.getJSONObject("value").getJSONArray("timeSeries").getJSONObject(1).getJSONArray("values").getJSONObject(0).getJSONArray("value").getJSONObject(0).getString("value"));
                    double heightFt = Double.valueOf(result.getJSONObject("value").getJSONArray("timeSeries").getJSONObject(2).getJSONArray("values").getJSONObject(0).getJSONArray("value").getJSONObject(0).getString("value"));

                    ContentValues values = new ContentValues();
                    values.put("measureDate", measureDate);
                    values.put("latitude", latitude);
                    values.put("longitude", longitude);
                    values.put("tempC", tempC);
                    values.put("flowCFPS", flowCFPS);
                    values.put("heightFt", heightFt);

                    theDB.update("gauges", values, "site = ?", new String[]{site});
                } catch (JSONException je) {
                    Log.d(TAG, "Error parsing JSON: " + je.getMessage());
                }
            }
        }.execute(params);
    }

}
