package com.helirfid.ultimate;

import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class DumpReader {

    public static String dumpTagInfo(Tag tag) {

        StringBuilder result = new StringBuilder();

        result.append("=== TAG INFO ===\n");
        result.append("UID: ").append(bytesToHex(tag.getId())).append("\n");

        try {
            MifareClassic mc = MifareClassic.get(tag);
            mc.connect();

            result.append("\n=== MIFARE CLASSIC INFO ===\n");
            result.append("Size: ").append(mc.getSize()).append(" bytes\n");
            result.append("Sector Count: ").append(mc.getSectorCount()).append("\n");
            result.append("Block Count: ").append(mc.getBlockCount()).append("\n");

            // 讀取 Sector 0 的 Block 0 (卡片資訊區)
            byte[] block0 = mc.readBlock(0);
            result.append("\n=== BLOCK 0 (Manufacturer Block) ===\n");
            result.append(bytesToHex(block0)).append("\n");

            // 讀取幾個 block 的範例
            result.append("\n=== SAMPLE BLOCKS ===\n");
            for (int i = 1; i < Math.min(4, mc.getBlockCount()); i++) {
                try {
                    byte[] block = mc.readBlock(i);
                    result.append("Block ").append(i).append(": ").append(bytesToHex(block)).append("\n");
                } catch (Exception e) {
                    result.append("Block ").append(i).append(": ").append("Access Denied\n");
                }
            }

            mc.close();

        } catch (Exception e) {
            Log.e("DumpReader", "Dump failed", e);
            result.append("\nDump Error: ").append(e.getMessage());
        }

        return result.toString();
    }

    public static String dumpSector(Tag tag, int sector) {

        StringBuilder result = new StringBuilder();

        try {
            MifareClassic mc = MifareClassic.get(tag);
            mc.connect();

            int blockCount = mc.getBlockCount() / mc.getSectorCount();
            int startBlock = sector * blockCount;

            result.append("=== SECTOR ").append(sector).append(" ===\n");

            for (int i = 0; i < blockCount; i++) {
                int blockNum = startBlock + i;
                try {
                    byte[] block = mc.readBlock(blockNum);
                    result.append("Block ").append(blockNum).append(": ").append(bytesToHex(block)).append("\n");
                } catch (Exception e) {
                    result.append("Block ").append(blockNum).append(": [Access Denied]\n");
                }
            }

            mc.close();

        } catch (Exception e) {
            result.append("Sector Dump Error: ").append(e.getMessage());
        }

        return result.toString();
    }

    public static List<String> dumpAllSectors(Tag tag) {

        List<String> sectors = new ArrayList<>();

        try {
            MifareClassic mc = MifareClassic.get(tag);
            mc.connect();

            int sectorCount = mc.getSectorCount();

            for (int s = 0; s < sectorCount; s++) {
                sectors.add(dumpSector(tag, s));
            }

            mc.close();

        } catch (Exception e) {
            sectors.add("Error: " + e.getMessage());
        }

        return sectors;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }
}
