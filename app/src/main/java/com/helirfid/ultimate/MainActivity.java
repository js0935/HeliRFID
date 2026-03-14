package com.helirfid.ultimate;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.widget.*;
import android.view.View;
import java.util.List;

public class MainActivity extends Activity {

    TextView uidText, hexText, dec10Text, dec8Text, w26Text, typeText, cloneText, keyText;
    Button btnClear, btnDump, btnExport;
    ListView historyList;
    HistoryManager history;

    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    Tag currentTag;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uidText = findViewById(R.id.txtUID);
        hexText = findViewById(R.id.txtHEX);
        dec10Text = findViewById(R.id.txt10);
        dec8Text = findViewById(R.id.txt8);
        w26Text = findViewById(R.id.txtW26);
        typeText = findViewById(R.id.txtCard);
        cloneText = findViewById(R.id.txtClone);
        keyText = findViewById(R.id.txtKey);
        btnClear = findViewById(R.id.btnClear);
        btnDump = findViewById(R.id.btnDump);
        btnExport = findViewById(R.id.btnExport);
        historyList = findViewById(R.id.history);

        history = new HistoryManager(this);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        Intent intent = new Intent(this, getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? PendingIntent.FLAG_MUTABLE
                : PendingIntent.FLAG_UPDATE_CURRENT;

        pendingIntent = PendingIntent.getActivity(
                this, 0, intent, flags);

        btnClear.setOnClickListener(v -> {
            history.clear();
            updateHistory();
        });

        btnDump.setOnClickListener(v -> {
            if (currentTag != null) {
                String dumpResult = DumpReader.dumpTagInfo(currentTag);
                Toast.makeText(this, "Memory Dump:\n" + dumpResult, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "請先掃描 NFC 卡", Toast.LENGTH_SHORT).show();
            }
        });

        btnExport.setOnClickListener(v -> {
            boolean result = CsvExporter.export(this, history.get());
            Toast.makeText(this, result ? "匯出成功" : "匯出失敗", Toast.LENGTH_SHORT).show();
        });

        updateHistory();

        Intent startupIntent = getIntent();
        handleNfcIntent(startupIntent);
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
            byte[] uid = tag.getId();

            String uidStr = Converter.hex(uid);
            uidText.setText(uidStr);

            String hex = Converter.hex(uid);
            hexText.setText(hex);

            String dec10 = Converter.decimal10(uid);
            dec10Text.setText(dec10);

            String dec8 = Converter.decimal8(uid);
            dec8Text.setText(dec8);

            w26Text.setText(Wiegand.wiegand26(dec10));

            typeText.setText(CardAnalyzer.analyze(tag));

            cloneText.setText(CloneAnalyzer.analyze(tag));

            keyText.setText(KeyTester.testAllKeys(tag));

            history.add(dec10);
            updateHistory();

            Toast.makeText(this, "卡片讀取成功", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "讀取卡片錯誤: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
    protected void onNewIntent(android.content.Intent intent){
        super.onNewIntent(intent);

        handleNfcIntent(intent);
    }

    private void updateHistory(){
        List<String> list = history.get();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,list);

        historyList.setAdapter(adapter);
    }
}
