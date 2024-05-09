/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.method;

import acrgb.structure.ACRGBWSResult;
import acrgb.structure.ReportsParam;
import acrgb.utility.Utility;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;

/**
 *
 * @author MinoSun
 */
@RequestScoped
public class Reports {
    
    private final Utility utility = new Utility();
    
    public ACRGBWSResult GetReports(final DataSource dataSource, final ReportsParam reports) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setSuccess(false);
        result.setResult("");
        
        switch (Integer.parseInt(reports.getAccount())) {
            case 1:
                
                break;
            case 2:
                break;
            
            case 3:
                break;
            
            case 4:
                break;
            
            case 5:
                break;
            
            case 6:
                break;
            
            case 7:
                break;
            
        }
        
        
        
        return result;
    }
    
}
