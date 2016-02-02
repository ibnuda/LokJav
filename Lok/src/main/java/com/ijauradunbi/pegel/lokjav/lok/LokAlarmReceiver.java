package com.ijauradunbi.pegel.lokjav.lok;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class LokAlarmReceiver extends WakefulBroadcastReceiver {
    public LokAlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, LokService.class));
    }
}
