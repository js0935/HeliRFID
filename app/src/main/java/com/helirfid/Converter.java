/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.0.1
 */
package com.helirfid;

import java.math.BigInteger;

public class Converter {

    public static String hex(byte[] uid){
        StringBuilder sb=new StringBuilder();

        for(byte b:uid)
            sb.append(String.format("%02X",b));

        return sb.toString();
    }

    public static String decimal10(byte[] uid){
        String hex=hex(uid);

        String last4=hex.substring(hex.length()-8);

        String reversed=
                last4.substring(6,8)+
                last4.substring(4,6)+
                last4.substring(2,4)+
                last4.substring(0,2);

        BigInteger dec=new BigInteger(reversed,16);

        String num=dec.toString();

        while(num.length()<10)
            num="0"+num;

        return num;
    }

    public static String decimal8(byte[] uid){
        String d=decimal10(uid);

        return d.substring(d.length()-8);
    }

    public static String bytesToHex(byte[] bytes){
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }
}
