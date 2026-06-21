package com.helirfid;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NfcBackgroundMonitorActivity extends BaseNfcActivity {

    TextView txtStatus, txtLog, txtTodayCount;
    Button btnStart, btnStop, btnOpenLogs, btnClear;
    boolean isMonitoring = false;
    int todayCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setUseReaderMode(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_background_monitor);

        txtStatus = findViewById(R.id.txtBgStatus);
        txtLog = findViewById(R.id.txtBgLog);
        txtTodayCount = findViewById(R.id.txtBgTodayCount);
        btnStart = findViewById(R.id.btnBgStart);
        btnStop = findViewById(R.id.btnBgStop);
        btnOpenLogs = findViewById(R.id.btnBgOpenLogs);
        btnClear = findViewById(R.id.btnBgClear);

        updateStatus("就緒");

        btnStart.setOnClickListener(v -> startMonitoring());
        btnStop.setOnClickListener(v -> stopMonitoring());
        btnClear.setOnClickListener(v -> txtLog.setText(""));

        btnOpenLogs.setOnClickListener(v -> {
            File dir = new File(getExternalFilesDir(null), "nfc_logs");
            if (!dir.exists()) dir.mkdirs();
            Toast.makeText(this, "日誌目錄: " + dir.getAbsolutePath(), Toast.LENGTH_LONG).show();
        });
    }

    private void startMonitoring() {
        isMonitoring = true;
        Intent serviceIntent = new Intent(this, NfcBackgroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        btnStart.setEnabled(false);
        btnStop.setEnabled(true);
        updateStatus("監控中 (Reader Mode)");
        appendLog("=== NFC 背景監控已啟動 ===");
    }

    private void stopMonitoring() {
        isMonitoring = false;
        Intent serviceIntent = new Intent(this, NfcBackgroundService.class);
        serviceIntent.setAction("STOP");
        startService(serviceIntent);
        btnStart.setEnabled(true);
        btnStop.setEnabled(false);
        updateStatus("已停止");
        appendLog("=== NFC 背景監控已停止 ===");
    }

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        if (!isMonitoring) return;
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) return;

        String uid = Converter.hex(tag.getId());
        todayCount++;

        StringBuilder techs = new StringBuilder();
        for (String t : tag.getTechList()) {
            techs.append(t.substring(t.lastIndexOf('.') + 1)).append(" ");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== NFC 標籤感應 ===\n");
        sb.append("UID: ").append(uid).append("\n");
        sb.append("技術: ").append(techs).append("\n");

        String ndefInfo = NFCReader.readNdefMessage(intent);
        if (ndefInfo != null && !ndefInfo.isEmpty()) {
            sb.append("NDEF: ").append(ndefInfo).append("\n");
        }

        NfcBackgroundService.saveLogToCsv(this, uid,
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

        appendLog(sb.toString().trim());
        txtTodayCount.setText("今日感應次數: " + todayCount);
        vibrate();
    }

    private void appendLog(String msg) {
        String prev = txtLog.getText().toString();
        txtLog.setText(msg + "\n" + prev);
        if (txtLog.getLineCount() > 100) {
            String[] lines = txtLog.getText().toString().split("\n", 50);
            txtLog.setText(lines.length > 1 ? lines[lines.length - 1] : "");
        }
    }

    private void updateStatus(String s) {
        txtStatus.setText("狀態: " + s);
    }

    @Override
    protected void onDestroy() {
        if (isMonitoring) stopMonitoring();
        super.onDestroy();
    }
}
