package com.helirfid;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class NtagActivity extends AppCompatActivity {

    TextView txtResult;
    Button btnRead, btnReadPages, btnReadCnt, btnIncCnt, btnPwdAuth, btnWritePage, btnReadSig, btnFormat, btnClear;
    EditText editCounter, editPwd, editPage, editPageData;
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    IntentFilter[] nfcFilters;
    Tag currentTag;
    MifareUltralight currentMU;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ntag);

        txtResult = findViewById(R.id.txtNtagResult);
        btnRead = findViewById(R.id.btnNtagRead);
        btnReadPages = findViewById(R.id.btnNtagReadPages);
        btnReadCnt = findViewById(R.id.btnNtagReadCnt);
        btnIncCnt = findViewById(R.id.btnNtagIncCnt);
        btnPwdAuth = findViewById(R.id.btnNtagPwdAuth);
        btnWritePage = findViewById(R.id.btnNtagWritePage);
        btnReadSig = findViewById(R.id.btnNtagReadSig);
        btnFormat = findViewById(R.id.btnNtagFormat);
        btnClear = findViewById(R.id.btnNtagClear);
        editCounter = findViewById(R.id.editNtagCounter);
        editPwd = findViewById(R.id.editNtagPwd);
        editPage = findViewById(R.id.editNtagPageWrite);
        editPageData = findViewById(R.id.editNtagPageData);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        Intent intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) flags |= PendingIntent.FLAG_MUTABLE;
        pendingIntent = PendingIntent.getActivity(this, 0, intent, flags);
        nfcFilters = new IntentFilter[]{
                new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED),
                new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED),
                new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED),
        };

        btnClear.setOnClickListener(v -> txtResult.setText(""));

        btnRead.setOnClickListener(v -> {
            if (currentTag != null) readTag();
            else txtResult.setText("請先將卡片靠近手機");
        });

        btnReadPages.setOnClickListener(v -> {
            if (currentTag != null) readAllPages();
            else txtResult.setText("請先將卡片靠近手機");
        });

        btnReadCnt.setOnClickListener(v -> {
            if (currentTag != null) readCounter();
            else txtResult.setText("請先將卡片靠近手機");
        });

        btnIncCnt.setOnClickListener(v -> {
            if (currentTag != null) incrementCounter();
            else txtResult.setText("請先將卡片靠近手機");
        });

        btnPwdAuth.setOnClickListener(v -> {
            if (currentTag != null) pwdAuth();
            else txtResult.setText("請先將卡片靠近手機");
        });

        btnWritePage.setOnClickListener(v -> {
            if (currentTag != null) writePage();
            else txtResult.setText("請先將卡片靠近手機");
        });

        btnReadSig.setOnClickListener(v -> {
            if (currentTag != null) readSignature();
            else txtResult.setText("請先將卡片靠近手機");
        });

        btnFormat.setOnClickListener(v -> {
            if (currentTag != null) formatTag();
            else txtResult.setText("請先將卡片靠近手機");
        });
    }

    private void readTag() {
        runOnUiThread(() -> txtResult.setText("讀取中..."));
        new Thread(() -> {
            try {
                MifareUltralight mu = MifareUltralight.get(currentTag);
                if (mu == null) {
                    runOnUiThread(() -> txtResult.setText("不支援 MIFARE Ultralight/NTAG"));
                    return;
                }
                mu.connect();
                currentMU = mu;
                StringBuilder sb = new StringBuilder();
                sb.append("=== 標籤資訊 ===\n");
                byte[] uid = currentTag.getId();
                sb.append("UID: ").append(bytesToHex(uid)).append("\n");
                sb.append("UID 長度: ").append(uid.length).append(" bytes\n\n");

                try {
                    byte[] ver = mu.transceive(new byte[]{(byte)0x60});
                    sb.append("GetVersion: ").append(bytesToHex(ver)).append("\n");
                    if (ver.length >= 8) {
                        String[] vendors = {"未知", "NXP (Philips)", "Infineon", "Renesas", "STMicroelectronics"};
                        int v = ver[0] & 0xFF;
                        sb.append("  廠商: ").append(v < vendors.length ? vendors[v] : String.format("0x%02X", v)).append("\n");
                        sb.append("  類型: 0x").append(String.format("%02X", ver[1])).append("\n");
                        sb.append("  子類型: 0x").append(String.format("%02X", ver[2])).append("\n");
                        sb.append("  版本: ").append(ver[3] & 0xFF).append("\n");
                        int memSize = 1 << (ver[4] & 0xFF);
                        sb.append("  記憶體: ").append(memSize).append(" bytes\n");
                        String prodName = guessProduct(ver[1] & 0xFF, ver[2] & 0xFF);
                        sb.append("  產品: ").append(prodName).append("\n");
                    }
                } catch (Exception e) {
                    sb.append("GetVersion: 不支援或指令失敗\n");
                }

                sb.append("\n--- Lock Bits (頁面 2-3) ---\n");
                try {
                    byte[] pg2 = mu.readPages(2);
                    sb.append("  頁面 2-5: ").append(bytesToHex(pg2)).append("\n");
                    sb.append("  Lock0: 0x").append(String.format("%02X", pg2[2])).append("\n");
                    sb.append("  Lock1: 0x").append(String.format("%02X", pg2[3])).append("\n");
                } catch (Exception e) {
                    sb.append("  讀取 Lock Bits 失敗\n");
                }

                sb.append("\n--- 頁面 0-15 ---\n");
                for (int page = 0; page < 16; page += 4) {
                    try {
                        byte[] data = mu.readPages(page);
                        if (data != null) {
                            for (int i = 0; i < 4; i++) {
                                int p = page + i;
                                byte[] pdata = new byte[]{data[i*4], data[i*4+1], data[i*4+2], data[i*4+3]};
                                sb.append(String.format("  P%02X: ", p)).append(bytesToHex(pdata));
                                if (p == 0) sb.append(" (UID + BCC)");
                                else if (p == 1) sb.append(" (UID/內部)");
                                else if (p == 2 || p == 3) sb.append(" (Lock/OTP)");
                                sb.append("\n");
                            }
                        }
                    } catch (Exception e) {
                        break;
                    }
                }
                mu.close();
                currentMU = null;
                runOnUiThread(() -> txtResult.setText(sb.toString()));
            } catch (Exception e) {
                runOnUiThread(() -> txtResult.setText("讀取失敗: " + e.getMessage()));
            }
        }).start();
    }

    private void readAllPages() {
        new Thread(() -> {
            try {
                MifareUltralight mu = MifareUltralight.get(currentTag);
                if (mu == null) { runOnUiThread(() -> txtResult.setText("不支援")); return; }
                mu.connect();
                currentMU = mu;
                StringBuilder sb = new StringBuilder("=== 全部頁面 ===\n");
                int maxPage = detectMaxPage(mu);
                for (int page = 0; page <= maxPage; page += 4) {
                    try {
                        byte[] data = mu.readPages(page);
                        if (data != null) {
                            for (int i = 0; i < 4 && page + i <= maxPage; i++) {
                                int p = page + i;
                                byte[] pdata = new byte[]{data[i*4], data[i*4+1], data[i*4+2], data[i*4+3]};
                                sb.append(String.format("P%02X: ", p)).append(bytesToHex(pdata)).append("\n");
                            }
                        }
                    } catch (Exception e) { break; }
                }
                sb.append("\n總頁數: ").append(maxPage + 1).append("\n");
                mu.close();
                currentMU = null;
                runOnUiThread(() -> txtResult.setText(sb.toString()));
            } catch (Exception e) {
                runOnUiThread(() -> txtResult.setText("讀取失敗: " + e.getMessage()));
            }
        }).start();
    }

    private void readCounter() {
        String cntStr = editCounter.getText().toString().trim();
        if (TextUtils.isEmpty(cntStr)) {
            txtResult.setText("請輸入計數器編號 (0/1/2)");
            return;
        }
        int cntNum;
        try { cntNum = Integer.parseInt(cntStr); } catch (Exception e) {
            txtResult.setText("計數器編號必須為數字");
            return;
        }
        if (cntNum < 0 || cntNum > 2) {
            txtResult.setText("計數器編號範圍: 0-2");
            return;
        }
        final int cn = cntNum;
        new Thread(() -> {
            try {
                MifareUltralight mu = MifareUltralight.get(currentTag);
                if (mu == null) { runOnUiThread(() -> txtResult.setText("不支援")); return; }
                mu.connect();
                byte[] result = mu.transceive(new byte[]{(byte)0x39, (byte)cn});
                mu.close();
                if (result != null && result.length >= 3) {
                    int val = ((result[0] & 0xFF) << 16) | ((result[1] & 0xFF) << 8) | (result[2] & 0xFF);
                    final int fval = val;
                    runOnUiThread(() -> txtResult.setText(
                            "計數器 " + cn + " 數值:\n" +
                            "  Hex: " + bytesToHex(result) + "\n" +
                            "  Decimal: " + fval));
                } else {
                    runOnUiThread(() -> txtResult.setText("讀取計數器失敗"));
                }
            } catch (Exception e) {
                runOnUiThread(() -> txtResult.setText("錯誤: " + e.getMessage()));
            }
        }).start();
    }

    private void incrementCounter() {
        String input = editCounter.getText().toString().trim();
        if (TextUtils.isEmpty(input)) {
            txtResult.setText("請輸入「計數器編號 遞增值」 (例如: 0 1)");
            return;
        }
        String[] parts = input.split("\\s+");
        if (parts.length < 2) {
            txtResult.setText("請輸入計數器編號和遞增值 (hex)");
            return;
        }
        int cntNum;
        try { cntNum = Integer.parseInt(parts[0]); } catch (Exception e) {
            txtResult.setText("計數器編號必須為數字"); return;
        }
        if (cntNum < 0 || cntNum > 2) { txtResult.setText("計數器編號 0-2"); return; }

        final int cn = cntNum;
        final byte[] incVal;
        try {
            String hex = parts[1].replace(" ", "");
            if (hex.length() != 6) hex = String.format("%6s", hex).replace(' ', '0');
            incVal = hexStringToBytes(hex);
        } catch (Exception e) {
            txtResult.setText("遞增值格式錯誤 (6位 hex)"); return;
        }

        new Thread(() -> {
            try {
                MifareUltralight mu = MifareUltralight.get(currentTag);
                if (mu == null) { runOnUiThread(() -> txtResult.setText("不支援")); return; }
                mu.connect();
                byte[] cmd = new byte[5];
                cmd[0] = (byte)0xA5;
                cmd[1] = (byte)cn;
                cmd[2] = incVal[0];
                cmd[3] = incVal[1];
                cmd[4] = incVal[2];
                mu.transceive(cmd);
                mu.close();
                runOnUiThread(() -> txtResult.setText("計數器 " + cn + " 已遞增 " + bytesToHex(incVal)));
            } catch (Exception e) {
                runOnUiThread(() -> txtResult.setText("遞增失敗: " + e.getMessage()));
            }
        }).start();
    }

    private void pwdAuth() {
        String pwdHex = editPwd.getText().toString().trim().replace(" ", "");
        if (pwdHex.length() != 8) {
            txtResult.setText("請輸入 4-byte 密碼 (8 位十六進位)");
            return;
        }
        final byte[] pwd;
        try { pwd = hexStringToBytes(pwdHex); } catch (Exception e) {
            txtResult.setText("密碼格式錯誤"); return;
        }

        new Thread(() -> {
            try {
                MifareUltralight mu = MifareUltralight.get(currentTag);
                if (mu == null) { runOnUiThread(() -> txtResult.setText("不支援")); return; }
                mu.connect();
                byte[] cmd = new byte[5];
                cmd[0] = (byte)0x1B;
                System.arraycopy(pwd, 0, cmd, 1, 4);
                byte[] response = mu.transceive(cmd);
                mu.close();
                if (response != null && response.length >= 2) {
                    final byte[] pack = new byte[]{response[0], response[1]};
                    runOnUiThread(() -> txtResult.setText(
                            "PWD 驗證成功!\nPACK: " + bytesToHex(pack) + "\n\n" +
                            "驗證後可讀取受保護頁面"));
                } else {
                    runOnUiThread(() -> txtResult.setText("PWD 驗證失敗 (密碼錯誤或不支援)"));
                }
            } catch (Exception e) {
                runOnUiThread(() -> txtResult.setText("PWD 驗證錯誤: " + e.getMessage()));
            }
        }).start();
    }

    private void writePage() {
        String pageStr = editPage.getText().toString().trim();
        String dataStr = editPageData.getText().toString().trim().replace(" ", "");
        if (TextUtils.isEmpty(pageStr) || dataStr.length() != 8) {
            txtResult.setText("請輸入頁碼和 4-byte 資料 (8位hex)");
            return;
        }
        final int page;
        final byte[] data;
        try {
            page = Integer.parseInt(pageStr, 16);
            data = hexStringToBytes(dataStr);
        } catch (Exception e) {
            txtResult.setText("格式錯誤"); return;
        }

        new Thread(() -> {
            try {
                MifareUltralight mu = MifareUltralight.get(currentTag);
                if (mu == null) { runOnUiThread(() -> txtResult.setText("不支援")); return; }
                mu.connect();
                mu.writePage(page, data);
                mu.close();
                runOnUiThread(() -> txtResult.setText("頁面 " + String.format("%02X", page) +
                        " 已寫入: " + bytesToHex(data)));
            } catch (Exception e) {
                runOnUiThread(() -> txtResult.setText("寫入失敗: " + e.getMessage()));
            }
        }).start();
    }

    private void readSignature() {
        new Thread(() -> {
            try {
                MifareUltralight mu = MifareUltralight.get(currentTag);
                if (mu == null) { runOnUiThread(() -> txtResult.setText("不支援")); return; }
                mu.connect();
                byte[] sig = mu.transceive(new byte[]{(byte)0x3C});
                mu.close();
                if (sig != null && sig.length >= 32) {
                    StringBuilder sb = new StringBuilder("=== ECC 簽名 (32 bytes) ===\n");
                    for (int i = 0; i < 32; i += 8) {
                        byte[] row = new byte[8];
                        System.arraycopy(sig, i, row, 0, 8);
                        sb.append("  ").append(String.format("%02X", i)).append(": ").append(bytesToHex(row)).append("\n");
                    }
                    final String result = sb.toString();
                    runOnUiThread(() -> txtResult.setText(result));
                } else {
                    runOnUiThread(() -> txtResult.setText("讀取簽名失敗"));
                }
            } catch (Exception e) {
                runOnUiThread(() -> txtResult.setText("錯誤: " + e.getMessage()));
            }
        }).start();
    }

    private void formatTag() {
        new Thread(() -> {
            try {
                MifareUltralight mu = MifareUltralight.get(currentTag);
                if (mu == null) { runOnUiThread(() -> txtResult.setText("不支援")); return; }
                mu.connect();
                int maxPage = detectMaxPage(mu);
                for (int p = 4; p <= maxPage - 4; p++) {
                    mu.writePage(p, new byte[]{0, 0, 0, 0});
                }
                mu.close();
                runOnUiThread(() -> txtResult.setText("NTAG 格式化完成 (頁面 4 ~ " +
                        String.format("%02X", maxPage - 4) + " 已清空)"));
            } catch (Exception e) {
                runOnUiThread(() -> txtResult.setText("格式化失敗: " + e.getMessage()));
            }
        }).start();
    }

    private int detectMaxPage(MifareUltralight mu) {
        int[] candidates = {0xFF, 0xE7, 0x86, 0x2B, 0x0F};
        for (int p : candidates) {
            try {
                mu.readPages(p);
                return p + 3;
            } catch (Exception e) { }
        }
        return 0x2B;
    }

    private String guessProduct(int type, int subType) {
        if (type == 0) {
            if (subType == 1) return "MIFARE Ultralight EV1 (MF0UL11) 80B";
            if (subType == 2) return "MIFARE Ultralight EV1 (MF0UL21) 164B";
            return "MIFARE Ultralight EV1";
        }
        if (type == 2) {
            if (subType == 1) return "NTAG 213 (180B)";
            if (subType == 2) return "NTAG 215 (540B)";
            if (subType == 3) return "NTAG 216 (924B)";
            if (subType == 4) return "NTAG 212 (128B)";
            if (subType == 5) return "NTAG 213 TagTamper";
            if (subType == 6) return "NTAG 215 TagTamper";
            if (subType == 7) return "NTAG 216 TagTamper";
            return "NTAG 21x 系列";
        }
        if (type == 4) {
            if (subType == 1) return "MIFARE Ultralight C (MF0ICU2)";
            return "MIFARE Ultralight C";
        }
        return String.format("未知 (類型 0x%02X, 子類型 0x%02X)", type, subType);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null)
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, nfcFilters, null);
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
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            currentTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            txtResult.setText("已偵測到 NTAG 卡片。點擊「讀取標籤資訊」開始分析。");
        }
    }

    private String bytesToHex(byte[] bytes) {
        if (bytes == null) return "";
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02X ", b));
        return sb.toString().trim();
    }

    private byte[] hexStringToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2)
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        return data;
    }
}
