/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.structure;


import java.util.Date;
import lombok.Data;

/**
 *
 * @author DRG_SHADOWBILLING
 */
@Data
public class ACRGBPayload {

    private String Code1;
    private String Code2;
    private Date exp;
}
