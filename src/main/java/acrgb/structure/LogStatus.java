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
public class LogStatus {

    public LogStatus() {
    }
    private String account;
    private String status;
    private String datechange;
    private String actby;
    private String remarks;
    private String datefrom;
    private String dateto;

}
