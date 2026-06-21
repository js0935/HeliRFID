/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.3.1
 */
package com.helirfid;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class NtagConfigActivity extends BaseNfcActivity {

    private TextView txtTagInfo, txtCurrentConfig;
    private EditText editUidMirror, editCounterMirror, editNfcCounter, editAskMod;
    private CheckBox chkUidMirror, chkCounterMirror, chkNfcCounter;
    private Button btnReadConfig, btnWriteConfig, btnSetUidMirror, btnSetCounterMirror, btnSetNfcCounter;
    private Tag currentTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ntag_config);

        txtTagInfo = findViewById(R.id.txtNtagCfgTagInfo);
        txtCurrentConfig = findViewById(R.id.txtNtagCfgCurrent);
        editUidMirror = findViewById(R.id.editNtagCfgUidMirror);
        editCounterMirror = findViewById(R.id.editNtagCfgCounterMirror);
        editNfcCounter = findViewById(R.id.editNtagCfgNfcCounter);
        editAskMod = findViewById(R.id.editNtagCfgAskMod);
        chkUidMirror = findViewById(R.id.chkNtagCfgUidMirror);
        chkCounterMirror = findViewById(R.id.chkNtagCfgCounterMirror);
        chkNfcCounter = findViewById(R.id.chkNtagCfgNfcCounter);
        btnReadConfig = findViewById(R.id.btnNtagCfgRead);
        btnWriteConfig = findViewById(R.id.btnNtagCfgWrite);
        btnSetUidMirror = findViewById(R.id.btnNtagCfgSetUidMirror);
        btnSetCounterMirror = findViewById(R.id.btnNtagCfgSetCounterMirror);
        btnSetNfcCounter = findViewById(R.id.btnNtagCfgSetNfcCounter);

        btnReadConfig.setOnClickListener(v -> readConfig());
        btnWriteConfig.setOnClickListener(v -> writeConfig());
        btnSetUidMirror.setOnClickListener(v -> setUidMirror());
        btnSetCounterMirror.setOnClickListener(v -> setCounterMirror());
        btnSetNfcCounter.setOnClickListener(v -> setNfcCounter());
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

    private MifareUltralight getMu() {
        if (currentTag == null) return null;
        return MifareUltralight.get(currentTag);
    }

    private void readConfig() {
        MifareUltralight mu = getMu();
        if (mu == null) {
            Toast.makeText(this, "不支援 MIFARE Ultralight / NTAG", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(() -> {
            try {
                mu.connect();
                byte[] page2 = mu.readPages(2);
                byte[] page3 = mu.readPages(3);
                mu.close();
                StringBuilder sb = new StringBuilder("=== 目前 NTAG 設定 ===\n");
                sb.append("Page 0x02: ").append(Converter.bytesToHex(page2)).append("\n");
                sb.append("Page 0x03: ").append(Converter.bytesToHex(page3)).append("\n\n");
                boolean uidMirror = (page2[0] & 0x01) != 0;
                boolean counterMirror = (page3[0] & 0x01) != 0;
                boolean nfcCounter = (page3[1] & 0x01) != 0;
                sb.append("UID Mirror: ").append(uidMirror ? "啟用" : "停用").append("\n");
                sb.append("Counter Mirror: ").append(counterMirror ? "啟用" : "停用").append("\n");
                sb.append("NFC Counter: ").append(nfcCounter ? "啟用" : "停用").append("\n");
                sb.append("UID Mirror Config: 0x").append(String.format("%02X", page2[0])).append("\n");
                sb.append("Counter Mirror Config: 0x").append(String.format("%02X", page3[0])).append("\n");
                sb.append("NFC Counter Config: 0x").append(String.format("%02X", page3[1])).append("\n");
                sb.append("ASK Modulation: 0x").append(String.format("%02X", page2[1])).append("\n");
                final String res = sb.toString();
                runOnUiThread(() -> {
                    txtCurrentConfig.setText(res);
                    editUidMirror.setText(String.format("%02X", page2[0]));
                    editCounterMirror.setText(String.format("%02X", page3[0]));
                    editNfcCounter.setText(String.format("%02X", page3[1]));
                    editAskMod.setText(String.format("%02X", page2[1]));
                    chkUidMirror.setChecked(uidMirror);
                    chkCounterMirror.setChecked(counterMirror);
                    chkNfcCounter.setChecked(nfcCounter);
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "讀取失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void writeConfig() {
        MifareUltralight mu = getMu();
        if (mu == null) {
            Toast.makeText(this, "不支援 MIFARE Ultralight / NTAG", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(() -> {
            try {
                String uidStr = editUidMirror.getText().toString().trim();
                String cntStr = editCounterMirror.getText().toString().trim();
                String nfcStr = editNfcCounter.getText().toString().trim();
                String askStr = editAskMod.getText().toString().trim();
                if (uidStr.isEmpty() || cntStr.isEmpty() || nfcStr.isEmpty() || askStr.isEmpty()) {
                    runOnUiThread(() -> Toast.makeText(this, "請填寫所有設定值", Toast.LENGTH_SHORT).show());
                    return;
                }
                mu.connect();
                int uidVal = Integer.parseInt(uidStr, 16) & 0xFF;
                int askVal = Integer.parseInt(askStr, 16) & 0xFF;
                mu.writePage(2, new byte[]{(byte) uidVal, (byte) askVal, 0, 0});
                int cntVal = Integer.parseInt(cntStr, 16) & 0xFF;
                int nfcVal = Integer.parseInt(nfcStr, 16) & 0xFF;
                mu.writePage(3, new byte[]{(byte) cntVal, (byte) nfcVal, 0, 0});
                mu.close();
                runOnUiThread(() -> {
                    Toast.makeText(this, "設定寫入成功", Toast.LENGTH_SHORT).show();
                    readConfig();
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "寫入失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void setUidMirror() {
        boolean enable = chkUidMirror.isChecked();
        int val = enable ? 0x01 : 0x00;
        editUidMirror.setText(String.format("%02X", val));
        Toast.makeText(this, "UID Mirror 已設定為 " + (enable ? "啟用" : "停用"), Toast.LENGTH_SHORT).show();
    }

    private void setCounterMirror() {
        boolean enable = chkCounterMirror.isChecked();
        int val = enable ? 0x01 : 0x00;
        editCounterMirror.setText(String.format("%02X", val));
        Toast.makeText(this, "Counter Mirror 已設定為 " + (enable ? "啟用" : "停用"), Toast.LENGTH_SHORT).show();
    }

    private void setNfcCounter() {
        boolean enable = chkNfcCounter.isChecked();
        int val = enable ? 0x01 : 0x00;
        editNfcCounter.setText(String.format("%02X", val));
        Toast.makeText(this, "NFC Counter 已設定為 " + (enable ? "啟用" : "停用"), Toast.LENGTH_SHORT).show();
    }
}
