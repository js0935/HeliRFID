/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.0.1
 */
package com.helirfid;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class WriteActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private Tag currentTag;

    EditText editNdefText, editNdefExtra, editSector, editBlock, editBlockData, editULPage, editULData, editBlock0Data, editNtagPassword;
    TextView txtWriteResult, txtUidSource;
    Button btnWriteNdef, btnWriteBlock, btnWriteULPage, btnReadUidSource, btnWriteUidClone, btnWriteBlock0;
    Button btnSetNtagPassword, btnRemoveNtagPassword, btnLockNtag, btnFormatNtag;
    Spinner spinnerNdefType;

    private String storedUid = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        txtWriteResult = findViewById(R.id.txtWriteResult);
        txtUidSource = findViewById(R.id.txtUidSource);
        spinnerNdefType = findViewById(R.id.spinnerNdefType);
        editNdefText = findViewById(R.id.editNdefText);
        editNdefExtra = findViewById(R.id.editNdefExtra);
        editSector = findViewById(R.id.editSector);
        editBlock = findViewById(R.id.editBlock);
        editBlockData = findViewById(R.id.editBlockData);
        editULPage = findViewById(R.id.editULPage);
        editULData = findViewById(R.id.editULData);
        editBlock0Data = findViewById(R.id.editBlock0Data);
        editNtagPassword = findViewById(R.id.editNtagPassword);

        btnWriteNdef = findViewById(R.id.btnWriteNdef);
        btnWriteBlock = findViewById(R.id.btnWriteBlock);
        btnWriteULPage = findViewById(R.id.btnWriteULPage);
        btnReadUidSource = findViewById(R.id.btnReadUidSource);
        btnWriteUidClone = findViewById(R.id.btnWriteUidClone);
        btnWriteBlock0 = findViewById(R.id.btnWriteBlock0);
        btnSetNtagPassword = findViewById(R.id.btnSetNtagPassword);
        btnRemoveNtagPassword = findViewById(R.id.btnRemoveNtagPassword);
        btnLockNtag = findViewById(R.id.btnLockNtag);
        btnFormatNtag = findViewById(R.id.btnFormatNtag);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        Intent intent = new Intent(this, getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? PendingIntent.FLAG_MUTABLE
                : PendingIntent.FLAG_UPDATE_CURRENT;

        pendingIntent = PendingIntent.getActivity(this, 0, intent, flags);

        spinnerNdefType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int pos, long id) {
                editNdefExtra.setVisibility(pos >= 2 ? android.view.View.VISIBLE : android.view.View.GONE);
                if (pos == 0) {
                    editNdefText.setHint("請輸入文字內容");
                } else if (pos == 1) {
                    editNdefText.setHint("請輸入網址 (URL)");
                } else if (pos == 2) {
                    editNdefText.setHint("收件人 Email");
                    editNdefExtra.setHint("主旨");
                } else if (pos == 3) {
                    editNdefText.setHint("電話號碼");
                } else if (pos == 4) {
                    editNdefText.setHint("電話號碼");
                    editNdefExtra.setHint("簡訊內容");
                } else if (pos == 5) {
                    editNdefText.setHint("緯度,經度 (如 25.033,121.565)");
                } else if (pos == 6) {
                    editNdefText.setHint("WiFi SSID");
                    editNdefExtra.setHint("密碼");
                } else if (pos == 7) {
                    editNdefText.setHint("姓名");
                    editNdefExtra.setHint("電話,Email (用逗號分隔)");
                } else if (pos == 8) {
                    editNdefText.setHint("藍牙 MAC 位址");
                    editNdefExtra.setHint("裝置名稱");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        btnWriteNdef.setOnClickListener(v -> {
            if (currentTag == null) {
                Toast.makeText(this, "請先掃描 NFC 卡", Toast.LENGTH_SHORT).show();
                return;
            }
            String text = editNdefText.getText().toString().trim();
            if (TextUtils.isEmpty(text)) {
                Toast.makeText(this, "請輸入內容", Toast.LENGTH_SHORT).show();
                return;
            }
            String extra = editNdefExtra.getText().toString().trim();
            int type = spinnerNdefType.getSelectedItemPosition();
            NdefRecord record = null;
            switch (type) {
                case 0: record = NdefRecord.createTextRecord("zh", text); break;
                case 1: record = NFCWriter.createUrlRecord(text); break;
                case 2: record = NFCWriter.createEmailRecord(text, extra, ""); break;
                case 3: record = NFCWriter.createPhoneRecord(text); break;
                case 4: record = NFCWriter.createSmsRecord(text, extra); break;
                case 5: {
                    String[] parts = text.split(",");
                    if (parts.length >= 2) {
                        try {
                            record = NFCWriter.createGeoLocationRecord(
                                    Double.parseDouble(parts[0].trim()),
                                    Double.parseDouble(parts[1].trim()));
                        } catch (NumberFormatException e) {
                            Toast.makeText(this, "請輸入有效的緯度,經度", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else {
                        Toast.makeText(this, "請輸入緯度,經度", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    break;
                }
                case 6: record = NFCWriter.createWifiConfigRecord(text, extra, "WPA"); break;
                case 7: {
                    String[] parts = extra.split(",");
                    String phone = parts.length >= 1 ? parts[0].trim() : "";
                    String email = parts.length >= 2 ? parts[1].trim() : "";
                    record = NFCWriter.createVCardRecord(text, phone, email);
                    break;
                }
                case 8: record = NFCWriter.createBluetoothRecord(text, extra); break;
            }
            if (record == null) {
                Toast.makeText(this, "無法建立 NDEF 記錄", Toast.LENGTH_SHORT).show();
                return;
            }
            String result = NFCWriter.writeNdefMessage(currentTag, record);
            txtWriteResult.setText(result);
            Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
        });

        btnWriteBlock.setOnClickListener(v -> {
            if (currentTag == null) {
                Toast.makeText(this, "請先掃描 NFC 卡", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                int sector = Integer.parseInt(editSector.getText().toString().trim());
                int block = Integer.parseInt(editBlock.getText().toString().trim());
                String hexData = editBlockData.getText().toString().trim().replace(" ", "");

                if (hexData.length() != 32) {
                    Toast.makeText(this, "請輸入 16 bytes (32 個 16 進制字元)", Toast.LENGTH_SHORT).show();
                    return;
                }

                byte[] data = hexStringToByteArray(hexData);
                String result = NFCWriter.writeMifareBlock(currentTag, sector, block, data);
                txtWriteResult.setText(result);
                Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "請輸入有效的磁區和區塊編號", Toast.LENGTH_SHORT).show();
            }
        });

        btnWriteULPage.setOnClickListener(v -> {
            if (currentTag == null) {
                Toast.makeText(this, "請先掃描 NFC 卡", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                int page = Integer.parseInt(editULPage.getText().toString().trim());
                String hexData = editULData.getText().toString().trim().replace(" ", "");

                if (hexData.length() != 8) {
                    Toast.makeText(this, "請輸入 4 bytes (8 個 16 進制字元)", Toast.LENGTH_SHORT).show();
                    return;
                }

                byte[] data = hexStringToByteArray(hexData);
                String result = NFCWriter.writeUltralightPage(currentTag, page, data);
                txtWriteResult.setText(result);
                Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "請輸入有效的頁碼", Toast.LENGTH_SHORT).show();
            }
        });

        btnReadUidSource.setOnClickListener(v -> {
            if (currentTag == null) {
                Toast.makeText(this, "請先掃描來源卡片", Toast.LENGTH_SHORT).show();
                return;
            }
            byte[] uid = currentTag.getId();
            StringBuilder sb = new StringBuilder();
            for (byte b : uid) sb.append(String.format("%02X", b));
            storedUid = sb.toString();
            txtUidSource.setText("來源 UID: " + storedUid);
            Toast.makeText(this, "已讀取來源 UID", Toast.LENGTH_SHORT).show();
        });

        btnWriteUidClone.setOnClickListener(v -> {
            if (storedUid == null) {
                Toast.makeText(this, "請先讀取來源 UID", Toast.LENGTH_SHORT).show();
                return;
            }
            if (currentTag == null) {
                Toast.makeText(this, "請先掃描目標卡片", Toast.LENGTH_SHORT).show();
                return;
            }
            byte[] uidBytes = hexStringToByteArray(storedUid);
            byte[] block0 = new byte[16];
            System.arraycopy(uidBytes, 0, block0, 0, Math.min(uidBytes.length, 16));
            String result = NFCWriter.writeManufacturerBlock(currentTag, block0);
            txtWriteResult.setText(result);
            Toast.makeText(this, result, Toast.LENGTH_LONG).show();
        });

        btnWriteBlock0.setOnClickListener(v -> {
            if (currentTag == null) {
                Toast.makeText(this, "請先掃描 NFC 卡", Toast.LENGTH_SHORT).show();
                return;
            }
            String hexData = editBlock0Data.getText().toString().trim().replace(" ", "");
            if (hexData.length() != 32) {
                Toast.makeText(this, "請輸入 16 bytes (32 個 16 進制字元)", Toast.LENGTH_SHORT).show();
                return;
            }
            byte[] data = hexStringToByteArray(hexData);
            String result = NFCWriter.writeManufacturerBlock(currentTag, data);
            txtWriteResult.setText(result);
            Toast.makeText(this, result, Toast.LENGTH_LONG).show();
        });

        btnSetNtagPassword.setOnClickListener(v -> {
            if (currentTag == null) {
                Toast.makeText(this, "請先掃描 NFC 卡", Toast.LENGTH_SHORT).show();
                return;
            }
            String pwdHex = editNtagPassword.getText().toString().trim().replace(" ", "");
            if (pwdHex.length() != 8) {
                Toast.makeText(this, "請輸入 4 bytes 密碼 (8 hex 字元)", Toast.LENGTH_SHORT).show();
                return;
            }
            byte[] pwd = hexStringToByteArray(pwdHex);
            String result = NfcUltralightUtils.setPassword(currentTag, pwd);
            txtWriteResult.setText(result);
            Toast.makeText(this, result, Toast.LENGTH_LONG).show();
        });

        btnRemoveNtagPassword.setOnClickListener(v -> {
            if (currentTag == null) {
                Toast.makeText(this, "請先掃描 NFC 卡", Toast.LENGTH_SHORT).show();
                return;
            }
            String result = NfcUltralightUtils.removePassword(currentTag);
            txtWriteResult.setText(result);
            Toast.makeText(this, result, Toast.LENGTH_LONG).show();
        });

        btnLockNtag.setOnClickListener(v -> {
            if (currentTag == null) {
                Toast.makeText(this, "請先掃描 NFC 卡", Toast.LENGTH_SHORT).show();
                return;
            }
            String result = NfcUltralightUtils.lockTag(currentTag);
            txtWriteResult.setText(result);
            Toast.makeText(this, result, Toast.LENGTH_LONG).show();
        });

        btnFormatNtag.setOnClickListener(v -> {
            if (currentTag == null) {
                Toast.makeText(this, "請先掃描 NFC 卡", Toast.LENGTH_SHORT).show();
                return;
            }
            String result = NfcUltralightUtils.formatUltralight(currentTag);
            txtWriteResult.setText(result);
            Toast.makeText(this, result, Toast.LENGTH_LONG).show();
        });
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
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
            txtWriteResult.setText("已偵測到卡片，可以進行寫入操作");
            Toast.makeText(this, "卡片已偵測", Toast.LENGTH_SHORT).show();
        }
    }
}
