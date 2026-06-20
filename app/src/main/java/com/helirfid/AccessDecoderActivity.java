package com.helirfid;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;

public class AccessDecoderActivity extends AppCompatActivity {

    TextView txtResult;
    Button btnLoadDump, btnClear;

    private static final int PICK_DUMP = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access_decoder);

        txtResult = findViewById(R.id.txtAccessDecoderResult);
        btnLoadDump = findViewById(R.id.btnLoadDump);
        btnClear = findViewById(R.id.btnClearAccess);

        btnLoadDump.setOnClickListener(v -> {
            android.content.Intent i = new android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT);
            i.addCategory(android.content.Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
            startActivityForResult(i, PICK_DUMP);
        });

        btnClear.setOnClickListener(v -> txtResult.setText(""));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_DUMP && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) loadAndDecode(uri);
        }
    }

    private void loadAndDecode(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            byte[] dump = new byte[is.available()];
            is.read(dump);
            is.close();

            if (dump.length < 1024) {
                txtResult.setText("Dump 太小 (需要至少 1024 bytes)");
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("存取條件解碼 (").append(dump.length >= 4096 ? "MIFARE 4K" : "MIFARE 1K").append(")\n\n");

            int sectorCount = dump.length >= 4096 ? 40 : 16;
            int blockSize = 16;
            int sectorSize = sectorCount > 32 ? 16 * 16 : 4 * 16; // 4K sectors 32-39 have 16 blocks

            int currentOffset = 0;
            for (int sector = 0; sector < sectorCount; sector++) {
                int blocksInSector = (sector < 32) ? 4 : 16;
                int trailerBlock = currentOffset + (blocksInSector - 1) * blockSize;

                if (trailerBlock + blockSize <= dump.length) {
                    byte[] trailer = new byte[16];
                    System.arraycopy(dump, trailerBlock, trailer, 0, 16);
                    sb.append(decodeSector(sector, trailer));
                }
                currentOffset += blocksInSector * blockSize;
            }

            txtResult.setText(sb.toString());

        } catch (Exception e) {
            txtResult.setText("讀取失敗: " + e.getMessage());
        }
    }

    private String decodeSector(int sector, byte[] trailer) {
        StringBuilder sb = new StringBuilder();
        sb.append("Sector ").append(sector).append(":\n");

        byte[] acBytes = new byte[6];
        System.arraycopy(trailer, 6, acBytes, 0, 6);

        byte b6 = acBytes[0], b7 = acBytes[1], b8 = acBytes[2];

        int[][] bits = {
            {(b6 >> 6) & 1, (b6 >> 7) & 1, (b7 >> 6) & 1},
            {(b8) & 1, (b8 >> 5) & 1, (b7) & 1},
            {(b8 >> 1) & 1, (b8 >> 2) & 1, (b7 >> 1) & 1},
            {(b8 >> 3) & 1, (b8 >> 4) & 1, (b7 >> 3) & 1}
        };

        String[] names = {"Block 0", "Block 1", "Block 2", "Trailer"};
        for (int i = 0; i < 4; i++) {
            sb.append("  ").append(names[i]).append(": C1=").append(bits[i][0])
              .append(" C2=").append(bits[i][1]).append(" C3=").append(bits[i][2])
              .append(" → ");
            if (i < 3) sb.append(dataBlockAccess(bits[i]));
            else sb.append(trailerAccess(bits[i]));
            sb.append("\n");
        }

        sb.append("  KeyA: ");
        for (int i = 0; i < 6; i++) sb.append(String.format("%02X ", trailer[i]));
        sb.append("\n");

        sb.append("  KeyB: ");
        for (int i = 10; i < 16; i++) sb.append(String.format("%02X ", trailer[i]));
        sb.append("\n\n");

        return sb.toString();
    }

    private String dataBlockAccess(int[] c) {
        if (c[0]==0 && c[1]==0 && c[2]==0) return "KeyA/B 讀/寫/增/減";
        if (c[0]==0 && c[1]==1 && c[2]==0) return "KeyA/B 讀, 不可寫/增/減";
        if (c[0]==1 && c[1]==0 && c[2]==0) return "KeyA/B 讀, KeyB 寫, 不可增/減";
        if (c[0]==1 && c[1]==1 && c[2]==0) return "KeyA/B 讀, KeyB 寫/增, KeyA/B 減";
        if (c[0]==0 && c[1]==0 && c[2]==1) return "KeyA/B 寫/減, 不可讀/增";
        if (c[0]==0 && c[1]==1 && c[2]==1) return "KeyB 讀/寫, 不可增/減";
        if (c[0]==1 && c[1]==0 && c[2]==1) return "KeyB 讀, 不可寫/增/減";
        if (c[0]==1 && c[1]==1 && c[2]==1) return "不可存取";
        return "未知";
    }

    private String trailerAccess(int[] c) {
        if (c[0]==0 && c[1]==0 && c[2]==0) return "KeyA/B 讀/寫 AC, KeyA/B 讀 KeyB";
        if (c[0]==0 && c[1]==1 && c[2]==0) return "KeyA 不可讀, KeyA/B 讀 KeyB";
        if (c[0]==1 && c[1]==0 && c[2]==0) return "KeyA 不可讀, KeyB 讀 KeyB/AC";
        if (c[0]==1 && c[1]==1 && c[2]==0) return "KeyA 不可讀/寫, KeyB 讀 KeyB/AC";
        if (c[0]==0 && c[1]==0 && c[2]==1) return "KeyA/B 讀/寫 AC, KeyA 讀 KeyB";
        if (c[0]==0 && c[1]==1 && c[2]==1) return "KeyA/B 寫 AC, KeyB 讀 KeyB";
        if (c[0]==1 && c[1]==0 && c[2]==1) return "KeyB 讀 AC/KeyB, KeyA 不可寫";
        if (c[0]==1 && c[1]==1 && c[2]==1) return "不可存取";
        return "未知";
    }
}
