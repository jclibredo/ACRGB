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
public class UserRoleIndex {

    public UserRoleIndex() {
    }

    private String roleid;
    private String userid;
    private String accessid;
    private String createdby;
    private String datecreated;
    private String status;
    private String contractdate;
}
