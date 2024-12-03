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
public class Book {

    public Book() {
    }
    private String booknum;
    private String conid;
    private String datecreated;
    private String createdby;
    private String hcpncode;
    private String tags;

}
