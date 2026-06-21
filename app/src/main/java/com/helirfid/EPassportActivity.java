/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.3.1
 */
package com.helirfid;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class EPassportActivity extends BaseNfcActivity {

    private EditText editDocNum, editDob, editExpiry;
    private TextView txtResult;
    private Button btnCompute;

    private byte[] ksEnc, ksMac;
    private Tag currentTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_epassport);

        editDocNum = findViewById(R.id.editEpassDocNum);
        editDob = findViewById(R.id.editEpassDob);
        editExpiry = findViewById(R.id.editEpassExpiry);
        txtResult = findViewById(R.id.txtEpassResult);
        btnCompute = findViewById(R.id.btnEpassComputeKeys);

        btnCompute.setOnClickListener(v -> computeKeys());

        Tag tag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) currentTag = tag;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            currentTag = tag;
            appendLog("偵測到卡片 (UID: " + Converter.hex(tag.getId()) + ")");
            if (ksEnc != null && ksMac != null) {
                doPassportRead(tag);
            } else {
                appendLog("請先輸入 MRZ 並計算 BAC 金鑰");
            }
        }
    }

    private void computeKeys() {
        String docNum = editDocNum.getText().toString().trim();
        String dob = editDob.getText().toString().trim();
        String expiry = editExpiry.getText().toString().trim();

        if (docNum.isEmpty() || dob.isEmpty() || expiry.isEmpty()) {
            appendLog("請填寫所有 MRZ 欄位");
            return;
        }

        try {
            String mrzInfo = docNum + dob + expiry;
            while (mrzInfo.length() < 32) mrzInfo += "<";
            mrzInfo = mrzInfo.substring(0, 32);

            byte[] mrzBytes = mrzInfo.getBytes(StandardCharsets.US_ASCII);
            byte[] sha1 = MessageDigest.getInstance("SHA-1").digest(mrzBytes);

            byte[] kSeed = Arrays.copyOf(sha1, 16);
            ksEnc = deriveKey(kSeed, 1);
            ksMac = deriveKey(kSeed, 2);

            StringBuilder sb = new StringBuilder();
            sb.append("=== BAC 金鑰計算 ===\n\n");
            sb.append("MRZ 資訊: ").append(mrzInfo).append("\n");
            sb.append("KSeed: ").append(Converter.bytesToHex(kSeed)).append("\n\n");
            sb.append("KS_ENC: ").append(Converter.bytesToHex(ksEnc)).append("\n");
            sb.append("KS_MAC: ").append(Converter.bytesToHex(ksMac)).append("\n\n");
            sb.append("金鑰計算完成，請掃描 ePassport 卡片");

            txtResult.setText(sb.toString());
        } catch (Exception e) {
            appendLog("金鑰計算錯誤: " + e.getMessage());
        }
    }

    private byte[] deriveKey(byte[] kSeed, int c) {
        try {
            byte[] input = Arrays.copyOf(kSeed, kSeed.length + 1);
            input[input.length - 1] = (byte) c;
            byte[] sha1 = MessageDigest.getInstance("SHA-1").digest(input);
            byte[] keyBytes = Arrays.copyOf(sha1, 16);

            for (int i = 0; i < keyBytes.length; i += 8) {
                int parity = 0;
                for (int j = 0; j < 7; j++) {
                    parity ^= (keyBytes[i + j] >> j) & 1;
                }
                if ((parity & 1) == 0) keyBytes[i + 7] ^= 1;
            }

            return keyBytes;
        } catch (Exception e) {
            return null;
        }
    }

    private void doPassportRead(Tag tag) {
        new Thread(() -> {
            try {
                IsoDep isodep = IsoDep.get(tag);
                if (isodep == null) {
                    appendLog("不支援 ISO-DEP (IsoDep)");
                    return;
                }
                isodep.connect();
                isodep.setTimeout(5000);

                appendLog("已連線 IsoDep");

                byte[] selectEfCom = hexToBytes("00A4040C0C315449432E4546312E300082");
                byte[] response = isodep.transceive(selectEfCom);
                appendLog("SELECT EF.COM: " + Converter.bytesToHex(response));

                byte[] readBinary = hexToBytes("00B0000000");
                response = isodep.transceive(readBinary);
                appendLog("READ BINARY (EF.COM): " + Converter.bytesToHex(response));

                byte[] selectDg1 = hexToBytes("00A4040C023101");
                response = isodep.transceive(selectDg1);
                appendLog("SELECT DG1: " + Converter.bytesToHex(response));

                byte[] readDg1 = hexToBytes("00B0000000");
                response = isodep.transceive(readDg1);
                appendLog("READ BINARY (DG1): " + Converter.bytesToHex(response));

                byte[] selectDg2 = hexToBytes("00A4040C023102");
                response = isodep.transceive(selectDg2);
                appendLog("SELECT DG2: " + Converter.bytesToHex(response));

                byte[] readDg2 = hexToBytes("00B0000000");
                response = isodep.transceive(readDg2);
                appendLog("READ BINARY (DG2): " + Converter.bytesToHex(response));

                isodep.close();

                StringBuilder sb = new StringBuilder();
                sb.append("=== ePassport 讀取完成 ===\n\n");
                sb.append("DG1 (MRZ 資料): ").append(Converter.bytesToHex(selectDg1)).append("...\n");
                sb.append("DG2 (臉部影像): ").append(Converter.bytesToHex(selectDg2)).append("...\n\n");
                sb.append("註：此為簡化實作，展示 BAC 流程。\n");
                sb.append("完整實作需處理金鑰交換與安全訊息。");

                final String res = sb.toString();
                runOnUiThread(() -> appendLog(res));

            } catch (Exception e) {
                appendLog("APDU 錯誤: " + e.getMessage());
            }
        }).start();
    }

    private void appendLog(String msg) {
        runOnUiThread(() -> {
            String prev = txtResult.getText().toString();
            txtResult.setText(prev + "\n" + msg);
        });
    }

    private byte[] hexToBytes(String s) {
        String clean = s.replaceAll("[^0-9A-Fa-f]", "");
        int len = clean.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2)
            data[i / 2] = (byte) ((Character.digit(clean.charAt(i), 16) << 4)
                    + Character.digit(clean.charAt(i + 1), 16));
        return data;
    }
}
