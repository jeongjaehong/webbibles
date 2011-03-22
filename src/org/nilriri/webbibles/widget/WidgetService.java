package org.nilriri.webbibles.widget;

import org.nilriri.webbibles.com.Common;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

public class WidgetService extends android.app.Service {

    protected AlarmManager alarm;
    protected PendingIntent alarmOperation;
    protected PendingIntent alarmAnimationOperation;
    protected int alarmInterval;
    protected int animation;
    protected boolean running;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(Common.TAG, "WidgetService.onCreate()");

        alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, WidgetService.class);
        intent.setAction(Common.ACTION_REFRESH);

        alarmOperation = PendingIntent.getService(this, 0, intent, 0);

        doAlarmStart();

        Intent intentAnimation = new Intent(this, WidgetService.class);

        alarmAnimationOperation = PendingIntent.getService(this, 0, intentAnimation, 0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(Common.TAG, "WidgetService.onDestroy()");

        doAlarmStop();
    }

    @Override
    public void onStart(final Intent intent, int startId) {
        super.onStart(intent, startId);

        Log.d(Common.TAG, "WidgetService.onStart(" + intent + ", " + startId + ")");

        String action = intent.getAction();

        if (Common.ACTION_ALARM_START.equals(action)) {
            doAlarmStart();
            return;
        }
        if (Common.ACTION_ALARM_STOP.equals(action)) {
            doAlarmStop();
            return;
        }
        if (Common.ACTION_REFRESH.equals(action)) {
            if (running)
                return;

            new Thread(new Runnable() {

                public void run() {
                    doRefresh();
                }
            }).start();
            return;
        }
        if (Common.ACTION_UPDATE.equals(action)) {
            Common.sendWidgetUpdate(this);
            return;
        }

        return;
    }

    protected synchronized void doRefresh() {
        Log.d(Common.TAG, "Service.doRefresh() - begin");

        //StringBuffer exception = new StringBuffer();

        running = true;

        sendBroadcast(new Intent(Common.ACTION_REFRESH_START));

        Intent startWidget2 = new Intent(this, AppWidgetProvider4x2.class);
        startWidget2.setAction(Common.ACTION_REFRESH_START);
        sendBroadcast(startWidget2);

        Common.sendRefreshFinish(this);

        running = false;

        Log.d(Common.TAG, "Service.doRefresh() - end");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected void doAlarmStart() {

        alarm.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), 10 * 60 * 1000, alarmOperation);

    }

    protected void doAlarmStop() {

        alarm.cancel(alarmOperation);

    }

}
