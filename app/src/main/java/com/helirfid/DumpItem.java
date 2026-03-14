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
