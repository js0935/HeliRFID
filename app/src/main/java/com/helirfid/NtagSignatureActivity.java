package com.helirfid;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;

public class NtagSignatureActivity extends BaseNfcActivity {

    private TextView txtInfo, txtResult;
    private Tag currentTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ntag_signature);

        txtInfo = findViewById(R.id.txtNtagSigInfo);
        txtResult = findViewById(R.id.txtNtagSigResult);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) return;
        currentTag = tag;
        txtInfo.setText("卡片已偵測\nUID: " + Converter.hex(tag.getId()));
        analyzeSignature();
    }

    private void analyzeSignature() {
        new Thread(() -> {
            try {
                MifareUltralight mu = MifareUltralight.get(currentTag);
                if (mu == null) {
                    runOnUiThread(() -> txtResult.setText("不支援 MIFARE Ultralight/NTAG"));
                    return;
                }
                mu.connect();

                StringBuilder sb = new StringBuilder();
                sb.append("=== NTAG 簽章分析 ===\n\n");

                // Read pages 0-4 (UID + internal + lock)
                byte[] pages0 = mu.readPages(0);
                sb.append("Pages 0-3 (UID/Internal):\n");
                for (int i = 0; i < pages0.length; i += 4) {
                    sb.append(String.format("  Page %d: ", i / 4));
                    for (int j = 0; j < 4; j++)
                        sb.append(String.format("%02X ", pages0[i + j]));
                    sb.append("\n");
                }

                // UID is in pages 0-2 (first 7 bytes)
                StringBuilder uid = new StringBuilder();
                for (int i = 0; i < 7; i++)
                    uid.append(String.format("%02X", pages0[i]));
                sb.append("\nUID (7 bytes): ").append(uid).append("\n");

                // Read capability container (pages 0xE0-0xE3)
                try {
                    byte[] cc = mu.readPages(0xE0);
                    sb.append("\n=== Capability Container (CC) ===\n");
                    sb.append("CC bytes: ");
                    for (int i = 0; i < cc.length; i++)
                        sb.append(String.format("%02X ", cc[i]));
                    sb.append("\n");

                    // Parse CC
                    if (cc.length >= 4) {
                        int magic = ((cc[0] & 0xFF) << 8) | (cc[1] & 0xFF);
                        sb.append("Magic Number: 0x").append(String.format("%04X", magic));
                        sb.append(magic == 0xE103 ? " (Valid NDEF Magic)" : " (Unknown)").append("\n");

                        int version = (cc[2] >> 4) & 0x0F;
                        int access = cc[2] & 0x0F;
                        sb.append("Version: ").append(version).append(", Access: 0x").append(String.format("%01X", access)).append("\n");

                        int memSize = cc[3] & 0xFF;
                        sb.append("Memory Size: ").append(memSize * 8).append(" bytes\n");
                    }
                } catch (Exception e) {
                    sb.append("\nCC 讀取失敗 (可能不支援): ").append(e.getMessage()).append("\n");
                }

                // Read signature data (last 32 bytes)
                // For NTAG213: signature at pages 0x2D-0x34 (32 bytes = 8 pages)
                // Try to find the max page and read signature area
                sb.append("\n=== 簽章資料 (ECC Signature) ===\n");
                boolean sigFound = false;

                // Try reading from known signature areas
                int[] sigStartPages = {0x2D, 0x3D, 0x7D, 0x85};
                for (int startPage : sigStartPages) {
                    try {
                        byte[] sig = mu.readPages(startPage);
                        if (sig != null && sig.length >= 4) {
                            sb.append("簽章起始 Page: 0x").append(String.format("%02X", startPage)).append("\n");
                            for (int i = 0; i < 32; i += 4) {
                                if (startPage + i / 4 <= 0xFF) {
                                    try {
                                        byte[] pageData = mu.readPages(startPage + i / 4);
                                        sb.append(String.format("  Page 0x%02X: ", startPage + i / 4));
                                        for (int j = 0; j < 4; j++)
                                            sb.append(String.format("%02X ", pageData[j]));
                                        sb.append("\n");
                                    } catch (Exception e) {
                                        break;
                                    }
                                }
                            }
                            sigFound = true;
                            break;
                        }
                    } catch (Exception e) { }
                }

                if (sigFound) {
                    sb.append("\n簽章狀態: 簽章資料已讀取\n");
                    sb.append("注意: 完整的 ECC 驗證需使用 NXP 公開金鑰，此為展示讀取功能\n");
                } else {
                    sb.append("簽章資料: 未找到或此標籤不支援 NXP 簽章\n");
                    sb.append("NTAG213/215/216 在最後 32 bytes 儲存 NXP ECC 簽章\n");
                }

                // Try to read NDEF data if present
                try {
                    android.nfc.tech.Ndef ndef = android.nfc.tech.Ndef.get(currentTag);
                    if (ndef != null) {
                        ndef.connect();
                        android.nfc.NdefMessage msg = ndef.getNdefMessage();
                        if (msg != null) {
                            sb.append("\n=== NDEF 資料 ===\n");
                            sb.append("記錄數: ").append(msg.getRecords().length).append("\n");
                            for (android.nfc.NdefRecord r : msg.getRecords()) {
                                byte[] payload = r.getPayload();
                                if (payload != null) {
                                    String text = new String(payload, StandardCharsets.UTF_8).replace('\n', ' ').replace('\r', ' ');
                                    sb.append("  ").append(text.substring(0, Math.min(50, text.length()))).append("\n");
                                }
                            }
                        }
                        ndef.close();
                    }
                } catch (Exception e) {
                    sb.append("\nNDEF 讀取: ").append(e.getMessage()).append("\n");
                }

                mu.close();

                final String res = sb.toString();
                runOnUiThread(() -> txtResult.setText(res));

            } catch (Exception e) {
                runOnUiThread(() -> txtResult.setText("錯誤: " + e.getMessage()));
            }
        }).start();
    }
}
