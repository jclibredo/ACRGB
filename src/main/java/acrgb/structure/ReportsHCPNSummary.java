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
public class ReportsHCPNSummary {

    public ReportsHCPNSummary() {
    }

    private String totalnumberofreleased;//OK
    private String contractadatefrom;
    private String contractadateto;
    private String contractnumber;//OK
    private String conctractamount;//OK
    private String amountrelease;//OK
    private String hcpnname;//OK
    private String remainingbal;//OK

}
