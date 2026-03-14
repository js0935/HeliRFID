package com.helirfid.ultimate;

import android.nfc.Tag;

public class NFCReader {

    public static String getUID(Tag tag){

        byte[] uid = tag.getId();

        StringBuilder sb = new StringBuilder();

        for(byte b:uid)
            sb.append(String.format("%02X:",b));

        return sb.substring(0,sb.length()-1);

    }

}
