/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.0.1
 */
package com.helirfid;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ScanLogActivity extends AppCompatActivity {

    TextView txtScanLog;
    Button btnClear, btnExport;
    SharedPreferences prefs;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_log);

        txtScanLog = findViewById(R.id.txtScanLogContent);
        btnClear = findViewById(R.id.btnClearScanLog);
        btnExport = findViewById(R.id.btnExportScanLog);

        prefs = getSharedPreferences("scan_log", MODE_PRIVATE);

        loadLog();

        btnClear.setOnClickListener(v -> {
            prefs.edit().clear().apply();
            txtScanLog.setText("掃描日誌已清除");
            Toast.makeText(this, "已清除", Toast.LENGTH_SHORT).show();
        });

        btnExport.setOnClickListener(v -> {
            String log = txtScanLog.getText().toString();
            if (log.isEmpty() || log.equals("掃描日誌已清除")) {
                Toast.makeText(this, "無日誌", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                java.io.File dir = new java.io.File(getExternalFilesDir(null), "scan_logs");
                dir.mkdirs();
                java.io.File file = new java.io.File(dir,
                        "scan_log_" + System.currentTimeMillis() + ".txt");
                java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
                fos.write(log.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                fos.close();
                Toast.makeText(this, "已匯出: " + file.getName(), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "匯出失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void addScanLog(android.content.Context context, String uid, String tech) {
        SharedPreferences prefs = context.getSharedPreferences("scan_log", MODE_PRIVATE);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String time = sdf.format(new Date());
        String entry = time + " | " + uid + " | " + tech;

        String existing = prefs.getString("logs", "");
        StringBuilder sb = new StringBuilder();
        sb.append(entry).append("\n");
        if (!existing.isEmpty()) {
            String[] lines = existing.split("\n");
            int count = Math.min(lines.length, 199);
            for (int i = 0; i < count; i++) sb.append(lines[i]).append("\n");
        }
        prefs.edit().putString("logs", sb.toString()).apply();
    }

    private void loadLog() {
        String logs = prefs.getString("logs", "");
        if (logs.isEmpty()) {
            txtScanLog.setText("尚無掃描記錄");
        } else {
            txtScanLog.setText(logs);
        }
    }
}
