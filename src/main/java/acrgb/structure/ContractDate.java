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
public class ContractDate {

    public ContractDate() {
    }
    private String condateid;
    private String status;
    private String datefrom;
    private String dateto;
    private String createdby;
    private String datecreated;
    private String accountunder;

}
