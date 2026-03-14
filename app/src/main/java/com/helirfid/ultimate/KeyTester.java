package com.helirfid.ultimate;

import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import java.util.ArrayList;
import java.util.List;

public class KeyTester {

    private static final byte[][] DEFAULT_KEYS = {
        {(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF}, // Factory Default
        {(byte)0xA0,(byte)0xA1,(byte)0xA2,(byte)0xA3,(byte)0xA4,(byte)0xA5}, // Madroid
        {(byte)0xD3,(byte)0xF7,(byte)0xD3,(byte)0xF7,(byte)0xD3,(byte)0xF7}, // NXP
        {(byte)0x4D,(byte)0x3A,(byte)0x99,(byte)0xCB,(byte)0x34,(byte)0x0B}, // Uncommon
        {(byte)0x1A,(byte)0x98,(byte)0x2C,(byte)0x7E,(byte)0x45,(byte)0x9A}  // Public
    };

    private static final String[] KEY_NAMES = {
        "Factory Default",
        "Madroid",
        "NXP",
        "Uncommon",
        "Public"
    };

    public static String testAllKeys(Tag tag) {

        StringBuilder result = new StringBuilder();
        result.append("=== KEY TEST RESULTS ===\n");

        try {
            MifareClassic mc = MifareClassic.get(tag);
            mc.connect();

            int sectorCount = mc.getSectorCount();
            List<String> foundKeys = new ArrayList<>();

            for (int sector = 0; sector < Math.min(4, sectorCount); sector++) {
                result.append("\nSector ").append(sector).append(":\n");

                for (int i = 0; i < DEFAULT_KEYS.length; i++) {
                    try {
                        if (mc.authenticateSectorWithKeyA(sector, DEFAULT_KEYS[i])) {
                            result.append("  ✓ KeyA [").append(KEY_NAMES[i])
                                    .append("] = ").append(bytesToHex(DEFAULT_KEYS[i])).append("\n");
                            foundKeys.add("Sector " + sector + ": " + KEY_NAMES[i]);
                        }
                    } catch (Exception e) {
                        // Ignore authentication failures
                    }
                }
            }

            mc.close();

            if (foundKeys.isEmpty()) {
                result.append("\n✗ No tested keys found. Card may use custom keys.\n");
            } else {
                result.append("\n✓ Found ").append(foundKeys.size()).append(" accessible sector(s).\n");
            }

        } catch (Exception e) {
            result.append("Key Test Error: ").append(e.getMessage());
        }

        return result.toString();
    }

    public static boolean testKey(Tag tag, byte[] keyA, byte[] keyB) {

        try {
            MifareClassic mc = MifareClassic.get(tag);
            mc.connect();

            boolean authenticated = false;

            // 測試 KeyA
            if (keyA != null) {
                for (int sector = 0; sector < mc.getSectorCount(); sector++) {
                    try {
                        if (mc.authenticateSectorWithKeyA(sector, keyA)) {
                            authenticated = true;
                            break;
                        }
                    } catch (Exception e) {
                        // Try next sector
                    }
                }
            }

            // 如果 KeyA 失失敗，測試 KeyB
            if (!authenticated && keyB != null) {
                for (int sector = 0; sector < mc.getSectorCount(); sector++) {
                    try {
                        if (mc.authenticateSectorWithKeyB(sector, keyB)) {
                            authenticated = true;
                            break;
                        }
                    } catch (Exception e) {
                        // Try next sector
                    }
                }
            }

            mc.close();
            return authenticated;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
