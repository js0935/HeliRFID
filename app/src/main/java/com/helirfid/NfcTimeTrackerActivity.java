/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.3.1
 */
package com.helirfid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class NfcTimeTrackerActivity extends BaseNfcActivity {

    private Button btnClockIn, btnClockOut, btnReportHours;
    private TextView txtTimeTrackerStatus;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setUseReaderMode(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_time_tracker);

        btnClockIn = findViewById(R.id.btnClockIn);
        btnClockOut = findViewById(R.id.btnClockOut);
        btnReportHours = findViewById(R.id.btnReportHours);
        txtTimeTrackerStatus = findViewById(R.id.txtTimeTrackerStatus);
        prefs = getSharedPreferences("nfc_timetrack", MODE_PRIVATE);

        updateStatus();

        btnClockIn.setOnClickListener(v -> {
            TaskExecutor.execute(this, 270, "", "", 0, false);
            prefs.edit().putLong("last_clock_in", System.currentTimeMillis()).apply();
            updateStatus();
        });

        btnClockOut.setOnClickListener(v -> {
            TaskExecutor.execute(this, 271, "", "", 0, false);
            prefs.edit().putLong("last_clock_out", System.currentTimeMillis()).apply();
            updateStatus();
        });

        btnReportHours.setOnClickListener(v -> {
            TaskExecutor.execute(this, 272, "", "", 0, false);
            txtTimeTrackerStatus.setText("工時報表已產生");
        });
    }

    private void updateStatus() {
        long clockIn = prefs.getLong("last_clock_in", -1);
        long clockOut = prefs.getLong("last_clock_out", -1);
        String in = clockIn > 0 ? new java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date(clockIn)) : "無紀錄";
        String out = clockOut > 0 ? new java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date(clockOut)) : "無紀錄";
        txtTimeTrackerStatus.setText("上班: " + in + "\n下班: " + out);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) return;
        txtTimeTrackerStatus.setText("感應到標籤: " + Converter.hex(tag.getId()));
        vibrate();
    }
}
