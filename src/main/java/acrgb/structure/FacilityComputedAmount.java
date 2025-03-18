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
public class FacilityComputedAmount {

    public FacilityComputedAmount() {
    }
    private String hospital;
    private String yearfrom;
    private String yearto;
    private String totalamount;
    private String totalclaims;
    private String datefiled;
    private String dateadmit;
    private String daterefiled;
    private String sb;
    private String thirty;
    private String c1rvcode;
    private String c2rvcode;
    private String c1icdcode;
    private String c2icdcode;
    private String series;

}
