package com.helirfid;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class QrScannerActivity extends BaseNfcActivity {

    private TextView txtResult, txtStatus;
    private Button btnScan, btnWriteUrl, btnWriteText;
    private String scannedContent;
    private String scannedFormat;
    private Tag currentTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        txtResult = findViewById(R.id.txtQrResult);
        txtStatus = findViewById(R.id.txtQrStatus);
        btnScan = findViewById(R.id.btnQrScan);
        btnWriteUrl = findViewById(R.id.btnQrWriteUrl);
        btnWriteText = findViewById(R.id.btnQrWriteText);

        btnScan.setOnClickListener(v -> startScan());
        btnWriteUrl.setOnClickListener(v -> writeToTag(true));
        btnWriteText.setOnClickListener(v -> writeToTag(false));

        Tag tag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) currentTag = tag;
    }

    private void startScan() {
        new IntentIntegrator(this)
                .setPrompt("對準 QR Code 或條碼")
                .setBeepEnabled(true)
                .setOrientationLocked(false)
                .setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
                .initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                txtResult.setText("掃描已取消");
            } else {
                scannedContent = result.getContents();
                scannedFormat = result.getFormatName();
                txtResult.setText("格式: " + scannedFormat + "\n內容: " + scannedContent);
                btnWriteUrl.setEnabled(true);
                btnWriteText.setEnabled(true);
                txtStatus.setText("已掃描完成，可將內容寫入 NFC 標籤");
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            currentTag = tag;
            txtStatus.setText("卡片已偵測 (UID: " + Converter.hex(tag.getId()) + ")");
        }
    }

    private void writeToTag(boolean asUrl) {
        if (currentTag == null) {
            Toast.makeText(this, "請先掃描 NFC 標籤", Toast.LENGTH_SHORT).show();
            return;
        }
        if (scannedContent == null) {
            Toast.makeText(this, "請先掃描 QR Code", Toast.LENGTH_SHORT).show();
            return;
        }

        final String content = scannedContent;
        new Thread(() -> {
            try {
                Ndef ndef = Ndef.get(currentTag);
                if (ndef == null) {
                    runOnUiThread(() -> txtStatus.setText("標籤不支援 NDEF"));
                    return;
                }
                ndef.connect();
                String result;
                if (asUrl) {
                    String url = content;
                    if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        url = "http://" + url;
                    }
                    result = NFCWriter.writeNdefMessage(currentTag,
                            android.nfc.NdefRecord.createUri(url));
                } else {
                    result = NFCWriter.writeNdefMessage(currentTag,
                            android.nfc.NdefRecord.createTextRecord("zh", content));
                }
                final String fresult = result;
                runOnUiThread(() -> txtStatus.setText(fresult));
                ndef.close();
            } catch (Exception e) {
                runOnUiThread(() -> txtStatus.setText("寫入失敗: " + e.getMessage()));
            }
        }).start();
    }
}
