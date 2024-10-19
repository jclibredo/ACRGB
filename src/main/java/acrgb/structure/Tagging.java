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
public class Tagging {
    public Tagging() {
    }
    private String hcino;
    private String ancservicetype;
    private String rno;
    private String licno;
    private String startdate;
    private String expireddate;
    private String liccategory;
    private String username;
    private String entrydate;
    private String entrymode;
    private String issuedate;
    private String effdate;

}
