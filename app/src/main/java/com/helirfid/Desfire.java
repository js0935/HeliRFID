package com.helirfid;

import android.nfc.tech.IsoDep;

import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Desfire {

    private IsoDep isoDep;
    private boolean sessionOpen = false;
    private byte[] sessionKey;

    private static final byte[] DESFIRE_AID = {(byte)0xD2, (byte)0x76, 0x00, 0x00, (byte)0x85, 0x01, 0x01};

    public Desfire(IsoDep isoDep) {
        this.isoDep = isoDep;
    }

    public static boolean isDesfire(IsoDep isoDep) {
        try {
            byte[] resp = isoDep.transceive(new byte[]{
                    0x00, (byte)0xA4, 0x04, 0x00, 0x07,
                    DESFIRE_AID[0], DESFIRE_AID[1], DESFIRE_AID[2],
                    DESFIRE_AID[3], DESFIRE_AID[4], DESFIRE_AID[5], DESFIRE_AID[6], 0x00
            });
            return resp != null && resp.length >= 2
                    && resp[resp.length - 2] == (byte)0x90
                    && resp[resp.length - 1] == 0x00;
        } catch (Exception e) {
            return false;
        }
    }

    public DesfireVersion getVersion() throws Exception {
        byte[] resp = send(new byte[]{(byte)0x90, 0x60, 0x00, 0x00, 0x00});
        if (resp == null || resp.length < 8) return null;
        DesfireVersion v = new DesfireVersion();
        int idx = 0;
        v.hardwareVendor = resp[idx++];
        v.hardwareType = resp[idx++];
        v.hardwareSubtype = resp[idx++];
        v.hardwareMajor = resp[idx++];
        v.hardwareMinor = resp[idx++];
        v.hardwareStorage = resp[idx++];
        v.softwareVendor = resp[idx++];
        v.softwareType = resp[idx++];
        v.softwareSubtype = resp[idx++];
        v.softwareMajor = resp[idx++];
        v.softwareMinor = resp[idx++];
        v.softwareStorage = resp[idx++];
        if (resp.length > 14) {
            v.uid = Arrays.copyOfRange(resp, 14, resp.length - 2);
        } else {
            resp = send(new byte[]{(byte)0x90, 0x51, 0x00, 0x00, 0x00});
            if (resp != null && resp.length > 2)
                v.uid = Arrays.copyOfRange(resp, 0, resp.length - 2);
        }
        v.batchNumber = 0;
        return v;
    }

    public byte[] getCardUID() throws Exception {
        byte[] resp = send(new byte[]{(byte)0x90, 0x51, 0x00, 0x00, 0x00});
        if (resp != null && resp.length >= 2
                && resp[resp.length - 2] == (byte)0x91
                && resp[resp.length - 1] == 0x00) {
            return Arrays.copyOf(resp, resp.length - 2);
        }
        return null;
    }

    public int[] getApplicationIDs() throws Exception {
        byte[] resp = send(new byte[]{(byte)0x90, 0x6A, 0x00, 0x00, 0x00});
        if (resp == null || resp.length < 3) return new int[0];
        int dataLen = resp.length - 2;
        int count = dataLen / 3;
        int[] aids = new int[count];
        for (int i = 0; i < count; i++) {
            aids[i] = ((resp[i * 3] & 0xFF) << 16)
                    | ((resp[i * 3 + 1] & 0xFF) << 8)
                    | (resp[i * 3 + 2] & 0xFF);
        }
        return aids;
    }

    public boolean selectApplication(int aid) throws Exception {
        byte[] resp = send(new byte[]{
                (byte)0x90, 0x5A, 0x00, 0x00, 0x03,
                (byte)((aid >> 16) & 0xFF),
                (byte)((aid >> 8) & 0xFF),
                (byte)(aid & 0xFF), 0x00
        });
        return resp != null && resp.length >= 2
                && resp[resp.length - 2] == 0x91 && resp[resp.length - 1] == 0x00;
    }

    public boolean selectPICC() throws Exception {
        return selectApplication(0x000000);
    }

    public int[] getFileIDs() throws Exception {
        byte[] resp = send(new byte[]{(byte)0x90, 0x6F, 0x00, 0x00, 0x00});
        if (resp == null || resp.length < 3) return new int[0];
        int count = resp.length - 2;
        int[] fids = new int[count];
        for (int i = 0; i < count; i++) fids[i] = resp[i] & 0xFF;
        return fids;
    }

    public DesfireFileSettings getFileSettings(int fileNo) throws Exception {
        byte[] resp = send(new byte[]{
                (byte)0x90, (byte)0xF5, 0x00, 0x00, 0x01,
                (byte)(fileNo & 0xFF), 0x00
        });
        if (resp == null || resp.length < 5) return null;
        DesfireFileSettings fs = new DesfireFileSettings();
        fs.fileType = resp[0] & 0xFF;
        fs.commMode = resp[1] & 0xFF;
        fs.readKey = resp[2] & 0xFF;
        fs.writeKey = resp[3] & 0xFF;
        fs.readWriteKey = resp[4] & 0xFF;
        if (fs.fileType == 0x00) {
            if (resp.length >= 9) {
                fs.fileSize = ((resp[5] & 0xFF) << 24) | ((resp[6] & 0xFF) << 16)
                        | ((resp[7] & 0xFF) << 8) | (resp[8] & 0xFF);
            }
        }
        return fs;
    }

    public byte[] readDataPlain(int fileNo, int offset, int length) throws Exception {
        byte[] resp = send(new byte[]{
                (byte)0x90, (byte)0xBD, 0x00, 0x00, 0x07,
                (byte)(fileNo & 0xFF),
                (byte)(offset & 0xFF), (byte)((offset >> 8) & 0xFF), (byte)((offset >> 16) & 0xFF),
                (byte)(length & 0xFF), (byte)((length >> 8) & 0xFF),
                0x00
        });
        if (resp != null && resp.length >= 2
                && resp[resp.length - 2] == 0x91 && resp[resp.length - 1] == 0x00) {
            return Arrays.copyOf(resp, resp.length - 2);
        }
        return null;
    }

    public int getKeySettings() throws Exception {
        byte[] resp = send(new byte[]{(byte)0x90, 0x45, 0x00, 0x00, 0x00});
        if (resp != null && resp.length >= 2) {
            return (resp[resp.length - 4] & 0xFF) | ((resp[resp.length - 3] & 0xFF) << 8);
        }
        return -1;
    }

    public boolean authenticateLegacy(byte[] key, int keyNo) throws Exception {
        byte[] resp = send(new byte[]{
                (byte)0x90, 0x0A, 0x00, 0x00, 0x01,
                (byte)(keyNo & 0xFF), 0x00
        });
        if (resp == null || resp.length < 3) return false;
        byte[] encRndB = Arrays.copyOf(resp, resp.length - 2);
        byte[] decRndB = desDecrypt(encRndB, key);
        byte[] rndB = new byte[8];
        System.arraycopy(decRndB, 0, rndB, 0, 8);
        byte[] rndB_rot = rotateLeft(rndB);
        byte[] encRndB_rot = desEncrypt(rndB_rot, key);
        byte[] rndA = desDecrypt(encRndB_rot, key);
        resp = send(concat(new byte[]{(byte)0x90, (byte)0xAF, 0x00, 0x00, (byte)encRndB_rot.length}, encRndB_rot));
        if (resp == null || resp.length < 3) return false;
        sessionKey = new byte[8];
        System.arraycopy(deriveSessionKey(key, rndA, rndB), 0, sessionKey, 0, 8);
        return true;
    }

    public byte[] createApplication(int aid, int keySettings, int numKeys) throws Exception {
        return send(new byte[]{
                (byte)0x90, (byte)0xCA, 0x00, 0x00, 0x07,
                (byte)((aid >> 16) & 0xFF),
                (byte)((aid >> 8) & 0xFF),
                (byte)(aid & 0xFF),
                (byte)(keySettings & 0xFF),
                (byte)((keySettings >> 8) & 0xFF),
                (byte)(numKeys & 0xFF),
                0x00
        });
    }

    public byte[] createStdDataFile(int fileNo, int commMode, int accessRights, int fileSize) throws Exception {
        return send(new byte[]{
                (byte)0x90, (byte)0xCD, 0x00, 0x00, 0x07,
                (byte)(fileNo & 0xFF),
                (byte)(commMode & 0xFF),
                (byte)(accessRights & 0xFF),
                (byte)((accessRights >> 8) & 0xFF),
                (byte)(fileSize & 0xFF),
                (byte)((fileSize >> 8) & 0xFF),
                0x00
        });
    }

    public byte[] deleteFile(int fileNo) throws Exception {
        return send(new byte[]{
                (byte)0x90, (byte)0xDF, 0x00, 0x00, 0x01,
                (byte)(fileNo & 0xFF), 0x00
        });
    }

    private byte[] send(byte[] cmd) throws Exception {
        if (isoDep == null) return null;
        return isoDep.transceive(cmd);
    }

    private byte[] desDecrypt(byte[] data, byte[] key) throws Exception {
        Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
        SecretKeySpec ks = new SecretKeySpec(key, "DES");
        cipher.init(Cipher.DECRYPT_MODE, ks);
        return cipher.doFinal(data);
    }

    private byte[] desEncrypt(byte[] data, byte[] key) throws Exception {
        Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
        SecretKeySpec ks = new SecretKeySpec(key, "DES");
        cipher.init(Cipher.ENCRYPT_MODE, ks);
        return cipher.doFinal(data);
    }

    private byte[] rotateLeft(byte[] data) {
        byte[] result = new byte[data.length];
        System.arraycopy(data, 1, result, 0, data.length - 1);
        result[data.length - 1] = data[0];
        return result;
    }

    private byte[] deriveSessionKey(byte[] key, byte[] rndA, byte[] rndB) {
        byte[] sk = new byte[8];
        for (int i = 0; i < 8; i++) sk[i] = (byte)(rndA[i] ^ rndB[i]);
        return sk;
    }

    private byte[] concat(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    public static class DesfireVersion {
        public int hardwareVendor, hardwareType, hardwareSubtype;
        public int hardwareMajor, hardwareMinor, hardwareStorage;
        public int softwareVendor, softwareType, softwareSubtype;
        public int softwareMajor, softwareMinor, softwareStorage;
        public byte[] uid;
        public long batchNumber;

        public String getHardwareName() {
            return majorMinor(hardwareMajor, hardwareMinor);
        }

        public String getSoftwareName() {
            return majorMinor(softwareMajor, softwareMinor);
        }

        private String majorMinor(int mj, int mn) {
            return mj + "." + mn;
        }

        public String getHardwareTypeName() {
            switch (hardwareType) {
                case 0x01: return "DESFire EV1";
                case 0x02: return "DESFire EV2";
                case 0x03: return "DESFire EV3";
                default: return "DESFire 0x" + String.format("%02X", hardwareType);
            }
        }

        public String getStorageName() {
            int gb = (hardwareStorage >> 4) & 0x0F;
            int g = hardwareStorage & 0x0F;
            String[] sizes = {"", "2K", "4K", "8K", "16K", "32K", "64K", "128K", "256K"};
            if (g >= 0 && g < sizes.length) return sizes[g] + (gb > 0 ? " (GB=" + gb + ")" : "");
            return "0x" + String.format("%02X", hardwareStorage);
        }

        public String getUidString() {
            if (uid == null) return "N/A";
            StringBuilder sb = new StringBuilder();
            for (byte b : uid) sb.append(String.format("%02X", b));
            return sb.toString();
        }
    }

    public static class DesfireFileSettings {
        public int fileType, commMode, readKey, writeKey, readWriteKey;
        public int fileSize;

        public String getFileTypeName() {
            switch (fileType) {
                case 0x00: return "Standard Data File";
                case 0x01: return "Backup Data File";
                case 0x02: return "Value File";
                case 0x03: return "Linear Record File";
                case 0x04: return "Cyclic Record File";
                default: return "Unknown (0x" + String.format("%02X", fileType) + ")";
            }
        }

        public String getCommModeName() {
            switch (commMode) {
                case 0x00: return "Plain";
                case 0x01: return "MACed";
                case 0x03: return "Full Enciphered";
                default: return "0x" + String.format("%02X", commMode);
            }
        }
    }
}
