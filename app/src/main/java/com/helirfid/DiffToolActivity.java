/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.0.1
 */
package com.helirfid;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class DiffToolActivity extends BaseNfcActivity {

    Button btnLoadA, btnLoadB, btnCompare;
    TextView txtDumpA, txtDumpB, txtResult;

    String[] dumpA, dumpB;
    String nameA, nameB;

    private static final int REQUEST_FILE_A = 100;
    private static final int REQUEST_FILE_B = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diff_tool);

        btnLoadA = findViewById(R.id.btnLoadDumpA);
        btnLoadB = findViewById(R.id.btnLoadDumpB);
        btnCompare = findViewById(R.id.btnCompareDumps);
        txtDumpA = findViewById(R.id.txtDumpA);
        txtDumpB = findViewById(R.id.txtDumpB);
        txtResult = findViewById(R.id.txtDiffResult);

        btnLoadA.setOnClickListener(v -> showFilePicker(REQUEST_FILE_A));
        btnLoadB.setOnClickListener(v -> showFilePicker(REQUEST_FILE_B));

        btnCompare.setOnClickListener(v -> {
            if (dumpA == null || dumpB == null) {
                Toast.makeText(this, "請先載入兩個 Dump 檔案", Toast.LENGTH_SHORT).show();
                return;
            }
            compareDumps();
        });
    }

    private void showFilePicker(int requestCode) {
        File dir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), "HeliRFID");
        if (!dir.exists()) dir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS);

        File[] files = dir.listFiles((d, name) ->
                name.endsWith(".bin") || name.endsWith(".eml") || name.endsWith(".mct") || name.endsWith(".txt"));
        if (files == null || files.length == 0) {
            Toast.makeText(this, "在 " + dir.getPath() + " 中找不到 dump 檔案", Toast.LENGTH_LONG).show();
            return;
        }

        String[] names = new String[files.length];
        for (int i = 0; i < files.length; i++) names[i] = files[i].getName();

        new AlertDialog.Builder(this)
                .setTitle("選擇 Dump 檔案")
                .setItems(names, (dialog, which) -> {
                    loadDump(files[which], requestCode);
                })
                .show();
    }

    private void loadDump(File file, int requestCode) {
        try {
            List<String> lines = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("+") || line.startsWith("#")) continue;
                    String[] parts = line.split("\\s+");
                    for (String p : parts) {
                        if (p.length() == 2 && p.matches("[0-9A-Fa-f]{2}")) {
                            lines.add(p);
                        }
                    }
                }
            }

            String[] data = lines.toArray(new String[0]);
            StringBuilder preview = new StringBuilder();
            preview.append(file.getName()).append(" (").append(data.length / 16).append(" blocks)\n\n");
            int maxPreview = Math.min(data.length / 16, 10);
            for (int i = 0; i < maxPreview; i++) {
                for (int j = 0; j < 16 && i*16+j < data.length; j++) {
                    preview.append(data[i*16+j]).append(" ");
                }
                preview.append("\n");
            }
            if (maxPreview < data.length / 16) preview.append("...");

            if (requestCode == REQUEST_FILE_A) {
                dumpA = data;
                nameA = file.getName();
                txtDumpA.setText(preview.toString());
            } else {
                dumpB = data;
                nameB = file.getName();
                txtDumpB.setText(preview.toString());
            }

            Toast.makeText(this, "已載入: " + file.getName(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "讀取失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void compareDumps() {
        StringBuilder sb = new StringBuilder();
        sb.append("比對結果: ").append(nameA).append(" vs ").append(nameB).append("\n\n");

        int maxLen = Math.max(dumpA.length, dumpB.length);
        int diffCount = 0;

        for (int i = 0; i < maxLen; i++) {
            String a = i < dumpA.length ? dumpA[i] : "--";
            String b = i < dumpB.length ? dumpB[i] : "--";
            if (!a.equals(b)) {
                int block = i / 16;
                int offset = i % 16;
                sb.append(String.format("Block %02d [%02d]: %s vs %s\n", block, offset, a, b));
                diffCount++;
                if (diffCount >= 30) {
                    sb.append("... 尚有更多差異\n");
                    break;
                }
            }
        }

        if (diffCount == 0) {
            sb.append("✓ 兩個 Dump 完全相同！\n");
        } else {
            sb.append("\n共 ").append(diffCount).append(" 個 byte 不同\n");
        }

        txtResult.setText(sb.toString());
    }
}
