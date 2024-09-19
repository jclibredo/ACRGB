/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.structure;

import lombok.Data;

/**
 *
 * @author MinoSun
 */
@Data
public class Contract {

    private String conid;//THIS
    private String hcfid;
    private String amount;
    private String stats; // THIS
    private String createdby;
    private String datecreated;
    private String contractdate;
    private String transcode;
    private String baseamount;
    private String enddate;// THIS
    private String remarks;// THIS
    private String remainingbalance;
    private String traches;
    private String totalclaims;
    private String percentage;
    private String comittedClaimsVol;
    private String computedClaimsVol;
    private String sb;
    private String addamount;
    private String totaltrancheamount;
    private String totalclaimsamount;
    private String totalclaimspercentage;
    private String quarter;
    private String totalamountrecieved;

}
