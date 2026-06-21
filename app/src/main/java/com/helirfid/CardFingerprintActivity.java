package com.helirfid;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;
import java.util.Arrays;

public class CardFingerprintActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] nfcFilters;
    private Tag currentTag;

    private TextView txtCardInfo, txtScanStatus, txtOysterMatch, txtRatpMatch, txtSkgtMatch, txtDetail;
    private View cardDetail;

    private byte[] fpOyster, fpRatp, fpSkgt;
    private byte[] scannedDump;
    private int readableSectors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_fingerprint);

        txtCardInfo = findViewById(R.id.txtCardInfo);
        txtScanStatus = findViewById(R.id.txtScanStatus);
        txtOysterMatch = findViewById(R.id.txtOysterMatch);
        txtRatpMatch = findViewById(R.id.txtRatpMatch);
        txtSkgtMatch = findViewById(R.id.txtSkgtMatch);
        txtDetail = findViewById(R.id.txtDetail);
        cardDetail = findViewById(R.id.cardDetail);

        Button btnScan = findViewById(R.id.btnScanFingerprint);
        btnScan.setOnClickListener(v -> scanCard());

        loadFingerprintTemplates();

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

        if (getIntent() != null) {
            Tag tag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) {
                currentTag = tag;
                scanCard();
            }
        }
    }

    private void loadFingerprintTemplates() {
        try {
            fpOyster = readRawResource(R.raw.fp_oyster);
            fpRatp = readRawResource(R.raw.fp_ratb);
            fpSkgt = readRawResource(R.raw.fp_skgt);
        } catch (Exception e) {
            txtScanStatus.setText("載入指紋模板失敗: " + e.getMessage());
        }
    }

    private byte[] readRawResource(int resId) throws Exception {
        try (InputStream is = getResources().openRawResource(resId)) {
            byte[] data = new byte[is.available()];
            int offset = 0;
            while (offset < data.length) {
                int count = is.read(data, offset, data.length - offset);
                if (count < 0) break;
                offset += count;
            }
            return data;
        }
    }

    private void scanCard() {
        if (currentTag == null) {
            txtScanStatus.setText("請將卡片靠近手機 NFC");
            Toast.makeText(this, "請掃描卡片", Toast.LENGTH_SHORT).show();
            return;
        }
        txtScanStatus.setText("正在讀取卡片資料...");
        new Thread(this::readDump).start();
    }

    private void readDump() {
        try {
            MifareClassic mfc = MifareClassic.get(currentTag);
            if (mfc == null) {
                runOnUiThread(() -> txtScanStatus.setText("不支援 MIFARE Classic 卡片"));
                return;
            }

            mfc.connect();
            int sectorCount = mfc.getSectorCount();
            int blocksPerSector = 4;
            int totalBlocks = sectorCount * blocksPerSector;
            byte[] dump = new byte[totalBlocks * 16];
            Arrays.fill(dump, (byte) 0x00);

            StringBuilder cardInfo = new StringBuilder();
            cardInfo.append("UID: ").append(Converter.hex(currentTag.getId())).append("\n");
            cardInfo.append("容量: ").append(sectorCount).append(" 磁區\n");
            cardInfo.append("技術: ");
            for (String t : currentTag.getTechList()) {
                String shortName = t.substring(t.lastIndexOf('.') + 1);
                cardInfo.append(shortName).append(" ");
            }

            readableSectors = 0;

            for (int s = 0; s < sectorCount; s++) {
                boolean auth = false;
                for (int k = 0; k < KeyTester.getKeyCount(); k++) {
                    byte[] key = KeyTester.getKeyByKeyIndex(k);
                    if (key == null) continue;
                    try {
                        if (mfc.authenticateSectorWithKeyA(s, key)) {
                            auth = true;
                            break;
                        }
                    } catch (Exception e) {
                        // try next key
                    }
                }

                int firstBlock = mfc.sectorToBlock(s);
                if (auth) {
                    readableSectors++;
                    for (int b = 0; b < blocksPerSector; b++) {
                        byte[] data = mfc.readBlock(firstBlock + b);
                        System.arraycopy(data, 0, dump, (firstBlock + b) * 16, 16);
                    }
                }
            }

            mfc.close();
            scannedDump = dump;

            runOnUiThread(() -> {
                txtCardInfo.setText(cardInfo.toString());
                txtScanStatus.setText("已讀取 " + readableSectors + "/" + sectorCount + " 磁區");
                performFingerprintMatch();
            });

        } catch (Exception e) {
            runOnUiThread(() -> txtScanStatus.setText("讀取失敗: " + e.getMessage()));
        }
    }

    private void performFingerprintMatch() {
        if (scannedDump == null || scannedDump.length == 0) return;

        int dumpLen = scannedDump.length;

        FpResult oysterResult = matchFingerprint(fpOyster, dumpLen, "Oyster");
        FpResult ratpResult = matchFingerprint(fpRatp, dumpLen, "RATP");
        FpResult skgtResult = matchFingerprint(fpSkgt, dumpLen, "SKGT");

        txtOysterMatch.setText(formatResult(oysterResult));
        txtRatpMatch.setText(formatResult(ratpResult));
        txtSkgtMatch.setText(formatResult(skgtResult));

        setCardBackground(findViewById(R.id.cardOyster), oysterResult.confidence);
        setCardBackground(findViewById(R.id.cardRatp), ratpResult.confidence);
        setCardBackground(findViewById(R.id.cardSkgt), skgtResult.confidence);

        StringBuilder detail = new StringBuilder();
        detail.append("=== 指紋比對結果 ===\n\n");

        FpResult best = oysterResult;
        String bestName = "Oyster (倫敦)";
        if (ratpResult.confidence > best.confidence) {
            best = ratpResult;
            bestName = "RATP/RATB (巴黎/布加勒斯特)";
        }
        if (skgtResult.confidence > best.confidence) {
            best = skgtResult;
            bestName = "SKGT (雅典)";
        }

        if (best.confidence >= 50) {
            detail.append("最佳匹配: ").append(bestName).append("\n");
            detail.append("信心度: ").append(String.format("%.1f%%", best.confidence)).append("\n\n");
        } else {
            detail.append("未匹配已知指紋\n\n");
        }

        detail.append("Oyster: ").append(String.format("%.1f%%", oysterResult.confidence));
        detail.append(" (").append(oysterResult.matchedBytes).append("/").append(oysterResult.totalBytes).append(" bytes)\n");
        detail.append("RATP: ").append(String.format("%.1f%%", ratpResult.confidence));
        detail.append(" (").append(ratpResult.matchedBytes).append("/").append(ratpResult.totalBytes).append(" bytes)\n");
        detail.append("SKGT: ").append(String.format("%.1f%%", skgtResult.confidence));
        detail.append(" (").append(skgtResult.matchedBytes).append("/").append(skgtResult.totalBytes).append(" bytes)\n\n");

        if (readableSectors > 0) {
            detail.append("已讀取 ").append(readableSectors).append(" 磁區可供比對\n");
        }

        detail.append("掃描尺寸: ").append(dumpLen).append(" bytes\n");

        txtDetail.setText(detail.toString());
        cardDetail.setVisibility(View.VISIBLE);
    }

    private FpResult matchFingerprint(byte[] template, int dumpLen, String name) {
        int compareLen = Math.min(template.length, dumpLen);
        int matched = 0;
        int examined = 0;

        int sectorSize = 64;
        int sectorCount = compareLen / sectorSize;

        for (int s = 0; s < sectorCount; s++) {
            int sectorOffset = s * sectorSize;
            boolean sectorHasData = false;
            int sectorMatch = 0;
            int sectorTotal = 0;

            for (int i = 0; i < sectorSize && (sectorOffset + i) < compareLen; i++) {
                int idx = sectorOffset + i;
                byte scannedByte = scannedDump[idx];
                byte templByte = template[idx];

                if (scannedByte == 0x00 && templByte == 0x00) continue;

                sectorHasData = true;
                sectorTotal++;
                if (scannedByte == templByte) {
                    sectorMatch++;
                }
            }

            if (sectorHasData) {
                examined += sectorTotal;
                matched += sectorMatch;
            }
        }

        double confidence = examined > 0 ? (matched * 100.0 / examined) : 0;

        return new FpResult(matched, examined, confidence);
    }

    private String formatResult(FpResult result) {
        if (result.totalBytes == 0) return "無可比較資料";
        String conf = String.format("%.1f%%", result.confidence);
        String summary = conf + " (" + result.matchedBytes + "/" + result.totalBytes + " bytes 匹配)";
        if (result.confidence >= 70) {
            return summary + " ✓ 高度匹配";
        } else if (result.confidence >= 40) {
            return summary + " ~ 部分匹配";
        } else {
            return summary + " ✗ 不匹配";
        }
    }

    private void setCardBackground(View cardView, double confidence) {
        int color;
        if (confidence >= 70) {
            color = 0xFFE8F5E9;
        } else if (confidence >= 40) {
            color = 0xFFFFF8E1;
        } else {
            color = 0xFFFFEBEE;
        }
        cardView.setBackgroundColor(color);
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
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            currentTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (currentTag != null) {
                txtScanStatus.setText("已偵測到卡片，點擊「掃描卡片指紋」開始比對");
                String uid = Converter.hex(currentTag.getId());
                txtCardInfo.setText("UID: " + uid + "\n已偵測，等待掃描...");
            }
        }
    }

    private static class FpResult {
        final int matchedBytes;
        final int totalBytes;
        final double confidence;

        FpResult(int matchedBytes, int totalBytes, double confidence) {
            this.matchedBytes = matchedBytes;
            this.totalBytes = totalBytes;
            this.confidence = confidence;
        }
    }
}
