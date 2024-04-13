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
public class Accreditation {

    public Accreditation() {
    }
    private String accreno;
    private String datefrom;
    private String dateto;
    private String datecreated;
    private String createdby;
    private String status;

}
