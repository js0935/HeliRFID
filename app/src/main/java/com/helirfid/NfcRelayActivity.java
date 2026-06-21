package com.helirfid;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

public class NfcRelayActivity extends BaseNfcActivity {

    private EditText editIp, editPort;
    private TextView txtStatus, txtLog;
    private Button btnServer, btnReader, btnTag, btnStop;

    private ServerThread serverThread;
    private ClientThread clientThread;
    private Tag currentTag;
    private boolean isReaderMode, isConnected;

    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_relay);

        editIp = findViewById(R.id.editRelayIp);
        editPort = findViewById(R.id.editRelayPort);
        txtStatus = findViewById(R.id.txtRelayStatus);
        txtLog = findViewById(R.id.txtRelayLog);
        btnServer = findViewById(R.id.btnRelayServer);
        btnReader = findViewById(R.id.btnRelayReader);
        btnTag = findViewById(R.id.btnRelayTag);
        btnStop = findViewById(R.id.btnRelayStop);

        editPort.setText(String.valueOf(RelayConstants.DEFAULT_PORT));

        btnServer.setOnClickListener(v -> startServer());
        btnReader.setOnClickListener(v -> connectAsReader());
        btnTag.setOnClickListener(v -> connectAsTag());
        btnStop.setOnClickListener(v -> stopAll());

        setStatus("就緒");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) return;
        currentTag = tag;

        String uid = Converter.hex(tag.getId());
        addLog("偵測到卡片 UID: " + uid);

        if (isReaderMode && clientThread != null && clientThread.isAlive()) {
            addLog("Reader 模式: 開始中繼卡片資料...");
            new Thread(() -> relayCard(tag)).start();
        }
    }

    private void startServer() {
        stopAll();
        int port = getPort();
        if (port < 0) return;

        serverThread = new ServerThread(port);
        serverThread.start();
        setStatus("伺服器模式 — 監聽中 port " + port);
        addLog("伺服器啟動，等待連線...");
    }

    private void connectAsReader() {
        String ip = editIp.getText().toString().trim();
        if (TextUtils.isEmpty(ip)) {
            Toast.makeText(this, "請輸入伺服器 IP", Toast.LENGTH_SHORT).show();
            return;
        }
        int port = getPort();
        if (port < 0) return;

        stopAll();
        isReaderMode = true;
        clientThread = new ClientThread(ip, port, true);
        clientThread.start();
        setStatus("Reader 模式 — 等待卡片...");
        addLog("Reader 模式: 請將手機靠近真實卡片");
    }

    private void connectAsTag() {
        String ip = editIp.getText().toString().trim();
        if (TextUtils.isEmpty(ip)) {
            Toast.makeText(this, "請輸入伺服器 IP", Toast.LENGTH_SHORT).show();
            return;
        }
        int port = getPort();
        if (port < 0) return;

        stopAll();
        isReaderMode = false;
        clientThread = new ClientThread(ip, port, false);
        clientThread.start();
        setStatus("標籤模式 — HCE 模擬中...");
        addLog("Tag 模式: 請用外部讀卡器靠近本手機");
    }

    private void stopAll() {
        if (serverThread != null) { serverThread.interrupt(); serverThread = null; }
        if (clientThread != null) { clientThread.interrupt(); clientThread = null; }
        isConnected = false;
        isReaderMode = false;
        setStatus("已停止");
        addLog("已停止所有服務");
    }

    private void relayCard(Tag tag) {
        try {
            IsoDep isoDep = IsoDep.get(tag);
            if (isoDep == null) {
                addLog("錯誤: 不支援 ISO-DEP (需 MIFARE DESFire 或 ISO 14443-4)");
                return;
            }
            isoDep.connect();
            addLog("ISO-DEP 已連線，等待中繼指令...");

            while (!Thread.currentThread().isInterrupted() && isConnected) {
                // Wait for APDU from network, poll the client thread's incoming queue
                byte[] apdu = clientThread != null ? clientThread.pollApdu() : null;
                if (apdu == null) {
                    Thread.sleep(50);
                    continue;
                }
                addLog("→ 收到 APDU: " + Converter.hex(apdu));
                byte[] response = isoDep.transceive(apdu);
                addLog("← 卡片回應: " + Converter.hex(response));
                if (clientThread != null) clientThread.sendResponse(response);
            }
            isoDep.close();
        } catch (Exception e) {
            addLog("中繼錯誤: " + e.getMessage());
        }
    }

    private int getPort() {
        try {
            return Integer.parseInt(editPort.getText().toString().trim());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "請輸入有效埠號", Toast.LENGTH_SHORT).show();
            return -1;
        }
    }

    private void setStatus(String s) {
        uiHandler.post(() -> txtStatus.setText("狀態: " + s));
    }

    private void addLog(String s) {
        uiHandler.post(() -> {
            String cur = txtLog.getText().toString();
            txtLog.setText(cur + "\n" + s);
            final int scroll = Math.max(0, txtLog.getLineCount() - 20);
            txtLog.setScrollY(scroll);
        });
    }

    // ─── TCP Server ────────────────
    private class ServerThread extends Thread {
        private final int port;
        ServerThread(int port) { this.port = port; setName("RelayServer"); }

        @Override
        public void run() {
            try (ServerSocket ss = new ServerSocket(port)) {
                setStatus("等待 Reader 連線...");
                Socket reader = ss.accept();
                addLog("Reader 已連線: " + reader.getInetAddress());
                setStatus("等待 Tag 連線...");
                Socket tag = ss.accept();
                addLog("Tag 已連線: " + tag.getInetAddress());
                isConnected = true;
                setStatus("中繼連線中 (Reader ↔ Tag)");
                pipe(reader, tag);
            } catch (IOException e) {
                if (!isInterrupted()) addLog("伺服器錯誤: " + e.getMessage());
            }
        }

        private void pipe(Socket r, Socket t) {
            try {
                DataInputStream rIn = new DataInputStream(r.getInputStream());
                DataOutputStream rOut = new DataOutputStream(r.getOutputStream());
                DataInputStream tIn = new DataInputStream(t.getInputStream());
                DataOutputStream tOut = new DataOutputStream(t.getOutputStream());
                r.setSoTimeout(60000); t.setSoTimeout(60000);
                while (!isInterrupted()) {
                    // Read from reader
                    int len = rIn.readInt();
                    if (len <= 0) break;
                    byte[] data = new byte[len];
                    rIn.readFully(data);
                    tOut.writeInt(len);
                    tOut.write(data);
                    tOut.flush();

                    // Read from tag
                    len = tIn.readInt();
                    if (len <= 0) break;
                    data = new byte[len];
                    tIn.readFully(data);
                    rOut.writeInt(len);
                    rOut.write(data);
                    rOut.flush();
                }
            } catch (IOException e) {
                addLog("中繼管道中斷: " + e.getMessage());
            }
        }
    }

    // ─── TCP Client ────────────────
    private class ClientThread extends Thread {
        private final String host;
        private final int port;
        private final boolean isReader;
        private DataOutputStream out;
        private final LinkedBlockingQueue<byte[]> incomingApdus = new LinkedBlockingQueue<>();

        ClientThread(String host, int port, boolean isReader) {
            this.host = host; this.port = port; this.isReader = isReader;
            setName("RelayClient");
        }

        @Override
        public void run() {
            try (Socket s = new Socket(host, port)) {
                out = new DataOutputStream(s.getOutputStream());
                DataInputStream in = new DataInputStream(s.getInputStream());
                isConnected = true;
                addLog("已連線到伺服器 " + host + ":" + port);

                if (isReader) {
                    // Reader mode: wait for APDU from network → send to real card
                    setStatus("Reader 已連線，等待卡片中...");
                    while (!isInterrupted() && isConnected) {
                        int len = in.readInt();
                        if (len <= 0) break;
                        byte[] apdu = new byte[len];
                        in.readFully(apdu);
                        incomingApdus.offer(apdu);
                    }
                } else {
                    // Tag mode: forward HCE APDUs to network → wait for response
                    RelayClientBridge.setActive(true);
                    setStatus("Tag 已連線，HCE 模擬中...");
                    try {
                        while (!isInterrupted() && isConnected) {
                            byte[] apdu = RelayClientBridge.pollApdu();
                            if (apdu == null) break;
                            // Send to reader via server
                            out.writeInt(apdu.length);
                            out.write(apdu);
                            out.flush();
                            // Wait for response
                            int len = in.readInt();
                            if (len <= 0) break;
                            byte[] resp = new byte[len];
                            in.readFully(resp);
                            RelayHceService.deliverResponse(resp);
                        }
                    } finally {
                        RelayClientBridge.setActive(false);
                    }
                }
            } catch (Exception e) {
                if (!isInterrupted()) addLog("連線錯誤: " + e.getMessage());
            }
            isConnected = false;
        }

        byte[] pollApdu() { return incomingApdus.poll(); }

        void sendResponse(byte[] data) {
            try {
                if (out != null) {
                    out.writeInt(data.length);
                    out.write(data);
                    out.flush();
                }
            } catch (IOException e) {
                addLog("發送失敗: " + e.getMessage());
            }
        }

        void sendApdu(byte[] apdu) {
            incomingApdus.offer(apdu);
        }
    }
}
