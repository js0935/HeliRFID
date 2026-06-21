package com.helirfid;

import android.content.Context;
import android.content.SharedPreferences;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;

import java.util.Arrays;

public class HceSimulationService extends HostApduService {

    private static final String PREF_NAME = "hce_profiles";
    private static final String KEY_ACTIVE = "active_profile";

    private static final byte[] NDEF_AID = hexToBytes("D2760000850101");
    private static final byte[] SELECT_APDU_HEAD = {0x00, (byte)0xA4, 0x04, 0x00};
    private static final byte[] SELECT_FILE_APDU = {0x00, (byte)0xA4, 0x00, 0x0C, 0x02};
    private static final byte[] READ_BINARY = {0x00, (byte)0xB0};
    private static final byte[] UPDATE_BINARY = {0x00, (byte)0xD6};

    private static final int SW_SUCCESS = 0x9000;
    private static final int SW_NOT_FOUND = 0x6A82;
    private static final int SW_WRONG_LE = 0x6C00;
    private static final int SW_UNSUPPORTED = 0x6A81;
    private static final int SW_END_OF_FILE = 0x6B00;

    private static final short FILE_ID_CC = (short)0xE103;
    private static final short FILE_ID_NDEF = (short)0xE104;

    private short selectedFile = -1;
    private boolean ndefSelected = false;

    private byte[] ndefMessage;

    private byte[] buildCC() {
        byte[] cc = new byte[15];
        cc[0] = 0x00; cc[1] = 0x0F;
        cc[2] = 0x20;
        cc[3] = (byte)0xFF;
        cc[4] = (byte)0xFE;
        cc[5] = 0x04; cc[6] = 0x06;
        cc[7] = (byte)0xE1; cc[8] = 0x04;
        int ndefLen = (ndefMessage != null) ? ndefMessage.length : 0;
        cc[9] = (byte)((ndefLen + 10) >> 8);
        cc[10] = (byte)((ndefLen + 10) & 0xFF);
        cc[11] = 0x00;
        cc[12] = 0x00; cc[13] = (byte)0xFF;
        cc[14] = 0x00;
        return cc;
    }

    private byte[] buildNDEF() {
        if (ndefMessage == null) {
            return new byte[]{0x00, 0x00};
        }
        byte[] result = new byte[2 + ndefMessage.length];
        result[0] = (byte)(ndefMessage.length >> 8);
        result[1] = (byte)(ndefMessage.length & 0xFF);
        System.arraycopy(ndefMessage, 0, result, 2, ndefMessage.length);
        return result;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        loadActiveProfile();
    }

    @Override
    public int onStartCommand(android.content.Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("profile_data")) {
            String uid = intent.getStringExtra("profile_uid");
            String name = intent.getStringExtra("profile_name");
            updateNdefMessage(uid, name);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        if (commandApdu == null || commandApdu.length < 4)
            return getStatusWord(SW_UNSUPPORTED);

        if (matchApdu(commandApdu, SELECT_APDU_HEAD)) {
            return handleSelect(commandApdu);
        }

        if (matchApdu(commandApdu, SELECT_FILE_APDU) && ndefSelected) {
            return handleSelectFile(commandApdu);
        }

        if (commandApdu[0] == READ_BINARY[0] && commandApdu[1] == READ_BINARY[1] && ndefSelected) {
            return handleReadBinary(commandApdu);
        }

        if (commandApdu[0] == UPDATE_BINARY[0] && commandApdu[1] == UPDATE_BINARY[1] && ndefSelected) {
            return handleUpdateBinary(commandApdu);
        }

        return getStatusWord(SW_UNSUPPORTED);
    }

    @Override
    public void onDeactivated(int reason) {
        selectedFile = -1;
        ndefSelected = false;
    }

    private byte[] handleSelect(byte[] cmd) {
        if (cmd.length < 7) return getStatusWord(SW_NOT_FOUND);
        int aidLen = cmd[4] & 0xFF;
        if (cmd.length < 5 + aidLen) return getStatusWord(SW_NOT_FOUND);

        byte[] aid = Arrays.copyOfRange(cmd, 5, 5 + aidLen);
        if (Arrays.equals(aid, NDEF_AID)) {
            ndefSelected = true;
            selectedFile = -1;
            return getStatusWord(SW_SUCCESS);
        }
        ndefSelected = false;
        return getStatusWord(SW_NOT_FOUND);
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
        int le = (cmd.length >= 5) ? (cmd[4] & 0xFF) : 0;
        if (le == 0) le = 256;

        byte[] data;
        if (selectedFile == FILE_ID_CC) {
            data = buildCC();
        } else if (selectedFile == FILE_ID_NDEF) {
            data = buildNDEF();
        } else {
            return getStatusWord(SW_NOT_FOUND);
        }

        if (offset >= data.length) return getStatusWord(SW_END_OF_FILE);
        int available = data.length - offset;
        int len = Math.min(le, available);
        byte[] result = Arrays.copyOfRange(data, offset, offset + len);
        byte[] response = new byte[result.length + 2];
        System.arraycopy(result, 0, response, 0, result.length);
        response[response.length - 2] = (byte)(SW_SUCCESS >> 8);
        response[response.length - 1] = (byte)(SW_SUCCESS & 0xFF);
        return response;
    }

    private byte[] handleUpdateBinary(byte[] cmd) {
        if (selectedFile != FILE_ID_NDEF) return getStatusWord(SW_UNSUPPORTED);
        if (cmd.length < 5) return getStatusWord(SW_WRONG_LE);
        int offset = ((cmd[2] & 0xFF) << 8) | (cmd[3] & 0xFF);
        int dataLen = cmd[4] & 0xFF;
        if (cmd.length < 5 + dataLen) return getStatusWord(SW_WRONG_LE);

        if (offset == 0 && dataLen >= 2) {
            int msgLen = ((cmd[5] & 0xFF) << 8) | (cmd[6] & 0xFF);
            if (dataLen >= 2 + msgLen) {
                ndefMessage = Arrays.copyOfRange(cmd, 7, 7 + msgLen);
                saveNdefMessage();
            }
        }

        return getStatusWord(SW_SUCCESS);
    }

    private boolean matchApdu(byte[] cmd, byte[] head) {
        if (cmd.length < head.length) return false;
        for (int i = 0; i < head.length; i++) {
            if (cmd[i] != head[i]) return false;
        }
        return true;
    }

    private byte[] getStatusWord(int sw) {
        return new byte[]{(byte)(sw >> 8), (byte)(sw & 0xFF)};
    }

    private void updateNdefMessage(String uid, String name) {
        if (uid == null || uid.isEmpty()) {
            ndefMessage = null;
            return;
        }
        String text;
        if (name != null && !name.isEmpty()) {
            text = "HeliRFID Card: " + name + " | UID: " + uid;
        } else {
            text = "HeliRFID Card | UID: " + uid;
        }
        byte[] textBytes = text.getBytes();
        ndefMessage = buildNdefTextRecord(textBytes);
        saveNdefMessage();
    }

    private byte[] buildNdefTextRecord(byte[] textBytes) {
        int tnfByte = 0xD1;
        int typeLen = 0x01;
        int payloadLen = 3 + textBytes.length;
        byte[] record = new byte[4 + payloadLen];
        record[0] = (byte)tnfByte;
        record[1] = (byte)typeLen;
        record[2] = (byte)(payloadLen);
        record[3] = 'T';
        record[4] = 0x02;
        record[5] = 'e'; record[6] = 'n';
        System.arraycopy(textBytes, 0, record, 7, textBytes.length);
        return record;
    }

    private void loadActiveProfile() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String profileJson = prefs.getString(KEY_ACTIVE, "");
        if (!profileJson.isEmpty()) {
            HceCardProfile profile = HceCardProfile.fromStorageString(profileJson);
            if (profile != null) {
                updateNdefMessage(profile.getUid(), profile.getName());
            }
        }
    }

    private void saveNdefMessage() {
    }

    private static byte[] hexToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte)((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static void setActiveProfile(android.content.Context context, HceCardProfile profile) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (profile != null) {
            prefs.edit().putString(KEY_ACTIVE, profile.toStorageString()).apply();
        } else {
            prefs.edit().remove(KEY_ACTIVE).apply();
        }
    }

    public static HceCardProfile getActiveProfile(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String s = prefs.getString(KEY_ACTIVE, "");
        if (s.isEmpty()) return null;
        return HceCardProfile.fromStorageString(s);
    }
}
