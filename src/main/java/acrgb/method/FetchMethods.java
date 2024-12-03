/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.method;

import acrgb.structure.ACRGBWSResult;
import acrgb.structure.Accreditation;
import acrgb.structure.Assets;
import acrgb.structure.Book;
import acrgb.structure.Contract;
import acrgb.structure.ContractDate;
import acrgb.structure.DateSettings;
import acrgb.structure.HealthCareFacility;
import acrgb.structure.ManagingBoard;
import acrgb.structure.NclaimsData;
import acrgb.structure.Pro;
import acrgb.structure.Total;
import acrgb.structure.Tranch;
import acrgb.structure.User;
import acrgb.structure.UserActivity;
import acrgb.structure.UserInfo;
import acrgb.structure.UserLevel;
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
 * @author DRG_SHADOWBILLING
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
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKG.GETFACILITY(:hcfrid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("hcfrid", uhcfid);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                HealthCareFacility hcf = new HealthCareFacility();
                hcf.setHcfname(resultset.getString("HCFNAME"));
                hcf.setHcfaddress(resultset.getString("HCFADDRESS"));
                hcf.setHcfcode(resultset.getString("HCFCODE"));
                hcf.setType(resultset.getString("HCFTYPE"));
                hcf.setHcilevel(resultset.getString("HCILEVEL"));
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(hcf));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult GETROLEINDEXUSERID(final DataSource dataSource, final String puserid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.INDEXUSERIDHCPN(:puserid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("puserid", puserid.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            ArrayList<UserRoleIndex> roleList = new ArrayList<>();
            while (resultset.next()) {
                UserRoleIndex role = new UserRoleIndex();
                role.setUserid(resultset.getString("USERID"));
                role.setAccessid(resultset.getString("ACCESSID"));
                roleList.add(role);
            }
            if (roleList.size() > 0) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(roleList));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult GETROLEINDEXUSERIDHCI(final DataSource dataSource, final String paccessid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.INDEXUSERIDHCI(:paccessid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("paccessid", paccessid.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                UserRoleIndex role = new UserRoleIndex();
                role.setUserid(resultset.getString("USERID"));
                role.setAccessid(resultset.getString("ACCESSID"));
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(role));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET ALL FACILITY(MULTIPLE)
    public ACRGBWSResult GETALLFACILITY(final DataSource dataSource, final String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKG.ACR_HCF(); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            ArrayList<HealthCareFacility> hcflist = new ArrayList<>();
            while (resultset.next()) {
                if (resultset.getString("HCFTYPE") != null) {
                    HealthCareFacility hcf = new HealthCareFacility();
                    hcf.setHcfname(resultset.getString("HCFNAME"));
                    hcf.setHcfaddress(resultset.getString("HCFADDRESS"));
                    hcf.setHcfcode(resultset.getString("HCFCODE"));
                    hcf.setHcilevel(resultset.getString("HCILEVEL"));
                    hcf.setType(resultset.getString("HCFTYPE"));
                    if (resultset.getString("HCFTYPE").equals("AH")) {
                        //GETAPPELLATE
                        ACRGBWSResult restA = this.GetAffiliate(dataSource, resultset.getString("HCFCODE"), tags.toUpperCase().trim());
                        List<String> hcpnlist = Arrays.asList(restA.getResult().split(","));
                        ArrayList<String> mblist = new ArrayList<>();
                        ArrayList<String> prolist = new ArrayList<>();
                        for (int h = 0; h < hcpnlist.size(); h++) {
                            ACRGBWSResult mgresult = new Methods().GETMBWITHID(dataSource, hcpnlist.get(h));
                            if (mgresult.isSuccess()) {
                                ManagingBoard mb = utility.ObjectMapper().readValue(mgresult.getResult(), ManagingBoard.class);
                                mblist.add(mb.getControlnumber());
                                ACRGBWSResult restC = new Methods().GETROLEREVERESE(dataSource, mb.getControlnumber(), tags);
                                if (restC.isSuccess()) {
                                    //GET PRO USING PROID
                                    ACRGBWSResult getproid = new Methods().GetProWithPROID(dataSource, restC.getResult());
                                    if (getproid.isSuccess()) {
                                        Pro pro = utility.ObjectMapper().readValue(getproid.getResult(), Pro.class);
                                        prolist.add(pro.getProname());
                                    }
                                }
                            }
                        }
                        if (mblist.isEmpty()) {
                            hcf.setMb("N/A");
                        } else if (prolist.isEmpty()) {
                            hcf.setProid("N/A");
                        } else {
                            hcf.setProid(prolist.toString());
                            hcf.setMb(mblist.toString());
                        }
                    } else {
                        ACRGBWSResult methodsresult = new Methods().GETROLEREVERESE(dataSource, resultset.getString("HCFCODE"), tags);
                        if (methodsresult.isSuccess()) {
                            ACRGBWSResult mgresult = new Methods().GETMBWITHID(dataSource, methodsresult.getResult());
                            if (mgresult.isSuccess()) {
                                ManagingBoard mb = utility.ObjectMapper().readValue(mgresult.getResult(), ManagingBoard.class);
                                hcf.setMb(mb.getControlnumber());
                                ACRGBWSResult restC = new Methods().GETROLEREVERESE(dataSource, mb.getControlnumber(), tags);
                                if (restC.isSuccess()) {
                                    //GET PRO USING PROID
                                    ACRGBWSResult getproid = new Methods().GetProWithPROID(dataSource, restC.getResult());
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

                    }
                    hcf.setAmount("N/A");
                    hcf.setRemainingbalance("N/A");
                    hcflist.add(hcf);
                }

            }
            if (hcflist.size() > 0) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(hcflist));
            } else {
                result.setMessage("N/A");
            }
            //----------------------------------------------------------------------
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET ALL FACILITY(MULTIPLE)
    public ACRGBWSResult GETFACILITYUNDERMB(final DataSource dataSource, final String puserid,
            final String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ArrayList<HealthCareFacility> hcflist = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            ACRGBWSResult restAA = new Methods().GETROLE(dataSource, puserid, tags.toUpperCase().trim());
            if (restAA.isSuccess()) {
                ACRGBWSResult restBB = new Methods().GETROLEMULITPLE(dataSource, restAA.getResult(), tags.toUpperCase().trim());
                if (restBB.isSuccess()) {
                    List<String> FacilityList = Arrays.asList(restBB.getResult().split(","));
                    for (int v = 0; v < FacilityList.size(); v++) {
                        //---------------------------------------------------------------
                        CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKG.getfacility(:hcfrid); end;");
                        statement.registerOutParameter("v_result", OracleTypes.CURSOR);
                        statement.setString("hcfrid", FacilityList.get(v));
                        statement.execute();
                        ResultSet resultset = (ResultSet) statement.getObject("v_result");
                        if (resultset.next()) {
                            HealthCareFacility hcf = new HealthCareFacility();
                            hcf.setHcfname(resultset.getString("HCFNAME"));
                            hcf.setHcfaddress(resultset.getString("HCFADDRESS"));
                            hcf.setHcfcode(resultset.getString("HCFCODE"));
                            hcf.setHcilevel(resultset.getString("HCILEVEL"));
                            hcf.setType(resultset.getString("HCFTYPE"));
                            //-------------------------------------------------
                            if (resultset.getString("HCFTYPE").equals("AH")) {
                                //GETAPPELLATE
                                ACRGBWSResult restA = this.GetAffiliate(dataSource, resultset.getString("HCFCODE"), tags.toUpperCase().trim());
                                List<String> hcpnlist = Arrays.asList(restA.getResult().split(","));
                                ArrayList<String> mblist = new ArrayList<>();
                                ArrayList<String> prolist = new ArrayList<>();
                                for (int h = 0; h < hcpnlist.size(); h++) {
                                    ACRGBWSResult mgresult = new Methods().GETMBWITHID(dataSource, hcpnlist.get(h));
                                    if (mgresult.isSuccess()) {
                                        ManagingBoard mb = utility.ObjectMapper().readValue(mgresult.getResult(), ManagingBoard.class);
                                        mblist.add(mb.getControlnumber());
                                        ACRGBWSResult restC = new Methods().GETROLEREVERESE(dataSource, mb.getControlnumber(), tags.toUpperCase().trim());
                                        if (restC.isSuccess()) {
                                            //GET PRO USING PROID
                                            ACRGBWSResult getproid = new Methods().GetProWithPROID(dataSource, restC.getResult());
                                            if (getproid.isSuccess()) {
                                                Pro pro = utility.ObjectMapper().readValue(getproid.getResult(), Pro.class);
                                                prolist.add(pro.getProname());
                                            }
                                        }
                                    }
                                }
                                if (mblist.isEmpty()) {
                                    hcf.setMb("N/A");
                                } else if (prolist.isEmpty()) {
                                    hcf.setProid("N/A");
                                } else {
                                    hcf.setProid(utility.ObjectMapper().writeValueAsString(prolist));
                                    hcf.setMb(utility.ObjectMapper().writeValueAsString(mblist));
                                }
                            } else {
                                ACRGBWSResult methodsresult = new Methods().GETROLEREVERESE(dataSource, resultset.getString("HCFCODE"), tags.toUpperCase().trim());
                                if (methodsresult.isSuccess()) {
                                    ACRGBWSResult mgresult = new Methods().GETMBWITHID(dataSource, methodsresult.getResult());
                                    if (mgresult.isSuccess()) {
                                        ManagingBoard mb = utility.ObjectMapper().readValue(mgresult.getResult(), ManagingBoard.class);
                                        hcf.setMb(mb.getControlnumber());
                                        ACRGBWSResult restC = new Methods().GETROLEREVERESE(dataSource, mb.getControlnumber(), tags.toUpperCase().trim());
                                        if (restC.isSuccess()) {
                                            //GET PRO USING PROID
                                            ACRGBWSResult getproid = new Methods().GetProWithPROID(dataSource, restC.getResult());
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
                            }
                            hcf.setAmount("N/A");
                            hcf.setRemainingbalance("N/A");
                            hcflist.add(hcf);
                        }
                        //----------------------------------------------------------------------

                    }
                } else {
                    result.setMessage(restBB.getMessage());
                }
            } else {
                result.setMessage(restAA.getMessage());
            }

            if (hcflist.size() > 0) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(hcflist));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET USER INFO
    public ACRGBWSResult GETFULLDETAILS(
            final DataSource dataSource,
            final String userid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETFULLDETAILS(:userid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("userid", userid);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                UserInfo userinfo = new UserInfo();
                userinfo.setRole(resultset.getString("LEVNAME"));
                userinfo.setDid(resultset.getString("BDID"));
                userinfo.setFirstname(resultset.getString("FIRSTNAME"));
                userinfo.setLastname(resultset.getString("LASTNAME"));
                userinfo.setMiddlename(resultset.getString("MIDDLENAME"));
                userinfo.setDatecreated(dateformat.format(resultset.getTimestamp("DATECREATED")));//resultset.getString("DATECREATED"));
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(userinfo));
            } else {
                result.setMessage("N/A");
            }
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
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETUSERROLEINDEX(:puserid); end;");
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
                    userole.setCreatedby(creator.getMessage());
                }
                userole.setDatecreated(dateformat.format(resultset.getTimestamp("DATECREATED")));//resultset.getString("DATECREATED"));
                userole.setStatus(resultset.getString("STATUS"));
                userolelist.add(userole);
            }
            if (!userolelist.isEmpty()) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(userolelist));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    // ACR___ASSETS   BY CONTRACT ID
    public ACRGBWSResult ACR_ASSETS(final DataSource dataSource, final String tags, final String phcfid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :assets_type := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.ACR_ASSETS(:tags,:phcfid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("tags", tags.replaceAll("\\s", "").toUpperCase());
            statement.setString("phcfid", phcfid);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            ArrayList<Assets> listassets = new ArrayList<>();
            while (resultset.next()) {
                Assets assets = new Assets();
                assets.setAssetid(resultset.getString("ASSETSID"));
                ACRGBWSResult tranchresult = this.ACR_TRANCHWITHID(dataSource, resultset.getString("TRANCHID"));
                if (tranchresult.isSuccess()) {
                    assets.setTranchid(tranchresult.getResult());
                } else {
                    assets.setTranchid(tranchresult.getMessage());
                }
                ACRGBWSResult facilityresult = this.GETFACILITYID(dataSource, resultset.getString("HCFID"));
                if (facilityresult.isSuccess()) {
                    assets.setHcfid(facilityresult.getResult());
                } else {
                    ACRGBWSResult getHCPN =  new Methods().GETMBWITHID(dataSource, resultset.getString("HCFID"));
                    if (getHCPN.isSuccess()) {
                        assets.setHcfid(getHCPN.getResult());
                    } else {
                        assets.setHcfid(getHCPN.getMessage());
                    }
                }
                assets.setDatereleased(dateformat.format(resultset.getTimestamp("DATERELEASED")));//resultset.getDate("DATERELEASED"));
                assets.setReceipt(resultset.getString("RECEIPT"));
                assets.setReleasedamount(resultset.getString("RELEASEDAMOUNT"));
                assets.setPreviousbalance(resultset.getString("PREVIOUSBAL"));
                assets.setAmount(resultset.getString("AMOUNT"));
                ACRGBWSResult creator = this.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                if (creator.isSuccess()) {
                    UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class);
                    assets.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                } else {
                    assets.setCreatedby("N/A");
                }
                assets.setDatecreated(dateformat.format(resultset.getTimestamp("DATECREATED")));
                ACRGBWSResult getcon = this.GETCONTRACTCONID(dataSource, resultset.getString("CONID").trim(), tags.toUpperCase().trim());
                if (getcon.isSuccess()) {
                    assets.setConid(getcon.getResult());
                } else {
                    assets.setCreatedby("N/A");
                }
                assets.setStatus(resultset.getString("STATS"));
                assets.setClaimscount(resultset.getString("CLAIMSCOUNT"));
                listassets.add(assets);
            }
            if (!listassets.isEmpty()) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(listassets));
            } else {
                result.setMessage("NO DATA FOUND");
            }

        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

// GET CONTRACT USING PRO USERID
    public ACRGBWSResult ACR_CONTRACTPROID(
            final DataSource dataSource,
            final String tags,
            final String userid,
            final String ustate) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            ArrayList<Contract> contractlist = new ArrayList<>();
            ACRGBWSResult restA = new Methods().GETROLE(dataSource, userid, "ACTIVE");//GET (PROID) USING (USERID)
            if (restA.isSuccess()) {
                ACRGBWSResult restB = new Methods().GETROLEMULITPLE(dataSource, restA.getResult(), tags);//GET (NCPN) USING (PROID)          
                List<String> restist = Arrays.asList(restB.getResult().split(","));
                if (restB.isSuccess()) {
                    for (int b = 0; b < restist.size(); b++) {
                        //--------------------------------------------------------
                        ACRGBWSResult getcon = this.GetHCPNSingleContract(dataSource, tags, restist.get(b).trim(), ustate.trim().toUpperCase());
                        if (getcon.isSuccess()) {
                            Contract conresult = utility.ObjectMapper().readValue(getcon.getResult(), Contract.class);
                            contractlist.add(conresult);
                        }
                    }
                }
            }
            //---------------------------------------------------------------
            if (!contractlist.isEmpty()) {
                result.setMessage("OK");
                result.setResult(utility.ObjectMapper().writeValueAsString(contractlist));
                result.setSuccess(true);
            } else {
                result.setMessage("N/A");
            }
        } catch (IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET ALL HCPN CONTRACT
    //userid =0
    public ACRGBWSResult GETALLHCPNCONTRACT(
            final DataSource dataSource,
            final String tags,
            final String userid,
            final String ustate) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            ArrayList<Contract> contractlist = new ArrayList<>();
            ACRGBWSResult mblist = this.GetManagingBoard(dataSource, "ACTIVE");
            if (mblist.isSuccess()) {
                List<ManagingBoard> mblistresult = Arrays.asList(utility.ObjectMapper().readValue(mblist.getResult(), ManagingBoard[].class));
                for (int x = 0; x < mblistresult.size(); x++) {
                    ACRGBWSResult getmbcon = this.GetHCPNSingleContract(dataSource, tags.trim(), mblistresult.get(x).getControlnumber(), ustate.trim().toUpperCase());
                    if (getmbcon.isSuccess()) {
                        Contract contrac = utility.ObjectMapper().readValue(getmbcon.getResult(), Contract.class);
                        contractlist.add(contrac);
                    }
                }
            }
            if (!contractlist.isEmpty()) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(contractlist));
            } else {
                result.setMessage("N/A");
            }
        } catch (IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET CONTRACT MB USING MB USERID ACCOUNT
    //userid =0
    public ACRGBWSResult GETCONTRACTUNDERMB(final DataSource dataSource, final String utags, final String userid, final String ustate) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            ArrayList<Contract> contractlist = new ArrayList<>();
            ACRGBWSResult reatA = new Methods().GETROLE(dataSource, userid, "ACTIVE");
            if (reatA.isSuccess()) {
                CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETENDCONOPENCONSTATE(:utags,:uhcfcode,:ustate); end;");
                statement.registerOutParameter("v_result", OracleTypes.CURSOR);
                statement.setString("utags", utags.trim());
                statement.setString("uhcfcode", reatA.getResult().trim());
                statement.setString("ustate", ustate.trim());
                statement.execute();
                ResultSet resultset = (ResultSet) statement.getObject("v_result");
                if (resultset.next()) {
                    Contract contract = new Contract();
                    contract.setConid(resultset.getString("CONID"));
                    ACRGBWSResult facility = new Methods().GETMBWITHID(dataSource, resultset.getString("HCFID"));
                    if (facility.isSuccess()) {
                        ManagingBoard mb = utility.ObjectMapper().readValue(facility.getResult(), ManagingBoard.class);
                        contract.setHcfid(mb.getMbname());
                    } else {
                        contract.setHcfid(facility.getMessage());
                    }
                    //END OF GET NETWORK FULL DETAILS
                    contract.setAmount(resultset.getString("AMOUNT"));
                    contract.setStats(resultset.getString("STATS"));
                    ACRGBWSResult creator = this.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                    if (creator.isSuccess()) {
                        contract.setCreatedby(creator.getResult());
                    } else {
                        contract.setCreatedby(creator.getMessage());
                    }
                    contract.setDatecreated(dateformat.format(resultset.getTimestamp("DATECREATED")));
                    contract.setBaseamount(resultset.getString("BASEAMOUNT"));
                    contract.setComittedClaimsVol(resultset.getString("C_CLAIMSVOL"));
                    contract.setComputedClaimsVol(resultset.getString("T_CLAIMSVOL"));
                    contract.setSb(resultset.getString("SB"));
                    contract.setAddamount(resultset.getString("ADDAMOUNT"));
                    contract.setQuarter(resultset.getString("QUARTER"));
                    if (resultset.getString("ENDDATE") == null) {
                        contract.setEnddate("");
                    } else {
                        contract.setEnddate(dateformat.format(resultset.getTimestamp("ENDDATE")));
                    }
                    //=============================================
                    int numberofclaims = 0;
                    double totalclaimsamount = 0.00;
                    double percentageA = 0.00;
                    double percentageB = 0.00;
                    double trancheamount = 0.00;
                    double totalrecievedamount = 0.00;
                    int tranches = 0;
                    ACRGBWSResult getcondateA = new ContractMethod().GETCONDATEBYID(dataSource, resultset.getString("CONTRACTDATE"));
                    if (getcondateA.isSuccess()) {
                        contract.setContractdate(getcondateA.getResult());
                        ACRGBWSResult GetAccessRoleList = new Methods().GETROLEMULITPLE(dataSource, reatA.getResult(), utags);
                        List<String> hcflist = Arrays.asList(GetAccessRoleList.getResult().split(","));
                        ACRGBWSResult getIdType = new CurrentBalance().GETTRANCHBYTYPE(dataSource, "1STFINAL");
                        for (int y = 0; y < hcflist.size(); y++) {
                            ACRGBWSResult restAB = this.GETASSETBYIDANDCONID(dataSource, hcflist.get(y).trim(), resultset.getString("CONID"), utags.trim().toUpperCase());
                            if (restAB.isSuccess()) {
                                List<Assets> assetlist = Arrays.asList(utility.ObjectMapper().readValue(restAB.getResult(), Assets[].class));
                                for (int g = 0; g < assetlist.size(); g++) {
                                    if (assetlist.get(g).getPreviousbalance() != null) {
                                        Tranch tranch = utility.ObjectMapper().readValue(assetlist.get(g).getTranchid(), Tranch.class);
                                        switch (tranch.getTranchtype()) {
                                            case "1ST": {
                                                trancheamount += Double.parseDouble(assetlist.get(g).getPreviousbalance());
                                                trancheamount += Double.parseDouble(assetlist.get(g).getReleasedamount());
                                                tranches++;
                                                break;
                                            }
                                            case "1STFINAL": {
                                                trancheamount += Double.parseDouble(assetlist.get(g).getReleasedamount());
//                                                tranches--;
                                            }
                                            default: {
                                                trancheamount += Double.parseDouble(assetlist.get(g).getReleasedamount());
                                                tranches++;
                                                break;
                                            }
                                        }
                                    } else {
                                        trancheamount += Double.parseDouble(assetlist.get(g).getReleasedamount());
                                        tranches++;
                                    }
                                }
                            }
                            //GET CLAIMS AMOUNT OF FACILITY UNDER SELECTED NETWORK
                            if (getcondateA.isSuccess()) {
                                ContractDate condate = utility.ObjectMapper().readValue(getcondateA.getResult(), ContractDate.class);
//                                ACRGBWSResult GetFacilityContract = contractmethod.GETCONTRACT(dataSource, tags, hcflist.get(y).trim());
                                ACRGBWSResult GetFacilityContract = new ContractMethod().GETCONTRACTWITHOPENSTATE(dataSource, utags.trim().toUpperCase(), hcflist.get(y).trim(), ustate.trim().toUpperCase());
                                if (GetFacilityContract.isSuccess()) {
                                    //------------------------------------------------------------------- GET ALL PMCC NO UNDER SELECTED FACILITY
                                    ACRGBWSResult getHcfByCode = new GetHCFMultiplePMCCNO().GETFACILITYBYCODE(dataSource, hcflist.get(y).trim());
                                    if (getHcfByCode.isSuccess()) {
                                        HealthCareFacility healthCareFacility = utility.ObjectMapper().readValue(getHcfByCode.getResult(), HealthCareFacility.class);
                                        //GET HCF DETAILS BY NAME
                                        ACRGBWSResult getHcfByName = new GetHCFMultiplePMCCNO().GETFACILITYBYNAME(dataSource, healthCareFacility.getHcfname().trim(), healthCareFacility.getStreet().trim());
                                        if (getHcfByName.isSuccess()) {
                                            List<HealthCareFacility> healthCareFacilityList = Arrays.asList(utility.ObjectMapper().readValue(getHcfByName.getResult(), HealthCareFacility[].class));
                                            for (int yu = 0; yu < healthCareFacilityList.size(); yu++) {
                                                //------------------------------------------------------------------------ END OF GET ALL PMCC NO UNDER SELECTED FACILITY
                                                ACRGBWSResult sumresult = this.GETNCLAIMS(dataSource, healthCareFacilityList.get(yu).getHcfcode().trim(), "G",
                                                        condate.getDatefrom(), utility.AddMinusDaysDate(condate.getDateto(), "60"), "CURRENTSTATUS");
                                                if (sumresult.isSuccess()) {
                                                    Contract cons = utility.ObjectMapper().readValue(GetFacilityContract.getResult(), Contract.class);
                                                    List<NclaimsData> nclaimsdata = Arrays.asList(utility.ObjectMapper().readValue(sumresult.getResult(), NclaimsData[].class));
                                                    for (int i = 0; i < nclaimsdata.size(); i++) {
                                                        int ledgerCount = 0;
                                                        if (nclaimsdata.get(i).getRefiledate().isEmpty()) {
                                                            if (cons.getEnddate().isEmpty()) {
                                                                if (dateformat.parse(nclaimsdata.get(i).getDatesubmitted()).compareTo(dateformat.parse(utility.AddMinusDaysDate(condate.getDateto(), "60"))) <= 0) {
                                                                    ledgerCount++;
                                                                }
                                                            } else {
                                                                if (dateformat.parse(nclaimsdata.get(i).getDatesubmitted()).compareTo(dateformat.parse(utility.AddMinusDaysDate(cons.getEnddate().trim(), "60"))) <= 0) {
                                                                    ledgerCount++;
                                                                }
                                                            }
                                                        } else {
                                                            if (cons.getEnddate().isEmpty()) {
                                                                if (dateformat.parse(nclaimsdata.get(i).getRefiledate()).compareTo(dateformat.parse(utility.AddMinusDaysDate(condate.getDateto(), "60"))) <= 0) {
                                                                    ledgerCount++;
                                                                }
                                                            } else {
                                                                if (dateformat.parse(nclaimsdata.get(i).getRefiledate()).compareTo(dateformat.parse(utility.AddMinusDaysDate(cons.getEnddate().trim(), "60"))) <= 0) {
                                                                    ledgerCount++;
                                                                }
                                                            }
                                                        }

                                                        if (ledgerCount > 0) {
                                                            numberofclaims += Integer.parseInt(nclaimsdata.get(i).getTotalclaims());
                                                            totalclaimsamount += Double.parseDouble(nclaimsdata.get(i).getClaimamount());
                                                        }
                                                    }
                                                }
                                            }

                                        }
                                    }
                                }
                            }
                        }

                        ACRGBWSResult restAB = this.GETASSETBYIDANDCONID(dataSource, resultset.getString("HCFID").trim(), resultset.getString("CONID"), utags.trim().toUpperCase());
                        if (restAB.isSuccess()) {
                            if (getIdType.isSuccess()) {
                                Tranch tranch = utility.ObjectMapper().readValue(getIdType.getResult(), Tranch.class);
                                ACRGBWSResult totalResult = new Methods().GETSUMMARY(dataSource, utags.trim().toUpperCase(), resultset.getString("HCFID").trim(), tranch.getTranchid(), resultset.getString("CONID").trim());
                                if (totalResult.isSuccess()) {
                                    Total getResult = utility.ObjectMapper().readValue(totalResult.getResult(), Total.class);
                                    //   tranches += Integer.parseInt(getResult.getCcount());
                                    totalrecievedamount += Double.parseDouble(getResult.getCtotal());
                                }
                                //GET TRANCHE AMOUNT
                                ACRGBWSResult getTranchid = new CurrentBalance().GET1STFINAL(dataSource, utags.trim().toUpperCase(), resultset.getString("HCFID").trim(), tranch.getTranchid(), resultset.getString("CONID").trim());
                                if (getTranchid.isSuccess()) {
                                    Total getResult = utility.ObjectMapper().readValue(getTranchid.getResult(), Total.class);
                                    // tranches -= Integer.parseInt(getResult.getCcount());
                                    totalrecievedamount += Double.parseDouble(getResult.getCtotal());
                                }
                                List<Assets> assetlist = Arrays.asList(utility.ObjectMapper().readValue(restAB.getResult(), Assets[].class));
                                for (int g = 0; g < assetlist.size(); g++) {
                                    if (assetlist.get(g).getPreviousbalance() != null) {
                                        Tranch tranchs = utility.ObjectMapper().readValue(assetlist.get(g).getTranchid(), Tranch.class);
                                        switch (tranchs.getTranchtype()) {
                                            case "1ST": {
                                                totalrecievedamount += Double.parseDouble(assetlist.get(g).getPreviousbalance());
                                                totalrecievedamount += Double.parseDouble(assetlist.get(g).getReleasedamount());
                                                break;
                                            }
                                            case "1STFINAL": {
                                                totalrecievedamount += Double.parseDouble(assetlist.get(g).getReleasedamount());
                                                break;
                                            }
                                            default: {
                                                totalrecievedamount += Double.parseDouble(assetlist.get(g).getReleasedamount());
                                                break;
                                            }
                                        }
                                    } else {
                                        totalrecievedamount += Double.parseDouble(assetlist.get(g).getReleasedamount());
                                    }
                                }
                            }
                        }

                        double ConAmount = Double.parseDouble(resultset.getString("AMOUNT"));
                        double sums = trancheamount / ConAmount;
                        double sumsA = sums * 100;
                        if (sumsA > 100) {
                            double negvalue = 100 - sumsA;
                            percentageA += negvalue;
                        } else {
                            percentageA += sumsA;
                        }
                        //----------------
                        double sumsB = (totalclaimsamount / trancheamount) * 100;
                        if (sumsB > 100) {
                            double negvalue = 100 - sumsB;
                            percentageB += negvalue;
                        } else {
                            percentageB += sumsB;
                        }
//                        totalrecievedamount
                        contract.setTotalamountrecieved(String.valueOf(totalrecievedamount));
                        contract.setTotalclaims(String.valueOf(numberofclaims));
                        contract.setTraches(String.valueOf(tranches));
                        contract.setTotaltrancheamount(String.valueOf(trancheamount));
                        contract.setTotalclaimsamount(String.valueOf(totalclaimsamount));
                        contract.setPercentage(String.valueOf(percentageA));
                        contract.setTotalclaimspercentage(String.valueOf(percentageB));
                    }
                    contractlist.add(contract);
                }

            } else {
                result.setMessage("N/A");
            }
            if (!contractlist.isEmpty()) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(contractlist));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET CONTRACT MB USING MB USERID ACCOUNT
    public ACRGBWSResult GETCONTRACTCONID(final DataSource dataSource,
            final String pconid,
            final String utags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            //--------------------------------------------------------
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETCONTRACTCONID(:pconid,:utags); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("pconid", pconid.trim());
            statement.setString("utags", utags.trim().toUpperCase());
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
                    contract.setCreatedby(creator.getResult());
                } else {
                    contract.setCreatedby(creator.getMessage());
                }
                contract.setDatecreated(dateformat.format(resultset.getTimestamp("DATECREATED")));//resultset.getString("DATECREATED"));
                ACRGBWSResult getcondateA = new ContractMethod().GETCONDATEBYID(dataSource, resultset.getString("CONTRACTDATE"));
                if (getcondateA.isSuccess()) {
                    contract.setContractdate(getcondateA.getResult());
                }
                contract.setTranscode(resultset.getString("TRANSCODE"));
                contract.setBaseamount(resultset.getString("BASEAMOUNT"));
                contract.setComittedClaimsVol(resultset.getString("C_CLAIMSVOL"));
                contract.setComputedClaimsVol(resultset.getString("T_CLAIMSVOL"));
                contract.setSb(resultset.getString("SB"));
                contract.setAddamount(resultset.getString("ADDAMOUNT"));
                contract.setQuarter(resultset.getString("QUARTER"));
                if (resultset.getTimestamp("ENDDATE") != null) {
                    contract.setEnddate(dateformat.format(resultset.getTimestamp("ENDDATE")));
                } else {
                    contract.setEnddate("");
                }
                result.setMessage("OK");
                result.setResult(utility.ObjectMapper().writeValueAsString(contract));
                result.setSuccess(true);
            } else {
                result.setMessage("N/A");
            }

        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET CONTRACT USING HCPN ACCOUNT USERID
    public ACRGBWSResult GetFacilityContractUsingHCPNAccountUserID(final DataSource dataSource,
            final String utags,
            final String uhcfcode,
            final String ustate) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            ArrayList<Contract> contractlist = new ArrayList<>();
            ACRGBWSResult restAA = new Methods().GETROLE(dataSource, uhcfcode.trim(), "ACTIVE");
            if (restAA.isSuccess()) {
                ACRGBWSResult restA = new Methods().GETROLEMULITPLE(dataSource, restAA.getResult(), utags.trim().toUpperCase());
                List<String> hcflist = Arrays.asList(restA.getResult().split(","));
                //-------------- END OF GET APEX FACILITY
                for (int y = 0; y < hcflist.size(); y++) {
                    CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETENDCONOPENCONSTATE(:utags,:uhcfcode,:ustate); end;");
                    statement.registerOutParameter("v_result", OracleTypes.CURSOR);
                    statement.setString("utags", utags.trim());
                    statement.setString("uhcfcode", hcflist.get(y).trim());
                    statement.setString("ustate", ustate.trim());
                    statement.execute();
                    ResultSet resultset = (ResultSet) statement.getObject("v_result");
                    if (resultset.next()) {
                        Contract contract = new Contract();
                        contract.setConid(resultset.getString("CONID"));
                        ACRGBWSResult facility = this.GETFACILITYID(dataSource, resultset.getString("HCFID").trim());
                        if (facility.isSuccess()) {
                            contract.setHcfid(facility.getResult());
                        } else {
                            contract.setHcfid(facility.getMessage());
                        }
                        //END OF GET NETWORK FULL DETAILS
                        contract.setAmount(resultset.getString("AMOUNT"));
                        contract.setStats(resultset.getString("STATS"));
                        ACRGBWSResult creator = this.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                        if (creator.isSuccess()) {
                            contract.setCreatedby(creator.getResult());
                        } else {
                            contract.setCreatedby(creator.getMessage());
                        }
                        contract.setDatecreated(dateformat.format(resultset.getTimestamp("DATECREATED")));//resultset.getString("DATECREATED"));
                        ACRGBWSResult getcondateA = new ContractMethod().GETCONDATEBYID(dataSource, resultset.getString("CONTRACTDATE"));
                        if (getcondateA.isSuccess()) {
                            contract.setContractdate(getcondateA.getResult());
                            ContractDate condate = utility.ObjectMapper().readValue(getcondateA.getResult(), ContractDate.class);
                            contract.setTranscode(resultset.getString("TRANSCODE"));
                            contract.setBaseamount(resultset.getString("BASEAMOUNT"));
                            contract.setComittedClaimsVol(resultset.getString("C_CLAIMSVOL"));
                            contract.setComputedClaimsVol(resultset.getString("T_CLAIMSVOL"));
                            contract.setSb(resultset.getString("SB"));
                            contract.setAddamount(resultset.getString("ADDAMOUNT")); //=============================================
                            contract.setQuarter(resultset.getString("QUARTER"));
                            if (resultset.getString("ENDDATE") == null) {
                                contract.setEnddate("");
                            } else {
                                contract.setEnddate(dateformat.format(resultset.getTimestamp("ENDDATE")));
                            }
                            int numberofclaims = 0;
                            double totalclaimsamount = 0.00;
                            double percentageA = 0.00;
                            double percentageB = 0.00;
                            double trancheamount = 0.00;
                            int tranches = 0;
                            ACRGBWSResult restAB = this.GETASSETBYIDANDCONID(dataSource, resultset.getString("HCFID").trim(), resultset.getString("CONID").trim(), utags.trim().toUpperCase());
                            if (restAB.isSuccess()) {
                                List<Assets> assetlist = Arrays.asList(utility.ObjectMapper().readValue(restAB.getResult(), Assets[].class));
                                for (int g = 0; g < assetlist.size(); g++) {
                                    if (assetlist.get(g).getPreviousbalance() != null) {
                                        Tranch tranch = utility.ObjectMapper().readValue(assetlist.get(g).getTranchid(), Tranch.class);
                                        switch (tranch.getTranchtype()) {
                                            case "1ST": {
                                                trancheamount += Double.parseDouble(assetlist.get(g).getPreviousbalance());
                                                trancheamount += Double.parseDouble(assetlist.get(g).getReleasedamount());
                                                tranches++;
                                                break;
                                            }
                                            case "1STFINAL": {
                                                trancheamount += Double.parseDouble(assetlist.get(g).getReleasedamount());
//                                                tranches--;
                                                break;
                                            }
                                            default: {
                                                trancheamount += Double.parseDouble(assetlist.get(g).getReleasedamount());
                                                tranches++;
                                                break;
                                            }
                                        }
                                    } else {
                                        trancheamount += Double.parseDouble(assetlist.get(g).getReleasedamount());
                                        tranches++;
                                    }
                                }
                            }

                            //-------------------------------------------------------------------
                            ACRGBWSResult getHcfByCode = new GetHCFMultiplePMCCNO().GETFACILITYBYCODE(dataSource, resultset.getString("HCFID").trim());
                            if (getHcfByCode.isSuccess()) {
                                HealthCareFacility healthCareFacility = utility.ObjectMapper().readValue(getHcfByCode.getResult(), HealthCareFacility.class);
                                //GET HCF DETAILS BY NAME
                                ACRGBWSResult getHcfByName = new GetHCFMultiplePMCCNO().GETFACILITYBYNAME(dataSource, healthCareFacility.getHcfname().trim(), healthCareFacility.getStreet().trim());
                                if (getHcfByName.isSuccess()) {
                                    List<HealthCareFacility> healthCareFacilityList = Arrays.asList(utility.ObjectMapper().readValue(getHcfByName.getResult(), HealthCareFacility[].class));
                                    for (int yu = 0; yu < healthCareFacilityList.size(); yu++) {
                                        //------------------------------------------------------------------------
                                        ACRGBWSResult sumresult = this.GETNCLAIMS(dataSource, healthCareFacilityList.get(yu).getHcfcode().trim(), "G",
                                                condate.getDatefrom(), utility.AddMinusDaysDate(condate.getDateto(), "60"), "CURRENTSTATUS");
                                        if (sumresult.isSuccess()) {
                                            List<NclaimsData> nclaimsdata = Arrays.asList(utility.ObjectMapper().readValue(sumresult.getResult(), NclaimsData[].class));
                                            for (int i = 0; i < nclaimsdata.size(); i++) {
                                                int countLedger = 0;  //dateformat.format(resultset.getTimestamp("ENDDATE"))
                                                if (nclaimsdata.get(i).getRefiledate().isEmpty()) {
                                                    if (resultset.getString("ENDDATE") == null) {
                                                        if (dateformat.parse(nclaimsdata.get(i).getDatesubmitted()).compareTo(dateformat.parse(utility.AddMinusDaysDate(condate.getDateto().trim(), "60"))) <= 0) {
                                                            countLedger++;
                                                        }
                                                    } else {
                                                        if (dateformat.parse(nclaimsdata.get(i).getDatesubmitted()).compareTo(dateformat.parse(utility.AddMinusDaysDate(dateformat.format(resultset.getTimestamp("ENDDATE")), "60"))) <= 0) {
                                                            countLedger++;
                                                        }
                                                    }
                                                } else {
                                                    if (resultset.getString("ENDDATE") == null) {
                                                        if (dateformat.parse(nclaimsdata.get(i).getRefiledate()).compareTo(dateformat.parse(utility.AddMinusDaysDate(condate.getDateto().trim(), "60"))) <= 0) {
                                                            countLedger++;
                                                        }
                                                    } else {
                                                        if (dateformat.parse(nclaimsdata.get(i).getRefiledate()).compareTo(dateformat.parse(utility.AddMinusDaysDate(dateformat.format(resultset.getTimestamp("ENDDATE")), "60"))) <= 0) {
                                                            countLedger++;
                                                        }
                                                    }
                                                }
                                                if (countLedger > 0) {
                                                    numberofclaims += Integer.parseInt(nclaimsdata.get(i).getTotalclaims());
                                                    totalclaimsamount += Double.parseDouble(nclaimsdata.get(i).getClaimamount());
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            double sumsA = (trancheamount / Double.parseDouble(resultset.getString("AMOUNT"))) * 100;
                            if (sumsA > 100) {
                                double negvalue = 100 - sumsA;
                                percentageA += negvalue;
                            } else {
                                percentageA += sumsA;
                            }
                            //----------------
                            double sumsB = (totalclaimsamount / trancheamount) * 100;
                            if (sumsB > 100) {
                                double negvalue = 100 - sumsB;
                                percentageB += negvalue;
                            } else {
                                percentageB += sumsB;
                            }
                            contract.setTotalamountrecieved(String.valueOf(trancheamount));
                            contract.setTotalclaims(String.valueOf(numberofclaims));
                            contract.setTraches(String.valueOf(tranches));
                            contract.setTotaltrancheamount(String.valueOf(trancheamount));
                            contract.setTotalclaimsamount(String.valueOf(totalclaimsamount));
                            contract.setPercentage(String.valueOf(percentageA));
                            contract.setTotalclaimspercentage(String.valueOf(percentageB));
                        }
                        contractlist.add(contract);
                    }
                }
            }
            if (!contractlist.isEmpty()) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(contractlist));
            } else {
                result.setMessage("N/A");
            }

        } catch (SQLException | IOException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

//ACR_HCF
    public ACRGBWSResult ACR_HCF(final DataSource dataSource) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKG.ACR_HCF(); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            ArrayList<HealthCareFacility> listHCF = new ArrayList<>();
            while (resultset.next()) {
                if (!new Methods().GETROLEREVERESE(dataSource, resultset.getString("HCFCODE").trim(), "ACTIVE").isSuccess()) {
                    HealthCareFacility hcf = new HealthCareFacility();
                    hcf.setHcfname(resultset.getString("HCFNAME"));
                    hcf.setHcfaddress(resultset.getString("HCFADDRESS"));
                    hcf.setHcfcode(resultset.getString("HCFCODE"));
                    hcf.setType(resultset.getString("HCFTYPE"));
                    hcf.setHcilevel(resultset.getString("HCILEVEL"));
                    listHCF.add(hcf);
                }
            }
            if (listHCF.size() > 0) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(listHCF));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
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
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.ACR_TRANCH(:tags); end;");
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
                    UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class);
                    tranch.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                } else {
                    tranch.setCreatedby(creator.getMessage());
                }
                tranch.setStats(resultset.getString("STATS"));
                tranch.setDatecreated(dateformat.format(resultset.getTimestamp("DATECREATED")));//resultset.getString("DATECREATED"));
                listtranch.add(tranch);
            }

            if (!listtranch.isEmpty()) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(listtranch));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    // THIS AREA IS FOR USER DETAILS 
    public ACRGBWSResult ACR_USER_DETAILS(final DataSource dataSource, final String tags, final String pdid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.ACR_USER_DETAILS(:tags,:pdid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("tags", tags.replaceAll("\\s", "").toUpperCase());
            statement.setString("pdid", pdid.trim());
            statement.execute();
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            ArrayList<UserInfo> listuserinfo = new ArrayList<>();
            while (resultset.next()) {
                UserInfo userinfo = new UserInfo();
                userinfo.setDid(resultset.getString("DID"));
                userinfo.setFirstname(resultset.getString("FIRSTNAME"));
                userinfo.setEmail(resultset.getString("EMAIL"));
                userinfo.setContact(resultset.getString("CONTACT"));
                userinfo.setLastname(resultset.getString("LASTNAME"));
                userinfo.setMiddlename(resultset.getString("MIDDLENAME"));
                userinfo.setDatecreated(dateformat.format(resultset.getTimestamp("DATECREATED")));//resultset.getString("DATECREATED"));
                ACRGBWSResult creator = this.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                if (creator.isSuccess()) {
                    UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class);
                    userinfo.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                } else {
                    userinfo.setCreatedby(creator.getMessage());
                }
                userinfo.setStats(resultset.getString("STATS"));
                listuserinfo.add(userinfo);
            }
            if (!listuserinfo.isEmpty()) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(listuserinfo));
            } else {
                result.setMessage("N/A");
            }

        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
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
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.ACR_USER_LEVEL(:tags); end;");
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
                    UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class);
                    userlevel.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                } else {
                    userlevel.setCreatedby(creator.getMessage());
                }
                userlevel.setDatecreated(dateformat.format(resultset.getTimestamp("DATECREATED")));//resultset.getString("DATECREATED"));
                userlevel.setStats(resultset.getString("STATS"));
                listuserlevel.add(userlevel);
            }
            if (!listuserlevel.isEmpty()) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(listuserlevel));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
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
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKG.ACR_PRO(); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            ArrayList<Pro> prolist = new ArrayList<>();
            while (resultset.next()) {
                Pro pro = new Pro();
                pro.setProname(resultset.getString("PRONAME"));
                pro.setProaddress(resultset.getString("PROADDRESS"));
                pro.setProcode("2024" + resultset.getString("PROCODE").trim());
                //======================================================
                // double baseamount = 0.00;
                double tranchamount = 0.00;
                double contractamount = 0.00;
                int totalnumberofclaims = 0;
                double totalclaimsamount = 0.00;
                int totalnumberoftranhce = 0;

                ACRGBWSResult getProFund = new ContractMethod().GETCONBYCODEOFPRO(dataSource, "2024" + resultset.getString("PROCODE").trim());
                if (getProFund.isSuccess()) {
                    List<Contract> contractlist = Arrays.asList(utility.ObjectMapper().readValue(getProFund.getResult(), Contract[].class));
                    for (int conlist = 0; conlist < contractlist.size(); conlist++) {
                        contractamount += Double.parseDouble(contractlist.get(conlist).getAmount());
                    }
                }
                //=========================================================
                ACRGBWSResult restA = new Methods().GETROLEMULITPLE(dataSource, "2024" + resultset.getString("PROCODE").trim(), tags);
                List<String> hcpncodeList = Arrays.asList(restA.getResult().split(","));
                for (int x = 0; x < hcpncodeList.size(); x++) {
                    ACRGBWSResult getProbudget = this.GETCONBYCODE(dataSource, hcpncodeList.get(x).trim());
                    if (getProbudget.isSuccess()) {
                        Contract con = utility.ObjectMapper().readValue(getProbudget.getResult(), Contract.class);
                        //GET TRANCH PER NETWORK
                        ACRGBWSResult tranche = this.GETASSETSBYCONID(dataSource, con.getConid());
                        if (tranche.isSuccess()) {
                            //   List<Assets> assetsList = Arrays.asList(utility.ObjectMapper().readValue(tranche.getResult(), Assets[].class));
                            //   for (int p = 0; p < assetsList.size(); p++) {
                            ACRGBWSResult getIdType = new CurrentBalance().GETTRANCHBYTYPE(dataSource, "1STFINAL");
                            if (getIdType.isSuccess()) {
                                Tranch tranch = utility.ObjectMapper().readValue(getIdType.getResult(), Tranch.class);
                                ACRGBWSResult totalResult = new Methods().GETSUMMARY(dataSource, tags.trim().toUpperCase(), con.getHcfid().trim(), tranch.getTranchid(), con.getConid());
                                if (totalResult.isSuccess()) {
                                    Total getResult = utility.ObjectMapper().readValue(totalResult.getResult(), Total.class);
                                    totalnumberoftranhce += Integer.parseInt(getResult.getCcount());
                                    tranchamount += Double.parseDouble(getResult.getCtotal());
                                }
                                //GET TRANCHE AMOUNT
                                ACRGBWSResult getTranchid = new CurrentBalance().GET1STFINAL(dataSource, tags.trim().toUpperCase(), con.getHcfid().trim(), tranch.getTranchid(), con.getConid().trim());
                                if (getTranchid.isSuccess()) {
                                    Total getResult = utility.ObjectMapper().readValue(getTranchid.getResult(), Total.class);
                                    totalnumberoftranhce -= Integer.parseInt(getResult.getCcount());
                                    tranchamount += Double.parseDouble(getResult.getCtotal());
                                }
                            }
                            //  }

//                            for (int p = 0; p < assetsList.size(); p++) {
//                                tranchamount += Double.parseDouble(assetsList.get(p).getAmount());
//                            }
//                            totalnumberoftranhce += assetsList.size();
                        }
                    }

                    //GET FACLITY UNDER SELECT HCPN
                    ACRGBWSResult GetFacility = new Methods().GETROLEMULITPLE(dataSource, hcpncodeList.get(x).trim(), tags);
                    if (GetFacility.isSuccess()) {
                        List<String> hcflist = Arrays.asList(GetFacility.getResult().split(","));
                        for (int y = 0; y < hcflist.size(); y++) {
                            if (getProbudget.isSuccess()) {
                                Contract con = utility.ObjectMapper().readValue(getProbudget.getResult(), Contract.class);
                                if (con.getContractdate() != null) {
                                    ContractDate condate = utility.ObjectMapper().readValue(con.getContractdate(), ContractDate.class);
                                    ACRGBWSResult sumresult = this.GETNCLAIMS(dataSource, hcflist.get(y).trim(), "G",
                                            condate.getDatefrom(), utility.AddMinusDaysDate(condate.getDateto().trim(), "60"), "CURRENTSTATUS");
                                    if (sumresult.isSuccess()) {
                                        List<NclaimsData> nclaimsdata = Arrays.asList(utility.ObjectMapper().readValue(sumresult.getResult(), NclaimsData[].class));
                                        for (int i = 0; i < nclaimsdata.size(); i++) {
                                            if (nclaimsdata.get(i).getRefiledate().isEmpty()) {
                                                if (dateformat.parse(nclaimsdata.get(i).getDatesubmitted()).compareTo(dateformat.parse(utility.AddMinusDaysDate(condate.getDateto(), "60"))) <= 0) {
                                                    totalnumberofclaims += Integer.parseInt(nclaimsdata.get(i).getTotalclaims());
                                                    totalclaimsamount += Double.parseDouble(nclaimsdata.get(i).getClaimamount());
                                                }
                                            } else {
                                                if (dateformat.parse(nclaimsdata.get(i).getRefiledate()).compareTo(dateformat.parse(utility.AddMinusDaysDate(condate.getDateto(), "60"))) <= 0) {
                                                    totalnumberofclaims += Integer.parseInt(nclaimsdata.get(i).getTotalclaims());
                                                    totalclaimsamount += Double.parseDouble(nclaimsdata.get(i).getClaimamount());
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                //===============================================
                double amountottranch = (tranchamount / contractamount) * 100;
                double tranchclaimamount = (totalclaimsamount / tranchamount) * 100;
                pro.setUtilize(String.valueOf(tranchamount));
                pro.setUnutilize(String.valueOf(contractamount - totalclaimsamount));
                pro.setPercentage(String.valueOf(amountottranch));
                pro.setClaimspercentage(String.valueOf(tranchclaimamount));
                pro.setContractamount(String.valueOf(contractamount));
                pro.setTotaltranhce(String.valueOf(totalnumberoftranhce));
                pro.setClaimsamount(String.valueOf(totalclaimsamount));
                pro.setNumberofclaims(String.valueOf(totalnumberofclaims));
                pro.setTotaltranhceamount(String.valueOf(tranchamount));
                pro.setAmounttranchper(String.valueOf(amountottranch));
                pro.setTranchclaimsamountper(String.valueOf(tranchclaimamount));
                pro.setTotalpercentage(String.valueOf((totalclaimsamount / contractamount) * 100));
                prolist.add(pro);
            }
            if (!prolist.isEmpty()) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(prolist));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult ACR_USER(
            final DataSource dataSource,
            final String tags,
            final String pid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.ACR_USER(:tags,:pid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("tags", tags.replaceAll("\\s", "").toUpperCase());
            statement.setString("pid", pid.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            ArrayList<User> listuser = new ArrayList<>();
            while (resultset.next()) {
                User user = new User();
                user.setUserid(resultset.getString("USERID"));
                ACRGBWSResult levelresult = this.GETUSERLEVEL(dataSource, resultset.getString("LEVELID").trim());
                if (levelresult.isSuccess()) {
                    user.setLeveid(levelresult.getResult());
                    user.setLeveldetails(levelresult.getMessage());
                } else {
                    user.setLeveldetails(levelresult.getMessage());
                    user.setLeveid(levelresult.getMessage());
                }
                user.setUsername(resultset.getString("USERNAME"));
                if (resultset.getString("STATS").equals("2")) {
                    user.setUserpassword(resultset.getString("USERPASSWORD"));
                } else {
                    user.setUserpassword(resultset.getString("USERPASSWORD"));
                }
                user.setDatecreated(dateformat.format(resultset.getTimestamp("DATECREATED")));//resultset.getString("DATECREATED"));
                user.setStatus(resultset.getString("STATS"));
                user.setDid(resultset.getString("DID"));
                ACRGBWSResult creator = this.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                if (creator.isSuccess()) {
                    UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class);
                    user.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                } else {
                    user.setCreatedby(creator.getMessage());
                }
                listuser.add(user);
            }
            if (!listuser.isEmpty()) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(listuser));
            } else {
                result.setMessage("NO AVAILABLE DATA");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET TRANCH AMOUNT PERCENTAGE
    public ACRGBWSResult GETTRANCHAMOUNT(
            final DataSource dataSource,
            final String p_tranchid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETTRANCHAMOUNT(:p_tranchid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("p_tranchid", p_tranchid.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(resultset.getString("PERCENTAGE"));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET CONTRACT AMOUNT
    public ACRGBWSResult GETCONTRACTAMOUNT(
            final DataSource dataSource,
            final String p_conid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETCONTRACTAMOUNT(:p_conid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("p_conid", p_conid.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(resultset.getString("AMOUNT"));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET USER LEVEL
    public ACRGBWSResult GETUSERLEVEL(
            final DataSource dataSource,
            final String levid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETLEVEL(:levid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("levid", levid.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                result.setMessage(resultset.getString("LEVDETAILS"));
                result.setSuccess(true);
                result.setResult(resultset.getString("LEVNAME"));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET ASSETS WITH PARAMETER
    public ACRGBWSResult GETASSETSWITHPARAM(
            final DataSource dataSource,
            final String phcfid,
            final String conid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETASSETSBYHCFID(:phcfid,:pconid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("phcfid", phcfid.trim());
            statement.setString("pconid", conid.trim());
            statement.execute();
            ArrayList<Assets> assetslist = new ArrayList<>();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            while (resultset.next()) {
                Assets assets = new Assets();
                assets.setAssetid(resultset.getString("ASSETSID"));
                assets.setHcfid(resultset.getString("HCFID"));
                assets.setTranchid(resultset.getString("TRANCHID"));
                assets.setReceipt(resultset.getString("RECEIPT"));
                assets.setAmount(resultset.getString("AMOUNT"));
                assets.setReleasedamount(resultset.getString("RELEASEDAMOUNT"));
                assets.setPreviousbalance(resultset.getString("PREVIOUSBAL"));
                ACRGBWSResult creator = this.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                if (creator.isSuccess()) {
                    UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class);
                    assets.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                } else {
                    assets.setCreatedby(creator.getMessage());
                }
                assets.setClaimscount(resultset.getString("CLAIMSCOUNT"));
                assets.setDatereleased(dateformat.format(resultset.getTimestamp("DATERELEASED")));//resultset.getString("DATERELEASED"));
                assets.setDatecreated(dateformat.format(resultset.getTimestamp("DATECREATED")));//resultset.getString("DATECREATED"));
                assetslist.add(assets);
            }
            if (assetslist.size() > 0) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(assetslist));
            } else {
                result.setMessage("N/A");
            }

        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET ACTIVITY LOGS
    public ACRGBWSResult ACRACTIVTYLOGS(
            final DataSource dataSource) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.ACRACTIVTYLOGS(); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            ArrayList<UserActivity> logslist = new ArrayList<>();
            while (resultset.next()) {
                UserActivity useractivity = new UserActivity();
                useractivity.setActid(resultset.getString("ACTID"));
                useractivity.setActdate(datetimeformat.format(resultset.getTimestamp("ACTDATE")));
                useractivity.setActdetails(resultset.getString("ACTDETAILS"));
                useractivity.setActstatus(resultset.getString("ACTSTATS"));
                ACRGBWSResult creator = this.GETFULLDETAILS(dataSource, resultset.getString("ACTBY").trim());
                if (creator.isSuccess()) {
                    UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class);
                    useractivity.setActby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                } else {
                    useractivity.setActby(creator.getMessage());
                }
                ACRGBWSResult levelname = this.GETUSERBYID(dataSource, resultset.getString("ACTBY").trim());
                if (levelname.isSuccess()) {
                    useractivity.setUserlevel(levelname.getResult());
                } else {
                    useractivity.setUserlevel(levelname.getMessage());
                }
                logslist.add(useractivity);
            }
            if (logslist.size() > 0) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(logslist));
            } else {
                result.setMessage("N/A");
            }

        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET NCLAIMS DATA AND AMOUNT OF ITS CLAIMS TOTAL
    public ACRGBWSResult GETNCLAIMS(
            final DataSource dataSource,
            final String upmccno,
            final String u_tags,
            final String u_from,
            final String u_to,
            final String reqtype) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            ArrayList<NclaimsData> claimsList = new ArrayList<>();
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKG.GETNCLAIMS(:upmccno,:u_tags,:u_from,:u_to); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("upmccno", upmccno.trim());
            statement.setString("u_tags", u_tags.trim().toUpperCase());
            if (reqtype.toUpperCase().trim().equals("CURRENTSTATUS")) {
                statement.setDate("u_from", (Date) new Date(utility.StringToDate(u_from).getTime()));
                statement.setDate("u_to", (Date) new Date(utility.StringToDate(u_to).getTime()));
                statement.execute();
                ResultSet resultset = (ResultSet) statement.getObject("v_result");
                while (resultset.next()) {
                    NclaimsData nclaimsdata = new NclaimsData();
                    nclaimsdata.setAccreno(resultset.getString("PMCC_NO"));
                    nclaimsdata.setClaimamount(resultset.getString("CTOTAL"));
                    nclaimsdata.setTotalclaims(resultset.getString("COUNTVAL"));
                    if (resultset.getTimestamp("DATESUB") != null) {
                        nclaimsdata.setDatesubmitted(dateformat.format(resultset.getTimestamp("DATESUB")));
                    } else {
                        nclaimsdata.setDatesubmitted("");
                    }
                    if (resultset.getTimestamp("DATEREFILE") != null) {
                        nclaimsdata.setRefiledate(dateformat.format(resultset.getTimestamp("DATEREFILE")));
                    } else {
                        nclaimsdata.setRefiledate("");
                    }
                    if (resultset.getTimestamp("DATEADM") != null) {
                        nclaimsdata.setDateadmission(dateformat.format(resultset.getTimestamp("DATEADM")));
                    } else {
                        nclaimsdata.setDateadmission("");
                    }
                    claimsList.add(nclaimsdata);
                }
            } else if (reqtype.toUpperCase().trim().equals("HISTORICALSTATUS")) {
                ACRGBWSResult GetDateRange = utility.ProcessDateAmountComputation(u_from, u_to);
                if (GetDateRange.isSuccess()) {
                    List<DateSettings> dateSettings = Arrays.asList(utility.ObjectMapper().readValue(GetDateRange.getResult(), DateSettings[].class));
                    for (int i = 0; i < dateSettings.size(); i++) {
                        statement.setDate("u_from", (Date) new Date(utility.StringToDate(dateSettings.get(i).getDatefrom()).getTime()));
                        statement.setDate("u_to", (Date) new Date(utility.StringToDate(dateSettings.get(i).getDateto()).getTime()));
                        statement.execute();
                        ResultSet resultset = (ResultSet) statement.getObject("v_result");
                        while (resultset.next()) {
                            NclaimsData nclaimsdata = new NclaimsData();
                            nclaimsdata.setPmccno(resultset.getString("PMCC_NO"));
                            nclaimsdata.setClaimamount(resultset.getString("CTOTAL"));
                            nclaimsdata.setTotalclaims(resultset.getString("COUNTVAL"));
                            if (resultset.getTimestamp("DATESUB") != null) {
                                nclaimsdata.setDatesubmitted(dateformat.format(resultset.getTimestamp("DATESUB")));
                            } else {
                                nclaimsdata.setDatesubmitted("");
                            }
                            if (resultset.getTimestamp("DATEREFILE") != null) {
                                nclaimsdata.setRefiledate(dateformat.format(resultset.getTimestamp("DATEREFILE")));
                            } else {
                                nclaimsdata.setRefiledate("");
                            }
                            if (resultset.getTimestamp("DATEADM") != null) {
                                nclaimsdata.setDateadmission(dateformat.format(resultset.getTimestamp("DATEADM")));
                            } else {
                                nclaimsdata.setDateadmission("");
                            }
                            claimsList.add(nclaimsdata);
                        }
                    }
                }
            }
            if (claimsList.size() > 0) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(claimsList));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET MANAGING BOARD ALL
    public ACRGBWSResult GetManagingBoard(
            final DataSource dataSource,
            final String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            ArrayList<ManagingBoard> mblist = new ArrayList<>();
            //-------------------------------------------------------
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETMB(:tags); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("tags", tags.toUpperCase().trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            while (resultset.next()) {
                ManagingBoard mb = new ManagingBoard();
                mb.setMbid(resultset.getString("MBID"));
                mb.setMbname(resultset.getString("MBNAME"));
                mb.setAddress(resultset.getString("ADDRESS"));
                mb.setBankaccount(resultset.getString("BANKACCOUNT"));
                mb.setBankname(resultset.getString("BANKNAME"));
                mb.setRemarks(resultset.getString("REMARKS"));
                mb.setDatecreated(dateformat.format(resultset.getTimestamp("DATECREATED")));
                ACRGBWSResult creator = this.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                if (creator.isSuccess()) {
                    UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class);
                    mb.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                } else {
                    mb.setCreatedby(creator.getMessage());
                }
                mb.setControlnumber(resultset.getString("CONNUMBER"));
                mb.setStatus(resultset.getString("STATUS"));
                ACRGBWSResult accreResult = this.GETACCREDITATION(dataSource, resultset.getString("CONNUMBER"));
                if (accreResult.isSuccess()) {
                    Accreditation accree = utility.ObjectMapper().readValue(accreResult.getResult(), Accreditation.class);
                    mb.setLicensedatefrom(accree.getDatefrom());
                    mb.setLicensedateto(accree.getDateto());
                } else {
                    mb.setLicensedatefrom(accreResult.getMessage());
                    mb.setLicensedateto(accreResult.getMessage());
                }
                ACRGBWSResult getPro = new Methods().GETROLEREVERESEMULTIPLE(dataSource, resultset.getString("CONNUMBER"), tags);
                if (getPro.isSuccess()) {
                    // GetProWithPROID
                    List<String> accesslist = Arrays.asList(getPro.getResult().split(","));
                    for (int u = 0; u < accesslist.size(); u++) {
                        ACRGBWSResult getProValue = new Methods().GetProWithPROID(dataSource, accesslist.get(u));
                        if (getProValue.isSuccess()) {
                            Pro pro = utility.ObjectMapper().readValue(getProValue.getResult(), Pro.class);
                            mb.setPro(pro.getProname());
                        }
                    }
                } else {
                    mb.setPro(getPro.getMessage());
                }
                mblist.add(mb);
            }
            //-------------------------------------------------------
            if (mblist.size() > 0) {
                result.setResult(utility.ObjectMapper().writeValueAsString(mblist));
                result.setMessage("OK");
                result.setSuccess(true);
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult GETROLETWOPARAM(
            final DataSource dataSource,
            final String puserid,
            final String paccessid,
            final String utags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETROLETWOPARAM(:utags,:puserid,:paccessid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("utags", utags.trim());
            statement.setString("puserid", puserid.trim());
            statement.setString("paccessid", paccessid.trim());
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
                result.setMessage("N/A");
            }

        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET TRANCH USING TRANCHID
    public ACRGBWSResult ACR_TRANCHWITHID(
            final DataSource dataSource,
            final String ptranchid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.ACR_TRANCHWITHID(:ptranchid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("ptranchid", ptranchid.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                Tranch tr = new Tranch();
                tr.setTranchtype(resultset.getString("TRANCHTYPE"));
                tr.setTranchid(resultset.getString("TRANCHID"));
                tr.setPercentage(resultset.getString("PERCENTAGE"));
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(tr));
            } else {
                result.setMessage("N/A");
            }

        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET SINGLE CONTRACT 
    public ACRGBWSResult GetHCPNSingleContract(
            final DataSource dataSource,
            final String utags,
            final String uhcfcode,
            final String ustate) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETENDCONOPENCONSTATE(:utags,:uhcfcode,:ustate); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("utags", utags.trim());
            statement.setString("uhcfcode", uhcfcode.trim());
            statement.setString("ustate", ustate.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                Contract contract = new Contract();
                contract.setConid(resultset.getString("CONID"));
                ACRGBWSResult facility = new Methods().GETMBWITHID(dataSource, resultset.getString("HCFID"));
                if (facility.isSuccess()) {
                    contract.setHcfid(facility.getResult());
                } else {
                    contract.setHcfid(facility.getMessage());
                }
                //END OF GET NETWORK FULL DETAILS
                contract.setAmount(resultset.getString("AMOUNT"));
                contract.setStats(resultset.getString("STATS"));
                ACRGBWSResult creator = this.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                if (creator.isSuccess()) {
                    contract.setCreatedby(creator.getResult());
                } else {
                    contract.setCreatedby(creator.getMessage());
                }
                contract.setDatecreated(dateformat.format(resultset.getTimestamp("DATECREATED")));
                ACRGBWSResult getcondateA = new ContractMethod().GETCONDATEBYID(dataSource, resultset.getString("CONTRACTDATE"));
                //SET OF NUMBER OF CLAIMS AND TRANCHES MOVEMENT
                int numberofclaims = 0;
                double totalclaimsamount = 0.00;
                double percentageA = 0.00;
                double percentageB = 0.00;
                double trancheamount = 0.00;
//                double totalrecievedamount = 0.00;
                int tranches = 0;
                if (getcondateA.isSuccess()) {
                    ContractDate condate = utility.ObjectMapper().readValue(getcondateA.getResult(), ContractDate.class);
                    contract.setContractdate(getcondateA.getResult());
                    contract.setBaseamount(resultset.getString("BASEAMOUNT"));
                    contract.setTranscode(resultset.getString("TRANSCODE"));
                    contract.setComittedClaimsVol(resultset.getString("C_CLAIMSVOL"));
                    contract.setComputedClaimsVol(resultset.getString("T_CLAIMSVOL"));
                    contract.setSb(resultset.getString("SB"));
                    contract.setAddamount(resultset.getString("ADDAMOUNT"));
                    contract.setQuarter(resultset.getString("QUARTER")); //GET TRANCHE AMOUNT
                    if (resultset.getString("ENDDATE") == null) {
                        contract.setEnddate("");
                    } else {
                        contract.setEnddate(dateformat.format(resultset.getTimestamp("ENDDATE")));
                    }
                    ACRGBWSResult restAB = this.GETASSETBYIDANDCONID(dataSource, resultset.getString("HCFID").trim(), resultset.getString("CONID"), utags.trim().toUpperCase());
                    if (restAB.isSuccess()) {
                        List<Assets> assetlist = Arrays.asList(utility.ObjectMapper().readValue(restAB.getResult(), Assets[].class));
                        for (int g = 0; g < assetlist.size(); g++) {
                            if (assetlist.get(g).getPreviousbalance() != null) {
                                Tranch tranch = utility.ObjectMapper().readValue(assetlist.get(g).getTranchid(), Tranch.class);
                                switch (tranch.getTranchtype()) {
                                    case "1ST": {
                                        trancheamount += Double.parseDouble(assetlist.get(g).getPreviousbalance());
                                        trancheamount += Double.parseDouble(assetlist.get(g).getReleasedamount());
                                        tranches++;
                                        break;
                                    }
                                    case "1STFINAL": {
                                        trancheamount += Double.parseDouble(assetlist.get(g).getReleasedamount());
//                                        tranches--;
                                        break;
                                    }
                                    default:
                                        trancheamount += Double.parseDouble(assetlist.get(g).getReleasedamount());
                                        tranches++;
                                        break;
                                }
                            } else {
                                trancheamount += Double.parseDouble(assetlist.get(g).getReleasedamount());
                                tranches++;
                            }
                        }
                    }
                    //GET CLAIMS AMOUNT AND CLAIMS OF FACILITY UNDER HCPN SELECTED
                    ACRGBWSResult restA = new Methods().GETROLEMULITPLE(dataSource, resultset.getString("HCFID"), utags.trim().toUpperCase());
                    if (restA.isSuccess()) {
                        List<String> hcflist = Arrays.asList(restA.getResult().split(","));
                        for (int v = 0; v < hcflist.size(); v++) {
//                            ACRGBWSResult GetFacilityContract = contractmethod.GETCONTRACT(dataSource, tags, hcflist.get(v).trim());
                            ACRGBWSResult GetFacilityContract = new ContractMethod().GETCONTRACTWITHOPENSTATE(dataSource, utags.trim().toUpperCase(), hcflist.get(v).trim(), ustate.trim().toUpperCase());
                            if (GetFacilityContract.isSuccess()) {
                                //-------------------------------------------------------------------  GET ALL ACCRE CODE UNDER SELECTED FACILITY
                                ACRGBWSResult getHcfByCode = new GetHCFMultiplePMCCNO().GETFACILITYBYCODE(dataSource, hcflist.get(v).trim());
                                if (getHcfByCode.isSuccess()) {
                                    HealthCareFacility healthCareFacility = utility.ObjectMapper().readValue(getHcfByCode.getResult(), HealthCareFacility.class);
                                    //GET HCF DETAILS BY NAME
                                    ACRGBWSResult getHcfByName = new GetHCFMultiplePMCCNO().GETFACILITYBYNAME(dataSource, healthCareFacility.getHcfname().trim(), healthCareFacility.getStreet().trim());
                                    if (getHcfByName.isSuccess()) {
                                        List<HealthCareFacility> healthCareFacilityList = Arrays.asList(utility.ObjectMapper().readValue(getHcfByName.getResult(), HealthCareFacility[].class));
                                        for (int yu = 0; yu < healthCareFacilityList.size(); yu++) {
                                            //------------------------------------------------------------------------END GET ALL ACCRE CODE UNDER SELECTED FACILITY
                                            ACRGBWSResult sumresult = this.GETNCLAIMS(dataSource, healthCareFacilityList.get(yu).getHcfcode().trim(), "G".toUpperCase(), condate.getDatefrom(), utility.AddMinusDaysDate(condate.getDateto(), "60"),
                                                    "CURRENTSTATUS");
                                            if (sumresult.isSuccess()) {
                                                Contract conss = utility.ObjectMapper().readValue(GetFacilityContract.getResult(), Contract.class);
                                                List<NclaimsData> nclaimsdata = Arrays.asList(utility.ObjectMapper().readValue(sumresult.getResult(), NclaimsData[].class));
                                                for (int i = 0; i < nclaimsdata.size(); i++) {
                                                    int countLedger = 0;
                                                    if (nclaimsdata.get(i).getRefiledate().isEmpty()) {
                                                        if (conss.getEnddate().isEmpty()) {
                                                            if (dateformat.parse(nclaimsdata.get(i).getDatesubmitted()).compareTo(dateformat.parse(utility.AddMinusDaysDate(condate.getDateto(), "60"))) <= 0) {
                                                                countLedger++;
                                                            }
                                                        } else {
                                                            if (dateformat.parse(nclaimsdata.get(i).getDatesubmitted()).compareTo(dateformat.parse(utility.AddMinusDaysDate(conss.getEnddate().trim(), "60"))) <= 0) {
                                                                countLedger++;
                                                            }
                                                        }
                                                    } else {
                                                        if (conss.getEnddate().isEmpty()) {
                                                            if (dateformat.parse(nclaimsdata.get(i).getRefiledate()).compareTo(dateformat.parse(utility.AddMinusDaysDate(condate.getDateto(), "60"))) <= 0) {
                                                                countLedger++;
                                                            }
                                                        } else {
                                                            if (dateformat.parse(nclaimsdata.get(i).getRefiledate()).compareTo(dateformat.parse(utility.AddMinusDaysDate(conss.getEnddate().trim(), "60"))) <= 0) {
                                                                countLedger++;
                                                            }
                                                        }
                                                    }
                                                    if (countLedger > 0) {
                                                        numberofclaims += Integer.parseInt(nclaimsdata.get(i).getTotalclaims());
                                                        totalclaimsamount += Double.parseDouble(nclaimsdata.get(i).getClaimamount());
                                                    }
                                                }
                                            }

                                        }
                                    }
                                }

                            }
                        }
                    }
                }
                double sumsA = (trancheamount / Double.parseDouble(resultset.getString("AMOUNT"))) * 100;
                if (sumsA > 100) {
                    double negvalue = 100 - sumsA;
                    percentageA += negvalue;
                } else {
                    percentageA += sumsA;
                }
                //----------------
                double sumsB = (totalclaimsamount / trancheamount) * 100;
                if (sumsB > 100) {
                    double negvalue = 100 - sumsB;
                    percentageB += negvalue;
                } else {
                    percentageB += sumsB;
                }
                contract.setTotalamountrecieved(String.valueOf(trancheamount));
                contract.setTotalclaims(String.valueOf(numberofclaims));
                contract.setTraches(String.valueOf(tranches));
                contract.setTotaltrancheamount(String.valueOf(trancheamount));
                contract.setTotalclaimsamount(String.valueOf(totalclaimsamount));
                contract.setPercentage(String.valueOf(percentageA));
                contract.setTotalclaimspercentage(String.valueOf(percentageB));
                result.setMessage("OK");
                result.setResult(utility.ObjectMapper().writeValueAsString(contract));
                result.setSuccess(true);
            }
        } catch (Exception ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET TERMINATED CONTRACT SINGLE RESULT
//    public ACRGBWSResult GetTerminateContract(
//            final DataSource dataSource,
//            final String pcode) {
//        ACRGBWSResult result = utility.ACRGBWSResult();
//        result.setMessage("");
//        result.setResult("");
//        result.setSuccess(false);
//        Methods methods = new Methods();
//        ContractMethod contractmethod = new ContractMethod();
//        try (Connection connection = dataSource.getConnection()) {
//            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETTERMINATECON(:pan); end;");
//            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
//            statement.setString("pan", pcode.trim());
//            statement.execute();
//            ResultSet resultset = (ResultSet) statement.getObject("v_result");
//            if (resultset.next()) {
//                Contract contract = new Contract();
//                contract.setAmount(resultset.getString("AMOUNT"));
//                contract.setBaseamount(resultset.getString("BASEAMOUNT"));
//                contract.setConid(resultset.getString("CONID"));
//                ACRGBWSResult creator = this.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
//                if (creator.isSuccess()) {
//                    contract.setCreatedby(creator.getResult());
//                } else {
//                    contract.setCreatedby(creator.getMessage());
//                }
//                contract.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));
//                ACRGBWSResult getcondateA = contractmethod.GETCONDATEBYID(dataSource, resultset.getString("CONTRACTDATE"));
//                if (getcondateA.isSuccess()) {
//                    contract.setContractdate(getcondateA.getResult());
//                } else {
//                    contract.setContractdate(getcondateA.getMessage());
//                }
//                contract.setEnddate(dateformat.format(resultset.getDate("ENDDATE")));
//                ACRGBWSResult facility = this.GETFACILITYID(dataSource, resultset.getString("HCFID"));
//                if (facility.isSuccess()) {
//                    HealthCareFacility hcf = utility.ObjectMapper().readValue(facility.getResult(), HealthCareFacility.class);
//                    contract.setHcfid(facility.getResult());
//                    ACRGBWSResult GetTotalClaimsAmount = methods.GetAmount(dataSource,
//                            hcf.getHcfcode(),
//                            dateformat.format(resultset.getDate("DATEFROM")),
//                            dateformat.format(resultset.getDate("ENDDATE")));
//                    if (GetTotalClaimsAmount.isSuccess()) {
//                        FacilityComputedAmount fca = utility.ObjectMapper().readValue(GetTotalClaimsAmount.getResult(), FacilityComputedAmount.class);
//                        fca.getTotalclaims();
//                        fca.getTotalamount();
//                        contract.setTotalclaims(fca.getTotalclaims());
//                        contract.setRemainingbalance(String.valueOf(Double.parseDouble(resultset.getString("AMOUNT")) - Double.parseDouble(fca.getTotalamount())));
//                    } else {
//                        contract.setTotalclaims("N/A");
//                        contract.setRemainingbalance(resultset.getString("AMOUNT"));
//                    }
//                }
//                contract.setRemarks(resultset.getString("REMARKS"));
//                contract.setStats(resultset.getString("STATS"));
//                contract.setTranscode(resultset.getString("TRANSCODE"));
//                //GET COUNT OF TRANCHES RELEASED
//                ACRGBWSResult Assets = this.GETASSETSWITHPARAM(dataSource, resultset.getString("HCFID"), resultset.getString("CONID"));
//                if (Assets.isSuccess()) {
//                    List<Assets> assetsList = Arrays.asList(utility.ObjectMapper().readValue(Assets.getResult(), Assets[].class));
//                    contract.setTraches(String.valueOf(assetsList.size()));
//                } else {
//                    contract.setTraches("NO TRANCH RELEASED");
//                }
//                result.setMessage("OK");
//                result.setSuccess(true);
//                result.setResult(utility.ObjectMapper().writeValueAsString(contract));
//            } else {
//                result.setMessage("N/A");
//            }
//        } catch (Exception ex) {
//            result.setMessage(ex.toString());
//            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        return result;
//    }
    //GET END OR NONRENEW CONTRACT SINGLE RESULT
//    public ACRGBWSResult GetEndContract(
//            final DataSource dataSource,
//            final String pcode) {
//        ACRGBWSResult result = utility.ACRGBWSResult();
//        result.setMessage("");
//        result.setResult("");
//        result.setSuccess(false);
//        Methods methods = new Methods();
//        ContractMethod contractmethod = new ContractMethod();
//        try (Connection connection = dataSource.getConnection()) {
//            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETENDCON(:pan); end;");
//            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
//            statement.setString("pan", pcode.trim());
//            statement.execute();
//            ResultSet resultset = (ResultSet) statement.getObject("v_result");
//            if (resultset.next()) {
//                Contract contract = new Contract();
//                contract.setAmount(resultset.getString("AMOUNT"));
//                contract.setBaseamount(resultset.getString("BASEAMOUNT"));
//                contract.setConid(resultset.getString("CONID"));
//                ACRGBWSResult creator = this.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
//                if (creator.isSuccess()) {
//                    contract.setCreatedby(creator.getResult());
//                } else {
//                    contract.setCreatedby(creator.getMessage());
//                }
//                contract.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));
//                ACRGBWSResult getcondateA = contractmethod.GETCONDATEBYID(dataSource, resultset.getString("CONTRACTDATE"));
//                if (getcondateA.isSuccess()) {
//                    contract.setContractdate(getcondateA.getResult());
//                }
//                contract.setEnddate(dateformat.format(resultset.getDate("ENDDATE")));
//                ACRGBWSResult facility = this.GETFACILITYID(dataSource, resultset.getString("HCFID"));
//                if (facility.isSuccess()) {
//                    HealthCareFacility hcf = utility.ObjectMapper().readValue(facility.getResult(), HealthCareFacility.class);
//                    contract.setHcfid(facility.getResult());
//                    ACRGBWSResult GetTotalClaimsAmount = methods.GetAmount(dataSource,
//                            hcf.getHcfcode(),
//                            dateformat.format(resultset.getDate("DATEFROM")),
//                            dateformat.format(resultset.getDate("ENDDATE")));
//                    if (GetTotalClaimsAmount.isSuccess()) {
//                        FacilityComputedAmount fca = utility.ObjectMapper().readValue(GetTotalClaimsAmount.getResult(), FacilityComputedAmount.class);
//                        fca.getTotalclaims();
//                        fca.getTotalamount();
//                        contract.setTotalclaims(fca.getTotalclaims());
//                        contract.setRemainingbalance(String.valueOf(Double.parseDouble(resultset.getString("AMOUNT")) - Double.parseDouble(fca.getTotalamount())));
//                    } else {
//                        contract.setTotalclaims("N/A");
//                        contract.setRemainingbalance(resultset.getString("AMOUNT"));
//                    }
//                }
//                contract.setRemarks(resultset.getString("REMARKS"));
//                contract.setStats(resultset.getString("STATS"));
//                contract.setTranscode(resultset.getString("TRANSCODE"));
//                //GET COUNT OF TRANCHES RELEASED
//                ACRGBWSResult Assets = this.GETASSETSWITHPARAM(dataSource, resultset.getString("HCFID"), resultset.getString("CONID"));
//                if (Assets.isSuccess()) {
//                    List<Assets> assetsList = Arrays.asList(utility.ObjectMapper().readValue(Assets.getResult(), Assets[].class));
//                    contract.setTraches(String.valueOf(assetsList.size()));
//                } else {
//                    contract.setTraches("NO TRANCH RELEASED");
//                }
//                result.setMessage("OK");
//                result.setSuccess(true);
//                result.setResult(utility.ObjectMapper().writeValueAsString(contract));
//            } else {
//                result.setMessage("N/A");
//            }
//        } catch (Exception ex) {
//            result.setMessage(ex.toString());
//            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        return result;
//    }
    //GET ASSETS BY CONID
    public ACRGBWSResult GETASSETSBYCONID(
            final DataSource dataSource,
            final String conid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ArrayList<Assets> listassets = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETASSETSBYCONID(:pconid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("pconid", conid.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            while (resultset.next()) {
                Assets assets = new Assets();
                assets.setAssetid(resultset.getString("ASSETSID"));
                ACRGBWSResult tranchresult = this.ACR_TRANCHWITHID(dataSource, resultset.getString("TRANCHID"));
                if (tranchresult.isSuccess()) {
                    assets.setTranchid(tranchresult.getResult());
                } else {
                    assets.setTranchid(tranchresult.getMessage());
                }
                ACRGBWSResult facilityresult = this.GETFACILITYID(dataSource, resultset.getString("HCFID"));
                if (facilityresult.isSuccess()) {
                    assets.setHcfid(facilityresult.getResult());
                } else {
                    assets.setHcfid(facilityresult.getMessage());
                }
                assets.setDatereleased(dateformat.format(resultset.getTimestamp("DATERELEASED")));//resultset.getDate("DATERELEASED"));
                assets.setReceipt(resultset.getString("RECEIPT"));
                assets.setAmount(resultset.getString("AMOUNT"));
                ACRGBWSResult creator = this.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                if (creator.isSuccess()) {
                    UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class);
                    assets.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                } else {
                    assets.setCreatedby(creator.getMessage());
                }
                assets.setDatecreated(dateformat.format(resultset.getTimestamp("DATECREATED")));
                ACRGBWSResult getcon = this.GETCONTRACTCONID(dataSource, resultset.getString("CONID").trim(), "ACTIVE");
                if (getcon.isSuccess()) {
                    assets.setConid(getcon.getResult());
                } else {
                    assets.setConid(getcon.getMessage());
                }
                assets.setReleasedamount(resultset.getString("RELEASEDAMOUNT"));
                assets.setStatus(resultset.getString("STATS"));
                assets.setPreviousbalance(resultset.getString("PREVIOUSBAL"));
                assets.setClaimscount(resultset.getString("CLAIMSCOUNT"));
                listassets.add(assets);
            }
            if (listassets.size() > 0) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(listassets));
            } else {
                result.setMessage("N/A");
            }
        } catch (Exception ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET ACCREDITATION NUMBER BY ACCOUNTCODE
    public ACRGBWSResult GETACCREDITATION(
            final DataSource dataSource,
            final String uaccreno) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETACCREDITATION(:uaccreno); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("uaccreno", uaccreno.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                Accreditation accree = new Accreditation();
                accree.setAccreno(resultset.getString("ACCRENO"));
                accree.setDatefrom(dateformat.format(resultset.getTimestamp("DATEFROM")));
                accree.setDateto(dateformat.format(resultset.getTimestamp("DATETO")));
                accree.setDatecreated(dateformat.format(resultset.getTimestamp("DATECREATED")));
                ACRGBWSResult creator = this.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                if (creator.isSuccess()) {
                    UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class);
                    accree.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                } else {
                    accree.setCreatedby(creator.getMessage());
                }
                accree.setStatus(resultset.getString("STATUS"));
                result.setResult(utility.ObjectMapper().writeValueAsString(accree));
                result.setMessage("OK");
                result.setSuccess(true);
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET ACCREDITATION NUMBER BY ACCOUNTCODE
    public ACRGBWSResult GETCONBYCODE(
            final DataSource dataSource,
            final String ucode) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETCONBYCODE(:ucode); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("ucode", ucode.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                Contract con = new Contract();
                con.setConid(resultset.getString("CONID"));
                con.setHcfid(resultset.getString("HCFID"));
                con.setAmount(resultset.getString("AMOUNT"));
                con.setStats(resultset.getString("STATS"));
                ACRGBWSResult creator = this.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                if (creator.isSuccess()) {
                    UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class);
                    con.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                } else {
                    con.setCreatedby(creator.getMessage());
                }
                con.setDatecreated(dateformat.format(resultset.getTimestamp("DATECREATED")));
                ACRGBWSResult getcondateA = new ContractMethod().GETCONDATEBYID(dataSource, resultset.getString("CONTRACTDATE"));
                if (getcondateA.isSuccess()) {
                    con.setContractdate(getcondateA.getResult());
                }
                con.setTranscode(resultset.getString("TRANSCODE"));
                con.setBaseamount(resultset.getString("BASEAMOUNT"));
                con.setRemarks(resultset.getString("REMARKS"));
                con.setComittedClaimsVol(resultset.getString("C_CLAIMSVOL"));
                con.setComputedClaimsVol(resultset.getString("T_CLAIMSVOL"));
                con.setAddamount(resultset.getString("ADDAMOUNT"));
                con.setQuarter(resultset.getString("QUARTER"));
                con.setSb(resultset.getString("SB"));
                if (resultset.getTimestamp("ENDDATE") == null) {
                    con.setEnddate("N/A");
                } else {
                    con.setEnddate(dateformat.format(resultset.getTimestamp("ENDDATE")));
                }
                result.setResult(utility.ObjectMapper().writeValueAsString(con));
                result.setMessage("OK");
                result.setSuccess(true);
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET APPELLATE CONTROL
    public ACRGBWSResult GetAffiliate(
            final DataSource dataSource,
            final String accesscode,
            final String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ArrayList<String> accesslist = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETAPPELLATE(:tags,:accesscode); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("tags", tags.toUpperCase().trim());
            statement.setString("accesscode", accesscode.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            while (resultset.next()) {
                accesslist.add(resultset.getString("CONTROLCODE"));
            }
            if (!accesslist.isEmpty()) {
                result.setSuccess(true);
                result.setResult(String.join(",", accesslist));
                result.setMessage("OK");
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult GETASSETBYIDANDCONID(
            final DataSource dataSource,
            final String phcfid,
            final String uconid,
            final String utags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETASSETBYIDANDCONID(:phcfid,:uconid,:utags); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("phcfid", phcfid.trim());
            statement.setString("uconid", uconid.trim());
            statement.setString("utags", utags.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            ArrayList<Assets> listassets = new ArrayList<>();
            while (resultset.next()) {
                Assets assets = new Assets();
                assets.setAssetid(resultset.getString("ASSETSID"));
                ACRGBWSResult tranchresult = this.ACR_TRANCHWITHID(dataSource, resultset.getString("TRANCHID"));
                if (tranchresult.isSuccess()) {
                    assets.setTranchid(tranchresult.getResult());
                } else {
                    assets.setTranchid(tranchresult.getMessage());
                }
                ACRGBWSResult facilityresult = this.GETFACILITYID(dataSource, resultset.getString("HCFID"));
                if (facilityresult.isSuccess()) {
                    assets.setHcfid(facilityresult.getResult());
                } else {
                    ACRGBWSResult getHCPN = new Methods().GETMBWITHID(dataSource, resultset.getString("HCFID"));
                    if (getHCPN.isSuccess()) {
                        assets.setHcfid(getHCPN.getResult());
                    } else {
                        assets.setHcfid(getHCPN.getMessage());
                    }
                }
                assets.setDatereleased(dateformat.format(resultset.getTimestamp("DATERELEASED")));//resultset.getDate("DATERELEASED"));
                assets.setReceipt(resultset.getString("RECEIPT"));
                assets.setAmount(resultset.getString("AMOUNT"));
                ACRGBWSResult creator = this.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                if (creator.isSuccess()) {
                    UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class);
                    assets.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                } else {
                    assets.setCreatedby("N/A");
                }
                assets.setDatecreated(dateformat.format(resultset.getTimestamp("DATECREATED")));
                ACRGBWSResult getcon = this.GETCONTRACTCONID(dataSource, resultset.getString("CONID").trim(), utags);
                if (getcon.isSuccess()) {
                    assets.setConid(getcon.getResult());
                } else {
                    assets.setCreatedby("N/A");
                }
                assets.setStatus(resultset.getString("STATS"));
                assets.setPreviousbalance(resultset.getString("PREVIOUSBAL"));
                assets.setClaimscount(resultset.getString("CLAIMSCOUNT"));
                assets.setReleasedamount(resultset.getString("RELEASEDAMOUNT"));
                listassets.add(assets);
            }
            if (listassets.size() > 0) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(listassets));
            } else {
                result.setMessage("N/A");
            }

        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult GETASSETSHCFID(
            final DataSource dataSource,
            final String phcfid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :assets_type := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETASSETSHCFID(:phcfid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("phcfid", phcfid.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            ArrayList<Assets> listassets = new ArrayList<>();
            while (resultset.next()) {
                Assets assets = new Assets();
                assets.setAssetid(resultset.getString("ASSETSID"));
                ACRGBWSResult tranchresult = this.ACR_TRANCHWITHID(dataSource, resultset.getString("TRANCHID"));
                if (tranchresult.isSuccess()) {
                    assets.setTranchid(tranchresult.getResult());
                } else {
                    assets.setTranchid(tranchresult.getMessage());
                }
                ACRGBWSResult facilityresult = this.GETFACILITYID(dataSource, resultset.getString("HCFID"));
                if (facilityresult.isSuccess()) {
                    assets.setHcfid(facilityresult.getResult());
                } else {
                    ACRGBWSResult getHCPN = new Methods().GETMBWITHID(dataSource, resultset.getString("HCFID"));
                    if (getHCPN.isSuccess()) {
                        assets.setHcfid(getHCPN.getResult());
                    } else {
                        assets.setHcfid(getHCPN.getMessage());
                    }
                }
                assets.setDatereleased(dateformat.format(resultset.getTimestamp("DATERELEASED")));//resultset.getDate("DATERELEASED"));
                assets.setReceipt(resultset.getString("RECEIPT"));
                assets.setAmount(resultset.getString("AMOUNT"));
                assets.setReleasedamount(resultset.getString("RELEASEDAMOUNT"));
                ACRGBWSResult creator = this.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                if (creator.isSuccess()) {
                    UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class);
                    assets.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                } else {
                    assets.setCreatedby("N/A");
                }
                assets.setDatecreated(dateformat.format(resultset.getTimestamp("DATECREATED")));
                ACRGBWSResult getcon = this.GETCONTRACTCONID(dataSource, resultset.getString("CONID").trim(), "ACTIVE");
                if (getcon.isSuccess()) {
                    assets.setConid(getcon.getResult());
                } else {
                    assets.setCreatedby("N/A");
                }
                assets.setStatus(resultset.getString("STATS"));
                assets.setPreviousbalance(resultset.getString("PREVIOUSBAL"));
                assets.setClaimscount(resultset.getString("CLAIMSCOUNT"));
                listassets.add(assets);
            }
            if (!listassets.isEmpty()) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(listassets));
            } else {
                result.setMessage("NO DATA FOUND");
            }

        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //-----------------------------------------------------------------------
    public ACRGBWSResult GETUSERBYID(
            final DataSource dataSource,
            final String uUserid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETUSERBYID(:uUserid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("uUserid", uUserid.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(resultset.getString("LEVNAME"));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET CONTRACT USING HCPN CONTROL CODE
    public ACRGBWSResult GetFacilityContractUsingHCPNCodeS(
            final DataSource dataSource,
            final String utags,
            final String controlcode,
            final String ustate) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            ArrayList<Contract> contractlist = new ArrayList<>();
            //-------------- GET APEX FACILITY
            ACRGBWSResult restA = new Methods().GETROLEMULITPLE(dataSource, controlcode.trim(), utags);
            List<String> hcflist = Arrays.asList(restA.getResult().split(","));
            //-------------- END OF GET APEX FACILITY
            for (int y = 0; y < hcflist.size(); y++) {
                //--------------------------------------------------------
//                CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.ACR_CONTRACT(:tags,:pfchid); end;");
//                statement.registerOutParameter("v_result", OracleTypes.CURSOR);
//                statement.setString("tags", tags.trim());
//                statement.setString("pfchid", hcflist.get(y).trim());

                CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETENDCONOPENCONSTATE(:utags,:uhcfcode,:ustate); end;");
                statement.registerOutParameter("v_result", OracleTypes.CURSOR);
                statement.setString("utags", utags.trim());
                statement.setString("uhcfcode", hcflist.get(y).trim());
                statement.setString("ustate", ustate.trim());
                statement.execute();
                ResultSet resultset = (ResultSet) statement.getObject("v_result");
                if (resultset.next()) {
                    Contract contract = new Contract();
                    contract.setConid(resultset.getString("CONID"));
                    ACRGBWSResult facility = this.GETFACILITYID(dataSource, resultset.getString("HCFID"));
                    if (facility.isSuccess()) {
                        contract.setHcfid(facility.getResult());
                    } else {
                        contract.setHcfid(facility.getMessage());
                    }
                    //END OF GET NETWORK FULL DETAILS
                    contract.setAmount(resultset.getString("AMOUNT"));
                    contract.setStats(resultset.getString("STATS"));
                    ACRGBWSResult creator = this.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                    if (creator.isSuccess()) {
                        contract.setCreatedby(creator.getResult());
                    } else {
                        contract.setCreatedby(creator.getMessage());
                    }
                    contract.setDatecreated(dateformat.format(resultset.getTimestamp("DATECREATED")));//resultset.getString("DATECREATED"));
                    ACRGBWSResult getcondateA = new ContractMethod().GETCONDATEBYID(dataSource, resultset.getString("CONTRACTDATE"));
                    //=============================================
                    int numberofclaims = 0;
                    double totalclaimsamount = 0.00;
                    double percentageA = 0.00;
                    double percentageB = 0.00;
                    double trancheamount = 0.00;
//                    double totalrecievedamount = 0.00;
                    int tranches = 0;
                    if (getcondateA.isSuccess()) {
                        contract.setContractdate(getcondateA.getResult());
                        ContractDate condate = utility.ObjectMapper().readValue(getcondateA.getResult(), ContractDate.class);
                        contract.setTranscode(resultset.getString("TRANSCODE"));
                        contract.setBaseamount(resultset.getString("BASEAMOUNT"));
                        contract.setComittedClaimsVol(resultset.getString("C_CLAIMSVOL"));
                        contract.setComputedClaimsVol(resultset.getString("T_CLAIMSVOL"));
                        contract.setSb(resultset.getString("SB"));
                        contract.setAddamount(resultset.getString("ADDAMOUNT"));
                        contract.setQuarter(resultset.getString("QUARTER"));
                        if (resultset.getString("ENDDATE") == null) {
                            contract.setEnddate("");
                        } else {
                            contract.setEnddate(dateformat.format(resultset.getTimestamp("ENDDATE")));
                        }
                        ACRGBWSResult restAB = this.GETASSETBYIDANDCONID(dataSource, resultset.getString("HCFID").trim(), resultset.getString("CONID"), utags.trim().toUpperCase());
                        if (restAB.isSuccess()) {
                            List<Assets> assetlist = Arrays.asList(utility.ObjectMapper().readValue(restAB.getResult(), Assets[].class));
                            for (int g = 0; g < assetlist.size(); g++) {
                                if (assetlist.get(g).getPreviousbalance() != null) {
                                    Tranch tranch = utility.ObjectMapper().readValue(assetlist.get(g).getTranchid(), Tranch.class);
                                    switch (tranch.getTranchtype()) {
                                        case "1ST": {
                                            trancheamount += Double.parseDouble(assetlist.get(g).getPreviousbalance());
                                            trancheamount += Double.parseDouble(assetlist.get(g).getReleasedamount());
                                            tranches++;
                                            break;
                                        }
                                        case "1STFINAL": {
                                            trancheamount += Double.parseDouble(assetlist.get(g).getReleasedamount());
//                                            tranches--;
                                            break;
                                        }
                                        default: {
                                            trancheamount += Double.parseDouble(assetlist.get(g).getReleasedamount());
                                            tranches++;
                                            break;
                                        }
                                    }
                                } else {
                                    trancheamount += Double.parseDouble(assetlist.get(g).getReleasedamount());
                                    tranches++;
                                }
                            }
                        }
//                        ACRGBWSResult GetFacilityContract = contractmethod.GETCONTRACT(dataSource, tags, resultset.getString("HCFID").trim());
//                        ACRGBWSResult GetFacilityContract = contractmethod.GETCONTRACTWITHOPENSTATE(dataSource, utags.trim().toUpperCase(), resultset.getString("HCFID").trim(), ustate.trim().toUpperCase());
//                        if (GetFacilityContract.isSuccess()) {
                        //-------------------------------------------------------------------
                        ACRGBWSResult getHcfByCode = new GetHCFMultiplePMCCNO().GETFACILITYBYCODE(dataSource, resultset.getString("HCFID").trim());
                        if (getHcfByCode.isSuccess()) {
                            HealthCareFacility healthCareFacility = utility.ObjectMapper().readValue(getHcfByCode.getResult(), HealthCareFacility.class);
                            //GET HCF DETAILS BY NAME
                            ACRGBWSResult getHcfByName = new GetHCFMultiplePMCCNO().GETFACILITYBYNAME(dataSource, healthCareFacility.getHcfname().trim(), healthCareFacility.getStreet().trim());
                            if (getHcfByName.isSuccess()) {
                                List<HealthCareFacility> healthCareFacilityList = Arrays.asList(utility.ObjectMapper().readValue(getHcfByName.getResult(), HealthCareFacility[].class));
                                for (int yu = 0; yu < healthCareFacilityList.size(); yu++) {
                                    //------------------------------------------------------------------------
                                    ACRGBWSResult sumresult = this.GETNCLAIMS(dataSource, healthCareFacilityList.get(yu).getHcfcode().trim(), "G",
                                            condate.getDatefrom(), utility.AddMinusDaysDate(condate.getDateto(), "60"), "CURRENTSTATUS");
                                    if (sumresult.isSuccess()) {
//                                Contract ownCon = utility.ObjectMapper().readValue(GetFacilityContract.getResult(), Contract.class);
                                        List<NclaimsData> nclaimsdata = Arrays.asList(utility.ObjectMapper().readValue(sumresult.getResult(), NclaimsData[].class));
                                        for (int i = 0; i < nclaimsdata.size(); i++) {
                                            int countLedger = 0;
                                            if (nclaimsdata.get(i).getRefiledate().isEmpty()) {
                                                if (resultset.getString("ENDDATE") == null) {
                                                    if (dateformat.parse(nclaimsdata.get(i).getDatesubmitted()).compareTo(dateformat.parse(utility.AddMinusDaysDate(condate.getDateto(), "60"))) <= 0) {
                                                        countLedger++;
                                                    }
                                                } else {
                                                    if (dateformat.parse(nclaimsdata.get(i).getDatesubmitted()).compareTo(dateformat.parse(utility.AddMinusDaysDate(dateformat.format(resultset.getTimestamp("ENDDATE")), "60"))) <= 0) {
                                                        countLedger++;
                                                    }
                                                }
                                            } else {
                                                if (resultset.getString("ENDDATE") == null) {
                                                    if (dateformat.parse(nclaimsdata.get(i).getRefiledate()).compareTo(dateformat.parse(utility.AddMinusDaysDate(condate.getDateto(), "60"))) <= 0) {
                                                        countLedger++;
                                                    }
                                                } else {
                                                    if (dateformat.parse(nclaimsdata.get(i).getRefiledate()).compareTo(dateformat.parse(utility.AddMinusDaysDate(dateformat.format(resultset.getTimestamp("ENDDATE")), "60"))) <= 0) {
                                                        countLedger++;
                                                    }
                                                }
                                            }
                                            if (countLedger > 0) {
                                                numberofclaims += Integer.parseInt(nclaimsdata.get(i).getTotalclaims());
                                                totalclaimsamount += Double.parseDouble(nclaimsdata.get(i).getClaimamount());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    double sumsA = (trancheamount / Double.parseDouble(resultset.getString("AMOUNT"))) * 100;
                    if (sumsA > 100) {
                        double negvalue = 100 - sumsA;
                        percentageA += negvalue;
                    } else {
                        percentageA += sumsA;
                    }
                    //----------------
                    double sumsB = (totalclaimsamount / trancheamount) * 100;
                    if (sumsB > 100) {
                        double negvalue = 100 - sumsB;
                        percentageB += negvalue;
                    } else {
                        percentageB += sumsB;
                    }
                    contract.setTotalamountrecieved(String.valueOf(trancheamount));
                    contract.setTotalclaims(String.valueOf(numberofclaims));
                    contract.setTraches(String.valueOf(tranches));
                    contract.setTotaltrancheamount(String.valueOf(trancheamount));
                    contract.setTotalclaimsamount(String.valueOf(totalclaimsamount));
                    contract.setPercentage(String.valueOf(percentageA));
                    contract.setTotalclaimspercentage(String.valueOf(percentageB));
                    contractlist.add(contract);
                }
            }

            if (!contractlist.isEmpty()) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(contractlist));
            } else {
                result.setMessage("N/A");
            }

        } catch (SQLException | IOException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

//GET MULTIPLE CLAIMS FOR BOOKING
//GET NCLAIMS DATA AND AMOUNT OF ITS CLAIMS TOTAL
    public ACRGBWSResult BOOKCLAIMSDATA(
            final DataSource dataSource,
            final String upmccno,
            final String u_tags,
            final String u_from,
            final String u_to) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKG.GETNCLAIMS(:upmccno,:u_tags,:u_from,:u_to); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("upmccno", upmccno.trim());
            statement.setString("u_tags", u_tags.trim());
            statement.setDate("u_from", (Date) new Date(utility.StringToDate(u_from).getTime()));
            statement.setDate("u_to", (Date) new Date(utility.StringToDate(u_to).getTime()));
            statement.execute();
            ArrayList<NclaimsData> claimsList = new ArrayList<>();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            while (resultset.next()) {
                NclaimsData nclaimsdata = new NclaimsData();
                nclaimsdata.setAccreno(resultset.getString("PMCC_NO"));
                nclaimsdata.setClaimamount(resultset.getString("CTOTAL"));
                nclaimsdata.setTotalclaims(resultset.getString("COUNTVAL"));
                if (resultset.getTimestamp("DATESUB") != null) {
                    nclaimsdata.setDatesubmitted(dateformat.format(resultset.getTimestamp("DATESUB")));
                } else {
                    nclaimsdata.setDatesubmitted("");
                }
                if (resultset.getTimestamp("DATEREFILE") != null) {
                    nclaimsdata.setRefiledate(dateformat.format(resultset.getTimestamp("DATEREFILE")));
                } else {
                    nclaimsdata.setRefiledate("");
                }
                if (resultset.getTimestamp("DATEADM") != null) {
                    nclaimsdata.setDateadmission(dateformat.format(resultset.getTimestamp("DATEADM")));
                } else {
                    nclaimsdata.setDateadmission("");
                }
                claimsList.add(nclaimsdata);
            }
            if (claimsList.size() > 0) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(claimsList));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET USER INFO USING EMAIL ADDRESS
    public ACRGBWSResult GETUSERINFOUSINGEMAIL(
            final DataSource dataSource,
            final String uemailadd) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETUSERINFOUSINGEMAIL(:uemailadd); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("uemailadd", uemailadd.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                UserInfo userinfo = new UserInfo();
                userinfo.setDid(resultset.getString("DID"));
                userinfo.setFirstname(resultset.getString("FIRSTNAME"));
                userinfo.setLastname(resultset.getString("LASTNAME"));
                userinfo.setMiddlename(resultset.getString("MIDDLENAME"));
                userinfo.setDatecreated(resultset.getString("DATECREATED"));
                userinfo.setStats(resultset.getString("STATS"));
                userinfo.setCreatedby(resultset.getString("CREATEDBY"));
                userinfo.setEmail(resultset.getString("EMAIL"));
                userinfo.setContact(resultset.getString("CONTACT"));
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(userinfo));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET USER ACCOUNT USING EMAIL ACCOUNT
    public ACRGBWSResult GETACCOUNTUSINGEMAIL(
            final DataSource dataSource,
            final String uusername) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETACCOUNTUSINGEMAIL(:uusername); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("uusername", uusername.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                User user = new User();
                user.setUserid(resultset.getString("USERID"));
                user.setLeveid(resultset.getString("LEVELID"));
                user.setUsername(resultset.getString("USERNAME"));
                user.setUserpassword(resultset.getString("USERPASSWORD"));
                user.setDatecreated(resultset.getString("DATECREATED"));
                user.setStatus(resultset.getString("STATS"));
                user.setCreatedby(resultset.getString("CREATEDBY"));
                user.setDid(resultset.getString("DID"));
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(user));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET USER ACCOUNT USING EMAIL ACCOUNT
    public ACRGBWSResult FORUSERLEVEL(
            final DataSource dataSource,
            final String ulevelid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.FORUSERLEVEL(:ulevelid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("ulevelid", ulevelid.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                UserLevel userlevel = new UserLevel();
                userlevel.setLevelid(resultset.getString("LEVELID"));
                userlevel.setLevdetails(resultset.getString("LEVDETAILS"));
                userlevel.setLevname(resultset.getString("LEVNAME"));
                userlevel.setCreatedby(resultset.getString("CREATEDBY"));
                userlevel.setDatecreated(resultset.getString("DATECREATED"));
                userlevel.setStats(resultset.getString("STATS"));
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(userlevel));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult GETOLDPASSCODE(
            final DataSource dataSource,
            final String puserid,
            final String passcode) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETPASSUSINGUSERID(:puserid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("puserid", puserid.replaceAll("\\s", "").toUpperCase());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                if ( new Cryptor().decrypt(resultset.getString("USERPASSWORD"), passcode, "ACRGB").trim().equals(passcode.trim())) {
                    result.setMessage("OK");
                    result.setSuccess(true);
                } else {
                    result.setMessage("PASSWORD NOT VALID");
                }
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET DIRECT CONTRACT OF PRO AND QUARTER
    public ACRGBWSResult CONTRACTWITHQUARTER(
            final DataSource dataSource,
            final String tags,
            final String uprocode) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        int numberofclaims = 0;
        double totalclaimsamount = 0.00;
        double percentageA = 0.00;
        double percentageB = 0.00;
        double trancheamount = 0.00;
        double totalamount = 0.00;
        int tranches = 0;
        try (Connection connection = dataSource.getConnection()) {
            ArrayList<Contract> contractlist = new ArrayList<>();
            //--------------------------------------------------------
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.ACR_CONTRACT(:tags,:pfchid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("tags", tags.trim());
            statement.setString("pfchid", uprocode.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            while (resultset.next()) {
                Contract contract = new Contract();
                contract.setConid(resultset.getString("CONID"));
                ACRGBWSResult facility = this.GETFACILITYID(dataSource, resultset.getString("HCFID"));
                if (facility.isSuccess()) {
                    contract.setHcfid(facility.getResult());
                } else {
                    contract.setHcfid(facility.getMessage());
                }
                //END OF GET NETWORK FULL DETAILS
                contract.setAmount(resultset.getString("AMOUNT"));
                contract.setStats(resultset.getString("STATS"));
                ACRGBWSResult creator = this.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                if (creator.isSuccess()) {
                    contract.setCreatedby(creator.getResult());
                } else {
                    contract.setCreatedby(creator.getMessage());
                }
                contract.setDatecreated(dateformat.format(resultset.getTimestamp("DATECREATED")));//resultset.getString("DATECREATED"));
                ACRGBWSResult getcondateA = new ContractMethod().GETCONDATEBYID(dataSource, resultset.getString("CONTRACTDATE"));
                contract.setContractdate(getcondateA.getResult());
                contract.setTranscode(resultset.getString("TRANSCODE"));
                contract.setBaseamount(resultset.getString("BASEAMOUNT"));
                contract.setComittedClaimsVol(resultset.getString("C_CLAIMSVOL"));
                contract.setComputedClaimsVol(resultset.getString("T_CLAIMSVOL"));
                contract.setSb(resultset.getString("SB"));
                contract.setAddamount(resultset.getString("ADDAMOUNT"));
                contract.setQuarter(resultset.getString("QUARTER"));
                //GET ALL FACILITY UNDER HCPN
                if (getcondateA.isSuccess()) {
                    ContractDate condate = utility.ObjectMapper().readValue(getcondateA.getResult(), ContractDate.class);
                    ACRGBWSResult GetRoleB = new Methods().GETROLEMULITPLE(dataSource, uprocode.trim(), tags);
                    if (GetRoleB.isSuccess()) {
                        List<String> HCPNList = Arrays.asList(GetRoleB.getResult().split(","));
                        for (int A = 0; A < HCPNList.size(); A++) {
                            ACRGBWSResult GetRoleC = new Methods().GETROLEMULITPLE(dataSource, HCPNList.get(A).trim(), tags);
                            if (GetRoleC.isSuccess()) {
                                List<String> HCIList = Arrays.asList(GetRoleC.getResult().split(","));
                                for (int B = 0; B < HCIList.size(); B++) {
                                    ACRGBWSResult getIdType = new CurrentBalance().GETTRANCHBYTYPE(dataSource, "1STFINAL");
                                    if (getIdType.isSuccess()) {
                                        Tranch tranch = utility.ObjectMapper().readValue(getIdType.getResult(), Tranch.class);
                                        ACRGBWSResult totalResult = new Methods().GETSUMMARY(dataSource, tags, HCIList.get(B).trim(), tranch.getTranchid(), resultset.getString("CONID"));
                                        if (totalResult.isSuccess()) {
                                            Total getResult = utility.ObjectMapper().readValue(totalResult.getResult(), Total.class);
                                            tranches += Integer.parseInt(getResult.getCcount());
                                            trancheamount += Double.parseDouble(getResult.getCtotal());
                                        }
                                        //GET TRANCHE AMOUNT
                                        ACRGBWSResult getTranchid = new CurrentBalance().GET1STFINAL(dataSource, tags.trim().toUpperCase(), HCIList.get(B).trim(), tranch.getTranchid(), resultset.getString("CONID"));
                                        if (getTranchid.isSuccess()) {
                                            Total getResult = utility.ObjectMapper().readValue(getTranchid.getResult(), Total.class);
                                            tranches -= Integer.parseInt(getResult.getCcount());
                                            trancheamount += Double.parseDouble(getResult.getCtotal());
                                        }
                                    }

                                    ACRGBWSResult sumresultB = this.GETNCLAIMS(dataSource, HCIList.get(B).trim(), "G", condate.getDatefrom(), condate.getDateto(), "CURRENTSTATUS");
                                    if (sumresultB.isSuccess()) {
                                        List<NclaimsData> nclaimsdata = Arrays.asList(utility.ObjectMapper().readValue(sumresultB.getResult(), NclaimsData[].class));
                                        for (int i = 0; i < nclaimsdata.size(); i++) {
                                            if (nclaimsdata.get(i).getRefiledate().isEmpty()) {
                                                if (dateformat.parse(nclaimsdata.get(i).getDatesubmitted()).compareTo(dateformat.parse(utility.AddMinusDaysDate(condate.getDateto(), "60"))) <= 0) {
                                                    numberofclaims += Integer.parseInt(nclaimsdata.get(i).getTotalclaims());
                                                    totalclaimsamount += Double.parseDouble(nclaimsdata.get(i).getClaimamount());
                                                }
                                            } else {
                                                if (dateformat.parse(nclaimsdata.get(i).getRefiledate()).compareTo(dateformat.parse(utility.AddMinusDaysDate(condate.getDateto(), "60"))) <= 0) {
                                                    numberofclaims += Integer.parseInt(nclaimsdata.get(i).getTotalclaims());
                                                    totalclaimsamount += Double.parseDouble(nclaimsdata.get(i).getClaimamount());
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                totalamount += Double.parseDouble(resultset.getString("AMOUNT"));
                double sumsA = (trancheamount / Double.parseDouble(resultset.getString("AMOUNT"))) * 100;
                if (sumsA > 100) {
                    double negvalue = 100 - sumsA;
                    percentageA += negvalue;
                } else {
                    percentageA += sumsA;
                }
                double sumsB = (totalclaimsamount / trancheamount) * 100;
                if (sumsB > 100) {
                    double negvalue = 100 - sumsB;
                    percentageB += negvalue;
                } else {
                    percentageB += sumsB;
                }
                contract.setTotalclaims(String.valueOf(numberofclaims));
                contract.setTraches(String.valueOf(tranches));
                contract.setTotaltrancheamount(String.valueOf(trancheamount));
                contract.setTotalclaimsamount(String.valueOf(totalclaimsamount));
                contract.setPercentage(String.valueOf(percentageA));
                contract.setTotalclaimspercentage(String.valueOf(percentageB));
                contractlist.add(contract);
            }
            if (contractlist.size() > 0) {
                Contract contractA = new Contract();
                contractA.setAmount(String.valueOf(totalamount));
                contractA.setTotalclaims(String.valueOf(numberofclaims));
                contractA.setTraches(String.valueOf(tranches));
                contractA.setTotaltrancheamount(String.valueOf(trancheamount));
                contractA.setTotalclaimsamount(String.valueOf(totalclaimsamount));
                contractA.setPercentage(String.valueOf(percentageA));
                contractA.setTotalclaimspercentage(String.valueOf(percentageB));
                contractlist.add(contractA);
                //=====================================    
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(contractlist));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET BOOK DATA
    public ACRGBWSResult GETACR_BOOKING(
            final DataSource dataSource,
            final String pconid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETACR_BOOKING(:pconid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("pconid", pconid.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                Book book = new Book();
                book.setBooknum(resultset.getString("BOOKNUM"));
                book.setConid(resultset.getString("CONID"));
                book.setDatecreated(dateformat.format(resultset.getTimestamp("DATECREATED")));
                ACRGBWSResult creator = this.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                if (creator.isSuccess()) {
                    UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class);
                    book.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                } else {
                    book.setCreatedby(creator.getMessage());
                }
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(book));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET ALL CONTRACT DATA
    public ACRGBWSResult GETALLCONTRACT(
            final DataSource dataSource,
            final String utags,
            final String uhcfcode) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETALLCONTRACT(:utags,:uhcfcode); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("utags", utags.trim());
            statement.setString("uhcfcode", uhcfcode.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            ArrayList<Contract> contractList = new ArrayList<>();
            while (resultset.next()) {
                Contract contract = new Contract();
                contract.setConid(resultset.getString("CONID"));
                //GET ACCOUNT NAME
                ACRGBWSResult GetFacility = this.GETFACILITYID(dataSource, resultset.getString("HCFID").trim());//GET FACILITY
                ACRGBWSResult GetHCPN = new Methods().GETMBWITHID(dataSource, resultset.getString("HCFID").trim());
                ACRGBWSResult GetPRO = new Methods().GetProWithPROID(dataSource, resultset.getString("HCFID").trim());
                if (GetFacility.isSuccess()) {
                    contract.setHcfid(GetFacility.getResult());
                } else if (GetHCPN.isSuccess()) {
                    contract.setHcfid(GetHCPN.getResult());
                } else if (GetPRO.isSuccess()) {
                    contract.setHcfid(GetPRO.getResult());
                } else {
                    contract.setHcfid("N/A");
                }
                // END OF GET ACCOUNT NAME
                contract.setAmount(resultset.getString("AMOUNT"));
                contract.setStats(resultset.getString("STATS"));
                contract.setCreatedby(resultset.getString("CREATEDBY"));
                contract.setDatecreated(dateformat.format(resultset.getTimestamp("DATECREATED")));
                contract.setTranscode(resultset.getString("TRANSCODE"));
                contract.setBaseamount(resultset.getString("BASEAMOUNT"));
                contract.setRemarks(resultset.getString("REMARKS"));
                if (resultset.getTimestamp("ENDDATE") == null) {
                    contract.setEnddate(resultset.getString("ENDDATE"));
                } else {
                    contract.setEnddate(dateformat.format(resultset.getTimestamp("ENDDATE")));
                }
                //GET CONTRACT DATE PERIOD
                ACRGBWSResult GetContractPeriod = new ContractMethod().GETCONDATEBYID(dataSource, resultset.getString("CONTRACTDATE").trim());
                if (GetContractPeriod.isSuccess()) {
                    contract.setContractdate(GetContractPeriod.getResult());
                } else {
                    contract.setContractdate(GetContractPeriod.getMessage());
                }
                //END GET CONTRACT DATE PERIOD
                contract.setComittedClaimsVol(resultset.getString("C_CLAIMSVOL"));
                contract.setComputedClaimsVol(resultset.getString("T_CLAIMSVOL"));
                contract.setSb(resultset.getString("SB"));
                contract.setAddamount(resultset.getString("ADDAMOUNT"));
                contract.setQuarter(resultset.getString("QUARTER"));
                contractList.add(contract);
            }
            if (contractList.size() > 0) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(contractList));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult GETALLCONTRACTNOTBBOK(
            final DataSource dataSource,
            final String utags,
            final String uhcfcode) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETALLCONTRACT(:utags,:uhcfcode); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("utags", utags.trim());
            statement.setString("uhcfcode", uhcfcode.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            ArrayList<Contract> contractList = new ArrayList<>();
            while (resultset.next()) {
                Contract contract = new Contract();
                if (!this.GETACR_BOOKING(dataSource, resultset.getString("CONID").trim()).isSuccess()) {
                    contract.setConid(resultset.getString("CONID"));
                    //GET ACCOUNT NAME
                    ACRGBWSResult GetFacility = this.GETFACILITYID(dataSource, resultset.getString("HCFID").trim());//GET FACILITY
                    ACRGBWSResult GetHCPN = new Methods().GETMBWITHID(dataSource, resultset.getString("HCFID").trim());
                    ACRGBWSResult GetPRO = new Methods().GetProWithPROID(dataSource, resultset.getString("HCFID").trim());
                    if (GetFacility.isSuccess()) {
                        contract.setHcfid(GetFacility.getResult());
                    } else if (GetHCPN.isSuccess()) {
                        contract.setHcfid(GetHCPN.getResult());
                    } else if (GetPRO.isSuccess()) {
                        contract.setHcfid(GetPRO.getResult());
                    } else {
                        contract.setHcfid("N/A");
                    }
                    // END OF GET ACCOUNT NAME
                    contract.setAmount(resultset.getString("AMOUNT"));
                    contract.setStats(resultset.getString("STATS"));
                    contract.setCreatedby(resultset.getString("CREATEDBY"));
                    contract.setDatecreated(dateformat.format(resultset.getTimestamp("DATECREATED")));
                    contract.setTranscode(resultset.getString("TRANSCODE"));
                    contract.setBaseamount(resultset.getString("BASEAMOUNT"));
                    contract.setRemarks(resultset.getString("REMARKS"));
                    if (resultset.getTimestamp("ENDDATE") == null) {
                        contract.setEnddate(resultset.getString("ENDDATE"));
                    } else {
                        contract.setEnddate(dateformat.format(resultset.getTimestamp("ENDDATE")));
                    }
                    //GET CONTRACT DATE PERIOD
                    ACRGBWSResult GetContractPeriod = new ContractMethod().GETCONDATEBYID(dataSource, resultset.getString("CONTRACTDATE").trim());
                    if (GetContractPeriod.isSuccess()) {
                        contract.setContractdate(GetContractPeriod.getResult());
                    } else {
                        contract.setContractdate(GetContractPeriod.getMessage());
                    }
                    //END GET CONTRACT DATE PERIOD
                    contract.setComittedClaimsVol(resultset.getString("C_CLAIMSVOL"));
                    contract.setComputedClaimsVol(resultset.getString("T_CLAIMSVOL"));
                    contract.setSb(resultset.getString("SB"));
                    contract.setAddamount(resultset.getString("ADDAMOUNT"));
                    contract.setQuarter(resultset.getString("QUARTER"));
                    contractList.add(contract);
                }
            }
            if (contractList.size() > 0) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(contractList));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET USER BY USERID
    public ACRGBWSResult GETUSERBYUSERID(
            final DataSource dataSource,
            final String puserid,
            final String utags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETUSERBYUSERID(:puserid,:utags); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("puserid", puserid.trim());
            statement.setString("utags", utags.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                User user = new User();
                user.setUserid(resultset.getString("USERID"));
                ACRGBWSResult levelresult = this.GETUSERLEVEL(dataSource, resultset.getString("LEVELID").trim());
                if (levelresult.isSuccess()) {
                    user.setLeveid(levelresult.getResult());
                } else {
                    user.setLeveid(levelresult.getMessage());
                }
                user.setUsername(resultset.getString("USERNAME"));
                user.setUserpassword(resultset.getString("USERPASSWORD"));
                user.setDatecreated(dateformat.format(resultset.getTimestamp("DATECREATED")));//resultset.getString("DATECREATED"));
                user.setStatus(resultset.getString("STATS"));
                ACRGBWSResult creators = this.GETUSERDETAILSBYDID(dataSource, resultset.getString("DID").trim(), utags);
                if (creators.isSuccess()) {
                    user.setDid(creators.getResult());
                } else {
                    user.setDid(creators.getMessage());
                }
                ACRGBWSResult creator = this.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                if (creator.isSuccess()) {
                    UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class);
                    user.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                } else {
                    user.setCreatedby(creator.getMessage());
                }
                user.setFa2(resultset.getString("FA2"));
                user.setFa2code(resultset.getString("FACODE"));
                if (resultset.getTimestamp("FAEXPIRED") == null) {
                    user.setFa2expiration(resultset.getString("FAEXPIRED"));
                } else {
                    user.setFa2expiration(datetimeformat.format(resultset.getTimestamp("FAEXPIRED")));
                }
                result.setMessage("OK");
                result.setResult(utility.ObjectMapper().writeValueAsString(user));
                result.setSuccess(true);
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET USER INFO
    public ACRGBWSResult GETUSERDETAILSBYDID(
            final DataSource dataSource,
            final String pdid,
            final String utags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETUSERDETAILSBYDID(:pdid,:utags); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("pdid", pdid.trim());
            statement.setString("utags", utags.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                UserInfo userinfo = new UserInfo();
                userinfo.setDid(resultset.getString("DID"));
                userinfo.setFirstname(resultset.getString("FIRSTNAME"));
                userinfo.setLastname(resultset.getString("LASTNAME"));
                userinfo.setMiddlename(resultset.getString("MIDDLENAME"));
                userinfo.setDatecreated(dateformat.format(resultset.getTimestamp("DATECREATED")));//resultset.getString("DATECREATED"));
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(userinfo));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //VALIDATE CONTROL NUMBER
    //GET USER INFO
    public ACRGBWSResult GETMBCONTROL(
            final DataSource dataSource,
            final String pcontrolnum) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETMBCONTROL(:pcontrolnum); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("pcontrolnum", pcontrolnum.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                ManagingBoard mb = new ManagingBoard();
                mb.setMbid(resultset.getString("MBID"));
                mb.setMbname(resultset.getString("MBNAME"));
                mb.setAddress(resultset.getString("ADDRESS"));
                mb.setBankaccount(resultset.getString("BANKACCOUNT"));
                mb.setBankname(resultset.getString("BANKNAME"));
                mb.setRemarks(resultset.getString("REMARKS"));
                mb.setDatecreated(dateformat.format(resultset.getTimestamp("DATECREATED")));
                ACRGBWSResult creator = this.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                if (creator.isSuccess()) {
                    UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class);
                    mb.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                } else {
                    mb.setCreatedby(creator.getMessage());
                }
                mb.setControlnumber(resultset.getString("CONNUMBER"));
                mb.setStatus(resultset.getString("STATUS"));
                ACRGBWSResult accreResult = this.GETACCREDITATION(dataSource, resultset.getString("CONNUMBER"));
                if (accreResult.isSuccess()) {
                    Accreditation accree = utility.ObjectMapper().readValue(accreResult.getResult(), Accreditation.class);
                    mb.setLicensedatefrom(accree.getDatefrom());
                    mb.setLicensedateto(accree.getDateto());
                } else {
                    mb.setLicensedatefrom(accreResult.getMessage());
                    mb.setLicensedateto(accreResult.getMessage());
                }
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(mb));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult GETBOOKCONTRACT(final DataSource dataSource, final String utags, final String uhcfcode) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETALLCONTRACT(:utags,:uhcfcode); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("utags", utags.trim());
            statement.setString("uhcfcode", uhcfcode.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            ArrayList<Contract> contractList = new ArrayList<>();
            while (resultset.next()) {
                Contract contract = new Contract();
                if (this.GETACR_BOOKING(dataSource, resultset.getString("CONID").trim()).isSuccess()) {
                    contract.setConid(resultset.getString("CONID"));
                    //GET ACCOUNT NAME
                    ACRGBWSResult GetFacility = this.GETFACILITYID(dataSource, resultset.getString("HCFID").trim());//GET FACILITY
                    ACRGBWSResult GetHCPN = new Methods().GETMBWITHID(dataSource, resultset.getString("HCFID").trim());
                    ACRGBWSResult GetPRO = new Methods().GetProWithPROID(dataSource, resultset.getString("HCFID").trim());
                    if (GetFacility.isSuccess()) {
                        contract.setHcfid(GetFacility.getResult());
                    } else if (GetHCPN.isSuccess()) {
                        contract.setHcfid(GetHCPN.getResult());
                    } else if (GetPRO.isSuccess()) {
                        contract.setHcfid(GetPRO.getResult());
                    } else {
                        contract.setHcfid("N/A");
                    }
                    // END OF GET ACCOUNT NAME
                    contract.setAmount(resultset.getString("AMOUNT"));
                    contract.setStats(resultset.getString("STATS"));
                    contract.setCreatedby(resultset.getString("CREATEDBY"));
                    contract.setDatecreated(dateformat.format(resultset.getTimestamp("DATECREATED")));
                    contract.setTranscode(resultset.getString("TRANSCODE"));
                    contract.setBaseamount(resultset.getString("BASEAMOUNT"));
                    contract.setRemarks(resultset.getString("REMARKS"));
                    if (resultset.getTimestamp("ENDDATE") == null) {
                        contract.setEnddate(resultset.getString("ENDDATE"));
                    } else {
                        contract.setEnddate(dateformat.format(resultset.getTimestamp("ENDDATE")));
                    }
                    //GET CONTRACT DATE PERIOD
                    ACRGBWSResult GetContractPeriod = new ContractMethod().GETCONDATEBYID(dataSource, resultset.getString("CONTRACTDATE").trim());
                    if (GetContractPeriod.isSuccess()) {
                        contract.setContractdate(GetContractPeriod.getResult());
                    }
                    //END GET CONTRACT DATE PERIOD
                    contract.setComittedClaimsVol(resultset.getString("C_CLAIMSVOL"));
                    contract.setComputedClaimsVol(resultset.getString("T_CLAIMSVOL"));
                    contract.setSb(resultset.getString("SB"));
                    contract.setAddamount(resultset.getString("ADDAMOUNT"));
                    contract.setQuarter(resultset.getString("QUARTER"));
                    contractList.add(contract);
                }
            }
            if (contractList.size() > 0) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(contractList));
            } else {
                result.setMessage("N/A");
            }

        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //COUNT USERNAME USING 
    public ACRGBWSResult COUNTUSERNAME(final DataSource dataSource, final String pusername) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.COUNTUSERNAME(:pusername); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("pusername", pusername.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(resultset.getString("SUMUSER"));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //COUNT USERNAME USING 
    public ACRGBWSResult GETLEVELBYLEVNAME(final DataSource dataSource, final String plevname) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETLEVELBYLEVNAME(:plevname); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("plevname", plevname.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                UserLevel ulevel = new UserLevel();
                ulevel.setLevelid(resultset.getString("LEVELID"));
                ulevel.setLevname(resultset.getString("LEVNAME"));
                ulevel.setLevdetails(resultset.getString("LEVDETAILS"));
                ulevel.setStats(resultset.getString("STATS"));
                result.setMessage(resultset.getString("LEVELID"));
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(ulevel));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET TRANCH USING TRANCHID
    public ACRGBWSResult VALIDATERECIEPT(
            final DataSource dataSource,
            final String utags,
            final String ureciept) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.VALIDATERECEIPTNUMBER(:utags,:ureciept); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("utags", utags.trim().toUpperCase());
            statement.setString("ureciept", ureciept.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                result.setMessage("OK");
                result.setSuccess(true);
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
