/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.3.1
 */
package com.helirfid;

import android.content.Intent;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.widget.Button;
import android.widget.TextView;

public class MemoryMapActivity extends BaseNfcActivity {

    private TextView txtInfo, txtGrid, txtLegend;
    private Button btnRefresh;
    private Tag currentTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_map);

        txtInfo = findViewById(R.id.txtMemInfo);
        txtGrid = findViewById(R.id.txtMemGrid);
        txtLegend = findViewById(R.id.txtMemLegend);
        btnRefresh = findViewById(R.id.btnMemRefresh);

        btnRefresh.setOnClickListener(v -> {
            if (currentTag != null) renderMemoryMap(currentTag);
        });

        Tag tag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            currentTag = tag;
            renderMemoryMap(tag);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            currentTag = tag;
            txtInfo.setText("卡片已偵測 (UID: " + Converter.hex(tag.getId()) + ")");
            renderMemoryMap(tag);
        }
    }

    private void renderMemoryMap(Tag tag) {
        new Thread(() -> {
            try {
                MifareClassic mfc = MifareClassic.get(tag);
                if (mfc != null) {
                    renderMifareClassic(mfc);
                    return;
                }
                MifareUltralight mu = MifareUltralight.get(tag);
                if (mu != null) {
                    renderUltralight(mu);
                    return;
                }
                runOnUiThread(() -> txtInfo.setText("不支援的卡片類型"));
            } catch (Exception e) {
                runOnUiThread(() -> txtInfo.setText("讀取錯誤: " + e.getMessage()));
            }
        }).start();
    }

    private void renderMifareClassic(MifareClassic mfc) {
        try {
            mfc.connect();
            int sectors = mfc.getSectorCount();
            StringBuilder sb = new StringBuilder();
            sb.append("MIFARE Classic (").append(sectors).append(" 磁區)\n\n");

            SpannableStringBuilder colored = new SpannableStringBuilder();

            for (int s = 0; s < sectors; s++) {
                int blocks = mfc.getBlockCountInSector(s);
                int firstBlock = mfc.sectorToBlock(s);
                sb.append(String.format("S%02d: ", s));

                String prefix = String.format("S%02d: ", s);
                colored.append(prefix);

                boolean authed = false;
                try {
                    authed = mfc.authenticateSectorWithKeyA(s, MifareClassic.KEY_DEFAULT);
                } catch (Exception ignored) {}

                for (int b = 0; b < blocks; b++) {
                    int blockNum = firstBlock + b;
                    boolean isManufacturer = (s == 0 && b == 0);
                    boolean isTrailer = (b == blocks - 1);

                    String data;
                    try {
                        if (!authed && !isManufacturer) {
                            data = "?? ?? ?? ?? ?? ?? ?? ?? ?? ?? ?? ?? ?? ?? ?? ??";
                        } else {
                            byte[] blockData = mfc.readBlock(blockNum);
                            StringBuilder hex = new StringBuilder();
                            for (byte bb : blockData) hex.append(String.format("%02X ", bb));
                            data = hex.toString().trim();
                        }
                    } catch (Exception e) {
                        data = "?? ?? ?? ?? ?? ?? ?? ?? ?? ?? ?? ?? ?? ?? ?? ??";
                    }

                    int start = colored.length();
                    colored.append(data).append("  ");
                    int end = colored.length();

                    int color;
                    if (isManufacturer) color = Color.GRAY;
                    else if (isTrailer) color = Color.rgb(255, 152, 0);
                    else {
                        boolean empty = true;
                        for (char c : data.toCharArray()) {
                            if (c != '0' && c != ' ' && c != '?') { empty = false; break; }
                        }
                        color = empty ? Color.rgb(144, 238, 144) : Color.WHITE;
                    }
                    colored.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                colored.append("\n");
                sb.append("\n");
            }
            mfc.close();

            final SpannableStringBuilder finalColored = colored;
            runOnUiThread(() -> {
                txtGrid.setText(finalColored);
                txtInfo.setText("MIFARE Classic - " + sectors + " 磁區");
            });
        } catch (Exception e) {
            runOnUiThread(() -> txtInfo.setText("MFC 讀取錯誤: " + e.getMessage()));
        }
    }

    private void renderUltralight(MifareUltralight mu) {
        try {
            mu.connect();
            int pages = 64;
            StringBuilder sb = new StringBuilder();
            sb.append("NTAG/Ultralight (").append(pages).append(" pages)\n\n");

            SpannableStringBuilder colored = new SpannableStringBuilder();

            for (int p = 0; p < pages; p += 4) {
                byte[] pageData;
                try {
                    pageData = mu.readPages(p);
                } catch (Exception e) {
                    break;
                }

                String line = String.format("P%02d-%02d: ", p, p + 3);
                colored.append(line);

                for (int i = 0; i < pageData.length; i++) {
                    int pageNum = p + (i / 4);
                    boolean isManufacturer = (pageNum == 0);
                    boolean isLock = (pageNum == 2 || pageNum == 3);
                    boolean isOtp = (pageNum == 1);

                    int start = colored.length();
                    colored.append(String.format("%02X ", pageData[i]));
                    int end = colored.length();

                    int color;
                    if (isManufacturer || isOtp) color = Color.GRAY;
                    else if (isLock) color = Color.rgb(255, 152, 0);
                    else if (pageData[i] == 0) color = Color.rgb(144, 238, 144);
                    else color = Color.WHITE;

                    if (color != Color.WHITE) {
                        colored.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
                colored.append("\n");
            }
            mu.close();

            final SpannableStringBuilder finalColored = colored;
            runOnUiThread(() -> {
                txtGrid.setText(finalColored);
                txtInfo.setText("NTAG/Ultralight");
            });
        } catch (Exception e) {
            runOnUiThread(() -> txtInfo.setText("UL 讀取錯誤: " + e.getMessage()));
        }
    }
}
