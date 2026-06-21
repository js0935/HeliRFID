package com.helirfid;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * PCAPNG writer for NFC traffic export, compatible with Wireshark.
 * Uses LINKTYPE_USER0 (147) for raw NFC tag data.
 */
public class PcapngWriter {

    private ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private int interfaceId = 0;
    private long startTimeNs;
    private int packetCount = 0;

    // Block types
    private static final int BLOCK_SHB = 0x0A0D0D0A;
    private static final int BLOCK_IDB = 0x00000001;
    private static final int BLOCK_EPB = 0x00000006;

    // Options
    private static final int OPT_SHB_HARDWARE = 0x0002;
    private static final int OPT_SHB_OS = 0x0003;
    private static final int OPT_SHB_USERAPPL = 0x0004;
    private static final int OPT_IDB_NAME = 0x0002;

    public PcapngWriter(String appName) {
        startTimeNs = System.nanoTime();
        writeSectionHeader(appName);
        writeInterfaceDescription();
    }

    private void writeSectionHeader(String app) {
        ByteBuffer bb = ByteBuffer.allocate(1024).order(ByteOrder.LITTLE_ENDIAN);

        bb.putInt(BLOCK_SHB);                    // Block Type
        int posLen = bb.position();
        bb.putInt(0);                             // Placeholder for length
        bb.putInt(0x1A2B3C4D);                   // Byte Order Magic
        bb.putShort((short) 1);                   // Major Version
        bb.putShort((short) 0);                   // Minor Version
        bb.putLong(-1L);                          // Section Length (unknown)

        // Options: User Application
        byte[] appBytes = (app != null ? app : "HeliRFID").getBytes();
        writeOption(bb, OPT_SHB_USERAPPL, appBytes);

        // OS
        writeOption(bb, OPT_SHB_OS, System.getProperty("os.name", "Android").getBytes());

        // End of options
        bb.putInt(0);
        bb.putInt(0);

        int len = bb.position();
        bb.putInt(posLen, len);                   // Block Total Length (start)
        bb.putInt(len);                            // Block Total Length (end)
        writeToBaos(bb, len);
    }

    private void writeInterfaceDescription() {
        ByteBuffer bb = ByteBuffer.allocate(256).order(ByteOrder.LITTLE_ENDIAN);

        bb.putInt(BLOCK_IDB);
        int posLen = bb.position();
        bb.putInt(0);                              // Placeholder
        bb.putShort((short) 147);                  // LINKTYPE_USER0 (raw NFC)
        bb.putShort((short) 0);                    // Reserved
        bb.putInt(0xFFFF);                         // SnapLen (max)

        // Name option
        writeOption(bb, OPT_IDB_NAME, "NFC Tag Interface".getBytes());

        bb.putInt(0);
        bb.putInt(0);

        int len = bb.position();
        bb.putInt(posLen, len);
        bb.putInt(len);
        writeToBaos(bb, len);
    }

    public void addPacket(byte[] data, String direction, long timestampNs) {
        if (data == null) return;
        packetCount++;

        // Build packet data with direction prefix
        byte dirByte = (byte) ("READER_TO_TAG".equals(direction) ? 0x00 : 0x01);
        ByteArrayOutputStream packetData = new ByteArrayOutputStream();
        try {
            packetData.write(dirByte);
            packetData.write(data);
        } catch (IOException ignored) {}

        byte[] pktBytes = packetData.toByteArray();
        long ts = timestampNs > 0 ? timestampNs : System.nanoTime() - startTimeNs;

        ByteBuffer bb = ByteBuffer.allocate(128 + pktBytes.length + 4)
                .order(ByteOrder.LITTLE_ENDIAN);

        bb.putInt(BLOCK_EPB);
        int posLen = bb.position();
        bb.putInt(0);
        bb.putInt(interfaceId);
        bb.putInt((int) (ts / 1000000000L));       // Timestamp High (seconds)
        bb.putInt((int) (ts % 1000000000L));       // Timestamp Low (nanoseconds)
        bb.putInt(pktBytes.length);                // Captured length
        bb.putInt(pktBytes.length);                // Original length
        bb.put(pktBytes);                          // Packet data

        // Padding to 4 bytes
        int pad = (4 - (pktBytes.length % 4)) % 4;
        for (int i = 0; i < pad; i++) bb.put((byte) 0);

        // End of options
        bb.putInt(0);
        bb.putInt(0);

        int len = bb.position();
        bb.putInt(posLen, len);
        bb.putInt(len);
        writeToBaos(bb, len);
    }

    public void addPacket(byte[] data, String direction) {
        addPacket(data, direction, 0);
    }

    public int getPacketCount() { return packetCount; }

    public byte[] toByteArray() { return baos.toByteArray(); }

    public void save(File file) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(baos.toByteArray());
        }
    }

    private void writeOption(ByteBuffer bb, int code, byte[] value) {
        bb.putShort((short) code);
        bb.putShort((short) value.length);
        bb.put(value);
        // Pad to 4 bytes
        int pad = (4 - (value.length % 4)) % 4;
        for (int i = 0; i < pad; i++) bb.put((byte) 0);
    }

    private void writeToBaos(ByteBuffer bb, int len) {
        byte[] bytes = new byte[len];
        bb.flip();
        bb.get(bytes, 0, len);
        try { baos.write(bytes); } catch (IOException ignored) {}
    }
}
