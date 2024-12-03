/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.structure.cf5;

import lombok.Data;

/**
 *
 * @author ACR_GB
 */
@Data
public class Info {

    public Info() {
    }
    private String pdx;
    private String NBTimeofbirth;
    private String NBweight;
    private String claimnumber;
    private String hospitalcode;
    private String series;
    private String procedure;
    private String secondary;
    private String warningerr;

}
