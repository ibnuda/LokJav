package com.ijauradunbi.pegel.lokjav.lok;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.support.annotation.StringDef;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private String defaultUploadSite = "http://pancanaka.net/gpstracker/updatelocation.php";//getString(R.string.default_upload_site);

    private static EditText etUsername;
    private static Button buttTracking;

    private boolean trackingNow;
    private RadioGroup radioGroupInterval;
    private int interval;
    private AlarmManager alarmManager;
    private Intent intentTrack;
    private PendingIntent intentPending;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etUsername = (EditText) findViewById(R.id.edit_text_username);
        radioGroupInterval = (RadioGroup) findViewById(R.id.rg_interval);
        buttTracking = (Button) findViewById(R.id.butt_track);

        etUsername.setImeOptions(EditorInfo.IME_ACTION_DONE);

        SharedPreferences prefs = this.getSharedPreferences("lokjav", Context.MODE_PRIVATE);
        trackingNow = prefs.getBoolean("trackingNow", false);
        boolean firstTime = prefs.getBoolean("firstTime", true);

        if (firstTime) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("firstTime", false);
            editor.putString("appID", UUID.randomUUID().toString());
            editor.apply();
        }

        radioGroupInterval.setOnCheckedChangeListener(
                new RadioGroup.OnCheckedChangeListener(){
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int i){
                        saveInterval();
                    }
                });

        buttTracking.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                trackLocation(view);
            }
        });
    }

    private void saveInterval() {
        Log.d(TAG, "saveInternal");
        if (trackingNow)
            Toast.makeText(getApplicationContext(), R.string.harus_restart, Toast.LENGTH_LONG).show();

        SharedPreferences prefs = this.getSharedPreferences("lokjav", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        if (radioGroupInterval.getCheckedRadioButtonId() == R.id.butt_1_menit) editor.putInt("interval", 1);
        if (radioGroupInterval.getCheckedRadioButtonId() == R.id.butt_3_menit) editor.putInt("interval", 3);
        if (radioGroupInterval.getCheckedRadioButtonId() == R.id.butt_5_menit) editor.putInt("interval", 5);

        editor.apply();
    }

    private void cancelAlarmManager() {
        Log.d(TAG, "cancelAlarmManager");

        Context context = getBaseContext();
        Intent trackIntent = new Intent(context, LokAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, trackIntent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    private void startAlarmManager() {
        Log.d(TAG, "startAlarmManager");

        Context context = getBaseContext();
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        intentTrack = new Intent(context, LokAlarmReceiver.class);
        intentPending = PendingIntent.getBroadcast(context, 0, intentTrack, 0);

        SharedPreferences preferences = this.getSharedPreferences("lokjav", Context.MODE_PRIVATE);
        interval = preferences.getInt("interval", 1);

        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), interval * 10000, intentPending);
    }

    private boolean saveSettings(){
        if (pakeSpasi()) return false;
        SharedPreferences preferences = this.getSharedPreferences("lokjav", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        if (radioGroupInterval.getCheckedRadioButtonId() == R.id.butt_1_menit) editor.putInt("interval", 1);
        if (radioGroupInterval.getCheckedRadioButtonId() == R.id.butt_3_menit) editor.putInt("interval", 3);
        if (radioGroupInterval.getCheckedRadioButtonId() == R.id.butt_5_menit) editor.putInt("interval", 5);

        editor.putString("username", etUsername.getText().toString().trim());
        editor.putString("defaultUploadSite", defaultUploadSite);

        editor.apply();

        return true;
    }

    private void displaySettings(){
        SharedPreferences preferences = this.getSharedPreferences("lokjav", MODE_PRIVATE);
        interval = preferences.getInt("interval", 1);
        if (interval == 1) radioGroupInterval.check(R.id.butt_1_menit);
        if (interval == 3) radioGroupInterval.check(R.id.butt_3_menit);
        if (interval == 5) radioGroupInterval.check(R.id.butt_5_menit);

        etUsername.setText(preferences.getString("username", ""));
    }

    private boolean pakeSpasi() {
        String username = etUsername.getText().toString().trim();
        return (username.length() < 1) || (username.split(" ").length > 1);
    }

    protected void trackLocation(View v){
        SharedPreferences preferences= this.getSharedPreferences("lokjav", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        if (!saveSettings()) return;
        if (!checkGooglePlayEnabled()) return;

        if (trackingNow){
            cancelAlarmManager();
            trackingNow =false;
            editor.putBoolean("trackingNow", trackingNow);
            editor.putString("sessionID", "");
        } else {
            startAlarmManager();
            trackingNow = true;
            editor.putBoolean("trackingNow", trackingNow);
            editor.putFloat("totalDistance", 0f);
            editor.putBoolean("firstTime", false);
            editor.putString("sessionID", UUID.randomUUID().toString());
        }
        editor.apply();
        setTrackButton();
    }

    private void setTrackButton() {
        if (trackingNow) buttTracking.setText(R.string.track_on);
        else buttTracking.setText(R.string.track_off);
    }

    private boolean checkGooglePlayEnabled(){
        return GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
