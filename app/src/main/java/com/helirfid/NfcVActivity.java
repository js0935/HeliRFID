/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.0.1
 */
package com.helirfid;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcV;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class NfcVActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private Tag currentTag;

    TextView txtNfcVInfo, txtNfcVResult;
    Button btnReadAll, btnReadBlock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfcv);

        txtNfcVInfo = findViewById(R.id.txtNfcVInfo);
        txtNfcVResult = findViewById(R.id.txtNfcVResult);
        btnReadAll = findViewById(R.id.btnNfcVReadAll);
        btnReadBlock = findViewById(R.id.btnNfcVReadBlock);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        Intent intent = new Intent(this, getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? PendingIntent.FLAG_MUTABLE
                : PendingIntent.FLAG_UPDATE_CURRENT;
        pendingIntent = PendingIntent.getActivity(this, 0, intent, flags);

        btnReadBlock.setOnClickListener(v -> readTag(false));
        btnReadAll.setOnClickListener(v -> readTag(true));
    }

    private void readTag(boolean readAll) {
        if (currentTag == null) {
            Toast.makeText(this, "請先掃描 ISO 15693 卡片", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                NfcV nfcV = NfcV.get(currentTag);
                if (nfcV == null) {
                    runOnUiThread(() -> txtNfcVResult.setText("不支援 NfcV (ISO 15693)"));
                    return;
                }
                nfcV.connect();

                byte[] uid = currentTag.getId();
                StringBuilder info = new StringBuilder("UID: ");
                for (byte b : uid) info.append(String.format("%02X", b));

                byte[] response = nfcV.transceive(new byte[]{0x01, (byte)0x8B, 0x00});
                if (response != null && response.length >= 1) {
                    int flags = response[0] & 0xFF;
                    info.append("\nSystem Info Flags: 0x").append(String.format("%02X", flags));
                    if (response.length >= 9) {
                        long uidVal = 0;
                        for (int i = 1; i <= 8; i++)
                            uidVal = (uidVal << 8) | (response[i] & 0xFF);
                        info.append("\nIC Reference: 0x").append(String.format("%02X", response[9]));
                        if (response.length >= 11)
                            info.append("\nBlock Size: ").append(response[10] & 0xFF);
                    }
                }

                runOnUiThread(() -> txtNfcVInfo.setText(info.toString()));

                if (readAll) {
                    StringBuilder sb = new StringBuilder();
                    for (int block = 0; block < 64; block++) {
                        byte[] cmd = {0x01, 0x20, (byte) block};
                        byte[] resp = nfcV.transceive(cmd);
                        if (resp != null && resp.length >= 5) {
                            sb.append(String.format("[%03d] ", block));
                            for (int i = 1; i < resp.length; i++)
                                sb.append(String.format("%02X ", resp[i]));
                            sb.append("\n");
                        } else {
                            break;
                        }
                    }
                    final String result = sb.length() > 0 ? sb.toString() : "無資料";
                    runOnUiThread(() -> txtNfcVResult.setText(result));
                } else {
                    byte[] cmd = {0x01, 0x20, 0x00};
                    byte[] resp = nfcV.transceive(cmd);
                    if (resp != null && resp.length >= 5) {
                        StringBuilder sb = new StringBuilder("Block 0: ");
                        for (int i = 1; i < resp.length; i++)
                            sb.append(String.format("%02X ", resp[i]));
                        runOnUiThread(() -> txtNfcVResult.setText(sb.toString()));
                    } else {
                        runOnUiThread(() -> txtNfcVResult.setText("讀取失敗"));
                    }
                }

                nfcV.close();

            } catch (Exception e) {
                runOnUiThread(() -> txtNfcVResult.setText("錯誤: " + e.getMessage()));
            }
        }).start();
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
            txtNfcVInfo.setText(info.toString());
        }
    }
}
