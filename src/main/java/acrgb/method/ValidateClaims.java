/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.method;

import acrgb.structure.ACRGBWSResult;
import acrgb.utility.Utility;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;

/**
 *
 * @author MinoSun
 */
@RequestScoped
public class ValidateClaims {

    private final Utility utility = new Utility();

    public ACRGBWSResult ValidateClaims(final DataSource dataSource, final String upmccno, final String useries) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        
        
        
        
        
        
        
        

        return result;
    }

}
