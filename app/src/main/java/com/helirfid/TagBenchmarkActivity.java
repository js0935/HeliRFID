package com.helirfid;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class TagBenchmarkActivity extends BaseNfcActivity {

    private TextView txtStatus, txtResult;
    private Button btnStart;
    private Tag currentTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_benchmark);

        txtStatus = findViewById(R.id.txtBenchStatus);
        txtResult = findViewById(R.id.txtBenchResult);
        btnStart = findViewById(R.id.btnBenchStart);

        btnStart.setOnClickListener(v -> runBenchmark());

        Tag tag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) currentTag = tag;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            currentTag = tag;
            txtStatus.setText("卡片已偵測 (UID: " + Converter.hex(tag.getId()) + ")");
        }
    }

    private void runBenchmark() {
        if (currentTag == null) {
            txtStatus.setText("請先掃描 NFC 卡片");
            return;
        }
        new Thread(() -> {
            StringBuilder sb = new StringBuilder();
            sb.append("=== 效能測試 ===\n\n");

            NfcA nfca = NfcA.get(currentTag);
            if (nfca != null) {
                try {
                    nfca.connect();
                    sb.append("NfcA 連線: OK\n");
                    long t0 = System.nanoTime();
                    int n = 50;
                    for (int i = 0; i < n; i++) {
                        nfca.transceive(new byte[]{0x00});
                    }
                    long t1 = System.nanoTime();
                    double avg = (t1 - t0) / (double) n / 1_000_000.0;
                    sb.append(String.format("50次空命令: %.2f ms/次\n", avg));
                    nfca.close();
                } catch (Exception e) {
                    sb.append("NfcA 錯誤: ").append(e.getMessage()).append("\n");
                }
            }

            MifareClassic mfc = MifareClassic.get(currentTag);
            if (mfc != null) {
                try {
                    mfc.connect();
                    sb.append("\nMIFARE Classic:\n");
                    int sectors = mfc.getSectorCount();
                    sb.append("  磁區數: ").append(sectors).append("\n");
                    long t0 = System.nanoTime();
                    int blocks = 0;
                    for (int s = 0; s < sectors && s < 5; s++) {
                        try {
                            mfc.authenticateSectorWithKeyA(s, MifareClassic.KEY_DEFAULT);
                            int bc = mfc.getBlockCountInSector(s);
                            for (int b = 0; b < bc - 1; b++) {
                                mfc.readBlock(mfc.sectorToBlock(s) + b);
                                blocks++;
                            }
                        } catch (Exception ignored) { }
                    }
                    long t1 = System.nanoTime();
                    if (blocks > 0) {
                        double avg = (t1 - t0) / (double) blocks / 1_000_000.0;
                        sb.append(String.format("  讀取 %d blocks: %.2f ms/block\n", blocks, avg));
                    }
                    mfc.close();
                } catch (Exception e) {
                    sb.append("MFC 錯誤: ").append(e.getMessage()).append("\n");
                }
            }

            MifareUltralight mu = MifareUltralight.get(currentTag);
            if (mu != null) {
                try {
                    mu.connect();
                    sb.append("\nMIFARE Ultralight/NTAG:\n");
                    long t0 = System.nanoTime();
                    for (int p = 0; p < 10; p++) {
                        mu.readPages(p);
                    }
                    long t1 = System.nanoTime();
                    double avg = (t1 - t0) / 10.0 / 1_000_000.0;
                    sb.append(String.format("  讀取 10 pages: %.2f ms/page\n", avg));
                    mu.close();
                } catch (Exception e) {
                    sb.append("MUL 錯誤: ").append(e.getMessage()).append("\n");
                }
            }

            Ndef ndef = Ndef.get(currentTag);
            if (ndef != null) {
                try {
                    ndef.connect();
                    sb.append("\nNDEF:\n");
                    sb.append("  最大大小: ").append(ndef.getMaxSize()).append(" bytes\n");
                    long t0 = System.nanoTime();
                    ndef.getNdefMessage();
                    long t1 = System.nanoTime();
                    double avg = (t1 - t0) / 1_000_000.0;
                    sb.append(String.format("  讀取 NDEF: %.2f ms\n", avg));
                    ndef.close();
                } catch (Exception e) {
                    sb.append("NDEF 錯誤: ").append(e.getMessage()).append("\n");
                }
            }

            sb.append("\n=== 測試完成 ===");
            final String res = sb.toString();
            runOnUiThread(() -> txtResult.setText(res));
        }).start();
    }
}
