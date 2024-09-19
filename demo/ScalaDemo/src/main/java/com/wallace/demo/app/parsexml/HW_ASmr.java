package com.wallace.demo.app.parsexml;

/**
 * com.wallace.demo.app.parsexml
 * Created by 10192057 on 2017/12/11 0011.
 */
public class HW_ASmr {
    private String CellId = "";
    private String TimeStamp = "";
    private String mme = "";
    private String mmeGroupID = "";
    private String mmeS1ApID = "";
    private HW_Field FieldSpliced = new HW_Field();

    public String getCellId() {
        return CellId;
    }

    public void setCellId(String cid) {
        this.CellId = cid;
    }

    public String getTimeStamp() {
        return TimeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.TimeStamp = timeStamp;
    }

    public HW_Field getFieldSpliced() {
        return FieldSpliced;
    }

    public void setField(HW_Field field) {
        this.FieldSpliced = field;
    }

    public String getMme() {
        return mme;
    }

    public void setMme(String mme) {
        this.mme = mme;
    }

    public String getMmeGroupID() {
        return mmeGroupID;
    }

    public void setMmeGroupID(String mmeGroupID) {
        this.mmeGroupID = mmeGroupID;
    }

    public String getMmeS1ApID() {
        return mmeS1ApID;
    }

    public void setMmeS1ApID(String mmeS1ApID) {
        this.mmeS1ApID = mmeS1ApID;
    }

}
