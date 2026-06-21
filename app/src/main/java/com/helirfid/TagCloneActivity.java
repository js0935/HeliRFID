package com.helirfid;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

public class TagCloneActivity extends BaseNfcActivity {

    private TextView txtSourceInfo, txtTargetInfo, txtStatus;
    private Button btnReadSource, btnWriteTarget;
    private Tag currentTag;
    private byte[] cachedSourceUid;
    private NdefMessage cachedNdefMessage;
    private boolean hasSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_clone);

        txtSourceInfo = findViewById(R.id.txtCloneSource);
        txtTargetInfo = findViewById(R.id.txtCloneTarget);
        txtStatus = findViewById(R.id.txtCloneStatus);
        btnReadSource = findViewById(R.id.btnCloneRead);
        btnWriteTarget = findViewById(R.id.btnCloneWrite);

        btnReadSource.setOnClickListener(v -> readSource());
        btnWriteTarget.setOnClickListener(v -> writeTarget());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) return;
        currentTag = tag;
        String uid = Converter.hex(tag.getId());
        StringBuilder sb = new StringBuilder("UID: ").append(uid);

        if (!hasSource) {
            cachedSourceUid = tag.getId();
            sb.append("\n(來源 — 點擊「讀取來源」)");
            txtSourceInfo.setText(sb.toString());
        } else {
            sb.append("\n(目標 — 點擊「寫入目標」)");
            txtTargetInfo.setText(sb.toString());
        }
    }

    private void readSource() {
        Tag tag = getCurrentTag();
        if (tag == null) {
            Toast.makeText(this, "請先掃描來源標籤", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(() -> {
            try {
                Ndef ndef = Ndef.get(tag);
                if (ndef != null) {
                    ndef.connect();
                    cachedNdefMessage = ndef.getNdefMessage();
                    ndef.close();
                }
                if (cachedNdefMessage == null) {
                    cachedNdefMessage = new NdefMessage(new NdefRecord[]{
                            NdefRecord.createTextRecord("zh", "CloneSource")
                    });
                }
                cachedSourceUid = tag.getId();
                hasSource = true;
                runOnUiThread(() -> {
                    txtSourceInfo.setText("UID: " + Converter.hex(cachedSourceUid)
                            + "\n✓ 已讀取 " + cachedNdefMessage.getRecords().length + " 筆 NDEF 記錄");
                    txtStatus.setText("來源已就緒，請掃描目標標籤");
                    btnReadSource.setEnabled(false);
                });
            } catch (Exception e) {
                runOnUiThread(() -> txtStatus.setText("讀取失敗: " + e.getMessage()));
            }
        }).start();
    }

    private void writeTarget() {
        if (!hasSource || cachedNdefMessage == null) {
            Toast.makeText(this, "請先讀取來源", Toast.LENGTH_SHORT).show();
            return;
        }
        Tag tag = getCurrentTag();
        if (tag == null) {
            Toast.makeText(this, "請先掃描目標標籤", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Arrays.equals(tag.getId(), cachedSourceUid)) {
            Toast.makeText(this, "來源和目標是同一張卡片！", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(() -> {
            try {
                Ndef ndef = Ndef.get(tag);
                if (ndef != null) {
                    ndef.connect();
                    ndef.writeNdefMessage(cachedNdefMessage);
                    ndef.close();
                    runOnUiThread(() -> txtStatus.setText("✓ 複製成功!"));
                    return;
                }
                NdefFormatable fmt = NdefFormatable.get(tag);
                if (fmt != null) {
                    fmt.connect();
                    fmt.format(cachedNdefMessage);
                    fmt.close();
                    runOnUiThread(() -> txtStatus.setText("✓ 格式化並寫入成功!"));
                    return;
                }
                runOnUiThread(() -> txtStatus.setText("目標標籤不支援 NDEF 寫入"));
            } catch (Exception e) {
                runOnUiThread(() -> txtStatus.setText("寫入失敗: " + e.getMessage()));
            }
        }).start();
    }

    private Tag getCurrentTag() {
        return currentTag;
    }
}
