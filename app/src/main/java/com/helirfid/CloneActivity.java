/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.0.1
 */
package com.helirfid;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class CloneActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private Tag currentTag;
    private boolean writing = false;

    TextView txtSourceInfo, txtStatus, txtProgress;
    Button btnStartClone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clone);

        txtSourceInfo = findViewById(R.id.txtSourceInfo);
        txtStatus = findViewById(R.id.txtCloneStatus);
        txtProgress = findViewById(R.id.txtCloneProgress);
        btnStartClone = findViewById(R.id.btnStartClone);

        if (DumpStore.hasDump()) {
            txtSourceInfo.setText("來源: " + DumpStore.getSourceInfo()
                    + "\n區塊數: " + DumpStore.getDump().size());
        } else {
            txtSourceInfo.setText("無 Dump 資料，請先從 MemoryDump 匯出");
            btnStartClone.setEnabled(false);
        }

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        Intent intent = new Intent(this, getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? PendingIntent.FLAG_MUTABLE
                : PendingIntent.FLAG_UPDATE_CURRENT;
        pendingIntent = PendingIntent.getActivity(this, 0, intent, flags);

        btnStartClone.setOnClickListener(v -> {
            if (!DumpStore.hasDump()) {
                Toast.makeText(this, "無 Dump 資料", Toast.LENGTH_SHORT).show();
                return;
            }
            if (currentTag == null) {
                Toast.makeText(this, "請先掃描目標卡片", Toast.LENGTH_SHORT).show();
                return;
            }
            writing = true;
            startClone();
        });
    }

    private void startClone() {
        txtStatus.setText("正在複製...");
        txtProgress.setText("");
        btnStartClone.setEnabled(false);

        new Thread(() -> {
            StringBuilder result = new StringBuilder();
            try {
                MifareClassic mfc = MifareClassic.get(currentTag);
                if (mfc == null) {
                    runOnUiThread(() -> {
                        txtStatus.setText("失敗：不支援 MIFARE Classic");
                        btnStartClone.setEnabled(true);
                        writing = false;
                    });
                    return;
                }

                mfc.connect();
                List<DumpItem> dump = DumpStore.getDump();
                int success = 0;
                int fail = 0;

                for (DumpItem item : dump) {
                    if (item.getSector() < 0) continue;

                    String hexStr = item.getData().replace(" ", "");
                    if (hexStr.length() != 32) continue;

                    byte[] data = hexStringToByteArray(hexStr);
                    int sector = item.getSector();
                    int block = item.getBlock();

                    boolean auth = false;
                    for (int k = 0; k < KeyTester.getKeyCount(); k++) {
                        byte[] key = KeyTester.getKeyByKeyIndex(k);
                        if (key == null) continue;
                        try {
                            if (mfc.authenticateSectorWithKeyA(sector, key)) {
                                auth = true;
                                break;
                            }
                        } catch (Exception e) { }
                    }

                    if (!auth) {
                        fail++;
                        continue;
                    }

                    try {
                        mfc.writeBlock(block, data);
                        success++;
                    } catch (Exception e) {
                        fail++;
                    }

                    final int s = success;
                    final int f = fail;
                    runOnUiThread(() ->
                        txtProgress.setText("已寫入: " + s + " / 失敗: " + f));
                }

                mfc.close();

                final int finalSuccess = success;
                final int finalFail = fail;
                runOnUiThread(() -> {
                    txtStatus.setText("複製完成");
                    txtProgress.setText("成功: " + finalSuccess + " 區塊, 失敗: " + finalFail + " 區塊");
                    btnStartClone.setEnabled(true);
                    writing = false;
                    Toast.makeText(this, "複製完成", Toast.LENGTH_LONG).show();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    txtStatus.setText("複製失敗: " + e.getMessage());
                    btnStartClone.setEnabled(true);
                    writing = false;
                });
            }
        }).start();
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null)
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
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
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            currentTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            txtStatus.setText("已偵測到目標卡片，點擊「開始複製」");
            Toast.makeText(this, "目標卡片已偵測", Toast.LENGTH_SHORT).show();
        }
    }
}
