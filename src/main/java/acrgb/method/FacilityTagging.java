/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.method;

import acrgb.structure.ACRGBWSResult;
import acrgb.structure.HealthCareFacility;
import acrgb.structure.Tagging;
import acrgb.utility.Utility;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
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
 * @author DRG_SHADOWBILLING
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
        ArrayList<String> errorList = new ArrayList<>();
        try (Connection connection = datasource.getConnection()) {
            List<String> serviceList = Arrays.asList(servicetype.split(","));
            for (int i = 0; i < serviceList.size(); i++) {
                CallableStatement getinsertresult = connection.prepareCall("call DRG_SHADOWBILLING.ACRGBACCREPACKAGE.ACRGBFACILITYTAGGING(:Message,:Code,:upmcc_no,:uservicetype,:udatestart,:udateended,:ucreator,:udatecreated,:udateissue,:udateeffective)");
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
                } else {
                    if (this.CHECKIFEXIST(datasource, tagging.getHcino(), serviceList.get(i).trim().toUpperCase()).isSuccess()) {
                        ACRGBWSResult update = this.ACRGBHCITAGUPDATE(datasource, tagging, serviceList.get(i).trim().toUpperCase());
                        if (!update.isSuccess()) {
                            errorList.add(getinsertresult.getString("Message"));
                        }
                    } else {
                        ACRGBWSResult addnew = this.ACRGBHCITAGNEW(datasource, tagging, serviceList.get(i).trim().toUpperCase());
                        if (!addnew.isSuccess()) {
                            errorList.add(getinsertresult.getString("Message"));
                        }
                    }
                }
            }
            if (errorList.size() > 0) {
                result.setMessage(errorList.toString());
            } else {
                result.setMessage("OK");
                result.setSuccess(true);
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(FacilityTagging.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult CHECKIFEXIST(
            final DataSource dataSource,
            final String pmccno,
            final String servicetype) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBACCREPACKAGE.CHECKIFEXIST(:upmcc_no,:uservicetype); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("upmcc_no", pmccno);
            statement.setString("uservicetype", servicetype);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                Tagging tagging = new Tagging();
                tagging.setAncservicetype(resultset.getString("ANC_SERVICE_TYPE"));
                tagging.setHcino(resultset.getString("HCI_NO"));
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(tagging));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult ACRGBHCITAGUPDATE(final DataSource datasource,
            final Tagging tagging,
            final String serviceType) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement getinsertresult = connection.prepareCall("call DRG_SHADOWBILLING.ACRGBACCREPACKAGE.ACRGBHCITAGUPDATE(:Message,:Code,:upmcc_no,:uservicetype,:udatestart,:udateended,:udatecreated,:udateissue,:udateeffective)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setString("upmcc_no", tagging.getHcino());
            getinsertresult.setString("uservicetype", serviceType.trim().toUpperCase());
            getinsertresult.setDate("udatestart", (Date) new Date(utility.StringToDate(tagging.getStartdate()).getTime()));//tranch.getDatecreated());
            getinsertresult.setDate("udateended", (Date) new Date(utility.StringToDate(tagging.getExpireddate()).getTime()));//tranch.getDatecreated());
            getinsertresult.setTimestamp("udatecreated", new java.sql.Timestamp(utility.GetCurrentDate().getTime()));//tranch.getDatecreated());
            getinsertresult.setDate("udateissue", (Date) new Date(utility.StringToDate(tagging.getIssuedate()).getTime()));//tranch.getDatecreated());
            getinsertresult.setDate("udateeffective", (Date) new Date(utility.StringToDate(tagging.getEffdate()).getTime()));//tranch.getDatecreated());
            getinsertresult.execute();
            if (!getinsertresult.getString("Message").equals("SUCC")) {
                result.setMessage(getinsertresult.getString("Message"));
            } else {
                result.setMessage(getinsertresult.getString("Message"));
                result.setSuccess(true);
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(FacilityTagging.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult ACRGBHCITAGNEW(final DataSource datasource,
            final Tagging tagging,
            final String servicetype) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement getinsertresult = connection.prepareCall("call DRG_SHADOWBILLING.ACRGBACCREPACKAGE.ACRGBHCITAGNEW(:Message,:Code,:upmcc_no,:uservicetype,:udatestart,:udateended,:ucreator,:udatecreated,:udateissue,:udateeffective)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setString("upmcc_no", tagging.getHcino());
            getinsertresult.setString("uservicetype", servicetype.trim().toUpperCase());
            getinsertresult.setDate("udatestart", (Date) new Date(utility.StringToDate(tagging.getStartdate()).getTime()));//tranch.getDatecreated());
            getinsertresult.setDate("udateended", (Date) new Date(utility.StringToDate(tagging.getExpireddate()).getTime()));//tranch.getDatecreated());
            getinsertresult.setString("ucreator", tagging.getUsername());
            getinsertresult.setTimestamp("udatecreated", new java.sql.Timestamp(utility.GetCurrentDate().getTime()));//tranch.getDatecreated());
            getinsertresult.setDate("udateissue", (Date) new Date(utility.StringToDate(tagging.getIssuedate()).getTime()));//tranch.getDatecreated());
            getinsertresult.setDate("udateeffective", (Date) new Date(utility.StringToDate(tagging.getEffdate()).getTime()));//tranch.getDatecreated());
            getinsertresult.execute();
            if (!getinsertresult.getString("Message").equals("SUCC")) {
                result.setMessage(getinsertresult.getString("Message"));
            } else {
                result.setMessage(getinsertresult.getString("Message"));
                result.setSuccess(true);
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(FacilityTagging.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET APEX FACILITY
    public ACRGBWSResult GETAPEXFACILITYS(final DataSource dataSource) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKG.GETAPEXFACILITY(); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.execute();
            ArrayList<HealthCareFacility> hcflist = new ArrayList<>();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            while (resultset.next()) {
                HealthCareFacility hcf = new HealthCareFacility();
                hcf.setHcfname(resultset.getString("HCFNAME"));
                hcf.setHcfaddress(resultset.getString("HCFADDRESS"));
                hcf.setHcfcode(resultset.getString("HCFCODE"));
                hcf.setType(resultset.getString("HCFTYPE"));
                hcf.setHcilevel(resultset.getString("HCILEVEL"));
                hcflist.add(hcf);
            }
            if (hcflist.size() > 0) {
                result.setResult(utility.ObjectMapper().writeValueAsString(hcflist));
                result.setMessage("OK");
                result.setSuccess(true);
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(FacilityTagging.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
