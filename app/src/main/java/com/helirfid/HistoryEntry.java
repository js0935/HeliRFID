/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.0.1
 */
package com.helirfid;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HistoryEntry {
    private String card10;
    private String card8;
    private String uid;
    private long timestamp;

    public HistoryEntry(String card10, String card8, String uid, long timestamp) {
        this.card10 = card10;
        this.card8 = card8;
        this.uid = uid;
        this.timestamp = timestamp;
    }

    public String getCard10() { return card10; }
    public String getCard8() { return card8; }
    public String getUid() { return uid; }
    public long getTimestamp() { return timestamp; }

    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public String toStorageString() {
        return card10 + "|" + card8 + "|" + uid + "|" + timestamp;
    }

    public static HistoryEntry fromStorageString(String s) {
        String[] parts = s.split("\\|", 4);
        if (parts.length == 4) {
            return new HistoryEntry(parts[0], parts[1], parts[2], Long.parseLong(parts[3]));
        }
        return null;
    }
}
