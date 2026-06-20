/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.0.1
 */
package com.helirfid;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.*;

public class HistoryManager {

    private static final int MAX_SIZE = 200;
    SharedPreferences prefs;

    public HistoryManager(Context c){
        prefs=c.getSharedPreferences("history",Context.MODE_PRIVATE);
    }

    public void add(String card10, String card8, String uid) {
        List<HistoryEntry> list = getEntries();

        String uidClean = uid != null ? uid.replace("UID: ", "").trim() : "";
        long now = System.currentTimeMillis();
        HistoryEntry entry = new HistoryEntry(card10, card8, uidClean, now);

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getCard10().equals(card10)) {
                list.remove(i);
                break;
            }
        }

        list.add(0, entry);

        while (list.size() > MAX_SIZE) {
            list.remove(list.size() - 1);
        }

        save(list);
    }

    public List<HistoryEntry> getEntries() {
        String s = prefs.getString("data", "");
        List<HistoryEntry> list = new ArrayList<>();
        if (!s.isEmpty()) {
            if (s.contains("|")) {
                for (String item : s.split(",")) {
                    HistoryEntry entry = HistoryEntry.fromStorageString(item);
                    if (entry != null) list.add(entry);
                }
            } else {
                for (String c : s.split(",")) {
                    if (!c.isEmpty()) {
                        list.add(new HistoryEntry(c, "", "", System.currentTimeMillis()));
                    }
                }
            }
        }
        return list;
    }

    public List<String> get() {
        List<HistoryEntry> entries = getEntries();
        List<String> result = new ArrayList<>();
        for (HistoryEntry e : entries) {
            result.add(e.toStorageString());
        }
        return result;
    }

    public void clear(){
        prefs.edit().clear().apply();
    }

    private void save(List<HistoryEntry> list) {
        List<String> strings = new ArrayList<>();
        for (HistoryEntry e : list) {
            strings.add(e.toStorageString());
        }
        prefs.edit().putString("data", String.join(",", strings)).apply();
    }
}
