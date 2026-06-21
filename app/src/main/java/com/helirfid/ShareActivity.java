/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.0.1
 */
package com.helirfid;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;

public class ShareActivity extends BaseNfcActivity {

    Button btnShareDump, btnShareKeys, btnShareLog;
    TextView txtShareInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        btnShareDump = findViewById(R.id.btnShareDump);
        btnShareKeys = findViewById(R.id.btnShareKeys);
        btnShareLog = findViewById(R.id.btnShareLog);
        txtShareInfo = findViewById(R.id.txtShareInfo);

        btnShareDump.setOnClickListener(v -> {
            byte[] data = DumpStore.getDumpData();
            if (data == null) {
                Toast.makeText(this, "沒有 Dump 資料，請先讀取卡片", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                File dir = new File(getCacheDir(), "share");
                dir.mkdirs();
                File file = new File(dir, "dump_" + System.currentTimeMillis() + ".bin");
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(data);
                fos.close();
                shareFile(file, "application/octet-stream");
                txtShareInfo.setText("已分享 Dump: " + file.getName());
            } catch (Exception e) {
                Toast.makeText(this, "分享失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        btnShareKeys.setOnClickListener(v -> {
            File keysDir = new File(android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_DOWNLOADS), "HeliRFID/keys");
            if (!keysDir.exists()) {
                Toast.makeText(this, "無金鑰檔案", Toast.LENGTH_SHORT).show();
                return;
            }
            File[] files = keysDir.listFiles();
            if (files == null || files.length == 0) {
                Toast.makeText(this, "無金鑰檔案", Toast.LENGTH_SHORT).show();
                return;
            }
            shareFile(files[0], "text/plain");
            txtShareInfo.setText("已分享: " + files[0].getName());
        });

        btnShareLog.setOnClickListener(v -> {
            java.util.List<String> logLines = LogUtil.getLogs();
            if (logLines.isEmpty()) {
                Toast.makeText(this, "無日誌資料", Toast.LENGTH_SHORT).show();
                return;
            }
            StringBuilder sb = new StringBuilder();
            for (String line : logLines) sb.append(line).append("\n");
            String log = sb.toString();
            try {
                File dir = new File(getCacheDir(), "share");
                dir.mkdirs();
                File file = new File(dir, "helirfid_log.txt");
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(log.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                fos.close();
                shareFile(file, "text/plain");
                txtShareInfo.setText("已分享日誌");
            } catch (Exception e) {
                Toast.makeText(this, "分享失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void shareFile(File file, String mimeType) {
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "分享檔案"));
    }
}
