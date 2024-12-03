/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.structure;

import lombok.Data;

/**
 *
 * @author ACR_GB
 */
@Data
public class MBFacilityUnderRequest {

    public MBFacilityUnderRequest() {
    }

    private String transcode;
    private String facility;
    private String datecreated;

}
