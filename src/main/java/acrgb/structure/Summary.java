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
public class Summary {

    public Summary() {
    }
    private String hcfid;
    private String accreno;
    private String tranchcount;
    private String totalpercentage;
    private String remarks;
    private String totalclaims;

}
