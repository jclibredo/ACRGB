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
    private String accreno;
    private String datesubmitted;
    private String claimamount;
    private String series;
    private String claimid;
    private String tags;
    private String totalclaims;
}
