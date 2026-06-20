/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.0.1
 */
package com.helirfid;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

import java.util.Arrays;

public class Acr122uManager {

    public static final int VENDOR_ID = 0x072F;
    public static final int PRODUCT_ID = 0x2200;

    private UsbDeviceConnection connection;
    private UsbEndpoint bulkOut;
    private UsbEndpoint bulkIn;
    private UsbInterface usbInterface;
    private int seq = 0;

    public boolean connect(UsbManager usbManager, UsbDevice device) {
        if (device.getVendorId() != VENDOR_ID || device.getProductId() != PRODUCT_ID)
            return false;

        usbInterface = device.getInterface(0);
        connection = usbManager.openDevice(device);
        if (connection == null) return false;

        connection.claimInterface(usbInterface, true);

        for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
            UsbEndpoint ep = usbInterface.getEndpoint(i);
            if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                if (ep.getDirection() == UsbConstants.USB_DIR_OUT)
                    bulkOut = ep;
                else if (ep.getDirection() == UsbConstants.USB_DIR_IN)
                    bulkIn = ep;
            }
        }

        byte[] firmware = getFirmwareVersion();
        return firmware != null && firmware.length > 0;
    }

    public void disconnect() {
        if (connection != null) {
            if (usbInterface != null) connection.releaseInterface(usbInterface);
            connection.close();
        }
        connection = null;
        bulkOut = null;
        bulkIn = null;
        usbInterface = null;
    }

    public boolean isConnected() {
        return connection != null;
    }

    public byte[] getFirmwareVersion() {
        byte[] cmd = {0x6F, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] resp = transceiveRaw(cmd);
        if (resp == null || resp.length < 10) return null;
        int dataLen = ((resp[4] & 0xFF) << 24) | ((resp[3] & 0xFF) << 16)
                | ((resp[2] & 0xFF) << 8) | (resp[1] & 0xFF);
        if (dataLen > 0 && resp.length >= 10 + dataLen) {
            byte[] data = new byte[dataLen];
            System.arraycopy(resp, 10, data, 0, dataLen);
            return data;
        }
        return resp;
    }

    public byte[] getUid() {
        byte[] apdu = {(byte)0xFF, (byte)0xCA, 0x00, 0x00, 0x00};
        byte[] resp = sendApdu(apdu);
        if (resp != null && resp.length >= 2
                && resp[resp.length - 2] == (byte)0x90
                && resp[resp.length - 1] == 0x00) {
            byte[] uid = new byte[resp.length - 2];
            System.arraycopy(resp, 0, uid, 0, uid.length);
            return uid;
        }
        return null;
    }

    public boolean loadKey(byte[] key) {
        byte[] apdu = new byte[11];
        apdu[0] = (byte)0xFF;
        apdu[1] = (byte)0x82;
        apdu[2] = 0x00;
        apdu[3] = 0x00;
        apdu[4] = 0x06;
        System.arraycopy(key, 0, apdu, 5, 6);
        byte[] resp = sendApdu(apdu);
        return resp != null && resp.length >= 2
                && resp[resp.length - 2] == (byte)0x90
                && resp[resp.length - 1] == 0x00;
    }

    public boolean authenticate(int block, boolean keyA) {
        byte[] apdu = {
                (byte)0xFF, (byte)0x86, 0x00, 0x00, 0x05, 0x01, 0x00,
                (byte)block, (byte)(keyA ? 0x60 : 0x61), 0x00
        };
        byte[] resp = sendApdu(apdu);
        return resp != null && resp.length >= 2
                && resp[resp.length - 2] == (byte)0x90
                && resp[resp.length - 1] == 0x00;
    }

    public byte[] readBlock(int block) {
        byte[] apdu = {(byte)0xFF, (byte)0xB0, 0x00, (byte)block, 0x10};
        byte[] resp = sendApdu(apdu);
        if (resp != null && resp.length >= 18
                && resp[resp.length - 2] == (byte)0x90
                && resp[resp.length - 1] == 0x00) {
            byte[] data = new byte[16];
            System.arraycopy(resp, 0, data, 0, 16);
            return data;
        }
        return null;
    }

    public boolean writeBlock(int block, byte[] data) {
        if (data.length != 16) return false;
        byte[] apdu = new byte[21];
        apdu[0] = (byte)0xFF;
        apdu[1] = (byte)0xD6;
        apdu[2] = 0x00;
        apdu[3] = (byte)block;
        apdu[4] = 0x10;
        System.arraycopy(data, 0, apdu, 5, 16);
        byte[] resp = sendApdu(apdu);
        return resp != null && resp.length >= 2
                && resp[resp.length - 2] == (byte)0x90
                && resp[resp.length - 1] == 0x00;
    }

    public boolean formatTag() {
        byte[] apdu = {(byte)0xFF, 0x00, 0x00, 0x00, 0x01, 0x0D};
        byte[] resp = sendApdu(apdu);
        return resp != null && resp.length >= 2
                && resp[resp.length - 2] == (byte)0x90
                && resp[resp.length - 1] == 0x00;
    }

    private byte[] sendApdu(byte[] apdu) {
        int len = apdu.length;
        byte[] ccid = new byte[10 + len];
        ccid[0] = (byte)0x6F;
        ccid[1] = (byte)(len & 0xFF);
        ccid[2] = (byte)((len >> 8) & 0xFF);
        ccid[3] = (byte)((len >> 16) & 0xFF);
        ccid[4] = (byte)((len >> 24) & 0xFF);
        ccid[5] = 0x00;
        ccid[6] = (byte)(seq++ & 0xFF);
        ccid[7] = 0x00;
        ccid[8] = 0x00;
        ccid[9] = 0x00;
        System.arraycopy(apdu, 0, ccid, 10, len);

        byte[] rawResp = transceiveRaw(ccid);
        if (rawResp == null || rawResp.length < 10) return null;

        int respLen = ((rawResp[4] & 0xFF) << 24) | ((rawResp[3] & 0xFF) << 16)
                | ((rawResp[2] & 0xFF) << 8) | (rawResp[1] & 0xFF);
        if (respLen > 0 && rawResp.length >= 10 + respLen) {
            byte[] data = new byte[respLen];
            System.arraycopy(rawResp, 10, data, 0, respLen);
            return data;
        }
        return null;
    }

    private byte[] transceiveRaw(byte[] cmd) {
        if (connection == null || bulkOut == null || bulkIn == null) return null;

        byte[] outBuf = new byte[cmd.length + 2];
        outBuf[0] = 0x00;
        outBuf[1] = 0x00;
        System.arraycopy(cmd, 0, outBuf, 2, cmd.length);

        int sent = connection.bulkTransfer(bulkOut, outBuf, outBuf.length, 1000);
        if (sent < 0) return null;

        byte[] inBuf = new byte[512];
        int received = connection.bulkTransfer(bulkIn, inBuf, inBuf.length, 1000);
        if (received < 0) return null;

        if (received > 2) {
            byte[] result = new byte[received - 2];
            System.arraycopy(inBuf, 2, result, 0, received - 2);
            return result;
        }
        return null;
    }
}
