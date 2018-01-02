package com.wallace.demo.app.parsexml;

import java.util.ArrayList;

/**
 * com.wallace.demo.app.parsexml
 * Created by 10192057 on 2017/12/11 0011.
 */
public class MRO {
    /**
     * eNB id *
     */
    private String eNB;
    /**
     * HUA WEI SMR
     **/
//    private HashMap<String, HW_ASmr> hw_smr = new HashMap<String, HW_ASmr>();
    private ArrayList<HW_ASmr> hw_smr = new ArrayList<HW_ASmr>(2 ^ 16);
    /**
     * Plr smr*
     */
//    private HashMap<String, ASmrPlr> smrPlr = new HashMap<String, ASmrPlr>();
    private ArrayList<ASmrPlr> smrPlr = new ArrayList<ASmrPlr>(2 ^ 16);

    /*
     * eNB get/set
    */
    public String geteNB() {
        return eNB;
    }

    public void seteNB(String eNB) {
        this.eNB = eNB;
    }

    /*
     * hw_smr get/set
    */
    public ArrayList<HW_ASmr> getHw_smr() {
        return hw_smr;
    }

    public void setHw_smr(ArrayList<HW_ASmr> hw_smr) {
        this.hw_smr = hw_smr;
    }

    /*
     * smrPlr get/set
     */
    public ArrayList<ASmrPlr> getsmrPlr() {
        return smrPlr;
    }

    public void setsmrPlr(ArrayList<ASmrPlr> smrPlr) {
        this.smrPlr = smrPlr;
    }

}
