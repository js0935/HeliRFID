package com.helirfid;

public class Wiegand {

    public static String wiegand26(String card){
        int num=Integer.parseInt(card);

        int facility=(num>>16)&0xFF;

        int id=num&0xFFFF;

        return facility+"-"+id;
    }

    public static String wiegand34(String card){
        long num=Long.parseLong(card);

        long facility=(num>>16)&0xFFFF;

        long id=num&0xFFFF;

        return facility+"-"+id;
    }
}
