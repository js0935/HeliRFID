package com.helirfid;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;

public class DumpHighlightActivity extends BaseNfcActivity {

    TextView txtResult;
    Button btnLoad, btnClear;

    private static final int PICK_FILE = 1001;

    private static final int COLOR_MFR = Color.rgb(255, 193, 7);
    private static final int COLOR_DATA = Color.rgb(76, 175, 80);
    private static final int COLOR_TRAILER = Color.rgb(244, 67, 54);
    private static final int COLOR_VALUE = Color.rgb(33, 150, 243);
    private static final int COLOR_ADDR = Color.rgb(156, 39, 176);
    private static final int COLOR_OFFSET = Color.rgb(158, 158, 158);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dump_highlight);

        txtResult = findViewById(R.id.txtDumpHighlightResult);
        btnLoad = findViewById(R.id.btnLoadDumpHighlight);
        btnClear = findViewById(R.id.btnClearHighlight);

        btnLoad.setOnClickListener(v -> {
            android.content.Intent i = new android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT);
            i.addCategory(android.content.Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
            startActivityForResult(i, PICK_FILE);
        });

        btnClear.setOnClickListener(v -> txtResult.setText(""));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) loadAndShow(uri);
        }
    }

    private void loadAndShow(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            byte[] dump = new byte[is.available()];
            is.read(dump);
            is.close();

            if (dump.length < 16) {
                txtResult.setText("檔案太小");
                return;
            }

            SpannableStringBuilder ssb = new SpannableStringBuilder();
            ssb.append("Dump 色彩高亮檢視 (").append(String.valueOf(dump.length)).append(" bytes)\n\n");

            int sectorCount = dump.length >= 4096 ? 40 : (dump.length >= 1024 ? 16 : 1);
            int offset = 0;
            int blockCount = 4;

            for (int sector = 0; sector < sectorCount; offset += blockCount * 16, sector++) {
                blockCount = (sector < 32) ? 4 : 16;

                String hdr = String.format("─ Sector %02d ─\n", sector);
                int hdrStart = ssb.length();
                ssb.append(hdr);
                ssb.setSpan(new ForegroundColorSpan(Color.rgb(0, 150, 136)), hdrStart, ssb.length(), 0);

                for (int b = 0; b < blockCount && offset + b * 16 < dump.length; b++) {
                    int bo = offset + b * 16;
                    int remain = Math.min(16, dump.length - bo);
                    if (remain <= 0) break;

                    int blockType;
                    String label;
                    int color;

                    if (sector == 0 && b == 0) {
                        blockType = 0;
                        label = "MFR";
                        color = COLOR_MFR;
                    } else if (b == blockCount - 1) {
                        blockType = 2;
                        label = "TRL";
                        color = COLOR_TRAILER;
                    } else {
                        blockType = 1;
                        label = "DAT";
                        color = COLOR_DATA;
                    }

                    int start = ssb.length();
                    ssb.append(String.format("  %s %s  ", label, getBlockLabel(sector, b, blockType)));

                    for (int i = 0; i < remain; i++) {
                        ssb.append(String.format("%02X ", dump[bo + i]));
                    }
                    ssb.append("\n");
                    ssb.setSpan(new ForegroundColorSpan(color), start, ssb.length(), 0);
                }
            }

            if (dump.length > 4096) {
                ssb.append("\n(限 40 sectors, 完整 ").append(String.valueOf(dump.length)).append(" bytes)\n");
            }

            ssb.append("\n圖例:\n");
            appendColorLegend(ssb, "MFR", "製造商區塊", COLOR_MFR);
            appendColorLegend(ssb, "DAT", "資料區塊", COLOR_DATA);
            appendColorLegend(ssb, "TRL", "Sector Trailer", COLOR_TRAILER);
            appendColorLegend(ssb, "VAL", "數值區塊", COLOR_VALUE);
            appendColorLegend(ssb, "ADR", "位址區塊", COLOR_ADDR);

            txtResult.setText(ssb);

        } catch (Exception e) {
            txtResult.setText("讀取失敗: " + e.getMessage());
        }
    }

    private String getBlockLabel(int sector, int block, int type) {
        return String.format("S%02d B%02d", sector, block);
    }

    private void appendColorLegend(SpannableStringBuilder ssb, String tag, String desc, int color) {
        int s = ssb.length();
        ssb.append("  ").append(tag).append(" - ").append(desc).append("\n");
        ssb.setSpan(new ForegroundColorSpan(color), s, s + tag.length() + 2, 0);
    }
}
