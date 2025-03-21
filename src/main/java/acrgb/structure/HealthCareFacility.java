/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.structure;

import lombok.Data;

/**
 *
 * @author DRG_SHADOWBILLING
 */
@Data
public class HealthCareFacility {

    public HealthCareFacility() {
    }
    private String hcfid;
    private String hcfname;
    private String hcfaddress;
    private String mainaccre;
    private String hcfcode;
    private String createdby;
    private String type;
    private String datecreated;
    private String proid;
    private String mb;
    private String amount;
    private String totalclaims;
    private String gbtags;
    private String baseamount;
    private String remainingbalance;
    private String hcilevel;
    private String street;
}
