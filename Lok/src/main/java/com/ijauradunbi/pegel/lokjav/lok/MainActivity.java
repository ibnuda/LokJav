package com.ijauradunbi.pegel.lokjav.lok;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.StringDef;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private String defaultUploadSite = getString(R.string.default_upload_site);

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

    private void startAlarmManager() {
        Log.d(TAG, "startAlarmManager");

        Context context = getBaseContext();
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // intentTrack = new Intent(context, )
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
