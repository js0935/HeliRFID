package com.helirfid;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EmvCardActivity extends AppCompatActivity {

    TextView txtResult, txtWalletType;
    Button btnRead, btnClear;
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    IntentFilter[] nfcFilters;
    Tag currentTag;
    private String detectedWalletType = "";

    private static final byte[] PPSE = {
            0x00, (byte)0xA4, 0x04, 0x00, 0x0E,
            0x32, 0x50, 0x41, 0x59, 0x2E, 0x53, 0x59, 0x53, 0x2E, 0x44, 0x44, 0x46, 0x30, 0x31, 0x00
    };
    private static final byte[] GPO = {
            (byte)0x80, (byte)0xA8, 0x00, 0x00, 0x02, 0x03, (byte)0x83, 0x00
    };
    private static final byte[] SELECT_AID_HEADER = {0x00, (byte)0xA4, 0x04, 0x00};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emv_card);

        txtResult = findViewById(R.id.txtEmvResult);
        txtWalletType = findViewById(R.id.txtEmvWalletType);
        btnRead = findViewById(R.id.btnEmvRead);
        btnClear = findViewById(R.id.btnEmvClear);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        Intent intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) flags |= PendingIntent.FLAG_MUTABLE;
        pendingIntent = PendingIntent.getActivity(this, 0, intent, flags);
        nfcFilters = new IntentFilter[]{
                new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED),
                new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED),
                new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED),
        };

        btnRead.setOnClickListener(v -> {
            if (currentTag != null) readCard();
            else txtResult.setText("請將信用卡靠近手機");
        });

        btnClear.setOnClickListener(v -> {
            txtResult.setText("");
            txtWalletType.setText("");
            detectedWalletType = "";
        });
    }

    private void readCard() {
        txtResult.setText("讀取中...");
        new Thread(() -> {
            try {
                IsoDep isoDep = IsoDep.get(currentTag);
                if (isoDep == null) {
                    runOnUiThread(() -> txtResult.setText("此卡片不支援 ISO-DEP (非 EMV 感應卡)"));
                    return;
                }
                isoDep.connect();
                isoDep.setTimeout(10000);

                StringBuilder sb = new StringBuilder();
                sb.append("=== EMV 感應卡讀取 ===\n\n");

                byte[] fci = selectPpse(isoDep);
                if (fci == null) {
                    fci = tryDirectSelect(isoDep);
                }
                if (fci == null) {
                    runOnUiThread(() -> txtResult.setText("無法識別此卡片 (未找到 EMV 應用程式)"));
                    isoDep.close();
                    return;
                }

                Map<Integer, byte[]> fciTlv = parseTlv(fci, 0, fci.length);
                byte[] aid = null;
                if (fciTlv.containsKey(0x84)) {
                    aid = fciTlv.get(0x84);
                    sb.append("AID: ").append(bytesToHex(aid)).append("\n");
                    sb.append("AID 名稱: ").append(getAidName(aid)).append("\n\n");
                }

                byte[] fci2 = selectAid(isoDep, aid);
                if (fci2 == null) {
                    runOnUiThread(() -> txtResult.setText("選擇應用程式失敗"));
                    isoDep.close();
                    return;
                }

                Map<Integer, byte[]> fci2Tlv = parseTlv(fci2, 0, fci2.length);
                if (fci2Tlv.containsKey(0x50)) {
                    sb.append("應用標籤: ").append(new String(fci2Tlv.get(0x50))).append("\n");
                }
                if (fci2Tlv.containsKey(0x9F12)) {
                    sb.append("慣用名稱: ").append(new String(fci2Tlv.get(0x9F12))).append("\n");
                }
                sb.append("\n");

                byte[] gpoResp = sendApdu(isoDep, GPO);
                if (gpoResp == null) {
                    sb.append("GET PROCESSING OPTIONS 失敗\n");
                } else {
                    sb.append("GPO 回應: ").append(bytesToHex(gpoResp)).append("\n\n");
                    List<int[]> aflList = parseAfl(gpoResp);
                    for (int[] afl : aflList) {
                        readRecord(isoDep, sb, afl[0], afl[1]);
                    }
                }

                isoDep.close();

                final String result = sb.toString();
                final String wallet = detectedWalletType.isEmpty() ? "" : "偵測錢包: " + detectedWalletType;
                runOnUiThread(() -> {
                    txtResult.setText(result);
                    txtWalletType.setText(wallet);
                });

            } catch (Exception e) {
                runOnUiThread(() -> txtResult.setText("錯誤: " + e.getMessage()));
            }
        }).start();
    }

    private byte[] selectPpse(IsoDep isoDep) {
        try {
            return sendApdu(isoDep, PPSE);
        } catch (Exception e) {
            return null;
        }
    }

    private byte[] tryDirectSelect(IsoDep isoDep) {
        byte[][] commonAids = {
                {(byte)0xA0, 0x00, 0x00, 0x00, 0x03, 0x10, 0x10},       // Visa
                {(byte)0xA0, 0x00, 0x00, 0x00, 0x04, 0x10, 0x10},       // Mastercard
                {(byte)0xA0, 0x00, 0x00, 0x00, 0x25, 0x01},              // Amex
                {(byte)0xA0, 0x00, 0x00, 0x00, 0x42, 0x10, 0x10},       // JCB
                {(byte)0xA0, 0x00, 0x00, 0x00, 0x65, 0x10, 0x10},       // Discover
                {(byte)0xA0, 0x00, 0x00, 0x00, 0x03, 0x20, 0x10},       // Visa Debit
                {(byte)0xA0, 0x00, 0x00, 0x00, 0x04, 0x30, 0x10},       // Maestro
                {(byte)0xA0, 0x00, 0x00, 0x00, 0x01, 0x70, 0x00},       // UnionPay
        };
        for (byte[] aid : commonAids) {
            try {
                byte[] cmd = buildSelectAid(aid);
                byte[] resp = sendApdu(isoDep, cmd);
                if (resp != null && resp.length > 2) {
                    return resp;
                }
            } catch (Exception e) { }
        }
        return null;
    }

    private byte[] selectAid(IsoDep isoDep, byte[] aid) {
        if (aid == null) return null;
        try {
            byte[] cmd = buildSelectAid(aid);
            return sendApdu(isoDep, cmd);
        } catch (Exception e) {
            return null;
        }
    }

    private byte[] buildSelectAid(byte[] aid) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(SELECT_AID_HEADER, 0, 4);
        baos.write(aid.length);
        baos.write(aid, 0, aid.length);
        baos.write(0x00);
        return baos.toByteArray();
    }

    private byte[] sendApdu(IsoDep isoDep, byte[] apdu) {
        try {
            byte[] resp = isoDep.transceive(apdu);
            if (resp.length >= 2) {
                int sw = ((resp[resp.length - 2] & 0xFF) << 8) | (resp[resp.length - 1] & 0xFF);
                if (sw == 0x9000) {
                    byte[] data = new byte[resp.length - 2];
                    System.arraycopy(resp, 0, data, 0, data.length);
                    return data;
                } else if (sw == 0x6283 || sw == 0x6285 || sw == 0x6A82 || sw == 0x6A88) {
                    return null;
                }
            }
            return resp;
        } catch (Exception e) {
            return null;
        }
    }

    private List<int[]> parseAfl(byte[] gpoResp) {
        List<int[]> aflList = new ArrayList<>();
        if (gpoResp == null || gpoResp.length < 2) return aflList;
        Map<Integer, byte[]> tlv = parseTlv(gpoResp, 0, gpoResp.length);
        byte[] aflBytes = tlv.get(0x94);
        if (aflBytes == null) {
            if (gpoResp.length > 2) aflBytes = gpoResp;
        }
        if (aflBytes == null) return aflList;
        for (int i = 0; i + 4 <= aflBytes.length; i += 4) {
            int sfi = (aflBytes[i] & 0xFF) >> 3;
            int firstRec = aflBytes[i + 1] & 0xFF;
            int lastRec = aflBytes[i + 2] & 0xFF;
            for (int r = firstRec; r <= lastRec; r++) {
                aflList.add(new int[]{sfi, r});
            }
        }
        return aflList;
    }

    private void readRecord(IsoDep isoDep, StringBuilder sb, int sfi, int recNo) {
        try {
            byte[] cmd = {
                    0x00, (byte)0xB2, (byte)recNo, (byte)((sfi << 3) | 0x04), 0x00
            };
            byte[] resp = sendApdu(isoDep, cmd);
            if (resp == null || resp.length == 0) return;

            Map<Integer, byte[]> tlv = parseTlv(resp, 0, resp.length);
            sb.append("--- 記錄 SFI=").append(sfi).append(" REC=").append(recNo).append(" ---\n");

            for (Map.Entry<Integer, byte[]> entry : tlv.entrySet()) {
                int tag = entry.getKey();
                byte[] val = entry.getValue();
                String desc = describeEmvTag(tag, val);
                if (desc != null) {
                    sb.append("  ").append(desc).append("\n");
                }
            }

            if (tlv.containsKey(0x5A)) {
                byte[] pan = tlv.get(0x5A);
                String panStr = bytesToHex(pan).replace(" ", "");
                sb.append("  PAN (卡號): ").append(maskPan(panStr)).append("\n");
            }
            if (tlv.containsKey(0x5F20)) {
                sb.append("  持卡人姓名: ").append(new String(tlv.get(0x5F20))).append("\n");
            }
            if (tlv.containsKey(0x5F24)) {
                byte[] exp = tlv.get(0x5F24);
                String expStr = bytesToHex(exp).replace(" ", "");
                if (expStr.length() >= 4) {
                    sb.append("  卡片到期日: 20").append(expStr.substring(0, 2))
                            .append("/").append(expStr.substring(2, 4)).append("\n");
                }
            }
            if (tlv.containsKey(0x5F28)) {
                byte[] issue = tlv.get(0x5F28);
                String issStr = bytesToHex(issue).replace(" ", "");
                if (issStr.length() >= 4) {
                    sb.append("  卡片生效日: 20").append(issStr.substring(0, 2))
                            .append("/").append(issStr.substring(2, 4)).append("\n");
                }
            }
            if (tlv.containsKey(0x9F17)) {
                sb.append("  PIN 嘗試剩餘: ").append((tlv.get(0x9F17)[0] & 0xFF)).append("\n");
            }
            if (tlv.containsKey(0x9F6E)) {
                byte[] ff = tlv.get(0x9F6E);
                String ffHex = bytesToHex(ff).replace(" ", "");
                String walletLabel = detectWallet(ff, tlv);
                sb.append("  錢包類型: ").append(walletLabel).append(" (9F6E: ").append(ffHex).append(")\n");
                detectedWalletType = walletLabel;
            }
            if (tlv.containsKey(0x9F19)) {
                byte[] tr = tlv.get(0x9F19);
                sb.append("  Token Requestor ID: ").append(bytesToHex(tr).replace(" ", "")).append("\n");
            }
            if (tlv.containsKey(0x9F07)) {
                byte[] auc = tlv.get(0x9F07);
                sb.append("  應用程式使用控制 (9F07): ").append(bytesToHex(auc)).append("\n");
            }
            if (tlv.containsKey(0x84)) {
                byte[] df = tlv.get(0x84);
                sb.append("  DF Name (AID): ").append(bytesToHex(df)).append("\n");
            }
            sb.append("\n");
        } catch (Exception e) {
            sb.append("  讀取記錄 SFI=").append(sfi).append(" REC=").append(recNo)
                    .append(" 失敗: ").append(e.getMessage()).append("\n\n");
        }
    }

    private String detectWallet(byte[] formFactor, Map<Integer, byte[]> allTags) {
        if (formFactor == null || formFactor.length == 0) return "實體卡片";
        int val = formFactor[0] & 0xFF;
        switch (val) {
            case 0x10: return "Google Wallet";
            case 0x20: return "Apple Pay";
            case 0x30:
                if (allTags.containsKey(0x9F19)) {
                    byte[] tr = allTags.get(0x9F19);
                    if (tr != null && tr.length > 0) {
                        String trHex = bytesToHex(tr).replace(" ", "");
                        if ("000001".equals(trHex) || "00000001".equals(trHex)) return "Samsung Pay";
                    }
                }
                return "Apple Pay";
            case 0x01:
            case 0x00: return "實體卡片";
            default: return "未知 (" + String.format("0x%02X", val) + ")";
        }
    }

    private String describeEmvTag(int tag, byte[] val) {
        switch (tag) {
            case 0x4F: return "AID: " + bytesToHex(val) + " (" + getAidName(val) + ")";
            case 0x50: return "應用標籤: " + new String(val);
            case 0x57: return "Track 2 等效資料: " + bytesToHex(val);
            case 0x5A: return null;
            case 0x5F20: return null;
            case 0x5F24: return null;
            case 0x5F28: return null;
            case 0x9F12: return "慣用名稱: " + new String(val);
            case 0x9F11: return "發行代碼 (Issuer Code): " + bytesToHex(val);
            case 0x9F17: return null;
            case 0x9F26: return "應用交易計數器 (ATC): " + bytesToHex(val);
            case 0x9F36: return "ATC (2字節): " + bytesToHex(val);
            case 0x9F37: return "不可預測數值 (UN): " + bytesToHex(val);
            case 0x9F6E: return null;
            case 0x9F6C: return "卡片類型 (Card Type): " + bytesToHex(val);
            case 0x9F19: return null;
            case 0x9F07: return null;
            case 0x82: return "應用程式互動設定檔 (AIP): " + bytesToHex(val);
            case 0x84: return null;
            case 0x8A: return "授權回覆碼: " + bytesToHex(val);
            case 0x9F10: return "卡片持有者驗證方法 (CVM): " + bytesToHex(val);
            case 0x9F1A: return "終端國家代碼: " + bytesToHex(val);
            case 0x9F1E: return "終端介面裝置序號 (IFD): " + bytesToHex(val);
            case 0x9F0D: return "發行機構動作代碼 - 預設 (ISS Action): " + bytesToHex(val);
            case 0x9F0E: return "發行機構動作代碼 - 拒絕 (ICC Action): " + bytesToHex(val);
            case 0x9F0F: return "發行機構動作代碼 - 線上 (AID Opt): " + bytesToHex(val);
            case 0x94: return "應用檔案定位器 (AFL): " + bytesToHex(val);
            case 0x71: return "發行機構指令 (ISS Script): " + bytesToHex(val);
            case 0x72: return "卡片指令 (ICC Script): " + bytesToHex(val);
            case 0x9F42: return "應用貨幣代碼: " + bytesToHex(val);
            case 0x5F34: return "應用貨幣指數: " + bytesToHex(val);
            case 0x9F44: return "應用貨幣指數/小數點: " + bytesToHex(val);
            case 0x5F25: return "應用生效日: " + bytesToHex(val);
            case 0x5F30: return "服務代碼: " + bytesToHex(val);
            case 0x9F05: return "終端應用版本號: " + bytesToHex(val);
            case 0x9F06: return "終端 AID: " + bytesToHex(val);
            case 0x9F09: return "應用版本號: " + bytesToHex(val);
            case 0x9F34: return "卡片持有者驗證 (CVM) 結果: " + bytesToHex(val);
            case 0x9F35: return "終端類型: " + bytesToHex(val);
            case 0x9F38: return "處理選項資料物件清單 (PDOL): " + bytesToHex(val);
            case 0x9F39: return "點子帳戶類型: " + bytesToHex(val);
            case 0x9F40: return "其他金額: " + bytesToHex(val);
            case 0x9F41: return "交易序號計數器: " + bytesToHex(val);
            case 0x9F53: return "交易限額: " + bytesToHex(val);
            case 0x9F66: return "終端交易屬性: " + bytesToHex(val);
            case 0x9F7C: return "客戶專屬資料: " + bytesToHex(val);
            case 0xDF01: return "EMV 診斷: " + bytesToHex(val);
            default: return "Tag 0x" + String.format("%04X", tag) + ": " + bytesToHex(val) + " (" + getPossibleTagName(tag) + ")";
        }
    }

    private String getPossibleTagName(int tag) {
        switch (tag) {
            case 0x42: return "發行機構識別碼 (IIN)";
            case 0x4E: return "商家姓名";
            case 0x4C: return "交易貨幣代碼";
            case 0x5F50: return "發行機構 URL";
            case 0x5F51: return "發行機構電話";
            case 0x5F52: return "發行機構 Email";
            case 0x9F19: return "Token Requestor ID";
            case 0x9F07: return "應用程式使用控制 (AUC)";
            case 0x9F6E: return "Form Factor Indicator";
            case 0x61: return "FCI 範本";
            case 0x6F: return "FCI 範本";
            case 0x84: return "DF Name (AID)";
            case 0x87: return "應用程式優先順序指標";
            case 0x9F2D: return "終端 PIN 限額";
            case 0x9F2E: return "積分帳戶限額";
            case 0x9F2F: return "現金帳戶限額";
            default: return "";
        }
    }

    private String maskPan(String pan) {
        if (pan.length() <= 6) return pan;
        String first6 = pan.substring(0, 6);
        String last4 = pan.substring(pan.length() - 4);
        int masked = pan.length() - 10;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < masked; i++) sb.append("*");
        return first6 + sb.toString() + last4 + " (長度: " + pan.length() + ")";
    }

    private String getAidName(byte[] aid) {
        if (aid == null) return "未知";
        String hex = bytesToHex(aid).replace(" ", "");
        if (hex.startsWith("A000000003")) return "Visa";
        if (hex.startsWith("A000000004")) return "Mastercard";
        if (hex.startsWith("A000000025")) return "American Express";
        if (hex.startsWith("A000000042")) return "JCB";
        if (hex.startsWith("A000000065")) return "Discover";
        if (hex.startsWith("A00000000320")) return "Visa Debit";
        if (hex.startsWith("A00000000430")) return "Maestro";
        if (hex.startsWith("A00000000170")) return "UnionPay";
        return "未知 (" + hex + ")";
    }

    private Map<Integer, byte[]> parseTlv(byte[] data, int offset, int length) {
        Map<Integer, byte[]> map = new LinkedHashMap<>();
        int pos = offset;
        int end = offset + length;
        while (pos < end - 1) {
            int tag = data[pos] & 0xFF;
            pos++;
            if ((tag & 0x1F) == 0x1F && pos < end) {
                tag = (tag << 8) | (data[pos] & 0xFF);
                pos++;
            }
            if (pos >= end) break;
            int valLen = data[pos] & 0xFF;
            pos++;
            if ((valLen & 0x80) != 0) {
                int numBytes = valLen & 0x7F;
                valLen = 0;
                for (int i = 0; i < numBytes && pos < end; i++) {
                    valLen = (valLen << 8) | (data[pos] & 0xFF);
                    pos++;
                }
            }
            if (pos + valLen > end) valLen = end - pos;
            if (valLen > 0) {
                byte[] val = new byte[valLen];
                System.arraycopy(data, pos, val, 0, valLen);
                map.put(tag, val);
            }
            pos += valLen;
        }
        return map;
    }

    private String bytesToHex(byte[] bytes) {
        if (bytes == null) return "";
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02X ", b));
        return sb.toString().trim();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null)
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, nfcFilters, null);
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
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            currentTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            txtResult.setText("已偵測到卡片。點擊「等待卡片靠近」開始讀取 EMV 資料。");
        }
    }
}
