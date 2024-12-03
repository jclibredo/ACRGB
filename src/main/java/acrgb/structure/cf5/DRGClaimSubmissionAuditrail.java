/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.structure.cf5;

import lombok.Data;

/**
 *
 * @author MinoSun
 */
@Data
public class DRGClaimSubmissionAuditrail {

    public DRGClaimSubmissionAuditrail() {
    }
    private String datetime;
    private String details;
    private String series;
    private String status;
    private String claimnumber;
    private String filecontent;

}
