/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.method;

import acrgb.structure.ACRGBWSResult;
import acrgb.structure.HealthCareFacility;
import acrgb.utility.Utility;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;
import oracle.jdbc.OracleTypes;

/**
 *
 * @author ACR_GB
 */
@RequestScoped
public class GetHCFMultiplePMCCNO {

    public GetHCFMultiplePMCCNO() {
    }
    
    private final Utility utility = new Utility();
    
    public ACRGBWSResult GETFACILITYBYCODE(
            final DataSource datasource,
            final String hcfrid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKG.GETFACILITY(:hcfrid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("hcfrid", hcfrid.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                HealthCareFacility hcf = new HealthCareFacility();
                hcf.setHcfname(resultset.getString("HCFNAME"));
                hcf.setHcfaddress(resultset.getString("HCFADDRESS"));
                hcf.setHcfcode(resultset.getString("HCFCODE"));
                hcf.setType(resultset.getString("HCFTYPE"));
                hcf.setHcilevel(resultset.getString("HCILEVEL"));
                hcf.setStreet(resultset.getString("STREET"));
                result.setResult(utility.ObjectMapper().writeValueAsString(hcf));
                result.setSuccess(true);
                result.setMessage("OK");
            } else {
                result.setMessage("NO RECORD FOUND");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GetHCFMultiplePMCCNO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    public ACRGBWSResult GETFACILITYBYNAME(
            final DataSource datasource,
            final String uhcfname,
            final String ustreet) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKG.GETFACILITYBYNAME(:uhcfname,:ustreet); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("uhcfname", uhcfname.trim());
            statement.setString("ustreet", ustreet.trim());
            statement.execute();
            ArrayList<HealthCareFacility> hcfList = new ArrayList<>();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            while (resultset.next()) {
                HealthCareFacility hcf = new HealthCareFacility();
                hcf.setHcfname(resultset.getString("HCFNAME"));
                hcf.setHcfaddress(resultset.getString("HCFADDRESS"));
                hcf.setHcfcode(resultset.getString("HCFCODE"));
                hcf.setType(resultset.getString("HCFTYPE"));
                hcf.setHcilevel(resultset.getString("HCILEVEL"));
                hcf.setStreet(resultset.getString("STREET"));
                hcfList.add(hcf);
            }
            if (hcfList.size() > 0) {
                result.setResult(utility.ObjectMapper().writeValueAsString(hcfList));
                result.setSuccess(true);
                result.setMessage("OK");
            } else {
                result.setMessage("NO RECORD FOUND");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GetHCFMultiplePMCCNO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
}
