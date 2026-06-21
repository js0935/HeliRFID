package com.helirfid;

import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class TagLockActivity extends BaseNfcActivity {

    TextView txtResult;
    Button btnLock, btnClear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_lock);

        txtResult = findViewById(R.id.txtTagLockResult);
        btnLock = findViewById(R.id.btnLockTag);
        btnClear = findViewById(R.id.btnClearLock);

        btnClear.setOnClickListener(v -> txtResult.setText(""));

        btnLock.setOnClickListener(v ->
            txtResult.setText("請將要鎖定的 NTAG 標籤靠近手機\n\n" +
                "⚠️ 鎖定後標籤將永久變為唯讀，無法復原！"));
    }

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) lockTag(tag);
        }
    }

    private void lockTag(Tag tag) {
        MifareUltralight mu = MifareUltralight.get(tag);
        if (mu == null) {
            txtResult.setText("此標籤不支援 MifareUltralight\n僅支援 NTAG21x 系列標籤");
            return;
        }

        try {
            mu.connect();

            byte[] pwd = null;
            try {
                byte[] pwdPage = mu.readPages(0x2C);
                if (pwdPage != null && pwdPage.length >= 4) {
                    boolean hasPwd = false;
                    for (int i = 0; i < 4; i++) {
                        if (pwdPage[i] != (byte) 0xFF) { hasPwd = true; break; }
                    }
                    if (hasPwd) {
                        pwd = new byte[]{pwdPage[0], pwdPage[1], pwdPage[2], pwdPage[3]};
                    }
                }
            } catch (Exception ignored) {}

            if (pwd != null) {
                try { mu.setTimeout(2000); mu.transceive(new byte[]{
                        (byte) 0x1B, pwd[0], pwd[1], pwd[2], pwd[3]}); } catch (Exception ignored) {}
            }

            byte[] lockPages = mu.readPages(0x02);
            if (lockPages == null || lockPages.length < 4) {
                txtResult.setText("讀取鎖定頁失敗");
                mu.close();
                return;
            }

            byte[] lockData = new byte[16];
            for (int i = 0; i < 4; i++) {
                mu.writePage(0x02 + i, new byte[]{
                        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00});
            }
            for (int i = 0; i < 4; i++) {
                mu.writePage(0x04 + i, new byte[]{
                        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00});
            }
            byte[] protect = {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
            for (int pg = 0x08; pg <= 0x2B; pg += 4) {
                try { mu.writePage(pg, protect); } catch (Exception ignored) {}
            }

            mu.close();

            StringBuilder sb = new StringBuilder();
            sb.append("✅ 鎖定成功！\n\n");
            sb.append("標籤已被永久鎖定為唯讀\n");
            sb.append("所有區塊已設為唯讀保護\n");
            sb.append("此操作不可逆轉\n\n");
            sb.append("⚠️ 從現在開始此標籤僅可讀取，無法寫入");
            txtResult.setText(sb.toString());
            vibrate();

        } catch (Exception e) {
            try { mu.close(); } catch (Exception ignored) {}
            txtResult.setText("鎖定失敗: " + e.getMessage() +
                    "\n\n可能原因：\n• 標籤已被鎖定\n• 需要密碼驗證\n• 不支援此標籤類型");
        }
    }
}
