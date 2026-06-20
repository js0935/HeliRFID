package com.helirfid;

import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NfcPollerActivity extends AppCompatActivity {

    TextView txtResult;
    Button btnStart, btnStop, btnClear;
    NfcAdapter nfcAdapter;
    boolean polling = false;
    List<String> log = new ArrayList<>();
    int scanCount = 0;
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_poller);

        txtResult = findViewById(R.id.txtPollerResult);
        btnStart = findViewById(R.id.btnPollerStart);
        btnStop = findViewById(R.id.btnPollerStop);
        btnClear = findViewById(R.id.btnPollerClear);

        NfcManager nfcManager = (NfcManager) getSystemService(NFC_SERVICE);
        nfcAdapter = nfcManager.getDefaultAdapter();

        btnStart.setOnClickListener(v -> {
            polling = true;
            log.add("=== 輪詢開始 ===");
            updateDisplay();
            btnStart.setEnabled(false);
            btnStop.setEnabled(true);
        });

        btnStop.setOnClickListener(v -> {
            polling = false;
            log.add("=== 輪詢結束 ===");
            updateDisplay();
            btnStart.setEnabled(true);
            btnStop.setEnabled(false);
        });

        btnClear.setOnClickListener(v -> {
            log.clear();
            scanCount = 0;
            txtResult.setText("");
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            android.app.PendingIntent pi = android.app.PendingIntent.getActivity(this, 0,
                    new android.content.Intent(this, getClass())
                            .addFlags(android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP),
                    android.app.PendingIntent.FLAG_MUTABLE);
            nfcAdapter.enableForegroundDispatch(this, pi, null, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        if (!polling) return;

        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) {
                scanCount++;
                byte[] uid = tag.getId();
                StringBuilder uidStr = new StringBuilder();
                for (byte b : uid) uidStr.append(String.format("%02X ", b));

                String ts = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(new Date());
                String[] techs = tag.getTechList();
                String techStr = techs.length > 0 ? techs[0].replace("android.nfc.tech.", "") : "未知";

                log.add(String.format("#%d [%s] UID: %s (%s)", scanCount, ts, uidStr.toString().trim(), techStr));

                if (log.size() > 500) {
                    log = log.subList(log.size() - 500, log.size());
                }

                updateDisplay();
            }
        }
    }

    private void updateDisplay() {
        StringBuilder sb = new StringBuilder();
        sb.append("NFC 輪詢模式\n\n");
        sb.append("狀態: ").append(polling ? "● 執行中" : "○ 已停止").append("\n");
        sb.append("掃描次數: ").append(scanCount).append("\n");
        sb.append("日誌筆數: ").append(log.size()).append("\n\n");

        int start = Math.max(0, log.size() - 50);
        for (int i = start; i < log.size(); i++) {
            sb.append(log.get(i)).append("\n");
        }

        txtResult.setText(sb.toString());
    }
}
