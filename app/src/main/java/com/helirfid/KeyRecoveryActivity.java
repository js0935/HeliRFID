package com.helirfid;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class KeyRecoveryActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private Tag currentTag;

    TextView txtStatus, txtResult;
    EditText editPattern;
    Button btnSave;

    List<byte[]> knownKeys = new ArrayList<>();
    List<String> knownKeySources = new ArrayList<>();
    Map<Integer, byte[]> recoveredKeys = new LinkedHashMap<>();
    Map<Integer, String> recoveryDetails = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_recovery);

        txtStatus = findViewById(R.id.txtKeyRecoveryStatus);
        txtResult = findViewById(R.id.txtKeyRecoveryResult);
        editPattern = findViewById(R.id.editBrutePattern);
        btnSave = findViewById(R.id.btnSaveRecoveredKeys);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        Intent intent = new Intent(this, getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? PendingIntent.FLAG_MUTABLE
                : PendingIntent.FLAG_UPDATE_CURRENT;
        pendingIntent = PendingIntent.getActivity(this, 0, intent, flags);

        loadBuiltinKeys();
        loadFileKeys();

        btnSave.setOnClickListener(v -> saveRecoveredKeys());
    }

    private void loadBuiltinKeys() {
        String[] builtin = {
            "FFFFFFFFFFFF", "000000000000", "A0A1A2A3A4A5", "A0B0C0D0E0F0",
            "D3F7D3F7D3F7", "B0B1B2B3B4B5", "A1B2C3D4E5F6", "C0C1C2C3C4C5",
            "4D3A99C351DD", "1A982C7E45EA", "714C5C886E97", "587EE4F9A8B3",
            "A18EEDCEA4F6", "506050605060", "FFFFFFFFFFFF", "AABBCCDDEEFF",
            "111111111111", "222222222222", "333333333333", "444444444444",
            "555555555555", "666666666666", "777777777777", "888888888888",
            "999999999999", "AAAAAAAAAAAA", "BBBBBBBBBBBB", "CCCCCCCCCCCC",
            "DDDDDDDDDDDD", "EEEEEEEEEEEE"
        };
        for (String hex : builtin) {
            knownKeys.add(hexToBytes(hex));
            knownKeySources.add("(內建字典)");
        }
        txtStatus.setText("內建 " + knownKeys.size() + " 字典金鑰 + 檔案金鑰，請掃描 MIFARE Classic 卡片");
    }

    private void loadFileKeys() {
        List<String> keyFiles = KeyManager.getKeyFileNames(this);
        for (String fileName : keyFiles) {
            List<byte[]> keys = KeyManager.loadKeys(this, fileName);
            for (byte[] key : keys) {
                knownKeys.add(key);
                knownKeySources.add(fileName);
            }
        }
    }

    private void recoverKeys() {
        if (currentTag == null) return;

        new Thread(() -> {
            try {
                final MifareClassic mfc = MifareClassic.get(currentTag);
                if (mfc == null) {
                    runOnUiThread(() -> txtStatus.setText("不支援 MIFARE Classic"));
                    return;
                }

                mfc.connect();
                mfc.setTimeout(2000);

                final int sectorCount = mfc.getSectorCount();
                StringBuilder uidSb = new StringBuilder("UID: ");
                for (byte b : currentTag.getId()) uidSb.append(String.format("%02X", b));

                runOnUiThread(() -> txtStatus.setText(uidSb.toString() + "\n掃描中... 共 " + sectorCount + " 個 Sector"));

                recoveredKeys.clear();
                recoveryDetails.clear();

                List<byte[]> tryKeys = new ArrayList<>(knownKeys);
                List<String> trySources = new ArrayList<>(knownKeySources);

                String pattern = editPattern.getText().toString().trim();
                if (!pattern.isEmpty()) {
                    List<byte[]> bruteKeys = generateBruteForceKeys(pattern);
                    if (bruteKeys != null) {
                        tryKeys.addAll(bruteKeys);
                        for (int i = 0; i < bruteKeys.size(); i++) {
                            trySources.add("(暴力破解)");
                        }
                    } else {
                        runOnUiThread(() -> txtResult.setText("暴力破解格式錯誤，最大支援 2 個 ??\n"));
                    }
                }

                final int totalToTry = tryKeys.size();

                for (int sector = 0; sector < sectorCount; sector++) {
                    int blocksPerSector = mfc.getBlockCountInSector(sector);
                    int trailerBlock = mfc.sectorToBlock(sector) + blocksPerSector - 1;
                    boolean found = false;

                    for (int ki = 0; ki < totalToTry; ki++) {
                        byte[] key = tryKeys.get(ki);
                        if (key.length != 6) continue;

                        if (mfc.authenticateSectorWithKeyA(sector, key)) {
                            byte[] trailer = mfc.readBlock(trailerBlock);
                            byte[] keyA = new byte[6];
                            byte[] keyB = new byte[6];
                            System.arraycopy(trailer, 0, keyA, 0, 6);
                            System.arraycopy(trailer, 10, keyB, 0, 6);

                            String keyAHex = bytesToHex(keyA);
                            String keyBHex = bytesToHex(keyB);

                            StringBuilder sb = new StringBuilder();
                            sb.append("Sector ").append(sector)
                                    .append(" (Block ").append(trailerBlock).append(")\n");
                            sb.append("  Key A: ").append(keyAHex)
                                    .append("  [").append(trySources.get(ki)).append("]\n");
                            sb.append("  Key B: ").append(keyBHex).append("\n");
                            String bCheck = keyBHex.replace("0", "").replace("F", "");
                            if (!bCheck.isEmpty()) {
                                sb.append("  Key B 可讀取\n");
                            } else {
                                sb.append("  Key B 受限\n");
                            }

                            recoveryDetails.put(sector, sb.toString());
                            recoveredKeys.put(sector, keyA);
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        recoveryDetails.put(sector, "Sector " + sector + ": 所有金鑰認證失敗\n");
                    }

                    final int p = sector + 1;
                    runOnUiThread(() -> txtStatus.setText(uidSb.toString()
                            + "\n進度: " + p + "/" + sectorCount + " sectors"));
                }

                mfc.close();
                buildResultText(sectorCount);
                runOnUiThread(() -> {
                    btnSave.setEnabled(!recoveredKeys.isEmpty());
                    txtStatus.setText(uidSb.toString()
                            + "\n完成! " + totalToTry + " 組金鑰嘗試，恢復 "
                            + recoveredKeys.size() + "/" + sectorCount + " 個金鑰");
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    txtStatus.setText("錯誤: " + e.getMessage());
                    Toast.makeText(this, "錯誤: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private List<byte[]> generateBruteForceKeys(String pattern) {
        String clean = pattern.replace(" ", "").toUpperCase();
        if (clean.length() != 12) return null;

        List<Integer> unknownPositions = new ArrayList<>();
        for (int i = 0; i < clean.length(); i += 2) {
            if (clean.charAt(i) == '?' && clean.charAt(i + 1) == '?') {
                unknownPositions.add(i / 2);
            }
        }
        if (unknownPositions.isEmpty()) return null;
        if (unknownPositions.size() > 2) return null;

        byte[] base = new byte[6];
        for (int i = 0; i < 6; i++) {
            String pair = clean.substring(i * 2, i * 2 + 2);
            if (pair.equals("??")) {
                base[i] = 0;
            } else {
                base[i] = (byte) ((Character.digit(pair.charAt(0), 16) << 4)
                        + Character.digit(pair.charAt(1), 16));
            }
        }

        List<byte[]> results = new ArrayList<>();
        int pos0 = unknownPositions.get(0);
        if (unknownPositions.size() == 1) {
            for (int v0 = 0; v0 < 256; v0++) {
                byte[] k = base.clone();
                k[pos0] = (byte) v0;
                results.add(k);
            }
        } else {
            int pos1 = unknownPositions.get(1);
            for (int v0 = 0; v0 < 256; v0++) {
                for (int v1 = 0; v1 < 256; v1++) {
                    byte[] k = base.clone();
                    k[pos0] = (byte) v0;
                    k[pos1] = (byte) v1;
                    results.add(k);
                }
            }
        }
        return results;
    }

    private void buildResultText(int totalSectors) {
        StringBuilder sb = new StringBuilder();
        sb.append("金鑰恢復結果 (").append(recoveredKeys.size())
                .append("/").append(totalSectors).append(" sectors)\n---\n");
        for (int s = 0; s < totalSectors; s++) {
            String detail = recoveryDetails.get(s);
            if (detail != null) sb.append(detail).append("\n");
        }
        final String result = sb.toString();
        runOnUiThread(() -> txtResult.setText(result));
    }

    private void saveRecoveredKeys() {
        if (recoveredKeys.isEmpty()) {
            Toast.makeText(this, "沒有金鑰可儲存", Toast.LENGTH_SHORT).show();
            return;
        }
        List<String> hexLines = new ArrayList<>();
        for (byte[] key : recoveredKeys.values()) {
            hexLines.add(bytesToHex(key));
        }
        String fileName = "recovered_" + System.currentTimeMillis() + ".keys";
        if (KeyManager.saveKeys(this, fileName, hexLines)) {
            Toast.makeText(this, "已儲存 " + hexLines.size() + " 個金鑰到 " + fileName, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "儲存失敗", Toast.LENGTH_SHORT).show();
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02X", b));
        return sb.toString();
    }

    private static byte[] hexToBytes(String s) {
        byte[] data = new byte[s.length() / 2];
        for (int i = 0; i < s.length(); i += 2) {
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
            recoverKeys();
        }
    }
}
