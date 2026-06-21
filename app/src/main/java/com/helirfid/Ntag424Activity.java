package com.helirfid;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class Ntag424Activity extends BaseNfcActivity {

    private TextView txtStatus, txtInfo;
    private EditText editFileNo, editOffset, editWriteData;
    private Button btnVersion, btnAuth, btnRead, btnWrite, btnSdmMeta, btnSunCheck;

    private Ntag424 ntag424;
    private IsoDep isoDep;
    private byte[] tagUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ntag_424);

        txtStatus = findViewById(R.id.txt424Status);
        txtInfo = findViewById(R.id.txt424Info);
        editFileNo = findViewById(R.id.edit424FileNo);
        editOffset = findViewById(R.id.edit424Offset);
        editWriteData = findViewById(R.id.edit424WriteData);
        btnVersion = findViewById(R.id.btn424Version);
        btnAuth = findViewById(R.id.btn424Auth);
        btnRead = findViewById(R.id.btn424Read);
        btnWrite = findViewById(R.id.btn424Write);
        btnSdmMeta = findViewById(R.id.btn424SdmMeta);
        btnSunCheck = findViewById(R.id.btn424SunCheck);

        btnVersion.setOnClickListener(v -> doGetVersion());
        btnAuth.setOnClickListener(v -> doAuthenticate());
        btnRead.setOnClickListener(v -> doReadData());
        btnWrite.setOnClickListener(v -> doWriteData());
        btnSdmMeta.setOnClickListener(v -> doSdmMeta());
        btnSunCheck.setOnClickListener(v -> doSunCheck());

        Tag tag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) setupTag(tag);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) setupTag(tag);
    }

    private void setupTag(Tag tag) {
        tagUid = tag.getId();
        isoDep = IsoDep.get(tag);
        if (isoDep == null) {
            txtStatus.setText("此標籤不支援 IsoDep\nNTAG 424 DNA 需要 ISO 14443-4 協定");
            ntag424 = null;
            return;
        }
        try {
            isoDep.connect();
            ntag424 = new Ntag424(isoDep);
            txtStatus.setText("卡片已連線\nUID: " + Converter.hex(tagUid));
            txtInfo.setText("NTAG 424 DNA 已就緒\n請選擇上方功能");
        } catch (IOException e) {
            txtStatus.setText("連線失敗: " + e.getMessage());
            ntag424 = null;
        }
    }

    private void doGetVersion() {
        if (ntag424 == null) { toast("請先掃描卡片"); return; }
        new Thread(() -> {
            try {
                String info = ntag424.getVersionString();
                runOnUiThread(() -> txtInfo.setText(info));
            } catch (Exception e) {
                runOnUiThread(() -> txtInfo.setText("錯誤: " + e.getMessage()));
            }
        }).start();
    }

    private void doAuthenticate() {
        if (ntag424 == null) { toast("請先掃描卡片"); return; }
        new Thread(() -> {
            try {
                boolean ok = ntag424.authenticateDefault();
                String msg = ok ? "認證成功！可使用進階功能" : "預設金鑰認證失敗 (卡片可能已更換金鑰)";
                runOnUiThread(() -> {
                    txtStatus.setText(msg);
                    txtInfo.setText(msg + "\n工作階段金鑰: " +
                            (ok ? Converter.hex(ntag424.getSessionKey()) : "N/A"));
                });
            } catch (Exception e) {
                runOnUiThread(() -> txtInfo.setText("認證錯誤: " + e.getMessage()));
            }
        }).start();
    }

    private void doReadData() {
        if (ntag424 == null) { toast("請先掃描卡片"); return; }
        int fileNo = getInt(editFileNo, 0);
        int offset = getInt(editOffset, 0);
        new Thread(() -> {
            try {
                byte[] resp = ntag424.readData(fileNo, offset, 48);
                boolean ok = Ntag424.isSuccess(resp);
                byte[] data = Ntag424.getData(resp);
                StringBuilder sb = new StringBuilder();
                if (ok) {
                    sb.append("讀取成功 (").append(data.length).append(" bytes)\n");
                    sb.append(MifareUtils.hexWithAscii(data));
                } else {
                    sb.append("讀取失敗 (SW=").append(String.format("%04X", Ntag424.getSw(resp))).append(")");
                }
                final String res = sb.toString();
                runOnUiThread(() -> txtInfo.setText(res));
            } catch (Exception e) {
                runOnUiThread(() -> txtInfo.setText("讀取錯誤: " + e.getMessage()));
            }
        }).start();
    }

    private void doWriteData() {
        if (ntag424 == null) { toast("請先掃描卡片"); return; }
        int fileNo = getInt(editFileNo, 0);
        int offset = getInt(editOffset, 0);
        String hex = editWriteData.getText().toString().trim().replace(" ", "");
        if (hex.isEmpty()) { toast("請輸入寫入資料 (hex)"); return; }
        byte[] data = Converter.hexToBytes(hex);
        new Thread(() -> {
            try {
                byte[] resp = ntag424.writeData(fileNo, offset, data);
                boolean ok = Ntag424.isSuccess(resp);
                String msg = ok ? "寫入成功" : "寫入失敗 (SW=" + String.format("%04X", Ntag424.getSw(resp)) + ")";
                final String fmsg = msg;
                runOnUiThread(() -> txtInfo.setText(fmsg));
            } catch (Exception e) {
                runOnUiThread(() -> txtInfo.setText("寫入錯誤: " + e.getMessage()));
            }
        }).start();
    }

    private void doSdmMeta() {
        if (ntag424 == null) { toast("請先掃描卡片"); return; }
        new Thread(() -> {
            try {
                byte[] meta = ntag424.getSdmMeta(tagUid);
                boolean ok = Ntag424.isSuccess(meta);
                StringBuilder sb = new StringBuilder("SDM 配置:\n");
                if (ok) {
                    byte[] d = Ntag424.getData(meta);
                    sb.append(MifareUtils.hexWithAscii(d));
                } else {
                    sb.append("讀取失敗 (SW=").append(String.format("%04X", Ntag424.getSw(meta))).append(")");
                }
                final String res = sb.toString();
                runOnUiThread(() -> txtInfo.setText(res));
            } catch (Exception e) {
                runOnUiThread(() -> txtInfo.setText("錯誤: " + e.getMessage()));
            }
        }).start();
    }

    private void doSunCheck() {
        if (ntag424 == null) { toast("請先掃描卡片"); return; }
        new Thread(() -> {
            try {
                byte[] resp = ntag424.getFileIds();
                boolean ok = Ntag424.isSuccess(resp);
                StringBuilder sb = new StringBuilder("SUN 驗證:\n");
                if (ok) {
                    byte[] d = Ntag424.getData(resp);
                    sb.append("檔案 ID: ").append(Converter.hex(d)).append("\n");
                    sb.append("SUN 需透過 SDM 讀取認證後資料驗證\n");
                } else {
                    sb.append("讀取失敗 (SW=").append(String.format("%04X", Ntag424.getSw(resp))).append(")");
                }
                final String res = sb.toString();
                runOnUiThread(() -> txtInfo.setText(res));
            } catch (Exception e) {
                runOnUiThread(() -> txtInfo.setText("錯誤: " + e.getMessage()));
            }
        }).start();
    }

    private int getInt(EditText et, int def) {
        try { return Integer.parseInt(et.getText().toString().trim()); }
        catch (Exception e) { return def; }
    }

    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }
}
