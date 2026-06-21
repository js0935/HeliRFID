/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.3.1
 */
package com.helirfid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class TagCyclesActivity extends BaseNfcActivity {

    private TextView txtTagCyclesStatus;
    private Button btnTagCyclesReset;
    private SharedPreferences prefs;
    private String currentUid = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setUseReaderMode(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_cycles);

        txtTagCyclesStatus = findViewById(R.id.txtTagCyclesStatus);
        btnTagCyclesReset = findViewById(R.id.btnTagCyclesReset);
        prefs = getSharedPreferences("tag_cycles", MODE_PRIVATE);

        btnTagCyclesReset.setOnClickListener(v -> {
            if (!currentUid.isEmpty()) {
                prefs.edit().putInt(currentUid, 0).apply();
                txtTagCyclesStatus.setText(currentUid + "\n寫入次數: 0 (已重置)");
            } else {
                txtTagCyclesStatus.setText("請先感應標籤");
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) return;
        currentUid = Converter.hex(tag.getId());
        int count = prefs.getInt(currentUid, 0) + 1;
        prefs.edit().putInt(currentUid, count).apply();
        txtTagCyclesStatus.setText("UID: " + currentUid + "\n寫入次數: " + count);
        vibrate();
    }
}
