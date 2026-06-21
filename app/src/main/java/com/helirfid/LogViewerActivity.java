/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.0.1
 */
package com.helirfid;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LogViewerActivity extends BaseNfcActivity {

    TextView txtLog;
    Button btnClear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_viewer);

        txtLog = findViewById(R.id.txtLog);
        btnClear = findViewById(R.id.btnClearLog);

        refreshLog();

        btnClear.setOnClickListener(v -> {
            LogUtil.clear();
            refreshLog();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshLog();
    }

    private void refreshLog() {
        StringBuilder sb = new StringBuilder();
        for (String line : LogUtil.getLogs()) {
            sb.append(line).append("\n");
        }
        txtLog.setText(sb.length() > 0 ? sb.toString() : "無日誌");
    }
}
