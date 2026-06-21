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
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TagArchiverActivity extends BaseNfcActivity {

    private static final String ARCHIVE_TYPE = "helirfid.com:archive";
    private static final int CHUNK_SIZE = 40;

    private TextView txtFileInfo, txtLog;
    private Button btnSelectFile, btnStartArchive, btnReassemble;

    private byte[] selectedFileData;
    private String selectedFileName;
    private int fileSize;

    private int currentChunkIndex;
    private int totalChunks;
    private boolean archivingMode;
    private boolean reassembleMode;

    private final List<byte[]> collectedChunks = new ArrayList<>();

    private static final int REQ_CODE_PICK_FILE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_archiver);

        txtFileInfo = findViewById(R.id.txtArchiveFileInfo);
        txtLog = findViewById(R.id.txtArchiveLog);
        btnSelectFile = findViewById(R.id.btnArchiveSelectFile);
        btnStartArchive = findViewById(R.id.btnArchiveStart);
        btnReassemble = findViewById(R.id.btnArchiveReassemble);

        btnSelectFile.setOnClickListener(v -> pickFile());
        btnStartArchive.setOnClickListener(v -> startArchiveMode());
        btnReassemble.setOnClickListener(v -> startReassembleMode());
    }

    private void pickFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, REQ_CODE_PICK_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_PICK_FILE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) loadFile(uri);
        }
    }

    private void loadFile(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            if (is == null) return;
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            is.close();

            selectedFileData = sb.toString().getBytes(StandardCharsets.UTF_8);
            fileSize = selectedFileData.length;
            selectedFileName = uri.getLastPathSegment();
            totalChunks = (int) Math.ceil((double) fileSize / CHUNK_SIZE);

            txtFileInfo.setText("檔案: " + selectedFileName + " (" + fileSize + " bytes, 將分 " + totalChunks + " 塊)");
            btnStartArchive.setEnabled(true);
            appendLog("已載入檔案: " + selectedFileName + " (" + fileSize + " bytes)");
        } catch (Exception e) {
            appendLog("檔案讀取錯誤: " + e.getMessage());
        }
    }

    private void startArchiveMode() {
        if (selectedFileData == null) {
            appendLog("請先選擇檔案");
            return;
        }
        archivingMode = true;
        reassembleMode = false;
        currentChunkIndex = 0;
        appendLog("=== 封存模式啟動 ===");
        appendLog("請掃描標籤 #" + (currentChunkIndex + 1) + " / " + totalChunks);
    }

    private void startReassembleMode() {
        reassembleMode = true;
        archivingMode = false;
        collectedChunks.clear();
        appendLog("=== 還原模式啟動 ===");
        appendLog("請依序掃描封存標籤");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) return;

        if (archivingMode) {
            writeChunk(tag);
        } else if (reassembleMode) {
            readChunk(tag);
        }
    }

    private void writeChunk(Tag tag) {
        if (currentChunkIndex >= totalChunks) {
            appendLog("所有區塊已寫入完成");
            archivingMode = false;
            return;
        }

        int start = currentChunkIndex * CHUNK_SIZE;
        int end = Math.min(start + CHUNK_SIZE, fileSize);
        byte[] chunk = new byte[end - start];
        System.arraycopy(selectedFileData, start, chunk, 0, chunk.length);

        ByteBuffer meta = ByteBuffer.allocate(8);
        meta.putInt(currentChunkIndex);
        meta.putInt(totalChunks);

        byte[] typePayload = new byte[meta.capacity() + chunk.length];
        System.arraycopy(meta.array(), 0, typePayload, 0, meta.capacity());
        System.arraycopy(chunk, 0, typePayload, meta.capacity(), chunk.length);

        NdefRecord record = new NdefRecord(NdefRecord.TNF_EXTERNAL_TYPE,
                ARCHIVE_TYPE.getBytes(StandardCharsets.US_ASCII),
                new byte[0], typePayload);
        NdefMessage msg = new NdefMessage(new NdefRecord[]{record});

        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();
                ndef.writeNdefMessage(msg);
                ndef.close();
                currentChunkIndex++;
                appendLog("✓ 區塊 " + currentChunkIndex + "/" + totalChunks + " 寫入成功");
                vibrate();
                if (currentChunkIndex < totalChunks) {
                    appendLog("請掃描下一個標籤 #" + (currentChunkIndex + 1));
                } else {
                    appendLog("=== 封存完成 ===");
                    archivingMode = false;
                }
                return;
            }
            NdefFormatable fmt = NdefFormatable.get(tag);
            if (fmt != null) {
                fmt.connect();
                fmt.format(msg);
                fmt.close();
                currentChunkIndex++;
                appendLog("✓ 區塊 " + currentChunkIndex + "/" + totalChunks + " (格式化+寫入)");
                vibrate();
                if (currentChunkIndex < totalChunks) {
                    appendLog("請掃描下一個標籤 #" + (currentChunkIndex + 1));
                } else {
                    appendLog("=== 封存完成 ===");
                    archivingMode = false;
                }
                return;
            }
            appendLog("✗ 不支援 NDEF");
        } catch (Exception e) {
            appendLog("✗ 寫入失敗: " + e.getMessage());
        }
    }

    private void readChunk(Tag tag) {
        try {
            android.nfc.tech.Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                appendLog("✗ 不支援 NDEF");
                return;
            }
            ndef.connect();
            NdefMessage msg = ndef.getNdefMessage();
            ndef.close();

            if (msg == null || msg.getRecords().length == 0) {
                appendLog("✗ 無 NDEF 資料");
                return;
            }

            NdefRecord record = msg.getRecords()[0];
            String type = new String(record.getType(), StandardCharsets.US_ASCII);
            if (!ARCHIVE_TYPE.equals(type)) {
                appendLog("✗ 非封存標籤 (type: " + type + ")");
                return;
            }

            byte[] payload = record.getPayload();
            if (payload.length < 8) {
                appendLog("✗ 資料格式錯誤");
                return;
            }

            ByteBuffer meta = ByteBuffer.wrap(payload, 0, 8);
            int seq = meta.getInt();
            int total = meta.getInt();

            byte[] chunk = new byte[payload.length - 8];
            System.arraycopy(payload, 8, chunk, 0, chunk.length);

            if (collectedChunks.size() <= seq) {
                while (collectedChunks.size() < seq) collectedChunks.add(null);
                collectedChunks.add(chunk);
            } else {
                collectedChunks.set(seq, chunk);
            }

            int collected = 0;
            for (byte[] c : collectedChunks) if (c != null) collected++;
            appendLog("✓ 讀取區塊 " + (seq + 1) + "/" + total + " (已收集 " + collected + "/" + total + ")");
            vibrate();

            if (collected >= total) {
                reassembleFile(total);
            }
        } catch (Exception e) {
            appendLog("✗ 讀取失敗: " + e.getMessage());
        }
    }

    private void reassembleFile(int total) {
        try {
            int totalSize = 0;
            for (byte[] chunk : collectedChunks) totalSize += chunk.length;

            byte[] fileData = new byte[totalSize];
            int offset = 0;
            for (byte[] chunk : collectedChunks) {
                System.arraycopy(chunk, 0, fileData, offset, chunk.length);
                offset += chunk.length;
            }

            String content = new String(fileData, StandardCharsets.UTF_8);
            appendLog("=== 檔案還原完成 ===");
            appendLog("大小: " + fileData.length + " bytes");
            appendLog("前200字元: " + content.substring(0, Math.min(200, content.length())));

            reassembleMode = false;
        } catch (Exception e) {
            appendLog("✗ 還原失敗: " + e.getMessage());
        }
    }

    private void appendLog(String msg) {
        runOnUiThread(() -> {
            String prev = txtLog.getText().toString();
            txtLog.setText(prev + "\n" + msg);
        });
    }
}
