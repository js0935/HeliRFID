package com.helirfid;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

public class NtagPasswordActivity extends BaseNfcActivity {

    private EditText editPwd, editPack, editAuth0;
    private TextView txtResult;
    private Button btnSetPwd, btnLockTag, btnCheckProt;

    private Tag currentTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ntag_password);

        editPwd = findViewById(R.id.editNtagPwd);
        editPack = findViewById(R.id.editNtagPack);
        editAuth0 = findViewById(R.id.editNtagAuth0);
        txtResult = findViewById(R.id.txtNtagPwdResult);
        btnSetPwd = findViewById(R.id.btnNtagSetPwd);
        btnLockTag = findViewById(R.id.btnNtagLockTag);
        btnCheckProt = findViewById(R.id.btnNtagCheckProt);

        editPwd.setText("00000000");
        editPack.setText("0000");
        editAuth0.setText("1");

        btnSetPwd.setOnClickListener(v -> setPassword());
        btnLockTag.setOnClickListener(v -> lockTagReadOnly());
        btnCheckProt.setOnClickListener(v -> checkProtection());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) return;
        currentTag = tag;

        String techs = Arrays.toString(tag.getTechList());
        appendResult("偵測到標籤\nUID: " + Converter.hex(tag.getId()));
        appendResult("技術: " + techs);

        if (!techs.contains("MifareUltralight")) {
            appendResult("警告: 非 NTAG 標籤，可能不支援");
        }
    }

    private void setPassword() {
        if (currentTag == null) {
            Toast.makeText(this, "請先掃描 NFC 標籤", Toast.LENGTH_SHORT).show();
            return;
        }

        String pwdStr = editPwd.getText().toString().trim().replace(" ", "");
        String packStr = editPack.getText().toString().trim().replace(" ", "");
        String auth0Str = editAuth0.getText().toString().trim();

        if (pwdStr.length() != 8) {
            Toast.makeText(this, "密碼需為 8 hex 字元 (4 bytes)", Toast.LENGTH_SHORT).show();
            return;
        }
        if (packStr.length() != 4) {
            Toast.makeText(this, "PACK 需為 4 hex 字元 (2 bytes)", Toast.LENGTH_SHORT).show();
            return;
        }

        int auth0;
        try {
            auth0 = Integer.parseInt(auth0Str);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "AUTH0 需為有效數字", Toast.LENGTH_SHORT).show();
            return;
        }

        final int auth0Final = auth0;
        final byte[] pwd = Converter.hexToBytes(pwdStr);
        final byte[] pack = Converter.hexToBytes(packStr);

        new Thread(() -> {
            try {
                MifareUltralight mu = MifareUltralight.get(currentTag);
                if (mu == null) {
                    runOnUiThread(() -> appendResult("錯誤: 不支援 MIFARE Ultralight"));
                    return;
                }
                mu.connect();
                appendLog("已連線，寫入密碼...");

                // Write PWD (page 0x84)
                byte[] pwdPage = new byte[16];
                System.arraycopy(pwd, 0, pwdPage, 0, 4);
                mu.writePage(0x84, Arrays.copyOfRange(pwdPage, 0, 4));
                appendLog("PWD 已寫入頁面 0x84");

                // Write PACK (page 0x85)
                byte[] packPage = new byte[4];
                System.arraycopy(pack, 0, packPage, 0, 2);
                mu.writePage(0x85, packPage);
                appendLog("PACK 已寫入頁面 0x85");

                // Set PROT (page 0x86) - bit 0 = 1 means PWD required for write
                mu.writePage(0x86, new byte[]{0x01, 0x00, 0x00, 0x00});
                appendLog("PROT 已設定 (寫入需密碼)");

                // Set AUTH0 (page 0x83)
                mu.writePage(0x83, new byte[]{(byte)auth0Final, 0x00, 0x00, 0x00});
                appendLog("AUTH0 已設定 = " + auth0Final);

                mu.close();
                runOnUiThread(() -> appendResult("✓ 密碼保護設定完成!\n密碼: " + Converter.hex(pwd) + "\nPACK: " + Converter.hex(pack)));
            } catch (Exception e) {
                runOnUiThread(() -> appendResult("設定失敗: " + e.getMessage()));
            }
        }).start();
    }

    private void lockTagReadOnly() {
        if (currentTag == null) {
            Toast.makeText(this, "請先掃描 NFC 標籤", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                MifareUltralight mu = MifareUltralight.get(currentTag);
                if (mu == null) {
                    runOnUiThread(() -> appendResult("錯誤: 不支援 MIFARE Ultralight"));
                    return;
                }
                mu.connect();
                appendLog("已連線，執行鎖定...");

                // Read current config from page 0x82 (ACCESS)
                byte[] accessPage = mu.readPages(0x82);

                // For NTAG21x: set lock bits in page 0x02 (dynamic lock)
                // But the safest locking mechanism is writing to the ACCESS page
                // Setting bit 0 of page 0x82 byte 0 to 0 = NFC_CNT_EN disabled
                // Setting AUTH0 to 0 to protect all pages

                // Primary lock: set ACCESS.NFC_PROT to lock all pages as read-only
                // Page 0x82 format: [ACCESS] [RFUI] [RFUI] [RFUI]
                // Setting ACCESS byte = 0x00 means full read-only protection
                mu.writePage(0x82, new byte[]{0x00, 0x00, 0x00, 0x00});
                appendLog("ACCESS 已設定 (全區保護)");

                // Set AUTH0 = 0 to protect all pages
                mu.writePage(0x83, new byte[]{0x00, 0x00, 0x00, 0x00});
                appendLog("AUTH0 = 0 (所有頁面受保護)");

                // Set PROT = 0x01 for write protection
                mu.writePage(0x86, new byte[]{0x01, 0x00, 0x00, 0x00});
                appendLog("PROT = 0x01");

                mu.close();
                runOnUiThread(() -> appendResult("✓ 標籤已鎖定為唯讀!\n注意: 此操作不可逆 (只支援 NTAG21x)"));
            } catch (Exception e) {
                runOnUiThread(() -> appendResult("鎖定失敗: " + e.getMessage()));
            }
        }).start();
    }

    private void checkProtection() {
        if (currentTag == null) {
            Toast.makeText(this, "請先掃描 NFC 標籤", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                MifareUltralight mu = MifareUltralight.get(currentTag);
                if (mu == null) {
                    runOnUiThread(() -> appendResult("錯誤: 不支援 MIFARE Ultralight"));
                    return;
                }
                mu.connect();

                StringBuilder sb = new StringBuilder("=== 保護狀態 ===\n");
                byte[] pg02 = mu.readPages(0x02);
                sb.append("Lock (pg02): ").append(Converter.hex(pg02)).append("\n");

                byte[] pg82 = mu.readPages(0x82);
                sb.append("ACCESS (pg82): ").append(Converter.hex(pg82)).append("\n");

                byte[] pg83 = mu.readPages(0x83);
                sb.append("AUTH0 (pg83): ").append(Converter.hex(pg83)).append("\n");

                byte[] pg84 = mu.readPages(0x84);
                sb.append("PWD (pg84): ").append(Converter.hex(pg84)).append("\n");

                byte[] pg85 = mu.readPages(0x85);
                sb.append("PACK (pg85): ").append(Converter.hex(pg85)).append("\n");

                byte[] pg86 = mu.readPages(0x86);
                sb.append("PROT (pg86): ").append(Converter.hex(pg86)).append("\n");

                mu.close();
                final String result = sb.toString();
                runOnUiThread(() -> appendResult(result));
            } catch (Exception e) {
                runOnUiThread(() -> appendResult("讀取失敗: " + e.getMessage()));
            }
        }).start();
    }

    private void appendResult(String s) {
        String cur = txtResult.getText().toString();
        txtResult.setText(cur + "\n" + s);
    }

    private void appendLog(String s) {
        runOnUiThread(() -> appendResult("  " + s));
    }
}
