package com.helirfid;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class OriginalityCheckActivity extends BaseNfcActivity {

    private TextView txtOrigStatus, txtOrigResult;
    private Button btnOrigCheck, btnOrigRead;
    private Tag currentTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setUseReaderMode(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_originality_check);

        txtOrigStatus = findViewById(R.id.txtOrigStatus);
        txtOrigResult = findViewById(R.id.txtOrigResult);
        btnOrigCheck = findViewById(R.id.btnOrigCheck);
        btnOrigRead = findViewById(R.id.btnOrigRead);

        btnOrigCheck.setOnClickListener(v -> checkOriginality());
        btnOrigRead.setOnClickListener(v -> readSignature());
    }

    private void checkOriginality() {
        if (currentTag == null) {
            txtOrigStatus.setText("請先感應 NFC 標籤");
            return;
        }
        try {
            byte[] uid = currentTag.getId();
            String uidHex = Converter.hex(uid);

            MifareUltralight mful = MifareUltralight.get(currentTag);
            if (mful != null) {
                mful.connect();
                byte[] sig = mful.transceive(new byte[]{
                    (byte) 0x50,  // READ_SIG
                    (byte) 0x00
                });
                mful.close();

                StringBuilder sigHex = new StringBuilder();
                for (byte b : sig) sigHex.append(String.format("%02X", b));

                txtOrigResult.setText("UID: " + uidHex + "\n簽章 (32 bytes):\n" + sigHex);
                txtOrigStatus.setText("NTAG 原廠簽章讀取成功 (32 bytes)");
                return;
            }

            NfcA nfcA = NfcA.get(currentTag);
            if (nfcA != null) {
                nfcA.connect();
                short sakVal = nfcA.getSak();
                byte[] sak = new byte[]{(byte) sakVal};
                byte[] atqa = nfcA.getAtqa();
                nfcA.close();
                txtOrigResult.setText("UID: " + uidHex
                    + "\nSAK: " + String.format("%02X", sakVal)
                    + "\nATQA: " + (atqa != null ? String.format("%02X%02X", atqa[0], atqa[1]) : "N/A")
                    + "\n晶片: " + guessChipType(sak, uid));
                txtOrigStatus.setText("MIFARE 晶片辨識完成");
            } else {
                txtOrigResult.setText("UID: " + uidHex + "\n不支援原廠簽章檢查");
                txtOrigStatus.setText("不支援的標籤類型");
            }
        } catch (Exception e) {
            txtOrigResult.setText("檢查失敗: " + e.getMessage());
            txtOrigStatus.setText("原廠簽章檢查錯誤");
        }
    }

    private void readSignature() {
        if (currentTag == null) {
            txtOrigStatus.setText("請先感應 NFC 標籤");
            return;
        }
        try {
            MifareUltralight mful = MifareUltralight.get(currentTag);
            if (mful == null) {
                txtOrigResult.setText("此標籤不支援 NTAG 簽章讀取\n僅 NTAG/Ultralight 系列支援");
                txtOrigStatus.setText("不支援的標籤類型");
                return;
            }
            mful.connect();
            byte[] sig = mful.transceive(new byte[]{
                (byte) 0x50,  // READ_SIG
                (byte) 0x00
            });
            mful.close();

            StringBuilder sb = new StringBuilder("ECC 簽章 (32 bytes):\n");
            for (int i = 0; i < sig.length; i++) {
                sb.append(String.format("%02X ", sig[i]));
                if ((i + 1) % 16 == 0) sb.append("\n");
            }

            String uidHex = Converter.hex(currentTag.getId());
            sb.insert(0, "UID: " + uidHex + "\n\n");

            txtOrigResult.setText(sb.toString().trim());
            txtOrigStatus.setText("NTAG ECC 簽章讀取成功");
        } catch (Exception e) {
            txtOrigResult.setText("讀取簽章失敗: " + e.getMessage());
            txtOrigStatus.setText("簽章讀取錯誤");
        }
    }

    private String guessChipType(byte[] sak, byte[] uid) {
        if (sak == null || sak.length == 0) return "未知";
        int s = sak[0] & 0xFF;
        if (s == 0x08) return "MIFARE Classic 1K";
        if (s == 0x18) return "MIFARE Classic 4K";
        if (s == 0x88) return "MIFARE Classic Mini";
        if (s == 0x00) {
            if (uid != null && uid.length == 4) return "MIFARE Ultralight / NTAG";
            return "MIFARE Ultralight";
        }
        if (s == 0x20) return "ISO 14443-4 (DESFire/Plus)";
        return String.format("未知 (SAK=%02X)", s);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        currentTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (currentTag == null) return;
        String uid = Converter.hex(currentTag.getId());
        txtOrigStatus.setText("偵測到標籤 UID: " + uid + "\n點擊檢查原廠簽章");
        txtOrigResult.setText("");
        vibrate();
    }
}
