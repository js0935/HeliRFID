/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.0.1
 */
package com.helirfid;

import android.nfc.Tag;
import android.nfc.tech.MifareClassic;

public class KeyTester {

    private static final byte[][] TEST_KEYS = {
        {0x00, 0x00, 0x00, 0x00, 0x00, 0x00},
        {(byte)0xA0, (byte)0xA1, (byte)0xA2, (byte)0xA3, (byte)0xA4, (byte)0xA5},
        {(byte)0xD3, (byte)0xF7, (byte)0xD3, (byte)0xF7, (byte)0xD3, (byte)0xF7},
        {(byte)0x4D, 0x3A, (byte)0x99, (byte)0xCB, 0x34, 0x0B},
        {0x1A, (byte)0x98, 0x2C, 0x7E, 0x45, (byte)0x9A},
        {(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF},
        {(byte)0xA0, (byte)0xB0, (byte)0xC0, (byte)0xD0, (byte)0xE0, (byte)0xF0},
        {0x00, 0x01, 0x02, 0x03, 0x04, 0x05}
    };

    private static final String[] KEY_NAMES = {
        "工廠預設", "Madroid", "NXP", "Uncommon", "Public",
        "全 FF", "ABCDEF", "012345"
    };

    public static String testKeys(Tag tag) {
        MifareClassic mifare = MifareClassic.get(tag);

        if (mifare == null) {
            return "不支援 Mifare Classic 卡片";
        }

        StringBuilder result = new StringBuilder();
        result.append("金鑰測試結果:\n");
        int foundCount = 0;

        try {
            mifare.connect();

            for (int i = 0; i < TEST_KEYS.length; i++) {
                if (testSector0Key(mifare, TEST_KEYS[i])) {
                    result.append("✓ ").append(KEY_NAMES[i]).append(": ");
                    result.append(bytesToHex(TEST_KEYS[i])).append("\n");
                    foundCount++;
                }
            }

            mifare.close();

            if (foundCount == 0) {
                result.append("無可用金鑰");
            } else {
                result.append("\n共找到 ").append(foundCount).append(" 組可用金鑰");
            }

        } catch (Exception e) {
            return "測試失敗: " + e.getMessage();
        }

        return result.toString();
    }

    public static String testKeysAllSectors(Tag tag) {
        MifareClassic mifare = MifareClassic.get(tag);

        if (mifare == null) {
            return "不支援 Mifare Classic 卡片";
        }

        StringBuilder result = new StringBuilder();
        result.append("跨磁區金鑰測試:\n");

        try {
            mifare.connect();
            int sectorCount = mifare.getSectorCount();
            int totalFound = 0;

            for (int s = 0; s < sectorCount; s++) {
                boolean sectorOk = false;
                for (int k = 0; k < TEST_KEYS.length; k++) {
                    try {
                        if (mifare.authenticateSectorWithKeyA(s, TEST_KEYS[k])) {
                            result.append("S").append(s).append(": ✓ ")
                                  .append(KEY_NAMES[k]).append("\n");
                            sectorOk = true;
                            totalFound++;
                            break;
                        }
                    } catch (Exception e) {
                        // try next key
                    }
                }
                if (!sectorOk) {
                    result.append("S").append(s).append(": ✗ 無可用金鑰\n");
                }
            }

            mifare.close();

            result.append("\n總計 ").append(totalFound).append("/")
                  .append(sectorCount).append(" 磁區可存取");

        } catch (Exception e) {
            return "測試失敗: " + e.getMessage();
        }

        return result.toString();
    }

    private static boolean testSector0Key(MifareClassic mifare, byte[] key) {
        try {
            mifare.authenticateSectorWithKeyA(0, key);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    public static String getKeyByKeyName(String keyName) {
        for (int i = 0; i < KEY_NAMES.length; i++) {
            if (KEY_NAMES[i].equals(keyName)) {
                return bytesToHex(TEST_KEYS[i]);
            }
        }
        return null;
    }

    public static byte[] getKeyByKeyIndex(int index) {
        if (index >= 0 && index < TEST_KEYS.length) {
            return TEST_KEYS[index].clone();
        }
        return null;
    }

    public static int getKeyCount() {
        return TEST_KEYS.length;
    }

    public static String getAllKeys() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < KEY_NAMES.length; i++) {
            sb.append(KEY_NAMES[i]).append(": ").append(bytesToHex(TEST_KEYS[i])).append("\n");
        }
        return sb.toString();
    }
}
