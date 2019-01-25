package com.androstock.smsapp;

import java.util.HashMap;
import java.util.Map;

public class Konversi {

    private static final Map<String, String> BinToHexa;
    static {
        BinToHexa = new HashMap<>();
        BinToHexa.put("0000","0");
        BinToHexa.put("0001","1");
        BinToHexa.put("0010","2");
        BinToHexa.put("0011","3");
        BinToHexa.put("0100","4");
        BinToHexa.put("0101","5");
        BinToHexa.put("0110","6");
        BinToHexa.put("0111","7");
        BinToHexa.put("1000","8");
        BinToHexa.put("1001","9");
        BinToHexa.put("1010","A");
        BinToHexa.put("1011","B");
        BinToHexa.put("1100","C");
        BinToHexa.put("1101","D");
        BinToHexa.put("1110","E");
        BinToHexa.put("1111","F");
    }

    private static final Map<String,String>HexaToBin;
    static {
        HexaToBin= new HashMap<>();
        HexaToBin.put("0", "0000");
        HexaToBin.put("1", "0001");
        HexaToBin.put("2", "0010");
        HexaToBin.put("3", "0011");
        HexaToBin.put("4", "0100");
        HexaToBin.put("5", "0101");
        HexaToBin.put("6", "0110");
        HexaToBin.put("7", "0111");
        HexaToBin.put("8", "1000");
        HexaToBin.put("9", "1001");
        HexaToBin.put("A", "1010");
        HexaToBin.put("B", "1011");
        HexaToBin.put("C", "1100");
        HexaToBin.put("D", "1101");
        HexaToBin.put("E", "1110");
        HexaToBin.put("F", "1111");
    }

    public static String hexaToBin(String sHexa){
        StringBuffer sH = new StringBuffer();
        int lenSHex = sHexa.length();
        sHexa = sHexa.toUpperCase();
        for (int i = 0; i<lenSHex;i++){
            sH.append(HexaToBin.get(sHexa.substring(i,i+1)));
        }
        return sH.toString();
    }

    public static String binToHexa(String sBin){
        StringBuffer sB = new StringBuffer();
        for (int i = 0; i<sBin.length();i+=4){
            sB.append(BinToHexa.get(sBin.substring(i,i+4)));
        }
        return sB.toString();
    }

    public static String asciiToHexa(String asciiStr){
        char[] chars = asciiStr.toCharArray();
        StringBuffer hex = new StringBuffer();
        for(int i = 0; i < chars.length; i++){
            hex.append(Integer.toHexString((int)chars[i]));
        }
        return hex.toString();
    }

    public static String hexaToascii(String hexaStr){
        StringBuilder output = new StringBuilder();
        for (int i=0; i<hexaStr.length()-1; i+=2){
            String str = hexaStr.substring(i, i+2);
            int decimal = Integer.parseInt(str,16);
            output.append((char)decimal);
        }
        return output.toString();
    }
}
