package com.helirfid;

import android.content.Intent;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DumpExportActivity extends BaseNfcActivity {

    private static final int REQUEST_CODE_CREATE_FILE = 1001;

    private TextView txtInfo, txtResult;
    private ListView lvDumps;
    private Spinner spinnerFormat;
    private Button btnExportSelected, btnExportAll, btnRefresh;
    private CheckBox[] checkBoxes;
    private List<DumpItem> dumpList;
    private List<DumpItem> pendingExportItems;
    private String currentFormat = "nfc";
    private Tag currentTag;

    private final String[] FORMATS = {".nfc (Flipper Zero)", ".txt (Hex Dump)", ".bin (Raw Binary)", ".hex (Intel HEX)"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dump_export);

        txtInfo = findViewById(R.id.txtDumpExportInfo);
        txtResult = findViewById(R.id.txtDumpExportResult);
        lvDumps = findViewById(R.id.lvDumpExportList);
        spinnerFormat = findViewById(R.id.spinnerDumpExportFormat);
        btnExportSelected = findViewById(R.id.btnDumpExportSelected);
        btnExportAll = findViewById(R.id.btnDumpExportAll);
        btnRefresh = findViewById(R.id.btnDumpExportRefresh);

        ArrayAdapter<String> fmtAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, FORMATS);
        fmtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFormat.setAdapter(fmtAdapter);

        dumpList = new ArrayList<>();

        currentTag = getIntent().getParcelableExtra("tag");
        if (currentTag != null) readDump(currentTag);

        ArrayAdapter<String> dumpAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_multiple_choice, new String[]{"Load a tag first"});
        lvDumps.setAdapter(dumpAdapter);
        lvDumps.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        spinnerFormat.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> p, android.view.View v, int pos, long id) {
                switch (pos) { case 0: currentFormat = "nfc"; break; case 1: currentFormat = "txt"; break; case 2: currentFormat = "bin"; break; case 3: currentFormat = "hex"; break; }
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> p) {}
        });

        btnExportSelected.setOnClickListener(v -> exportSelected());
        btnExportAll.setOnClickListener(v -> exportAll());
        btnRefresh.setOnClickListener(v -> {
            if (currentTag != null) { dumpList.clear(); readDump(currentTag); updateDumpList(); Toast.makeText(this, "已重新讀取", Toast.LENGTH_SHORT).show(); }
            else Toast.makeText(this, "無卡片資料", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) return;
        currentTag = tag;
        dumpList.clear();
        readDump(tag);
        txtInfo.setText("卡片已偵測:\nUID: " + Converter.hex(tag.getId()));
        updateDumpList();
    }

    private void readDump(Tag tag) {
        try {
            android.nfc.tech.MifareClassic mfc = android.nfc.tech.MifareClassic.get(tag);
            if (mfc == null) {
                dumpList.add(new DumpItem(-1, -1, "Error", "不支援 MIFARE Classic"));
                return;
            }
            mfc.connect();
            int sectorCount = mfc.getSectorCount();
            for (int s = 0; s < sectorCount; s++) {
                boolean auth = false;
                for (int k = 0; k < KeyTester.getKeyCount(); k++) {
                    byte[] key = KeyTester.getKeyByKeyIndex(k);
                    if (key == null) continue;
                    try { if (mfc.authenticateSectorWithKeyA(s, key)) { auth = true; break; } } catch (Exception e) { }
                }
                int blockCount = mfc.getBlockCountInSector(s);
                int firstBlock = mfc.sectorToBlock(s);
                if (auth) {
                    for (int b = 0; b < blockCount; b++) {
                        byte[] data = mfc.readBlock(firstBlock + b);
                        String hex = Converter.bytesToHex(data);
                        String desc = (s == 0 && b == 0) ? "UID Block" : (b == blockCount - 1 ? "Access / Key Block" : "Data Block");
                        dumpList.add(new DumpItem(s, firstBlock + b, hex, desc));
                    }
                } else {
                    dumpList.add(new DumpItem(s, firstBlock, "讀取失敗: 無可用金鑰", "Sector " + s));
                }
            }
            mfc.close();
        } catch (Exception e) {
            dumpList.add(new DumpItem(-1, -1, "Error", "讀取失敗: " + e.getMessage()));
        }
    }

    private void updateDumpList() {
        String[] items = new String[dumpList.size()];
        for (int i = 0; i < dumpList.size(); i++) {
            DumpItem item = dumpList.get(i);
            items[i] = String.format("S%02d B%02d: %s", item.getSector(), item.getBlock(), item.getDescription());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_multiple_choice, items);
        lvDumps.setAdapter(adapter);
    }

    private void exportSelected() {
        List<DumpItem> selected = new ArrayList<>();
        for (int i = 0; i < lvDumps.getCount(); i++) {
            if (lvDumps.isItemChecked(i)) selected.add(dumpList.get(i));
        }
        if (selected.isEmpty()) { Toast.makeText(this, "請選擇要匯出的區塊", Toast.LENGTH_SHORT).show(); return; }
        doExport(selected);
    }

    private void exportAll() {
        if (dumpList.isEmpty()) { Toast.makeText(this, "沒有資料可匯出", Toast.LENGTH_SHORT).show(); return; }
        doExport(dumpList);
    }

    private void doExport(List<DumpItem> items) {
        pendingExportItems = items;
        String ext = currentFormat;
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, "HeliRFID_Dump." + ext);
        startActivityForResult(intent, REQUEST_CODE_CREATE_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CREATE_FILE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            if (uri != null && pendingExportItems != null) {
                try (OutputStream os = getContentResolver().openOutputStream(uri)) {
                    byte[] exportData = buildExportData(pendingExportItems);
                    if (exportData != null) {
                        os.write(exportData);
                        os.flush();
                        txtResult.setText("匯出成功: " + uri.getLastPathSegment());
                        Toast.makeText(this, "匯出成功", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    txtResult.setText("匯出失敗: " + e.getMessage());
                    Toast.makeText(this, "匯出失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private byte[] buildExportData(List<DumpItem> items) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            switch (currentFormat) {
                case "nfc":
                    baos.write(buildNfcExport(items).getBytes(StandardCharsets.UTF_8));
                    break;
                case "txt":
                    baos.write(buildTxtExport(items).getBytes(StandardCharsets.UTF_8));
                    break;
                case "bin":
                    baos.write(buildBinExport(items));
                    break;
                case "hex":
                    baos.write(buildHexExport(items).getBytes(StandardCharsets.UTF_8));
                    break;
                default:
                    baos.write(buildNfcExport(items).getBytes(StandardCharsets.UTF_8));
            }
            return baos.toByteArray();
        } catch (Exception e) {
            txtResult.setText("建構失敗: " + e.getMessage());
            return null;
        }
    }

    private String buildNfcExport(List<DumpItem> items) {
        StringBuilder sb = new StringBuilder();
        String uid = "";
        String sak = "00";
        String atqa = "0000";
        List<String> blocks = new ArrayList<>();

        for (DumpItem item : items) {
            if (item.getSector() == 0 && item.getBlock() == 0) {
                String hex = item.getData().replace(" ", "");
                if (hex.length() >= 8) uid = hex.substring(0, 8);
                if (hex.length() >= 38) {
                    sak = hex.substring(34, 36);
                    atqa = hex.substring(30, 34);
                }
            }
            if (item.getSector() >= 0) {
                String data = item.getData().replace(" ", "");
                if (!data.isEmpty() && !data.contains("讀取失敗")) {
                    blocks.add(String.format("Block %d: %s", item.getBlock(), data));
                }
            }
        }

        sb.append("Filetype: Flipper NFC device\n");
        sb.append("Version: 2\n");
        sb.append("# Device type can be MIFARE Classic, NTAG, Ultralight\n");
        sb.append("Device type: Mifare Classic\n");
        sb.append("# UID, SAK, ATQA are common for all card types\n");
        sb.append("UID: ").append(uid).append("\n");
        sb.append("SAK: ").append(sak).append("\n");
        sb.append("ATQA: ").append(atqa).append("\n");
        sb.append("# Memory data\n");
        for (String block : blocks) {
            sb.append(block).append("\n");
        }
        return sb.toString();
    }

    private String buildTxtExport(List<DumpItem> items) {
        StringBuilder sb = new StringBuilder();
        sb.append("HeliRFID Dump Export\n");
        sb.append("====================\n\n");
        sb.append("Block Count: ").append(items.size()).append("\n\n");

        for (DumpItem item : items) {
            if (item.getSector() >= 0) {
                String hex = item.getData();
                String ascii = asciiPart(hex);
                sb.append(String.format("S%02d B%02d: %-48s  %s", item.getSector(), item.getBlock(), hex, ascii)).append("\n");
            }
        }
        return sb.toString();
    }

    private byte[] buildBinExport(List<DumpItem> items) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (DumpItem item : items) {
            if (item.getSector() >= 0) {
                String hex = item.getData().replace(" ", "");
                for (int i = 0; i < hex.length(); i += 2) {
                    baos.write((byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i + 1), 16)));
                }
            }
        }
        return baos.toByteArray();
    }

    private String buildHexExport(List<DumpItem> items) {
        StringBuilder sb = new StringBuilder();
        int address = 0;
        int lineCount = 0;
        int dataSize = 0;
        ByteArrayOutputStream dataStream = new ByteArrayOutputStream();

        for (DumpItem item : items) {
            if (item.getSector() >= 0) {
                String hex = item.getData().replace(" ", "");
                for (int i = 0; i < hex.length(); i += 2) {
                    dataStream.write((byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i + 1), 16)));
                }
            }
        }

        byte[] allData = dataStream.toByteArray();
        dataSize = allData.length;

        for (int i = 0; i < dataSize; i += 16) {
            int len = Math.min(16, dataSize - i);
            sb.append(String.format(":%02X%04X00", len, address));
            int checksum = len + (address >> 8) + (address & 0xFF);
            for (int j = 0; j < len; j++) {
                sb.append(String.format("%02X", allData[i + j]));
                checksum += allData[i + j];
            }
            checksum = (~checksum + 1) & 0xFF;
            sb.append(String.format("%02X\n", checksum));
            address += len;
            lineCount++;
        }

        // End of file record
        sb.append(":00000001FF\n");
        return sb.toString();
    }

    private String asciiPart(String hex) {
        StringBuilder sb = new StringBuilder();
        String[] parts = hex.split(" ");
        for (String p : parts) {
            if (p.length() == 2) {
                byte b = (byte) Integer.parseInt(p, 16);
                sb.append((b >= 0x20 && b < 0x7F) ? (char) b : '.');
            }
        }
        return sb.toString();
    }
}
