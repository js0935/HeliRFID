/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.0.1
 */
package com.helirfid;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcA;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MagicCardActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private Tag currentTag;
    private int detectedGen = 0;

    TextView txtCardInfo, txtDetectResult;
    EditText editNewUid;
    Button btnDetect, btnWriteUid;
    StringBuilder log = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_magic_card);

        txtCardInfo = findViewById(R.id.txtMagicCardInfo);
        txtDetectResult = findViewById(R.id.txtDetectResult);
        editNewUid = findViewById(R.id.editNewUid);
        btnDetect = findViewById(R.id.btnDetectMagic);
        btnWriteUid = findViewById(R.id.btnWriteMagicUid);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        Intent intent = new Intent(this, getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? PendingIntent.FLAG_MUTABLE
                : PendingIntent.FLAG_UPDATE_CURRENT;
        pendingIntent = PendingIntent.getActivity(this, 0, intent, flags);

        btnDetect.setOnClickListener(v -> {
            if (currentTag == null) {
                Toast.makeText(this, "請先掃描卡片", Toast.LENGTH_SHORT).show();
                return;
            }
            detectMagicCard();
        });

        btnWriteUid.setOnClickListener(v -> {
            if (currentTag == null) {
                Toast.makeText(this, "請先掃描卡片", Toast.LENGTH_SHORT).show();
                return;
            }
            String uid = editNewUid.getText().toString().trim().replace(" ", "").replace(":", "");
            if (TextUtils.isEmpty(uid) || uid.length() != 8) {
                Toast.makeText(this, "請輸入 4 bytes UID (8 hex 字元)", Toast.LENGTH_SHORT).show();
                return;
            }
            writeNewUid(uid);
        });
    }

    private void appendLog(String text) {
        log.append(text).append("\n");
        txtDetectResult.setText(log.toString());
    }

    private void detectMagicCard() {
        log.setLength(0);
        detectedGen = 0;
        new Thread(() -> {
            try {
                NfcA nfcA = NfcA.get(currentTag);
                if (nfcA == null) {
                    runOnUiThread(() -> appendLog("不支援 NfcA 技術"));
                    return;
                }
                nfcA.connect();
                nfcA.setTimeout(5000);

                byte[] atqa = nfcA.getAtqa();
                short sak = nfcA.getSak();
                byte[] uid = currentTag.getId();

                final String atqaStr = String.format("%02X %02X", atqa[0], atqa[1]);
                final String sakStr = String.format("%02X", sak & 0xFF);
                final String uidStr = bytesToHex(uid);

                runOnUiThread(() -> {
                    appendLog("UID: " + uidStr);
                    appendLog("ATQA: " + atqaStr + "  SAK: " + sakStr);
                });

                byte[] testPattern = {(byte)0xAA, (byte)0xBB, (byte)0xCC, (byte)0xDD, (byte)0xEE, (byte)0xFF, (byte)0x11, (byte)0x22, (byte)0x33, (byte)0x44, (byte)0x55, (byte)0x66, (byte)0x77, (byte)0x88, (byte)0x99, (byte)0x00};

                boolean writeBlock0Ok = false;
                try {
                    byte[] writeCmd = buildWriteBlock0Command(nfcA, testPattern);
                    byte[] resp = nfcA.transceive(writeCmd);
                    if (resp != null && resp.length > 0 && (resp[0] & 0x00) == 0x00) {
                        writeBlock0Ok = true;
                    }
                } catch (Exception e) {
                    runOnUiThread(() -> appendLog("Block 0 寫入嘗試失敗 (正常卡片應如此)"));
                }

                if (!writeBlock0Ok) {
                    try {
                        byte[] auth = {0x60, 0x00, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF};
                        nfcA.transceive(auth);
                        byte[] writeCmd = buildWriteBlock0Command(nfcA, testPattern);
                        byte[] resp = nfcA.transceive(writeCmd);
                        if (resp != null && resp.length > 0 && (resp[0] & 0x80) == 0) {
                            writeBlock0Ok = true;
                        }
                    } catch (Exception e) {
                    }
                }

                byte[] readBack = null;
                if (writeBlock0Ok) {
                    try {
                        byte[] readCmd = {0x30, 0x00};
                        byte[] resp = nfcA.transceive(readCmd);
                        if (resp != null && resp.length >= 16) {
                            readBack = resp;
                        }
                    } catch (Exception e) {
                    }

                    try {
                        byte[] originalUid = currentTag.getId();
                        byte[] restoreCmd = buildWriteBlock0Command(nfcA, padUidToBlock(originalUid));
                        nfcA.transceive(restoreCmd);
                    } catch (Exception e) {
                    }

                    final byte[] rb = readBack;
                    runOnUiThread(() -> {
                        detectedGen = 1;
                        appendLog("═══════════════════════════");
                        appendLog("✅ 偵測到魔術卡!");
                        appendLog("可以寫入 Block 0 (UID)");
                        if (rb != null) {
                            appendLog("寫入驗證成功: " + bytesToHex(rb));
                        }
                        appendLog("推測世代: Gen1 (中國魔術卡)");
                        appendLog("═══════════════════════════");
                    });
                    nfcA.close();
                    return;
                }

                try {
                    byte[] gen4Cmd = {(byte)0xCF};
                    byte[] gen4Resp = nfcA.transceive(gen4Cmd);
                    if (gen4Resp != null && gen4Resp.length >= 2) {
                        runOnUiThread(() -> {
                            detectedGen = 4;
                            appendLog("═══════════════════════════");
                            appendLog("✅ 偵測到 GTU/Gibbon 魔術卡 (Gen4)");
                            appendLog("配置回應: " + bytesToHex(gen4Resp));
                            appendLog("═══════════════════════════");
                        });
                        nfcA.close();
                        return;
                    }
                } catch (Exception e) {
                }

                byte[] gen2Cmd = {0x40};
                try {
                    byte[] gen2Resp = nfcA.transceive(gen2Cmd);
                    if (gen2Resp != null && gen2Resp.length >= 1 && gen2Resp[0] == 0x0A) {
                        runOnUiThread(() -> {
                            detectedGen = 2;
                            appendLog("═══════════════════════════");
                            appendLog("✅ 偵測到魔術卡");
                            appendLog("回應 0x40 命令 (ACK 0x0A)");
                            appendLog("推測世代: Gen2/Gen3 (CUID/UID 可寫)");
                            appendLog("═══════════════════════════");
                        });
                        nfcA.close();
                        return;
                    }
                } catch (Exception e) {
                }

                runOnUiThread(() -> {
                    appendLog("═══════════════════════════");
                    appendLog("❌ 未偵測到魔術卡特性");
                    appendLog("此卡片為一般 MIFARE Classic 卡");
                    appendLog("═══════════════════════════");
                });

                nfcA.close();

            } catch (Exception e) {
                runOnUiThread(() -> appendLog("偵測失敗: " + e.getMessage()));
            }
        }).start();
    }

    private byte[] buildWriteBlock0Command(NfcA nfcA, byte[] data) {
        byte[] cmd = new byte[18];
        cmd[0] = (byte)0xA0;
        cmd[1] = 0x00;
        System.arraycopy(data, 0, cmd, 2, 16);
        return cmd;
    }

    private byte[] padUidToBlock(byte[] uid) {
        byte[] block = new byte[16];
        int len = Math.min(uid.length, 4);
        System.arraycopy(uid, 0, block, 0, len);
        byte bcc = 0;
        for (int i = 0; i < len; i++) bcc ^= uid[i];
        block[4] = bcc;
        return block;
    }

    private void writeNewUid(String uidHex) {
        new Thread(() -> {
            try {
                NfcA nfcA = NfcA.get(currentTag);
                if (nfcA == null) {
                    runOnUiThread(() -> Toast.makeText(this, "不支援 NfcA", Toast.LENGTH_SHORT).show());
                    return;
                }
                nfcA.connect();
                nfcA.setTimeout(5000);

                byte[] uidBytes = hexStringToByteArray(uidHex);
                byte[] blockData = padUidToBlock(uidBytes);

                byte[] writeCmd = buildWriteBlock0Command(nfcA, blockData);
                byte[] resp = nfcA.transceive(writeCmd);

                final boolean success = resp != null && resp.length > 0 && (resp[0] & 0x80) == 0;

                nfcA.close();

                runOnUiThread(() -> {
                    if (success) {
                        appendLog("UID 寫入成功: " + uidHex);
                        Toast.makeText(this, "UID 已更新", Toast.LENGTH_SHORT).show();
                    } else {
                        appendLog("UID 寫入失敗 (回應: " + (resp != null ? bytesToHex(resp) : "null") + ")");
                        Toast.makeText(this, "寫入失敗", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    appendLog("寫入錯誤: " + e.getMessage());
                    Toast.makeText(this, "寫入錯誤", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02X ", b));
        return sb.toString().trim();
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2)
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
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
            StringBuilder info = new StringBuilder("卡片已偵測\nUID: ");
            for (byte b : currentTag.getId()) info.append(String.format("%02X", b));
            info.append("\n技術: ");
            for (String t : currentTag.getTechList()) {
                String s = t.substring(t.lastIndexOf('.') + 1);
                info.append(s).append(" ");
            }
            txtCardInfo.setText(info.toString());
            Toast.makeText(this, "卡片已偵測", Toast.LENGTH_SHORT).show();
        }
    }
}
