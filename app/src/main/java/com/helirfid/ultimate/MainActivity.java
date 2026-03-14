package com.helirfid.ultimate;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView txtUID,txtHEX,txt10,txt8,txtW26,txtClone,txtCard;
    Button btnDump,btnClear,btnExport;
    ListView history;

    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;

    HistoryManager historyManager;

    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        txtUID=findViewById(R.id.txtUID);
        txtHEX=findViewById(R.id.txtHEX);
        txt10=findViewById(R.id.txt10);
        txt8=findViewById(R.id.txt8);
        txtW26=findViewById(R.id.txtW26);
        txtClone=findViewById(R.id.txtClone);
        txtCard=findViewById(R.id.txtCard);

        btnDump=findViewById(R.id.btnDump);
        btnClear=findViewById(R.id.btnClear);
        btnExport=findViewById(R.id.btnExport);

        history=findViewById(R.id.history);

        historyManager=new HistoryManager(this);

        nfcAdapter=NfcAdapter.getDefaultAdapter(this);

        Intent intent=new Intent(this,getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        pendingIntent=PendingIntent.getActivity(
                this,0,intent,PendingIntent.FLAG_MUTABLE);

        btnDump.setOnClickListener(v->{

            AlertDialog.Builder builder=new AlertDialog.Builder(this);
            builder.setTitle("Memory Dump");
            builder.setMessage("Full memory dump feature will be available in the paid version.\n\nIncludes: Sector analysis, Block reading, Key cracking tools.");
            builder.setPositiveButton("OK",(dialog,which)->dialog.dismiss());
            builder.show();

        });

        btnClear.setOnClickListener(v->{

            historyManager.clear();
            refreshHistory();
            Toast.makeText(this,"紀錄已清除",Toast.LENGTH_SHORT).show();

        });

        btnExport.setOnClickListener(v->{

            List<String> list=historyManager.get();

            boolean success=CsvExporter.export(this,list);

            if(success){
                Toast.makeText(this,"CSV 已匯出到 Downloads",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this,"匯出失敗",Toast.LENGTH_SHORT).show();
            }

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

            String uidStr=NFCReader.getUID(tag);
            byte[] uid=tag.getId();
            String hex=Converter.hex(uid);
            String d10=Converter.decimal10(uid);
            String d8=Converter.decimal8(uid);
            String w26=Wiegand.wiegand26(d10.substring(d10.length()-8));
            String clone=CloneAnalyzer.analyze(tag);
            String cardType=CardAnalyzer.analyze(tag);

            txtUID.setText("UID: "+uidStr);
            txtHEX.setText("HEX: "+hex);
            txt10.setText("10碼: "+d10);
            txt8.setText("8碼: "+d8);
            txtW26.setText("Wiegand26: "+w26);
            txtClone.setText(clone);
            txtCard.setText(cardType);

            historyManager.add(d10);

            refreshHistory();
        }
    }

    void refreshHistory(){

        List<String> list=historyManager.get();

        ArrayAdapter<String> adapter=
                new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_1,list);

        history.setAdapter(adapter);
    }
}
