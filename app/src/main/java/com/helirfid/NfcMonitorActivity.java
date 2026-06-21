package com.helirfid;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.NfcA;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NfcMonitorActivity extends AppCompatActivity {

    TextView txtLog;
    Button btnStart, btnStop, btnExport, btnClear;
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    IntentFilter[] nfcFilters;
    Tag currentTag;
    boolean monitoring = false;
    StringBuilder logBuffer = new StringBuilder();
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_monitor);

        txtLog = findViewById(R.id.txtMonitorLog);
        btnStart = findViewById(R.id.btnMonitorStart);
        btnStop = findViewById(R.id.btnMonitorStop);
        btnExport = findViewById(R.id.btnMonitorExport);
        btnClear = findViewById(R.id.btnMonitorClear);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        Intent intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) flags |= PendingIntent.FLAG_MUTABLE;
        pendingIntent = PendingIntent.getActivity(this, 0, intent, flags);
        nfcFilters = new IntentFilter[]{
                new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED),
                new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED),
                new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED),
        };

        btnStart.setOnClickListener(v -> {
            monitoring = true;
            appendLog("=== NFC 監聽已啟動 ===");
        });

        btnStop.setOnClickListener(v -> {
            monitoring = false;
            appendLog("=== NFC 監聽已停止 ===");
        });

        btnClear.setOnClickListener(v -> {
            logBuffer.setLength(0);
            txtLog.setText("");
        });

        btnExport.setOnClickListener(v -> exportLog());
    }

    private void exportLog() {
        String log = txtLog.getText().toString();
        if (TextUtils.isEmpty(log)) {
            Toast.makeText(this, "無日誌可匯出", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            File dir = new File(getExternalFilesDir(null), "nfc_monitor");
            dir.mkdirs();
            File file = new File(dir, "nfc_monitor_" + System.currentTimeMillis() + ".txt");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(log.getBytes(StandardCharsets.UTF_8));
            fos.close();
            Toast.makeText(this, "已匯出: " + file.getName(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "匯出失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void appendLog(String line) {
        String time = sdf.format(new Date());
        logBuffer.append("[").append(time).append("] ").append(line).append("\n");
        txtLog.setText(logBuffer.toString());
    }

    private void handleTag(Tag tag) {
        currentTag = tag;
        if (!monitoring) return;

        StringBuilder sb = new StringBuilder();
        sb.append("=== 卡片已偵測 ===\n");
        sb.append("UID: ").append(NFCReader.getUID(tag)).append("\n");
        sb.append("技術: ");
        for (String t : tag.getTechList()) {
            sb.append(t.substring(t.lastIndexOf('.') + 1)).append(" ");
        }
        sb.append("\n");

        NfcA nfcA = NfcA.get(tag);
        if (nfcA != null) {
            try {
                nfcA.connect();
                byte[] atqa = nfcA.getAtqa();
                short sak = nfcA.getSak();
                nfcA.close();
                sb.append("ATQA: ").append(bytesToHex(atqa)).append("\n");
                sb.append("SAK: 0x").append(String.format("%02X", sak & 0xFF)).append("\n");
            } catch (Exception e) { }
        }

        IsoDep isoDep = IsoDep.get(tag);
        if (isoDep != null) {
            try {
                isoDep.connect();
                byte[] hist = isoDep.getHistoricalBytes();
                isoDep.close();
                if (hist != null && hist.length > 0) {
                    sb.append("Historical Bytes: ").append(bytesToHex(hist)).append("\n");
                }
            } catch (Exception e) { }
        }

        MifareClassic mfc = MifareClassic.get(tag);
        if (mfc != null) {
            try {
                mfc.connect();
                sb.append("MIFARE Classic: ").append(mfc.getMaxTransceiveLength()).append(" bytes\n");
                sb.append("  大小: ").append(mfc.getSize() / 1024).append("K\n");
                sb.append("  Sector 數: ").append(mfc.getSectorCount()).append("\n");
                sb.append("  Block 數: ").append(mfc.getBlockCount()).append("\n");
                mfc.close();
            } catch (Exception e) { }
        }

        MifareUltralight mu = MifareUltralight.get(tag);
        if (mu != null) {
            try {
                mu.connect();
                sb.append("MIFARE Ultralight/NTAG\n");
                sb.append("  超時: ").append(mu.getTimeout()).append("ms\n");
                mu.close();
                try {
                    mu.connect();
                    byte[] ver = mu.transceive(new byte[]{(byte)0x60});
                    sb.append("  GetVersion: ").append(bytesToHex(ver)).append("\n");
                    mu.close();
                } catch (Exception e) { }
            } catch (Exception e) { }
        }

        appendLog(sb.toString().trim());
    }

    private String bytesToHex(byte[] bytes) {
        if (bytes == null) return "";
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02X ", b));
        return sb.toString().trim();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null)
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, nfcFilters, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null)
            nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) handleTag(tag);
        }
    }
}
