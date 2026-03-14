package com.helirfid;

import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MemoryDumpActivity extends AppCompatActivity {

    RecyclerView rvDump;
    List<DumpItem> dumpList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_dump);

        rvDump = findViewById(R.id.rvDump);
        rvDump.setLayoutManager(new LinearLayoutManager(this));

        dumpList = new ArrayList<>();

        Tag tag = getIntent().getParcelableExtra("tag");

        if(tag != null){
            readDump(tag);
        }

        rvDump.setAdapter(new DumpAdapter(dumpList));
    }

    private void readDump(Tag tag){
        try{
            MifareClassic mfc = MifareClassic.get(tag);
            mfc.connect();
            int sectorCount = mfc.getSectorCount();

            for(int s=0; s<sectorCount; s++){
                boolean auth = mfc.authenticateSectorWithKeyA(s, MifareClassic.KEY_DEFAULT);
                int blockCount = mfc.getBlockCountInSector(s);
                int firstBlock = mfc.sectorToBlock(s);

                for(int b=0; b<blockCount; b++){
                    byte[] data = mfc.readBlock(firstBlock+b);
                    String hex = Converter.bytesToHex(data);

                    String desc = "";
                    if(s==0 && b==0) desc = "UID Block";
                    else if(b==blockCount-1) desc = "Access / Key Block";
                    else desc = "Data Block";

                    dumpList.add(new DumpItem(s, firstBlock+b, hex, desc));
                }
            }

            mfc.close();

        }catch(Exception e){
            dumpList.add(new DumpItem(-1,-1,"Error","讀取失敗: " + e.getMessage()));
        }
    }
}
