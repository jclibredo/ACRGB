/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.method;

import acrgb.structure.ACRGBWSResult;
import acrgb.structure.Tagging;
import acrgb.utility.Utility;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
public class FacilityTagging {

    public FacilityTagging() {
    }

    private final Utility utility = new Utility();

    public ACRGBWSResult TaggFacility(final DataSource datasource, final Tagging tagging, final String servicetype) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
//        java.util.Date d1 = new java.util.Date();
        ArrayList<String> errorList = new ArrayList<>();
        try (Connection connection = datasource.getConnection()) {
            List<String> serviceList = Arrays.asList(servicetype.split(","));
            for (int i = 0; i < serviceList.size(); i++) {
                CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBFACILITYTAGGING(:Message,:Code,:upmcc_no,:uservicetype,:udatestart,:udateended,:ucreator,:udatecreated,:udateissue,:udateeffective)");
                getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                getinsertresult.setString("upmcc_no", tagging.getHcino());
                getinsertresult.setString("uservicetype", serviceList.get(i).trim().toUpperCase());
                getinsertresult.setDate("udatestart", (Date) new Date(utility.StringToDate(tagging.getStartdate()).getTime()));//tranch.getDatecreated());
                getinsertresult.setDate("udateended", (Date) new Date(utility.StringToDate(tagging.getExpireddate()).getTime()));//tranch.getDatecreated());
                getinsertresult.setString("ucreator", tagging.getUsername());
                getinsertresult.setTimestamp("udatecreated", new java.sql.Timestamp(utility.GetCurrentDate().getTime()));//tranch.getDatecreated());
                getinsertresult.setDate("udateissue", (Date) new Date(utility.StringToDate(tagging.getIssuedate()).getTime()));//tranch.getDatecreated());
                getinsertresult.setDate("udateeffective", (Date) new Date(utility.StringToDate(tagging.getEffdate()).getTime()));//tranch.getDatecreated());
                getinsertresult.execute();
                if (!getinsertresult.getString("Message").equals("SUCC")) {
                    errorList.add(getinsertresult.getString("Message"));
                }
            }
            if (errorList.size() > 0) {
                result.setMessage(errorList.toString());
            } else {
                result.setMessage("OK");
                result.setSuccess(true);
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FacilityTagging.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

//    public ACRGBWSResult TaggApexFacility(final DataSource datasource, Tagging tagging) {
//        ACRGBWSResult result = utility.ACRGBWSResult();
//        result.setMessage("");
//        result.setResult("");
//        result.setSuccess(false);
//        try (Connection connection = datasource.getConnection()) {
//            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBFACILITYTAGGING(:Message,:Code,:upmcc_no,:uservicetype,:udatestart,:udateended,:ucreator,:udatecreated,:udateissue,:udateeffective)");
//            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
//            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
//            getinsertresult.setString("upmcc_no", tagging.getHcino());
//            getinsertresult.setString("uservicetype", tagging.getAncservicetype());
//            getinsertresult.setDate("udatestart", (Date) new Date(utility.StringToDate(tagging.getStartdate()).getTime()));//tranch.getDatecreated());
//            getinsertresult.setDate("udateended", (Date) new Date(utility.StringToDate(tagging.getExpireddate()).getTime()));//tranch.getDatecreated());
//            getinsertresult.setString("ucreator", tagging.getUsername());
//            getinsertresult.setDate("udatecreated", (Date) new Date(utility.StringToDate(tagging.getEntrydate()).getTime()));//tranch.getDatecreated());
//            getinsertresult.setDate("udateissue", (Date) new Date(utility.StringToDate(tagging.getIssuedate()).getTime()));//tranch.getDatecreated());
//            getinsertresult.setDate("udateeffective", (Date) new Date(utility.StringToDate(tagging.getEffdate()).getTime()));//tranch.getDatecreated());
//            getinsertresult.execute();
//            if (getinsertresult.getString("Message").equals("SUCC")) {
//                result.setSuccess(true);
//                result.setMessage(getinsertresult.getString("Message"));
//            } else {
//                result.setMessage(getinsertresult.getString("Message"));
//            }
//        } catch (SQLException ex) {
//            result.setMessage(ex.toString());
//            Logger.getLogger(FacilityTagging.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return result;
//    }
}
