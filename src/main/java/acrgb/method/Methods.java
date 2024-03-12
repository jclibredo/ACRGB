/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.method;

import acrgb.structure.ACRGBWSResult;
import acrgb.structure.FacilityComputedAmount;
import acrgb.structure.HealthCareFacility;
import acrgb.structure.MBRequestSummary;
import acrgb.structure.ManagingBoard;
import acrgb.structure.NclaimsData;
import acrgb.structure.Summary;
import acrgb.structure.Total;
import acrgb.structure.User;
import acrgb.structure.UserActivity;
import acrgb.structure.UserInfo;
import acrgb.structure.UserRoleIndex;
import acrgb.utility.Cryptor;
import acrgb.utility.Utility;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
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
public class Methods {

    public Methods() {
    }

    private final Utility utility = new Utility();
    private final FetchMethods fm = new FetchMethods();
    private final Cryptor cryptor = new Cryptor();
    private final SimpleDateFormat datetimeformat = utility.SimpleDateFormat("MM-dd-yyyy hh:mm a");
    private final SimpleDateFormat dateformat = utility.SimpleDateFormat("MM-dd-yyyy");

    //--------------------------------------------------------
// ACR GB USER ACCOUNT LOGIN
    public ACRGBWSResult ACRUSERLOGIN(final DataSource datasource, final String p_username, final String p_password) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setSuccess(false);
        result.setMessage("");
        result.setResult("");
        try {
            ACRGBWSResult resultfm = fm.ACR_USER(datasource, "active");
            if (resultfm.isSuccess()) {
                List<User> userlist = Arrays.asList(utility.ObjectMapper().readValue(resultfm.getResult(), User[].class));
                int resultcounter = 0;
                for (int x = 0; x < userlist.size(); x++) {
                    UserPassword userPassword = new UserPassword();
                    if (userlist.get(x).getStatus().equals("2")) {
                        userPassword.setDbpass(cryptor.decrypt(userlist.get(x).getUserpassword(), p_password, "ACRGB"));
                    } else {
                        userPassword.setDbpass(userlist.get(x).getUserpassword());
                    }
                    if (userPassword.getDbpass() != null) {
                        if (userlist.get(x).getUsername().equals(p_username) && userPassword.getDbpass().equals(p_password)) {
                            User user = new User();
                            user.setUserid(userlist.get(x).getUserid());
                            user.setLeveid(userlist.get(x).getLeveid().toUpperCase());
                            user.setUsername(userlist.get(x).getUsername());
                            user.setUserpassword(userlist.get(x).getUserpassword());
                            user.setDatecreated(userlist.get(x).getDatecreated());
                            user.setStatus(userlist.get(x).getStatus());
                            ACRGBWSResult detailsresult = fm.GETFULLDETAILS(datasource, userlist.get(x).getUserid());
                            if (detailsresult.isSuccess()) {
                                user.setDid(detailsresult.getResult());
                            } else {
                                user.setDid(detailsresult.getMessage());
                            }
                            user.setCreatedby(userlist.get(x).getCreatedby());
                            result.setSuccess(true);
                            result.setResult(utility.ObjectMapper().writeValueAsString(user));
                            result.setMessage("OK");
                            resultcounter++;
                            break;
                        }
                    } else {
                        result.setResult("INVALID USERNAME AND PASSWORD");
                        result.setMessage("CREDENTIAL NOT FOUND");
                    }
                }

                if (resultcounter == 0) {
                    result.setMessage("CREDENTIAL NOT FOUND");
                    result.setResult("INVALID USERNAME AND PASSWORD");
                }
            } else {
                //result.setSuccess(false);
                result.setMessage("NO AVAILABLE DATA");
            }
        } catch (IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

    //-------------------------- NEW OBJECT -----------------
    public class UserPassword {

        private String dbpass;

        public String getDbpass() {
            return dbpass;
        }

        public void setDbpass(String dbpass) {
            this.dbpass = dbpass;
        }
    }

    //-------------------------- NEW OBJECT -----------------
    //--------------------------------------------------------
    // ACR GB USERNAME CHECKING
    public ACRGBWSResult ACRUSERNAME(final DataSource dataSource, final String p_username) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :p_user := ACR_GB.ACRGBPKGFUNCTION.ACRUSERNAME(:p_username); end;");
            statement.registerOutParameter("p_user", OracleTypes.CURSOR);
            statement.setString("p_username", p_username);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("p_user");
            if (resultset.next()) {
                result.setMessage("USERNAME IS ALREADY EXIST");
                result.setSuccess(false);
                result.setResult(p_username);
            } else {
                result.setMessage("OK");
                result.setSuccess(true);
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //--------------------------------------------------------
    // ACR GB USER LEVEL
    public ACRGBWSResult ACRUSERLEVEL(final DataSource dataSource, final String p_levelid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_level := ACR_GB.ACRGBPKGFUNCTION.ACRUSERLEVEL(:p_levelid); end;");
            statement.registerOutParameter("v_level", OracleTypes.CURSOR);
            statement.setString("p_levelid", p_levelid);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_level");
            if (resultset.next()) {
                result.setResult(resultset.getString("LEVNAME"));
            } else {
                result.setMessage("USER ROLE IS NOT AVAILABLE");
            }
            result.setSuccess(true);
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //--------------------------------------------------------
    // UPDATE USER INFORMATION
    public ACRGBWSResult UPDATEUSERDETAILS(final DataSource dataSource, final UserInfo userinfo) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.UPDATEUSERDETAILS(:Message,:Code,"
                    + ":p_firstname,:p_lastname,:p_middlename,:p_did,:p_hcfid)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setString("p_firstname", userinfo.getFirstname().toUpperCase());
            getinsertresult.setString("p_lastname", userinfo.getLastname().toUpperCase());
            getinsertresult.setString("p_middlename", userinfo.getMiddlename().toUpperCase());
            getinsertresult.setString("p_did", userinfo.getDid());
            getinsertresult.setString("p_hcfid", userinfo.getHcfid());
            getinsertresult.execute();
            if (getinsertresult.getString("Message").equals("SUCC")) {
                result.setSuccess(true);
                result.setMessage("OK");
            } else {
                result.setMessage(getinsertresult.getString("Message"));
                result.setSuccess(false);
            }
            result.setResult(utility.ObjectMapper().writeValueAsString(userinfo));
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //--------------------------------------------------------
    // ACR GB USER CREDENTIALS
    public ACRGBWSResult UPDATEUSERCREDENTIALS(final DataSource dataSource, final String userid, final String p_username, final String p_password) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            ACRGBWSResult usernameResult = this.ACRUSERNAME(dataSource, p_username);
            if (usernameResult.isSuccess()) {
                if (!utility.validatePassword(p_password)) {
                    result.setSuccess(false);
                    result.setMessage("PASSWORD IS NOT VALID");
                } else {
                    CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.UPDATEUSERCREDENTIALS(:Message,:Code,:userid,:p_username,:p_password,:p_stats)");
                    getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                    getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                    getinsertresult.setString("userid", userid);
                    getinsertresult.setString("p_username", p_username);
                    getinsertresult.setString("p_password", cryptor.encrypt(p_password, p_password, "ACRGB"));
                    getinsertresult.setString("p_stats", "2");
                    getinsertresult.execute();
                    if (getinsertresult.getString("Message").equals("SUCC")) {
                        result.setSuccess(true);
                        result.setMessage("OK");
                    } else {
                        result.setMessage(getinsertresult.getString("Message"));
                        result.setSuccess(false);
                    }
                }
            } else {
                result.setMessage(usernameResult.getMessage());
                result.setSuccess(usernameResult.isSuccess());
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    // CHANGEUSERNAME
    public ACRGBWSResult CHANGEUSERNAME(final DataSource dataSource, final String userid, final String p_username) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            ACRGBWSResult usernameResult = this.ACRUSERNAME(dataSource, p_username);
            if (usernameResult.isSuccess()) {
                CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.UPDATEUSERNAME(:Message,:Code,:p_userid,:p_username,:p_stats)");
                getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                getinsertresult.setString("p_userid", userid);
                getinsertresult.setString("p_username", p_username);
                getinsertresult.setString("p_stats", "2");
                getinsertresult.execute();
                if (getinsertresult.getString("Message").equals("SUCC")) {
                    result.setSuccess(true);
                    result.setMessage("OK");
                } else {
                    result.setMessage(getinsertresult.getString("Message"));
                    result.setSuccess(false);
                }

            } else {
                result.setMessage(usernameResult.getMessage());
                result.setSuccess(usernameResult.isSuccess());
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult RESETPASSWORD(final DataSource dataSource, final String userid, final String p_password) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            if (!utility.validatePassword(p_password)) {
                result.setSuccess(false);
                result.setMessage("PASSWORD IS NOT VALID");
            } else {
                CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.UPDATEPASSWORD(:Message,:Code,:p_userid,:p_password,:p_stats)");
                getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                getinsertresult.setString("p_userid", userid);
                getinsertresult.setString("p_password", p_password);
                getinsertresult.setString("p_stats", "1");
                getinsertresult.execute();
                if (getinsertresult.getString("Message").equals("SUCC")) {
                    result.setSuccess(true);
                    result.setMessage("OK");
                } else {
                    result.setMessage(getinsertresult.getString("Message"));
                    result.setSuccess(false);
                }
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //CHANGE PASSWORD
    public ACRGBWSResult CHANGEPASSWORD(final DataSource dataSource, final String userid, final String p_password) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            if (!utility.validatePassword(p_password)) {
                result.setSuccess(false);
                result.setMessage("PASSWORD IS NOT VALID");
            } else {
                CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.UPDATEPASSWORD(:Message,:Code,:p_userid,:p_password,:p_stats)");
                getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                getinsertresult.setString("p_userid", userid);
                getinsertresult.setString("p_password", cryptor.encrypt(p_password, p_password, "ACRGB"));
                getinsertresult.setString("p_stats", "2");
                getinsertresult.execute();
                if (getinsertresult.getString("Message").equals("SUCC")) {
                    result.setSuccess(true);
                    result.setMessage("OK");
                } else {
                    result.setMessage(getinsertresult.getString("Message"));
                    result.setSuccess(false);
                }
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    // CHANGEUSELEVEL
    public ACRGBWSResult CHANGEUSELEVELID(final DataSource dataSource, final String userid, final String levelid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.UPDATELEVEL(:Message,:Code,:p_userid,:p_levelid,:p_stats)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setString("p_userid", userid);
            getinsertresult.setString("p_levelid", levelid);
            getinsertresult.setString("p_stats", "2");
            getinsertresult.execute();
            if (getinsertresult.getString("Message").equals("SUCC")) {
                result.setSuccess(true);
                result.setMessage("OK");
            } else {
                result.setMessage(getinsertresult.getString("Message"));
                result.setSuccess(false);
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    // CHANGEUSELEVEL
    public ACRGBWSResult GETSUMMARY(final DataSource dataSource, final String phcfid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETSUMMARY(:phcfid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("phcfid", phcfid);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                Total tot = new Total();
                tot.setCtotal(resultset.getString("cTOTAL"));
                tot.setHcfid(resultset.getString("HCFID"));
                tot.setCcount(resultset.getString("cCOUNT"));
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(tot));
            } else {
                result.setMessage("NO DATA FOUND");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //--------------------------------------------------------
    //ACR GB GET SUMMARY
    public ACRGBWSResult JOINCONHCFTBL(final DataSource dataSource) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.JOINCONHCFTBL(); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.execute();
            ArrayList<Summary> summarylist = new ArrayList<>();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            while (resultset.next()) {
                String u_accreno = resultset.getString("HCFCODE");
                Date u_from = resultset.getDate("DATEFROM");
                Date u_to = resultset.getDate("DATETO");
                String u_tags = "GOOD";
                Summary summary = new Summary();
                ACRGBWSResult sumresult = fm.GETNCLAIMS(dataSource, u_accreno, u_tags, u_from, u_to);
                if (sumresult.isSuccess()) {
                    NclaimsData nclaimsdata = utility.ObjectMapper().readValue(sumresult.getResult(), NclaimsData.class);
                    ACRGBWSResult totalResult = this.GETSUMMARY(dataSource, resultset.getString("HCFID"));
                    if (totalResult.isSuccess()) {
                        Total getResult = utility.ObjectMapper().readValue(totalResult.getResult(), Total.class);
                        summary.setTranchcount(getResult.getCcount());
                        Double assetsamount = Double.parseDouble(getResult.getCtotal());
                        Double totalclaimsamount = Double.parseDouble(nclaimsdata.getClaimamount());
                        Double sums = totalclaimsamount / assetsamount * 100;
                        if (sums > 100) {
                            Double negvalue = 100 - sums;
                            summary.setTotalpercentage(String.valueOf(Math.round(negvalue)));
                        } else {
                            summary.setTotalpercentage(String.valueOf(Math.round(sums)));
                        }
                    } else {
                        summary.setTotalpercentage(totalResult.getMessage());
                    }
                    summary.setAccreno(u_accreno);
                    summary.setHcfid(resultset.getString("HCFID"));
                    summary.setRemarks("YES");
                    summarylist.add(summary);

                } else {
                    summary.setAccreno(u_accreno);
                    summary.setHcfid(resultset.getString("HCFID"));
                    summary.setTotalpercentage("");
                    summary.setTranchcount("");
                    summary.setRemarks("NO");
                    summarylist.add(summary);
                }
            }
            if (summarylist.size() > 0) {
                result.setMessage("OK");
                result.setResult(utility.ObjectMapper().writeValueAsString(summarylist));
            } else {
                result.setMessage("NO DATA FOUND");
            }
            result.setSuccess(true);
        } catch (SQLException | IOException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    // ACR GB USER ACTIVITY LOGS
    public ACRGBWSResult ActivityLogs(final DataSource dataSource, final UserActivity useractivity) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        java.util.Date d1 = new java.util.Date();
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.ACTIVITYLOGS(:Message,:Code,:a_date,:a_details,:a_by)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setTimestamp("a_date", new java.sql.Timestamp(d1.getTime()));
            getinsertresult.setString("a_details", useractivity.getActdetails().toUpperCase());
            getinsertresult.setInt("a_by", Integer.parseInt(useractivity.getActby()));
            getinsertresult.execute();
            if (getinsertresult.getString("Message").equals("SUCC")) {
                result.setSuccess(true);
                result.setMessage("OK");
            } else {
                result.setMessage(getinsertresult.getString("Message"));
                result.setSuccess(false);
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    // ACR GB USER ACTIVITY LOGS WITH PARAMETER
    public ACRGBWSResult GetLogsWithID(final DataSource dataSource, final String userid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        java.util.Date d1 = new java.util.Date();
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETLOGSWITHID(:userid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("userid", userid);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                UserActivity logs = new UserActivity();
                logs.setActby(resultset.getString("ACTBY"));
                logs.setActdate(datetimeformat.format(resultset.getDate("ACTDATE")));
                logs.setActdetails(resultset.getString("ACTDETAILS"));
                logs.setActid(resultset.getString("ACTID"));
                result.setResult(utility.ObjectMapper().writeValueAsString(logs));
                result.setMessage("OK");
            } else {
                result.setMessage("NO DATA FOUND");
            }
            result.setSuccess(true);
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET AMOUNT PER FACILITY
    //public ACRGBWSResult GetAmountPerFacility(DataSource dataSource, final String uaccreno, final String udatefrom, final String diff) {
    public ACRGBWSResult GetAmountPerFacility(DataSource dataSource, final String uaccreno, final String udatefrom, final String udateto) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        String utags = "GOOD";
        try (Connection connection = dataSource.getConnection()) {
            //if (!utility.IsValidDateDifference(udatefrom) || !utility.IsValidDate(udateto)) {
            if (!utility.IsValidDate(udatefrom) || !utility.IsValidDate(udateto)) {
                result.setMessage("Date Format is not valid");
//            } else if (!utility.IsValidNumber(diff)) {
//                result.setMessage("Number Format is not valid");
            } else {
//                String udateto = utility.ComputeDateBackward(udatefrom, Integer.parseInt(diff));
//                String udatefroms = LocalDate.parse(udatefrom).format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
                CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKG.GETSUMAMOUNTCLAIMS(:uaccreno,:utags,:udatefrom,:udateto); end;");
                statement.registerOutParameter("v_result", OracleTypes.CURSOR);
                statement.setString("uaccreno", uaccreno);
                statement.setString("utags", utags);
                statement.setDate("udatefrom", (Date) new Date(utility.StringToDate(udatefrom).getTime()));
                statement.setDate("udateto", (Date) new Date(utility.StringToDate(udateto).getTime()));
                statement.execute();
                ResultSet resultset = (ResultSet) statement.getObject("v_result");
                if (resultset.next()) {
                    FacilityComputedAmount fca = new FacilityComputedAmount();
                    fca.setHospital(resultset.getString("ACCRENO"));
                    fca.setTotalamount(resultset.getString("CTOTAL"));
                    fca.setYearfrom(udatefrom);
                    fca.setYearto(udateto);
                    result.setResult(utility.ObjectMapper().writeValueAsString(fca));
                    result.setMessage("OK");
                } else {
                    result.setMessage("NO DATA FOUND");
                }
                result.setSuccess(true);
            }

        } catch (SQLException | IOException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET AMOUNT PER FACILITY FROM SKIP YEAR
    public ACRGBWSResult GetAmountPerFacilitySkipYear(DataSource dataSource, final String uaccreno) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        String utags = "GOOD";
        try (Connection connection = dataSource.getConnection()) {
            String udateto = "12-31-2021";
            String udatefroms = "01-01-2020";
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKG.GETSKIPYEARAMOUNT(:uaccreno,:utags,:udatefrom,:udateto); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("uaccreno", uaccreno);
            statement.setString("utags", utags);
            statement.setDate("udatefrom", (Date) new Date(utility.StringToDate(udatefroms).getTime()));
            statement.setDate("udateto", (Date) new Date(utility.StringToDate(udateto).getTime()));
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");

            if (resultset.next()) {
                FacilityComputedAmount fca = new FacilityComputedAmount();
                fca.setHospital(resultset.getString("ACCRENO"));
                fca.setTotalamount(resultset.getString("CTOTAL"));
                fca.setYearfrom(udatefroms);
                fca.setYearto(udateto);
                result.setResult(utility.ObjectMapper().writeValueAsString(fca));
                result.setMessage("OK");
                result.setSuccess(true);
            } else {
                result.setMessage("NO DATA FOUND");
            }

        } catch (SQLException | IOException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //INSERT MB REQUEST
    public ACRGBWSResult InsertMBRequest(DataSource dataSource, final MBRequestSummary mbrequestsummry) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        java.util.Date d1 = new java.util.Date();
        try (Connection connection = dataSource.getConnection()) {
            if (!utility.IsValidDate(mbrequestsummry.getDaterequest()) || !utility.IsValidDate(mbrequestsummry.getYearfrom()) || !utility.IsValidDate(mbrequestsummry.getYearto())) {
                result.setMessage("DATE FORMAT IS NOT VALID MM-dd-yyyy");
                result.setSuccess(false);
            } else if (!utility.IsValidNumber(mbrequestsummry.getTotalamount())) {
                result.setMessage("AMOUNT FORMAT IS NOT VALID");
                result.setSuccess(false);
            } else {
                CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.MBREQUEST(:Message,:Code,:udaterequest,:udatefrom,"
                        + ":udateto,:urequestor,:utranscode,:uremarks,:uamount,:udatecreated)");
                getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                getinsertresult.setDate("udaterequest", (Date) new Date(utility.StringToDate(mbrequestsummry.getDaterequest()).getTime()));
                getinsertresult.setDate("udatefrom", (Date) new Date(utility.StringToDate(mbrequestsummry.getYearfrom()).getTime()));
                getinsertresult.setDate("udateto", (Date) new Date(utility.StringToDate(mbrequestsummry.getYearto()).getTime()));
                getinsertresult.setInt("urequestor", Integer.parseInt(mbrequestsummry.getRequestor()));
                getinsertresult.setString("utranscode", mbrequestsummry.getTranscode());
                getinsertresult.setString("uremarks", mbrequestsummry.getRemarks());
                getinsertresult.setString("uamount", mbrequestsummry.getTotalamount());
                getinsertresult.setTimestamp("udatecreated", new java.sql.Timestamp(d1.getTime()));
                getinsertresult.execute();
                if (getinsertresult.getString("Message").equals("SUCC")) {

                    ArrayList<String> errorList = new ArrayList<>();
                    List<String> facilitylist = Arrays.asList(mbrequestsummry.getFacility().split(","));
                    int errCounter = 0;
                    for (int x = 0; x < facilitylist.size(); x++) {
                        CallableStatement statement = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.MBREQUESTFCHUNDER(:Message,:Code,:utranscode,:udatecreated,:ufacility)");
                        statement.registerOutParameter("Message", OracleTypes.VARCHAR);
                        statement.registerOutParameter("Code", OracleTypes.INTEGER);
                        statement.setString("utranscode", mbrequestsummry.getTotalamount());
                        statement.setTimestamp("udatecreated", new java.sql.Timestamp(d1.getTime()));
                        statement.setString("ufacility", facilitylist.get(x));
                        statement.execute();
                        if (!statement.getString("Message").equals("SUCC")) {
                            errCounter++;
                            errorList.add(getinsertresult.getString("Message"));
                        }
                    }

                    if (errCounter == 0) {
                        result.setMessage("OK");
                        result.setSuccess(true);
                    } else {
                        result.setMessage(errorList.toString());
                    }

                } else {
                    result.setMessage(getinsertresult.getString("Message"));
                    result.setSuccess(false);
                }
            }
        } catch (SQLException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET FHCI WITH BADGET
    public ACRGBWSResult MethodGetHealthFacilityBadget(final DataSource dataSource, final String udatefrom, final String udateto) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKG.GETFACILITYVALUE(); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            ArrayList<HealthCareFacility> listHCF = new ArrayList<>();
            while (resultset.next()) {
                HealthCareFacility hcf = new HealthCareFacility();
                hcf.setHcfid(resultset.getString("HCFID"));
                hcf.setHcfname(resultset.getString("HCFNAME"));
                hcf.setHcfaddress(resultset.getString("HCFADDRESS"));
                hcf.setHcfcode(resultset.getString("HCFCODE"));
                //GET DATE CREATOR
                ACRGBWSResult creator = fm.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                if (creator.isSuccess()) {
                    if (!creator.getResult().isEmpty()) {
                        UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class);
                        hcf.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                    } else {
                        hcf.setCreatedby(creator.getMessage());
                    }
                } else {
                    hcf.setCreatedby("DATA NOT FOUND");
                }
                //END OF GET DATE CREATOR
                hcf.setAreaid(resultset.getString("AREAID"));
                hcf.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));//resultset.getString("DATECREATED"));
                hcf.setProid(resultset.getString("PROID"));
                // GET BADGET 
                //FacilityComputedAmount
                ACRGBWSResult getBadgetResult = this.GetAmountPerFacility(dataSource, resultset.getString("HCFCODE"), udatefrom, udateto);//GET TOTAL CLAIMS AMOUNT FOR GOOD TAGS
                if (getBadgetResult.isSuccess()) {
                    FacilityComputedAmount getBadgetFirst = utility.ObjectMapper().readValue(getBadgetResult.getResult(), FacilityComputedAmount.class);
                    ACRGBWSResult getBadgetFirstSecond = this.GetAmountPerFacilitySkipYear(dataSource, getBadgetFirst.getHospital());//GET TOTAL BADGET FROM SKIP YEAR
                    if (getBadgetFirstSecond.isSuccess()) {
                        FacilityComputedAmount combadget = utility.ObjectMapper().readValue(getBadgetFirstSecond.getResult(), FacilityComputedAmount.class);
                        Double skipamount = Double.parseDouble(combadget.getTotalamount());
                        Double totalamount = Double.parseDouble(getBadgetFirst.getTotalamount());
                        String diff = String.valueOf(totalamount - skipamount);
                        hcf.setAmount(diff);

                    } else {
                        hcf.setAmount(getBadgetFirst.getTotalamount());
                    }
                } else {
                    hcf.setAmount(getBadgetResult.getMessage());
                }
                listHCF.add(hcf);
            }
            if (listHCF.size() < 1) {
                result.setMessage("NO DATA FOUND");
            } else {
                result.setMessage("OK");
                result.setResult(utility.ObjectMapper().writeValueAsString(listHCF));
            }
            result.setSuccess(true);
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

//
//    // ACR GB USER IMAGE
//    public ACRGBWSResult UPDATEUSERIMAGE(final DataSource dataSource, final String userid, final String p_image) {
//        ACRGBWSResult result = utility.ACRGBWSResult();
//        result.setMessage("");
//        result.setResult("");
//        result.setSuccess(false);
//        try (Connection connection = dataSource.getConnection()) {
//            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBINSERTPKG.UPDATEUSERIMAGE(:Message,:Code,:userid,:p_image)");
//            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
//            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
//            getinsertresult.setString("userid", userid);
//            getinsertresult.setString("p_image", p_image);
//            getinsertresult.execute();
//            if (getinsertresult.getString("Message").equals("SUCC")) {
//                result.setSuccess(true);
//                result.setMessage("SUCCESSFULLY INSERTED");
//            } else {
//                result.setMessage(getinsertresult.getString("Message"));
//                result.setSuccess(false);
//            }
//        } catch (SQLException ex) {
//            result.setMessage(ex.toString());
//            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return result;
//    }
//
//    // ACR GB ACCOUNTS PAYABLE
//    public ACRGBWSResult JOINPAYABLE(final DataSource dataSource, final String p_username) {
//        ACRGBWSResult result = utility.ACRGBWSResult();
//        result.setMessage("");
//        result.setResult("");
//        result.setSuccess(false);
//        try (Connection connection = dataSource.getConnection()) {
//            CallableStatement statement = connection.prepareCall("begin :joinresult := ACR_GB.ACRGBPKG.JOINPAYABLE(); end;");
//            statement.registerOutParameter("joinresult", OracleTypes.CURSOR);
//            statement.execute();
//            ResultSet resultset = (ResultSet) statement.getObject("joinresult");
//            ArrayList<AccountPayable> accountpayablelist = new ArrayList<>();
//            while (resultset.next()) {
//                AccountPayable accountpayable = new AccountPayable();
//                accountpayable.setPayableid(resultset.getString("PAYABLEID"));
//                accountpayable.setClaimseries(resultset.getString("CLAIMSERIES"));
//                accountpayable.setDatereleased(resultset.getString("DATERELEASED"));
//                accountpayable.setAmount(resultset.getString("AAMOUNT"));
//                accountpayable.setCreatedby(resultset.getString("CREATEDBY"));
//                accountpayable.setModeofpayment(resultset.getString("MODEOFPAYMENT"));
//                accountpayable.setPaidto(resultset.getString("PAIDTO"));
//                accountpayable.setAssetsid(resultset.getString("AASSETSID"));
//                accountpayable.setClaimid(resultset.getString("CLAIMID"));
//                accountpayablelist.add(accountpayable);
////------------------------------------------------------------------------------------------------------
//                Assets assests = new Assets();
//                assests.setAssetsid(resultset.getString("BASSETSID"));
//                assests.setAmount(resultset.getString("BAMOUNT"));
//                assests.setTransnumber(resultset.getString("TRANSNUMBER"));
//                assests.setTypeofassets(resultset.getString("TYPEOFASSETS"));
//                assests.setSourceoffunds(resultset.getString("SOURCEOFFUNDS"));
//                assests.setDaterecieved(resultset.getString("DATERECIEVED"));
//                assests.setEncodedby(resultset.getString("ENCODEDBY"));
//                assests.setAccreditation(resultset.getString("ACCREDITATION"));
//                assests.setHfid(resultset.getString("HFID"));
//                assests.setTransid(resultset.getString("BID"));
////------------------------------------------------------------------------------------------------------
//                Tranch trans = new Tranch();
//                trans.setTransid(resultset.getString("CID"));
//                trans.setDatereleased(resultset.getString("DATERELEASED"));
//                trans.setTranstype(resultset.getString("TRANSTYPEID"));
//                trans.setPercent(resultset.getString("PERCENT"));
//                trans.setHfid(resultset.getString("CHFID"));
//            }
//            
//            if (!accountpayablelist.isEmpty()) {
//                result.setSuccess(true);
//                result.setResult(utility.ObjectMapper().writeValueAsString(accountpayablelist));
//            } else {
//                result.setSuccess(false);
//                result.setMessage("NO DATA AVAILABLE");
//            }
//            
//        } catch (SQLException | IOException ex) {
//            result.setMessage(ex.toString());
//            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return result;
//    }
//  
    // ACR GB USER ACTIVITY LOGS WITH PARAMETER
    public ACRGBWSResult FetchMBRequest(final DataSource dataSource) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETMBREQUEST(); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            ArrayList<MBRequestSummary> mbrequestlist = new ArrayList<>();
            while (resultset.next()) {
                MBRequestSummary mbrequest = new MBRequestSummary();
                mbrequest.setMbrid(resultset.getString("MBRID"));
                mbrequest.setTotalamount(resultset.getString("AMOUNT"));
                mbrequest.setDaterequest(dateformat.format(resultset.getDate("DATEREQUEST")));
                mbrequest.setYearfrom(dateformat.format(resultset.getDate("DATEFROM")));
                mbrequest.setYearto(dateformat.format(resultset.getDate("DATETO")));
                mbrequest.setRequestor(resultset.getString("REQUESTOR"));
                mbrequest.setTranscode(resultset.getString("TRANSCODE"));
                mbrequest.setReqstatus(resultset.getString("STATUS"));
                mbrequest.setRemarks(resultset.getString("REMARKS"));
                mbrequest.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));
                mbrequestlist.add(mbrequest);
            }

            if (!mbrequestlist.isEmpty()) {
                result.setResult(utility.ObjectMapper().writeValueAsString(mbrequestlist));
                result.setMessage("OK");
                result.setSuccess(true);
            } else {
                result.setMessage("NO DATA FOUND");
            }

        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //MB TABLE
    public ACRGBWSResult InsertMB(final DataSource dataSource, ManagingBoard managingboard) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.INSERTMANAGINGBOARD(:Message,:Code,"
                    + ":umbname,:udatecreated,:ucreatedby)");
            statement.registerOutParameter("Message", OracleTypes.VARCHAR);
            statement.registerOutParameter("Code", OracleTypes.INTEGER);
            //-------------------------------------------------------------OUT PARAMETER
            statement.setString("umbname", managingboard.getMbname());
            statement.setDate("udatecreated", (Date) new Date(utility.StringToDate(managingboard.getDatecreated()).getTime()));
            statement.setString("ucreatedby", managingboard.getCreatedby());
            statement.execute();
            //------------------------------------------------------------------------------------------------
            if (statement.getString("Message").equals("SUCC")) {
                result.setMessage("OK");
                result.setSuccess(true);
            } else {
                result.setMessage(statement.getString("Message"));
            }

        } catch (SQLException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET ACCESS LEVEL USING USERID
    public ACRGBWSResult GETROLEWITHID(final DataSource dataSource, final String pid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            if (!utility.IsValidNumber(pid)) {
                result.setMessage("INVALID NUMBER FORMAT");
            } else {
                CallableStatement getstatementaccessid = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETROLEWITHID(:pid); end;");
                getstatementaccessid.registerOutParameter("v_result", OracleTypes.CURSOR);
                getstatementaccessid.setString("pid", pid);
                getstatementaccessid.execute();
                ResultSet accessidresultset = (ResultSet) getstatementaccessid.getObject("v_result");
                if (accessidresultset.next()) {
                    CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETROLEWITHID(:pid); end;");
                    statement.registerOutParameter("v_result", OracleTypes.CURSOR);
                    statement.setString("pid", accessidresultset.getString("ACCESSID"));
                    statement.execute();
                    ResultSet resultset = (ResultSet) statement.getObject("v_result");
                    ArrayList<String> accessidlist = new ArrayList<>();
                    ArrayList<ManagingBoard> mblist = new ArrayList<>();
                    ArrayList<HealthCareFacility> facilitylist = new ArrayList<>();
                    while (resultset.next()) {
                        accessidlist.add(resultset.getString("ACCESSID"));
                    }
                    if (accessidlist.size() > 0) {
                        for (int x = 0; x < accessidlist.size(); x++) {
                            ACRGBWSResult getmbresult = this.GETMBWITHID(dataSource, accessidlist.get(x));
                            if (getmbresult.isSuccess()) {
                                ManagingBoard managingboard = utility.ObjectMapper().readValue(getmbresult.getResult(), ManagingBoard.class);
                                //GET FALCITY UNDER EVERY MB
                                CallableStatement mbstatement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETROLEWITHID(:pid); end;");
                                mbstatement.registerOutParameter("v_result", OracleTypes.CURSOR);
                                mbstatement.setString("pid", managingboard.getMbid());
                                mbstatement.execute();
                                ArrayList<String> facilityidlist = new ArrayList<>();
                                ResultSet mbresultset = (ResultSet) mbstatement.getObject("v_result");
                                while (mbresultset.next()) {
                                    facilityidlist.add(mbresultset.getString("ACCESSID"));
                                }

                                for (int y = 0; y < facilityidlist.size(); y++) {
                                    ACRGBWSResult getfacility = fm.GETFACILITYID(dataSource, facilityidlist.get(y));
                                    if (getfacility.isSuccess()) {
                                        HealthCareFacility facility = utility.ObjectMapper().readValue(getfacility.getResult(), HealthCareFacility.class);
                                        HealthCareFacility newfacility = new HealthCareFacility();
                                        newfacility.setAmount(facility.getAmount());
                                        newfacility.setAreaid(facility.getAreaid());
                                        newfacility.setCreatedby(facility.getCreatedby());
                                        newfacility.setDatecreated(facility.getDatecreated());
                                        newfacility.setHcfaddress(facility.getHcfaddress());
                                        newfacility.setHcfcode(facility.getHcfcode());
                                        newfacility.setHcfid(facility.getHcfid());
                                        newfacility.setHcfname(facility.getHcfname());
                                        newfacility.setMb(utility.ObjectMapper().writeValueAsString(managingboard));
                                        newfacility.setProid(facility.getProid());
                                        facilitylist.add(newfacility);
                                    }
                                }
                                mblist.add(managingboard);
                            }
                            break;
                        }

                    } else {
                        result.setMessage("NO DATA FOUND");
                    }
                    if (mblist.size() > 0) {
                        result.setResult(utility.ObjectMapper().writeValueAsString(facilitylist));
                        result.setMessage("OK");
                        result.setSuccess(true);
                    } else {
                        result.setMessage("NO DATA FOUND");
                    }
                } else {
                    result.setMessage("NO DATA FOUND");
                }
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET FACILITY UNDER MB USING USERID
    public ACRGBWSResult GETFACILITYUNDERMBUSER(final DataSource dataSource, final String pid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            if (!utility.IsValidNumber(pid)) {
                result.setMessage("INVALID NUMBER FORMAT");
            } else {
                CallableStatement getstatementaccessid = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETROLEWITHID(:pid); end;");
                getstatementaccessid.registerOutParameter("v_result", OracleTypes.CURSOR);
                getstatementaccessid.setString("pid", pid);
                getstatementaccessid.execute();
                ResultSet accessidresultset = (ResultSet) getstatementaccessid.getObject("v_result");
                if (accessidresultset.next()) {
                    CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETROLEWITHID(:pid); end;");
                    statement.registerOutParameter("v_result", OracleTypes.CURSOR);
                    statement.setString("pid", accessidresultset.getString("ACCESSID"));
                    statement.execute();
                    ResultSet resultset = (ResultSet) statement.getObject("v_result");

                    //-------------testing area ----------------
                    //-------------testing area ----------------
                    ArrayList<String> fchlist = new ArrayList<>();
                    ArrayList<HealthCareFacility> healthcarefacilitylist = new ArrayList<>();
                    while (resultset.next()) {
                        fchlist.add(resultset.getString("ACCESSID"));

                    }
                    if (fchlist.size() > 0) {
                        for (int y = 0; y < fchlist.size(); y++) {
                            ACRGBWSResult getfacility = fm.GETFACILITYID(dataSource, fchlist.get(y));
                            if (getfacility.isSuccess()) {
                                HealthCareFacility facility = utility.ObjectMapper().readValue(getfacility.getResult(), HealthCareFacility.class);
                                healthcarefacilitylist.add(facility);
                            }
                        }
                    } else {
                        result.setMessage("NO DATA FOUND");
                    }
                    if (healthcarefacilitylist.size() > 0) {
                        result.setResult(utility.ObjectMapper().writeValueAsString(healthcarefacilitylist));
                        result.setMessage("OK");
                        result.setSuccess(true);
                    } else {
                        result.setMessage("NO DATA FOUND");
                    }
                } else {
                    result.setMessage("NO DATA FOUND");
                }
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET MB WITH ID
    public ACRGBWSResult GETMBWITHID(final DataSource dataSource, final String pid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {

            //  System.out.println(resultset.getString("MBNAME"));
            if (!utility.IsValidNumber(pid)) {
                result.setMessage("INVALID NUMBER FORMAT");
            } else {
                //---------------------------------------------------- 
                CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETMBWITHID(:pid); end;");
                statement.registerOutParameter("v_result", OracleTypes.CURSOR);
                statement.setString("pid", pid);
                statement.execute();
                ResultSet resultset = (ResultSet) statement.getObject("v_result");
                if (resultset.next()) {
                    //--------------------------------------------------------
                    ManagingBoard mb = new ManagingBoard();
                    mb.setMbid(resultset.getString("MBID"));
                    mb.setMbname(resultset.getString("MBNAME"));
                    mb.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));
                    ACRGBWSResult creator = fm.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                    if (creator.isSuccess()) {
                        if (!creator.getResult().isEmpty()) {
                            UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class);
                            mb.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                        } else {
                            mb.setCreatedby(creator.getMessage());
                        }
                    } else {
                        mb.setCreatedby("DATA NOT FOUND");
                    }

                    mb.setStatus(resultset.getString("STATUS"));
                    result.setMessage("OK");
                    result.setSuccess(true);
                    result.setResult(utility.ObjectMapper().writeValueAsString(mb));

                    //------------------------------------------------------
                } else {
                    result.setMessage("NO DATA FOUND");

                }

                //----------------------------------------------------------
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    // REMOVED ACCESS LEVEL USING ROLE INDEX
    public ACRGBWSResult REMOVEDROLEINDEX(final DataSource datasource, final String userid, final String accessid) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {

            if (!utility.IsValidNumber(userid)) {
                result.setMessage("NUMBER FORMAT IS NOT VALID");
            } else {
                ArrayList<String> errorList = new ArrayList<>();
                List<String> accesslist = Arrays.asList(accessid.split(","));
                int errCount = 0;
                for (int x = 0; x < accesslist.size(); x++) {
                    //------------------------------------------------------------------------------------------------
                    CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.REMOVEDROLEINDEX(:Message,:Code,"
                            + ":puserid,:paccessid)");
                    getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                    getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                    getinsertresult.setString("puserid", userid);
                    getinsertresult.setString("paccessid", accesslist.get(x));
                    getinsertresult.execute();
                    //------------------------------------------------------------------------------------------------
                    if (!getinsertresult.getString("Message").equals("SUCC")) {
                        errCount++;
                        errorList.add(getinsertresult.getString("Message"));
                    }
                }
                if (errCount == 0) {
                    result.setSuccess(true);
                    result.setMessage("OK");
                } else {
                    result.setMessage(errorList.toString());
                    result.setSuccess(false);
                    result.setResult(errorList.toString());
                }

            }

        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET MB WITH ID
    public ACRGBWSResult GETALLMBWITHPROID(final DataSource dataSource, final String proid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            //  System.out.println(resultset.getString("MBNAME"));
            if (!utility.IsValidNumber(proid)) {
                result.setMessage("INVALID NUMBER FORMAT");
            } else {
                CallableStatement statementproid = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETROLEWITHID(:pid); end;");
                statementproid.registerOutParameter("v_result", OracleTypes.CURSOR);
                statementproid.setString("pid", proid);
                statementproid.execute();
                ArrayList<ManagingBoard> mblist = new ArrayList<>();
                ResultSet resultsetpro = (ResultSet) statementproid.getObject("v_result");
                while (resultsetpro.next()) {
                    //---------------------------------------------------- 
                    CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETMBWITHID(:pid); end;");
                    statement.registerOutParameter("v_result", OracleTypes.CURSOR);
                    statement.setString("pid", resultsetpro.getString("ACCESSID"));
                    statement.execute();
                    ResultSet resultset = (ResultSet) statement.getObject("v_result");
                    while (resultset.next()) {
                        //--------------------------------------------------------
                        ManagingBoard mb = new ManagingBoard();
                        mb.setMbid(resultset.getString("MBID"));
                        mb.setMbname(resultset.getString("MBNAME"));
                        mb.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));
                        ACRGBWSResult creator = fm.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                        if (creator.isSuccess()) {
                            if (!creator.getResult().isEmpty()) {
                                UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class);
                                mb.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                            } else {
                                mb.setCreatedby(creator.getMessage());
                            }
                        } else {
                            mb.setCreatedby("DATA NOT FOUND");
                        }
                        mb.setStatus(resultset.getString("STATUS"));
                        mblist.add(mb);
                        //------------------------------------------------------
                    }
                }
                if (mblist.size() > 0) {
                    result.setMessage("OK");
                    result.setSuccess(true);
                    result.setResult(utility.ObjectMapper().writeValueAsString(mblist));
                } else {
                    result.setMessage("NO DATA FOUND");
                }

                //----------------------------------------------------------
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET FACILITY WITH MBID
    public ACRGBWSResult GETALLFACILITYWITHMBID(final DataSource dataSource, final String proid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            //  System.out.println(resultset.getString("MBNAME"));
            if (!utility.IsValidNumber(proid)) {
                result.setMessage("INVALID NUMBER FORMAT");
            } else {
                CallableStatement statementproid = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETROLEWITHID(:pid); end;");
                statementproid.registerOutParameter("v_result", OracleTypes.CURSOR);
                statementproid.setString("pid", proid);
                statementproid.execute();
                ArrayList<HealthCareFacility> fchlist = new ArrayList<>();
                ResultSet resultsetpro = (ResultSet) statementproid.getObject("v_result");
                while (resultsetpro.next()) {
                    //---------------------------------------------------- 
                    CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKG.GETFACILITY(:pid); end;");
                    statement.registerOutParameter("v_result", OracleTypes.CURSOR);
                    statement.setString("pid", resultsetpro.getString("ACCESSID"));
                    statement.execute();
                    ResultSet resultset = (ResultSet) statement.getObject("v_result");
                    while (resultset.next()) {
                        //--------------------------------------------------------

                        HealthCareFacility hcf = new HealthCareFacility();
                        hcf.setHcfid(resultset.getString("HCFID"));
                        hcf.setHcfname(resultset.getString("HCFNAME"));
                        hcf.setHcfaddress(resultset.getString("HCFADDRESS"));
                        hcf.setHcfcode(resultset.getString("HCFCODE"));
                        ACRGBWSResult creator = fm.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                        if (creator.isSuccess()) {
                            if (!creator.getResult().isEmpty()) {
                                UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class);
                                hcf.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                            } else {
                                hcf.setCreatedby(creator.getMessage());
                            }
                        } else {
                            hcf.setCreatedby("DATA NOT FOUND");
                        }
                        hcf.setAreaid(resultset.getString("AREAID"));
                        hcf.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));
                        hcf.setProid(resultset.getString("PROID"));
                        fchlist.add(hcf);
                        //------------------------------------------------------
                    }
                }
                if (fchlist.size() > 0) {
                    result.setMessage("OK");
                    result.setSuccess(true);
                    result.setResult(utility.ObjectMapper().writeValueAsString(fchlist));
                } else {
                    result.setMessage("NO DATA FOUND");
                }

                //----------------------------------------------------------
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
