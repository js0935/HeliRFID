package com.helirfid;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView txtUID, txtCard10, txtCard8, txtW26, txtType;
    Button btnClear, btnExport, btnDump;
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

        btnClear = findViewById(R.id.btnClear);
        btnExport = findViewById(R.id.btnExport);
        btnDump = findViewById(R.id.btnDump);

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
        });

        btnExport.setOnClickListener(v -> {
            List<String> list = historyManager.get();
            boolean result = CsvExporter.export(this, list);
            Toast.makeText(this, result ? "匯出成功" : "匯出失敗", Toast.LENGTH_SHORT).show();
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
            String uid = NFCReader.getUID(tag);
            txtUID.setText("UID: " + uid);

            String card10 = Converter.decimal10(tag.getId());
            String card8 = Converter.decimal8(tag.getId());
            String w26 = Wiegand.wiegand26(card10);

            txtCard10.setText("10碼: " + card10);
            txtCard8.setText("8碼: " + card8);
            txtW26.setText("Wiegand26: " + w26);

            String type = CardAnalyzer.analyze(tag);
            txtType.setText("Card Type:\n" + type);

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
