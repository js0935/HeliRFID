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

public class NfcSafeActivity extends BaseNfcActivity {

    private EditText editSafeKey, editSafeValue;
    private Button btnSafeSave, btnSafeRead;
    private TextView txtSafeStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setUseReaderMode(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_safe);

        editSafeKey = findViewById(R.id.editSafeKey);
        editSafeValue = findViewById(R.id.editSafeValue);
        btnSafeSave = findViewById(R.id.btnSafeSave);
        btnSafeRead = findViewById(R.id.btnSafeRead);
        txtSafeStatus = findViewById(R.id.txtSafeStatus);

        btnSafeSave.setOnClickListener(v -> {
            String key = editSafeKey.getText().toString().trim();
            String value = editSafeValue.getText().toString().trim();
            if (key.isEmpty()) {
                txtSafeStatus.setText("請輸入金鑰名稱");
                return;
            }
            if (value.isEmpty()) {
                txtSafeStatus.setText("請輸入數值");
                return;
            }
            TaskExecutor.execute(this, 265, key, value, 0, false);
            txtSafeStatus.setText("已儲存: " + key);
        });

        btnSafeRead.setOnClickListener(v -> {
            String key = editSafeKey.getText().toString().trim();
            if (key.isEmpty()) {
                txtSafeStatus.setText("請輸入要讀取的金鑰名稱");
                return;
            }
            TaskExecutor.execute(this, 266, key, "", 0, false);
            txtSafeStatus.setText("讀取中: " + key);
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) return;
        txtSafeStatus.setText("感應到標籤: " + Converter.hex(tag.getId()));
        vibrate();
    }
}
