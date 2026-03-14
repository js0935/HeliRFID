package com.helirfid.ultimate;

import android.nfc.Tag;

public class CloneAnalyzer {

    public static String analyze(Tag tag){

        String[] tech = tag.getTechList();

        boolean mifare=false;

        for(String t:tech){

            if(t.contains("MifareClassic"))
                mifare=true;

        }

        if(mifare)
            return "Clone Risk : Possible";

        return "Clone Risk : Low";

    }

}
