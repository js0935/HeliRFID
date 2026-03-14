package com.helirfid.ultimate;

import android.content.Context;
import android.os.Environment;
import java.io.*;
import java.util.List;

public class CsvExporter {

    public static boolean export(Context context,List<String> history){

        try{

            File path=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            File file=new File(path,"HeliRFID_History.csv");

            FileWriter fw=new FileWriter(file);

            BufferedWriter bw=new BufferedWriter(fw);

            bw.write("Card Number\n");

            for(String card:history)
                bw.write(card+"\n");

            bw.close();

            return true;

        }catch(Exception e){

            e.printStackTrace();

            return false;

        }

    }

}
