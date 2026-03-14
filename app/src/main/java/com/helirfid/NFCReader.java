package com.helirfid;

import android.nfc.Tag;

public class NFCReader {

    private static Tag lastTag = null;

    public static String getUID(Tag tag){
        if(tag == null) return "";

        byte[] uid = tag.getId();

        StringBuilder sb = new StringBuilder();

        for(byte b:uid)
            sb.append(String.format("%02X:",b));

        return sb.substring(0,sb.length()-1);
    }

    public static Tag getLastTag() {
        return lastTag;
    }

    public static void setLastTag(Tag tag) {
        lastTag = tag;
    }
}
