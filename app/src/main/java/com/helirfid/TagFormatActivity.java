package com.helirfid;

import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class TagFormatActivity extends AppCompatActivity {

    TextView txtResult;
    Button btnFormat, btnClear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_format);

        txtResult = findViewById(R.id.txtTagFormatResult);
        btnFormat = findViewById(R.id.btnFormatTag);
        btnClear = findViewById(R.id.btnClearFormat);

        btnClear.setOnClickListener(v -> txtResult.setText(""));

        btnFormat.setOnClickListener(v -> txtResult.setText("請將 MIFARE Classic 卡片靠近手機\n\n將會嘗試以預設金鑰 (FF FF FF FF FF FF) 驗證並格式化所有 Sector"));
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
            if (tag != null) formatTag(tag);
        }
    }

    private void formatTag(Tag tag) {
        MifareClassic mfc = MifareClassic.get(tag);
        if (mfc == null) {
            txtResult.setText("此卡片不是 MIFARE Classic 類型");
            return;
        }

        try {
            mfc.connect();

            byte[] key = { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
            int sectorCount = mfc.getSectorCount();

            StringBuilder sb = new StringBuilder();
            sb.append("格式化結果:\n\n");

            int formatted = 0, failed = 0;
            for (int sector = 0; sector < sectorCount; sector++) {
                try {
                    if (mfc.authenticateSectorWithKeyA(sector, key)) {
                        int blockIndex = mfc.sectorToBlock(sector);
                        int lastBlock = blockIndex + mfc.getBlockCountInSector(sector) - 1;

                        byte[] trailer = new byte[16];
                        trailer[0] = (byte) 0xFF; trailer[1] = (byte) 0xFF;
                        trailer[2] = (byte) 0xFF; trailer[3] = (byte) 0xFF;
                        trailer[4] = (byte) 0xFF; trailer[5] = (byte) 0xFF;
                        trailer[6] = (byte) 0xFF; trailer[7] = (byte) 0x07;
                        trailer[8] = (byte) 0x80; trailer[9] = (byte) 0x69;
                        trailer[10] = (byte) 0xFF; trailer[11] = (byte) 0xFF;
                        trailer[12] = (byte) 0xFF; trailer[13] = (byte) 0xFF;
                        trailer[14] = (byte) 0xFF; trailer[15] = (byte) 0xFF;

                        mfc.writeBlock(lastBlock, trailer);
                        formatted++;
                        sb.append("  Sector ").append(sector).append(" ✓\n");
                    } else {
                        failed++;
                    }
                } catch (Exception e) {
                    failed++;
                    sb.append("  Sector ").append(sector).append(" ✗ ").append(e.getMessage()).append("\n");
                }
            }

            mfc.close();

            sb.append("\n成功: ").append(formatted).append(" Sector\n");
            sb.append("失敗: ").append(failed).append(" Sector\n");

            if (formatted > 0) {
                sb.append("\n格式化完成！金鑰已重置為 FF FF FF FF FF FF");
                sb.append("\n存取條件已重置為出廠預設 (FF 07 80 69)");
            }

            txtResult.setText(sb.toString());

        } catch (Exception e) {
            txtResult.setText("格式化失敗: " + e.getMessage());
        }
    }
}
