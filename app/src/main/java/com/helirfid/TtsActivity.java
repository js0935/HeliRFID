/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.3.1
 */
package com.helirfid;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class TtsActivity extends BaseNfcActivity {

    private EditText editTtsText;
    private Button btnTtsSpeak, btnTtsStop;
    private TextView txtTtsStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setUseReaderMode(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tts);

        editTtsText = findViewById(R.id.editTtsText);
        btnTtsSpeak = findViewById(R.id.btnTtsSpeak);
        btnTtsStop = findViewById(R.id.btnTtsStop);
        txtTtsStatus = findViewById(R.id.txtTtsStatus);

        btnTtsSpeak.setOnClickListener(v -> {
            String text = editTtsText.getText().toString().trim();
            if (text.isEmpty()) {
                txtTtsStatus.setText("請輸入文字");
                return;
            }
            TaskExecutor.execute(this, 229, text, "", 0, false);
            txtTtsStatus.setText("朗讀中...");
        });

        btnTtsStop.setOnClickListener(v -> {
            TaskExecutor.execute(this, 230, "", "", 0, false);
            txtTtsStatus.setText("已停止");
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) return;
        txtTtsStatus.setText("感應到標籤: " + Converter.hex(tag.getId()));
        vibrate();
    }
}
