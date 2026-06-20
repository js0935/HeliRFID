/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.0.1
 */
package com.helirfid;

import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class NFCWriter {

    public static String writeNdefMessage(Tag tag, String text) {
        try {
            NdefRecord record = NdefRecord.createTextRecord("zh", text);
            NdefMessage message = new NdefMessage(new NdefRecord[]{record});

            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();
                ndef.writeNdefMessage(message);
                ndef.close();
                return "NDEF 寫入成功";
            }

            NdefFormatable formatable = NdefFormatable.get(tag);
            if (formatable != null) {
                formatable.connect();
                formatable.format(message);
                formatable.close();
                return "標籤已格式化並寫入 NDEF";
            }

            return "不支援 NDEF 寫入";
        } catch (FormatException e) {
            return "NDEF 格式錯誤: " + e.getMessage();
        } catch (IOException e) {
            return "I/O 錯誤: " + e.getMessage();
        } catch (Exception e) {
            return "寫入失敗: " + e.getMessage();
        }
    }

    public static String writeMifareBlock(Tag tag, int sectorIndex, int blockIndex, byte[] data) {
        try {
            MifareClassic mfc = MifareClassic.get(tag);
            if (mfc == null) {
                return "不支援 MIFARE Classic";
            }

            mfc.connect();

            boolean authenticated = false;
            for (int k = 0; k < KeyTester.getKeyCount(); k++) {
                byte[] key = KeyTester.getKeyByKeyIndex(k);
                if (key == null) continue;
                try {
                    mfc.authenticateSectorWithKeyA(sectorIndex, key);
                    authenticated = true;
                    break;
                } catch (Exception e) {
                    // try next key
                }
            }

            if (!authenticated) {
                mfc.close();
                return "無法認證磁區 " + sectorIndex + " (無可用金鑰)";
            }

            mfc.writeBlock(blockIndex, data);
            mfc.close();
            return "區塊 " + blockIndex + " 寫入成功";
        } catch (IOException e) {
            return "I/O 錯誤: " + e.getMessage();
        } catch (Exception e) {
            return "寫入失敗: " + e.getMessage();
        }
    }

    public static String factoryFormat(Tag tag) {
        try {
            MifareClassic mfc = MifareClassic.get(tag);
            if (mfc == null) {
                return "不支援 MIFARE Classic";
            }

            mfc.connect();
            int sectorCount = mfc.getSectorCount();
            int formatted = 0;

            for (int s = 0; s < sectorCount; s++) {
                boolean auth = false;
                byte[] usedKey = null;
                for (int k = 0; k < KeyTester.getKeyCount(); k++) {
                    byte[] key = KeyTester.getKeyByKeyIndex(k);
                    if (key == null) continue;
                    try {
                        if (mfc.authenticateSectorWithKeyA(s, key)) {
                            auth = true;
                            usedKey = key;
                            break;
                        }
                    } catch (Exception e) { }
                }
                if (!auth) continue;

                int blockCount = mfc.getBlockCountInSector(s);
                int firstBlock = mfc.sectorToBlock(s);

                for (int b = 0; b < blockCount - 1; b++) {
                    byte[] blank = new byte[16];
                    mfc.writeBlock(firstBlock + b, blank);
                }

                byte[] trailer = new byte[16];
                System.arraycopy(usedKey, 0, trailer, 0, 6);
                trailer[6] = (byte)0xFF;
                trailer[7] = (byte)0x07;
                trailer[8] = (byte)0x80;
                trailer[9] = 0x69;
                System.arraycopy(usedKey, 0, trailer, 10, 6);
                mfc.writeBlock(firstBlock + blockCount - 1, trailer);
                formatted++;
            }

            mfc.close();
            return "格式化完成 (" + formatted + "/" + sectorCount + " 磁區)";
        } catch (Exception e) {
            return "格式化失敗: " + e.getMessage();
        }
    }

    public static String writeManufacturerBlock(Tag tag, byte[] data) {
        try {
            MifareClassic mfc = MifareClassic.get(tag);
            if (mfc == null) {
                return "不支援 MIFARE Classic";
            }
            mfc.connect();

            boolean auth = false;
            for (int k = 0; k < KeyTester.getKeyCount(); k++) {
                byte[] key = KeyTester.getKeyByKeyIndex(k);
                if (key == null) continue;
                try {
                    if (mfc.authenticateSectorWithKeyA(0, key)) {
                        auth = true;
                        break;
                    }
                } catch (Exception e) { }
            }

            if (!auth) {
                mfc.close();
                return "無法認證 Sector 0 (無可用金鑰)";
            }

            mfc.writeBlock(0, data);
            mfc.close();
            return "製造商區塊 (Block 0) 寫入成功 (僅適用於 CUID/FUID 卡)";
        } catch (IOException e) {
            return "I/O 錯誤: " + e.getMessage();
        } catch (Exception e) {
            return "寫入失敗: " + e.getMessage() + "\n注意：標準卡片 Block 0 為唯讀，僅 CUID/FUID 卡可寫入";
        }
    }

    public static String writeNdefMessage(Tag tag, NdefRecord record) {
        try {
            NdefMessage message = new NdefMessage(new NdefRecord[]{record});
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();
                ndef.writeNdefMessage(message);
                ndef.close();
                return "NDEF 寫入成功";
            }
            NdefFormatable formatable = NdefFormatable.get(tag);
            if (formatable != null) {
                formatable.connect();
                formatable.format(message);
                formatable.close();
                return "標籤已格式化並寫入 NDEF";
            }
            return "不支援 NDEF 寫入";
        } catch (FormatException e) {
            return "NDEF 格式錯誤: " + e.getMessage();
        } catch (IOException e) {
            return "I/O 錯誤: " + e.getMessage();
        } catch (Exception e) {
            return "寫入失敗: " + e.getMessage();
        }
    }

    public static NdefRecord createUrlRecord(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }
        return NdefRecord.createUri(url);
    }

    public static NdefRecord createEmailRecord(String to, String subject, String body) {
        String uri = "mailto:" + to + "?subject=" + android.net.Uri.encode(subject)
                + "&body=" + android.net.Uri.encode(body);
        return NdefRecord.createUri(uri);
    }

    public static NdefRecord createPhoneRecord(String number) {
        return NdefRecord.createUri("tel:" + number.replaceAll("[^0-9+]", ""));
    }

    public static NdefRecord createSmsRecord(String number, String message) {
        return NdefRecord.createUri("sms:" + number + "?body=" + android.net.Uri.encode(message));
    }

    public static NdefRecord createGeoLocationRecord(double lat, double lng) {
        return NdefRecord.createUri("geo:" + lat + "," + lng);
    }

    public static NdefRecord createWifiConfigRecord(String ssid, String password, String type) {
        String config = "WIFI:S:" + ssid + ";T:" + type + ";P:" + password + ";;";
        return new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                "application/vnd.wfa.wsc".getBytes(StandardCharsets.US_ASCII),
                new byte[]{}, config.getBytes(StandardCharsets.UTF_8));
    }

    public static NdefRecord createVCardRecord(String name, String phone, String email) {
        String vcard = "BEGIN:VCARD\nVERSION:3.0\nFN:" + name
                + "\nTEL:" + phone + "\nEMAIL:" + email + "\nEND:VCARD";
        return new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                "text/vcard".getBytes(StandardCharsets.US_ASCII),
                new byte[]{}, vcard.getBytes(StandardCharsets.UTF_8));
    }

    public static NdefRecord createBluetoothRecord(String mac, String name) {
        String btUri = "btspp://" + mac;
        if (name != null && !name.isEmpty()) {
            btUri += "?name=" + android.net.Uri.encode(name);
        }
        return NdefRecord.createUri(btUri);
    }

    public static String writeUltralightPage(Tag tag, int page, byte[] data) {
        try {
            MifareUltralight mu = MifareUltralight.get(tag);
            if (mu == null) {
                return "不支援 MIFARE Ultralight";
            }

            mu.connect();
            mu.writePage(page, data);
            mu.close();
            return "頁面 " + page + " 寫入成功";
        } catch (IOException e) {
            return "I/O 錯誤: " + e.getMessage();
        } catch (Exception e) {
            return "寫入失敗: " + e.getMessage();
        }
    }
}
