/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.0.1
 */
package com.helirfid;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;

import java.nio.charset.StandardCharsets;

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

    public static String readNdefMessage(Intent intent) {
        if (intent == null) return null;
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) return null;
        Ndef ndef = Ndef.get(tag);
        if (ndef == null) return null;
        try {
            ndef.connect();
            NdefMessage msg = ndef.getNdefMessage();
            ndef.close();
            if (msg == null) return "空白";
            StringBuilder sb = new StringBuilder();
            for (NdefRecord record : msg.getRecords()) {
                String tnf = record.getTnf() + "";
                byte[] payload = record.getPayload();
                String type = new String(record.getType(), StandardCharsets.US_ASCII);
                if (record.getTnf() == NdefRecord.TNF_WELL_KNOWN && "U".equals(type)) {
                    sb.append("URL: ").append(new String(payload, StandardCharsets.UTF_8));
                } else if (record.getTnf() == NdefRecord.TNF_WELL_KNOWN && "T".equals(type)) {
                    sb.append("文字: ").append(new String(payload, StandardCharsets.UTF_8));
                } else {
                    sb.append(type).append(" (").append(payload.length).append(" bytes)");
                }
                sb.append(" | ");
            }
            return sb.toString().trim();
        } catch (Exception e) {
            return "讀取失敗: " + e.getMessage();
        }
    }
}
