package com.helirfid;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;

public class DumpAsciiActivity extends BaseNfcActivity {

    TextView txtResult;
    Button btnLoad, btnClear;

    private static final int PICK_FILE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dump_ascii);

        txtResult = findViewById(R.id.txtDumpAsciiResult);
        btnLoad = findViewById(R.id.btnLoadDumpAscii);
        btnClear = findViewById(R.id.btnClearAscii);

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

            StringBuilder sb = new StringBuilder();
            sb.append("Dump ASCII 檢視 (").append(dump.length).append(" bytes)\n\n");

            int lines = (dump.length + 15) / 16;
            for (int line = 0; line < lines && line < 256; line++) {
                int offset = line * 16;
                sb.append(String.format("%04X  ", offset));

                StringBuilder hex = new StringBuilder();
                StringBuilder ascii = new StringBuilder();

                for (int i = 0; i < 16 && offset + i < dump.length; i++) {
                    byte b = dump[offset + i];
                    hex.append(String.format("%02X ", b));
                    if (b >= 0x20 && b < 0x7F) ascii.append((char) b);
                    else ascii.append('.');
                }

                sb.append(String.format("%-48s  %s\n", hex.toString(), ascii.toString()));
            }

            if (dump.length > 4096) {
                sb.append("\n... (僅顯示前 256 行, 共 ").append(lines).append(" 行)");
            }

            txtResult.setText(sb.toString());

        } catch (Exception e) {
            txtResult.setText("讀取失敗: " + e.getMessage());
        }
    }
}
