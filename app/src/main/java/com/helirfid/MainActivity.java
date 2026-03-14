package com.helirfid;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView txtUID, txtCard10, txtCard8, txtW26, txtType, txtAnalysis, txtKeyTest;
    EditText editInput;
    Button btnClear, btnExport, btnDump, btnConvert, btnAnalyze, btnTestKeys;
    ListView historyList;
    HistoryManager historyManager;
    NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private Tag currentTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtUID = findViewById(R.id.txtUID);
        txtCard10 = findViewById(R.id.txtCard10);
        txtCard8 = findViewById(R.id.txtCard8);
        txtW26 = findViewById(R.id.txtW26);
        txtType = findViewById(R.id.txtType);
        txtAnalysis = findViewById(R.id.txtAnalysis);
        txtKeyTest = findViewById(R.id.txtKeyTest);
        editInput = findViewById(R.id.editInput);

        btnClear = findViewById(R.id.btnClear);
        btnExport = findViewById(R.id.btnExport);
        btnDump = findViewById(R.id.btnDump);
        btnConvert = findViewById(R.id.btnConvert);
        btnAnalyze = findViewById(R.id.btnAnalyze);
        btnTestKeys = findViewById(R.id.btnTestKeys);

        historyList = findViewById(R.id.historyList);
        historyManager = new HistoryManager(this);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        Intent intent = new Intent(this, getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? PendingIntent.FLAG_MUTABLE
                : PendingIntent.FLAG_UPDATE_CURRENT;

        pendingIntent = PendingIntent.getActivity(
                this, 0, intent, flags);

        btnClear.setOnClickListener(v -> {
            historyManager.clear();
            updateHistory();
            Toast.makeText(MainActivity.this, "歷史記錄已清除", Toast.LENGTH_SHORT).show();
        });

        btnExport.setOnClickListener(v -> {
            List<String> list = historyManager.get();
            if (list.isEmpty()) {
                Toast.makeText(MainActivity.this, "沒有歷史記錄可匯出", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean result = CsvExporter.export(this, list);
            Toast.makeText(MainActivity.this, result ? "匯出成功" : "匯出失敗", Toast.LENGTH_SHORT).show();
        });

        btnDump.setOnClickListener(v -> {
            if(currentTag != null){
                Intent i = new Intent(this, MemoryDumpActivity.class);
                i.putExtra("tag", currentTag);
                startActivity(i);
            } else {
                Toast.makeText(MainActivity.this, "請先掃描 NFC 卡", Toast.LENGTH_SHORT).show();
            }
        });

        btnConvert.setOnClickListener(v -> {
            String input = editInput.getText().toString().trim();

            if (TextUtils.isEmpty(input)) {
                Toast.makeText(MainActivity.this, "請輸入 UID", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                String uid = input.replace(":", "").replace(" ", "").toUpperCase();

                if (!uid.matches("[0-9A-F]+")) {
                    Toast.makeText(MainActivity.this, "請輸入有效的 16 進制字符 (0-9, A-F)", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (uid.length() != 8) {
                    Toast.makeText(MainActivity.this, "限制輸入 4 組，格式如: 00:00:00:00", Toast.LENGTH_SHORT).show();
                    return;
                }

                byte[] uidBytes = hexStringToByteArray(uid);
                String card10 = Converter.decimal10(uidBytes);
                String card8 = Converter.decimal8(uidBytes);
                String w26 = Wiegand.wiegand26(card10);
                String w34 = Wiegand.wiegand34(card10);

                String formattedUid = uid.substring(0, 2) + ":" + uid.substring(2, 4) + ":" + uid.substring(4, 6) + ":" + uid.substring(6, 8);

                txtUID.setText("UID: " + formattedUid);
                txtCard10.setText("10碼: " + card10);
                txtCard8.setText("8碼: " + card8);
                txtW26.setText("Wiegand26: " + w26);
                txtType.setText("Card Type:\n手動輸入");

                String analysis = CloneAnalyzer.analyze(formattedUid, card10);
                txtAnalysis.setText(analysis);

                historyManager.add(card10);
                updateHistory();

                Toast.makeText(MainActivity.this, "轉換成功", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "轉換失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        btnAnalyze.setOnClickListener(v -> {
            String uid = txtUID.getText().toString().replace("UID: ", "");
            String card10 = txtCard10.getText().toString().replace("10碼: ", "");
            
            if (TextUtils.isEmpty(uid) || uid.equals("請將 NFC 卡靠近手機")) {
                Toast.makeText(MainActivity.this, "請先掃描或輸入 UID", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String analysis = CloneAnalyzer.analyze(uid, card10);
            txtAnalysis.setText(analysis);
        });

        btnTestKeys.setOnClickListener(v -> {
            if (currentTag == null) {
                Toast.makeText(MainActivity.this, "請先掃描 NFC 卡", Toast.LENGTH_SHORT).show();
                return;
            }

            String keyTestResult = KeyTester.testKeys(currentTag);
            txtKeyTest.setText(keyTestResult);
            
            if (keyTestResult.contains("無可用金鑰")) {
                Toast.makeText(MainActivity.this, "測試完成：無可用金鑰", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this, "測試完成：找到可用金鑰", Toast.LENGTH_LONG).show();
            }
        });

        updateHistory();

        Intent startupIntent = getIntent();
        handleNfcIntent(startupIntent);
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    private void handleNfcIntent(Intent intent) {
        if (intent != null && NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            currentTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            processTag(currentTag);
        }
    }

    private void processTag(Tag tag) {
        if (tag == null) {
            return;
        }

        try {
            String uid = NFCReader.getUID(tag);
            txtUID.setText("UID: " + uid);

            String card10 = Converter.decimal10(tag.getId());
            String card8 = Converter.decimal8(tag.getId());
            String w26 = Wiegand.wiegand26(card10);
            String w34 = Wiegand.wiegand34(card10);

            txtCard10.setText("10碼: " + card10);
            txtCard8.setText("8碼: " + card8);
            txtW26.setText("Wiegand26: " + w26);

            String type = CardAnalyzer.analyze(tag);
            txtType.setText("Card Type:\n" + type);

            String analysis = CloneAnalyzer.analyze(uid, card10);
            txtAnalysis.setText(analysis);

            String keyTestResult = KeyTester.testKeys(tag);
            txtKeyTest.setText(keyTestResult);

            historyManager.add(card10);
            updateHistory();

            NFCReader.setLastTag(tag);

            Toast.makeText(MainActivity.this, "卡片讀取成功", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "讀取卡片錯誤: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();

        if(nfcAdapter != null)
            nfcAdapter.enableForegroundDispatch(
                    this, pendingIntent, null, null);
    }

    @Override
    protected void onPause(){
        super.onPause();

        if(nfcAdapter != null)
            nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent){
        super.onNewIntent(intent);

        handleNfcIntent(intent);
    }

    private void updateHistory(){
        List<String> list = historyManager.get();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,list);

        historyList.setAdapter(adapter);
    }
}
