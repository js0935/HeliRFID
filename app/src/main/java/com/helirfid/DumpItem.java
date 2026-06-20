/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.0.1
 */
package com.helirfid;

public class DumpItem {
    private int sector;
    private int block;
    private String data;
    private String description;

    public DumpItem(int sector, int block, String data, String description) {
        this.sector = sector;
        this.block = block;
        this.data = data;
        this.description = description;
    }

    public int getSector() {
        return sector;
    }

    public int getBlock() {
        return block;
    }

    public String getData() {
        return data;
    }

    public String getDescription() {
        return description;
    }
}
