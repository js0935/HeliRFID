package com.helirfid.app;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView result;
    EditText inputUid;
    Button btnConvert;
    ListView historyList;

    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;

    HistoryManager historyManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        result=findViewById(R.id.result);
        inputUid=findViewById(R.id.inputUid);
        btnConvert=findViewById(R.id.btnConvert);
        historyList=findViewById(R.id.historyList);

        historyManager=new HistoryManager(this);

        nfcAdapter=NfcAdapter.getDefaultAdapter(this);

        Intent intent=new Intent(this,getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        pendingIntent=PendingIntent.getActivity(
                this,0,intent,PendingIntent.FLAG_MUTABLE);

        btnConvert.setOnClickListener(v->{

            String uid=inputUid.getText().toString();

            String card=Converter.convert(uid);

            result.setText("門禁卡號\n"+card);

            historyManager.add(card);

            refreshHistory();
        });

        refreshHistory();
    }

    @Override
    protected void onResume(){

        super.onResume();

        if(nfcAdapter!=null)
            nfcAdapter.enableForegroundDispatch(
                    this,pendingIntent,null,null);
    }

    @Override
    protected void onPause(){

        super.onPause();

        if(nfcAdapter!=null)
            nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent){

        super.onNewIntent(intent);

        Tag tag=intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        if(tag!=null){

            byte[] uid=tag.getId();

            String hex=Converter.bytesToHex(uid);

            String card=Converter.convert(hex);

            result.setText("UID\n"+hex+"\n\n門禁卡號\n"+card);

            historyManager.add(card);

            refreshHistory();
        }
    }

    void refreshHistory(){

        List<String> list=historyManager.get();

        ArrayAdapter<String> adapter=
                new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_1,list);

        historyList.setAdapter(adapter);
    }
}
