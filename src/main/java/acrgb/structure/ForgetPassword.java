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
public class ForgetPassword {

    private String emailto;
    private String appuser;
    private String apppass;
    private String createdby;
    private String datecreated;
    private String mailhost;
    private String mailport;
    private String mailfrom;
}
