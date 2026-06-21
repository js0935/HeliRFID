package com.helirfid;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;

import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

public class RelayHceService extends HostApduService {

    private static final LinkedBlockingQueue<byte[]> pendingResponses = new LinkedBlockingQueue<>();
    private static final byte[] SW_UNKNOWN = {(byte)0x6A, (byte)0x82};

    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        // SELECT AID
        if (commandApdu.length >= 7 && commandApdu[0] == 0x00 && commandApdu[1] == (byte)0xA4
                && commandApdu[2] == 0x04 && commandApdu[3] == 0x00) {
            return new byte[]{(byte)0x90, 0x00};
        }

        // Forward to relay client via NfcRelayActivity
        // We use a static entry point since the service runs independently
        RelayClientBridge.enqueueApdu(commandApdu);

        // Wait for response (with timeout)
        try {
            byte[] resp = pendingResponses.take();
            return resp;
        } catch (InterruptedException e) {
            return SW_UNKNOWN;
        }
    }

    @Override
    public void onDeactivated(int reason) {
        // Connection lost
    }

    public static void deliverResponse(byte[] response) {
        pendingResponses.offer(response);
    }
}
