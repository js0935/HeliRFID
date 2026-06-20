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
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class TagInfoActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;

    TextView txtUid, txtTechList, txtAtqa, txtSak, txtSakMeaning, txtHistorical;
    TextView txtNfcB, txtNfcF, txtNfcV, txtDecodedId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_info);

        txtUid = findViewById(R.id.txtTagUid);
        txtTechList = findViewById(R.id.txtTagTechList);
        txtAtqa = findViewById(R.id.txtAtqa);
        txtSak = findViewById(R.id.txtSak);
        txtSakMeaning = findViewById(R.id.txtSakMeaning);
        txtHistorical = findViewById(R.id.txtHistorical);
        txtNfcB = findViewById(R.id.txtNfcB);
        txtNfcF = findViewById(R.id.txtNfcF);
        txtNfcV = findViewById(R.id.txtNfcV);
        txtDecodedId = findViewById(R.id.txtDecodedId);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        Intent intent = new Intent(this, getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? PendingIntent.FLAG_MUTABLE
                : PendingIntent.FLAG_UPDATE_CURRENT;

        pendingIntent = PendingIntent.getActivity(this, 0, intent, flags);

        Tag tag = getIntent().getParcelableExtra("tag");
        if (tag != null) {
            analyzeTag(tag);
        }
    }

    private void analyzeTag(Tag tag) {
        byte[] uidBytes = tag.getId();
        String uid = NFCReader.getUID(tag);
        txtUid.setText("UID: " + uid + " (" + uidBytes.length + " bytes)");

        StringBuilder techSb = new StringBuilder("支援技術:\n");
        for (String t : tag.getTechList()) {
            techSb.append("  • ").append(t.substring(t.lastIndexOf('.') + 1)).append("\n");
        }
        txtTechList.setText(techSb.toString().trim());

        // NFC-A / ATQA / SAK
        NfcA nfcA = NfcA.get(tag);
        if (nfcA != null) {
            try {
                nfcA.connect();
                byte[] atqa = nfcA.getAtqa();
                short sak = nfcA.getSak();
                nfcA.close();

                txtAtqa.setText("ATQA: " + bytesToHex(atqa) + " (" + atqa.length + " bytes)");
                txtSak.setText("SAK: 0x" + String.format("%02X", sak & 0xFF) + " (" + (sak & 0xFF) + ")");

                txtSakMeaning.setText(decodeSak(sak & 0xFF));
            } catch (Exception e) {
                txtAtqa.setText("ATQA: 讀取失敗");
                txtSak.setText("SAK: 讀取失敗");
                txtSakMeaning.setText("");
            }
        } else {
            txtAtqa.setText("ATQA: 不支援 NFC-A");
            txtSak.setText("SAK: 不支援 NFC-A");
            txtSakMeaning.setText("");
        }

        // ISO-DEP / Historical Bytes
        IsoDep isoDep = IsoDep.get(tag);
        if (isoDep != null) {
            try {
                isoDep.connect();
                byte[] hist = isoDep.getHistoricalBytes();
                isoDep.close();

                if (hist != null && hist.length > 0) {
                    txtHistorical.setText("Historical Bytes:\n" + bytesToHex(hist));
                } else {
                    txtHistorical.setText("Historical Bytes: 無");
                }
            } catch (Exception e) {
                txtHistorical.setText("Historical Bytes: 讀取失敗");
            }
        } else {
            txtHistorical.setText("Historical Bytes: 不支援 ISO-DEP");
        }

        // NFC-B
        NfcB nfcB = NfcB.get(tag);
        if (nfcB != null) {
            try {
                nfcB.connect();
                byte[] appData = nfcB.getApplicationData();
                byte[] protoInfo = nfcB.getProtocolInfo();
                nfcB.close();
                txtNfcB.setText("Application Data: " + bytesToHex(appData)
                        + "\nProtocol Info: " + bytesToHex(protoInfo));
            } catch (Exception e) {
                txtNfcB.setText("NFC-B: 讀取失敗");
            }
        } else {
            txtNfcB.setText("NFC-B: 不支援");
        }

        // NFC-F (FeliCa)
        NfcF nfcF = NfcF.get(tag);
        if (nfcF != null) {
            try {
                nfcF.connect();
                byte[] idm = nfcF.getSystemCode();
                nfcF.close();
                txtNfcF.setText("System Code: 0x" + bytesToHex(idm));
            } catch (Exception e) {
                txtNfcF.setText("NFC-F: 讀取失敗");
            }
        } else {
            txtNfcF.setText("NFC-F: 不支援");
        }

        // NFC-V
        NfcV nfcV = NfcV.get(tag);
        if (nfcV != null) {
            try {
                nfcV.connect();
                byte dsfId = nfcV.getDsfId();
                byte respFlags = nfcV.getResponseFlags();
                nfcV.close();
                txtNfcV.setText("DSF ID: 0x" + String.format("%02X", dsfId)
                        + "\nResponse Flags: 0x" + String.format("%02X", respFlags));
            } catch (Exception e) {
                txtNfcV.setText("NFC-V: 讀取失敗");
            }
        } else {
            txtNfcV.setText("NFC-V: 不支援");
        }

        // Decoded ID info
        StringBuilder decoded = new StringBuilder();
        String uidStr = NFCReader.getUID(tag);
        String card10 = Converter.decimal10(tag.getId());
        String card8 = Converter.decimal8(tag.getId());
        decoded.append("10碼卡號: ").append(card10).append("\n");
        decoded.append("8碼卡號: ").append(card8).append("\n");
        decoded.append("UID 長度: ").append(uidBytes.length).append(" bytes\n");
        if (uidBytes.length == 4) {
            decoded.append("UID 類型: UID-4 (單一大小)\n");
        } else if (uidBytes.length == 7) {
            decoded.append("UID 類型: UID-7 (雙重大小)\n");
        } else if (uidBytes.length == 10) {
            decoded.append("UID 類型: UID-10 (三重大小)\n");
        }
        decoded.append("\nWiegand 解碼:\n");
        decoded.append("  W26: ").append(Wiegand.wiegand26(card10)).append("\n");
        decoded.append("  W32: ").append(Wiegand.wiegand32(card10)).append("\n");
        decoded.append("  W34: ").append(Wiegand.wiegand34(card10)).append("\n");
        decoded.append("  W37: ").append(Wiegand.wiegand37(card10)).append("\n");
        decoded.append("  W40: ").append(Wiegand.wiegand40(card10)).append("\n");
        txtDecodedId.setText(decoded.toString().trim());
    }

    private String bytesToHex(byte[] bytes) {
        if (bytes == null) return "null";
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    private String decodeSak(int sak) {
        StringBuilder sb = new StringBuilder();
        if ((sak & 0x01) != 0) sb.append("• 支援 ISO 14443-4 通訊\n");
        if ((sak & 0x04) != 0) sb.append("• 支援 MIFARE Classic 模擬\n");
        if ((sak & 0x08) != 0) sb.append("• 支援 MIFARE 更高容量\n");
        if ((sak & 0x10) != 0) sb.append("• UID 長度為 4 bytes\n");
        if ((sak & 0x20) != 0) sb.append("• UID 長度為 7 bytes\n");
        if ((sak & 0x40) != 0) sb.append("• 支援 ISO 14443-3 通訊\n");
        if ((sak & 0x80) != 0) sb.append("• 不支援後續位元組 (ready only)\n");

        if ((sak & 0x00) == 0) sb.append("• MIFARE 1K 或 Ultralight\n");
        if (sak == 0x08) sb.append("• 推測: MIFARE Classic 1K\n");
        if (sak == 0x09) sb.append("• 推測: MIFARE Classic Mini\n");
        if (sak == 0x18) sb.append("• 推測: MIFARE Classic 4K\n");
        if (sak == 0x20) sb.append("• 推測: MIFARE Ultralight\n");
        if (sak == 0x38) sb.append("• 推測: MIFARE Plus 2K / 4K\n");
        if (sak == 0x50) sb.append("• 推測: NTAG 系列\n");

        return sb.toString().trim();
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
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) {
                analyzeTag(tag);
                Toast.makeText(this, "已分析新卡片", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
