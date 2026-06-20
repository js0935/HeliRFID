/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.0.1
 */
package com.helirfid;

public class Wiegand {

    public static String wiegand26(String card){
        long num=Long.parseLong(card);

        int facility=(int)(num>>16)&0xFF;

        int id=(int)num&0xFFFF;

        return facility+"-"+id;
    }

    public static String wiegand34(String card){
        long num=Long.parseLong(card);

        long facility=(num>>16)&0xFFFF;

        long id=num&0xFFFF;

        return facility+"-"+id;
    }

    public static String wiegand32(String card){
        long num=Long.parseLong(card);

        long facility=(num>>16)&0xFFFF;

        long id=num&0xFFFF;

        return facility+"-"+id;
    }

    public static String wiegand37(String card){
        long num=Long.parseLong(card);

        long facility=(num>>21)&0xFFFF;

        long id=num&0x1FFFFF;

        return facility+"-"+id;
    }

    public static String wiegand40(String card){
        long num=Long.parseLong(card);

        long facility=(num>>20)&0xFFFFF;

        long id=num&0xFFFFF;

        return facility+"-"+id;
    }

    public static String wiegand44(String card){
        long num=Long.parseLong(card);

        long facility=(num>>24)&0xFFFFFF;

        long id=num&0xFFFFFF;

        return facility+"-"+id;
    }
}
