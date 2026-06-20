package com.helirfid;

import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AuthMapActivity extends AppCompatActivity {

    TextView txtResult;
    Button btnScan, btnClear;

    private static final byte[][] KEY_DICT = {
        { (byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF },
        { (byte)0xA0,(byte)0xA1,(byte)0xA2,(byte)0xA3,(byte)0xA4,(byte)0xA5 },
        { (byte)0xD3,(byte)0xF7,(byte)0xD3,(byte)0xF7,(byte)0xD3,(byte)0xF7 },
        { (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00 },
        { (byte)0xB0,(byte)0xB1,(byte)0xB2,(byte)0xB3,(byte)0xB4,(byte)0xB5 },
        { (byte)0x4D,(byte)0x3A,(byte)0x99,(byte)0xCB,(byte)0x34,(byte)0x0B },
        { (byte)0x1A,(byte)0x98,(byte)0x2C,(byte)0x7E,(byte)0x45,(byte)0x9A },
        { (byte)0xAA,(byte)0xBB,(byte)0xCC,(byte)0xDD,(byte)0xEE,(byte)0xFF },
        { (byte)0x11,(byte)0x22,(byte)0x33,(byte)0x44,(byte)0x55,(byte)0x66 },
        { (byte)0xAB,(byte)0xCD,(byte)0xEF,(byte)0x12,(byte)0x34,(byte)0x56 }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_map);

        txtResult = findViewById(R.id.txtAuthMapResult);
        btnScan = findViewById(R.id.btnAuthMapScan);
        btnClear = findViewById(R.id.btnAuthMapClear);

        btnClear.setOnClickListener(v -> txtResult.setText(""));
        btnScan.setOnClickListener(v -> txtResult.setText("請將 MIFARE Classic 卡片靠近手機"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        NfcManager nfcManager = (NfcManager) getSystemService(NFC_SERVICE);
        NfcAdapter nfcAdapter = nfcManager.getDefaultAdapter();
        if (nfcAdapter != null) {
            android.app.PendingIntent pi = android.app.PendingIntent.getActivity(this, 0,
                    new android.content.Intent(this, getClass())
                            .addFlags(android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP),
                    android.app.PendingIntent.FLAG_MUTABLE);
            nfcAdapter.enableForegroundDispatch(this, pi, null, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        NfcManager nfcManager = (NfcManager) getSystemService(NFC_SERVICE);
        NfcAdapter nfcAdapter = nfcManager.getDefaultAdapter();
        if (nfcAdapter != null) nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) scanAuth(tag);
        }
    }

    private void scanAuth(Tag tag) {
        MifareClassic mfc = MifareClassic.get(tag);
        if (mfc == null) {
            txtResult.setText("此卡片不是 MIFARE Classic");
            return;
        }

        try {
            mfc.connect();
            int sectorCount = mfc.getSectorCount();

            StringBuilder sb = new StringBuilder();
            sb.append("驗證地圖 (10 組常見金鑰)\n\n");
            sb.append("圖例: ✓=可讀  ✗=鎖定  -=跳過\n\n");

            sb.append("     ");
            for (int k = 0; k < KEY_DICT.length; k++) {
                sb.append(String.format("K%02d ", k));
            }
            sb.append("  Sector\n");

            for (int sector = 0; sector < sectorCount; sector++) {
                sb.append(String.format("S%02d: ", sector));
                boolean anyOk = false;

                for (int k = 0; k < KEY_DICT.length; k++) {
                    if (sector == 0 && k > 0) {
                        sb.append("  -");
                        continue;
                    }
                    try {
                        boolean ok = mfc.authenticateSectorWithKeyA(sector, KEY_DICT[k]);
                        if (ok) {
                            sb.append("  ✓");
                            anyOk = true;
                        } else {
                            sb.append("  ✗");
                        }
                    } catch (Exception e) {
                        sb.append("  E");
                    }
                }

                sb.append(anyOk ? "  OK\n" : "  LOCKED\n");
            }

            mfc.close();

            int okCount = 0;
            for (int s = 0; s < sectorCount; s++) {
                for (int k = 0; k < KEY_DICT.length; k++) {
                    mfc.connect();
                    try {
                        if (mfc.authenticateSectorWithKeyA(s, KEY_DICT[k])) {
                            okCount++;
                            break;
                        }
                    } catch (Exception e) { }
                }
            }

            sb.append("\n可存取 Sector: ").append(okCount).append(" / ").append(sectorCount);

            txtResult.setText(sb.toString());

        } catch (Exception e) {
            txtResult.setText("掃描失敗: " + e.getMessage());
        }
    }
}
