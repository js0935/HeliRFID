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
import android.widget.TextView;

public class FlashlightActivity extends BaseNfcActivity {

    private Button btnFlashlightOn, btnFlashlightOff;
    private TextView txtFlashlightStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setUseReaderMode(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashlight);

        btnFlashlightOn = findViewById(R.id.btnFlashlightOn);
        btnFlashlightOff = findViewById(R.id.btnFlashlightOff);
        txtFlashlightStatus = findViewById(R.id.txtFlashlightStatus);

        btnFlashlightOn.setOnClickListener(v -> {
            TaskExecutor.execute(this, 60, "", "", 0, true);
            txtFlashlightStatus.setText("手電筒已開啟");
        });

        btnFlashlightOff.setOnClickListener(v -> {
            TaskExecutor.execute(this, 60, "", "", 0, false);
            txtFlashlightStatus.setText("手電筒已關閉");
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) return;
        txtFlashlightStatus.setText("感應到標籤: " + Converter.hex(tag.getId()));
        vibrate();
    }
}
