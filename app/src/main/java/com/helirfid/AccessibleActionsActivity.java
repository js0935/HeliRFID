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

public class AccessibleActionsActivity extends BaseNfcActivity {

    private Button btnToggleColorInversion, btnToggleHighContrast, btnToggleReadingMode, btnReduceAnimation, btnToggleDarkMode;
    private TextView txtAccessibleStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setUseReaderMode(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accessible_actions);

        btnToggleColorInversion = findViewById(R.id.btnToggleColorInversion);
        btnToggleHighContrast = findViewById(R.id.btnToggleHighContrast);
        btnToggleReadingMode = findViewById(R.id.btnToggleReadingMode);
        btnReduceAnimation = findViewById(R.id.btnReduceAnimation);
        btnToggleDarkMode = findViewById(R.id.btnToggleDarkMode);
        txtAccessibleStatus = findViewById(R.id.txtAccessibleStatus);

        btnToggleColorInversion.setOnClickListener(v -> {
            TaskExecutor.execute(this, 88, "", "", 0, false);
            txtAccessibleStatus.setText("已執行: 切換色彩反轉");
        });

        btnToggleHighContrast.setOnClickListener(v -> {
            TaskExecutor.execute(this, 96, "", "", 0, false);
            txtAccessibleStatus.setText("已執行: 切換高對比文字");
        });

        btnToggleReadingMode.setOnClickListener(v -> {
            TaskExecutor.execute(this, 92, "", "", 0, false);
            txtAccessibleStatus.setText("已執行: 切換閱讀模式");
        });

        btnReduceAnimation.setOnClickListener(v -> {
            TaskExecutor.execute(this, 175, "", "", 0, false);
            txtAccessibleStatus.setText("已執行: 減少動畫");
        });

        btnToggleDarkMode.setOnClickListener(v -> {
            TaskExecutor.execute(this, 27, "", "", 0, false);
            txtAccessibleStatus.setText("已執行: 切換深色模式");
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) return;
        txtAccessibleStatus.setText("感應到標籤: " + Converter.hex(tag.getId()));
        vibrate();
    }
}
