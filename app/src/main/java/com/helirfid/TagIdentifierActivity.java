package com.helirfid;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class TagIdentifierActivity extends BaseNfcActivity {

    private TextView txtStatus, txtInfo;
    private Button btnRead;
    private Tag currentTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_identifier);

        txtStatus = findViewById(R.id.txtIdentStatus);
        txtInfo = findViewById(R.id.txtIdentInfo);
        btnRead = findViewById(R.id.btnIdentRead);
        btnRead.setOnClickListener(v -> identifyTag());

        Tag tag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) currentTag = tag;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            currentTag = tag;
            txtStatus.setText("已侦测: " + Converter.hex(tag.getId()));
        }
    }

    private void identifyTag() {
        if (currentTag == null) {
            Toast.makeText(this, "请先扫描 NFC 卡片", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(() -> {
            StringBuilder sb = new StringBuilder();
            Tag tag = currentTag;
            String uid = Converter.hex(tag.getId());
            sb.append("UID: ").append(uid).append("\n\n");

            sb.append("支援技术: ");
            for (String t : tag.getTechList()) {
                sb.append(t.substring(t.lastIndexOf('.') + 1)).append(" ");
            }
            sb.append("\n\n");

            NfcA nfca = NfcA.get(tag);
            if (nfca != null) {
                try {
                    nfca.connect();
                    byte[] atqaBytes = nfca.getAtqa();
                    short sak = nfca.getSak();
                    int atqa = 0;
                    if (atqaBytes != null && atqaBytes.length >= 2) {
                        atqa = ((atqaBytes[0] & 0xFF) << 8) | (atqaBytes[1] & 0xFF);
                    }
                    sb.append("ATQA: ").append(String.format("0x%04X", atqa)).append("\n");
                    sb.append("SAK:  ").append(String.format("0x%02X", sak)).append("\n");
                    nfca.close();

                    String type = decodeSak(sak);
                    sb.append("类型: ").append(type).append("\n\n");

                    if (type.contains("DESFire") || type.contains("Plus")) {
                        IsoDep iso = IsoDep.get(tag);
                        if (iso != null) {
                            try {
                                iso.connect();
                                byte[] resp = iso.transceive(new byte[]{(byte)0x90, 0x60, 0x00, 0x00, 0x00});
                                if (resp != null && resp.length >= 8) {
                                    sb.append("ISO-DEP 版本: ");
                                    for (int i = 0; i < Math.min(resp.length - 2, 8); i++) {
                                        sb.append(String.format("%02X ", resp[i]));
                                    }
                                    sb.append("\n");
                                }
                                iso.close();
                            } catch (Exception e) {
                                sb.append("ISO-DEP 查询失败\n");
                            }
                        }
                    }
                } catch (Exception e) {
                    sb.append("NfcA 错误: ").append(e.getMessage()).append("\n");
                }
            }

            MifareClassic mfc = MifareClassic.get(tag);
            if (mfc != null) {
                try {
                    mfc.connect();
                    sb.append("MIFARE Classic:\n");
                    sb.append("  容量: ").append(mfc.getSize()).append(" bytes\n");
                    sb.append("  磁区: ").append(mfc.getSectorCount()).append("\n");
                    sb.append("  区块: ").append(mfc.getBlockCount()).append("\n");
                    mfc.close();
                } catch (Exception e) {
                    sb.append("MFC 错误: ").append(e.getMessage()).append("\n");
                }
            }

            MifareUltralight mu = MifareUltralight.get(tag);
            if (mu != null) {
                try {
                    mu.connect();
                    int type = mu.getType();
                    sb.append("NTAG/Ultralight:\n");
                    String[] types = {"未知", "Ultralight", "Ultralight C", "NTAG 213", "NTAG 215", "NTAG 216", "NTAG I2C"};
                    String typeName = (type >= 0 && type < types.length) ? types[type] : "类型 " + type;
                    sb.append("  型号: ").append(typeName).append("\n");
                    mu.close();
                } catch (Exception e) {
                    sb.append("MUL 错误: ").append(e.getMessage()).append("\n");
                }
            }

            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                sb.append("NDEF: 可用 (最大 ").append(ndef.getMaxSize()).append(" bytes)\n");
            }

            sb.append("\n--- 识别完成 ---");
            final String res = sb.toString();
            runOnUiThread(() -> txtInfo.setText(res));
        }).start();
    }

    private String decodeSak(short sak) {
        switch (sak & 0xFF) {
            case 0x08: return "MIFARE Classic 1K";
            case 0x18: return "MIFARE Classic 4K";
            case 0x20: return "MIFARE DESFire (EV1/EV2/Light)";
            case 0x38: return "MIFARE Plus 2K";
            case 0x28: return "MIFARE Plus 4K";
            case 0x00: return "NTAG / Ultralight / ISO 14443-4";
            default:   return String.format("未知 (SAK=0x%02X)", sak & 0xFF);
        }
    }
}
