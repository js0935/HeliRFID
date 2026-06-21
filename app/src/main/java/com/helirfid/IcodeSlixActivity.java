package com.helirfid;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class IcodeSlixActivity extends BaseNfcActivity {

    private TextView txtSlixStatus;
    private EditText editSlixAfi;
    private Button btnSlixReadAfi, btnSlixWriteAfi, btnSlixReadDsfid, btnSlixLockAfi;
    private Tag currentTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setUseReaderMode(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icode_slix);

        txtSlixStatus = findViewById(R.id.txtSlixStatus);
        editSlixAfi = findViewById(R.id.editSlixAfi);
        btnSlixReadAfi = findViewById(R.id.btnSlixReadAfi);
        btnSlixWriteAfi = findViewById(R.id.btnSlixWriteAfi);
        btnSlixReadDsfid = findViewById(R.id.btnSlixReadDsfid);
        btnSlixLockAfi = findViewById(R.id.btnSlixLockAfi);

        btnSlixReadAfi.setOnClickListener(v -> readAfi());
        btnSlixWriteAfi.setOnClickListener(v -> writeAfi());
        btnSlixReadDsfid.setOnClickListener(v -> readDsfid());
        btnSlixLockAfi.setOnClickListener(v -> lockAfi());
    }

    private void readAfi() {
        if (currentTag == null) {
            txtSlixStatus.setText("請先感應 ISO 15693 標籤");
            return;
        }
        try {
            NfcV nfcv = NfcV.get(currentTag);
            if (nfcv == null) {
                txtSlixStatus.setText("此標籤不支援 ISO 15693");
                return;
            }
            nfcv.connect();
            byte[] cmd = new byte[]{
                (byte) 0x02,  // Flag: high data rate, address
                (byte) 0x2B,  // Get System Information
                (byte) 0x00   // UID (0 = use addressed mode with UID)
            };
            byte[] resp = nfcv.transceive(cmd);
            nfcv.close();
            if (resp != null && resp.length >= 9) {
                byte afi = resp[7];
                txtSlixStatus.setText("AFI: 0x" + String.format("%02X", afi) + " (" + afi + ")");
            } else {
                txtSlixStatus.setText("讀取 AFI 失敗 (回應長度: " + (resp != null ? resp.length : 0) + ")");
            }
        } catch (Exception e) {
            txtSlixStatus.setText("讀取 AFI 錯誤: " + e.getMessage());
        }
    }

    private void writeAfi() {
        if (currentTag == null) {
            txtSlixStatus.setText("請先感應 ISO 15693 標籤");
            return;
        }
        String afiStr = editSlixAfi.getText().toString().trim();
        if (afiStr.isEmpty()) {
            txtSlixStatus.setText("請輸入 AFI 值 (00-FF)");
            return;
        }
        try {
            byte afi = (byte) Integer.parseInt(afiStr, 16);
            NfcV nfcv = NfcV.get(currentTag);
            if (nfcv == null) {
                txtSlixStatus.setText("此標籤不支援 ISO 15693");
                return;
            }
            nfcv.connect();
            byte[] cmd = new byte[]{
                (byte) 0x02,  // Flag
                (byte) 0x27,  // Write AFI
                (byte) 0x00,  // UID
                afi
            };
            byte[] resp = nfcv.transceive(cmd);
            nfcv.close();
            if (resp != null && resp.length > 0 && (resp[0] & 0x01) == 0) {
                txtSlixStatus.setText("AFI 寫入成功: 0x" + String.format("%02X", afi));
            } else {
                txtSlixStatus.setText("AFI 寫入失敗 (可能已鎖定)");
            }
        } catch (Exception e) {
            txtSlixStatus.setText("寫入 AFI 錯誤: " + e.getMessage());
        }
    }

    private void readDsfid() {
        if (currentTag == null) {
            txtSlixStatus.setText("請先感應 ISO 15693 標籤");
            return;
        }
        try {
            NfcV nfcv = NfcV.get(currentTag);
            if (nfcv == null) {
                txtSlixStatus.setText("此標籤不支援 ISO 15693");
                return;
            }
            nfcv.connect();
            byte[] cmd = new byte[]{
                (byte) 0x02,  // Flag
                (byte) 0x2B,  // Get System Information
                (byte) 0x00   // UID
            };
            byte[] resp = nfcv.transceive(cmd);
            nfcv.close();
            if (resp != null && resp.length >= 8) {
                byte dsfid = resp[6];
                txtSlixStatus.setText("DSFID: 0x" + String.format("%02X", dsfid) + " (" + dsfid + ")");
            } else {
                txtSlixStatus.setText("讀取 DSFID 失敗");
            }
        } catch (Exception e) {
            txtSlixStatus.setText("讀取 DSFID 錯誤: " + e.getMessage());
        }
    }

    private void lockAfi() {
        if (currentTag == null) {
            txtSlixStatus.setText("請先感應 ISO 15693 標籤");
            return;
        }
        try {
            NfcV nfcv = NfcV.get(currentTag);
            if (nfcv == null) {
                txtSlixStatus.setText("此標籤不支援 ISO 15693");
                return;
            }
            nfcv.connect();
            byte[] cmd = new byte[]{
                (byte) 0x02,  // Flag
                (byte) 0x28,  // Lock AFI
                (byte) 0x00   // UID
            };
            byte[] resp = nfcv.transceive(cmd);
            nfcv.close();
            if (resp != null && resp.length > 0 && (resp[0] & 0x01) == 0) {
                txtSlixStatus.setText("AFI 已成功鎖定 (不可修改)");
            } else {
                txtSlixStatus.setText("鎖定 AFI 失敗");
            }
        } catch (Exception e) {
            txtSlixStatus.setText("鎖定 AFI 錯誤: " + e.getMessage());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        currentTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (currentTag == null) return;
        String uid = Converter.hex(currentTag.getId());
        txtSlixStatus.setText("UID: " + uid + "\nICODE SLIX 標籤已就緒");
        vibrate();
    }
}
