package com.helirfid;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.widget.RemoteViews;

public class NfcWidgetProvider extends AppWidgetProvider {

    static final String ACTION_TAG_TAPPED = "com.helirfid.WIDGET_TAG_TAPPED";
    static final String ACTION_OPEN_APP = "com.helirfid.WIDGET_OPEN_APP";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_nfc);

            Intent openIntent = new Intent(context, NfcWidgetProvider.class);
            openIntent.setAction(ACTION_OPEN_APP);
            PendingIntent openPi = PendingIntent.getBroadcast(
                    context, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.widget_container, openPi);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_OPEN_APP.equals(intent.getAction())) {
            Intent launch = new Intent(context, ToolsActivity.class);
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(launch);
        }
    }

    public static void updateWidget(Context context, Tag tag) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                new android.content.ComponentName(context, NfcWidgetProvider.class));

        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_nfc);

            if (tag != null) {
                String uid = Converter.hex(tag.getId());
                views.setTextViewText(R.id.widget_text, "UID: " + uid);
                views.setTextViewText(R.id.widget_status, "已感應");
            }

            Intent openIntent = new Intent(context, NfcWidgetProvider.class);
            openIntent.setAction(ACTION_OPEN_APP);
            PendingIntent openPi = PendingIntent.getBroadcast(
                    context, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.widget_container, openPi);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
