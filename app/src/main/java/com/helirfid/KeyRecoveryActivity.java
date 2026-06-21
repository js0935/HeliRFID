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
        String[][] builtin = {
            {"工廠預設 (全 FF)",              "FFFFFFFFFFFF"},
            {"全零",                          "000000000000"},
            {"NXP 測試",                      "A0A1A2A3A4A5"},
            {"NDEF 預設",                     "D3F7D3F7D3F7"},
            {"順序 ABCDEF",                   "A0B0C0D0E0F0"},
            {"順序 B0B1B2",                   "B0B1B2B3B4B5"},
            {"順序 A1B2C3",                   "A1B2C3D4E5F6"},
            {"順序 C0C1C2",                   "C0C1C2C3C4C5"},
            {"Mifare 1K 運輸",               "4D3A99C351DD"},
            {"Mifare 4K 運輸",               "1A982C7E45EA"},
            {"Legic 運輸",                    "714C5C886E97"},
            {"NXP 半導體",                    "587EE4F9A8B3"},
            {"NXP 工程",                      "A18EEDCEA4F6"},
            {"NXP 測試 B",                    "506050605060"},
            {"VingCard A",                    "44454D4F414D"},
            {"VingCard B",                    "434144454D4F"},
            {"VingCard C",                    "56494E474341"},
            {"VingCard D",                    "56494E474344"},
            {"飯店 SALTON",                   "53414C544F4E"},
            {"飯店 hotel1",                   "686F74656C31"},
            {"荷蘭 OV-Chipkaart",             "4B0B20104B0B"},
            {"瑞典 Västtrafiken",             "828384858687"},
            {"iCopy-X 預設",                  "4A49434F5059"},
            {"RKF 地鐵",                      "50494B2B3752"},
            {"基輔地鐵 A",                    "8E0C99CCCCC0"},
            {"基輔地鐵 B",                    "81BD43176684"},
            {"XXT 停車卡",                    "931A4499C3D7"},
            {"Troika 交通卡",                 "8D540A123456"},
            {"香港八達通",                    "A234B456C789"},
            {"1 重複",                        "111111111111"},
            {"2 重複",                        "222222222222"},
            {"3 重複",                        "333333333333"},
            {"4 重複",                        "444444444444"},
            {"5 重複",                        "555555555555"},
            {"6 重複",                        "666666666666"},
            {"7 重複",                        "777777777777"},
            {"8 重複",                        "888888888888"},
            {"9 重複",                        "999999999999"},
            {"A 重複",                        "AAAAAAAAAAAA"},
            {"B 重複",                        "BBBBBBBBBBBB"},
            {"C 重複",                        "CCCCCCCCCCCC"},
            {"D 重複",                        "DDDDDDDDDDDD"},
            {"E 重複",                        "EEEEEEEEEEEE"},
            {"順序 01-06",                    "010203040506"},
            {"順序 0A-0F",                    "0A0B0C0D0E0F"},
            {"順序 11-16",                    "112233445566"},
            {"順序 12-17",                    "123456789ABC"},
            {"順序 40-45",                    "404142434445"},
            {"順序 61-66 (abcdef)",          "616263646566"},
            {"AABB 測試",                     "AABBCCDDEEFF"},
            {"ABAB 測試",                     "ABABABABABAB"},
            {"ABCD 測試",                     "ABCDEFABCDEF"},
            {"secret 字串",                   "736563726574"},
            {"univer 字串",                   "756E69766572"},
            {"FFFF-0000",                     "FFFFFF000000"},
            {"0000-FFFF",                     "000000FFFFFF"},
            {"01 重複",                       "010101010101"},
            {"02 重複",                       "020202020202"},
            {"03 重複",                       "030303030303"},
            {"04 重複",                       "040404040404"},
            {"05 重複",                       "050505050505"},
            {"06 重複",                       "060606060606"},
            {"07 重複",                       "070707070707"},
            {"08 重複",                       "080808080808"},
            {"09 重複",                       "090909090909"},
            {"0A 重複",                       "0A0A0A0A0A0A"},
            {"0B 重複",                       "0B0B0B0B0B0B"},
            {"0C 重複",                       "0C0C0C0C0C0C"},
            {"0D 重複",                       "0D0D0D0D0D0D"},
            {"0E 重複",                       "0E0E0E0E0E0E"},
            {"0F 重複",                       "0F0F0F0F0F0F"},
            {"F0 重複",                       "F0F0F0F0F0F0"},
            {"12 重複",                       "121212121212"},
            {"13 重複",                       "131313131313"},
            {"14 重複",                       "141414141414"},
            {"15 重複",                       "151515151515"},
            {"16 重複",                       "161616161616"},
            {"17 重複",                       "171717171717"},
            {"18 重複",                       "181818181818"},
            {"19 重複",                       "191919191919"},
            {"1A 重複",                       "1A1A1A1A1A1A"},
            {"1B 重複",                       "1B1B1B1B1B1B"},
            {"1C 重複",                       "1C1C1C1C1C1C"},
            {"1D 重複",                       "1D1D1D1D1D1D"},
            {"1E 重複",                       "1E1E1E1E1E1E"},
            {"1F 重複",                       "1F1F1F1F1F1F"},
            {"20 重複",                       "202020202020"},
            {"21 重複",                       "212121212121"},
            {"23 重複",                       "232323232323"},
            {"24 重複",                       "242424242424"},
            {"25 重複",                       "252525252525"},
            {"26 重複",                       "262626262626"},
            {"27 重複",                       "272727272727"},
            {"28 重複",                       "282828282828"},
            {"29 重複",                       "292929292929"},
            {"2A 重複",                       "2A2A2A2A2A2A"},
            {"2B 重複",                       "2B2B2B2B2B2B"},
            {"2C 重複",                       "2C2C2C2C2C2C"},
            {"2D 重複",                       "2D2D2D2D2D2D"},
            {"2E 重複",                       "2E2E2E2E2E2E"},
            {"2F 重複",                       "2F2F2F2F2F2F"},
            {"30 重複",                       "303030303030"},
            {"31 重複",                       "313131313131"},
            {"32 重複",                       "323232323232"},
            {"41 重複",                       "414141414141"},
            {"42 重複",                       "424242424242"},
            {"43 重複",                       "434343434343"},
            {"46 重複",                       "464646464646"},
            {"47 重複",                       "474747474747"},
            {"48 重複",                       "484848484848"},
            {"49 重複",                       "494949494949"},
            {"4A 重複",                       "4A4A4A4A4A4A"},
            {"4B 重複",                       "4B4B4B4B4B4B"},
            {"4C 重複",                       "4C4C4C4C4C4C"},
            {"4D 重複",                       "4D4D4D4D4D4D"},
            {"4E 重複",                       "4E4E4E4E4E4E"},
        };
        for (String[] entry : builtin) {
            knownKeys.add(hexToBytes(entry[1]));
            knownKeySources.add("(內建-"+entry[0]+")");
        }
        txtStatus.setText("內建 " + knownKeys.size() + " 組字典金鑰 + 檔案金鑰，請掃描 MIFARE Classic 卡片");
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
