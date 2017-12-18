package com.wallace.demo.app.parsexml;

import java.util.HashMap;

/**
 * com.wallace.demo.app.parsexml
 * Created by 10192057 on 2017/12/11 0011.
 */
public class NorthMRInfoHW {
    private static final NorthMRInfoHW instance = new NorthMRInfoHW();

    public static NorthMRInfoHW getInstance() {
        return instance;
    }

    public String GetFieldValueByName(String[] ndsMR, String sFiledName) {
        int iColumnIndex = GetColumnIndex(sFiledName);
        if (iColumnIndex < 0 || iColumnIndex >= ndsMR.length) {
            return "";
        }
        return ndsMR[iColumnIndex].trim();
    }

    public int GetColumnIndex(String ColumnName) {
        if (m_mNorthMRInfoHW == null) {
            return -1;
        }
        if (!m_mNorthMRInfoHW.containsKey(ColumnName)) {
            return -1;
        }
        return m_mNorthMRInfoHW.get(ColumnName);
    }

    /**
     * 设置北向smr节点各个字段的位置信息列表
     */
    public int setNorthMRInfo(String data) {
        try {
            String[] Result = data.split(" ");
            String sField = "";
            for (int i = 0; i < Result.length; i++) {
                sField = Result[i].toUpperCase();
                if (!sField.startsWith("MR."))
                    sField = "MR." + sField;
                m_mNorthMRInfoHW.put(sField, i);
            }
            return 0;
        } catch (Exception e) {
            m_mNorthMRInfoHW = null;
            return -1;
        }
    }

    private HashMap<String, Integer> m_mNorthMRInfoHW = new HashMap<String, Integer>();
}
