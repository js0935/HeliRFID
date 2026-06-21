/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.3.1
 */
package com.helirfid;

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

import java.nio.charset.StandardCharsets;

public class NdefValidatorActivity extends BaseNfcActivity {

    private TextView txtNdefInfo, txtNdefResult;
    private Button btnValidate, btnClear;
    private Tag currentTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ndef_validator);

        txtNdefInfo = findViewById(R.id.txtNdefValInfo);
        txtNdefResult = findViewById(R.id.txtNdefValResult);
        btnValidate = findViewById(R.id.btnNdefValValidate);
        btnClear = findViewById(R.id.btnNdefValClear);

        btnValidate.setOnClickListener(v -> doValidate());
        btnClear.setOnClickListener(v -> txtNdefResult.setText(""));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) return;
        currentTag = tag;
        String uid = Converter.hex(tag.getId());
        txtNdefInfo.setText("卡片已偵測\nUID: " + uid);
    }

    private void doValidate() {
        if (currentTag == null) {
            Toast.makeText(this, "請先掃描 NFC 標籤", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(() -> {
            try {
                Ndef ndef = Ndef.get(currentTag);
                if (ndef == null) {
                    runOnUiThread(() -> txtNdefResult.setText("不支援 NDEF"));
                    return;
                }
                ndef.connect();
                NdefMessage msg = ndef.getNdefMessage();
                ndef.close();
                if (msg == null) {
                    runOnUiThread(() -> txtNdefResult.setText("標籤無 NDEF 資料"));
                    return;
                }
                StringBuilder sb = new StringBuilder("=== NDEF 合規性檢查 ===\n");
                NdefRecord[] records = msg.getRecords();
                sb.append("記錄總數: ").append(records.length).append("\n\n");
                int passCount = 0, failCount = 0;
                for (int i = 0; i < records.length; i++) {
                    NdefRecord r = records[i];
                    sb.append("--- 記錄 ").append(i + 1).append(" ---\n");
                    short tnf = r.getTnf();
                    boolean tnfOk = tnf >= 0 && tnf <= 6;
                    sb.append(tnfOk ? "✅" : "❌").append(" TNF: ").append(tnf).append(" (有效範圍 0-6)\n");
                    if (tnfOk) passCount++; else failCount++;
                    byte[] type = r.getType();
                    boolean typeOk;
                    switch (tnf) {
                        case NdefRecord.TNF_EMPTY:
                            typeOk = (type == null || type.length == 0);
                            break;
                        case NdefRecord.TNF_WELL_KNOWN:
                        case NdefRecord.TNF_MIME_MEDIA:
                        case NdefRecord.TNF_EXTERNAL_TYPE:
                            typeOk = type != null && type.length > 0;
                            break;
                        case NdefRecord.TNF_ABSOLUTE_URI:
                        case NdefRecord.TNF_UNKNOWN:
                        case NdefRecord.TNF_UNCHANGED:
                            typeOk = (type == null || type.length == 0);
                            break;
                        default:
                            typeOk = false;
                    }
                    String typeStr = (type != null) ? new String(type, StandardCharsets.US_ASCII) : "null";
                    sb.append(typeOk ? "✅" : "❌").append(" 記錄類型有效: ").append(typeStr).append("\n");
                    if (typeOk) passCount++; else failCount++;
                    byte[] payload = r.getPayload();
                    boolean payloadOk = payload != null;
                    sb.append(payloadOk ? "✅" : "❌").append(" 有效負載: ").append(payload != null ? payload.length + " bytes" : "null").append("\n");
                    if (payloadOk) passCount++; else failCount++;
                    boolean emptyOk = (tnf != NdefRecord.TNF_EMPTY || (payload != null && payload.length == 0));
                    sb.append(emptyOk ? "✅" : "❌").append(" TNF_EMPTY 無 payload\n");
                    if (emptyOk) passCount++; else failCount++;
                    boolean unchangedFirstOk = !(tnf == NdefRecord.TNF_UNCHANGED && i == 0);
                    sb.append(unchangedFirstOk ? "✅" : "❌").append(" 首筆記錄非 TNF_UNCHANGED\n");
                    if (unchangedFirstOk) passCount++; else failCount++;
                    sb.append("\n");
                }
                sb.append("=== 總結 ===\n通過: ").append(passCount);
                sb.append(", 失敗: ").append(failCount);
                int total = passCount + failCount;
                sb.append(", 合規率: ").append(total > 0 ? (passCount * 100 / total) : 0).append("%\n");
                final String res = sb.toString();
                runOnUiThread(() -> txtNdefResult.setText(res));
            } catch (Exception e) {
                runOnUiThread(() -> txtNdefResult.setText("檢查錯誤: " + e.getMessage()));
            }
        }).start();
    }
}
