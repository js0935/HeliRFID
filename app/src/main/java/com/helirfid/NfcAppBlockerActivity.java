package com.helirfid;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class NfcAppBlockerActivity extends BaseNfcActivity {

    private EditText editBlockerPkg;
    private Button btnBlockApp, btnUnblockApp, btnKillApp;
    private TextView txtBlockerStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setUseReaderMode(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_app_blocker);

        editBlockerPkg = findViewById(R.id.editBlockerPkg);
        btnBlockApp = findViewById(R.id.btnBlockApp);
        btnUnblockApp = findViewById(R.id.btnUnblockApp);
        btnKillApp = findViewById(R.id.btnKillApp);
        txtBlockerStatus = findViewById(R.id.txtBlockerStatus);

        btnBlockApp.setOnClickListener(v -> {
            String pkg = editBlockerPkg.getText().toString().trim();
            if (pkg.isEmpty()) {
                txtBlockerStatus.setText("請輸入套件名稱");
                return;
            }
            TaskExecutor.execute(this, 261, pkg, "", 0, true);
            txtBlockerStatus.setText("已封鎖: " + pkg);
        });

        btnUnblockApp.setOnClickListener(v -> {
            String pkg = editBlockerPkg.getText().toString().trim();
            if (pkg.isEmpty()) {
                txtBlockerStatus.setText("請輸入套件名稱");
                return;
            }
            TaskExecutor.execute(this, 262, pkg, "", 0, false);
            txtBlockerStatus.setText("已解除封鎖: " + pkg);
        });

        btnKillApp.setOnClickListener(v -> {
            String pkg = editBlockerPkg.getText().toString().trim();
            if (pkg.isEmpty()) {
                txtBlockerStatus.setText("請輸入套件名稱");
                return;
            }
            TaskExecutor.execute(this, 255, pkg, "", 0, false);
            txtBlockerStatus.setText("已關閉: " + pkg);
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) return;
        txtBlockerStatus.setText("感應到標籤: " + Converter.hex(tag.getId()));
        vibrate();
    }
}
