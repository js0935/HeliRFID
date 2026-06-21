package com.helirfid;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Bridge between RelayHceService and NfcRelayActivity's client thread.
 * Uses a static queue to deliver APDUs from HCE service to the relay client.
 */
public class RelayClientBridge {

    private static final LinkedBlockingQueue<byte[]> apduQueue = new LinkedBlockingQueue<>();
    private static volatile boolean active = false;

    public static void setActive(boolean a) {
        active = a;
        if (!a) apduQueue.clear();
    }

    public static boolean isActive() {
        return active;
    }

    public static void enqueueApdu(byte[] apdu) {
        if (active) apduQueue.offer(apdu);
    }

    public static byte[] pollApdu() {
        try {
            return apduQueue.take();
        } catch (InterruptedException e) {
            return null;
        }
    }

    public static byte[] pollApduNonBlocking() {
        return apduQueue.poll();
    }
}
