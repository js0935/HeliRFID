/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.3.1
 */
package com.helirfid;

import android.app.AlertDialog;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class TagBackupActivity extends BaseNfcActivity {

    private TextView txtTagInfo, txtBackupList;
    private Button btnBackup, btnRestore, btnRefresh, btnDelete;
    private Tag currentTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_backup);

        txtTagInfo = findViewById(R.id.txtBakTagInfo);
        txtBackupList = findViewById(R.id.txtBakList);
        btnBackup = findViewById(R.id.btnBakBackup);
        btnRestore = findViewById(R.id.btnBakRestore);
        btnRefresh = findViewById(R.id.btnBakRefresh);
        btnDelete = findViewById(R.id.btnBakDelete);

        btnBackup.setOnClickListener(v -> doBackup());
        btnRestore.setOnClickListener(v -> showRestoreDialog());
        btnRefresh.setOnClickListener(v -> refreshBackupList());
        btnDelete.setOnClickListener(v -> showDeleteDialog());

        refreshBackupList();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) return;
        currentTag = tag;
        String uid = Converter.hex(tag.getId());
        StringBuilder sb = new StringBuilder("卡片已偵測\nUID: ").append(uid);
        for (String t : tag.getTechList())
            sb.append("\n  ").append(t.substring(t.lastIndexOf('.') + 1));
        txtTagInfo.setText(sb.toString());
    }

    private File getBackupDir() {
        File dir = new File(getFilesDir(), "ndef_backups");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    private void doBackup() {
        if (currentTag == null) {
            Toast.makeText(this, "請先掃描 NFC 標籤", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(() -> {
            try {
                Ndef ndef = Ndef.get(currentTag);
                if (ndef == null) {
                    runOnUiThread(() -> Toast.makeText(this, "不支援 NDEF", Toast.LENGTH_SHORT).show());
                    return;
                }
                ndef.connect();
                NdefMessage msg = ndef.getNdefMessage();
                ndef.close();
                if (msg == null) {
                    runOnUiThread(() -> Toast.makeText(this, "標籤無 NDEF 資料", Toast.LENGTH_SHORT).show());
                    return;
                }
                byte[] raw = msg.toByteArray();
                String uid = Converter.hex(currentTag.getId());
                String ts = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                String fileName = "backup_" + uid + "_" + ts + ".ndef";
                File f = new File(getBackupDir(), fileName);
                try (FileOutputStream fos = new FileOutputStream(f)) {
                    fos.write(raw);
                }
                runOnUiThread(() -> {
                    Toast.makeText(this, "備份成功: " + fileName, Toast.LENGTH_SHORT).show();
                    refreshBackupList();
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "備份失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void showRestoreDialog() {
        File[] files = getBackupDir().listFiles((d, n) -> n.endsWith(".ndef"));
        if (files == null || files.length == 0) {
            Toast.makeText(this, "無備份檔案", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentTag == null) {
            Toast.makeText(this, "請先掃描 NFC 標籤", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] items = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            items[i] = files[i].getName() + " (" + files[i].length() + " bytes)";
        }
        new AlertDialog.Builder(this)
                .setTitle("選擇備份還原")
                .setItems(items, (d, which) -> restoreFromFile(files[which]))
                .setNegativeButton("取消", null)
                .show();
    }

    private void restoreFromFile(File file) {
        new Thread(() -> {
            try {
                byte[] raw = new byte[(int) file.length()];
                try (FileInputStream fis = new FileInputStream(file)) {
                    fis.read(raw);
                }
                NdefMessage msg = new NdefMessage(raw);
                String result = NFCWriter.writeNdefMessage(currentTag, msg.getRecords());
                runOnUiThread(() -> {
                    txtTagInfo.setText(result);
                    Toast.makeText(this, result, Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "還原失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void showDeleteDialog() {
        File[] files = getBackupDir().listFiles((d, n) -> n.endsWith(".ndef"));
        if (files == null || files.length == 0) {
            Toast.makeText(this, "無備份檔案", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] items = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            items[i] = files[i].getName();
        }
        new AlertDialog.Builder(this)
                .setTitle("選擇要刪除的備份")
                .setItems(items, (d, which) -> {
                    if (files[which].delete()) {
                        Toast.makeText(this, "已刪除", Toast.LENGTH_SHORT).show();
                        refreshBackupList();
                    } else {
                        Toast.makeText(this, "刪除失敗", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void refreshBackupList() {
        File[] files = getBackupDir().listFiles((d, n) -> n.endsWith(".ndef"));
        if (files == null || files.length == 0) {
            txtBackupList.setText("無備份檔案");
            return;
        }
        StringBuilder sb = new StringBuilder("=== 備份列表 (").append(files.length).append(") ===\n");
        Arrays.sort(files, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
        for (File f : files) {
            String ts = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(new Date(f.lastModified()));
            sb.append(f.getName()).append("\n  ").append(ts).append(", ").append(f.length()).append(" bytes\n");
        }
        txtBackupList.setText(sb.toString());
    }
}
