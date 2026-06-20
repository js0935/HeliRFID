/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.0.1
 */
package com.helirfid;

import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;

public class CardAnalyzer {

    public static String analyze(Tag tag){
        String[] tech = tag.getTechList();

        StringBuilder result=new StringBuilder("Card Type: ");

        boolean mifareClassic=false;
        boolean mifareUL=false;

        for(String t:tech){
            if(t.contains("MifareClassic"))
                mifareClassic=true;
            if(t.contains("MifareUltralight"))
                mifareUL=true;
            if(t.contains("IsoDep"))
                result.append("ISO 14443-4 ");
            if(t.contains("NfcA"))
                result.append("NFC-A ");
            if(t.contains("NfcB"))
                result.append("NFC-B ");
            if(t.contains("NfcF"))
                result.append("NFC-F (FeliCa) ");
            if(t.contains("NfcV"))
                result.append("NFC-V ");
            if(t.contains("Ndef"))
                result.append("NDEF ");
            if(t.contains("NdefFormatable"))
                result.append("NDEF-可格式化 ");
        }

        result.append("\n\n");

        if(mifareClassic){
            result.append("Type: MIFARE Classic\n");
            try {
                MifareClassic mfc = MifareClassic.get(tag);
                int size = mfc.getSize();
                switch(size){
                    case MifareClassic.SIZE_1K:
                        result.append("容量: 1KB (16 磁區, 64 區塊)\n");
                        break;
                    case MifareClassic.SIZE_2K:
                        result.append("容量: 2KB (32 磁區, 128 區塊)\n");
                        break;
                    case MifareClassic.SIZE_4K:
                        result.append("容量: 4KB (40 磁區, 256 區塊)\n");
                        break;
                    default:
                        result.append("容量: 未知\n");
                }
            } catch(Exception e){
                result.append("容量: 讀取失敗\n");
            }
        }

        if(mifareUL){
            result.append("Type: MIFARE Ultralight\n");
            try {
                MifareUltralight mu = MifareUltralight.get(tag);
                int type = mu.getType();
                switch(type){
                    case MifareUltralight.TYPE_ULTRALIGHT:
                        result.append("型號: Ultralight (64 bytes)\n");
                        break;
                    case MifareUltralight.TYPE_ULTRALIGHT_C:
                        result.append("型號: Ultralight C (192 bytes)\n");
                        break;
                    default:
                        result.append("型號: Ultralight 其他\n");
                }
            } catch(Exception e){
                result.append("型號: 讀取失敗\n");
            }
        }

        if(!mifareClassic && !mifareUL){
            boolean hasNfcF=false;
            boolean hasIsoDep=false;
            for(String t:tech){
                if(t.contains("NfcF")) hasNfcF=true;
                if(t.contains("IsoDep")) hasIsoDep=true;
            }
            if(hasNfcF){
                result.append("Type: FeliCa 卡片\n");
            } else if(hasIsoDep){
                result.append("Type: ISO 14443-4 卡片 (可能為 DESFire)\n");
            } else {
                result.append("Type: 其他 NFC 標籤\n");
            }

            try {
                Ndef ndef = Ndef.get(tag);
                if(ndef != null){
                    result.append("支援 NDEF 格式\n");
                    result.append("最大大小: ").append(ndef.getMaxSize()).append(" bytes\n");
                }
            } catch(Exception e){
                // ignore
            }
        }

        return result.toString();
    }
}
