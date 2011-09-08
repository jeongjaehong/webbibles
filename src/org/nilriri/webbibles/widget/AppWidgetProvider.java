package org.nilriri.webbibles.widget;

import org.nilriri.webbibles.BibleMain;
import org.nilriri.webbibles.R;
import org.nilriri.webbibles.com.Common;
import org.nilriri.webbibles.com.Prefs;
import org.nilriri.webbibles.dao.BibleDao;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViews;

public class AppWidgetProvider extends android.appwidget.AppWidgetProvider {

    protected static final int DISPLAY_CONTENT = 1;
    protected static final int DISPLAY_SYNC = 2;
    protected static final int DISPLAY_UPDATE = 3;

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);

        Log.d(Common.TAG, "AppWidgetProvider.onEnabled(" + context + ")");

        Common.sendServiceAlarmStart(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);

        Log.d(Common.TAG, "AppWidgetProvider.onDisabled(" + context + ")");

        Common.sendServiceAlarmStop(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        Log.d(Common.TAG, "AppWidgetProvider.onUpdate(" + context + ", " + appWidgetManager + ", " + appWidgetIds + ")");

        doDisplay(context, DISPLAY_UPDATE);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        Log.d(Common.TAG, "AppWidgetProvider.onReceive(" + context + ", " + intent + ")");

        doDisplay(context, DISPLAY_CONTENT);

    }

    protected void doDisplay(Context context, int display) {
        Log.d(Common.TAG, "AppWidgetProvider.doDisplay(" + context + ", " + display + ")");

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), getWidgetLayout(context));

        BibleDao dao = new BibleDao(context, null, Prefs.getSDCardUse(context));

        String contents = "";

        Cursor cursor = dao.queryRandomFavorites();
        if (cursor.moveToNext()) {
            contents = cursor.getString(2) + "(" + cursor.getString(1) + ")";

        } else {

            contents = "등록된 즐겨찾기 구절이 없습니다.";
        }
        cursor.close();

        //RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget_provider);
        //views.setTextViewText(R.id.appwidget_title, mDday_title);
        //views.setTextViewText(R.id.appwidget_text, mDday_msg);

        remoteViews.setTextViewText(R.id.contents, contents);
        remoteViews.setTextColor(R.id.contents, Prefs.getWFontColor(context));

        //remoteViews.setImageViewResource(R.id.sync, R.drawable.widget_background_orange);

        remoteViews.setOnClickPendingIntent(R.id.widget, PendingIntent.getActivity(context, 0, new Intent(context, BibleMain.class), 0));

        appWidgetManager.updateAppWidget(new ComponentName(context, getWidgetClass()), remoteViews);
    }

    protected int getWidgetSize() {
        throw new Error();
    }

    protected int getWidgetLayout(Context context) {

        switch (getWidgetSize()) {
            case Common.SIZE_4x2:

                if (Prefs.getTheme(context)) {
                    return R.layout.widget4x2_black;
                } else {
                    return R.layout.widget4x2_white;
                }

        }

        throw new Error();
    }

    protected Class<?> getWidgetClass() {
        switch (getWidgetSize()) {
            case Common.SIZE_4x2:
                return AppWidgetProvider4x2.class;
        }

        throw new Error();
    }
}
