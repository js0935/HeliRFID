package com.helirfid;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcA;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class WriteBlock0Activity extends AppCompatActivity {

    TextView txtResult;
    EditText editUid, editSak, editAtqa;
    Button btnBuild, btnWrite, btnClear;
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    IntentFilter[] nfcFilters;
    Tag currentTag;
    byte[] builtBlock0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_block0);

        txtResult = findViewById(R.id.txtWb0Result);
        editUid = findViewById(R.id.editWb0Uid);
        editSak = findViewById(R.id.editWb0Sak);
        editAtqa = findViewById(R.id.editWb0Atqa);
        btnBuild = findViewById(R.id.btnWb0Build);
        btnWrite = findViewById(R.id.btnWb0Write);
        btnClear = findViewById(R.id.btnWb0Clear);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        Intent intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) flags |= PendingIntent.FLAG_MUTABLE;
        pendingIntent = PendingIntent.getActivity(this, 0, intent, flags);
        nfcFilters = new IntentFilter[]{
                new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED),
                new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED),
                new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED),
        };

        btnClear.setOnClickListener(v -> {
            txtResult.setText("");
            builtBlock0 = null;
        });

        btnBuild.setOnClickListener(v -> buildBlock0());

        btnWrite.setOnClickListener(v -> {
            if (currentTag != null) writeBlock0();
            else txtResult.setText("請先將卡片靠近手機");
        });
    }

    private void buildBlock0() {
        String uidStr = editUid.getText().toString().trim().replace(" ", "");
        String sakStr = editSak.getText().toString().trim().replace(" ", "");
        String atqaStr = editAtqa.getText().toString().trim().replace(" ", "");

        if (TextUtils.isEmpty(uidStr) || TextUtils.isEmpty(sakStr) || TextUtils.isEmpty(atqaStr)) {
            txtResult.setText("請輸入 UID、SAK 和 ATQA");
            return;
        }

        try {
            byte[] uid = hexStringToBytes(uidStr);
            if (uid.length != 4 && uid.length != 7) {
                txtResult.setText("UID 必須為 4 或 7 bytes");
                return;
            }
            byte[] sak = hexStringToBytes(sakStr);
            if (sak.length != 1) { txtResult.setText("SAK 必須為 1 byte"); return; }
            byte[] atqa = hexStringToBytes(atqaStr);
            if (atqa.length != 2) { txtResult.setText("ATQA 必須為 2 bytes"); return; }

            builtBlock0 = new byte[16];
            System.arraycopy(uid, 0, builtBlock0, 0, uid.length);

            int bcc = uid[0] ^ uid[1] ^ uid[2] ^ uid[3];
            builtBlock0[4] = (byte)bcc;

            if (uid.length == 7) {
                builtBlock0[5] = sak[0];
                builtBlock0[6] = atqa[0];
                builtBlock0[7] = atqa[1];
            } else {
                builtBlock0[5] = atqa[0];
                builtBlock0[6] = atqa[1];
                builtBlock0[7] = sak[0];
            }

            StringBuilder sb = new StringBuilder();
            sb.append("=== Block 0 建構完成 ===\n\n");
            sb.append(bytesToHex(builtBlock0)).append("\n\n");
            sb.append("解碼:\n");
            sb.append("  UID: ").append(bytesToHex(uid)).append("\n");
            sb.append("  BCC: 0x").append(String.format("%02X", bcc));
            sb.append(" (求值: ").append(bcc == (uid[0]^uid[1]^uid[2]^uid[3]) ? "正確" : "錯誤").append(")\n");
            if (uid.length == 7) {
                sb.append("  SAK: 0x").append(String.format("%02X", sak[0])).append("\n");
                sb.append("  ATQA: ").append(bytesToHex(atqa)).append("\n");
            } else {
                sb.append("  ATQA: ").append(bytesToHex(atqa)).append("\n");
                sb.append("  SAK: 0x").append(String.format("%02X", sak[0])).append("\n");
            }

            txtResult.setText(sb.toString());

        } catch (Exception e) {
            txtResult.setText("建構失敗: " + e.getMessage());
        }
    }

    private void writeBlock0() {
        if (builtBlock0 == null) {
            txtResult.setText("請先點擊「建構 Block 0」");
            return;
        }
        new Thread(() -> {
            try {
                MifareClassic mfc = MifareClassic.get(currentTag);
                if (mfc != null) {
                    mfc.connect();
                    if (!mfc.authenticateSectorWithKeyA(0, MifareClassic.KEY_DEFAULT)) {
                        mfc.close();
                        runOnUiThread(() -> txtResult.setText("無法驗證 Sector 0 (可能需要其他金鑰)"));
                        return;
                    }
                    mfc.writeBlock(0, builtBlock0);
                    mfc.close();
                    runOnUiThread(() -> txtResult.setText("Block 0 寫入成功!\n\n請重新偵測卡片確認變更。"));
                    return;
                }

                NfcA nfcA = NfcA.get(currentTag);
                if (nfcA != null) {
                    nfcA.connect();
                    nfcA.setTimeout(5000);
                    byte[] cmd = new byte[18];
                    cmd[0] = (byte)0xA0;
                    cmd[1] = 0x00;
                    System.arraycopy(builtBlock0, 0, cmd, 2, 16);
                    byte[] resp = nfcA.transceive(cmd);
                    nfcA.close();
                    runOnUiThread(() -> {
                        if (resp != null) {
                            txtResult.setText("Block 0 寫入完成 (回應: " + bytesToHex(resp) + ")");
                        } else {
                            txtResult.setText("Block 0 寫入失敗 (無回應)");
                        }
                    });
                    return;
                }

                runOnUiThread(() -> txtResult.setText("不支援 MIFARE Classic 或 NFC-A"));
            } catch (Exception e) {
                runOnUiThread(() -> txtResult.setText("寫入失敗: " + e.getMessage()));
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null)
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, nfcFilters, null);
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
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            currentTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            txtResult.setText("已偵測到卡片。點擊「等待卡片並寫入 Block 0」。");
        }
    }

    private String bytesToHex(byte[] bytes) {
        if (bytes == null) return "";
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02X ", b));
        return sb.toString().trim();
    }

    private byte[] hexStringToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2)
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        return data;
    }
}
