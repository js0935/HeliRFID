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
import android.widget.RadioGroup;
import android.widget.TextView;

public class NfcWebhookActivity extends BaseNfcActivity {

    private EditText editWebhookUrl, editWebhookBody;
    private RadioGroup radioWebhookMethod;
    private Button btnWebhookSend;
    private TextView txtWebhookStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setUseReaderMode(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_webhook);

        editWebhookUrl = findViewById(R.id.editWebhookUrl);
        editWebhookBody = findViewById(R.id.editWebhookBody);
        radioWebhookMethod = findViewById(R.id.radioWebhookMethod);
        btnWebhookSend = findViewById(R.id.btnWebhookSend);
        txtWebhookStatus = findViewById(R.id.txtWebhookStatus);

        btnWebhookSend.setOnClickListener(v -> {
            String url = editWebhookUrl.getText().toString().trim();
            if (url.isEmpty()) {
                txtWebhookStatus.setText("請輸入網址");
                return;
            }
            String body = editWebhookBody.getText().toString().trim();
            int selectedId = radioWebhookMethod.getCheckedRadioButtonId();
            if (selectedId == R.id.radioWebhookGet) {
                TaskExecutor.execute(this, 224, url, "", 0, false);
                txtWebhookStatus.setText("發送 GET 請求...");
            } else {
                TaskExecutor.execute(this, 225, url, body, 0, false);
                txtWebhookStatus.setText("發送 POST 請求...");
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) return;
        txtWebhookStatus.setText("感應到標籤: " + Converter.hex(tag.getId()));
        vibrate();
    }
}
