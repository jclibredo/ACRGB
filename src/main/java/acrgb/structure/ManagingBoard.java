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
public class ManagingBoard {

    public ManagingBoard() {
    }
    private String mbid;
    private String mbname;
    private String datecreated;
    private String createdby;
    private String status;
    private String controlnumber;
    private String baseamount;
    private String licensedatefrom;//DATE OF LICENSE EXPIRED
    private String licensedateto;//
}
