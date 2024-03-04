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
public class MBRequestSummary {

    public MBRequestSummary() {
    }

    private String mbrid;
    private String totalamount;
    private String daterequest;
    private String yearfrom;
    private String yearto;
    private String requestor;
    private String transcode;
    private String reqstatus;
    private String remarks;
    private String facility;

}
