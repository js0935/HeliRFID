/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.0.1
 */
package com.helirfid;

import java.util.ArrayList;
import java.util.List;

public class DumpStore {
    private static List<DumpItem> savedDump = null;
    private static String sourceInfo = "";
    private static byte[] rawDumpData = null;

    public static void saveDump(List<DumpItem> dump, String info) {
        savedDump = new ArrayList<>(dump);
        sourceInfo = info;
        rawDumpData = dumpToBytes(dump);
    }

    public static void setDumpData(byte[] data) {
        rawDumpData = data;
    }

    public static List<DumpItem> getDump() {
        return savedDump;
    }

    public static String getSourceInfo() {
        return sourceInfo;
    }

    public static void clear() {
        savedDump = null;
        sourceInfo = "";
    }

    public static byte[] getDumpData() {
        if (rawDumpData != null) return rawDumpData;
        if (savedDump != null) return dumpToBytes(savedDump);
        return null;
    }

    public static void clearDump() {
        savedDump = null;
        sourceInfo = "";
        rawDumpData = null;
    }

    public static boolean hasDump() {
        return (savedDump != null && !savedDump.isEmpty()) || rawDumpData != null;
    }

    private static byte[] dumpToBytes(List<DumpItem> dump) {
        if (dump == null) return null;
        java.util.ArrayList<Byte> bytes = new java.util.ArrayList<>();
        for (DumpItem item : dump) {
            String hex = item.getData().replace(" ", "");
            for (int i = 0; i < hex.length(); i += 2) {
                bytes.add((byte) ((Character.digit(hex.charAt(i), 16) << 4)
                        + Character.digit(hex.charAt(i + 1), 16)));
            }
        }
        byte[] result = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) result[i] = bytes.get(i);
        return result;
    }
}
