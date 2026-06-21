package com.helirfid;

import android.nfc.Tag;
import android.nfc.tech.MifareClassic;

public class KeyTester {

    private static final String[][] KEY_ENTRIES = {
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
        {"VingCard 飯店 A",               "44454D4F414D"},
        {"VingCard 飯店 B",               "434144454D4F"},
        {"VingCard 飯店 C",               "56494E474341"},
        {"VingCard 飯店 D",               "56494E474344"},
        {"飯店 SALTON",                   "53414C544F4E"},
        {"飯店 hotel1",                   "686F74656C31"},
        {"飯店 hotel2",                   "686F74656C32"},
        {"飯店 hotel3",                   "686F74656C33"},
        {"荷蘭 OV-Chipkaart",             "4B0B20104B0B"},
        {"瑞典 Västtrafiken",             "828384858687"},
        {"iCopy-X 預設",                  "4A49434F5059"},
        {"iCopy-X 備用",                  "49434F505958"},
        {"RKF 莫斯科地鐵",                 "50494B2B3752"},
        {"基輔地鐵 A",                     "8E0C99CCCCC0"},
        {"基輔地鐵 B",                     "81BD43176684"},
        {"基輔地鐵 C",                     "FD452B2C1122"},
        {"XXT 停車卡",                     "931A4499C3D7"},
        {"俄羅斯 Troika",                  "8D540A123456"},
        {"英國 Oyster",                    "000000000001"},
        {"香港八達通 A",                   "A234B456C789"},
        {"1 重複",                         "111111111111"},
        {"2 重複",                         "222222222222"},
        {"3 重複",                         "333333333333"},
        {"4 重複",                         "444444444444"},
        {"5 重複",                         "555555555555"},
        {"6 重複",                         "666666666666"},
        {"7 重複",                         "777777777777"},
        {"8 重複",                         "888888888888"},
        {"9 重複",                         "999999999999"},
        {"A 重複",                         "AAAAAAAAAAAA"},
        {"B 重複",                         "BBBBBBBBBBBB"},
        {"C 重複",                         "CCCCCCCCCCCC"},
        {"D 重複",                         "DDDDDDDDDDDD"},
        {"E 重複",                         "EEEEEEEEEEEE"},
        {"順序 01-06",                     "010203040506"},
        {"順序 0A-0F",                     "0A0B0C0D0E0F"},
        {"順序 11-16",                     "112233445566"},
        {"順序 12-17",                     "123456789ABC"},
        {"順序 12-10",                     "1234567890AB"},
        {"順序 21-09",                     "2143658709AB"},
        {"順序 34-3E",                     "3456789ABCDE"},
        {"順序 40-45",                     "404142434445"},
        {"順序 61-66 (abcdef)",           "616263646566"},
        {"順序 67-6C (ghijkl)",           "6768696A6B6C"},
        {"A122 測試",                      "A12233445566"},
        {"ABAB 測試",                      "ABABABABABAB"},
        {"ABCD 測試",                      "ABCDEFABCDEF"},
        {"AB0C 測試",                      "AB0CD0EF0AB0"},
        {"ABCD-01 測試",                   "ABCD01020304"},
        {"ACB1 測試",                      "ACB1230FEDCB"},
        {"AABB 測試",                      "AABBCCDDEEFF"},
        {"01 重複",                        "010101010101"},
        {"02 重複",                        "020202020202"},
        {"03 重複",                        "030303030303"},
        {"04 重複",                        "040404040404"},
        {"05 重複",                        "050505050505"},
        {"06 重複",                        "060606060606"},
        {"07 重複",                        "070707070707"},
        {"08 重複",                        "080808080808"},
        {"09 重複",                        "090909090909"},
        {"0A 重複",                        "0A0A0A0A0A0A"},
        {"0B 重複",                        "0B0B0B0B0B0B"},
        {"0C 重複",                        "0C0C0C0C0C0C"},
        {"0D 重複",                        "0D0D0D0D0D0D"},
        {"0E 重複",                        "0E0E0E0E0E0E"},
        {"0F 重複",                        "0F0F0F0F0F0F"},
        {"0F01 循環",                      "0F010F010F01"},
        {"F0 重複",                        "F0F0F0F0F0F0"},
        {"F1-F6 順序",                     "F1F2F3F4F5F6"},
        {"3E-00 系列",                     "3E0000000000"},
        {"4D-00 系列",                     "4D0000000000"},
        {"4E-00 系列",                     "4E0000000000"},
        {"50-00 系列",                     "500000000000"},
        {"51-00 系列",                     "510000000000"},
        {"52-00 系列",                     "520000000000"},
        {"53-00 系列",                     "530000000000"},
        {"54-00 系列",                     "540000000000"},
        {"55-00 系列",                     "550000000000"},
        {"56-00 系列",                     "560000000000"},
        {"57-00 系列",                     "570000000000"},
        {"58-00 系列",                     "580000000000"},
        {"59-00 系列",                     "590000000000"},
        {"12 重複",                        "121212121212"},
        {"13 重複",                        "131313131313"},
        {"14 重複",                        "141414141414"},
        {"15 重複",                        "151515151515"},
        {"16 重複",                        "161616161616"},
        {"17 重複",                        "171717171717"},
        {"18 重複",                        "181818181818"},
        {"19 重複",                        "191919191919"},
        {"1A 重複",                        "1A1A1A1A1A1A"},
        {"1B 重複",                        "1B1B1B1B1B1B"},
        {"1C 重複",                        "1C1C1C1C1C1C"},
        {"1D 重複",                        "1D1D1D1D1D1D"},
        {"1E 重複",                        "1E1E1E1E1E1E"},
        {"1F 重複",                        "1F1F1F1F1F1F"},
        {"20 重複",                        "202020202020"},
        {"21 重複",                        "212121212121"},
        {"23 重複",                        "232323232323"},
        {"24 重複",                        "242424242424"},
        {"25 重複",                        "252525252525"},
        {"26 重複",                        "262626262626"},
        {"27 重複",                        "272727272727"},
        {"28 重複",                        "282828282828"},
        {"29 重複",                        "292929292929"},
        {"2A 重複",                        "2A2A2A2A2A2A"},
        {"2B 重複",                        "2B2B2B2B2B2B"},
        {"2C 重複",                        "2C2C2C2C2C2C"},
        {"2D 重複",                        "2D2D2D2D2D2D"},
        {"2E 重複",                        "2E2E2E2E2E2E"},
        {"2F 重複",                        "2F2F2F2F2F2F"},
        {"30 重複",                        "303030303030"},
        {"31 重複",                        "313131313131"},
        {"32 重複",                        "323232323232"},
        {"41 重複",                        "414141414141"},
        {"42 重複",                        "424242424242"},
        {"43 重複",                        "434343434343"},
        {"46 重複",                        "464646464646"},
        {"47 重複",                        "474747474747"},
        {"48 重複",                        "484848484848"},
        {"49 重複",                        "494949494949"},
        {"4A 重複",                        "4A4A4A4A4A4A"},
        {"4B 重複",                        "4B4B4B4B4B4B"},
        {"4C 重複",                        "4C4C4C4C4C4C"},
        {"4D 重複",                        "4D4D4D4D4D4D"},
        {"4E 重複",                        "4E4E4E4E4E4E"},
        {"6F (ooo) 重複",                  "6F6F6F6F6F6F"},
        {"70 (ppp) 重複",                  "707070707070"},
        {"71 (qqq) 重複",                  "717171717171"},
        {"73 (sss) 重複",                  "737373737373"},
        {"74 (ttt) 重複",                  "747474747474"},
        {"75 (uuu) 重複",                  "757575757575"},
        {"76 (vvv) 重複",                  "767676767676"},
        {"78 (xxx) 重複",                  "787878787878"},
        {"79 (yyy) 重複",                  "797979797979"},
        {"7A (zzz) 重複",                  "7A7A7A7A7A7A"},
        {"3C (<<) 重複",                   "3C3C3C3C3C3C"},
        {"3E (>>) 重複",                   "3E3E3E3E3E3E"},
        {"3F (??) 重複",                   "3F3F3F3F3F3F"},
        {"secret 字串",                    "736563726574"},
        {"univer 字串",                    "756E69766572"},
        {"ledyor 字串",                    "6C6564796F72"},
        {"cardpass 字串",                  "63617264706173"},
        {"nokia 字串",                     "6E6F6B696120"},
        {"icopy 字串",                     "69636F707961"},
        {"FFFF-0000",                      "FFFFFF000000"},
        {"0000-FFFF",                      "000000FFFFFF"},
    };

    private static byte[] hexToBytes(String s) {
        byte[] data = new byte[s.length() / 2];
        for (int i = 0; i < s.length(); i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

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

            for (String[] entry : KEY_ENTRIES) {
                if (testSector0Key(mifare, hexToBytes(entry[1]))) {
                    result.append("✓ ").append(entry[0]).append(": ");
                    result.append(entry[1]).append("\n");
                    foundCount++;
                }
            }

            mifare.close();

            if (foundCount == 0) {
                result.append("無可用金鑰");
            } else {
                result.append("\n共找到 ").append(foundCount).append(" 組可用金鑰 (共 ")
                      .append(KEY_ENTRIES.length).append(" 組字典)");
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
        result.append("跨磁區金鑰測試 (").append(KEY_ENTRIES.length).append(" 組字典):\n");

        try {
            mifare.connect();
            int sectorCount = mifare.getSectorCount();
            int totalFound = 0;

            for (int s = 0; s < sectorCount; s++) {
                boolean sectorOk = false;
                for (String[] entry : KEY_ENTRIES) {
                    try {
                        if (mifare.authenticateSectorWithKeyA(s, hexToBytes(entry[1]))) {
                            result.append("S").append(s).append(": ✓ ")
                                  .append(entry[0]).append("\n");
                            sectorOk = true;
                            totalFound++;
                            break;
                        }
                    } catch (Exception e) {
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

    public static String getKeyByKeyName(String keyName) {
        for (String[] entry : KEY_ENTRIES) {
            if (entry[0].equals(keyName)) {
                return entry[1];
            }
        }
        return null;
    }

    public static byte[] getKeyByKeyIndex(int index) {
        if (index >= 0 && index < KEY_ENTRIES.length) {
            return hexToBytes(KEY_ENTRIES[index][1]);
        }
        return null;
    }

    public static int getKeyCount() {
        return KEY_ENTRIES.length;
    }

    public static String getAllKeys() {
        StringBuilder sb = new StringBuilder();
        sb.append("MIFARE Classic 金鑰字典 (").append(KEY_ENTRIES.length).append(" 組):\n\n");
        for (int i = 0; i < KEY_ENTRIES.length; i++) {
            sb.append(String.format("%3d. %-24s %s\n", i + 1, KEY_ENTRIES[i][0], KEY_ENTRIES[i][1]));
        }
        return sb.toString();
    }

    public static String[][] getKeyEntries() {
        return KEY_ENTRIES;
    }

    public static String getKeyHexByIndex(int index) {
        if (index >= 0 && index < KEY_ENTRIES.length) {
            return KEY_ENTRIES[index][1];
        }
        return null;
    }

    public static String getKeyNameByIndex(int index) {
        if (index >= 0 && index < KEY_ENTRIES.length) {
            return KEY_ENTRIES[index][0];
        }
        return null;
    }
}
