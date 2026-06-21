package com.helirfid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class TagHistoryActivity extends BaseNfcActivity {

    private TextView txtStatus, txtList;
    private Button btnClear, btnExport;
    private static final String PREFS_NAME = "tag_history";
    private static final String KEY_HISTORY = "history";
    private static final String KEY_FAVORITES = "favorites";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_history);

        txtStatus = findViewById(R.id.txtHistStatus);
        txtList = findViewById(R.id.txtHistList);
        btnClear = findViewById(R.id.btnHistClear);
        btnExport = findViewById(R.id.btnHistExport);

        btnClear.setOnClickListener(v -> {
            getPrefs().edit().remove(KEY_HISTORY).apply();
            refreshList();
            txtStatus.setText("記錄已清除");
        });

        btnExport.setOnClickListener(v -> {
            String data = txtList.getText().toString();
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_TEXT, data);
            startActivity(Intent.createChooser(i, "分享歷史記錄"));
        });

        refreshList();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) return;

        String uid = Converter.hex(tag.getId());
        String time = SimpleDateFormat.getDateTimeInstance(
                SimpleDateFormat.SHORT, SimpleDateFormat.MEDIUM).format(new Date());
        String entry = time + " | " + uid;
        addEntry(entry);
        refreshList();
        txtStatus.setText("已記錄: " + uid);
    }

    private SharedPreferences getPrefs() {
        return getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    private void addEntry(String entry) {
        Set<String> set = new HashSet<>(getPrefs().getStringSet(KEY_HISTORY, new HashSet<>()));
        set.add(entry);
        if (set.size() > 200) {
            Set<String> trimmed = new HashSet<>();
            int n = 0;
            for (String s : set) {
                if (n++ >= set.size() - 150) trimmed.add(s);
            }
            set = trimmed;
        }
        getPrefs().edit().putStringSet(KEY_HISTORY, set).apply();
    }

    private void refreshList() {
        Set<String> set = getPrefs().getStringSet(KEY_HISTORY, new HashSet<>());
        if (set.isEmpty()) {
            txtList.setText("尚無記錄\n掃描 NFC 標籤即自動記錄");
            return;
        }
        StringBuilder sb = new StringBuilder("=== 歷史記錄 (").append(set.size()).append(" 張) ===\n");
        Set<String> favs = getPrefs().getStringSet(KEY_FAVORITES, new HashSet<>());
        for (String s : set) {
            String mark = favs.contains(s) ? " ★" : "";
            sb.append(s).append(mark).append("\n");
        }
        txtList.setText(sb.toString());
    }
}
