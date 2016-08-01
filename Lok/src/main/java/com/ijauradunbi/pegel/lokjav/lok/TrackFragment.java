package com.ijauradunbi.pegel.lokjav.lok;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;


public class TrackFragment extends Fragment {
    private static final String TAG = "TrackFragment";
    private String defaultUploadSite = "https://pancanaka.net/gpstracker/updatelocation.php";
    private LinearLayout linearLayout;
    private FragmentActivity fragmentActivity;
    private boolean trackingNow = false;
    private Intent intentTrack;
    private PendingIntent pendingIntent;
    private int interval;

    public TrackFragment() {}

    public static TrackFragment newInstance() {
        TrackFragment trackFragment = new TrackFragment();
        Bundle args = new Bundle();
        trackFragment.setArguments(args);
        return trackFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentActivity = super.getActivity();
        Log.d(TAG, "onCreateView: " + fragmentActivity.toString());
        linearLayout = (LinearLayout) inflater.inflate(R.layout.fragment_track, container, false);
        Log.d(TAG, "onCreateView: " + linearLayout.toString());
        SharedPreferences preferences = fragmentActivity.getSharedPreferences("lokjav", Context.MODE_PRIVATE);
        trackingNow = preferences.getBoolean("trackingNow", false);
        boolean firstTime = preferences.getBoolean("firstTime", false);

        if (firstTime) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("firstTime", false);
            editor.apply();
        }

        return linearLayout;
    }

    private void startAlarmManager() {
        Context context = getContext();
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        intentTrack = new Intent(context, LokAlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(context, 0, intentTrack, 0);
        SharedPreferences sharedPreferences = fragmentActivity.getSharedPreferences("lokjav", Context.MODE_PRIVATE);
        interval = sharedPreferences.getInt("interval", 1);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), interval * 10000, pendingIntent);
    }

    private void trackLocatoin(View view) {
        SharedPreferences sharedPreferences = fragmentActivity.getSharedPreferences("lokjav", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        startAlarmManager();

        editor.apply();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}
