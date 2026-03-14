package com.helirfid.ultimate;

import android.app.Activity;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
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
    }

    @Override
    protected void onNewIntent(android.content.Intent intent){
        super.onNewIntent(intent);

        currentTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        byte[] uid = currentTag.getId();

        String uidStr = Converter.hex(uid);
        uidText.setText(uidStr);

        String hex = Converter.hex(uid);
        hexText.setText(hex);

        String dec10 = Converter.decimal10(uid);
        dec10Text.setText(dec10);

        String dec8 = Converter.decimal8(uid);
        dec8Text.setText(dec8);

        w26Text.setText(Wiegand.wiegand26(dec10));

        typeText.setText(CardAnalyzer.analyze(currentTag));

        cloneText.setText(CloneAnalyzer.analyze(currentTag));

        keyText.setText(KeyTester.testAllKeys(currentTag));

        history.add(dec10);
        updateHistory();
    }

    private void updateHistory(){
        List<String> list = history.get();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,list);

        historyList.setAdapter(adapter);
    }
}
