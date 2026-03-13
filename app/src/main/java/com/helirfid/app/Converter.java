package com.helirfid.app;

import java.math.BigInteger;

public class Converter {

    public static String bytesToHex(byte[] bytes){

        StringBuilder sb=new StringBuilder();

        for(byte b:bytes)
            sb.append(String.format("%02X:",b));

        return sb.substring(0,sb.length()-1);
    }

    public static String convert(String uid){

        uid=uid.replace(":","");

        if(uid.length()<8) return "錯誤";

        String last4=uid.substring(uid.length()-8);

        String reversed=
                last4.substring(6,8)+
                last4.substring(4,6)+
                last4.substring(2,4)+
                last4.substring(0,2);

        BigInteger dec=new BigInteger(reversed,16);

        String num=dec.toString();

        if(num.length()>10)
            num=num.substring(num.length()-10);

        return num;
    }
}
