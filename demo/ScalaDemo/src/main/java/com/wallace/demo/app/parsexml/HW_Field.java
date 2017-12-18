package com.wallace.demo.app.parsexml;

/**
 * com.wallace.demo.app.parsexml
 * Created by 10192057 on 2017/12/11 0011.
 */
public class HW_Field {

    private String LteScEarfcn = "";
    private String LteScPci = "";
    private String LteScRSRP = "";
    private String LteScRSRQ = "";
    private String LteScTadv = "";
    private String LteScPHR = "";
    private String LteScAOA = "";
    private String LteScSinrUL = "";
    private String LteNcEarfcn = "";
    private String LteNcPci = "";
    private String LteNcRSRP = "";
    private String LteNcRSRQ = "";
    private String LteScRIP = "";
    private String UlPdcpPktLossCtn = "";
    private String DlPdcpPktLossCtn = "";


    private String LteScRI = "";
    private String CQI = "";
    private String LteScPUSCHPRBNum = "";

    private String LteScPDSCHPRBNum = "";
    private String LteScBSR = "";
    private String CDMAtype = "";

    private String CDMANcBand = "";
    private String CDMANcArfcn = "";
    private String CDMAPNphase = "";
    private String LteCDMAorHRPDNcPilotStrength = "";
    private String CDMANcPci = "";

    private String Longitude = "";
    private String Latitude = "";

    public void SetField(NorthMRInfoHW handler, String[] values) {
        this.LteScEarfcn = handler.GetFieldValueByName(values, "MR.LTESCEARFCN");
        //System.out.println(this.LteScEarfcn );
        this.LteScPci = handler.GetFieldValueByName(values, "MR.LTESCPCI");
        this.LteScRSRP = handler.GetFieldValueByName(values, "MR.LTESCRSRP");
        this.LteScRSRQ = handler.GetFieldValueByName(values, "MR.LTESCRSRQ");
        this.LteScTadv = handler.GetFieldValueByName(values, "MR.LTESCTADV");
        this.LteScPHR = handler.GetFieldValueByName(values, "MR.LTESCPHR");
        this.LteScAOA = handler.GetFieldValueByName(values, "MR.LTESCAOA");
        this.LteScSinrUL = handler.GetFieldValueByName(values, "MR.LTESCSINRUL");
        this.LteNcEarfcn = handler.GetFieldValueByName(values, "MR.LTENCEARFCN");
        this.LteNcPci = handler.GetFieldValueByName(values, "MR.LTENCPCI");
        this.LteNcRSRP = handler.GetFieldValueByName(values, "MR.LTENCRSRP");
        this.LteNcRSRQ = handler.GetFieldValueByName(values, "MR.LTENCRSRQ");
        this.LteScRIP = handler.GetFieldValueByName(values, "MR.LTESCRIP");
//        this.UlPdcpPktLossCtn = GetULPktLossCtn(handler, values);
//        this.DlPdcpPktLossCtn = GetDLPktLossCtn(handler, values);

        this.LteScRI = handler.GetFieldValueByName(values, "MR.LTESCRI");
        this.CQI = handler.GetFieldValueByName(values, "MR.CQI");
        this.LteScPUSCHPRBNum = handler.GetFieldValueByName(values, "MR.LTESCPUSCHPRBNUM");
        this.LteScPDSCHPRBNum = handler.GetFieldValueByName(values, "MR.LTESCPDSCHPRBNUM");
        this.LteScBSR = handler.GetFieldValueByName(values, "MR.LTESCBSR");
        this.CDMAtype = handler.GetFieldValueByName(values, "MR.CDMATYPE");
        this.CDMANcBand = handler.GetFieldValueByName(values, "MR.CDMANCBAND");
        this.CDMANcArfcn = handler.GetFieldValueByName(values, "MR.CDMANCARFCN");

        this.CDMAPNphase = handler.GetFieldValueByName(values, "MR.CDMAPNPHASE");
        this.LteCDMAorHRPDNcPilotStrength = handler.GetFieldValueByName(values, "MR.LTECDMAORHRPDNCPILOTSTRENGTH");
        this.CDMANcPci = handler.GetFieldValueByName(values, "MR.CDMANCPCI");
        this.Longitude = GetLonLat(handler.GetFieldValueByName(values, "MR.LONGITUDE"));
        this.Latitude = GetLonLat(handler.GetFieldValueByName(values, "MR.LATITUDE"));
    }

    private String GetLonLat(String str) {
        if (str.length() == 0)
            return str;

        String ss = str.substring(str.length() - 1);

        if (ss.equals("E") || ss.equals("S") || ss.equals("W") || ss.equals("N")) {
            return str.substring(0, str.length() - 1);
        } else {
            return str;
        }

    }

    /**
     * 获取1-9上行QCI值，作为丢包数，以$分割拼接起来
     *
     * @param values
     * @return
     */
    public String GetULPktLossCtn(NorthMRInfoHW handler, String[] values) {
        String qci = "";
        for (int i = 0; i <= 9; i++) {
            //获取每个通道的QCI值
            String ulqci = handler.GetFieldValueByName(values, "MR.LteScPlrULQci" + i);
            if (ulqci.equals("NIL")) {
                ulqci = "";
            }

            qci += ulqci + "$";
        }
        return qci.substring(0, qci.length() - 1);
    }

    /**
     * 获取1-9上行QCI值，作为丢包数，以$分割拼接起来
     *
     * @param values
     * @return
     */
    public String GetDLPktLossCtn(NorthMRInfoHW handler, String[] values) {
        String qci = "";
        for (int i = 0; i <= 9; i++) {
            //获取每个通道的QCI值
            String dlqci = handler.GetFieldValueByName(values, "MR.LteScPlrDLQci" + i);
            if (dlqci.equals("NIL")) {
                dlqci = "";
            }
            qci += dlqci + "$";
        }
        return qci.substring(0, qci.length() - 1);
    }

    /**
     * 拼接邻区信息
     *
     * @param values
     */
    public void UpdateField(NorthMRInfoHW handler, String[] values) {
        this.LteNcEarfcn = this.LteNcEarfcn + "$" + handler.GetFieldValueByName(values, "MR.LTENCEARFCN");
        this.LteNcPci = this.LteNcPci + "$" + handler.GetFieldValueByName(values, "MR.LTENCPCI");
        this.LteNcRSRP = this.LteNcRSRP + "$" + handler.GetFieldValueByName(values, "MR.LTENCRSRP");
        this.LteNcRSRQ = this.LteNcRSRQ + "$" + handler.GetFieldValueByName(values, "MR.LTENCRSRQ");
    }

    public String getLteScEarfcn() {
        return LteScEarfcn;
    }

    public String getLteScPci() {
        return LteScPci;
    }

    public String getLteScRSRP() {
        return LteScRSRP;
    }

    public String getLteScRSRQ() {
        return LteScRSRQ;
    }

    public String getLteScTadv() {
        return LteScTadv;
    }

    public String getLteScPHR() {
        return LteScPHR;
    }

    public String getLteScAOA() {
        return LteScAOA;
    }

    public String getLteScSinrUL() {
        return LteScSinrUL;
    }

    public String getLteNcEarfcn() {
        return LteNcEarfcn;
    }

    public String getLteNcPci() {
        return LteNcPci;
    }

    public String getLteNcRSRP() {
        return LteNcRSRP;
    }

    public String getLteNcRSRQ() {
        return LteNcRSRQ;
    }

    public void setLteScEarfcn(String LteScEarfcn) {
        this.LteScEarfcn = LteScEarfcn;
    }

    public void setLteScPci(String LteScPci) {
        this.LteScPci = LteScPci;
    }

    public void setLteScRSRP(String LteScRSRP) {
        this.LteScRSRP = LteScRSRP;
    }

    public void setLteScRSRQ(String LteScRSRQ) {
        this.LteScRSRQ = LteScRSRQ;
    }

    public void setLteScTadv(String LteScTadv) {
        this.LteScTadv = LteScTadv;
    }

    public void setLteScPHR(String LteScPHR) {
        this.LteScPHR = LteScPHR;
    }

    public void setLteScAOA(String LteScAOA) {
        this.LteScAOA = LteScAOA;
    }

    public void setLteScSinrUL(String LteScSinrUL) {
        this.LteScSinrUL = LteScSinrUL;
    }

    public void setLteNcEarfcn(String LteNcEarfcn) {
        this.LteNcEarfcn = LteNcEarfcn;
    }

    public void setLteNcPci(String LteNcPci) {
        this.LteNcPci = LteNcPci;
    }

    public void setLteNcRSRP(String LteNcRSRP) {
        this.LteNcRSRP = LteNcRSRP;
    }

    public void setLteNcRSRQ(String LteNcRSRQ) {
        this.LteNcRSRQ = LteNcRSRQ;
    }

    public String getLteScRI() {
        return LteScRI;
    }

    public void setLteScRI(String lteScRI) {
        LteScRI = lteScRI;
    }

    public String getCQI() {
        return CQI;
    }

    public void setCQI(String cQI) {
        CQI = cQI;
    }

    public String getLteScPUSCHPRBNum() {
        return LteScPUSCHPRBNum;
    }

    public void setLteScPUSCHPRBNum(String lteScPUSCHPRBNum) {
        LteScPUSCHPRBNum = lteScPUSCHPRBNum;
    }

    public String getLteScPDSCHPRBNum() {
        return LteScPDSCHPRBNum;
    }

    public void setLteScPDSCHPRBNum(String lteScPDSCHPRBNum) {
        LteScPDSCHPRBNum = lteScPDSCHPRBNum;
    }

    public String getLteScBSR() {
        return LteScBSR;
    }

    public void setLteScBSR(String lteScBSR) {
        LteScBSR = lteScBSR;
    }

    public String getCDMAtype() {
        return CDMAtype;
    }

    public void setCDMAtype(String cDMAtype) {
        CDMAtype = cDMAtype;
    }

    public String getCDMANcBand() {
        return CDMANcBand;
    }

    public void setCDMANcBand(String cDMANcBand) {
        CDMANcBand = cDMANcBand;
    }

    public String getCDMANcArfcn() {
        return CDMANcArfcn;
    }

    public void setCDMANcArfcn(String cDMANcArfcn) {
        CDMANcArfcn = cDMANcArfcn;
    }

    public String getCDMAPNphase() {
        return CDMAPNphase;
    }

    public void setCDMAPNphase(String cDMAPNphase) {
        CDMAPNphase = cDMAPNphase;
    }

    public String getLteCDMAorHRPDNcPilotStrength() {
        return LteCDMAorHRPDNcPilotStrength;
    }

    public void setLteCDMAorHRPDNcPilotStrength(String lteCDMAorHRPDNcPilotStrength) {
        LteCDMAorHRPDNcPilotStrength = lteCDMAorHRPDNcPilotStrength;
    }

    public String getCDMANcPci() {
        return CDMANcPci;
    }

    public void setCDMANcPci(String cDMANcPci) {
        CDMANcPci = cDMANcPci;
    }

    public String getLongitude() {
        return Longitude;
    }

    public void setLongitude(String longitude) {
        Longitude = longitude;
    }

    public String getLatitude() {
        return Latitude;
    }

    public void setLatitude(String latitude) {
        Latitude = latitude;
    }

    public String getLteScRIP() {
        return LteScRIP;
    }

    public void setLteScRIP(String lteScRIP) {
        LteScRIP = lteScRIP;
    }

    public String getUlPdcpPktLossCtn() {
        return UlPdcpPktLossCtn;
    }

    public void setUlPdcpPktLossCtn(String ulPdcpPktLossCtn) {
        UlPdcpPktLossCtn = ulPdcpPktLossCtn;
    }

    public String getDlPdcpPktLossCtn() {
        return DlPdcpPktLossCtn;
    }

    public void setDlPdcpPktLossCtn(String dlPdcpPktLossCtn) {
        DlPdcpPktLossCtn = dlPdcpPktLossCtn;
    }
}
