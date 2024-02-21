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
public class User {

    private String userid;
    private String leveid;
    private String username;
    private String userpassword;
    private String datecreated;
    private String status;
    private String createdby;
    private String did;
    //1 FOR REQUIRED
    //2 ACTIVE
    //3 INACTIVE

}
