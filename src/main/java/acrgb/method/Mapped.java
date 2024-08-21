/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.method;

import acrgb.structure.ACRGBWSResult;
import acrgb.utility.Utility;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;
import oracle.jdbc.OracleTypes;

/**
 *
 * @author MinoSun
 */
@RequestScoped
public class Mapped {

    private final Utility utility = new Utility();

    public ACRGBWSResult GetHistoryResult(final DataSource dataSource) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setSuccess(false);
        result.setResult("");

        return result;
    }

    public ACRGBWSResult GETMAXCONDATE(final DataSource dataSource, final String puserid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setSuccess(false);
        result.setResult("");
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETMAXCONDATE(:puserid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("puserid", puserid.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                result.setSuccess(true);
                result.setResult(resultset.getString("CONDATE"));
                result.setMessage("OK");
            } else {
                result.setMessage("NO DATE FOUND");
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Mapped.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

//    public ACRGBWSResult GETPREVIOUSMAP(final DataSource dataSource, final String puserid) {
//        ACRGBWSResult result = utility.ACRGBWSResult();
//        result.setMessage("");
//        result.setResult("");
//        FetchMethods fm = new FetchMethods();
//        Methods m = new Methods();
//        result.setSuccess(false);
//        try (Connection connection = dataSource.getConnection()) {
//            if (this.GETMAXCONDATE(dataSource, puserid).isSuccess()) {
//                CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETPREVIOUSMAP(:puserid,:pcondate); end;");
//                statement.registerOutParameter("v_result", OracleTypes.CURSOR);
//                statement.setString("puserid", puserid.trim());
//                statement.setString("pcondate", this.GETMAXCONDATE(dataSource, puserid).getResult().trim());
//                statement.execute();
//                ArrayList<HealthCareFacility> hciList = new ArrayList<>();
//                ArrayList<ManagingBoard> mbList = new ArrayList<>();
//                ResultSet resultset = (ResultSet) statement.getObject("v_result");
//                while (resultset.next()) {
//                    if (fm.GETFACILITYID(dataSource, resultset.getString("ACCESSID")).isSuccess()) {
//                        HealthCareFacility hci = utility.ObjectMapper().readValue(fm.GETFACILITYID(dataSource, resultset.getString("ACCESSID")).getResult(), HealthCareFacility.class);
//                        hciList.add(hci);
//                    } else if (m.GETMBWITHID(dataSource, resultset.getString("ACCESSID")).isSuccess()) {
//                        ManagingBoard mb = utility.ObjectMapper().readValue(m.GETMBWITHID(dataSource, resultset.getString("ACCESSID")).getResult(), ManagingBoard.class);
//                        mbList.add(mb);
//                    }
//                }
//                if (hciList.size() > 0) {
//                    result.setMessage(resultset.getString("USERID").trim());
//                    result.setSuccess(true);
//                    result.setResult(utility.ObjectMapper().writeValueAsString(hciList));
//                } else if (mbList.size() > 0) {
//                    result.setMessage(resultset.getString("USERID").trim());
//                    result.setSuccess(true);
//                    result.setResult(utility.ObjectMapper().writeValueAsString(mbList));
//                } else {
//                    result.setMessage("NO DATA FOUND");
//                }
//            }
//        } catch (SQLException | IOException ex) {
//            result.setMessage(ex.toString());
//            Logger.getLogger(Mapped.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return result;
//    }

}
