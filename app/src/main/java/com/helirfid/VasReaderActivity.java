package com.helirfid;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;

public class VasReaderActivity extends BaseNfcActivity {

    private TextView txtStatus, txtResult;
    private Button btnRead;
    private Tag currentTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vas_reader);

        txtStatus = findViewById(R.id.txtVasStatus);
        txtResult = findViewById(R.id.txtVasResult);
        btnRead = findViewById(R.id.btnVasRead);

        btnRead.setOnClickListener(v -> doRead());

        Tag tag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) currentTag = tag;
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

    private void doRead() {
        if (currentTag == null) {
            toast("請先掃描 NFC 卡片");
            return;
        }

        new Thread(() -> {
            try {
                Ndef ndef = Ndef.get(currentTag);
                if (ndef == null) {
                    runOnUiThread(() -> txtResult.setText("此標籤不支援 NDEF 讀取\n可能為 MIFARE Classic 或純 RFID 標籤"));
                    return;
                }
                ndef.connect();
                NdefMessage msg = ndef.getNdefMessage();
                ndef.close();

                if (msg == null || msg.getRecords().length == 0) {
                    runOnUiThread(() -> txtResult.setText("標籤無 NDEF 資料"));
                    return;
                }

                StringBuilder sb = new StringBuilder("=== NDEF 內容 ===\n\n");
                boolean foundVas = false;
                boolean foundSmartTap = false;

                for (int i = 0; i < msg.getRecords().length; i++) {
                    NdefRecord r = msg.getRecords()[i];
                    String typeStr = new String(r.getType(), StandardCharsets.US_ASCII);
                    byte[] pl = r.getPayload();

                    sb.append("記錄 #").append(i + 1).append("\n");
                    sb.append("TNF: ").append(r.getTnf()).append("\n");
                    sb.append("Type: ").append(typeStr).append("\n");

                    if (r.getTnf() == NdefRecord.TNF_EXTERNAL_TYPE) {
                        if (typeStr.startsWith("apple.com:vas")) {
                            foundVas = true;
                            sb.append("*** Apple VAS 記錄 ***\n");
                            decodeAppleVas(pl, sb);
                        } else if (typeStr.startsWith("google.com:smarttap")
                                || typeStr.contains("smarttap")
                                || typeStr.startsWith("google.com")) {
                            foundSmartTap = true;
                            sb.append("*** Google Smart Tap 記錄 ***\n");
                            decodeSmartTap(pl, sb);
                        } else {
                            sb.append("外部類型: ").append(typeStr).append("\n");
                            sb.append("資料 (hex): ").append(Converter.hex(pl)).append("\n");
                        }
                    } else if (r.getTnf() == NdefRecord.TNF_MIME_MEDIA) {
                        sb.append("MIME: ").append(typeStr).append("\n");
                        sb.append("資料: ").append(tryDecode(pl)).append("\n");
                    } else if (r.getTnf() == NdefRecord.TNF_WELL_KNOWN) {
                        decodeWellKnown(r, sb);
                    } else {
                        sb.append("資料 (hex): ").append(Converter.hex(pl)).append("\n");
                    }
                    sb.append("---\n\n");
                }

                if (!foundVas && !foundSmartTap) {
                    sb.append("⚠ 未發現 Apple VAS 或 Google Smart Tap 記錄\n");
                    sb.append("僅顯示標準 NDEF 內容\n");
                }

                final String res = sb.toString();
                runOnUiThread(() -> txtResult.setText(res));

            } catch (Exception e) {
                runOnUiThread(() -> txtResult.setText("讀取錯誤: " + e.getMessage()));
            }
        }).start();
    }

    private void decodeAppleVas(byte[] pl, StringBuilder sb) {
        if (pl == null || pl.length < 4) {
            sb.append("  資料長度不足\n");
            return;
        }
        sb.append("  原始資料 (").append(pl.length).append(" bytes):\n");
        sb.append("  ").append(MifareUtils.hexWithAscii(pl)).append("\n\n");
        int pos = 0;
        while (pos + 4 <= pl.length) {
            int tag = pl[pos] & 0xFF;
            int len = ((pl[pos + 1] & 0xFF) << 8) | (pl[pos + 2] & 0xFF);
            int type = pl[pos + 3] & 0xFF;
            if (pos + 4 + len > pl.length) break;
            byte[] val = new byte[len];
            System.arraycopy(pl, pos + 4, val, 0, len);
            String tagName;
            switch (tag) {
                case 0x01: tagName = "VAS 資料"; break;
                case 0x02: tagName = "行動優惠"; break;
                case 0x03: tagName = "識別碼"; break;
                default: tagName = "標籤 0x" + String.format("%02X", tag);
            }
            sb.append("  ").append(tagName).append(": ");
            if (type == 0x01) {
                sb.append(new String(val, StandardCharsets.UTF_8));
            } else {
                sb.append(Converter.hex(val));
            }
            sb.append("\n");
            pos += 4 + len;
        }
    }

    private void decodeSmartTap(byte[] pl, StringBuilder sb) {
        if (pl == null) {
            sb.append("  無資料\n");
            return;
        }
        sb.append("  原始資料 (").append(pl.length).append(" bytes):\n");
        sb.append("  ").append(MifareUtils.hexWithAscii(pl)).append("\n");
        String text = tryDecode(pl);
        if (text != null && text.length() > 0) {
            sb.append(" 文字: ").append(text).append("\n");
        }
    }

    private void decodeWellKnown(NdefRecord r, StringBuilder sb) {
        String type = new String(r.getType(), StandardCharsets.US_ASCII);
        byte[] pl = r.getPayload();
        if ("T".equals(type)) {
            int langLen = pl[0] & 0x3F;
            String t = new String(pl, 1 + langLen, pl.length - 1 - langLen, StandardCharsets.UTF_8);
            sb.append("文字: ").append(t).append("\n");
        } else if ("U".equals(type)) {
            byte prefix = pl[0];
            String[] prefixes = {"", "http://www.", "https://www.", "http://", "https://",
                    "tel:", "mailto:", "ftp://"};
            String url = (prefix < prefixes.length ? prefixes[prefix] : "")
                    + new String(pl, 1, pl.length - 1, StandardCharsets.UTF_8);
            sb.append("URL: ").append(url).append("\n");
        } else if ("Sp".equals(type)) {
            sb.append("Smart Poster (內嵌記錄)\n");
        } else {
            sb.append("類型: ").append(type).append("\n");
            sb.append("資料: ").append(tryDecode(pl)).append("\n");
        }
    }

    private String tryDecode(byte[] pl) {
        try {
            if (pl == null || pl.length == 0) return "(空)";
            if (pl.length > 3) {
                int langLen = pl[0] & 0x3F;
                if (pl.length > langLen + 1) {
                    String t = new String(pl, 1 + langLen, pl.length - 1 - langLen, StandardCharsets.UTF_8);
                    if (t.length() > 0 && t.length() < 500) return t;
                }
            }
            return new String(pl, StandardCharsets.UTF_8).replace('\n', ' ').trim();
        } catch (Exception e) {
            return Converter.hex(pl);
        }
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}
