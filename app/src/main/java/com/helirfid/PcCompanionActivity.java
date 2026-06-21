/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.3.1
 */
package com.helirfid;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Locale;

public class PcCompanionActivity extends BaseNfcActivity {

    private TextView txtStatus, txtUrl, txtLog;
    private Button btnStart, btnStop;

    private ServerSocket serverSocket;
    private Thread serverThread;
    private boolean running;
    private Tag pendingTag;
    private String lastReadResult = "尚未讀取";

    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setUseReaderMode(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pc_companion);

        txtStatus = findViewById(R.id.txtPcStatus);
        txtUrl = findViewById(R.id.txtPcUrl);
        txtLog = findViewById(R.id.txtPcLog);
        btnStart = findViewById(R.id.btnPcStart);
        btnStop = findViewById(R.id.btnPcStop);

        btnStart.setOnClickListener(v -> startServer());
        btnStop.setOnClickListener(v -> stopServer());
    }

    private void startServer() {
        running = true;
        serverThread = new Thread(this::serverLoop);
        serverThread.setDaemon(true);
        serverThread.start();

        btnStart.setEnabled(false);
        btnStop.setEnabled(true);
        txtStatus.setText("伺服器啟動中...");
    }

    private void stopServer() {
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (Exception ignored) {}
        btnStart.setEnabled(true);
        btnStop.setEnabled(false);
        txtStatus.setText("伺服器已停止");
        txtUrl.setText("");
    }

    private void serverLoop() {
        try {
            serverSocket = new ServerSocket(8080);
            String ip = getLocalIpAddress();
            final String url = "http://" + ip + ":8080";
            runOnUiThread(() -> {
                txtStatus.setText("伺服器執行中");
                txtUrl.setText(url);
                appendLog("伺服器啟動於 " + url);
            });

            while (running) {
                try {
                    Socket client = serverSocket.accept();
                    handleClient(client);
                } catch (Exception e) {
                    if (running) appendLog("連線錯誤: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            runOnUiThread(() -> {
                txtStatus.setText("啟動失敗: " + e.getMessage());
                btnStart.setEnabled(true);
                btnStop.setEnabled(false);
            });
        }
    }

    private void handleClient(Socket client) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
            String requestLine = reader.readLine();
            if (requestLine == null) return;
            appendLog("收到請求: " + requestLine);

            String path = requestLine.split(" ")[1];

            OutputStream out = client.getOutputStream();
            String response;

            if (path.startsWith("/read")) {
                if (pendingTag != null) {
                    response = readTagData(pendingTag);
                } else {
                    response = "{\"status\":\"waiting\",\"message\":\"請將卡片靠近裝置\"}";
                }
            } else if (path.startsWith("/write")) {
                response = "{\"status\":\"ok\",\"message\":\"寫入功能待實作\"}";
            } else if (path.startsWith("/list")) {
                response = "{\"status\":\"ok\",\"data\":[\"Tag 1\",\"Tag 2\"]}";
            } else {
                response = getHtmlPage();
            }

            byte[] respBytes = response.getBytes(StandardCharsets.UTF_8);
            String header = "HTTP/1.1 200 OK\r\nContent-Type: " +
                    (path.equals("/read") || path.equals("/write") || path.equals("/list") ?
                            "application/json" : "text/html; charset=utf-8") +
                    "\r\nContent-Length: " + respBytes.length +
                    "\r\nConnection: close\r\n\r\n";
            out.write(header.getBytes(StandardCharsets.US_ASCII));
            out.write(respBytes);
            out.flush();
            out.close();
            reader.close();
            client.close();
        } catch (Exception e) {
            appendLog("處理請求錯誤: " + e.getMessage());
        }
    }

    private String readTagData(Tag tag) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"status\":\"ok\",\"uid\":\"").append(Converter.hex(tag.getId())).append("\",");
        sb.append("\"techs\":[");
        String[] techs = tag.getTechList();
        for (int i = 0; i < techs.length; i++) {
            String name = techs[i].substring(techs[i].lastIndexOf('.') + 1);
            sb.append("\"").append(name).append("\"");
            if (i < techs.length - 1) sb.append(",");
        }
        sb.append("],\"ndef\":");
        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();
                NdefMessage msg = ndef.getNdefMessage();
                ndef.close();
                if (msg != null) {
                    sb.append("{\"records\":").append(msg.getRecords().length).append("}");
                } else {
                    sb.append("null");
                }
            } else {
                sb.append("null");
            }
        } catch (Exception e) {
            sb.append("null");
        }
        sb.append("}");
        return sb.toString();
    }

    private String getHtmlPage() {
        return "<!DOCTYPE html><html lang='zh-TW'><head><meta charset='utf-8'>" +
                "<meta name='viewport' content='width=device-width,initial-scale=1'>" +
                "<title>HeliRFID PC Companion</title>" +
                "<style>body{font-family:sans-serif;max-width:600px;margin:50px auto;padding:20px;text-align:center}" +
                "h1{color:#1565C0}button{font-size:18px;padding:15px 30px;margin:10px;border:none;border-radius:8px;" +
                "color:white;cursor:pointer}button.read{background:#4CAF50}button.write{background:#FF9800}" +
                "button.list{background:#9C27B0}#result{background:#f5f5f5;padding:15px;border-radius:8px;" +
                "margin-top:20px;text-align:left;font-family:monospace;font-size:12px;white-space:pre-wrap}" +
                "</style></head><body>" +
                "<h1>\u7A0D HeliRFID PC Companion</h1>" +
                "<p>\u9023\u7DDA\u5230 Android NFC \u88DD\u7F6E\uFF0C\u9060\u7AEF\u63A7\u5236\u8B80\u5BEB\u5361\u7247</p>" +
                "<button class='read' onclick='fetch(\"/read\").then(r=>r.json()).then(d=>document.getElementById(\"result\").textContent=JSON.stringify(d,null,2))'>\u8B80\u53D6 Tag</button>" +
                "<button class='write' onclick='fetch(\"/write\").then(r=>r.json()).then(d=>document.getElementById(\"result\").textContent=JSON.stringify(d,null,2))'>\u5BEB\u5165 NDEF</button>" +
                "<button class='list' onclick='fetch(\"/list\").then(r=>r.json()).then(d=>document.getElementById(\"result\").textContent=JSON.stringify(d,null,2))'>\u5217\u51FA Tags</button>" +
                "<div id='result'>\u9EDE\u64CA\u4E0A\u65B9\u6309\u9215\u57F7\u884C\u64CD\u4F5C</div>" +
                "</body></html>";
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            pendingTag = tag;
            vibrate();
            String uid = Converter.hex(tag.getId());
            appendLog("偵測到卡片 UID: " + uid);
        }
    }

    private void appendLog(String msg) {
        handler.post(() -> {
            String prev = txtLog.getText().toString();
            txtLog.setText(msg + "\n" + prev);
            if (txtLog.getLineCount() > 50) {
                String[] lines = txtLog.getText().toString().split("\n", 2);
                txtLog.setText(lines.length > 1 ? lines[1] : "");
            }
        });
    }

    private String getLocalIpAddress() {
        try {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (ni.isLoopback() || !ni.isUp()) continue;
                for (InetAddress addr : Collections.list(ni.getInetAddresses())) {
                    if (addr instanceof java.net.Inet4Address) {
                        String ip = addr.getHostAddress();
                        if (!ip.startsWith("127.")) return ip;
                    }
                }
            }
        } catch (Exception ignored) {}
        return "127.0.0.1";
    }

    @Override
    protected void onDestroy() {
        stopServer();
        super.onDestroy();
    }
}
