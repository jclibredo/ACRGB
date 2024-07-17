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
public class UserActivity {

    public UserActivity() {
    }
    private String actid;
    private String actdate;
    private String actdetails;
    private String actby;
    private String userlevel;
    private String actstatus;
}
