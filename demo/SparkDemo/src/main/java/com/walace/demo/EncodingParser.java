package com.walace.demo;

import java.io.UnsupportedEncodingException;

/**
 * Created by Wallace on 2016/8/20.
 */
public class EncodingParser {
    public static String getEncoding(String str) {
        String encode = "GB2312";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {
                return encode;
            }
        } catch (Exception ignored) {
        }
        encode = "ISO-8859-1";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {
                return encode;
            }
        } catch (Exception ignored) {
        }
        encode = "UTF-8";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {
                return encode;
            }
        } catch (Exception ignored) {
        }
        encode = "GBK";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {
                return encode;
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    public static String gb2312ToWord(String string) {
        String result = "";
        byte[] bytes = new byte[string.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            byte high = Byte.parseByte(string.substring(i * 2, i * 2 + 1), 16);
            byte low = Byte.parseByte(string.substring(i * 2 + 1, i * 2 + 2), 16);
            bytes[i] = (byte) (high << 4 | low);
        }
        if (bytes.length > 0) {
            try {
                result = new String(bytes, "gbk");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return result;
    }


    public static String toStringHex(String s, String encodeType) {
        byte[] baKeyword = new byte[s.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (0xff & Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            s = new String(baKeyword, encodeType);//UTF-16le:Not
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return s;
    }

//    public static String toStringHex2(String s) {
//        byte[] baKeyword = new byte[s.length() / 2];
//        for (int i = 0; i < baKeyword.length; i++) {
//            try {
//                baKeyword[i] = (byte) (0xff & Integer.parseInt(s.substring(
//                        i * 2, i * 2 + 2), 16));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        try {
//            s = new String(baKeyword, "gbk");// UTF-16le:Not
//        } catch (Exception e1) {
//            e1.printStackTrace();
//        }
//        return s;
//    }

    public static void main(String args[]) {
        String ge = EncodingParser.getEncoding("4c455a303131373030535fc0f6ccecb4f3cfc33230c2a52de3fcd1c5bfb5c0d6b3c72833353731393729");
        System.out.println(ge);
        String res = EncodingParser.toStringHex("b9fdcec2","GB2312");
        System.out.println(res);
    }
}
