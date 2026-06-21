/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.3.1
 */
package com.helirfid;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Locale;

public class SignalStrengthActivity extends BaseNfcActivity {

    private TextView txtStatus, txtResult;
    private ProgressBar signalBar;
    private Button btnStart, btnStop;

    private int totalReads, successReads;
    private long startTime;
    private boolean measuring;

    private final Handler handler = new Handler();
    private static final long MEASURE_DURATION = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setUseReaderMode(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signal_strength);

        txtStatus = findViewById(R.id.txtSignalStatus);
        txtResult = findViewById(R.id.txtSignalResult);
        signalBar = findViewById(R.id.signalBar);
        btnStart = findViewById(R.id.btnSignalStart);
        btnStop = findViewById(R.id.btnSignalStop);

        btnStart.setOnClickListener(v -> startMeasurement());
        btnStop.setOnClickListener(v -> stopMeasurement());
    }

    private void startMeasurement() {
        measuring = true;
        totalReads = 0;
        successReads = 0;
        startTime = System.currentTimeMillis();
        btnStart.setEnabled(false);
        btnStop.setEnabled(true);
        txtStatus.setText("測量中...");
        signalBar.setProgress(0);
        txtResult.setText("");

        handler.postDelayed(() -> {
            if (measuring) stopMeasurement();
        }, MEASURE_DURATION);
    }

    private void stopMeasurement() {
        measuring = false;
        btnStart.setEnabled(true);
        btnStop.setEnabled(false);

        long elapsed = System.currentTimeMillis() - startTime;
        double rate = elapsed > 0 ? (double) successReads / elapsed * 1000 : 0;
        int pct = totalReads > 0 ? (successReads * 100 / totalReads) : 0;

        StringBuilder sb = new StringBuilder();
        sb.append("=== 訊號強度結果 ===\n\n");
        sb.append("測量時間: ").append(elapsed).append(" ms\n");
        sb.append("嘗試次數: ").append(totalReads).append("\n");
        sb.append("成功次數: ").append(successReads).append("\n");
        sb.append("成功率: ").append(pct).append("%\n");
        sb.append("讀取速率: ").append(String.format(Locale.US, "%.1f", rate)).append(" 次/秒\n\n");

        String level;
        if (pct >= 90) level = "● 訊號極強 (靠近卡片即可)";
        else if (pct >= 70) level = "● 訊號良好";
        else if (pct >= 50) level = "● 訊號中等";
        else if (pct >= 30) level = "● 訊號微弱";
        else level = "● 訊號極弱 (請靠近感應區)";
        sb.append(level);

        signalBar.setProgress(pct);
        txtStatus.setText("測量完成 - " + pct + "%");
        txtResult.setText(sb.toString());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (!measuring) return;
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) return;
        totalReads++;
        successReads++;
        vibrate();
        String uid = Converter.hex(tag.getId());
        txtStatus.setText("已讀取 #" + totalReads + " UID: " + uid);
    }

    @Override
    protected void onPause() {
        super.onPause();
        measuring = false;
        handler.removeCallbacksAndMessages(null);
        btnStart.setEnabled(true);
        btnStop.setEnabled(false);
    }
}
