package com.wallace.demo.app.utils;

import java.util.Vector;

/**
 * Created by 10192057 on 2018/3/5 0005.
 */
public class FuncUtil {
    /**
     * 将数组按照指定的字符拆分
     *
     * @param strArray    字符串数组;
     * @param strCon      字符串符
     * @param strSplitter 字符串分隔符
     */
    public static void GetSplitString(Vector<String> strArray, final String strCon, final String strSplitter) {
        if (strCon.isEmpty() || strSplitter.isEmpty()) {
            return;
        }
        try {
            String stemp = strCon;
            while (true) {
                int nPos = stemp.indexOf(strSplitter);
                if (nPos >= 0) {
                    String sSub = stemp.substring(0, nPos);
                    if (sSub.contains("\"")) {
                        if (sSub.indexOf("\"") == sSub.lastIndexOf("\"")) {
                            stemp = stemp.substring(nPos, stemp.length());
                            nPos = stemp.indexOf("\"");
                            if (nPos >= 0) {
                                sSub += stemp.substring(0, nPos);
                            }
                            sSub = sSub.substring(sSub.indexOf("\"") + 1, sSub.length() - sSub.lastIndexOf("\""));
                            nPos += 1;
                            stemp = stemp.substring(nPos + 1, stemp.length());
                        } else {
                            stemp = stemp.substring(nPos + 1, stemp.length());
                            sSub = sSub.substring(sSub.indexOf("\"") + 1, sSub.lastIndexOf("\""));
                        }
                    } else {
                        stemp = stemp.substring(nPos + 1, stemp.length());
                    }
                    strArray.add(sSub);
                } else {
                    strArray.add(stemp);
                    break;
                }
            }
        } catch (Exception e) {
            e.getStackTrace();
            strArray.clear();
        }
    }
}
