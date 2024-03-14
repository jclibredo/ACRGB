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
public class FacilityComputedAmount {

    public FacilityComputedAmount() {
    }
    private String hospital;
    private String yearfrom;
    private String yearto;
    private String totalamount;
    private String totalclaims;

}
