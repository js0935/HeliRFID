package com.helirfid.ultimate;

import android.nfc.Tag;

public class CardAnalyzer {

    public static String analyze(Tag tag){

        String[] tech = tag.getTechList();

        StringBuilder result=new StringBuilder("Card Type: ");

        for(String t:tech){

            if(t.contains("MifareClassic"))
                result.append("MIFARE Classic ");
            if(t.contains("MifareUltralight"))
                result.append("MIFARE Ultralight ");
            if(t.contains("IsoDep"))
                result.append("ISO 14443-4 ");
            if(t.contains("NfcA"))
                result.append("NFC-A ");
            if(t.contains("NfcB"))
                result.append("NFC-B ");
            if(t.contains("NfcF"))
                result.append("NFC-F ");
            if(t.contains("NfcV"))
                result.append("NFC-V ");

        }

        return result.toString();

    }

}
