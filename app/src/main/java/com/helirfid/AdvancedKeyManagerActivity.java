/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.0.1
 */
package com.helirfid;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class AdvancedKeyManagerActivity extends BaseNfcActivity {

    EditText editFileName;
    RadioGroup rgKeyType;
    Button btnCreate, btnLoad;
    TextView txtPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced_key);

        editFileName = findViewById(R.id.editAdvKeyName);
        rgKeyType = findViewById(R.id.rgKeyType);
        btnCreate = findViewById(R.id.btnCreateAdvKey);
        btnLoad = findViewById(R.id.btnLoadAdvKey);
        txtPreview = findViewById(R.id.txtAdvKeyPreview);

        btnCreate.setOnClickListener(v -> createKeyFile());
        btnLoad.setOnClickListener(v -> loadKeyFile());
    }

    private void createKeyFile() {
        String name = editFileName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "請輸入檔案名稱", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!name.endsWith(".keys")) name += ".keys";

        int typeId = rgKeyType.getCheckedRadioButtonId();
        List<String> keys = new ArrayList<>();

        if (typeId == R.id.radioKeyStd) {
            for (int i = 0; i < 16; i++) keys.add("FFFFFFFFFFFF");
        } else if (typeId == R.id.radioKeyExtended) {
            for (int i = 0; i < 40; i++) keys.add("FFFFFFFFFFFF");
        } else {
            int[] sectors = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
                    20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39};
            for (int s : sectors) {
                if (s < 32) keys.add(String.format("S%02DK%02X%02X%02X%02X%02X%02X",
                        s, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF));
                else keys.add(String.format("S%02DK%02X%02X%02X%02X%02X%02X",
                        s, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF));
            }
        }

        StringBuilder sb = new StringBuilder();
        for (String k : keys) sb.append(k).append("\n");
        txtPreview.setText("預覽 (" + keys.size() + " keys):\n" + sb.toString());

        int typeLabel = typeId == R.id.radioKeyStd ? 16 : typeId == R.id.radioKeyExtended ? 40 : 40;
        showConfirmDialog(name, sb.toString(), typeLabel);
    }

    private void showConfirmDialog(String name, String content, int count) {
        new AlertDialog.Builder(this)
                .setTitle("建立金鑰檔案")
                .setMessage("將建立 " + name + " (" + count + " keys)\n儲存至 Downloads/HeliRFID/keys/")
                .setPositiveButton("確定", (dialog, which) -> {
                    try {
                        File dir = new File(android.os.Environment.getExternalStoragePublicDirectory(
                                android.os.Environment.DIRECTORY_DOWNLOADS), "HeliRFID/keys");
                        dir.mkdirs();
                        File file = new File(dir, name);
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                        fos.close();
                        Toast.makeText(this, "已建立: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "建立失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void loadKeyFile() {
        String name = editFileName.getText().toString().trim();
        if (!name.endsWith(".keys")) name += ".keys";

        try {
            File dir = new File(android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_DOWNLOADS), "HeliRFID/keys");
            File file = new File(dir, name);
            java.io.FileReader fr = new java.io.FileReader(file);
            java.io.BufferedReader br = new java.io.BufferedReader(fr);
            StringBuilder content = new StringBuilder();
            String line;
            int count = 0;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
                count++;
            }
            br.close();
            txtPreview.setText("已載入 " + name + " (" + count + " keys):\n" + content.toString());
        } catch (Exception e) {
            txtPreview.setText("載入失敗: " + e.getMessage());
        }
    }
}
