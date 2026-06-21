package com.helirfid;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class HceNdefEmulationActivity extends BaseNfcActivity {

    EditText editText, editUrl, editPhone, editSmsNumber, editSmsText;
    RadioGroup radioType;
    CheckBox chkEnabled;
    Button btnStart, btnStop, btnClear, btnReadTag;
    TextView txtStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hce_ndef_emulation);

        editText = findViewById(R.id.editHceNdefText);
        editUrl = findViewById(R.id.editHceNdefUrl);
        editPhone = findViewById(R.id.editHceNdefPhone);
        editSmsNumber = findViewById(R.id.editHceNdefSmsNumber);
        editSmsText = findViewById(R.id.editHceNdefSmsText);
        radioType = findViewById(R.id.radioHceNdefType);
        chkEnabled = findViewById(R.id.chkHceNdefEnabled);
        btnStart = findViewById(R.id.btnHceNdefStart);
        btnStop = findViewById(R.id.btnHceNdefStop);
        btnClear = findViewById(R.id.btnHceNdefClear);
        btnReadTag = findViewById(R.id.btnHceNdefReadTag);
        txtStatus = findViewById(R.id.txtHceNdefStatus);

        boolean isActive = HceSimulationService.isCustomNdefActive(this);
        chkEnabled.setChecked(isActive);
        updateStatus(isActive);
        restoreSavedNdefData();

        btnStart.setOnClickListener(v -> startEmulation());
        btnStop.setOnClickListener(v -> stopEmulation());
        btnClear.setOnClickListener(v -> {
            editText.setText("");
            editUrl.setText("");
            editPhone.setText("");
            editSmsNumber.setText("");
            editSmsText.setText("");
            txtStatus.setText("已清除");
        });

        btnReadTag.setOnClickListener(v ->
                txtStatus.setText("請將 NDEF 標籤靠近手機以讀取內容"));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) return;

        Ndef ndef = Ndef.get(tag);
        if (ndef == null) {
            txtStatus.setText("此標籤不支援 NDEF");
            return;
        }

        try {
            ndef.connect();
            NdefMessage msg = ndef.getNdefMessage();
            ndef.close();

            if (msg == null) {
                txtStatus.setText("標籤無 NDEF 資料");
                return;
            }

            StringBuilder sb = new StringBuilder("=== 標籤 NDEF 內容 ===\n");
            for (NdefRecord record : msg.getRecords()) {
                byte[] payload = record.getPayload();
                String type = new String(record.getType(), java.nio.charset.StandardCharsets.US_ASCII);
                if (record.getTnf() == NdefRecord.TNF_WELL_KNOWN && "T".equals(type)) {
                    String text = new String(payload, java.nio.charset.StandardCharsets.UTF_8);
                    if (payload.length > 3) text = new String(payload, 3, payload.length - 3, java.nio.charset.StandardCharsets.UTF_8);
                    sb.append("文字: ").append(text).append("\n");
                    editText.setText(text);
                    radioType.check(R.id.radioHceNdefText);
                } else if (record.getTnf() == NdefRecord.TNF_WELL_KNOWN && "U".equals(type)) {
                    String url = new String(payload, java.nio.charset.StandardCharsets.UTF_8);
                    sb.append("網址: ").append(url).append("\n");
                    editUrl.setText(url);
                    radioType.check(R.id.radioHceNdefUrl);
                } else {
                    String content = new String(payload, java.nio.charset.StandardCharsets.UTF_8);
                    sb.append(type).append(": ").append(content).append("\n");
                    editText.setText(content);
                    radioType.check(R.id.radioHceNdefText);
                }
            }
            sb.append("\n已自動填入編輯欄位，可按「啟動模擬」儲存");
            txtStatus.setText(sb.toString());
            vibrate();
        } catch (Exception e) {
            txtStatus.setText("讀取失敗: " + e.getMessage());
        }
    }

    private void restoreSavedNdefData() {
        if (!HceSimulationService.isCustomNdefActive(this)) return;
        int savedType = HceSimulationService.getNdefType(this);
        String savedData = HceSimulationService.getNdefData(this);
        if (savedData == null || savedData.isEmpty()) return;

        switch (savedType) {
            case HceSimulationService.NDEF_TYPE_TEXT:
                editText.setText(savedData);
                radioType.check(R.id.radioHceNdefText);
                txtStatus.setText("已還原上次儲存的文字: " + savedData);
                break;
            case HceSimulationService.NDEF_TYPE_URL:
                editUrl.setText(savedData);
                radioType.check(R.id.radioHceNdefUrl);
                txtStatus.setText("已還原上次儲存的網址: " + savedData);
                break;
            case HceSimulationService.NDEF_TYPE_PHONE:
                editPhone.setText(savedData);
                radioType.check(R.id.radioHceNdefPhone);
                txtStatus.setText("已還原上次儲存的電話: " + savedData);
                break;
            case HceSimulationService.NDEF_TYPE_SMS: {
                String[] parts = savedData.split("\\|", 2);
                editSmsNumber.setText(parts.length > 0 ? parts[0] : "");
                editSmsText.setText(parts.length > 1 ? parts[1] : "");
                radioType.check(R.id.radioHceNdefSms);
                txtStatus.setText("已還原上次儲存的簡訊設定");
                break;
            }
        }
    }

    private void startEmulation() {
        int selected = radioType.getCheckedRadioButtonId();
        int ndefType;
        String ndefData;

        try {
            if (selected == R.id.radioHceNdefText) {
                String text = editText.getText().toString().trim();
                if (text.isEmpty()) { Toast.makeText(this, "請輸入文字", Toast.LENGTH_SHORT).show(); return; }
                ndefType = HceSimulationService.NDEF_TYPE_TEXT;
                ndefData = text;
            } else if (selected == R.id.radioHceNdefUrl) {
                String url = editUrl.getText().toString().trim();
                if (url.isEmpty()) { Toast.makeText(this, "請輸入網址", Toast.LENGTH_SHORT).show(); return; }
                if (!url.startsWith("http")) url = "https://" + url;
                ndefType = HceSimulationService.NDEF_TYPE_URL;
                ndefData = url;
            } else if (selected == R.id.radioHceNdefPhone) {
                String phone = editPhone.getText().toString().trim();
                if (phone.isEmpty()) { Toast.makeText(this, "請輸入電話號碼", Toast.LENGTH_SHORT).show(); return; }
                ndefType = HceSimulationService.NDEF_TYPE_PHONE;
                ndefData = phone;
            } else if (selected == R.id.radioHceNdefSms) {
                String number = editSmsNumber.getText().toString().trim();
                String smsBody = editSmsText.getText().toString().trim();
                if (number.isEmpty()) { Toast.makeText(this, "請輸入號碼", Toast.LENGTH_SHORT).show(); return; }
                ndefType = HceSimulationService.NDEF_TYPE_SMS;
                ndefData = number + "|" + smsBody;
            } else {
                return;
            }

            HceSimulationService.setCustomNdefData(this, ndefType, ndefData);
            chkEnabled.setChecked(true);
            updateStatus(true);
            Toast.makeText(this, "NDEF HCE 模擬已啟動", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "啟動失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void stopEmulation() {
        HceSimulationService.clearCustomNdef(this);
        chkEnabled.setChecked(false);
        updateStatus(false);
        Toast.makeText(this, "NDEF HCE 模擬已停止", Toast.LENGTH_SHORT).show();
    }

    private void updateStatus(boolean enabled) {
        txtStatus.setText(enabled ? "NDEF HCE 模擬中\n請用其他手機靠近本機背面讀取" : "模擬已停止");
    }
}
