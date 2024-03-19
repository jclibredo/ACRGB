/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.method;

import acrgb.structure.ACRGBWSResult;
import acrgb.structure.Assets;
import acrgb.structure.Contract;
import acrgb.structure.HealthCareFacility;
import acrgb.structure.ManagingBoard;
import acrgb.structure.NclaimsData;
import acrgb.structure.Pro;
import acrgb.structure.Tranch;
import acrgb.structure.User;
import acrgb.structure.UserActivity;
import acrgb.structure.UserInfo;
import acrgb.structure.UserLevel;
import acrgb.structure.UserRoleIndex;
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
public class FetchMethods {

    public FetchMethods() {
    }

    private final Utility utility = new Utility();
    private final SimpleDateFormat dateformat = utility.SimpleDateFormat("MM-dd-yyyy");
    private final SimpleDateFormat datetimeformat = utility.SimpleDateFormat("MM-dd-yyyy hh:mm:ss a");

    public ACRGBWSResult GETFACILITYID(final DataSource dataSource, final String uhcfid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKG.GETFACILITY(:userid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("hcfrid", uhcfid);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                HealthCareFacility hcf = new HealthCareFacility();
                hcf.setHcfid(resultset.getString("HCFID"));
                hcf.setHcfname(resultset.getString("HCFNAME"));
                hcf.setHcfaddress(resultset.getString("HCFADDRESS"));
                hcf.setHcfcode(resultset.getString("HCFCODE"));
                hcf.setCreatedby(resultset.getString("CREATEDBY"));
                hcf.setType(resultset.getString("HCFTYPE"));
                hcf.setDatecreated(resultset.getString("DATECREATED"));
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(hcf));
            } else {
                result.setMessage("NO DATA FOUND");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET ALL FACILITY(MULTIPLE)
    public ACRGBWSResult GETALLFACILITY(final DataSource dataSource) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Methods methods = new Methods();
        try (Connection connection = dataSource.getConnection()) {

            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKG.GETFACILITYVALUE(); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            ArrayList<HealthCareFacility> hcflist = new ArrayList<>();
            while (resultset.next()) {
                HealthCareFacility hcf = new HealthCareFacility();
                hcf.setHcfid(resultset.getString("HCFID"));
                hcf.setHcfname(resultset.getString("HCFNAME"));
                hcf.setHcfaddress(resultset.getString("HCFADDRESS"));
                hcf.setHcfcode(resultset.getString("HCFCODE"));
                ACRGBWSResult creator = this.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
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
                hcf.setType(resultset.getString("HCFTYPE"));
                hcf.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));
                //-------------------------------------------------
                ACRGBWSResult methodsresult = methods.GETROLEREVERESE(dataSource, resultset.getString("HCFID"));
                if (methodsresult.isSuccess()) {
                    ACRGBWSResult mgresult = methods.GETMBWITHID(dataSource, methodsresult.getResult());
                    if (mgresult.isSuccess()) {
                        ManagingBoard mb = utility.ObjectMapper().readValue(mgresult.getResult(), ManagingBoard.class);
                        hcf.setMb(mb.getMbname());
                        ACRGBWSResult restC = methods.GETROLEREVERESE(dataSource, mb.getMbid());
                        if (restC.isSuccess()) {
                            //GET PRO USING PROID
                            ACRGBWSResult getproid = methods.GetProWithPROID(dataSource, restC.getResult());
                            if (getproid.isSuccess()) {
                                Pro pro = utility.ObjectMapper().readValue(getproid.getResult(), Pro.class);
                                hcf.setProid(pro.getProname());
                            } else {
                                hcf.setProid(getproid.getMessage());
                            }
                        } else {
                            hcf.setProid(restC.getMessage());
                        }

                    } else {
                        hcf.setMb(mgresult.getMessage());
                    }
                }
                hcflist.add(hcf);
            }
            if (hcflist.size() > 0) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(hcflist));
            } else {
                result.setMessage("NO DATA FOUND");
            }

        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET USER INFO
    public ACRGBWSResult GETFULLDETAILS(final DataSource dataSource, final String userid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETFULLDETAILS(:userid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("userid", userid);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");

            if (resultset.next()) {
                UserInfo userinfo = new UserInfo();
                userinfo.setDid(resultset.getString("BDID"));
                userinfo.setFirstname(resultset.getString("FIRSTNAME"));
                userinfo.setLastname(resultset.getString("LASTNAME"));
                userinfo.setMiddlename(resultset.getString("MIDDLENAME"));
                userinfo.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));//resultset.getString("DATECREATED"));
                result.setMessage("OK");
                //result.setMessage(utility.ObjectMapper().writeValueAsString(user));
                result.setResult(utility.ObjectMapper().writeValueAsString(userinfo));
            } else {
                result.setMessage("NO DATA FOUND");
            }
            result.setSuccess(true);
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

//USER ROLE INDEX
    public ACRGBWSResult GETUSERROLEINDEX(final DataSource dataSource, final String puserid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETUSERROLEINDEX(:puserid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("puserid", puserid.toUpperCase());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            ArrayList<UserRoleIndex> userolelist = new ArrayList<>();
            while (resultset.next()) {
                UserRoleIndex userole = new UserRoleIndex();
                userole.setRoleid(resultset.getString("ROLEID"));
                userole.setUserid(resultset.getString("USERID"));
                userole.setAccessid(resultset.getString("ACCESSID"));
                ACRGBWSResult creator = GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                if (creator.isSuccess()) {
                    if (!creator.getResult().isEmpty()) {
                        UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class);
                        userole.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                    } else {
                        userole.setCreatedby(creator.getMessage());
                    }
                } else {
                    userole.setCreatedby("DATA NOT FOUND");
                }
                userole.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));//resultset.getString("DATECREATED"));
                userole.setStatus(resultset.getString("STATUS"));
                userolelist.add(userole);
            }
            if (!userolelist.isEmpty()) {
                result.setMessage("OK");
                result.setResult(utility.ObjectMapper().writeValueAsString(userolelist));
            } else {
                result.setMessage("NO DATA FOUND");
            }
            result.setSuccess(true);
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    // ACR___ASSETS
    public ACRGBWSResult ACR_ASSETS(final DataSource dataSource, final String tags, final String phcfid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :assets_type := ACR_GB.ACRGBPKGFUNCTION.ACR_ASSETS(:tags,:phcfid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("tags", tags.replaceAll("\\s", "").toUpperCase());
            statement.setString("phcfid", phcfid);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            ArrayList<Assets> listassets = new ArrayList<>();
            while (resultset.next()) {
                Assets assets = new Assets();
                assets.setAssetid(resultset.getString("ASSETSID"));
                assets.setTranchid(resultset.getString("TRANCHID"));
                assets.setHcfid(resultset.getString("HCFID"));
                assets.setDatereleased(dateformat.format(resultset.getDate("DATERELEASED")));//resultset.getDate("DATERELEASED"));
                assets.setReceipt(resultset.getString("RECEIPT"));
                assets.setAmount(resultset.getString("AMOUNT"));

                ACRGBWSResult creator = this.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                if (creator.isSuccess()) {
                    if (!creator.getResult().isEmpty()) {
                        UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class);
                        assets.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                    } else {
                        assets.setCreatedby(creator.getMessage());
                    }
                } else {
                    assets.setCreatedby("DATA NOT FOUND");
                }
                assets.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));
                ACRGBWSResult getcon = this.GETCONTRACTCONID(dataSource, resultset.getString("CONID").trim());
                if (getcon.isSuccess()) {
                    if (!getcon.getResult().isEmpty()) {
                        assets.setConid(getcon.getResult());
                    } else {
                        assets.setConid(getcon.getMessage());
                    }
                } else {
                    assets.setCreatedby("DATA NOT FOUND");
                }
                listassets.add(assets);
            }
            if (!listassets.isEmpty()) {
                result.setMessage("OK");
                result.setResult(utility.ObjectMapper().writeValueAsString(listassets));
            } else {
                result.setMessage("NO DATA FOUND");
            }
            result.setSuccess(true);
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET CONTRACT OF APEX FACILITY
    public ACRGBWSResult ACR_CONTRACT(final DataSource dataSource, final String tags, final String userid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Methods methods = new Methods();
        try (Connection connection = dataSource.getConnection()) {
            ArrayList<Contract> contractlist = new ArrayList<>();
            ArrayList<String> hcflist = new ArrayList<>();
            //-------------- GET APEX FACILITY
            ACRGBWSResult resultfm = methods.GETAPEXFACILITY(dataSource);
            if (resultfm.isSuccess()) {
                List<HealthCareFacility> userlist = Arrays.asList(utility.ObjectMapper().readValue(resultfm.getResult(), HealthCareFacility[].class));
                for (int x = 0; x < userlist.size(); x++) {
                    hcflist.add(userlist.get(x).getHcfid());
                }
            }   //-------------- END OF GET APEX FACILITY
            for (int y = 0; y < hcflist.size(); y++) {
                //--------------------------------------------------------
                CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.ACR_CONTRACT(:tags,:pfchid); end;");
                statement.registerOutParameter("v_result", OracleTypes.CURSOR);
                statement.setString("tags", tags.replaceAll("\\s", "").toUpperCase());
                statement.setString("pfchid", hcflist.get(y));
                statement.execute();
                ResultSet resultset = (ResultSet) statement.getObject("v_result");
                if (resultset.next()) {
                    Contract contract = new Contract();
                    contract.setConid(resultset.getString("CONID"));
                    ACRGBWSResult facility = this.GETFACILITYID(dataSource, resultset.getString("HCFID"));
                    if (facility.isSuccess()) {
                        if (!facility.getResult().isEmpty()) {
                            HealthCareFacility hcfresult = utility.ObjectMapper().readValue(facility.getResult(), HealthCareFacility.class);
                            contract.setHcfid(hcfresult.getHcfname());
                        } else {
                            contract.setHcfid("NOT DATA FOUND");
                        }
                    } else {
                        contract.setHcfid("NOT DATA FOUND");
                    }
                    //END OF GET NETWORK FULL DETAILS
                    contract.setAmount(resultset.getString("AMOUNT"));
                    contract.setStats(resultset.getString("STATS"));
                    ACRGBWSResult creator = this.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                    if (creator.isSuccess()) {
                        if (!creator.getResult().isEmpty()) {
                            contract.setCreatedby(creator.getResult());
                        } else {
                            contract.setCreatedby(creator.getMessage());
                        }
                    } else {
                        contract.setCreatedby("DATA NOT FOUND");
                    }
                    contract.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));//resultset.getString("DATECREATED"));
                    contract.setDatefrom(dateformat.format(resultset.getDate("DATEFROM")));//resultset.getString("DATECOVERED"));
                    contract.setDateto(dateformat.format(resultset.getDate("DATETO")));//resultset.getString("DATECOVERED"));
                    contractlist.add(contract);
                } else {
                    result.setMessage("NO DATA FOUND");
                }
            }

            if (!contractlist.isEmpty()) {
                result.setMessage("OK");
                result.setResult(utility.ObjectMapper().writeValueAsString(contractlist));
            } else {
                result.setMessage("NO DATA FOUND");
            }
            result.setSuccess(true);
        } catch (SQLException | IOException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

// GET CONTRACT USING PRO USERID
    public ACRGBWSResult ACR_CONTRACTPROID(final DataSource dataSource, final String tags, final String userid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Methods methods = new Methods();
        try (Connection connection = dataSource.getConnection()) {
            ArrayList<Contract> contractlist = new ArrayList<>();
            ACRGBWSResult restA = methods.GETROLE(dataSource, userid);//GET (PROID) USING (USERID)
            if (restA.isSuccess()) {
                ACRGBWSResult restB = methods.GETROLEMULITPLE(dataSource, restA.getResult());//GET (NCPN) USING (PROID)
                List<String> restist = Arrays.asList(restB.getResult().split(","));
                if (restB.isSuccess()) {
                    for (int b = 0; b < restist.size(); b++) {
                        //--------------------------------------------------------
                        CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.ACR_CONTRACT(:tags,:pfchid); end;");
                        statement.registerOutParameter("v_result", OracleTypes.CURSOR);
                        statement.setString("tags", tags.replaceAll("\\s", "").toUpperCase());
                        statement.setString("pfchid", restist.get(b));
                        statement.execute();
                        ResultSet resultset = (ResultSet) statement.getObject("v_result");
                        while (resultset.next()) {
                            Contract contract = new Contract();
                            contract.setConid(resultset.getString("CONID"));

                            ACRGBWSResult facility = methods.GETMBWITHID(dataSource, resultset.getString("HCFID"));
                            if (facility.isSuccess()) {
                                if (facility.isSuccess()) {
                                    ManagingBoard mb = utility.ObjectMapper().readValue(facility.getResult(), ManagingBoard.class);
                                    contract.setHcfid(mb.getMbname());
                                } else {
                                    contract.setHcfid("NO DATA FOUND");
                                }
                            } else {
                                contract.setHcfid("NOT DATA FOUND");
                            }
                            contract.setAmount(resultset.getString("AMOUNT"));
                            contract.setStats(resultset.getString("STATS"));

                            ACRGBWSResult creator = this.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                            if (creator.isSuccess()) {
                                if (!creator.getResult().isEmpty()) {
                                    UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class);
                                    contract.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                                } else {
                                    contract.setCreatedby(creator.getMessage());
                                }
                            } else {
                                contract.setCreatedby("DATA NOT FOUND");
                            }
                            contract.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));//resultset.getString("DATECREATED"));
                            contract.setDatefrom(dateformat.format(resultset.getDate("DATEFROM")));//resultset.getString("DATECOVERED"));
                            contract.setDateto(dateformat.format(resultset.getDate("DATETO")));//resultset.getString("DATECOVERED"));
                            contract.setTranscode(resultset.getString("TRANSCODE"));
                            contractlist.add(contract);
                        }
                    }
                } else {
                    result.setMessage("NO DATA FOUND");
                }
            }
            //---------------------------------------------------------------

            if (!contractlist.isEmpty()) {
                result.setMessage("OK");
                result.setResult(utility.ObjectMapper().writeValueAsString(contractlist));
            } else {
                result.setMessage("NO DATA FOUND");
            }
            result.setSuccess(true);
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET ALL HCPN CONTRACT
    //userid =0
    public ACRGBWSResult GETALLHCPNCONTRACT(final DataSource dataSource, final String tags, final String userid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Methods methods = new Methods();
        try (Connection connection = dataSource.getConnection()) {
            ArrayList<Contract> contractlist = new ArrayList<>();
            //--------------------------------------------------------
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.ACR_CONTRACT(:tags,:pfchid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("tags", tags.replaceAll("\\s", "").toUpperCase());
            statement.setString("pfchid", userid);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            while (resultset.next()) {
                Contract contract = new Contract();
                contract.setConid(resultset.getString("CONID"));

                ACRGBWSResult facility = methods.GETMBWITHID(dataSource, resultset.getString("HCFID"));
                if (facility.isSuccess()) {
                    if (facility.isSuccess()) {
                        ManagingBoard mb = utility.ObjectMapper().readValue(facility.getResult(), ManagingBoard.class);
                        contract.setHcfid(mb.getMbname());
                    } else {
                        contract.setHcfid("NO DATA FOUND");
                    }
                } else {
                    contract.setHcfid("NOT DATA FOUND");
                }
                //END OF GET NETWORK FULL DETAILS
                contract.setAmount(resultset.getString("AMOUNT"));
                contract.setStats(resultset.getString("STATS"));
                ACRGBWSResult creator = this.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                if (creator.isSuccess()) {
                    if (!creator.getResult().isEmpty()) {
                        contract.setCreatedby(creator.getResult());
                    } else {
                        contract.setCreatedby(creator.getMessage());
                    }
                } else {
                    contract.setCreatedby("DATA NOT FOUND");
                }
                contract.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));//resultset.getString("DATECREATED"));
                contract.setDatefrom(dateformat.format(resultset.getDate("DATEFROM")));//resultset.getString("DATECOVERED"));
                contract.setDateto(dateformat.format(resultset.getDate("DATETO")));//resultset.getString("DATECOVERED"));
                contractlist.add(contract);
            }

            if (!contractlist.isEmpty()) {
                result.setMessage("OK");
                result.setResult(utility.ObjectMapper().writeValueAsString(contractlist));
            } else {
                result.setMessage("NO DATA FOUND");
            }
            result.setSuccess(true);
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET CONTRACT MB USING MB USERID ACCOUNT
    //userid =0
    public ACRGBWSResult GETCONTRACTUNDERMB(final DataSource dataSource, final String tags, final String userid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Methods methods = new Methods();
        try (Connection connection = dataSource.getConnection()) {
            ArrayList<Contract> contractlist = new ArrayList<>();
            ACRGBWSResult reatA = methods.GETROLEWITHID(dataSource, userid);
            if (reatA.isSuccess()) {
                //--------------------------------------------------------
                CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.ACR_CONTRACT(:tags,:pfchid); end;");
                statement.registerOutParameter("v_result", OracleTypes.CURSOR);
                statement.setString("tags", tags.replaceAll("\\s", "").toUpperCase());
                statement.setString("pfchid", reatA.getResult());
                statement.execute();
                ResultSet resultset = (ResultSet) statement.getObject("v_result");
                while (resultset.next()) {
                    Contract contract = new Contract();
                    contract.setConid(resultset.getString("CONID"));
                    ACRGBWSResult facility = methods.GETMBWITHID(dataSource, resultset.getString("HCFID"));
                    if (facility.isSuccess()) {
                        if (facility.isSuccess()) {
                            ManagingBoard mb = utility.ObjectMapper().readValue(facility.getResult(), ManagingBoard.class);
                            contract.setHcfid(mb.getMbname());
                        } else {
                            contract.setHcfid("NO DATA FOUND");
                        }
                    } else {
                        contract.setHcfid("NOT DATA FOUND");
                    }
                    //END OF GET NETWORK FULL DETAILS
                    contract.setAmount(resultset.getString("AMOUNT"));
                    contract.setStats(resultset.getString("STATS"));
                    ACRGBWSResult creator = this.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                    if (creator.isSuccess()) {
                        if (!creator.getResult().isEmpty()) {
                            contract.setCreatedby(creator.getResult());
                        } else {
                            contract.setCreatedby(creator.getMessage());
                        }
                    } else {
                        contract.setCreatedby("DATA NOT FOUND");
                    }
                    contract.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));//resultset.getString("DATECREATED"));
                    contract.setDatefrom(dateformat.format(resultset.getDate("DATEFROM")));//resultset.getString("DATECOVERED"));
                    contract.setDateto(dateformat.format(resultset.getDate("DATETO")));//resultset.getString("DATECOVERED"));
                    contractlist.add(contract);
                }

            } else {
                result.setMessage("NO DATA FOUND");
            }
            if (!contractlist.isEmpty()) {
                result.setMessage("OK");
                result.setResult(utility.ObjectMapper().writeValueAsString(contractlist));
            } else {
                result.setMessage("NO DATA FOUND");
            }
            result.setSuccess(true);
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET CONTRACT MB USING MB USERID ACCOUNT
    //userid =0
    public ACRGBWSResult GETCONTRACTCONID(final DataSource dataSource, final String pconid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            //--------------------------------------------------------
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETCONTRACTCONID(:tags,:pconid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("tags", "ACTIVE");
            statement.setString("pconid", pconid);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                Contract contract = new Contract();
                contract.setConid(resultset.getString("CONID"));
                contract.setHcfid(resultset.getString("HCFID"));
                //END OF GET NETWORK FULL DETAILS
                contract.setAmount(resultset.getString("AMOUNT"));
                contract.setStats(resultset.getString("STATS"));
                ACRGBWSResult creator = this.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                if (creator.isSuccess()) {
                    if (!creator.getResult().isEmpty()) {
                        contract.setCreatedby(creator.getResult());
                    } else {
                        contract.setCreatedby(creator.getMessage());
                    }
                } else {
                    contract.setCreatedby("DATA NOT FOUND");
                }
                contract.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));//resultset.getString("DATECREATED"));
                contract.setDatefrom(dateformat.format(resultset.getDate("DATEFROM")));//resultset.getString("DATECOVERED"));
                contract.setDateto(dateformat.format(resultset.getDate("DATETO")));//resultset.getString("DATECOVERED"));
                result.setMessage("OK");
                result.setResult(utility.ObjectMapper().writeValueAsString(contract));
                result.setSuccess(true);
            } else {
                result.setMessage("NO DATA FOUND");
            }

        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

//ACR_HCF
    public ACRGBWSResult ACR_HCF(final DataSource dataSource, final String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.ACR_HCF(:tags); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("tags", tags.replaceAll("\\s", "").toUpperCase());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            ArrayList<HealthCareFacility> listHCF = new ArrayList<>();
            while (resultset.next()) {

                HealthCareFacility hcf = new HealthCareFacility();
                hcf.setHcfid(resultset.getString("HCFID"));
                hcf.setHcfname(resultset.getString("HCFNAME"));
                hcf.setHcfaddress(resultset.getString("HCFADDRESS"));
                hcf.setHcfcode(resultset.getString("HCFCODE"));
                ACRGBWSResult creator = this.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());

                if (creator.isSuccess()) {
                    if (!creator.getResult().isEmpty()) {
                        UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class
                        );
                        hcf.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                    } else {
                        hcf.setCreatedby(creator.getMessage());
                    }
                } else {
                    hcf.setCreatedby("DATA NOT FOUND");
                }
                hcf.setType(resultset.getString("HCFTYPE"));
                hcf.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));//resultset.getString("DATECREATED"));
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
            Logger
                    .getLogger(FetchMethods.class
                            .getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //ACR_TRANCH METHOD
    public ACRGBWSResult ACR_TRANCH(final DataSource dataSource, final String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.ACR_TRANCH(:tags); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("tags", tags.replaceAll("\\s", "").toUpperCase());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            ArrayList<Tranch> listtranch = new ArrayList<>();
            while (resultset.next()) {
                Tranch tranch = new Tranch();
                tranch.setTranchid(resultset.getString("TRANCHID"));
                tranch.setTranchtype(resultset.getString("TRANCHTYPE").toUpperCase());
                tranch.setPercentage(resultset.getString("PERCENTAGE"));
                ACRGBWSResult creator = this.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());

                if (creator.isSuccess()) {
                    if (!creator.getResult().isEmpty()) {
                        UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class
                        );
                        tranch.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                    } else {
                        tranch.setCreatedby(creator.getMessage());
                    }
                } else {
                    tranch.setCreatedby("DATA NOT FOUND");
                }
                tranch.setStats(resultset.getString("STATS"));
                tranch.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));//resultset.getString("DATECREATED"));
                listtranch.add(tranch);
            }
            if (!listtranch.isEmpty()) {
                result.setMessage("OK");
                result.setResult(utility.ObjectMapper().writeValueAsString(listtranch));
            } else {
                result.setMessage("NO DATA FOUND");
            }
            result.setSuccess(true);
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger
                    .getLogger(FetchMethods.class
                            .getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    // THIS AREA IS FOR USER DETAILS 
    public ACRGBWSResult ACR_USER_DETAILS(final DataSource dataSource, final String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.ACR_USER_DETAILS(:tags); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("tags", tags.replaceAll("\\s", "").toUpperCase());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            ArrayList<UserInfo> listuserinfo = new ArrayList<>();
            while (resultset.next()) {
                UserInfo userinfo = new UserInfo();
                userinfo.setDid(resultset.getString("DID"));
                userinfo.setFirstname(resultset.getString("FIRSTNAME"));
                userinfo.setLastname(resultset.getString("LASTNAME"));
                userinfo.setMiddlename(resultset.getString("MIDDLENAME"));
                userinfo.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));//resultset.getString("DATECREATED"));
                // userinfo.setAreaid(resultset.getString("AREAID"));
                ACRGBWSResult creator = this.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());

                if (creator.isSuccess()) {
                    if (!creator.getResult().isEmpty()) {
                        UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class
                        );
                        userinfo.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                    } else {
                        userinfo.setCreatedby(creator.getMessage());
                    }
                } else {
                    userinfo.setCreatedby("DATA NOT FOUND");
                }
                userinfo.setStats(resultset.getString("STATS"));
                listuserinfo.add(userinfo);
            }
            if (!listuserinfo.isEmpty()) {
                result.setMessage("OK");
                result.setResult(utility.ObjectMapper().writeValueAsString(listuserinfo));
            } else {
                result.setMessage("NO DATA FOUND");
            }
            result.setSuccess(true);

        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger
                    .getLogger(FetchMethods.class
                            .getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //THIS AREA IS FOR USER LEVEL
    public ACRGBWSResult ACR_USER_LEVEL(final DataSource dataSource, final String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.ACR_USER_LEVEL(:tags); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("tags", tags.replaceAll("\\s", "").toUpperCase());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            ArrayList<UserLevel> listuserlevel = new ArrayList<>();
            while (resultset.next()) {
                UserLevel userlevel = new UserLevel();
                userlevel.setLevelid(resultset.getString("LEVELID"));
                userlevel.setLevdetails(resultset.getString("LEVDETAILS"));
                userlevel.setLevname(resultset.getString("LEVNAME"));
                ACRGBWSResult creator = this.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());

                if (creator.isSuccess()) {
                    if (!creator.getResult().isEmpty()) {
                        UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class
                        );
                        userlevel.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                    } else {
                        userlevel.setCreatedby(creator.getMessage());
                    }
                } else {
                    userlevel.setCreatedby("DATA NOT FOUND");
                }
                userlevel.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));//resultset.getString("DATECREATED"));
                userlevel.setStats(resultset.getString("STATS"));
                listuserlevel.add(userlevel);
            }
            if (!listuserlevel.isEmpty()) {
                result.setMessage("OK");
                result.setResult(utility.ObjectMapper().writeValueAsString(listuserlevel));
            } else {
                result.setMessage("NO DATA FOUND");
            }
            result.setSuccess(true);
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger
                    .getLogger(FetchMethods.class
                            .getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //THIS AREA IS FOR USER LEVEL
    public ACRGBWSResult ACR_PRO(final DataSource dataSource, final String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.ACR_PRO(:tags); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("tags", tags.replaceAll("\\s", "").toUpperCase());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            ArrayList<Pro> prolist = new ArrayList<>();
            while (resultset.next()) {
                Pro pro = new Pro();
                pro.setProid(resultset.getString("PROID"));
                pro.setProname(resultset.getString("PRONAME"));
                ACRGBWSResult creator = this.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());

                if (creator.isSuccess()) {
                    if (!creator.getResult().isEmpty()) {
                        UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class
                        );
                        pro.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                    } else {
                        pro.setCreatedby(creator.getMessage());
                    }
                } else {
                    pro.setCreatedby("DATA NOT FOUND");
                }
                pro.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));
                pro.setStats(resultset.getString("STATS"));
                prolist.add(pro);
            }
            if (!prolist.isEmpty()) {
                result.setMessage("OK");
                result.setResult(utility.ObjectMapper().writeValueAsString(prolist));
            } else {
                result.setMessage("NO DATA FOUND");
            }
            result.setSuccess(true);
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger
                    .getLogger(FetchMethods.class
                            .getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult ACR_USER(final DataSource dataSource, final String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.ACR_USER(:tags); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("tags", tags.replaceAll("\\s", "").toUpperCase());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            ArrayList<User> listuser = new ArrayList<>();
            while (resultset.next()) {
                User user = new User();
                user.setUserid(resultset.getString("USERID"));
                ACRGBWSResult levelresult = this.GETUSERLEVEL(dataSource, resultset.getString("LEVELID").trim());
                if (levelresult.isSuccess()) {
                    if (!levelresult.getResult().isEmpty()) {
                        user.setLeveid(levelresult.getResult());
                    } else {
                        user.setLeveid(levelresult.getMessage());
                    }
                } else {
                    user.setLeveid(levelresult.getMessage());
                }
                user.setUsername(resultset.getString("USERNAME"));
                if (resultset.getString("STATS").equals("2")) {
                    user.setUserpassword(resultset.getString("USERPASSWORD"));
                } else {
                    user.setUserpassword(resultset.getString("USERPASSWORD"));
                }

                user.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));//resultset.getString("DATECREATED"));
                user.setStatus(resultset.getString("STATS"));
                user.setDid(resultset.getString("DID"));
                ACRGBWSResult creator = this.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());

                if (creator.isSuccess()) {
                    if (!creator.getResult().isEmpty()) {
                        UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class
                        );
                        user.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                    } else {
                        user.setCreatedby(creator.getMessage());
                    }
                } else {
                    user.setCreatedby("DATA NOT FOUND");
                }
                listuser.add(user);
            }
            if (!listuser.isEmpty()) {
                result.setMessage("OK");
                result.setResult(utility.ObjectMapper().writeValueAsString(listuser));
            } else {
                result.setMessage("NO DATA FOUND");
            }
            result.setSuccess(true);
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger
                    .getLogger(FetchMethods.class
                            .getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET TRANCH AMOUNT PERCENTAGE
    public ACRGBWSResult GETTRANCHAMOUNT(final DataSource dataSource, final String p_tranchid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETTRANCHAMOUNT(:p_tranchid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("p_tranchid", p_tranchid);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                result.setMessage("OK");
                result.setResult(resultset.getString("PERCENTAGE"));
            } else {
                result.setMessage("TRANCH NOT FOUND");
            }
            result.setSuccess(true);
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger
                    .getLogger(FetchMethods.class
                            .getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET CONTRACT AMOUNT
    public ACRGBWSResult GETCONTRACTAMOUNT(final DataSource dataSource, final String p_conid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETCONTRACTAMOUNT(:p_conid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("p_conid", p_conid);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                result.setMessage("OK");
                result.setResult(resultset.getString("AMOUNT"));
            } else {
                result.setMessage("CONTRACT NOT FOUND");
            }
            result.setSuccess(true);
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger
                    .getLogger(FetchMethods.class
                            .getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET USER LEVEL
    public ACRGBWSResult GETUSERLEVEL(final DataSource dataSource, final String levid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETLEVEL(:levid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("levid", levid);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                result.setMessage("OK");
                result.setResult(resultset.getString("LEVNAME"));
            } else {
                result.setMessage("ROLE NAME NOT FOUND");
            }
            result.setSuccess(true);
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger
                    .getLogger(FetchMethods.class
                            .getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET ASSETS WITH PARAMETER
    public ACRGBWSResult GETASSETSWITHPARAM(final DataSource dataSource, final String phcfid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETASSETSBYHCFID(:phcfid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("phcfid", phcfid);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                Assets assets = new Assets();
                assets.setAssetid(resultset.getString("ASSETSID"));
                assets.setHcfid(resultset.getString("HCFID"));
                assets.setTranchid(resultset.getString("TRANCHID"));
                assets.setReceipt(resultset.getString("RECEIPT"));
                assets.setAmount(resultset.getString("AMOUNT"));
                ACRGBWSResult creator = this.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());

                if (creator.isSuccess()) {
                    if (!creator.getResult().isEmpty()) {
                        UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class
                        );
                        assets.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                    } else {
                        assets.setCreatedby(creator.getMessage());
                    }
                } else {
                    assets.setCreatedby("DATA NOT FOUND");
                }
                assets.setDatereleased(dateformat.format(resultset.getDate("DATERELEASED")));//resultset.getString("DATERELEASED"));
                assets.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));//resultset.getString("DATECREATED"));
                result.setMessage("OK");
                result.setResult(utility.ObjectMapper().writeValueAsString(assets));
            } else {
                result.setMessage("NO DATA FOUND");
            }
            result.setSuccess(true);
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger
                    .getLogger(FetchMethods.class
                            .getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET ACTIVITY LOGS
    public ACRGBWSResult ACRACTIVTYLOGS(final DataSource dataSource) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.ACRACTIVTYLOGS(); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            ArrayList<UserActivity> logslist = new ArrayList<>();
            while (resultset.next()) {
                UserActivity useractivity = new UserActivity();
                useractivity.setActid(resultset.getString("ACTID"));
                useractivity.setActdate(datetimeformat.format(resultset.getTimestamp("ACTDATE")));
                useractivity.setActdetails(resultset.getString("ACTDETAILS"));

                ACRGBWSResult creator = this.GETFULLDETAILS(dataSource, resultset.getString("ACTBY").trim());

                if (creator.isSuccess()) {
                    if (!creator.getResult().isEmpty()) {
                        UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class
                        );
                        useractivity.setActby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                    } else {
                        useractivity.setActby(creator.getMessage());
                    }
                } else {
                    useractivity.setActby("DATA NOT FOUND");
                }

                logslist.add(useractivity);
            }
            if (!logslist.isEmpty()) {
                result.setMessage("OK");
                result.setResult(utility.ObjectMapper().writeValueAsString(logslist));
            } else {
                result.setMessage("NO DATA FOUND");
            }
            result.setSuccess(true);
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger
                    .getLogger(FetchMethods.class
                            .getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

//    public ACRGBWSResult ACR_TRANS_TYPE_TBL(final DataSource dataSource) {
//        ACRGBWSResult result = utility.ACRGBWSResult();
//        result.setMessage("");
//        result.setResult("");
//        result.setSuccess(false);
//        try (Connection connection = dataSource.getConnection()) {
//            CallableStatement statement = connection.prepareCall("begin :trans_type := ACR_GB.ACRGBPKG.ACR_TRANS_TYPE(); end;");
//            statement.registerOutParameter("trans_type", OracleTypes.CURSOR);
//            statement.execute();
//            ResultSet resultset = (ResultSet) statement.getObject("trans_type");
//            ArrayList<TransType> listTransType = new ArrayList<>();
//            while (resultset.next()) {
//                TransType transtype = new TransType();
//                transtype.setTypeid(resultset.getString("TID"));
//                transtype.setType(resultset.getString("TTYPE"));
//                listTransType.add(transtype);
//            }
//            if (!listTransType.isEmpty()) {
//                result.setMessage("DATA FOUND");
//                result.setSuccess(true);
//                result.setResult(utility.ObjectMapper().writeValueAsString(listTransType));
//            } else {
//                result.setMessage("NO DATA FOUND");
//                result.setSuccess(false);
//            }
//
//        } catch (SQLException | IOException ex) {
//            result.setMessage(ex.toString());
//            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return result;
//    }
//
//    // ACR GB GET USER LEVE DETAILS
//    public ACRGBWSResult ACRUSERLEVEL(final DataSource dataSource, final String p_userid) {
//        ACRGBWSResult result = utility.ACRGBWSResult();
//        result.setMessage("");
//        result.setResult("");
//        result.setSuccess(false);
//        try (Connection connection = dataSource.getConnection()) {
//            CallableStatement statement = connection.prepareCall("begin :userlevel := ACR_GB.ACRGBPKG.ACRUSERLEVEL(:p_userid); end;");
//            statement.registerOutParameter("userlevel", OracleTypes.CURSOR);
//            statement.setString("p_userid", p_userid);
//            statement.execute();
//            ResultSet resultset = (ResultSet) statement.getObject("userlevel");
//            if (resultset.next()) {
//                UserLevel userrole = new UserLevel();
//                userrole.setRoleid("");
//                userrole.setRolename(resultset.getString("ROLENAME"));
//                result.setResult(utility.ObjectMapper().writeValueAsString(userrole));
//                result.setSuccess(true);
//            } else {
//                result.setMessage("NO DATA FOUND");
//                result.setSuccess(false);
//            }
//
//        } catch (SQLException | IOException ex) {
//            result.setMessage(ex.toString());
//            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return result;
//    }
//
//    // ACR GB ACR_ASSETS_TBL , ACR_ASSETS_TYPE_TBL , ACR_HF_TBL
//    public ACRGBWSResult JOINASSETS(final DataSource dataSource) {
//        ACRGBWSResult result = utility.ACRGBWSResult();
//        result.setMessage("");
//        result.setResult("");
//        result.setSuccess(false);
//        try (Connection connection = dataSource.getConnection()) {
//            CallableStatement statement = connection.prepareCall("begin :joinresult := ACR_GB.ACRGBPKG.JOINASSETS(); end;");
//            statement.registerOutParameter("joinresult", OracleTypes.CURSOR);
//            statement.execute();
//            ResultSet resultset = (ResultSet) statement.getObject("joinresult");
//            ArrayList<Assets> assestlist = new ArrayList<>();
//            while (resultset.next()) {
//                Assets assests = new Assets();
//                assests.setAssetsid(resultset.getString("ASSETSID"));
//                assests.setAmount(resultset.getString("AMOUNT"));
//                assests.setTransnumber(resultset.getString("TRANSNUMBER"));
//                assests.setSourceoffunds(resultset.getString("SOURCEOFFUNDS"));
//                assests.setDaterecieved(resultset.getString("DATERECIEVED"));
//                assests.setTypeofassets(resultset.getString("TYPENAME"));
//                assests.setAccreditation(resultset.getString("HCINAME"));
//                assestlist.add(assests);
//            }
//            if (!assestlist.isEmpty()) {
//                result.setResult(utility.ObjectMapper().writeValueAsString(assestlist));
//                result.setSuccess(true);
//            } else {
//                result.setMessage("NO AVAILABLE DATA");
//                result.setSuccess(false);
//            }
//
//        } catch (SQLException | IOException ex) {
//            result.setMessage(ex.toString());
//            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return result;
//    }
    //GET NCLAIMS DATA AND AMOUNT OF ITS CLAIMS TOTAL
    public ACRGBWSResult GETNCLAIMS(final DataSource dataSource, final String u_accreno, final String u_tags, final Date u_from, final Date u_to) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKG.GETNCLAIMS(:u_accreno,:u_tags,:u_from,:u_to); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("u_accreno", u_accreno);
            statement.setString("u_tags", u_tags);
            statement.setDate("u_from", u_from);
            statement.setDate("u_to", u_to);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                NclaimsData nclaimsdata = new NclaimsData();
                nclaimsdata.setAccreno(resultset.getString("ACCRENO"));
                nclaimsdata.setClaimamount(resultset.getString("CTOTAL"));
                nclaimsdata.setTotalclaims(resultset.getString("COUNTVAL"));

                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(nclaimsdata));
            } else {
                result.setMessage("NO DATA FOUND");
                result.setSuccess(false);
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult GETCOMPUTATION(final DataSource dataSource, final String u_accreno, final String u_date, final String u_tags) {

        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(true);

        return null;
    }

//    //GET MANAGING BOARD DATA USING PRO USERID
//    public ACRGBWSResult GetManagingBoard(final DataSource dataSource, final String tags, final String puserid) throws ParseException {
//        ACRGBWSResult result = utility.ACRGBWSResult();
//        result.setMessage("");
//        result.setResult("");
//        result.setSuccess(false);
//        try (Connection connection = dataSource.getConnection()) {
//            Methods methods = new Methods();
//            ArrayList<ManagingBoard> mblist = new ArrayList<>();
//            ACRGBWSResult restA = methods.GETROLE(dataSource, puserid);
//            if (restA.isSuccess()) {
//                ACRGBWSResult restB = methods.GETROLEMULITPLE(dataSource, restA.getResult());
//                List<String> accessidlist = Arrays.asList(restB.getResult().split(","));
//                if (accessidlist.size() > 0) {
//                    for (int x = 0; x < accessidlist.size(); x++) {
//                        //-------------------------------------------------------
//                        CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETMBWITHID(:pid); end;");
//                        statement.registerOutParameter("v_result", OracleTypes.CURSOR);
//                        statement.setString("pid", accessidlist.get(x));
//                        statement.execute();
//                        ResultSet resultset = (ResultSet) statement.getObject("v_result");
//                        while (resultset.next()) {
//                            ManagingBoard mb = new ManagingBoard();
//                            mb.setMbid(resultset.getString("MBID"));
//                            mb.setMbname(resultset.getString("MBNAME"));
//                            mb.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));
//                            ACRGBWSResult creator = this.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
//                            if (creator.isSuccess()) {
//                                if (!creator.getResult().isEmpty()) {
//                                    UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class
//                                    );
//                                    mb.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
//                                } else {
//                                    mb.setCreatedby(creator.getMessage());
//                                }
//                            } else {
//                                mb.setCreatedby("NO DATA FOUND");
//                            }
//
//                            mb.setStatus(resultset.getString("STATUS"));
//                            mblist.add(mb);
//                        }
//                        //-------------------------------------------------------
//
//                    }
//                }
//            }
//
//            if (mblist.size() > 0) {
//                result.setResult(utility.ObjectMapper().writeValueAsString(mblist));
//                result.setMessage("OK");
//                result.setSuccess(true);
//            } else {
//                result.setMessage("NO DATA FOUND");
//            }
//        } catch (SQLException | IOException ex) {
//            result.setMessage(ex.toString());
//            Logger
//                    .getLogger(FetchMethods.class
//                            .getName()).log(Level.SEVERE, null, ex);
//        }
//        return result;
//    }
    //GET MANAGING BOARD ALL
    public ACRGBWSResult GetManagingBoard(final DataSource dataSource, final String tags) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            ArrayList<ManagingBoard> mblist = new ArrayList<>();
            //-------------------------------------------------------
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETMB(:tags); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("tags", tags.toUpperCase().trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            while (resultset.next()) {
                ManagingBoard mb = new ManagingBoard();
                mb.setMbid(resultset.getString("MBID"));
                mb.setMbname(resultset.getString("MBNAME"));
                mb.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));
                ACRGBWSResult creator = this.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                if (creator.isSuccess()) {
                    if (!creator.getResult().isEmpty()) {
                        UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class
                        );
                        mb.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                    } else {
                        mb.setCreatedby(creator.getMessage());
                    }
                } else {
                    mb.setCreatedby("NO DATA FOUND");
                }

                mb.setStatus(resultset.getString("STATUS"));
                mblist.add(mb);
            }
            //-------------------------------------------------------

            if (mblist.size() > 0) {
                result.setResult(utility.ObjectMapper().writeValueAsString(mblist));
                result.setMessage("OK");
                result.setSuccess(true);
            } else {
                result.setMessage("NO DATA FOUND");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult GETROLETWOPARAM(final DataSource dataSource, final String puserid, final String paccessid) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETROLETWOPARAM(:puserid,:paccessid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("puserid", puserid);
            statement.setString("paccessid", paccessid);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                UserRoleIndex role = new UserRoleIndex();
                role.setUserid(resultset.getString("USERID"));
                role.setAccessid(resultset.getString("ACCESSID"));
                result.setResult(utility.ObjectMapper().writeValueAsString(role));
                result.setMessage("OK");
                result.setSuccess(true);
            } else {
                result.setMessage("NO DATA FOUND");
            }

        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
