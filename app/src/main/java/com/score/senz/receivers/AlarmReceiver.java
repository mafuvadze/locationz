package com.score.senz.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Receive alarm to send ping message to senz service
 */
public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = AlarmReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "rebroadcast alarm");
        Intent alarmIntent = new Intent("PING_ALARM");
        context.sendBroadcast(alarmIntent);
    }
}
