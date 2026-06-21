package com.helirfid;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

public class TamperCheckActivity extends BaseNfcActivity {

    private TextView txtInfo, txtResult;
    private Tag currentTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tamper_check);

        txtInfo = findViewById(R.id.txtTamperInfo);
        txtResult = findViewById(R.id.txtTamperResult);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) return;
        currentTag = tag;
        txtInfo.setText("卡片已偵測\nUID: " + Converter.hex(tag.getId())
                + "\n技術: " + Arrays.toString(tag.getTechList()));
        checkSecurity();
    }

    private void checkSecurity() {
        new Thread(() -> {
            try {
                StringBuilder sb = new StringBuilder();
                sb.append("=== 標籤安全檢查 ===\n\n");

                int type = analyzeTagType(sb);
                sb.append("\n");

                switch (type) {
                    case 0: checkNtag(sb); break;
                    case 1: checkMifareClassic(sb); break;
                    case 2: checkGenericNdef(sb); break;
                    default: sb.append("不支援此卡片類型的安全檢查\n"); break;
                }

                sb.append("\n=== 總結 ===\n");
                sb.append(getSummary(sb.toString()));

                final String res = sb.toString();
                runOnUiThread(() -> txtResult.setText(res));

            } catch (Exception e) {
                runOnUiThread(() -> txtResult.setText("錯誤: " + e.getMessage()));
            }
        }).start();
    }

    private int analyzeTagType(StringBuilder sb) {
        MifareClassic mc = MifareClassic.get(currentTag);
        if (mc != null) {
            sb.append("卡型: MIFARE Classic");
            int sectors = mc.getSectorCount();
            int blocks = mc.getBlockCount();
            sb.append(" (").append(sectors).append(" 磁區, ").append(blocks).append(" 區塊)\n");
            return 1;
        }
        MifareUltralight mu = MifareUltralight.get(currentTag);
        if (mu != null) {
            sb.append("卡型: MIFARE Ultralight / NTAG\n");
            return 0;
        }
        Ndef ndef = Ndef.get(currentTag);
        if (ndef != null) {
            sb.append("卡型: NDEF 相容\n");
            return 2;
        }
        NfcA nfca = NfcA.get(currentTag);
        if (nfca != null) {
            sb.append("卡型: ISO 14443A (NFC-A)\n");
            return 2;
        }
        return -1;
    }

    private void checkNtag(StringBuilder sb) {
        try {
            MifareUltralight mu = MifareUltralight.get(currentTag);
            if (mu == null) { sb.append("無法取得 Ultralight 連線\n"); return; }
            mu.connect();

            // Read page 0 (UID)
            byte[] p0 = mu.readPages(0);
            sb.append("\n[1] UID / 晶片資訊\n");
            sb.append("UID (Pages 0-1): ");
            sb.append(Converter.bytesToHex(Arrays.copyOf(p0, 7))).append("\n");

            // Read page 2 (lock bytes)
            byte[] p2 = mu.readPages(2);
            int lock0 = p2[0] & 0xFF;
            int lock1 = p2[1] & 0xFF;
            sb.append("\n[2] Lock Bytes (Page 2)\n");
            sb.append("Lock0: 0x").append(String.format("%02X", lock0));
            sb.append("  Lock1: 0x").append(String.format("%02X", lock1)).append("\n");

            // Analyze lock bits for permanent protection
            if ((lock0 & 0x04) != 0) {
                sb.append("⚠ Lock0 bit2=1 → 部分區塊已永久鎖定 (OTP)\n");
            }
            if ((lock0 & 0x08) != 0) {
                sb.append("⚠ Lock0 bit3=1 → 靜態鎖定啟用\n");
            }
            if ((lock0 & 0x01) != 0 || (lock0 & 0x02) != 0) {
                sb.append("⚠ Lock0 低位元非零 → 部分記憶體區塊已保護\n");
            }
            if (lock0 == 0 && lock1 == 0) {
                sb.append("✓ Lock 皆為 0 → 無永久寫入保護\n");
            }

            // Try to detect tamper (NTAG213TT specific)
            sb.append("\n[3] 篡改偵測 (NTAG213TT/223TT)\n");
            boolean hasTamper = false;
            try {
                byte[] tePage = mu.readPages(0xE1);
                sb.append("Tamper Evidence Page 0xE1: ");
                for (byte b : tePage) sb.append(String.format("%02X ", b));
                sb.append("\n");
                boolean tampered = (tePage[0] != 0);
                if (tampered) {
                    sb.append("⚠ 偵測到篡改跡象！\n");
                } else {
                    sb.append("✓ 無篡改跡象\n");
                }
                hasTamper = true;
            } catch (Exception e) {
                sb.append("(非 TT 型號，無硬體篡改偵測)\n");
            }

            // OTP check
            sb.append("\n[4] OTP 頁面 (Page 0xE4)\n");
            try {
                byte[] otp = mu.readPages(0xE4);
                boolean isWritten = false;
                for (byte b : otp) if ((b & 0xFF) != 0) { isWritten = true; break; }
                sb.append(isWritten ? "⚠ OTP 已寫入 (不可逆)\n" : "✓ OTP 尚未使用\n");
            } catch (Exception e) {
                sb.append("讀取失敗\n");
            }

            // Capability container
            sb.append("\n[5] Capability Container\n");
            try {
                byte[] cc = mu.readPages(3);
                sb.append("Page 3: ");
                for (byte b : cc) sb.append(String.format("%02X ", b));
                sb.append("\n");
                if ((cc[0] & 0xFF) == 0xE1) {
                    int ver = (cc[1] >> 4) & 0xF;
                    int size = ((cc[2] & 0xFF) << 8) | 0x07;
                    sb.append("NDEF 版本: ").append(ver).append(".0\n");
                    sb.append("可用大小: ").append(size).append(" bytes\n");
                }
            } catch (Exception ignored) {}

            mu.close();

        } catch (Exception e) {
            sb.append("讀取錯誤: ").append(e.getMessage()).append("\n");
        }
        checkNdefWritable(sb);
    }

    private void checkMifareClassic(StringBuilder sb) {
        try {
            MifareClassic mc = MifareClassic.get(currentTag);
            if (mc == null) return;
            mc.connect();

            int sectors = mc.getSectorCount();
            sb.append("\n[1] 磁區概覽 (").append(sectors).append(" 磁區)\n");

            int authCount = 0;
            int readCount = 0;
            for (int s = 0; s < Math.min(sectors, 16); s++) {
                try {
                    boolean auth = mc.authenticateSectorWithKeyA(s, MifareClassic.KEY_DEFAULT);
                    if (auth) {
                        authCount++;
                        int bIndex = mc.sectorToBlock(s);
                        byte[] data = mc.readBlock(bIndex);
                        if (data != null) readCount++;
                    }
                } catch (Exception ignored) {}
            }
            sb.append("可用預設金鑰 A 驗證: ").append(authCount).append("/").append(Math.min(sectors, 16)).append(" 磁區\n");
            float ratio = (float) readCount / Math.min(sectors, 16) * 100;
            sb.append("可讀取率: ").append(String.format("%.0f", ratio)).append("%\n");

            if (authCount == sectors) {
                sb.append("✓ 全部磁區可使用預設金鑰存取\n");
            } else if (authCount > 0) {
                sb.append("⚠ 部分磁區使用非預設金鑰 (或需 Key B)\n");
            } else {
                sb.append("⚠ 預設金鑰皆無法存取 → 卡片已重新鎖定\n");
            }

            mc.close();

        } catch (Exception e) {
            sb.append("MIFARE 檢查失敗: ").append(e.getMessage()).append("\n");
        }
        checkNdefWritable(sb);
    }

    private void checkGenericNdef(StringBuilder sb) {
        checkNdefWritable(sb);

        // NfcA advanced info
        try {
            NfcA nfca = NfcA.get(currentTag);
            if (nfca != null) {
                sb.append("\nNFC-A 參數:\n");
                sb.append("  SAK: 0x").append(String.format("%02X", nfca.getSak())).append("\n");
                sb.append("  ATQA: 0x").append(String.format("%04X", nfca.getAtqa())).append("\n");
            }
        } catch (Exception ignored) {}
    }

    private void checkNdefWritable(StringBuilder sb) {
        sb.append("\n=== NDEF 寫入保護 ===\n");
        try {
            Ndef ndef = Ndef.get(currentTag);
            if (ndef != null) {
                ndef.connect();
                sb.append(ndef.isWritable() ? "✓ 可寫入" : "✗ 唯讀 (防寫保護)").append("\n");
                sb.append("容量: ").append(ndef.getMaxSize()).append(" bytes\n");
                if (ndef.getNdefMessage() != null) {
                    sb.append("已有 NDEF 資料存在\n");
                } else {
                    sb.append("(空白，無 NDEF 資料)\n");
                }
                ndef.close();
            } else {
                sb.append("不支援 NDEF 技術\n");
            }
        } catch (Exception e) {
            sb.append("檢查錯誤: ").append(e.getMessage()).append("\n");
        }
    }

    private String getSummary(String log) {
        if (log.contains("✗ 唯讀")) return "⚠ 卡片為唯讀狀態";
        if (log.contains("篡改")) return "⚠ 偵測到安全風險";
        if (log.contains("皆無法存取")) return "⚠ 卡片高度安全保護";
        if (log.contains("永久鎖定")) return "⚠ 部分記憶體永久鎖定";
        return "✓ 卡片安全狀態正常";
    }
}
