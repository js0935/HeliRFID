package com.helirfid.app;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.*;

public class HistoryManager {

    SharedPreferences prefs;

    public HistoryManager(Context c){

        prefs=c.getSharedPreferences("heli_history",
                Context.MODE_PRIVATE);
    }

    public void add(String card){

        List<String> list=get();

        list.add(0,card);

        if(list.size()>100)
            list.remove(list.size()-1);

        prefs.edit()
                .putString("data",
                        String.join(",",list))
                .apply();
    }

    public List<String> get(){

        String s=prefs.getString("data","");

        List<String> list=new ArrayList<>();

        if(!s.isEmpty())
            list.addAll(Arrays.asList(s.split(",")));

        return list;
    }

    public void clear(){

        prefs.edit().clear().apply();
    }
}
