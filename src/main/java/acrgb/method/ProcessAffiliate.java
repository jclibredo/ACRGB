/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.method;

import acrgb.structure.ACRGBWSResult;
import acrgb.structure.Appellate;
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
 * @author DRG_SHADOWBILLING
 */
@RequestScoped
public class ProcessAffiliate {

    private final Utility utility = new Utility();

    public ACRGBWSResult GETAFFILIATE(
            final DataSource dataSource, 
            final String uhcicode, 
            final String uhcpn, 
            final String ucondateid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETAFFILIATE(:uhcicode,:uhcpn,:ucondateid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("uhcicode", uhcicode);
            statement.setString("uhcpn", uhcpn);
            statement.setString("ucondateid", ucondateid);
            statement.execute();
            ArrayList<Appellate> affiliateList = new ArrayList<>();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            while (resultset.next()) {
                Appellate affiliate = new Appellate();
                affiliate.setAccesscode(resultset.getString("ACCESSCODE"));
                affiliate.setControlcode(resultset.getString("CONTROLCODE"));
                affiliateList.add(affiliate);
            }
            if (affiliateList.size() > 0) {
                result.setSuccess(true);
                result.setMessage("OK");
                result.setResult(utility.ObjectMapper().writeValueAsString(affiliateList));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(ProcessAffiliate.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
