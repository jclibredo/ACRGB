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
public class NclaimsData {

    public NclaimsData() {
    }
    private String id;
    private String accreno; //uaccreno
    private String datesubmitted; //udatefiled
    private String dateadmission; //udateadmission
    private String claimamount;  //uclaimamount
    private String series;  //useries
    private String claimid;
    private String tags;  //utags
    private String totalclaims;
    private String booknum;  //ubooknum
    private String caserate;  //ucaserate
}
