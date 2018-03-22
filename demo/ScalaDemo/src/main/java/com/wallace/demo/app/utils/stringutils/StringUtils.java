/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace.demo.app.utils.stringutils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wallace on 2018/3/23.
 */
public class StringUtils {

    public static List<String> restoreIpAddresses(String s) {
        List<String> res = new ArrayList<>();
        if (s == null || s.length() < 4 || s.length() > 12) return res;
        restoreIp(res, new ArrayList<>(), s, 0);
        return res;
    }

    private static void restoreIp(List<String> res, List<String> ip, String s, int pos) {
        if (ip.size() == 4) {
            if (pos != s.length()) return;
            StringBuilder ipSb = new StringBuilder();
            for (String str : ip) ipSb.append(str).append(".");
            ipSb.setLength(ipSb.length() - 1);
            res.add(ipSb.toString());
            return;
        }

        for (int i = pos; i < s.length() && i < pos + 3; i++) {
            String ipSeg = s.substring(pos, i + 1);
            if (isIpValid(ipSeg)) {
                ip.add(ipSeg);
                restoreIp(res, ip, s, i + 1);
                ip.remove(ip.size() - 1);
            }
        }
    }

    private static boolean isIpValid(String s) {
        if (s == null || s.length() == 0) return false;
        if (s.charAt(0) == '0') return s.equals("0");
        int ipInt = Integer.parseInt(s);
        return ipInt >= 0 && ipInt <= 255;
    }

}
