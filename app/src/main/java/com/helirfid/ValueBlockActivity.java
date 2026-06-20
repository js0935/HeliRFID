/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.0.1
 */
package com.helirfid;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ValueBlockActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private Tag currentTag;

    EditText editSector, editBlock, editValue, editKeyA;
    TextView txtResult;
    Button btnRead, btnInc, btnDec, btnWrite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_value_block);

        editSector = findViewById(R.id.editVbSector);
        editBlock = findViewById(R.id.editVbBlock);
        editValue = findViewById(R.id.editVbValue);
        editKeyA = findViewById(R.id.editVbKeyA);
        txtResult = findViewById(R.id.txtVbResult);
        btnRead = findViewById(R.id.btnVbRead);
        btnInc = findViewById(R.id.btnVbInc);
        btnDec = findViewById(R.id.btnVbDec);
        btnWrite = findViewById(R.id.btnVbWrite);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        Intent intent = new Intent(this, getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? PendingIntent.FLAG_MUTABLE
                : PendingIntent.FLAG_UPDATE_CURRENT;
        pendingIntent = PendingIntent.getActivity(this, 0, intent, flags);

        btnRead.setOnClickListener(v -> operateValueBlock(0));
        btnInc.setOnClickListener(v -> operateValueBlock(1));
        btnDec.setOnClickListener(v -> operateValueBlock(2));
        btnWrite.setOnClickListener(v -> operateValueBlock(3));
    }

    private void operateValueBlock(int mode) {
        if (currentTag == null) {
            Toast.makeText(this, "請先掃描卡片", Toast.LENGTH_SHORT).show();
            return;
        }

        int sector, block, value;
        try {
            sector = Integer.parseInt(editSector.getText().toString().trim());
            block = Integer.parseInt(editBlock.getText().toString().trim());
            value = Integer.parseInt(editValue.getText().toString().trim());
        } catch (Exception e) {
            txtResult.setText("請輸入有效的數值");
            return;
        }

        String keyA = editKeyA.getText().toString().trim().replace(" ", "");
        if (keyA.length() != 12) keyA = "FFFFFFFFFFFF";

        final int fMode = mode;
        final int fSector = sector;
        final int fBlock = block;
        final int fValue = value;
        final byte[] keyBytes = hexToBytes(keyA);

        new Thread(() -> {
            try {
                MifareClassic mfc = MifareClassic.get(currentTag);
                if (mfc == null) {
                    runOnUiThread(() -> txtResult.setText("不支援 MIFARE Classic"));
                    return;
                }
                mfc.connect();
                mfc.setTimeout(5000);

                boolean auth = mfc.authenticateSectorWithKeyA(fSector, keyBytes);
                if (!auth) {
                    mfc.close();
                    runOnUiThread(() -> txtResult.setText("金鑰驗證失敗"));
                    return;
                }

                int blockIndex = mfc.sectorToBlock(fSector) + (fBlock % 4);
                String result = "";

                switch (fMode) {
                    case 0:
                        byte[] data = mfc.readBlock(blockIndex);
                        result = "區塊 " + blockIndex + " 原始資料:\n" + bytesToHex(data) + "\n";
                        result += MifareUtils.decodeValueBlock(data);
                        break;
                    case 1:
                        mfc.increment(blockIndex, fValue);
                        byte[] afterInc = mfc.readBlock(blockIndex);
                        result = "遞增 " + fValue + " 成功\n" + bytesToHex(afterInc) + "\n"
                                + MifareUtils.decodeValueBlock(afterInc);
                        break;
                    case 2:
                        mfc.decrement(blockIndex, fValue);
                        byte[] afterDec = mfc.readBlock(blockIndex);
                        result = "遞減 " + fValue + " 成功\n" + bytesToHex(afterDec) + "\n"
                                + MifareUtils.decodeValueBlock(afterDec);
                        break;
                    case 3:
                        byte[] encoded = MifareUtils.encodeValueBlock(fValue, (byte) fBlock);
                        mfc.writeBlock(blockIndex, encoded);
                        result = "寫入數值 " + fValue + " 成功\n" + bytesToHex(encoded);
                        break;
                }

                mfc.close();
                final String r = result;
                runOnUiThread(() -> txtResult.setText(r));

            } catch (Exception e) {
                runOnUiThread(() -> txtResult.setText("操作失敗: " + e.getMessage()));
            }
        }).start();
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02X ", b));
        return sb.toString().trim();
    }

    private byte[] hexToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2)
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        return data;
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
            txtResult.setText(info.toString());
        }
    }
}
