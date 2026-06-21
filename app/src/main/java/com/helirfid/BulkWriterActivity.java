/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.3.1
 */
package com.helirfid;

import android.content.Intent;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BulkWriterActivity extends BaseNfcActivity {

    private TextView txtCount, txtLog, txtPlaceholderRef;
    private EditText editText;
    private Button btnStart, btnStop, btnCsvImport, btnCsvExport;
    private int writeCount;
    private boolean running;
    private List<String[]> csvEntries;
    private int csvIndex;

    private static final int REQ_CODE_CSV_PICK = 2001;
    private static final int REQ_CODE_CSV_SAVE = 2002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bulk_writer);

        editText = findViewById(R.id.editBulkText);
        txtCount = findViewById(R.id.txtBulkCount);
        txtLog = findViewById(R.id.txtBulkLog);
        btnStart = findViewById(R.id.btnBulkStart);
        btnStop = findViewById(R.id.btnBulkStop);
        btnCsvImport = findViewById(R.id.btnBulkCsvImport);
        btnCsvExport = findViewById(R.id.btnBulkCsvExport);
        txtPlaceholderRef = findViewById(R.id.txtBulkPlaceholderRef);

        btnStart.setOnClickListener(v -> {
            String text = editText.getText().toString().trim();
            if (text.isEmpty() && (csvEntries == null || csvEntries.isEmpty())) {
                Toast.makeText(this, "請輸入寫入內容或匯入 CSV", Toast.LENGTH_SHORT).show();
                return;
            }
            running = true;
            writeCount = 0;
            csvIndex = 0;
            updateUi();
            txtLog.setText("大量寫入模式已啟動\n掃描標籤即自動寫入...\n");
            btnStart.setEnabled(false);
            btnStop.setEnabled(true);
        });

        btnStop.setOnClickListener(v -> {
            running = false;
            btnStart.setEnabled(true);
            btnStop.setEnabled(false);
            txtLog.append("已停止\n");
        });

        btnCsvImport.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/*");
            startActivityForResult(intent, REQ_CODE_CSV_PICK);
        });

        btnCsvExport.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/csv");
            intent.putExtra(Intent.EXTRA_TITLE, "bulk_export.csv");
            startActivityForResult(intent, REQ_CODE_CSV_SAVE);
        });

        String placeholderInfo = "可用佔位符: %TIME% %DATE% %UID% %COUNT% %RND% %BATTERY% %WEEKDAY% %NL%";
        if (txtPlaceholderRef != null) txtPlaceholderRef.setText(placeholderInfo);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_CSV_PICK && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) importCsv(uri);
        }
        if (requestCode == REQ_CODE_CSV_SAVE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) exportCsv(uri);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (!running) return;
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) return;

        writeTag(tag);
    }

    private void importCsv(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            if (is == null) return;
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            csvEntries = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(",", 3);
                if (parts.length >= 1) {
                    for (int i = 0; i < parts.length; i++) parts[i] = parts[i].trim();
                    csvEntries.add(parts);
                }
            }
            reader.close();
            is.close();

            StringBuilder sb = new StringBuilder();
            sb.append("CSV 匯入完成 (").append(csvEntries.size()).append(" 筆)\n");
            for (String[] e : csvEntries) {
                sb.append("  ").append(e[0]);
                if (e.length > 1) sb.append(" → ").append(e[1]);
                sb.append("\n");
            }
            txtLog.setText(sb.toString());
            txtCount.setText("CSV: " + csvEntries.size() + " 筆");
            Toast.makeText(this, "已匯入 " + csvEntries.size() + " 筆 CSV 資料", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "CSV 匯入錯誤: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void exportCsv(Uri uri) {
        try {
            java.io.OutputStream os = getContentResolver().openOutputStream(uri);
            java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(os, java.nio.charset.StandardCharsets.UTF_8);
            writer.write("type,param1,param2\n");
            if (csvEntries != null) {
                for (String[] entry : csvEntries) {
                    writer.write(String.join(",", entry) + "\n");
                }
            } else {
                String text = editText.getText().toString().trim();
                if (!text.isEmpty()) writer.write("text," + text + ",\n");
            }
            writer.flush();
            writer.close();
            txtLog.append("CSV 已匯出\n");
            Toast.makeText(this, "CSV 匯出完成", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "CSV 匯出錯誤: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void writeTag(Tag tag) {
        NdefRecord record;
        NdefMessage msg;
        String uid = Converter.hex(tag.getId());

        if (csvEntries != null && csvIndex < csvEntries.size()) {
            String[] entry = csvEntries.get(csvIndex);
            String type = entry[0];
            String param1 = entry.length > 1 ? PlaceholderEngine.resolve(entry[1], this, uid) : "";
            String param2 = entry.length > 2 ? PlaceholderEngine.resolve(entry[2], this, uid) : "";

            switch (type.toLowerCase()) {
                case "text":
                    record = NdefRecord.createTextRecord("zh", param1);
                    break;
                case "url":
                    record = NFCWriter.createUrlRecord(param1);
                    break;
                case "phone":
                    record = NFCWriter.createPhoneRecord(param1);
                    break;
                case "email":
                    record = NFCWriter.createEmailRecord(param1, param2, "");
                    break;
                case "wifi":
                    record = NFCWriter.createWifiConfigRecord(param1, param2, "WPA");
                    break;
                default:
                    record = NdefRecord.createTextRecord("zh", param1);
                    break;
            }
            csvIndex++;
        } else {
            String text = PlaceholderEngine.resolve(editText.getText().toString().trim(), this, uid);
            record = NdefRecord.createTextRecord("zh", text);
        }
        msg = new NdefMessage(new NdefRecord[]{record});

        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();
                ndef.writeNdefMessage(msg);
                ndef.close();
                writeCount++;
                final String uidFinal = uid;
                runOnUiThread(() -> {
                    txtLog.append("✓ #" + writeCount + " UID: " + uidFinal + "\n");
                    updateUi();
                });
                return;
            }
            NdefFormatable fmt = NdefFormatable.get(tag);
            if (fmt != null) {
                fmt.connect();
                fmt.format(msg);
                fmt.close();
                writeCount++;
                final String uidFinal2 = uid;
                runOnUiThread(() -> {
                    txtLog.append("✓ #" + writeCount + " UID: " + uidFinal2 + " (格式化+寫入)\n");
                    updateUi();
                });
                return;
            }
            runOnUiThread(() -> txtLog.append("✗ 不支援 NDEF\n"));
        } catch (Exception e) {
            runOnUiThread(() -> txtLog.append("✗ 寫入失敗: " + e.getMessage() + "\n"));
        }
    }

    private void updateUi() {
        txtCount.setText("已寫入: " + writeCount + " 張");
    }

    @Override
    protected void onPause() {
        super.onPause();
        running = false;
        csvIndex = 0;
        btnStart.setEnabled(true);
        btnStop.setEnabled(false);
    }
}
