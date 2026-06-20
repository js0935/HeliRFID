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
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class SectorSelectActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private Tag currentTag;

    ListView listSectors;
    Button btnReadSelected, btnWriteSelected, btnSelectAll, btnDeselectAll;
    TextView txtSectorInfo;

    SectorAdapter adapter;
    List<SectorItem> sectorItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sector_select);

        listSectors = findViewById(R.id.listSectors);
        btnReadSelected = findViewById(R.id.btnReadSelected);
        btnWriteSelected = findViewById(R.id.btnWriteSelected);
        btnSelectAll = findViewById(R.id.btnSelectAll);
        btnDeselectAll = findViewById(R.id.btnDeselectAll);
        txtSectorInfo = findViewById(R.id.txtSectorInfo);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        Intent intent = new Intent(this, getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? PendingIntent.FLAG_MUTABLE
                : PendingIntent.FLAG_UPDATE_CURRENT;
        pendingIntent = PendingIntent.getActivity(this, 0, intent, flags);

        btnSelectAll.setOnClickListener(v -> {
            for (SectorItem item : sectorItems) item.checked = true;
            adapter.notifyDataSetChanged();
        });

        btnDeselectAll.setOnClickListener(v -> {
            for (SectorItem item : sectorItems) item.checked = false;
            adapter.notifyDataSetChanged();
        });

        btnReadSelected.setOnClickListener(v -> operateSectors(false));
        btnWriteSelected.setOnClickListener(v -> operateSectors(true));
    }

    private void operateSectors(boolean write) {
        if (currentTag == null) {
            Toast.makeText(this, "請先掃描卡片", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                MifareClassic mfc = MifareClassic.get(currentTag);
                if (mfc == null) {
                    runOnUiThread(() -> txtSectorInfo.setText("不支援 MIFARE Classic"));
                    return;
                }
                mfc.connect();
                mfc.setTimeout(5000);

                byte[] key = {(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF};

                StringBuilder sb = new StringBuilder();
                int count = 0;

                for (SectorItem item : sectorItems) {
                    if (!item.checked) continue;

                    boolean auth = mfc.authenticateSectorWithKeyA(item.sector, key);
                    if (!auth) {
                        sb.append("Sector ").append(item.sector).append(": 認證失敗\n");
                        continue;
                    }

                    int blockIndex = mfc.sectorToBlock(item.sector);
                    int blocksPerSector = item.sector < 32 ? 4 : 16;

                    if (!write) {
                        sb.append("Sector ").append(item.sector).append(":\n");
                        for (int b = 0; b < blocksPerSector; b++) {
                            byte[] data = mfc.readBlock(blockIndex + b);
                            sb.append(String.format("  [%03d] ", blockIndex + b));
                            for (byte d : data) sb.append(String.format("%02X ", d));
                            sb.append("\n");
                        }
                    } else {
                        byte[] dumpData = DumpStore.getDumpData();
                        if (dumpData != null) {
                            for (int b = 0; b < blocksPerSector; b++) {
                                int absBlock = blockIndex + b;
                                if (absBlock * 16 + 16 <= dumpData.length) {
                                    byte[] blockData = new byte[16];
                                    System.arraycopy(dumpData, absBlock * 16, blockData, 0, 16);
                                    mfc.writeBlock(absBlock, blockData);
                                }
                            }
                            sb.append("Sector ").append(item.sector).append(": 寫入完成\n");
                        } else {
                            sb.append("Sector ").append(item.sector).append(": 無 Dump 資料\n");
                        }
                    }
                    count++;
                }

                mfc.close();

                if (count == 0) sb.append("未選取任何 Sector");
                final String result = sb.toString();
                runOnUiThread(() -> txtSectorInfo.setText(result));

            } catch (Exception e) {
                runOnUiThread(() -> txtSectorInfo.setText("錯誤: " + e.getMessage()));
            }
        }).start();
    }

    private void initSectors() {
        sectorItems.clear();
        int totalSectors = 16;
        if (currentTag != null) {
            MifareClassic mfc = MifareClassic.get(currentTag);
            if (mfc != null) totalSectors = mfc.getSectorCount();
        }
        for (int i = 0; i < totalSectors; i++)
            sectorItems.add(new SectorItem(i, false));
        adapter.notifyDataSetChanged();
    }

    private class SectorItem {
        int sector;
        boolean checked;
        SectorItem(int s, boolean c) { sector = s; checked = c; }
    }

    private class SectorAdapter extends BaseAdapter {
        @Override
        public int getCount() { return sectorItems.size(); }

        @Override
        public Object getItem(int pos) { return sectorItems.get(pos); }

        @Override
        public long getItemId(int pos) { return pos; }

        @Override
        public View getView(int pos, View convert, ViewGroup parent) {
            if (convert == null)
                convert = getLayoutInflater().inflate(R.layout.sector_item, parent, false);

            SectorItem item = sectorItems.get(pos);
            CheckBox cb = convert.findViewById(R.id.cbSector);

            int blocksPerSector = item.sector < 32 ? 4 : 16;
            cb.setText("Sector " + item.sector + " (Blocks " +
                    (item.sector * (item.sector < 32 ? 4 : 16)) + "-" +
                    (item.sector * (item.sector < 32 ? 4 : 16) + blocksPerSector - 1) + ")");
            cb.setChecked(item.checked);
            cb.setOnCheckedChangeListener((buttonView, isChecked) -> item.checked = isChecked);

            return convert;
        }
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
            initSectors();
            StringBuilder info = new StringBuilder("卡片已偵測\nUID: ");
            for (byte b : currentTag.getId()) info.append(String.format("%02X", b));
            info.append("\nSectors: ").append(sectorItems.size());
            txtSectorInfo.setText(info.toString());
        }
    }
}
