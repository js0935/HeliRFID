package com.helirfid;

import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class FormatTagActivity extends BaseNfcActivity {

    TextView txtResult;
    Button btnFormat, btnQuickFormat, btnClear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_format_tag);

        txtResult = findViewById(R.id.txtFormatTagResult);
        btnFormat = findViewById(R.id.btnFormatNdef);
        btnQuickFormat = findViewById(R.id.btnQuickFormat);
        btnClear = findViewById(R.id.btnClearFormatTag);

        btnClear.setOnClickListener(v -> txtResult.setText(""));

        btnFormat.setOnClickListener(v ->
            txtResult.setText("請將要格式化的標籤靠近手機\n" +
                "將執行完整格式化（寫入 0x00 清除所有資料）"));

        btnQuickFormat.setOnClickListener(v ->
            txtResult.setText("請將要快速格式化的標籤靠近手機\n" +
                "將快速清除 NDEF 訊息區域"));
    }

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) formatTag(tag);
        }
    }

    private void formatTag(Tag tag) {
        try {
            NdefFormatable formatable = NdefFormatable.get(tag);
            if (formatable != null) {
                formatable.connect();
                formatable.format(null);
                formatable.close();
                txtResult.setText("✅ 格式化成功！\n\n標籤已格式化為空白 NDEF 標籤\n可以寫入新的 NDEF 訊息");
                vibrate();
                return;
            }
        } catch (Exception ignored) {}

        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();
                ndef.writeNdefMessage(null);
                ndef.close();
                txtResult.setText("✅ 格式化成功！\n\nNDEF 訊息已清除");
                vibrate();
                return;
            }
        } catch (Exception ignored) {}

        try {
            MifareUltralight mu = MifareUltralight.get(tag);
            if (mu != null) {
                mu.connect();
                int pageCount = 0;
                try {
                    byte[] pages = mu.readPages(0);
                    pageCount = (pages != null) ? 135 : 45;
                } catch (Exception e) {
                    pageCount = 45;
                }
                int formatted = 0;
                for (int pg = 4; pg < pageCount && pg < 0x2C; pg++) {
                    try {
                        mu.writePage(pg, new byte[]{0x00, 0x00, 0x00, 0x00});
                        formatted++;
                    } catch (Exception e) {
                        break;
                    }
                }
                mu.close();
                txtResult.setText("✅ 格式化完成\n已清除 " + formatted + " 頁\n\n標籤記憶體已歸零");
                vibrate();
                return;
            }
        } catch (Exception ignored) {}

        try {
            NfcA nfcA = NfcA.get(tag);
            if (nfcA != null) {
                nfcA.connect();
                txtResult.setText("❌ 不支援格式化\n\n此標籤類型不支援標準格式化\n請確認標籤為 NTAG / Ultralight / NDEF 類型");
                nfcA.close();
                return;
            }
        } catch (Exception ignored) {}

        txtResult.setText("❌ 無法連接標籤\n\n請確認標籤是否靠近手機");
    }
}
