package com.helirfid;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

public class ReaderModeActivity extends BaseNfcActivity {

    private TextView txtStatus, txtResult;
    private Button btnClear;
    private int scanCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setUseReaderMode(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader_mode);

        txtStatus = findViewById(R.id.txtReaderStatus);
        txtResult = findViewById(R.id.txtReaderResult);
        btnClear = findViewById(R.id.btnReaderClear);

        btnClear.setOnClickListener(v -> {
            scanCount = 0;
            txtResult.setText("");
            txtStatus.setText("已清除記錄，等待卡片...");
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) return;

        scanCount++;
        vibrate();

        StringBuilder sb = new StringBuilder();
        sb.append("=== 掃描 #").append(scanCount).append(" ===\n");
        sb.append("時間: ").append(java.text.SimpleDateFormat.getTimeInstance
                (java.text.SimpleDateFormat.MEDIUM).format(new java.util.Date())).append("\n");
        sb.append("UID: ").append(Converter.hex(tag.getId())).append("\n");
        sb.append("技術: ");
        for (String t : tag.getTechList()) {
            String name = t.substring(t.lastIndexOf('.') + 1);
            sb.append(name).append(", ");
        }
        sb.setLength(sb.length() - 2);
        sb.append("\n");

        try {
            NfcA nfca = NfcA.get(tag);
            if (nfca != null) {
                nfca.connect();
                sb.append("ATQA: ").append(String.format("%04X", nfca.getAtqa())).append("\n");
                sb.append("SAK: ").append(String.format("%02X", nfca.getSak())).append("\n");
                nfca.close();
            }
        } catch (IOException ignored) { }

        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                sb.append("NDEF 可用, 最大 ").append(ndef.getMaxSize()).append(" bytes\n");
            }
        } catch (Exception ignored) { }

        sb.append("---\n");
        String entry = sb.toString();

        String prev = txtResult.getText().toString();
        txtResult.setText(entry + prev);
        txtStatus.setText("已掃描 " + scanCount + " 張卡片 (Reader Mode)");
    }
}
