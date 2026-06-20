/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.0.1
 */
package com.helirfid;

import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;

import java.io.IOException;

public class NfcUltralightUtils {

    public static String setPassword(Tag tag, byte[] password) {
        try {
            MifareUltralight mu = MifareUltralight.get(tag);
            if (mu == null) {
                return "不支援 MIFARE Ultralight/NTAG";
            }

            if (password == null || password.length != 4) {
                return "密碼必須為 4 bytes";
            }

            mu.connect();

            int maxPage = getMaxPage(mu);
            int pwdPage = maxPage - 4;
            int packPage = maxPage - 3;

            mu.writePage(pwdPage, password);

            byte[] pack = new byte[]{0x00, 0x00};
            mu.writePage(packPage, new byte[]{pack[0], pack[1], (byte)0x00, (byte)0x00});

            mu.close();
            return "密碼設定成功 (PWD page " + pwdPage + ")";
        } catch (IOException e) {
            return "I/O 錯誤: " + e.getMessage();
        } catch (Exception e) {
            return "密碼設定失敗: " + e.getMessage();
        }
    }

    public static String removePassword(Tag tag) {
        try {
            MifareUltralight mu = MifareUltralight.get(tag);
            if (mu == null) {
                return "不支援 MIFARE Ultralight/NTAG";
            }

            mu.connect();

            int maxPage = getMaxPage(mu);
            int pwdPage = maxPage - 4;

            mu.writePage(pwdPage, new byte[]{ (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF });
            mu.writePage(maxPage - 3, new byte[]{ (byte)0xFF, (byte)0xFF, 0x00, 0x00 });

            mu.close();
            return "密碼已移除 (重設為 FFFFFFFF)";
        } catch (IOException e) {
            return "I/O 錯誤: " + e.getMessage();
        } catch (Exception e) {
            return "移除密碼失敗: " + e.getMessage();
        }
    }

    public static String lockTag(Tag tag) {
        try {
            MifareUltralight mu = MifareUltralight.get(tag);
            if (mu == null) {
                return "不支援 MIFARE Ultralight/NTAG";
            }

            mu.connect();

            byte[] data = mu.readPages(2);

            byte[] lockBytes = new byte[4];
            lockBytes[0] = (byte)(data[2] | 0x01);
            lockBytes[1] = (byte)(data[3] | 0x01);
            lockBytes[2] = 0x00;
            lockBytes[3] = 0x00;

            mu.writePage(2, lockBytes);

            mu.close();
            return "卡片已鎖定為唯讀 (不可逆)";
        } catch (IOException e) {
            return "I/O 錯誤: " + e.getMessage();
        } catch (Exception e) {
            return "鎖定失敗: " + e.getMessage();
        }
    }

    public static String formatUltralight(Tag tag) {
        try {
            MifareUltralight mu = MifareUltralight.get(tag);
            if (mu == null) {
                return "不支援 MIFARE Ultralight/NTAG";
            }
            mu.connect();

            int maxPage = getMaxPage(mu);
            for (int p = 4; p <= maxPage - 4; p++) {
                mu.writePage(p, new byte[]{0, 0, 0, 0});
            }
            mu.close();
            return "NTAG 已格式化完成";
        } catch (Exception e) {
            return "格式化失敗: " + e.getMessage();
        }
    }

    private static int getMaxPage(MifareUltralight mu) {
        for (int testPage : new int[]{0xE0, 0x80, 0x2C, 0x10}) {
            try {
                mu.readPages(testPage);
                return testPage + 4;
            } catch (Exception e) { }
        }
        return 45;
    }
}
