/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.0.1
 */
package com.helirfid;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class DesfireActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private Tag currentTag;

    TextView txtDesfireInfo, txtDesfireResult;
    Button btnRead, btnFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_desfire);

        txtDesfireInfo = findViewById(R.id.txtDesfireInfo);
        txtDesfireResult = findViewById(R.id.txtDesfireResult);
        btnRead = findViewById(R.id.btnDesfireRead);
        btnFormat = findViewById(R.id.btnDesfireFormat);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        Intent intent = new Intent(this, getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? PendingIntent.FLAG_MUTABLE
                : PendingIntent.FLAG_UPDATE_CURRENT;
        pendingIntent = PendingIntent.getActivity(this, 0, intent, flags);

        btnRead.setOnClickListener(v -> readDesfire());
        btnFormat.setOnClickListener(v -> formatDesfire());
    }

    private void readDesfire() {
        if (currentTag == null) {
            Toast.makeText(this, "請先掃描卡片", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                IsoDep isoDep = IsoDep.get(currentTag);
                if (isoDep == null) {
                    runOnUiThread(() -> txtDesfireResult.setText("不支援 IsoDep"));
                    return;
                }
                isoDep.connect();
                isoDep.setTimeout(5000);

                StringBuilder sb = new StringBuilder();

                byte[] cmac = isoDep.transceive(new byte[]{
                        (byte)0x90, 0x60, 0x00, 0x00, 0x00
                });
                if (cmac != null) {
                    sb.append("GetVersion: ").append(bytesToHex(cmac)).append("\n");
                }

                byte[] apps = isoDep.transceive(new byte[]{
                        (byte)0x90, 0x6A, 0x00, 0x00, 0x00
                });
                if (apps != null) {
                    sb.append("GetApplications: ").append(bytesToHex(apps)).append("\n");
                }

                byte[] uid = isoDep.transceive(new byte[]{
                        (byte)0x90, 0x60, 0x00, 0x00, 0x01, 0x00
                });
                if (uid != null && uid.length > 2) {
                    sb.append("UID: ");
                    for (int i = 0; i < uid.length - 2; i++)
                        sb.append(String.format("%02X", uid[i]));
                    sb.append("\n");
                }

                byte[] ndef = isoDep.transceive(new byte[]{
                        (byte)0x90, 0x6F, 0x00, 0x00, 0x02, 0x00, 0x00, 0x10, 0x00
                });
                if (ndef != null) {
                    sb.append("NDEF Read: ").append(bytesToHex(ndef)).append("\n");
                }

                isoDep.close();

                final String result = sb.length() > 0 ? sb.toString() : "無法讀取 DESFire 資料";
                runOnUiThread(() -> txtDesfireResult.setText(result));

            } catch (Exception e) {
                runOnUiThread(() -> txtDesfireResult.setText("錯誤: " + e.getMessage()));
            }
        }).start();
    }

    private void formatDesfire() {
        Toast.makeText(this, "DESFire 格式化需要金鑸授權，暫未實作", Toast.LENGTH_SHORT).show();
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02X ", b));
        return sb.toString().trim();
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
            info.append("\n技術: ");
            for (String t : currentTag.getTechList())
                info.append(t.substring(t.lastIndexOf('.') + 1)).append(" ");
            txtDesfireInfo.setText(info.toString());
        }
    }
}
