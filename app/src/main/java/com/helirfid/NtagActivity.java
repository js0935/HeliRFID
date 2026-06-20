package com.helirfid;

import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class NtagActivity extends AppCompatActivity {

    TextView txtResult;
    Button btnRead, btnWriteNdef, btnClear;
    EditText editNdefPayload;
    NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ntag);

        txtResult = findViewById(R.id.txtNtagResult);
        btnRead = findViewById(R.id.btnNtagRead);
        btnWriteNdef = findViewById(R.id.btnNtagWriteNdef);
        btnClear = findViewById(R.id.btnNtagClear);
        editNdefPayload = findViewById(R.id.editNdefPayload);

        NfcManager nfcManager = (NfcManager) getSystemService(NFC_SERVICE);
        nfcAdapter = nfcManager.getDefaultAdapter();

        btnClear.setOnClickListener(v -> txtResult.setText(""));

        btnRead.setOnClickListener(v -> txtResult.setText("請將 NTAG 卡片靠近手機背面"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            android.app.PendingIntent pi = android.app.PendingIntent.getActivity(this, 0,
                    new android.content.Intent(this, getClass())
                            .addFlags(android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP),
                    android.app.PendingIntent.FLAG_MUTABLE);
            nfcAdapter.enableForegroundDispatch(this, pi, null, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) handleTag(tag);
        }
    }

    private void handleTag(Tag tag) {
        MifareUltralight ultralight = MifareUltralight.get(tag);
        if (ultralight == null) {
            txtResult.setText("此卡片不是 NTAG/Ultralight 類型");
            return;
        }

        try {
            ultralight.connect();

            StringBuilder sb = new StringBuilder();
            sb.append("NTAG / Ultralight 資訊\n\n");

            byte[] uid = tag.getId();
            sb.append("UID: ");
            for (byte b : uid) sb.append(String.format("%02X ", b));
            sb.append("\n\n");

            try {
                byte[] version = ultralight.transceive(new byte[]{(byte) 0x60});
                sb.append("GetVersion: ");
                for (byte b : version) sb.append(String.format("%02X ", b));
                sb.append("\n");

                if (version.length >= 8) {
                    sb.append("  廠商: ").append(String.format("%02X", version[0])).append("\n");
                    sb.append("  類型: ").append(String.format("%02X", version[1])).append("\n");
                    sb.append("  子類型: ").append(String.format("%02X", version[2])).append("\n");
                    sb.append("  記憶體: ").append(String.format("%d bytes", 1 << version[4])).append("\n");
                }
            } catch (Exception e) {
                sb.append("GetVersion: 不支援\n");
            }

            sb.append("\n頁面 0-15:\n");
            for (int page = 0; page < 16; page++) {
                try {
                    byte[] data = ultralight.readPages(page);
                    if (data != null && data.length == 16) {
                        sb.append(String.format("  P%02d: ", page));
                        for (byte b : data) sb.append(String.format("%02X ", b));
                        sb.append("\n");
                    }
                } catch (Exception e) {
                    break;
                }
            }

            ultralight.close();
            txtResult.setText(sb.toString());

        } catch (Exception e) {
            txtResult.setText("讀取失敗: " + e.getMessage());
        }
    }
}
