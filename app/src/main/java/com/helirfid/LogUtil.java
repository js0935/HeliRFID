/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.0.1
 */
package com.helirfid;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LogUtil {

    private static final int MAX_LOG = 500;
    private static final List<String> logs = new ArrayList<>();

    public static void i(String tag, String msg) {
        add("INFO", tag, msg);
    }

    public static void w(String tag, String msg) {
        add("WARN", tag, msg);
    }

    public static void e(String tag, String msg) {
        add("ERROR", tag, msg);
    }

    private static void add(String level, String tag, String msg) {
        String time = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
                .format(new Date());
        String line = "[" + time + "][" + level + "][" + tag + "] " + msg;
        synchronized (logs) {
            logs.add(0, line);
            if (logs.size() > MAX_LOG) logs.remove(logs.size() - 1);
        }
        android.util.Log.i("HeliRFID_" + tag, msg);
    }

    public static List<String> getLogs() {
        synchronized (logs) {
            return new ArrayList<>(logs);
        }
    }

    public static void clear() {
        synchronized (logs) {
            logs.clear();
        }
    }
}
