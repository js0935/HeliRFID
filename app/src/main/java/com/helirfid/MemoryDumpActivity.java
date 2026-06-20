/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.0.1
 */
package com.helirfid;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class MemoryDumpActivity extends AppCompatActivity {

    RecyclerView rvDump;
    List<DumpItem> dumpList;
    DumpAdapter adapter;
    Tag currentTag;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    Button btnExportBin, btnExportEml, btnRefreshDump;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_dump);

        rvDump = findViewById(R.id.rvDump);
        rvDump.setLayoutManager(new LinearLayoutManager(this));

        btnExportBin = findViewById(R.id.btnExportBin);
        btnExportEml = findViewById(R.id.btnExportEml);
        btnRefreshDump = findViewById(R.id.btnRefreshDump);
        Button btnCloneDump = findViewById(R.id.btnCloneDump);

        dumpList = new ArrayList<>();

        currentTag = getIntent().getParcelableExtra("tag");

        if (currentTag != null) {
            readDump(currentTag);
        }

        adapter = new DumpAdapter(dumpList);
        rvDump.setAdapter(adapter);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        Intent intent = new Intent(this, getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? PendingIntent.FLAG_MUTABLE
                : PendingIntent.FLAG_UPDATE_CURRENT;
        pendingIntent = PendingIntent.getActivity(this, 0, intent, flags);

        btnExportBin.setOnClickListener(v -> exportDump("bin"));
        btnExportEml.setOnClickListener(v -> exportDump("eml"));

        btnCloneDump.setOnClickListener(v -> {
            if (dumpList.isEmpty()) {
                Toast.makeText(this, "沒有 Dump 資料", Toast.LENGTH_SHORT).show();
                return;
            }
            DumpStore.saveDump(dumpList, "Memory Dump");
            startActivity(new Intent(this, CloneActivity.class));
        });

        btnRefreshDump.setOnClickListener(v -> {
            if (currentTag != null) {
                dumpList.clear();
                readDump(currentTag);
                adapter.notifyDataSetChanged();
                Toast.makeText(this, "已重新讀取", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "無卡片資料", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void readDump(Tag tag){
        try{
            android.nfc.tech.MifareClassic mfc = android.nfc.tech.MifareClassic.get(tag);
            if (mfc == null) {
                dumpList.add(new DumpItem(-1,-1,"Error","不支援 MIFARE Classic"));
                return;
            }
            mfc.connect();
            int sectorCount = mfc.getSectorCount();

            for(int s=0; s<sectorCount; s++){
                boolean auth = false;
                for (int k = 0; k < KeyTester.getKeyCount(); k++) {
                    byte[] key = KeyTester.getKeyByKeyIndex(k);
                    if (key == null) continue;
                    try {
                        if (mfc.authenticateSectorWithKeyA(s, key)) {
                            auth = true;
                            break;
                        }
                    } catch (Exception e) {
                        // try next key
                    }
                }

                int blockCount = mfc.getBlockCountInSector(s);
                int firstBlock = mfc.sectorToBlock(s);

                if (auth) {
                    for(int b=0; b<blockCount; b++){
                        byte[] data = mfc.readBlock(firstBlock+b);
                        String hex = Converter.bytesToHex(data);

                        String desc = "";
                        if(s==0 && b==0) desc = "UID Block";
                        else if(b==blockCount-1) desc = "Access / Key Block";
                        else desc = "Data Block";

                        dumpList.add(new DumpItem(s, firstBlock+b, hex, desc));
                    }
                } else {
                    dumpList.add(new DumpItem(s, firstBlock, "讀取失敗: 無可用金鑰", "Sector " + s));
                }
            }

            mfc.close();

        }catch(Exception e){
            dumpList.add(new DumpItem(-1,-1,"Error","讀取失敗: " + e.getMessage()));
        }
    }

    private void exportDump(String format) {
        if (dumpList.isEmpty()) {
            Toast.makeText(this, "沒有資料可匯出", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String fileName = "HeliRFID_Dump." + format;
            File path = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "HeliRFID");
            if (!path.exists()) path.mkdirs();

            File file = new File(path, fileName);

            try (FileOutputStream fos = new FileOutputStream(file);
                 OutputStreamWriter writer = new OutputStreamWriter(fos)) {

                if (format.equals("eml")) {
                    // EML format: one line per block, "Sector:Block:Data"
                    for (DumpItem item : dumpList) {
                        if (item.getSector() >= 0) {
                            writer.write(String.format("+Sector: %d\n", item.getSector()));
                            writer.write(String.format("+Block: %d\n", item.getBlock()));
                            writer.write(item.getData().replace(" ", "") + "\n");
                        }
                    }
                } else {
                    // BIN format: hex dump style
                    for (DumpItem item : dumpList) {
                        if (item.getSector() >= 0) {
                            writer.write(String.format("S%02d B%02d: %s  %s\n",
                                    item.getSector(), item.getBlock(),
                                    item.getData(), item.getDescription()));
                        }
                    }
                }

                writer.flush();
            }

            Toast.makeText(this, "匯出成功: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this, "匯出失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
            if (currentTag != null) {
                dumpList.clear();
                readDump(currentTag);
                adapter.notifyDataSetChanged();
                Toast.makeText(this, "已讀取新卡片", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
