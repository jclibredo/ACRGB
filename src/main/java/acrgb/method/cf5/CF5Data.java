/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.method.cf5;

import acrgb.structure.ACRGBWSResult;
import acrgb.structure.cf5.Info;
import acrgb.structure.cf5.Procedure;
import acrgb.structure.cf5.Secondary;
import acrgb.structure.cf5.WarningError;
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
public class CF5Data {

    public CF5Data() {
    }
    private final Utility utility = new Utility();

    // GET CF5 PATIENT INFO
    public ACRGBWSResult INFO(final DataSource datasource,
            final String useries) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_results := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.INFO(:useries); end;");
            statement.registerOutParameter("v_results", OracleTypes.CURSOR);
            statement.setString("useries", useries.trim());
            statement.execute();
            ResultSet resultSet = (ResultSet) statement.getObject("v_results");
            if (resultSet.next()) {
                Info info = new Info();
                info.setPdx(resultSet.getString("PDX_CODE"));
                info.setNBTimeofbirth(resultSet.getString("NB_TOB"));
                info.setNBweight(resultSet.getString("NB_ADMWEIGHT"));
                info.setClaimnumber(resultSet.getString("CLAIMNUMBER"));
                info.setHospitalcode(resultSet.getString("ACCRENO"));
                info.setSeries(resultSet.getString("SERIES"));
                if (this.PROCEDURES(datasource, useries).isSuccess()) {
                    info.setProcedure(this.PROCEDURES(datasource, useries).getResult());
                }
                if (this.SECONDARY(datasource, useries).isSuccess()) {
                    info.setSecondary(this.SECONDARY(datasource, useries).getResult());
                }
                if (this.WARNINGERROR(datasource, useries).isSuccess()) {
                    info.setWarningerr(this.WARNINGERROR(datasource, useries).getResult());
                }
                result.setResult(utility.ObjectMapper().writeValueAsString(info));
                result.setSuccess(true);
            } else {
                result.setMessage("NO DATA FOUND");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(CF5Data.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    // GET CF5 PATIENT WARNING
    public ACRGBWSResult WARNINGERROR(final DataSource datasource,
            final String useries) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_results := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.WARNINGERROR(:useries); end;");
            statement.registerOutParameter("v_results", OracleTypes.CURSOR);
            statement.setString("useries", useries.trim());
            statement.execute();
            ArrayList<WarningError> warningErrorList = new ArrayList<>();
            ResultSet resultSet = (ResultSet) statement.getObject("v_results");
            while (resultSet.next()) {
                WarningError warningError = new WarningError();
                warningError.setClaimid(resultSet.getString("CLAIM_ID"));
                warningError.setData(resultSet.getString("DATA"));
                warningError.setDetails(resultSet.getString("DESCRIPTION"));
                warningError.setErrcode(resultSet.getString("ERROR_CODE"));
                warningError.setSeries(resultSet.getString("SERIES"));
                warningErrorList.add(warningError);
            }
            if (warningErrorList.size() > 0) {
                result.setResult(utility.ObjectMapper().writeValueAsString(warningErrorList));
                result.setSuccess(true);
            } else {
                result.setMessage("NO DATA FOUND");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(CF5Data.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    // GET CF5 PATIENT WARNING
    public ACRGBWSResult PROCEDURES(final DataSource datasource,
            final String useries) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
//        String[] errocode = {"203", "204", "205", "206", "207", "208", "506", "507", "508"};
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_results := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.PROCEDURES(:useries); end;");
            statement.registerOutParameter("v_results", OracleTypes.CURSOR);
            statement.setString("useries", useries.trim());
            statement.execute();
            ArrayList<Procedure> procedureList = new ArrayList<>();
            ResultSet resultSet = (ResultSet) statement.getObject("v_results");
            while (resultSet.next()) {
                Procedure procedure = new Procedure();
                procedure.setRvs(resultSet.getString("RVS"));
                procedure.setExt1(resultSet.getString("EXT1_CODE"));
                procedure.setExt2(resultSet.getString("EXT2_CODE"));
                procedure.setLat(resultSet.getString("LATERALITY"));
                procedureList.add(procedure);
            }

            if (procedureList.size() > 0) {
                result.setResult(utility.ObjectMapper().writeValueAsString(procedureList));
                result.setSuccess(true);
            } else {
                result.setMessage("NO DATA FOUND");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(CF5Data.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    // GET CF5 PATIENT WARNING
    public ACRGBWSResult SECONDARY(final DataSource datasource,
            final String useries) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
//        String[] errocode = {"202", "501", "502", "503", "504", "505"};
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_results := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.SECONDARY(:useries); end;");
            statement.registerOutParameter("v_results", OracleTypes.CURSOR);
            statement.setString("useries", useries.trim());
            statement.execute();
            ArrayList<Secondary> secondList = new ArrayList<>();
            ResultSet resultSet = (ResultSet) statement.getObject("v_results");
            while (resultSet.next()) {
                Secondary second = new Secondary();
                second.setSdx(resultSet.getString("SDX_CODE"));
                secondList.add(second);
            }
//            ACRGBWSResult getSecond = this.WARNINGERROR(datasource, useries.trim());
//            if (getSecond.isSuccess()) {
//                List<WarningError> war = Arrays.asList(utility.ObjectMapper().readValue(getSecond.getResult(), WarningError[].class));
//                for (int x = 0; x < war.size(); x++) {
//                    if (Arrays.asList(errocode).contains(war.get(x).getErrcode())) {
//                        Secondary sdx = new Secondary();
//                        sdx.setSdx(war.get(x).getData());
//                        secondList.add(sdx);
//                    }
//                }
//            }

            //GET OTHER SDX FROM WARNING
            if (secondList.size() > 0) {
                result.setResult(utility.ObjectMapper().writeValueAsString(secondList));
                result.setSuccess(true);
            } else {
                result.setMessage("NO DATA FOUND");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(CF5Data.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}