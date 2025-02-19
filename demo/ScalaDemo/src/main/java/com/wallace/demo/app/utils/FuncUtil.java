package com.wallace.demo.app.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

/**
 * Created by 10192057 on 2018/3/5 0005.
 */
public class FuncUtil {
    public static String[] split(String str, String sep, String other) {
        return split(str, sep.charAt(0), other.charAt(0));
    }

    public static String[] split(String str, char split, char other) {
        ArrayList<String> strList = new ArrayList<>();
        int startIndex = 0; // 字串的起始位置
        int subStrSize = 0; // 字串的长度
        int num = 0; // Initialize num
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            // 最后一个字符
            if (c != split && i == str.length() - 1) {
                strList.add(str.substring(startIndex, startIndex + subStrSize + 1));
            }
            if (c == other) {
                num++;
                subStrSize++;
            } else if (num % 2 == 0 && c == split) {
                strList.add(str.substring(startIndex, startIndex + subStrSize));
                startIndex += subStrSize + 1;
                subStrSize = 0;
            } else {
                subStrSize++;
            }
        }
        String fields[] = new String[strList.size()];
        return strList.toArray(fields);
    }

    public static String[] splitWithLimit(String str, char split, char other, int limit) {
        ArrayList<String> strList = new ArrayList<>(); // Initialize strList
        int startIndex = 0; // 字串的起始位置
        int subStrSize = 0; // 字串的长度
        int splitCount = 0; // 分割次数
        int num = 0; // Initialize num

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            // 最后一个字符
            if (c != split && i == str.length() - 1) {
                strList.add(str.substring(startIndex, startIndex + subStrSize + 1));
            }
            if (c == other) {
                num++;
                subStrSize++;
            } else if (num % 2 == 0 && c == split && splitCount < limit - 1) {
                strList.add(str.substring(startIndex, startIndex + subStrSize));
                startIndex += subStrSize + 1;
                subStrSize = 0;
                splitCount++;
            } else {
                subStrSize++;
            }
        }
        String fields[] = new String[strList.size()];
        return strList.toArray(fields);
    }

    /**
     * 将数组按照指定的字符拆分
     *
     * @param strArray    字符串数组;
     * @param strCon      字符串符
     * @param strSplitter 字符串分隔符
     */
    public static void GetSplitString(List<String> strArray, final String strCon, final String strSplitter) {
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

    public static Vector<String> ProcessNeighbourInfo(List<String> MRField) {
        Vector<String> records = new Vector();
        if (MRField.size() < 142) {
            return records;
        } else {
            String sNCellNum = MRField.get(44);
            boolean var4 = false;

            int NCellNum;
            try {
                NCellNum = Integer.parseInt(sNCellNum);
                if (NCellNum <= 0) {
                    return records;
                }
            } catch (Exception var16) {
                return records;
            }

            String[] enbList = (MRField.get(46)).split("\\$", -1);
            String[] cidList = (MRField.get(47)).split("\\$", -1);
            String[] rsrpList = (MRField.get(49)).split("\\$", -1);
            String[] rsrqList = (MRField.get(50)).split("\\$", -1);
            String[] pciList = (MRField.get(48)).split("\\$", -1);
            String[] NeighFlagList = (MRField.get(51)).split("\\$", -1);
            String[] NeighEarfcnList = (MRField.get(45)).split("\\$", -1);
            if (NCellNum == enbList.length && NCellNum == cidList.length && NCellNum == NeighFlagList.length && NCellNum == rsrpList.length) {
                int i;
                if (NCellNum != pciList.length || NCellNum != NeighEarfcnList.length) {
                    pciList = new String[NCellNum];
                    NeighEarfcnList = new String[NCellNum];

                    for (i = 0; i < NCellNum; ++i) {
                        pciList[i] = "";
                        NeighEarfcnList[i] = "";
                    }
                }

                if (NCellNum != rsrqList.length) {
                    rsrqList = new String[NCellNum];

                    for (i = 0; i < NCellNum; ++i) {
                        rsrqList[i] = "";
                    }
                }

                try {
                    for (i = 0; i < NCellNum; ++i) {
                        if (!pciList[i].equals("") && !NeighEarfcnList[i].equals("")) {
                            String ncellkey = enbList[i] + "_" + cidList[i];
                            String aRecord = MRField.get(0) + "," + MRField.get(1) + "," + MRField.get(2) + "," + MRField.get(29) + "," + MRField.get(10) + "," + MRField.get(11) + "," + MRField.get(5) + "," + enbList[i] + "," + cidList[i] + "," + pciList[i] + "," + rsrpList[i] + "," + rsrqList[i] + "," + MRField.get(7) + "," + MRField.get(8) + "," + MRField.get(12) + "," + MRField.get(13) + "," + MRField.get(23) + "," + MRField.get(24) + "," + MRField.get(25) + "," + MRField.get(44) + "," + NeighFlagList[i] + "," + MRField.get(9) + "," + NeighEarfcnList[i] + "," + i + "," + "\"" + MRField.get(69) + "\"" + "," + "\"" + MRField.get(70) + "\"" + "," + "\"" + MRField.get(71) + "\"" + "," + "\"" + MRField.get(72) + "\"" + "," + "\"" + MRField.get(73) + "\"" + "," + MRField.get(35) + "," + MRField.get(93) + "," + MRField.get(43) + "," + MRField.get(109) + "," + ncellkey + "," + MRField.get(63) + "," + MRField.get(74) + "," + MRField.get(131) + "," + MRField.get(132) + "," + MRField.get(133) + "," + MRField.get(139);
                            records.add(aRecord);
                        }
                    }

                    return records;
                } catch (Exception var15) {
                    return records;
                }
            } else {
                return records;
            }
        }
    }

    public static Vector<String> ProcessNeighbourInfo(String OneLineInfo) {
        Vector<String> MRField = new Vector();
        GetSplitString(MRField, OneLineInfo, ",");
        return ProcessNeighbourInfo(MRField);
    }

    public static Vector<String> ProcessNeighbourInfoOther(String OneLineInfo) {
        String[] MRField = split(OneLineInfo, ',', '"');
        return ProcessNeighbourInfo(Arrays.asList(MRField));
    }
}
