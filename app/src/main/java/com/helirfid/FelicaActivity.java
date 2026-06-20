/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.0.1
 */
package com.helirfid;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class FelicaActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private Tag currentTag;

    TextView txtFelicaInfo, txtFelicaResult;
    Button btnRead;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_felica);

        txtFelicaInfo = findViewById(R.id.txtFelicaInfo);
        txtFelicaResult = findViewById(R.id.txtFelicaResult);
        btnRead = findViewById(R.id.btnFelicaRead);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        Intent intent = new Intent(this, getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? PendingIntent.FLAG_MUTABLE
                : PendingIntent.FLAG_UPDATE_CURRENT;
        pendingIntent = PendingIntent.getActivity(this, 0, intent, flags);

        btnRead.setOnClickListener(v -> readFelica());
    }

    private void readFelica() {
        if (currentTag == null) {
            Toast.makeText(this, "請先掃描 FeliCa 卡片", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                NfcF nfcF = NfcF.get(currentTag);
                if (nfcF == null) {
                    runOnUiThread(() -> txtFelicaResult.setText("不支援 NfcF (FeliCa)"));
                    return;
                }
                nfcF.connect();

                byte[] uid = currentTag.getId();
                StringBuilder info = new StringBuilder("IDm: ");
                for (byte b : uid) info.append(String.format("%02X", b));

                byte[] pmm = nfcF.getManufacturer();
                if (pmm != null) {
                    info.append("\nPMm: ");
                    for (byte b : pmm) info.append(String.format("%02X", b));
                }

                byte[] readCmd = {
                        0x06,             // Read Without Encryption
                        0x01,             // Service Code (00 01 = NDEF)
                        0x01,             // Block Count
                        0x00, (byte)0x80  // Block 0
                };
                byte[] resp = nfcF.transceive(readCmd);
                if (resp != null) {
                    info.append("\nRead Response: ");
                    for (byte b : resp) info.append(String.format("%02X ", b));
                }

                byte[] pollingCmd = {
                        0x00,             // Sense All
                        (byte)0xFF, (byte)0xFF, 0x00, 0x00
                };
                try {
                    byte[] pollingResp = nfcF.transceive(pollingCmd);
                    if (pollingResp != null) {
                        info.append("\nPolling Response: ");
                        for (byte b : pollingResp) info.append(String.format("%02X ", b));
                    }
                } catch (Exception e) {
                }

                nfcF.close();

                final String result = info.toString();
                runOnUiThread(() -> {
                    txtFelicaInfo.setText("FeliCa 卡片已偵測");
                    txtFelicaResult.setText(result);
                });

            } catch (Exception e) {
                runOnUiThread(() -> txtFelicaResult.setText("錯誤: " + e.getMessage()));
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
            StringBuilder info = new StringBuilder("卡片已偵測\nIDm: ");
            for (byte b : currentTag.getId()) info.append(String.format("%02X", b));
            txtFelicaInfo.setText(info.toString());
        }
    }
}
