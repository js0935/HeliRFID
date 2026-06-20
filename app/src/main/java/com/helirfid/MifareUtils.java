/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.0.1
 */
package com.helirfid;

import java.util.ArrayList;
import java.util.List;

public class MifareUtils {

    public static String decodeAccessConditions(byte[] sectorTrailer) {
        if (sectorTrailer == null || sectorTrailer.length < 6) {
            return "資料不足 (需要至少 6 bytes)";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Access Conditions 解碼:\n\n");

        byte b6 = sectorTrailer[0];
        byte b7 = sectorTrailer[1];
        byte b8 = sectorTrailer[2];
        byte b9 = sectorTrailer[3];

        sb.append("原始位元組:\n");
        sb.append("  Byte 6: ").append(String.format("%02X", b6)).append("\n");
        sb.append("  Byte 7: ").append(String.format("%02X", b7)).append("\n");
        sb.append("  Byte 8: ").append(String.format("%02X", b8)).append("\n");
        sb.append("  Byte 9 (備用): ").append(String.format("%02X", b9)).append("\n\n");

        int c1_3 = (b8 >> 3) & 1;
        int c2_3 = (b8 >> 4) & 1;
        int c3_3 = (b7 >> 3) & 1;

        int c1_2 = (b8 >> 1) & 1;
        int c2_2 = (b8 >> 2) & 1;
        int c3_2 = (b7 >> 1) & 1;

        int c1_1 = (b8 >> 0) & 1;
        int c2_1 = (b8 >> 5) & 1;
        int c3_1 = (b7 >> 0) & 1;

        int c1_0 = (b6 >> 6) & 1;
        int c2_0 = (b6 >> 7) & 1;
        int c3_0 = (b7 >> 6) & 1;

        int[][] c = {
            {c1_0, c2_0, c3_0},
            {c1_1, c2_1, c3_1},
            {c1_2, c2_2, c3_2},
            {c1_3, c2_3, c3_3}
        };

        String[] blockNames = {"Block 0 (Data)", "Block 1 (Data)", "Block 2 (Data)", "Block 3 (Sector Trailer)"};

        sb.append("存取條件表格:\n");
        sb.append(String.format("  %-22s %-6s %-6s %-6s\n", "區塊", "C1", "C2", "C3"));
        sb.append("  ").append("─".repeat(40)).append("\n");

        for (int i = 0; i < 4; i++) {
            sb.append(String.format("  %-22s %-6d %-6d %-6d\n",
                    blockNames[i], c[i][0], c[i][1], c[i][2]));
        }

        sb.append("\n解讀:\n");
        sb.append("  Block 0: ").append(accessMeaning(c[0])).append("\n");
        sb.append("  Block 1: ").append(accessMeaning(c[1])).append("\n");
        sb.append("  Block 2: ").append(accessMeaning(c[2])).append("\n");
        sb.append("  Sector Trailer: ").append(trailerMeaning(c[3])).append("\n");

        return sb.toString();
    }

    private static String accessMeaning(int[] c) {
        if (c[0] == 0 && c[1] == 0 && c[2] == 0) return "讀/寫 (KeyA/B)";
        if (c[0] == 0 && c[1] == 1 && c[2] == 0) return "讀 (KeyA/B), 寫 (KeyB)";
        if (c[0] == 1 && c[1] == 0 && c[2] == 0) return "讀/寫 (KeyB)";
        if (c[0] == 1 && c[1] == 1 && c[2] == 0) return "讀 (KeyB), 寫 (KeyB)";
        if (c[0] == 0 && c[1] == 0 && c[2] == 1) return "讀 (KeyB), 寫 (KeyA/B)";
        if (c[0] == 0 && c[1] == 1 && c[2] == 1) return "讀 (KeyB), 寫 (KeyB)";
        if (c[0] == 1 && c[1] == 0 && c[2] == 1) return "讀 (KeyB), 寫 (KeyB)";
        if (c[0] == 1 && c[1] == 1 && c[2] == 1) return "不可存取";
        return "未知";
    }

    private static String trailerMeaning(int[] c) {
        if (c[0] == 0 && c[1] == 0 && c[2] == 0) return "KeyA 讀/寫, KeyB 讀/寫, AC 讀/寫 (KeyA/B)";
        if (c[0] == 0 && c[1] == 1 && c[2] == 0) return "KeyA 不可讀, KeyB 讀/寫, AC 讀/寫 (KeyB)";
        if (c[0] == 1 && c[1] == 0 && c[2] == 0) return "KeyA 不可讀, KeyB 讀/寫, AC 讀/寫 (KeyB)";
        if (c[0] == 1 && c[1] == 1 && c[2] == 0) return "KeyA 不可讀/寫, KeyB 讀/寫, AC 讀/寫 (KeyB)";
        if (c[0] == 0 && c[1] == 0 && c[2] == 1) return "KeyA 讀/寫, KeyB 讀, AC 讀/寫 (KeyA/B)";
        if (c[0] == 0 && c[1] == 1 && c[2] == 1) return "KeyB 讀/寫, AC 讀 (KeyB)";
        if (c[0] == 1 && c[1] == 0 && c[2] == 1) return "KeyB 讀, AC 讀 (KeyB)";
        if (c[0] == 1 && c[1] == 1 && c[2] == 1) return "不可存取";
        return "未知";
    }

    public static String decodeValueBlock(byte[] data) {
        if (data == null || data.length < 16) {
            return "資料不足 (需要 16 bytes)";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Value Block 解碼:\n\n");

        long value1 = bytesToLong(data, 0);
        long value2 = bytesToLong(data, 4);
        long value3 = bytesToLong(data, 8);
        byte addr = data[12];

        sb.append("  Value (bytes 0-3):  ").append(value1).append("\n");
        sb.append("  ~Value (bytes 4-7): ").append(value2).append("\n");
        sb.append("  Value (bytes 8-11): ").append(value3).append("\n");
        sb.append("  Addr (byte 12):     ").append(addr & 0xFF).append("\n\n");

        if (value1 == (~value2 & 0xFFFFFFFFL) && value1 == value3) {
            sb.append("✓ 區塊驗證成功 (Value == ~Inverted == Value 備份)\n");
            sb.append("  數值為: ").append((int)value1).append("\n");
        } else {
            sb.append("✗ 區塊驗證失敗 (不是有效的 Value Block)\n");
            if (value1 == (~value2 & 0xFFFFFFFFL)) {
                sb.append("  Value == ~Inverted (OK)，但 Value 備份不一致\n");
            } else {
                sb.append("  Value != ~Inverted\n");
            }
        }

        return sb.toString();
    }

    public static byte[] encodeValueBlock(int value, byte addr) {
        byte[] data = new byte[16];
        longToBytes(value, data, 0);
        longToBytes(~value, data, 4);
        longToBytes(value, data, 8);
        data[12] = addr;
        data[13] = (byte)~addr;
        data[14] = (byte)addr;
        data[15] = (byte)~addr;
        return data;
    }

    public static String calculateBcc(String uidHex) {
        String clean = uidHex.replace(":", "").replace(" ", "").toUpperCase();

        if (clean.length() < 8) {
            return "UID 太短 (需要至少 4 bytes = 8 hex 字元)";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("BCC 計算:\n\n");
        sb.append("UID (前 4 bytes): ").append(clean.substring(0, 8)).append("\n\n");

        byte[] uid4 = hexToBytes(clean.substring(0, 8));
        int bcc = 0;
        for (byte b : uid4) {
            bcc ^= (b & 0xFF);
        }

        sb.append("  byte 0: ").append(String.format("%02X", uid4[0])).append("\n");
        sb.append("  byte 1: ").append(String.format("%02X", uid4[1])).append("\n");
        sb.append("  byte 2: ").append(String.format("%02X", uid4[2])).append("\n");
        sb.append("  byte 3: ").append(String.format("%02X", uid4[3])).append("\n");
        sb.append("  ─────────────────\n");
        sb.append("  BCC:   ").append(String.format("%02X", bcc & 0xFF)).append("\n\n");

        sb.append("驗算: ");
        int check = 0;
        for (byte b : uid4) check ^= (b & 0xFF);
        sb.append((check ^ bcc) == 0 ? "✓ 正確" : "✗ 錯誤");

        if (clean.length() >= 10) {
            sb.append("\n\nUID (完整 ").append(clean.length()/2).append(" bytes): ");
            sb.append(clean.substring(0, 8));
            if (clean.length() > 8) {
                byte bcc1 = hexToBytes(clean.substring(8, 10))[0];
                sb.append(" + BCC=").append(String.format("%02X", bcc1));
                if ((bcc1 & 0xFF) == (bcc & 0xFF)) {
                    sb.append(" ✓ BCC 正確");
                } else {
                    sb.append(" ✗ BCC 應為 ").append(String.format("%02X", bcc & 0xFF));
                }
            }
        }

        return sb.toString();
    }

    public static String hexToAscii7Bit(String hexStr) {
        if (hexStr == null || hexStr.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        String clean = hexStr.replace(" ", "").replace(":", "");
        for (int i = 0; i + 1 < clean.length(); i += 2) {
            int val = Character.digit(clean.charAt(i), 16) << 4
                    | Character.digit(clean.charAt(i + 1), 16);
            if (val >= 0x20 && val <= 0x7E) {
                sb.append((char) val);
            } else {
                sb.append('.');
            }
        }
        return sb.toString();
    }

    public static String hexWithAscii(String hexStr) {
        if (hexStr == null || hexStr.isEmpty()) return "";
        String ascii = hexToAscii7Bit(hexStr);
        return hexStr + "  |  " + ascii;
    }

    public static String hexWithAscii(byte[] data) {
        if (data == null) return "null";
        StringBuilder sb = new StringBuilder();

        sb.append("HEX:  ");
        for (byte b : data) {
            sb.append(String.format("%02X ", b));
        }
        sb.append("\n");

        sb.append("ASCII: ");
        for (byte b : data) {
            if (b >= 0x20 && b < 0x7F) {
                sb.append((char) b).append("  ");
            } else {
                sb.append(".  ");
            }
        }

        return sb.toString();
    }

    private static long bytesToLong(byte[] data, int offset) {
        return ((data[offset] & 0xFFL)) |
               ((data[offset+1] & 0xFFL) << 8) |
               ((data[offset+2] & 0xFFL) << 16) |
               ((data[offset+3] & 0xFFL) << 24);
    }

    private static void longToBytes(int value, byte[] data, int offset) {
        data[offset] = (byte)(value & 0xFF);
        data[offset+1] = (byte)((value >> 8) & 0xFF);
        data[offset+2] = (byte)((value >> 16) & 0xFF);
        data[offset+3] = (byte)((value >> 24) & 0xFF);
    }

    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i/2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }
}
