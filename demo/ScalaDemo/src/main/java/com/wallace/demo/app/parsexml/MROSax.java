package com.wallace.demo.app.parsexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * com.wallace.demo.app.parsexml
 * Created by 10192057 on 2017/12/11 0011.
 */
public class MROSax extends DefaultHandler {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private HW_ASmr asmr;
    private MRO mro;
    private HW_Field field;
    private String preTag = "";
    private String flag = "";
    private int nullFlag = 0;
    //    private String skey = "";
    private NorthMRInfoHW handler = new NorthMRInfoHW();
    private ASmrPlr asmrPlr;
    //    private ASmrRIP asmrRIP;
    private StringBuffer xmldata;

    @Override
    public void startDocument() throws SAXException {
        asmr = new HW_ASmr();
        mro = new MRO();
        asmrPlr = new ASmrPlr();
//        asmrRIP = new ASmrRIP();
        xmldata = new StringBuffer();
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (mro != null && !flag.equals("RIP")) {
            if ("v".equals(preTag) && nullFlag == 0) {
                String data = new String(ch, start, length);
                xmldata.append(data);
            } else if ("smr".equals(preTag)) {
                String data = new String(ch, start, length);
                xmldata.append(data);
            }
        }
    }

    public void SplicePlr(String data) {
        try {
            String[] values = data.split(" ");
            String sTemp = "";
            for (int i = 0; i < 9; i++) {
                if (values[i].equals("NIL")) {
                    values[i] = "";
                }
                sTemp += values[i] + "$";
            }
            asmrPlr.setLteScPlrULQci(sTemp.substring(0, sTemp.length() - 1));
            sTemp = "";
            for (int i = 9; i < 18; i++) {
                if (values[i].equals("NIL")) {
                    values[i] = "";
                }
                sTemp += values[i] + "$";
            }
            asmrPlr.setLteScPlrDLQci(sTemp.substring(0, sTemp.length() - 1));
        } catch (Exception e) {
            //log
        }
    }

    public void SetField(String data) {
        String[] values = data.split(" ");
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals("NIL")) {
                values[i] = "";
            }
        }
        if (asmr.getFieldSpliced().getLteScPci().equals("") && asmr.getFieldSpliced().getLteScRSRP().equals("")
                && asmr.getFieldSpliced().getLteScPHR().equals("")) {
//            log.error("empty:"+asmr.getFieldSpliced().getLteScPci());
//            log.error("empty:"+asmr.getFieldSpliced().getLteScRSRP());
            field.SetField(handler, values);
            asmr.setField(field);

        } else {
//            log.error(asmr.getFieldSpliced().getLteScPci());
//            log.error(asmr.getFieldSpliced().getLteScRSRP());
            field.UpdateField(handler, values);
            asmr.setField(field);
        }
    }

    @Override
    public void startElement(String uri, String localName, String name, Attributes attr) throws SAXException {
        preTag = name;
        if (flag.equals("RIP")) {
            return;
        }
        if ("object".equals(name)) {

            String mmec = attr.getValue("MmeCode");
            String mmeS1ApId = attr.getValue("MmeUeS1apId");
            String mmeGroupId = attr.getValue("MmeGroupId");

            if (!(mmec.equals("NIL") && mmeS1ApId.equals("NIL") && mmeGroupId.equals("NIL"))) {
                if (flag.equals("smr")) {
                    String cellid = GetCellID(attr.getValue("id"));
                    asmr.setCellId(cellid);
                    asmr.setMme(mmec);
                    asmr.setMmeGroupID(mmeGroupId);

                    if ("-1".equals(mmeS1ApId)) {
                        mmeS1ApId = "";
                    }
                    asmr.setMmeS1ApID(mmeS1ApId);
                    asmr.setTimeStamp(attr.getValue("TimeStamp"));
                    field = new HW_Field();
                }
                nullFlag = 0;
            } else {
                nullFlag = 1;
            }
        } else if ("eNB".equals(name)) {
            mro.seteNB(attr.getValue("id"));
        }
    }


    /**
     * 获取每条MR记录的小区ID，xml中文件格式可能为“648548-49”或者“648548-49:1825:0”
     *
     * @param value， object结点中id的值，可能为“648548-49”或者“648548-49:1825:0”
     * @return 小区ID，如49
     */
    public String GetCellID(String value) {
        String CellID = "";
        try {
            String[] subValue = value.split(":");
            CellID = subValue[0].substring(subValue[0].indexOf("-") + 1);
        } catch (Exception e) {
            log.error("HW_MROSax: fail to split cellid, error is " + e.getMessage());
        }

        return CellID;
    }

    @Override
    public void endElement(String uri, String localName, String name) throws SAXException {
        if(flag.equals("RIP")){
            return;
        }
        if ("v".equals(preTag) && nullFlag == 0) {
            String data = xmldata.toString();
            if(flag.equals("Plr")){
                //QCI的拼接及存储属性值
                SplicePlr(data);
            }else if(flag.equals("smr")) {
                //填充Field的属性值
                SetField(data);
            }
        }else if("smr".equals(preTag)) {
            String data = xmldata.toString();
            if(data.contains("MR.LteScRIP"))
            {
                flag = "RIP";
            }
            else if(data.contains("MR.LteScPci"))
            {
                flag = "smr";
                handler.setNorthMRInfo(data);
            }
            else if(data.contains("MR.LteScPlrULQci"))
            {
                flag = "Plr";
            }
        }

        if (asmr != null && nullFlag == 0 && "object".equals(name)) {
            //获取第二个smr对应的object
            if (flag.equals("Plr")) {
                mro.getsmrPlr().add(asmrPlr);
                asmrPlr = new ASmrPlr();
            }
            //获取第一个smr对应的object
            else {
                mro.getHw_smr().add(asmr);
                asmr = new HW_ASmr();
            }
        }

        preTag = null;
        xmldata.delete(0, xmldata.length());
    }

    public MRO getMRO() {
        return mro;
    }
}
