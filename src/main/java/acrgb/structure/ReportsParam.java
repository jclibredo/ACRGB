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
public class ReportsParam {

    public ReportsParam() {
    }
    private String datefrom;
    private String dateto;
    private String contract;
    private String account;

}
