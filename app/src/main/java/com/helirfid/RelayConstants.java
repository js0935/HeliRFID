package com.helirfid;

public class RelayConstants {
    public static final int MSG_TYPE_APDU = 1;
    public static final int MSG_TYPE_STATUS = 2;
    public static final int MSG_TYPE_HEARTBEAT = 3;
    public static final int STATUS_READY = 0;
    public static final int STATUS_BUSY = 1;
    public static final int STATUS_ERROR = 2;
    public static final int STATUS_DISCONNECTED = 3;
    public static final String RELAY_AID = "F0010203040506";
    public static final String RELAY_AID_PATH = "F0010203040506";
    public static final int DEFAULT_PORT = 9090;
}
