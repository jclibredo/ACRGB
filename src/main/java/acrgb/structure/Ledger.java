/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.structure;

import lombok.Data;

/**
 *
 * @author ACR_GB
 */
@Data
public class Ledger {

    public Ledger() {
    }
    private String datetime;//DATE OF TRANSACTION
    private String particular;// SOURCE OF FUND OR TYPE
    private String credit;//AMOUNT INSERTED OR RELEASED
    private String totalclaims;//TOTAL NUMBER OF GOOD CLAIMS
    private String liquidation;//TOTAL CLAIMS AMOUNT
    private String balance;//REMAINING BALANCE
    private String contractnumber; //CONTRACT UNDER
    private String debit;
    private String facility;
    private String account;
    private String voucher;
}
