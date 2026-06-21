package com.helirfid;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;

import java.util.Arrays;

public class HceSimulationService extends HostApduService {

    private static final String PREF_NAME = "hce_profiles";
    private static final String KEY_ACTIVE = "active_profile";
    private static final String PREF_NDEF = "hce_ndef";
    private static final String KEY_NDEF_TYPE = "ndef_type";
    private static final String KEY_NDEF_DATA = "ndef_data";
    public static final int NDEF_TYPE_NONE = 0;
    public static final int NDEF_TYPE_TEXT = 1;
    public static final int NDEF_TYPE_URL = 2;
    public static final int NDEF_TYPE_PHONE = 3;
    public static final int NDEF_TYPE_SMS = 4;

    private static final byte[] NDEF_AID = Converter.hexToBytes("D2760000850101");
    private static final byte[] SELECT_AID_HEAD_00 = {0x00, (byte)0xA4, 0x04, 0x00};
    private static final byte[] SELECT_AID_HEAD_0C = {0x00, (byte)0xA4, 0x04, 0x0C};
    private static final byte[] SELECT_FILE_APDU = {0x00, (byte)0xA4, 0x00, 0x0C, 0x02};
    private static final byte[] READ_BINARY = {0x00, (byte)0xB0};
    private static final byte[] UPDATE_BINARY = {0x00, (byte)0xD6};

    private static final int SW_SUCCESS = 0x9000;
    private static final int SW_NOT_FOUND = 0x6A82;
    private static final int SW_WRONG_LE = 0x6C00;
    private static final int SW_UNSUPPORTED = 0x6A81;

    private static final short FILE_ID_CC = (short)0xE103;
    private static final short FILE_ID_NDEF = (short)0xE104;

    private short selectedFile = -1;
    private byte[] ndefMessage;

    private byte[] buildCC() {
        byte[] cc = new byte[14];
        cc[0] = 0x00; cc[1] = 0x0E;
        cc[2] = 0x20;
        cc[3] = (byte)0xFF;
        cc[4] = (byte)0xFE;
        cc[5] = 0x04; cc[6] = 0x06;
        cc[7] = (byte)0xE1; cc[8] = 0x04;
        int ndefLen = (ndefMessage != null) ? ndefMessage.length : 0;
        cc[9] = (byte)((ndefLen + 10) >> 8);
        cc[10] = (byte)((ndefLen + 10) & 0xFF);
        cc[11] = 0x00;
        cc[12] = (byte)0xFF;
        cc[13] = 0x00;
        return cc;
    }

    private byte[] buildNDEF() {
        if (ndefMessage == null) return new byte[]{0x00, 0x00};
        byte[] result = new byte[2 + ndefMessage.length];
        result[0] = (byte)(ndefMessage.length >> 8);
        result[1] = (byte)(ndefMessage.length & 0xFF);
        System.arraycopy(ndefMessage, 0, result, 2, ndefMessage.length);
        return result;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        loadNdefMessage();
        loadActiveProfile();
    }

    @Override
    public int onStartCommand(android.content.Intent intent, int flags, int startId) {
        if (intent == null) return START_STICKY;
        if (intent.hasExtra("profile_data")) {
            updateNdefMessage(
                    intent.getStringExtra("profile_uid"),
                    intent.getStringExtra("profile_name"),
                    intent.getStringExtra("profile_atqa"),
                    intent.getStringExtra("profile_sak"),
                    intent.getStringExtra("profile_tech"));
        }
        if (intent.hasExtra("custom_ndef")) {
            int type = intent.getIntExtra("ndef_type", NDEF_TYPE_NONE);
            String data = intent.getStringExtra("ndef_data");
            setCustomNdef(type, data);
            buildCustomNdefMessage(type, data);
        }
        return START_STICKY;
    }

    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        if (commandApdu == null || commandApdu.length < 4)
            return getStatusWord(SW_UNSUPPORTED);

        if (matchApdu(commandApdu, SELECT_AID_HEAD_00) || matchApdu(commandApdu, SELECT_AID_HEAD_0C))
            return handleSelect(commandApdu);
        if (matchApdu(commandApdu, SELECT_FILE_APDU))
            return handleSelectFile(commandApdu);
        if (commandApdu[0] == READ_BINARY[0] && commandApdu[1] == READ_BINARY[1])
            return handleReadBinary(commandApdu);
        if (commandApdu[0] == UPDATE_BINARY[0] && commandApdu[1] == UPDATE_BINARY[1])
            return handleUpdateBinary(commandApdu);

        return getStatusWord(SW_UNSUPPORTED);
    }

    @Override
    public void onDeactivated(int reason) {
        selectedFile = -1;
    }

    private byte[] handleSelect(byte[] cmd) {
        if (cmd.length < 7) return getStatusWord(SW_NOT_FOUND);
        int aidLen = cmd[4] & 0xFF;
        if (cmd.length < 5 + aidLen) return getStatusWord(SW_NOT_FOUND);

        if (Arrays.equals(Arrays.copyOfRange(cmd, 5, 5 + aidLen), NDEF_AID)) {
            selectedFile = -1;
            return (cmd[3] & 0x0C) != 0 ? buildFci() : getStatusWord(SW_SUCCESS);
        }
        return getStatusWord(SW_NOT_FOUND);
    }

    private byte[] buildFci() {
        byte[] aid = NDEF_AID;
        byte[] fci = new byte[6 + aid.length + 8];
        int off = 0;
        fci[off++] = (byte)0x6F;
        fci[off++] = (byte)(4 + aid.length + 8);
        fci[off++] = (byte)0x84;
        fci[off++] = (byte)aid.length;
        System.arraycopy(aid, 0, fci, off, aid.length);
        off += aid.length;
        fci[off++] = (byte)0xA5;
        fci[off++] = 0x08;
        fci[off++] = 0x06;
        fci[off++] = 0x02;
        fci[off++] = 0x20;
        fci[off++] = (byte)0xFF;
        fci[off++] = 0x5F;
        fci[off++] = 0x55;
        fci[off++] = 0x01;
        fci[off++] = (byte)0xFE;
        byte[] resp = Arrays.copyOf(fci, off + 2);
        resp[off] = (byte)(SW_SUCCESS >> 8);
        resp[off + 1] = (byte)(SW_SUCCESS & 0xFF);
        return resp;
    }

    private byte[] handleSelectFile(byte[] cmd) {
        if (cmd.length < 7) return getStatusWord(SW_NOT_FOUND);
        short fileId = (short)((cmd[5] & 0xFF) << 8 | (cmd[6] & 0xFF));
        if (fileId == FILE_ID_CC || fileId == FILE_ID_NDEF) {
            selectedFile = fileId;
            return getStatusWord(SW_SUCCESS);
        }
        return getStatusWord(SW_NOT_FOUND);
    }

    private byte[] handleReadBinary(byte[] cmd) {
        if (selectedFile == -1) return getStatusWord(SW_NOT_FOUND);
        int offset = ((cmd[2] & 0xFF) << 8) | (cmd[3] & 0xFF);
        int le = (cmd.length >= 5) ? (cmd[4] & 0xFF) : 256;
        if (le == 0) le = 256;

        byte[] data = (selectedFile == FILE_ID_CC) ? buildCC() : buildNDEF();

        if (offset >= data.length)
            return getStatusWord(offset > data.length ? 0x6B00 : 0x6282);

        int len = Math.min(le, data.length - offset);
        byte[] result = Arrays.copyOfRange(data, offset, offset + len);
        int sw = (len < le) ? 0x6282 : SW_SUCCESS;
        byte[] response = new byte[result.length + 2];
        System.arraycopy(result, 0, response, 0, result.length);
        response[response.length - 2] = (byte)(sw >> 8);
        response[response.length - 1] = (byte)(sw & 0xFF);
        return response;
    }

    private byte[] handleUpdateBinary(byte[] cmd) {
        if (selectedFile != FILE_ID_NDEF) return getStatusWord(SW_UNSUPPORTED);
        if (cmd.length < 5) return getStatusWord(SW_WRONG_LE);
        int dataLen = cmd[4] & 0xFF;
        if (cmd.length < 5 + dataLen) return getStatusWord(SW_WRONG_LE);

        int offset = ((cmd[2] & 0xFF) << 8) | (cmd[3] & 0xFF);
        if (offset == 0 && dataLen >= 2) {
            int msgLen = ((cmd[5] & 0xFF) << 8) | (cmd[6] & 0xFF);
            if (dataLen >= 2 + msgLen) {
                ndefMessage = Arrays.copyOfRange(cmd, 7, 7 + msgLen);
                saveNdefMessage();
            }
        }
        return getStatusWord(SW_SUCCESS);
    }

    private static boolean matchApdu(byte[] cmd, byte[] head) {
        if (cmd.length < head.length) return false;
        for (int i = 0; i < head.length; i++) {
            if (cmd[i] != head[i]) return false;
        }
        return true;
    }

    private static byte[] getStatusWord(int sw) {
        return new byte[]{(byte)(sw >> 8), (byte)(sw & 0xFF)};
    }

    private void updateNdefMessage(String uid, String name, String atqa, String sak, String tech) {
        if (uid == null || uid.isEmpty()) {
            ndefMessage = null;
            return;
        }
        StringBuilder sb = new StringBuilder("HeliRFID Card Info\n");
        if (name != null && !name.isEmpty()) sb.append("名稱: ").append(name).append("\n");
        sb.append("UID: ").append(uid).append("\n");
        if (atqa != null && !atqa.isEmpty() && !atqa.equals("0")) sb.append("ATQA: ").append(atqa).append("\n");
        if (sak != null && !sak.isEmpty() && !sak.equals("0")) sb.append("SAK: ").append(sak).append("\n");
        if (tech != null && !tech.isEmpty()) sb.append("技術: ").append(tech);
        ndefMessage = buildNdefTextRecord(sb.toString().getBytes());
        saveNdefMessage();
    }

    private static byte[] buildNdefTextRecord(byte[] textBytes) {
        int payloadLen = 3 + textBytes.length;
        byte[] record = new byte[4 + payloadLen];
        record[0] = (byte)0xD1;
        record[1] = 0x01;
        record[2] = (byte)payloadLen;
        record[3] = 'T';
        record[4] = 0x02;
        record[5] = 'e'; record[6] = 'n';
        System.arraycopy(textBytes, 0, record, 7, textBytes.length);
        return record;
    }

    private void loadActiveProfile() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String s = prefs.getString(KEY_ACTIVE, "");
        if (s.isEmpty()) return;
        HceCardProfile profile = HceCardProfile.fromStorageString(s);
        if (profile != null) {
            updateNdefMessage(profile.getUid(), profile.getName(),
                    profile.getAtqa(), profile.getSak(), profile.getTechTypes());
        }
    }

    private void buildCustomNdefMessage(int type, String data) {
        if (data == null || data.isEmpty()) {
            ndefMessage = null;
            return;
        }
        try {
            NdefRecord record;
            switch (type) {
                case NDEF_TYPE_URL:
                    record = NdefRecord.createUri(data.startsWith("http") ? data : "https://" + data);
                    break;
                case NDEF_TYPE_PHONE:
                    record = NdefRecord.createUri("tel:" + data.replaceAll("[^0-9+]", ""));
                    break;
                case NDEF_TYPE_SMS: {
                    String[] parts = data.split("\\|", 2);
                    record = NdefRecord.createUri("sms:" + parts[0] + "?body="
                            + android.net.Uri.encode(parts.length > 1 ? parts[1] : ""));
                    break;
                }
                default:
                    record = NdefRecord.createTextRecord("zh-TW", data);
                    break;
            }
            ndefMessage = new NdefMessage(new NdefRecord[]{record}).toByteArray();
        } catch (Exception ignored) {
            ndefMessage = null;
        }
    }

    private void setCustomNdef(int type, String data) {
        getSharedPreferences(PREF_NDEF, MODE_PRIVATE).edit()
                .putInt(KEY_NDEF_TYPE, type)
                .putString(KEY_NDEF_DATA, data != null ? data : "")
                .apply();
    }

    private void saveNdefMessage() {
        getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit()
                .putString("cached_ndef", ndefMessage != null ? Converter.bytesToHex(ndefMessage) : "")
                .apply();
    }

    private void loadNdefMessage() {
        String cached = getSharedPreferences(PREF_NAME, MODE_PRIVATE).getString("cached_ndef", "");
        if (!cached.isEmpty()) {
            ndefMessage = Converter.hexToBytes(cached);
        }
    }

    public static void setCustomNdefData(Context context, int type, String data) {
        context.getSharedPreferences(PREF_NDEF, Context.MODE_PRIVATE).edit()
                .putInt(KEY_NDEF_TYPE, type)
                .putString(KEY_NDEF_DATA, data != null ? data : "")
                .apply();
        Intent intent = new Intent(context, HceSimulationService.class);
        intent.putExtra("custom_ndef", true);
        intent.putExtra("ndef_type", type);
        intent.putExtra("ndef_data", data);
        context.startService(intent);
    }

    public static void clearCustomNdef(Context context) {
        context.getSharedPreferences(PREF_NDEF, Context.MODE_PRIVATE).edit()
                .putInt(KEY_NDEF_TYPE, NDEF_TYPE_NONE)
                .remove(KEY_NDEF_DATA)
                .apply();
        Intent intent = new Intent(context, HceSimulationService.class);
        intent.putExtra("profile_data", true);
        context.startService(intent);
    }

    public static int getNdefType(Context context) {
        return context.getSharedPreferences(PREF_NDEF, Context.MODE_PRIVATE)
                .getInt(KEY_NDEF_TYPE, NDEF_TYPE_NONE);
    }

    public static String getNdefData(Context context) {
        return context.getSharedPreferences(PREF_NDEF, Context.MODE_PRIVATE)
                .getString(KEY_NDEF_DATA, "");
    }

    public static boolean isCustomNdefActive(Context context) {
        return context.getSharedPreferences(PREF_NDEF, Context.MODE_PRIVATE)
                .getInt(KEY_NDEF_TYPE, NDEF_TYPE_NONE) != NDEF_TYPE_NONE;
    }

    public static void setActiveProfile(Context context, HceCardProfile profile) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (profile != null) {
            prefs.edit().putString(KEY_ACTIVE, profile.toStorageString()).apply();
        } else {
            prefs.edit().remove(KEY_ACTIVE).apply();
        }
    }

    public static HceCardProfile getActiveProfile(Context context) {
        String s = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_ACTIVE, "");
        return s.isEmpty() ? null : HceCardProfile.fromStorageString(s);
    }
}
