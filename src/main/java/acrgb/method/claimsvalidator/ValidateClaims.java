/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.method.claimsvalidator;

import acrgb.structure.ACRGBWSResult;
import acrgb.structure.Action;
import acrgb.structure.ContractDate;
import acrgb.structure.HealthCareFacility;
import acrgb.structure.UserRoleIndex;
import acrgb.utility.Utility;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
public class ValidateClaims {

    private final Utility utility = new Utility();
    private final SimpleDateFormat dateformat = utility.SimpleDateFormat("MM-dd-yyyy");
    private final String DaysExt = utility.GetString("DaysExtension");

    public ACRGBWSResult GETROLEREVERESE(
            final DataSource dataSource,
            final String puserid,
            final String utags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETROLEWITHIDREVERSE(:utags,:pid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("utags", utags.trim());
            statement.setString("pid", puserid.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            ArrayList<UserRoleIndex> userRoleList = new ArrayList<>();
            while (resultset.next()) {
                if (resultset.getString("CONDATE") != null) {
                    UserRoleIndex userRole = new UserRoleIndex();
                    userRole.setUserid(resultset.getString("USERID"));
                    userRole.setAccessid(resultset.getString("ACCESSID"));
                    ACRGBWSResult getContract = this.GETCONDATEBYID(dataSource, resultset.getString("CONDATE"));
                    if (getContract.isSuccess()) {
                        userRole.setContractdate(getContract.getResult());
                    } else {
                        userRole.setContractdate("");
                    }
                    userRoleList.add(userRole);
                }
            }
            if (userRoleList.size() > 0) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(userRoleList));
            } else {
                result.setMessage("NO FACILITY GB CONTRACT FOUND FOR THIS CLAIMS");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(ValidateClaims.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult GETNCLAIMS(
            final DataSource dataSource,
            final String useries,
            final String uaction) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        int counterCurrent = 0;
        int counterOld = 0;
        //MAP OBJECT REQUEST
        Action action = new Action();
        action.setAction(uaction);
        action.setSeries(useries);
        //END OF MAP OBJECT REQUEST
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKG.VALIDATECLAIMS(:useries); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("useries", useries.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                if (resultset.getString("OPD_TST").toUpperCase().trim().equals("F")) {
                    if (resultset.getString("TAGS").toUpperCase().trim().equals("G")) {
                        ArrayList<HealthCareFacility> testHCIlist = new ArrayList<>();
                        ACRGBWSResult getMainAccre = this.GETFACILITYBYMAINACCRE(dataSource, resultset.getString("PMCC_NO").trim());
                        if (getMainAccre.isSuccess()) {
                            testHCIlist.addAll(Arrays.asList(utility.ObjectMapper().readValue(getMainAccre.getResult(), HealthCareFacility[].class)));
                        } else if (this.GETFACILITYBYCODE(dataSource, resultset.getString("PMCC_NO").trim()).isSuccess()) {
                            testHCIlist.add(utility.ObjectMapper().readValue(this.GETFACILITYBYCODE(dataSource, resultset.getString("PMCC_NO").trim()).getResult(), HealthCareFacility.class));
                        }
                        if (testHCIlist.size() > 0) {
                            for (int x = 0; x < testHCIlist.size(); x++) {
                                ACRGBWSResult getActive = this.GETROLEREVERESE(dataSource, testHCIlist.get(x).getHcfcode().trim(), "ACTIVE");
                                if (getActive.isSuccess()) {
                                    //----------------------------------
                                    List<UserRoleIndex> userRoleList = Arrays.asList(utility.ObjectMapper().readValue(getActive.getResult(), UserRoleIndex[].class));
                                    for (int i = 0; i < userRoleList.size(); i++) {
                                        if (!userRoleList.get(i).getContractdate().isEmpty()) {
                                            ContractDate contractDate = utility.ObjectMapper().readValue(userRoleList.get(i).getContractdate(), ContractDate.class);
                                            if (resultset.getString("DATE_ADM") == null || resultset.getString("DATE_ADM").isEmpty() || resultset.getString("DATE_ADM").equals("")) {
                                            } else {
                                                if (dateformat.parse(contractDate.getDatefrom()).compareTo(dateformat.parse(dateformat.format(resultset.getTimestamp("DATE_ADM")))) <= 0) {
                                                    if (resultset.getString("REFILEDATE") == null || resultset.getString("REFILEDATE").isEmpty() || resultset.getString("REFILEDATE").equals("")) {
                                                        if (dateformat.parse(dateformat.format(resultset.getTimestamp("DATESUBMITTED"))).compareTo(dateformat.parse(utility.AddMinusDaysDate(contractDate.getDateto(), DaysExt))) <= 0) {
                                                            counterCurrent++;
                                                        }
                                                    } else {
                                                        if (dateformat.parse(dateformat.format(resultset.getTimestamp("REFILEDATE"))).compareTo(dateformat.parse(utility.AddMinusDaysDate(contractDate.getDateto(), DaysExt))) <= 0) {
                                                            counterCurrent++;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                ///-----------------------------------------------
                                //GET CLAIMS UNDER INACTIVE
                                ACRGBWSResult getInactive = this.GETROLEREVERESE(dataSource, testHCIlist.get(x).getHcfcode().trim(), "INACTIVE");
                                if (getInactive.isSuccess()) {
                                    List<UserRoleIndex> userRoleList = Arrays.asList(utility.ObjectMapper().readValue(getInactive.getResult(), UserRoleIndex[].class));
                                    for (int i = 0; i < userRoleList.size(); i++) {
                                        if (!userRoleList.get(i).getContractdate().isEmpty()) {
                                            ContractDate contractDate = utility.ObjectMapper().readValue(userRoleList.get(i).getContractdate(), ContractDate.class);
                                            if (resultset.getString("DATE_ADM") == null || resultset.getString("DATE_ADM").isEmpty() || resultset.getString("DATE_ADM").equals("")) {
                                            } else {
                                                if (dateformat.parse(contractDate.getDatefrom()).compareTo(dateformat.parse(dateformat.format(resultset.getTimestamp("DATE_ADM")))) <= 0) {
                                                    if (resultset.getString("REFILEDATE") == null || resultset.getString("REFILEDATE").isEmpty() || resultset.getString("REFILEDATE").equals("")) {
                                                        if (dateformat.parse(dateformat.format(resultset.getTimestamp("DATESUBMITTED"))).compareTo(dateformat.parse(utility.AddMinusDaysDate(contractDate.getDateto(), DaysExt))) <= 0) {
                                                            counterOld++;
                                                        }
                                                    } else {
                                                        if (dateformat.parse(dateformat.format(resultset.getTimestamp("REFILEDATE"))).compareTo(dateformat.parse(utility.AddMinusDaysDate(contractDate.getDateto(), DaysExt))) <= 0) {
                                                            counterOld++;
                                                        }
                                                    }
                                                }

                                            }
                                        }
                                    }
                                }
                                if (counterCurrent > 0) {
                                    ACRGBWSResult actionResult = this.INSERTACTION(dataSource, useries, uaction);
                                    if (actionResult.isSuccess()) {
                                        result.setSuccess(actionResult.isSuccess());
                                        result.setResult(utility.ObjectMapper().writeValueAsString(action));
                                        result.setMessage("CLAIM IS DEDUCTABLE FROM THE ACR-GB WITH ACTIVE CONTRACT");
                                    } else {
                                        result.setResult(actionResult.getResult());
                                        result.setMessage(actionResult.getMessage());
                                    }
                                } else if (counterOld > 0) {
                                    ACRGBWSResult actionResult = this.INSERTACTION(dataSource, useries, uaction);
                                    if (actionResult.isSuccess()) {
                                        result.setSuccess(actionResult.isSuccess());
                                        result.setResult(utility.ObjectMapper().writeValueAsString(action));
                                        result.setMessage("CLAIM IS DEDUCTABLE FROM THE ACR-GB WITH ENDED CONTRACT");
                                    } else {
                                        result.setResult(utility.ObjectMapper().writeValueAsString(action));
                                        result.setMessage(actionResult.getMessage());
                                    }
                                } else {
                                    result.setResult(utility.ObjectMapper().writeValueAsString(action));
                                    result.setMessage("CLAIM IS SUBJECT TO REIMBURSEMENT");
                                }
                            }
                        } else {
                            result.setResult(utility.ObjectMapper().writeValueAsString(action));
                            result.setMessage("CLAIMS SERIES NOT FOUND FACILITY");
                        }

                    } else {
                        result.setResult(utility.ObjectMapper().writeValueAsString(action));
                        result.setMessage("CLAIM WITH SERIES " + useries + "  STATUS  " + resultset.getString("TAGS"));
                    }
                } else {
                    result.setResult(utility.ObjectMapper().writeValueAsString(action));
                    result.setMessage("CLAIM WITH SERIES " + useries + " IS UNDER OUTPATIENT");
                }
            } else {
                result.setResult(utility.ObjectMapper().writeValueAsString(action));
                result.setMessage("CLAIM NOT FOUND WITH SERIES " + useries);
            }
        } catch (SQLException | IOException | ParseException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(ValidateClaims.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult GETCONDATEBYID(
            final DataSource dataSource,
            final String ucondateid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETCONDATEBYID(:ucondateid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("ucondateid", ucondateid.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                ContractDate contractdate = new ContractDate();
                contractdate.setCondateid(resultset.getString("CONDATEID"));
                contractdate.setCreatedby(resultset.getString("CREATEDBY"));
                contractdate.setDatecreated(dateformat.format(resultset.getTimestamp("DATECREATED")));//resultset.getString("DATECREATED"));
                contractdate.setDatefrom(dateformat.format(resultset.getTimestamp("DATEFROM")));//resultset.getString("DATECOVERED"));
                contractdate.setDateto(dateformat.format(resultset.getTimestamp("DATETO")));//resultset.getString("DATECOVERED"));
                contractdate.setStatus(resultset.getString("STATUS"));
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(contractdate));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(ValidateClaims.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult INSERTACTION(
            final DataSource datasource,
            final String useries,
            final String uaction) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("call DRG_SHADOWBILLING.ACRGBPKGPROCEDURE.INSERTACTION(:Message,:Code,:useries,:uaction,:udatecreated)");
            statement.registerOutParameter("Message", OracleTypes.VARCHAR);
            statement.registerOutParameter("Code", OracleTypes.INTEGER);
            statement.setString("useries", useries.trim());
            statement.setString("uaction", uaction.trim());
            statement.setTimestamp("udatecreated", new java.sql.Timestamp(utility.GetCurrentDate().getTime()));
            statement.execute();
            if (statement.getString("Message").equals("SUCC")) {
                result.setSuccess(true);
                result.setMessage(statement.getString("Message"));
            } else {
                result.setMessage(statement.getString("Message"));
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(ValidateClaims.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult GETFACILITYBYCODE(
            final DataSource datasource,
            final String hcfrid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKG.GETFACILITY(:hcfrid); end;");
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
            result.setMessage("Something went wrong");
            Logger.getLogger(ValidateClaims.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult GETFACILITYBYMAINACCRE(
            final DataSource datasource,
            final String umainaccre) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKG.GETFACILITYBYMAINACCRE(:umainaccre); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("umainaccre", umainaccre.trim());
            statement.execute();
            ArrayList<HealthCareFacility> hcfList = new ArrayList<>();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            while (resultset.next()) {
                HealthCareFacility hcf = new HealthCareFacility();
                hcf.setHcfname(resultset.getString("HCFNAME"));
                hcf.setHcfcode(resultset.getString("HCFCODE"));
                hcf.setType(resultset.getString("HCFTYPE"));
                hcf.setHcilevel(resultset.getString("HCILEVEL"));
                hcf.setStreet(resultset.getString("STREET"));
                hcf.setMainaccre(resultset.getString("MAIN_ACCRE"));
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
            result.setMessage("Something went wrong");
            Logger.getLogger(ValidateClaims.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
