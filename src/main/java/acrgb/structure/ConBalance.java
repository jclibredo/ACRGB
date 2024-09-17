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
public class ConBalance {

    public ConBalance() {
    }
    private String booknum; //BOPOKING NUMBER
    private String condateid; //CONTRACT DATE PERION ID
    private String account;  //ACCOUNT HCPN OR FACILITY
    private String conbalance; //REMAINING BALANCE OF CONTRACT
    private String conamount; //CONTRACT AMOUNT
    private String conutilized; // TOTAL UTILIZED BUDGET
    private String datecreated; // DATECREATED
    private String status;  //STATUS
    private String conid;  //CONTRACT ID
    private String createdby; //CREATED BY
    private String claimscount;
}
