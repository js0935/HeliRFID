package com.helirfid;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

public class Nfc2QrActivity extends BaseNfcActivity {

    private EditText editNfc2QrData;
    private Button btnNfc2QrGenerate, btnNfc2QrScan;
    private TextView txtNfc2QrStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setUseReaderMode(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc2qr);

        editNfc2QrData = findViewById(R.id.editNfc2QrData);
        btnNfc2QrGenerate = findViewById(R.id.btnNfc2QrGenerate);
        btnNfc2QrScan = findViewById(R.id.btnNfc2QrScan);
        txtNfc2QrStatus = findViewById(R.id.txtNfc2QrStatus);

        btnNfc2QrGenerate.setOnClickListener(v -> {
            String data = editNfc2QrData.getText().toString().trim();
            if (data.isEmpty()) {
                txtNfc2QrStatus.setText("請輸入資料內容");
                return;
            }
            TaskExecutor.execute(this, 220, data, "", 0, false);
            txtNfc2QrStatus.setText("正在產生 QR 碼...");
        });

        btnNfc2QrScan.setOnClickListener(v -> {
            TaskExecutor.execute(this, 221, "", "", 0, false);
            txtNfc2QrStatus.setText("正在開啟掃描器...");
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) return;
        txtNfc2QrStatus.setText("感應到標籤: " + Converter.hex(tag.getId()));
        vibrate();
    }
}
