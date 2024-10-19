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
public class Email {

    public Email() {
    }
    private String sender;
    private String emailto;
    private String subject;
    private String content;
    private String appuser;
    private String apppass;
    private String port;
    private String host;

}
