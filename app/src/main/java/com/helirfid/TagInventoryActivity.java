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

public class TagInventoryActivity extends BaseNfcActivity {

    private TextView txtInventoryStatus;
    private Button btnInventoryReset;
    private SharedPreferences prefs;
    private int scanCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setUseReaderMode(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_inventory);

        txtInventoryStatus = findViewById(R.id.txtInventoryStatus);
        btnInventoryReset = findViewById(R.id.btnInventoryReset);

        prefs = getSharedPreferences("tag_inventory", MODE_PRIVATE);
        scanCount = prefs.getInt("count", 0);
        updateStatus();

        btnInventoryReset.setOnClickListener(v -> {
            scanCount = 0;
            prefs.edit().putInt("count", 0).apply();
            updateStatus();
        });
    }

    private void updateStatus() {
        txtInventoryStatus.setText("掃描次數: " + scanCount);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) return;
        scanCount++;
        prefs.edit().putInt("count", scanCount).apply();
        updateStatus();
        vibrate();
    }
}
