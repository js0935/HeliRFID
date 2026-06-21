package com.helirfid;

import android.nfc.tech.IsoDep;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Ntag424 {

    private final IsoDep isoDep;
    private byte[] sessionKey;
    private boolean authenticated;

    private static final byte[] DEFAULT_KEY = new byte[16];
    private static final byte[] ZIP = new byte[16];
    private static final SecureRandom RNG = new SecureRandom();

    public Ntag424(IsoDep isoDep) {
        this.isoDep = isoDep;
        this.authenticated = false;
    }

    private static byte[] aes(byte[] key, byte[] data, int mode) {
        try {
            Cipher c = Cipher.getInstance("AES/ECB/NoPadding");
            c.init(mode, new SecretKeySpec(key, "AES"));
            return c.doFinal(data);
        } catch (Exception e) {
            return null;
        }
    }

    private static byte[] aesEncrypt(byte[] key, byte[] data) {
        return aes(key, data, Cipher.ENCRYPT_MODE);
    }

    private static byte[] aesDecrypt(byte[] key, byte[] data) {
        return aes(key, data, Cipher.DECRYPT_MODE);
    }

    private static byte[] xor(byte[] a, byte[] b) {
        byte[] r = new byte[Math.min(a.length, b.length)];
        for (int i = 0; i < r.length; i++) r[i] = (byte)(a[i] ^ b[i]);
        return r;
    }

    private static byte[] subBytes(byte[] src, int off, int len) {
        byte[] r = new byte[len];
        System.arraycopy(src, off, r, 0, len);
        return r;
    }

    private static byte[] concat(byte[]... parts) {
        int t = 0;
        for (byte[] p : parts) t += p.length;
        byte[] r = new byte[t];
        int p = 0;
        for (byte[] b : parts) { System.arraycopy(b, 0, r, p, b.length); p += b.length; }
        return r;
    }

    private static byte[] rotLeft(byte[] data) {
        if (data.length < 2) return data;
        byte[] r = new byte[data.length];
        System.arraycopy(data, 1, r, 0, data.length - 1);
        r[data.length - 1] = data[0];
        return r;
    }

    private static byte[] cmacSubkey(byte[] key) {
        byte[] zero = new byte[16];
        byte[] L = aesEncrypt(key, zero);
        byte[] k1 = dbl(L);
        byte[] k2 = dbl(k1);
        return concat(k1, k2);
    }

    private static byte[] dbl(byte[] block) {
        byte[] r = new byte[16];
        boolean msb = (block[0] & 0x80) != 0;
        for (int i = 0; i < 15; i++) r[i] = (byte)((block[i] << 1) | ((block[i + 1] >> 7) & 1));
        r[15] = (byte)(block[15] << 1);
        if (msb) r[15] ^= 0x87;
        return r;
    }

    public static byte[] computeCmac(byte[] key, byte[] data) {
        byte[] subkeys = cmacSubkey(key);
        byte[] k1 = subBytes(subkeys, 0, 16);
        byte[] k2 = subBytes(subkeys, 16, 16);
        int n = (data.length + 15) / 16;
        if (n == 0) n = 1;
        byte[] last;
        if (data.length % 16 == 0 && data.length > 0) {
            last = xor(subBytes(data, (n - 1) * 16, 16), k1);
        } else {
            byte[] padded = new byte[(n) * 16];
            System.arraycopy(data, 0, padded, 0, data.length);
            padded[data.length] = (byte)0x80;
            last = xor(subBytes(padded, (n - 1) * 16, 16), k2);
        }
        byte[] x = new byte[16];
        for (int i = 0; i < n - 1; i++) {
            byte[] block = subBytes(data, i * 16, 16);
            x = aesEncrypt(key, xor(x, block));
        }
        x = aesEncrypt(key, xor(x, last));
        return x;
    }

    private byte[] transceive(byte[] cmd) throws IOException {
        return isoDep.transceive(cmd);
    }

    public static int getSw(byte[] resp) {
        if (resp == null || resp.length < 2) return -1;
        return ((resp[resp.length - 2] & 0xFF) << 8) | (resp[resp.length - 1] & 0xFF);
    }

    public static byte[] getData(byte[] resp) {
        if (resp == null || resp.length <= 2) return new byte[0];
        return subBytes(resp, 0, resp.length - 2);
    }

    public static boolean isSuccess(byte[] resp) {
        return getSw(resp) == 0x9100;
    }

    public byte[] getVersion() throws IOException {
        return transceive(new byte[]{(byte)0x90, 0x60, 0x00, 0x00, 0x00});
    }

    public byte[] getChallenge() throws IOException {
        return transceive(new byte[]{(byte)0x90, (byte)0xAA, 0x00, 0x00, 0x00});
    }

    public byte[] getUid() throws IOException {
        byte[] cmd = {(byte)0x90, 0x51, 0x00, 0x00, 0x00};
        return transceive(cmd);
    }

    public boolean authenticate(byte[] key) throws IOException {
        byte[] chResp = getChallenge();
        if (!isSuccess(chResp)) return false;
        byte[] rndB = getData(chResp);
        if (rndB.length < 16) return false;
        byte[] rndB16 = subBytes(rndB, 0, 16);
        byte[] rndA = new byte[16];
        RNG.nextBytes(rndA);
        byte[] rndBPrime = rotLeft(rndB16);
        byte[] pcdData = concat(rndA, rndBPrime);
        byte[] encData = aesEncrypt(key, pcdData);
        byte[] ti = subBytes(rndB16, 0, 4);
        byte[] cmd1 = concat(new byte[]{(byte)0x90, 0x71, 0x00, 0x00, (byte)0x2D, 0x00, 0x00, 0x00, 0x00, 0x00}, ti, encData);
        cmd1[cmd1.length - 1] = (byte)0x00;
        byte[] resp1 = transceive(cmd1);
        if (!isSuccess(resp1)) return false;
        byte[] encResp = getData(resp1);
        if (encResp.length < 32) return false;
        byte[] decResp = aesDecrypt(key, subBytes(encResp, 0, 32));
        byte[] rndAPrime = subBytes(decResp, 0, 16);
        byte[] rndBPrimePrime = subBytes(decResp, 16, 16);
        byte[] rndARot = rotLeft(rndA);
        if (!Arrays.equals(rndAPrime, rndARot)) return false;
        byte[] tiNew = subBytes(encResp, 0, 4);
        byte[] rndBPrimePrimeRot = rotLeft(rndBPrimePrime);
        byte[] cmd2 = concat(new byte[]{(byte)0x90, (byte)0xAF, 0x00, 0x00, 0x15, 0x00, 0x00, 0x00, 0x00, 0x00}, tiNew, rndBPrimePrimeRot);
        byte[] pad = new byte[16];
        cmd2 = concat(cmd2, pad);
        byte[] sv = subBytes(encResp, 32, 16);
        cmd2 = concat(cmd2, sv);
        byte[] resp2 = transceive(cmd2);
        if (!isSuccess(resp2)) return false;
        byte[] encResp2 = getData(resp2);
        if (encResp2.length < 32) return false;
        byte[] decResp2 = aesDecrypt(key, subBytes(encResp2, 0, 32));
        sessionKey = subBytes(decResp2, 0, 16);
        authenticated = true;
        return true;
    }

    public boolean authenticateDefault() throws IOException {
        return authenticate(DEFAULT_KEY);
    }

    public byte[] readData(int fileNo, int offset, int length) throws IOException {
        byte[] cmd = new byte[11];
        cmd[0] = (byte)0x90; cmd[1] = (byte)0xBB; cmd[2] = 0x00; cmd[3] = 0x00; cmd[4] = 0x07;
        cmd[5] = (byte)((fileNo >> 24) & 0xFF); cmd[6] = (byte)((fileNo >> 16) & 0xFF);
        cmd[7] = (byte)((fileNo >> 8) & 0xFF); cmd[8] = (byte)(fileNo & 0xFF);
        cmd[9] = (byte)((offset >> 16) & 0xFF); cmd[10] = (byte)((offset >> 8) & 0xFF);
        return transceive(cmd);
    }

    public byte[] writeData(int fileNo, int offset, byte[] data) throws IOException {
        int len = data.length;
        byte[] cmd = new byte[11 + len];
        cmd[0] = (byte)0x90; cmd[1] = (byte)0xBC; cmd[2] = 0x00; cmd[3] = 0x00;
        cmd[4] = (byte)(7 + len);
        cmd[5] = (byte)((fileNo >> 24) & 0xFF); cmd[6] = (byte)((fileNo >> 16) & 0xFF);
        cmd[7] = (byte)((fileNo >> 8) & 0xFF); cmd[8] = (byte)(fileNo & 0xFF);
        cmd[9] = (byte)((offset >> 16) & 0xFF); cmd[10] = (byte)((offset >> 8) & 0xFF);
        System.arraycopy(data, 0, cmd, 11, len);
        return transceive(cmd);
    }

    public byte[] getSdmMeta(byte[] uid) throws IOException {
        byte[] cmd = {(byte)0x90, 0x5C, 0x00, 0x00, 0x03, 0x01, 0x00, 0x00};
        return transceive(cmd);
    }

    public byte[] getFileIds() throws IOException {
        byte[] cmd = {(byte)0x90, 0x3D, 0x00, 0x00, 0x00};
        return transceive(cmd);
    }

    public String getVersionString() throws IOException {
        byte[] resp = getVersion();
        if (!isSuccess(resp)) return "取得版本失敗 (SW=" + String.format("%04X", getSw(resp)) + ")";
        byte[] d = getData(resp);
        if (d.length < 8) return "回覆長度不足";
        int hwRev = d[2] & 0xFF;
        int swRev = d[3] & 0xFF;
        int storage = d[4] & 0xFF;
        int proto = d[5] & 0xFF;
        String prod = String.format("%02X%02X%02X", d[0] & 0xFF, d[1] & 0xFF, d[2] & 0xFF);
        String protoStr = (proto == 2) ? "ISO 14443-4" : "未知";
        String prodName = "NTAG 424 DNA";
        return String.format("產品: %s (%s)\n硬體版本: %d.%d\n軟體版本: %d.%d\n儲存大小: %d bytes\n協定: %s",
                prodName, prod, hwRev >> 4, hwRev & 0x0F, swRev >> 4, swRev & 0x0F, storage * 8, protoStr);
    }

    public String getInfo() {
        return "NTAG 424 DNA\nAES-128 加密認證\n支援 SUN / SDM / CMAC\n檔案系統: 最大 32 檔案\nEEPROM: 416 bytes";
    }

    public boolean isAuthenticated() { return authenticated; }
    public byte[] getSessionKey() { return sessionKey; }
}
