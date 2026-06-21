package com.helirfid;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class PlaceholderEngine {

    private static final String PREF_NAME = "placeholder_engine";
    private static int counter = -1;

    public static String resolve(String input, Context context, String tagUid) {
        if (input == null) return "";
        String result = input;
        result = result.replace("%TIME%", new SimpleDateFormat("HH:mm:ss", Locale.TAIWAN).format(new Date()));
        result = result.replace("%DATE%", new SimpleDateFormat("yyyy-MM-dd", Locale.TAIWAN).format(new Date()));
        result = result.replace("%DATETIME%", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN).format(new Date()));
        result = result.replace("%UID%", tagUid != null ? tagUid : "????????");
        result = result.replace("%RND%", String.valueOf((int) (Math.random() * 1000000)));
        result = result.replace("%UUID%", UUID.randomUUID().toString().substring(0, 8));
        result = result.replace("%COUNT%", String.valueOf(getNextCounter(context)));
        result = result.replace("%HOUR%", new SimpleDateFormat("HH", Locale.TAIWAN).format(new Date()));
        result = result.replace("%MINUTE%", new SimpleDateFormat("mm", Locale.TAIWAN).format(new Date()));
        result = result.replace("%DAY%", new SimpleDateFormat("dd", Locale.TAIWAN).format(new Date()));
        result = result.replace("%MONTH%", new SimpleDateFormat("MM", Locale.TAIWAN).format(new Date()));
        result = result.replace("%YEAR%", new SimpleDateFormat("yyyy", Locale.TAIWAN).format(new Date()));
        result = result.replace("%WEEKDAY%", new SimpleDateFormat("EEEE", Locale.TAIWAN).format(new Date()));
        result = result.replace("%BATTERY%", getBatteryLevel(context));
        result = result.replace("%NL%", "\n");
        return result;
    }

    private static int getNextCounter(Context context) {
        if (counter < 0) {
            counter = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getInt("counter", 0);
        }
        counter++;
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putInt("counter", counter).apply();
        return counter;
    }

    public static void resetCounter(Context context) {
        counter = 0;
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putInt("counter", 0).apply();
    }

    private static String getBatteryLevel(Context context) {
        try {
            android.content.IntentFilter ifilter = new android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED);
            android.content.Intent batteryStatus = context.registerReceiver(null, ifilter);
            if (batteryStatus != null) {
                int level = batteryStatus.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatus.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1);
                if (level >= 0 && scale > 0) return String.valueOf(level * 100 / scale);
            }
        } catch (Exception ignored) {}
        return "?";
    }
}
