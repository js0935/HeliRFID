/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.0.1
 */
package com.helirfid;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ApduConsoleActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private Tag currentTag;

    EditText editApduCommand;
    TextView txtApduResult, txtCardInfo;
    Button btnSendApdu, btnClearHistory;

    StringBuilder resultHistory = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apdu_console);

        txtCardInfo = findViewById(R.id.txtApduCardInfo);
        editApduCommand = findViewById(R.id.editApduCommand);
        txtApduResult = findViewById(R.id.txtApduResult);
        btnSendApdu = findViewById(R.id.btnSendApdu);
        btnClearHistory = findViewById(R.id.btnClearApduHistory);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        Intent intent = new Intent(this, getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? PendingIntent.FLAG_MUTABLE
                : PendingIntent.FLAG_UPDATE_CURRENT;
        pendingIntent = PendingIntent.getActivity(this, 0, intent, flags);

        btnClearHistory.setOnClickListener(v -> {
            resultHistory.setLength(0);
            txtApduResult.setText("");
        });

        btnSendApdu.setOnClickListener(v -> {
            if (currentTag == null) {
                Toast.makeText(this, "請先掃描卡片", Toast.LENGTH_SHORT).show();
                return;
            }
            String hex = editApduCommand.getText().toString().trim().replace(" ", "");
            if (TextUtils.isEmpty(hex)) {
                Toast.makeText(this, "請輸入 APDU 指令 (Hex)", Toast.LENGTH_SHORT).show();
                return;
            }
            sendApdu(hex);
        });
    }

    private void sendApdu(String hexCmd) {
        new Thread(() -> {
            try {
                byte[] cmd = hexStringToByteArray(hexCmd);
                byte[] response = null;
                String tech = "";

                IsoDep isoDep = IsoDep.get(currentTag);
                if (isoDep != null) {
                    try {
                        isoDep.connect();
                        isoDep.setTimeout(5000);
                        response = isoDep.transceive(cmd);
                        isoDep.close();
                        tech = "IsoDep";
                    } catch (Exception e) {
                        if (isoDep.isConnected()) isoDep.close();
                    }
                }

                if (response == null) {
                    NfcA nfcA = NfcA.get(currentTag);
                    if (nfcA != null) {
                        try {
                            nfcA.connect();
                            nfcA.setTimeout(5000);
                            response = nfcA.transceive(cmd);
                            nfcA.close();
                            tech = "NfcA";
                        } catch (Exception e) {
                            if (nfcA.isConnected()) nfcA.close();
                        }
                    }
                }

                if (response == null) {
                    NfcB nfcB = NfcB.get(currentTag);
                    if (nfcB != null) {
                        try {
                            nfcB.connect();
                            response = nfcB.transceive(cmd);
                            nfcB.close();
                            tech = "NfcB";
                        } catch (Exception e) {
                            if (nfcB.isConnected()) nfcB.close();
                        }
                    }
                }

                if (response == null) {
                    NfcF nfcF = NfcF.get(currentTag);
                    if (nfcF != null) {
                        try {
                            nfcF.connect();
                            nfcF.setTimeout(5000);
                            response = nfcF.transceive(cmd);
                            nfcF.close();
                            tech = "NfcF";
                        } catch (Exception e) {
                            if (nfcF.isConnected()) nfcF.close();
                        }
                    }
                }

                if (response == null) {
                    NfcV nfcV = NfcV.get(currentTag);
                    if (nfcV != null) {
                        try {
                            nfcV.connect();
                            response = nfcV.transceive(cmd);
                            nfcV.close();
                            tech = "NfcV";
                        } catch (Exception e) {
                            if (nfcV.isConnected()) nfcV.close();
                        }
                    }
                }

                final String techName = tech;
                final byte[] resp = response;

                runOnUiThread(() -> {
                    if (resp == null) {
                        appendResult("無法發送指令 (不支援此技術)");
                        return;
                    }

                    StringBuilder sb = new StringBuilder();
                    for (byte b : resp) sb.append(String.format("%02X ", b));

                    String sw = "";
                    if (resp.length >= 2) {
                        sw = String.format("SW=%02X%02X", resp[resp.length-2], resp[resp.length-1]);
                    }

                    String ascii = "";
                    for (byte b : resp) {
                        ascii += (b >= 0x20 && b <= 0x7E) ? (char)b : '.';
                    }

                    appendResult("→ " + hexCmd + "\n" +
                            "← [" + techName + "] " + sb.toString().trim() + "\n" +
                            "  " + sw + "  ASCII: " + ascii + "\n");
                });

            } catch (Exception e) {
                runOnUiThread(() -> appendResult("錯誤: " + e.getMessage() + "\n"));
            }
        }).start();
    }

    private void appendResult(String text) {
        resultHistory.append(text);
        txtApduResult.setText(resultHistory.toString());

        int scroll = txtApduResult.getLayout() != null
                ? txtApduResult.getLayout().getLineTop(txtApduResult.getLineCount()) - txtApduResult.getHeight()
                : 0;
        if (scroll > 0) txtApduResult.scrollTo(0, scroll);
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
            StringBuilder info = new StringBuilder("卡片已偵測\nUID: ");
            for (byte b : currentTag.getId()) info.append(String.format("%02X", b));
            info.append("\n技術: ");
            for (String t : currentTag.getTechList()) {
                String s = t.substring(t.lastIndexOf('.') + 1);
                info.append(s).append(" ");
            }
            txtCardInfo.setText(info.toString());
            Toast.makeText(this, "卡片已偵測", Toast.LENGTH_SHORT).show();
        }
    }
}
