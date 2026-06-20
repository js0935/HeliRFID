/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.0.1
 */
package com.helirfid;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class VerifyActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private Tag currentTag;

    EditText editVerifyUid, editVerifyBcc, editVerifyAtqa, editVerifySak;
    TextView txtVerifyResult;
    Button btnVerify, btnQuickVerify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);

        editVerifyUid = findViewById(R.id.editVerifyUid);
        editVerifyBcc = findViewById(R.id.editVerifyBcc);
        editVerifyAtqa = findViewById(R.id.editVerifyAtqa);
        editVerifySak = findViewById(R.id.editVerifySak);
        txtVerifyResult = findViewById(R.id.txtVerifyResult);
        btnVerify = findViewById(R.id.btnVerify);
        btnQuickVerify = findViewById(R.id.btnQuickVerify);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        Intent intent = new Intent(this, getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? PendingIntent.FLAG_MUTABLE
                : PendingIntent.FLAG_UPDATE_CURRENT;
        pendingIntent = PendingIntent.getActivity(this, 0, intent, flags);

        btnVerify.setOnClickListener(v -> verify());

        btnQuickVerify.setOnClickListener(v -> {
            if (currentTag == null) {
                Toast.makeText(this, "請先掃描卡片", Toast.LENGTH_SHORT).show();
                return;
            }
            quickVerify();
        });
    }

    private void verify() {
        String uid = editVerifyUid.getText().toString().trim().replace(" ", "").replace(":", "");
        String bccStr = editVerifyBcc.getText().toString().trim().replace(" ", "");
        String atqaStr = editVerifyAtqa.getText().toString().trim().replace(" ", "");
        String sakStr = editVerifySak.getText().toString().trim().replace(" ", "");

        StringBuilder sb = new StringBuilder();
        sb.append("═══ 驗證結果 ═══\n");

        if (!uid.isEmpty()) {
            if (uid.length() == 8) {
                byte bcc = 0;
                for (int i = 0; i < 8; i += 2)
                    bcc ^= (byte) Integer.parseInt(uid.substring(i, i + 2), 16);
                sb.append("UID: ").append(uid);
                sb.append("  BCC: ").append(String.format("%02X", bcc));

                if (bccStr.isEmpty()) {
                    sb.append(" (計算完成)\n");
                    editVerifyBcc.setText(String.format("%02X", bcc));
                } else {
                    byte expected = (byte) Integer.parseInt(bccStr, 16);
                    sb.append(bcc == expected ? " ✅ 符合" : " ❌ 不符 (期望 " + bccStr + ")");
                    sb.append("\n");
                }
            } else {
                sb.append("UID 長度錯誤 (需要 4 bytes)\n");
            }
        }

        if (!atqaStr.isEmpty() && atqaStr.length() == 4) {
            int atqa = Integer.parseInt(atqaStr, 16);
            sb.append("ATQA: ").append(atqaStr);
            if ((atqa & 0x0002) != 0) sb.append(" (UID 4 bytes)");
            if ((atqa & 0x0044) != 0) sb.append(" (UID 7 bytes)");
            sb.append("\n");
        }

        if (!sakStr.isEmpty() && sakStr.length() == 2) {
            int sak = Integer.parseInt(sakStr, 16);
            sb.append("SAK: ").append(sakStr);
            if ((sak & 0x08) != 0) sb.append(" (MIFARE Classic)");
            if ((sak & 0x10) != 0) sb.append(" (MIFARE Plus/DESFire)");
            if ((sak & 0x20) != 0) sb.append(" (MIFARE Pro/Desfire)");
            if ((sak & 0x40) != 0) sb.append(" (UID 非完整)");
            sb.append("\n");
        }

        txtVerifyResult.setText(sb.toString());
    }

    private void quickVerify() {
        new Thread(() -> {
            try {
                NfcA nfcA = NfcA.get(currentTag);
                if (nfcA == null) {
                    runOnUiThread(() -> txtVerifyResult.setText("不支援 NfcA"));
                    return;
                }
                nfcA.connect();

                byte[] uid = currentTag.getId();
                byte[] atqa = nfcA.getAtqa();
                short sak = nfcA.getSak();

                StringBuilder sb = new StringBuilder();
                sb.append("═══ 快速驗證 ═══\n");
                sb.append("UID: ");
                for (byte b : uid) sb.append(String.format("%02X", b));
                sb.append("\n");

                if (uid.length == 4) {
                    byte bcc = 0;
                    for (byte b : uid) bcc ^= b;
                    sb.append("BCC: ").append(String.format("%02X", bcc));
                    sb.append(" (有效 4-byte UID)\n");
                } else {
                    sb.append("BCC: N/A (非 4-byte UID)\n");
                }

                sb.append("ATQA: ").append(String.format("%02X %02X", atqa[0], atqa[1]));
                int atqaVal = (atqa[0] & 0xFF) | ((atqa[1] & 0xFF) << 8);
                if ((atqaVal & 0x0002) != 0) sb.append(" (UID bits 0-3)");
                sb.append("\n");

                sb.append("SAK: ").append(String.format("%02X", sak & 0xFF));
                if ((sak & 0x08) != 0) sb.append(" (MIFARE Classic)");
                if ((sak & 0x20) != 0) sb.append(" (ISO 14443-4)");
                sb.append("\n");

                sb.append("UID 長度: ").append(uid.length).append(" bytes\n");
                sb.append("卡片類型: ");
                for (String t : currentTag.getTechList()) {
                    String s = t.substring(t.lastIndexOf('.') + 1);
                    sb.append(s).append(" ");
                }
                sb.append("\n");

                runOnUiThread(() -> {
                    editVerifyUid.setText(bytesToHex(uid).replace(" ", ""));
                    txtVerifyResult.setText(sb.toString());
                });

                nfcA.close();

            } catch (Exception e) {
                runOnUiThread(() -> txtVerifyResult.setText("錯誤: " + e.getMessage()));
            }
        }).start();
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02X", b));
        return sb.toString();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null)
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null)
            nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            currentTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            StringBuilder info = new StringBuilder("卡片已偵測\nUID: ");
            for (byte b : currentTag.getId()) info.append(String.format("%02X", b));
            txtVerifyResult.setText(info.toString());
        }
    }
}
