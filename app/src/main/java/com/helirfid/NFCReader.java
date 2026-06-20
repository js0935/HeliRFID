/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.0.1
 */
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
