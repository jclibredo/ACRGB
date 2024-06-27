/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.method;

import acrgb.structure.ACRGBWSResult;
import acrgb.structure.Accreditation;
import acrgb.structure.Assets;
import acrgb.structure.Contract;
import acrgb.structure.ContractDate;
import acrgb.structure.DateSettings;
import acrgb.structure.FacilityComputedAmount;
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
    // private final ContractMethod contractmethod = new ContractMethod();
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

    //GET ALL FACILITY(MULTIPLE)
    public ACRGBWSResult GETALLFACILITY(final DataSource dataSource, final String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Methods methods = new Methods();
        try (Connection connection = dataSource.getConnection()) {
            //---------------------------------------------------------------
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKG.ACR_HCF(); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            ArrayList<HealthCareFacility> hcflist = new ArrayList<>();
            while (resultset.next()) {
                HealthCareFacility hcf = new HealthCareFacility();
                hcf.setHcfname(resultset.getString("HCFNAME"));
                hcf.setHcfaddress(resultset.getString("HCFADDRESS"));
                hcf.setHcfcode(resultset.getString("HCFCODE"));
                hcf.setHcilevel(resultset.getString("HCILEVEL"));
                hcf.setType(resultset.getString("HCFTYPE"));
                //-------------------------------------------------
                if (resultset.getString("HCFTYPE").equals("AH")) {
                    //GETAPPELLATE
                    ACRGBWSResult restA = this.GETAPPELLATE(dataSource, resultset.getString("HCFCODE"), tags.toUpperCase().trim());
                    List<String> hcpnlist = Arrays.asList(restA.getResult().split(","));
                    ArrayList<String> mblist = new ArrayList<>();
                    ArrayList<String> prolist = new ArrayList<>();
                    for (int h = 0; h < hcpnlist.size(); h++) {
                        ACRGBWSResult mgresult = methods.GETMBWITHID(dataSource, hcpnlist.get(h));
                        if (mgresult.isSuccess()) {
                            ManagingBoard mb = utility.ObjectMapper().readValue(mgresult.getResult(), ManagingBoard.class);
                            mblist.add(mb.getControlnumber());
                            ACRGBWSResult restC = methods.GETROLEREVERESE(dataSource, mb.getControlnumber(), tags);
                            if (restC.isSuccess()) {
                                //GET PRO USING PROID
                                ACRGBWSResult getproid = methods.GetProWithPROID(dataSource, restC.getResult());
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
                    ACRGBWSResult methodsresult = methods.GETROLEREVERESE(dataSource, resultset.getString("HCFCODE"), tags);
                    if (methodsresult.isSuccess()) {
                        ACRGBWSResult mgresult = methods.GETMBWITHID(dataSource, methodsresult.getResult());
                        if (mgresult.isSuccess()) {
                            ManagingBoard mb = utility.ObjectMapper().readValue(mgresult.getResult(), ManagingBoard.class);
                            hcf.setMb(mb.getControlnumber());
                            ACRGBWSResult restC = methods.GETROLEREVERESE(dataSource, mb.getControlnumber(), tags);
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
                }

                hcf.setAmount("N/A");
                hcf.setRemainingbalance("N/A");
                hcflist.add(hcf);
            }
            if (hcflist.size() > 0) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(hcflist));
            } else {
                result.setMessage("N/A");
            }
            //----------------------------------------------------------------------

        } catch (SQLException | IOException | ParseException ex) {
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
        Methods methods = new Methods();
        ArrayList<HealthCareFacility> hcflist = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            ACRGBWSResult restAA = methods.GETROLE(dataSource, puserid, tags.toUpperCase().trim());
            if (restAA.isSuccess()) {
                ACRGBWSResult restBB = methods.GETROLEMULITPLE(dataSource, restAA.getResult(), tags.toUpperCase().trim());
                if (restBB.isSuccess()) {
                    List<String> FacilityList = Arrays.asList(restBB.getResult().split(","));
                    for (int v = 0; v < FacilityList.size(); v++) {
                        //---------------------------------------------------------------
                        CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKG.getfacility(:hcfrid); end;");
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
                                ACRGBWSResult restA = this.GETAPPELLATE(dataSource, resultset.getString("HCFCODE"), tags.toUpperCase().trim());
                                List<String> hcpnlist = Arrays.asList(restA.getResult().split(","));
                                ArrayList<String> mblist = new ArrayList<>();
                                ArrayList<String> prolist = new ArrayList<>();
                                for (int h = 0; h < hcpnlist.size(); h++) {
                                    ACRGBWSResult mgresult = methods.GETMBWITHID(dataSource, hcpnlist.get(h));
                                    if (mgresult.isSuccess()) {
                                        ManagingBoard mb = utility.ObjectMapper().readValue(mgresult.getResult(), ManagingBoard.class);
                                        mblist.add(mb.getControlnumber());
                                        ACRGBWSResult restC = methods.GETROLEREVERESE(dataSource, mb.getControlnumber(), tags.toUpperCase().trim());
                                        if (restC.isSuccess()) {
                                            //GET PRO USING PROID
                                            ACRGBWSResult getproid = methods.GetProWithPROID(dataSource, restC.getResult());
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
                                ACRGBWSResult methodsresult = methods.GETROLEREVERESE(dataSource, resultset.getString("HCFCODE"), tags.toUpperCase().trim());
                                if (methodsresult.isSuccess()) {
                                    ACRGBWSResult mgresult = methods.GETMBWITHID(dataSource, methodsresult.getResult());
                                    if (mgresult.isSuccess()) {
                                        ManagingBoard mb = utility.ObjectMapper().readValue(mgresult.getResult(), ManagingBoard.class);
                                        hcf.setMb(mb.getControlnumber());
                                        ACRGBWSResult restC = methods.GETROLEREVERESE(dataSource, mb.getControlnumber(), tags.toUpperCase().trim());
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
                            }
                            //GET FACILITY BUDGET BASE AMOUNT

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
        } catch (SQLException | IOException | ParseException ex) {
            result.setMessage(ex.toString());
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
                    userole.setCreatedby(creator.getMessage());
                }
                userole.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));//resultset.getString("DATECREATED"));
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
        Methods methods = new Methods();
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
                    ACRGBWSResult getHCPN = methods.GETMBWITHID(dataSource, resultset.getString("HCFID"));
                    if (getHCPN.isSuccess()) {
                        assets.setHcfid(getHCPN.getResult());
                    } else {
                        assets.setHcfid(getHCPN.getMessage());
                    }
                }
                assets.setDatereleased(dateformat.format(resultset.getDate("DATERELEASED")));//resultset.getDate("DATERELEASED"));
                assets.setReceipt(resultset.getString("RECEIPT"));
                assets.setReleasedamount(resultset.getString("RELEASEDAMOUNT"));
                assets.setPreviousbalance(resultset.getString("PREVIOUSBAL"));
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
                    assets.setCreatedby("N/A");
                }
                assets.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));
                ACRGBWSResult getcon = this.GETCONTRACTCONID(dataSource, resultset.getString("CONID").trim(), tags);
                if (getcon.isSuccess()) {
                    if (!getcon.getResult().isEmpty()) {
                        assets.setConid(getcon.getResult());
                    } else {
                        assets.setConid(getcon.getMessage());
                    }
                } else {
                    assets.setCreatedby("N/A");
                }
                assets.setStatus(resultset.getString("STATS"));
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

    //GET CONTRACT OF APEX FACILITY
    public ACRGBWSResult ACR_CONTRACT(final DataSource dataSource, final String tags, final String userid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Methods methods = new Methods();
        ContractMethod contractmethod = new ContractMethod();
        try (Connection connection = dataSource.getConnection()) {
            ArrayList<Contract> contractlist = new ArrayList<>();
            ArrayList<String> hcflist = new ArrayList<>();
            //-------------- GET APEX FACILITY
            ACRGBWSResult resultfm = methods.GETAPEXFACILITY(dataSource);
            if (resultfm.isSuccess()) {
                List<HealthCareFacility> userlist = Arrays.asList(utility.ObjectMapper().readValue(resultfm.getResult(), HealthCareFacility[].class));
                for (int x = 0; x < userlist.size(); x++) {
                    hcflist.add(userlist.get(x).getHcfcode());
                }
            }   //-------------- END OF GET APEX FACILITY
            for (int y = 0; y < hcflist.size(); y++) {
                //--------------------------------------------------------
                CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.ACR_CONTRACT(:tags,:pfchid); end;");
                statement.registerOutParameter("v_result", OracleTypes.CURSOR);
                statement.setString("tags", tags);
                statement.setString("pfchid", hcflist.get(y));
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
                    contract.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));//resultset.getString("DATECREATED"));
                    ACRGBWSResult getcondateA = contractmethod.GETCONDATEBYID(dataSource, resultset.getString("CONTRACTDATE"));
                    contract.setContractdate(getcondateA.getResult());
                    contract.setTranscode(resultset.getString("TRANSCODE"));
                    contract.setBaseamount(resultset.getString("BASEAMOUNT"));
                    contract.setComittedClaimsVol(resultset.getString("C_CLAIMSVOL"));
                    contract.setComputedClaimsVol(resultset.getString("T_CLAIMSVOL"));
                    contract.setSb(resultset.getString("SB"));
                    contract.setAddamount(resultset.getString("ADDAMOUNT"));
                    contract.setQuarter(resultset.getString("QUARTER"));
                    //=============================================
                    ContractDate condate = utility.ObjectMapper().readValue(getcondateA.getResult(), ContractDate.class);
                    int numberofclaims = 0;
                    double totalclaimsamount = 0.00;
                    double percentageA = 0.00;
                    double percentageB = 0.00;
                    double trancheamount = 0.00;
                    int tranches = 0;
                    //GET TRANCHE SUMMARY 
                    ACRGBWSResult totalResult = methods.GETSUMMARY(dataSource, hcflist.get(y));
                    if (totalResult.isSuccess()) {
                        Total getResult = utility.ObjectMapper().readValue(totalResult.getResult(), Total.class);
                        tranches += Integer.parseInt(getResult.getCcount());
                        trancheamount += Double.parseDouble(getResult.getCtotal());
                    }
                    //GET CLAIMS SUMMARY OF FACILITY UNDER NETWORK
                    ACRGBWSResult sumresult = this.GETNCLAIMS(dataSource, hcflist.get(y), "G",
                            condate.getDatefrom(), condate.getDateto(), "CURRENTSTATUS");
                    if (sumresult.isSuccess()) {
                        List<NclaimsData> nclaimsdata = Arrays.asList(utility.ObjectMapper().readValue(sumresult.getResult(), NclaimsData[].class));
                        for (int i = 0; i < nclaimsdata.size(); i++) {
                            numberofclaims += Integer.parseInt(nclaimsdata.get(i).getTotalclaims());
                            totalclaimsamount += Double.parseDouble(nclaimsdata.get(i).getClaimamount());
                        }
                    }
                    double sumsA = trancheamount / Double.parseDouble(resultset.getString("AMOUNT")) * 100;
                    if (sumsA > 100) {
                        double negvalue = 100 - sumsA;
                        percentageA += negvalue;
                    } else {
                        percentageA += sumsA;
                    }
                    //----------------
                    double sumsB = totalclaimsamount / trancheamount * 100;
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

// GET CONTRACT USING PRO USERID
    public ACRGBWSResult ACR_CONTRACTPROID(final DataSource dataSource, final String tags, final String userid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Methods methods = new Methods();
        try {
            ArrayList<Contract> contractlist = new ArrayList<>();
            ACRGBWSResult restA = methods.GETROLE(dataSource, userid, "ACTIVE");//GET (PROID) USING (USERID)
            if (restA.isSuccess()) {
                ACRGBWSResult restB = methods.GETROLEMULITPLE(dataSource, restA.getResult(), tags);//GET (NCPN) USING (PROID)          
                List<String> restist = Arrays.asList(restB.getResult().split(","));
                if (restB.isSuccess()) {
                    for (int b = 0; b < restist.size(); b++) {
                        //--------------------------------------------------------
                        ACRGBWSResult getcon = this.GetHCPNSingleContract(dataSource, tags, restist.get(b));
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
        } catch (IOException | ParseException ex) {
            result.setMessage(ex.toString());
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
        // Methods methods = new Methods();
        try {
            ArrayList<Contract> contractlist = new ArrayList<>();
            ACRGBWSResult mblist = this.GetManagingBoard(dataSource, "ACTIVE");
            if (mblist.isSuccess()) {
                List<ManagingBoard> mblistresult = Arrays.asList(utility.ObjectMapper().readValue(mblist.getResult(), ManagingBoard[].class));
                for (int x = 0; x < mblistresult.size(); x++) {
                    ACRGBWSResult getmbcon = this.GetHCPNSingleContract(dataSource, tags, mblistresult.get(x).getControlnumber());
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

        } catch (IOException | ParseException ex) {
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
        ContractMethod contractmethod = new ContractMethod();
        try (Connection connection = dataSource.getConnection()) {
            ArrayList<Contract> contractlist = new ArrayList<>();
            ACRGBWSResult reatA = methods.GETROLE(dataSource, userid, "ACTIVE");
            if (reatA.isSuccess()) {
                //--------------------------------------------------------
                CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.ACR_CONTRACT(:tags,:pfchid); end;");
                statement.registerOutParameter("v_result", OracleTypes.CURSOR);
                statement.setString("tags", tags);
                statement.setString("pfchid", reatA.getResult());
                statement.execute();
                ResultSet resultset = (ResultSet) statement.getObject("v_result");
                if (resultset.next()) {
                    Contract contract = new Contract();
                    contract.setConid(resultset.getString("CONID"));
                    ACRGBWSResult facility = methods.GETMBWITHID(dataSource, resultset.getString("HCFID"));
                    if (facility.isSuccess()) {
                        ManagingBoard mb = utility.ObjectMapper().readValue(facility.getResult(), ManagingBoard.class);
                        contract.setHcfid(mb.getMbname());
                    } else {
                        contract.setHcfid("N/A");
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
                        contract.setCreatedby("N/A");
                    }
                    contract.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));
                    ACRGBWSResult getcondateA = contractmethod.GETCONDATEBYID(dataSource, resultset.getString("CONTRACTDATE"));
                    if (getcondateA.isSuccess()) {
                        contract.setContractdate(getcondateA.getResult());
                    } else {
                        contract.setContractdate(getcondateA.getMessage());
                    }
                    contract.setBaseamount(resultset.getString("BASEAMOUNT"));
                    contract.setComittedClaimsVol(resultset.getString("C_CLAIMSVOL"));
                    contract.setComputedClaimsVol(resultset.getString("T_CLAIMSVOL"));
                    contract.setSb(resultset.getString("SB"));
                    contract.setAddamount(resultset.getString("ADDAMOUNT"));
                    contract.setQuarter(resultset.getString("QUARTER"));
                    ContractDate condate = utility.ObjectMapper().readValue(getcondateA.getResult(), ContractDate.class);

                    //=============================================
                    int numberofclaims = 0;
                    double totalclaimsamount = 0.00;
                    double percentageA = 0.00;
                    double percentageB = 0.00;
                    double trancheamount = 0.00;
                    int tranches = 0;
                    ACRGBWSResult GetAccessRoleList = methods.GETROLEMULITPLE(dataSource, reatA.getResult(), tags);
                    List<String> hcflist = Arrays.asList(GetAccessRoleList.getResult().split(","));
                    for (int y = 0; y < hcflist.size(); y++) {
                        //GET TRANCHE SUMMARY
                        ACRGBWSResult totalResult = methods.GETSUMMARY(dataSource, hcflist.get(y));
                        if (totalResult.isSuccess()) {
                            Total getResult = utility.ObjectMapper().readValue(totalResult.getResult(), Total.class);
                            tranches += Integer.parseInt(getResult.getCcount());
                            trancheamount += Double.parseDouble(getResult.getCtotal());
                        }
                        //GET CLAIMS AMOUNT OF FACILITY UNDER SELECTED NETWORK
                        ACRGBWSResult sumresult = this.GETNCLAIMS(dataSource, hcflist.get(y), "G",
                                condate.getDatefrom(), condate.getDateto(), "CURRENTSTATUS");
                        if (sumresult.isSuccess()) {
                            List<NclaimsData> nclaimsdata = Arrays.asList(utility.ObjectMapper().readValue(sumresult.getResult(), NclaimsData[].class));
                            for (int i = 0; i < nclaimsdata.size(); i++) {
                                numberofclaims += Integer.parseInt(nclaimsdata.get(i).getTotalclaims());
                                totalclaimsamount += Double.parseDouble(nclaimsdata.get(i).getClaimamount());
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
                    double sumsB = totalclaimsamount / trancheamount * 100;
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
                    //=============================================
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
    //userid =0
    public ACRGBWSResult GETCONTRACTCONID(final DataSource dataSource,
            final String pconid,
            final String utags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ContractMethod contractmethod = new ContractMethod();
        try (Connection connection = dataSource.getConnection()) {
            //--------------------------------------------------------
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETCONTRACTCONID(:pconid,:utags); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("pconid", pconid);
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
                contract.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));//resultset.getString("DATECREATED"));
                ACRGBWSResult getcondateA = contractmethod.GETCONDATEBYID(dataSource, resultset.getString("CONTRACTDATE"));
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
    public ACRGBWSResult GetFacilityContractUsingHCPNAccountUserID(final DataSource dataSource, final String tags, final String userid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Methods methods = new Methods();
        ContractMethod contractmethod = new ContractMethod();
        try (Connection connection = dataSource.getConnection()) {
            ArrayList<Contract> contractlist = new ArrayList<>();
            ACRGBWSResult restAA = methods.GETROLE(dataSource, userid, "ACTIVE");
            if (restAA.isSuccess()) {
                //-------------- GET APEX FACILITY
                ACRGBWSResult restA = methods.GETROLEMULITPLE(dataSource, restAA.getResult(), tags);
                List<String> hcflist = Arrays.asList(restA.getResult().split(","));
                //-------------- END OF GET APEX FACILITY
                for (int y = 0; y < hcflist.size(); y++) {
                    //--------------------------------------------------------
                    CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.ACR_CONTRACT(:tags,:pfchid); end;");
                    statement.registerOutParameter("v_result", OracleTypes.CURSOR);
                    statement.setString("tags", tags);
                    statement.setString("pfchid", hcflist.get(y));
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
                        contract.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));//resultset.getString("DATECREATED"));
                        ACRGBWSResult getcondateA = contractmethod.GETCONDATEBYID(dataSource, resultset.getString("CONTRACTDATE"));
                        contract.setContractdate(getcondateA.getResult());
                        ContractDate condate = utility.ObjectMapper().readValue(getcondateA.getResult(), ContractDate.class);
                        contract.setTranscode(resultset.getString("TRANSCODE"));
                        contract.setBaseamount(resultset.getString("BASEAMOUNT"));
                        contract.setComittedClaimsVol(resultset.getString("C_CLAIMSVOL"));
                        contract.setComputedClaimsVol(resultset.getString("T_CLAIMSVOL"));
                        contract.setSb(resultset.getString("SB"));
                        contract.setAddamount(resultset.getString("ADDAMOUNT")); //=============================================
                        contract.setQuarter(resultset.getString("QUARTER"));
                        int numberofclaims = 0;
                        double totalclaimsamount = 0.00;
                        double percentageA = 0.00;
                        double percentageB = 0.00;
                        double trancheamount = 0.00;
                        int tranches = 0;
                        ACRGBWSResult totalResult = methods.GETSUMMARY(dataSource, hcflist.get(y));
                        if (totalResult.isSuccess()) {
                            Total getResult = utility.ObjectMapper().readValue(totalResult.getResult(), Total.class);
                            tranches += Integer.parseInt(getResult.getCcount());
                            trancheamount += Double.parseDouble(getResult.getCtotal());
                        }
                        //======================================
                        ACRGBWSResult sumresult = this.GETNCLAIMS(dataSource, hcflist.get(y), "G",
                                condate.getDatefrom(), condate.getDateto(), "CURRENTSTATUS");
                        if (sumresult.isSuccess()) {
                            List<NclaimsData> nclaimsdata = Arrays.asList(utility.ObjectMapper().readValue(sumresult.getResult(), NclaimsData[].class));
                            for (int i = 0; i < nclaimsdata.size(); i++) {
                                numberofclaims += Integer.parseInt(nclaimsdata.get(i).getTotalclaims());
                                totalclaimsamount += Double.parseDouble(nclaimsdata.get(i).getClaimamount());
                            }
                        }
                        //=============================================
                        double sumsA = trancheamount / Double.parseDouble(resultset.getString("AMOUNT")) * 100;
                        if (sumsA > 100) {
                            double negvalue = 100 - sumsA;
                            percentageA += negvalue;
                        } else {
                            percentageA += sumsA;
                        }
                        //----------------
                        double sumsB = totalclaimsamount / trancheamount * 100;
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
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKG.ACR_HCF(); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            ArrayList<HealthCareFacility> listHCF = new ArrayList<>();
            while (resultset.next()) {
                HealthCareFacility hcf = new HealthCareFacility();
                hcf.setHcfname(resultset.getString("HCFNAME"));
                hcf.setHcfaddress(resultset.getString("HCFADDRESS"));
                hcf.setHcfcode(resultset.getString("HCFCODE"));
                hcf.setType(resultset.getString("HCFTYPE"));
                hcf.setHcilevel(resultset.getString("HCILEVEL"));
                listHCF.add(hcf);
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
                    UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class);
                    tranch.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                } else {
                    tranch.setCreatedby(creator.getMessage());
                }
                tranch.setStats(resultset.getString("STATS"));
                tranch.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));//resultset.getString("DATECREATED"));
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
                userinfo.setEmail(resultset.getString("EMAIL"));
                userinfo.setContact(resultset.getString("CONTACT"));
                userinfo.setLastname(resultset.getString("LASTNAME"));
                userinfo.setMiddlename(resultset.getString("MIDDLENAME"));
                userinfo.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));//resultset.getString("DATECREATED"));
                // userinfo.setAreaid(resultset.getString("AREAID"));
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
                    UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class);
                    userlevel.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                } else {
                    userlevel.setCreatedby(creator.getMessage());
                }
                userlevel.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));//resultset.getString("DATECREATED"));
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
        Methods methods = new Methods();
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKG.ACR_PRO(); end;");
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
                double utilize = 0.00;
                double contractamount = 0.00;
                ACRGBWSResult getProFund = this.GETCONBYCODE(dataSource, "2024" + resultset.getString("PROCODE").trim());
                if (getProFund.isSuccess()) {
                    Contract contract = utility.ObjectMapper().readValue(getProFund.getResult(), Contract.class);
                    contractamount += Double.parseDouble(contract.getAmount());
                    pro.setConamount(contract.getBaseamount());
                    ACRGBWSResult restA = methods.GETROLEMULITPLE(dataSource, "2024" + resultset.getString("PROCODE").trim(), tags);
                    List<String> hcpncodeList = Arrays.asList(restA.getResult().split(","));
                    for (int x = 0; x < hcpncodeList.size(); x++) {
                        ACRGBWSResult getProbudget = this.GETCONBYCODE(dataSource, hcpncodeList.get(x));
                        if (getProbudget.isSuccess()) {
                            Contract con = utility.ObjectMapper().readValue(getProbudget.getResult(), Contract.class);
                            //GET TRANCH PER NETWORK
                            ACRGBWSResult tranche = this.GETASSETSBYCONID(dataSource, con.getConid());
                            if (tranche.isSuccess()) {
                                List<Assets> assetsList = Arrays.asList(utility.ObjectMapper().readValue(tranche.getResult(), Assets[].class));
                                for (int p = 0; p < assetsList.size(); p++) {
                                    utilize += Double.parseDouble(assetsList.get(p).getAmount());
                                }
                            }
                        }
                    }
                    double totalper = contractamount / utilize * 100;
                    pro.setUtilize(String.valueOf(utilize));
                    pro.setUnutilize(String.valueOf(contractamount - utilize));
                    pro.setPercentage(String.valueOf(totalper));
                    pro.setContractamount(contract.getAmount());
                    pro.setTranscode(contract.getTranscode());
                } else {
                    pro.setUtilize("N/A");
                    pro.setUnutilize("N/A");
                    pro.setPercentage("N/A");
                    pro.setContractamount("N/A");
                    pro.setTranscode("N/A");
                }
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
                result.setMessage("N/A");
            }

        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
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
    public ACRGBWSResult GETASSETSWITHPARAM(final DataSource dataSource, final String phcfid, final String conid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETASSETSBYHCFID(:phcfid,:pconid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("phcfid", phcfid);
            statement.setString("pconid", conid);
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
                    if (!creator.getResult().isEmpty()) {
                        UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class);
                        assets.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                    } else {
                        assets.setCreatedby(creator.getMessage());
                    }
                } else {
                    assets.setCreatedby(creator.getMessage());
                }
                assets.setDatereleased(dateformat.format(resultset.getDate("DATERELEASED")));//resultset.getString("DATERELEASED"));
                assets.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));//resultset.getString("DATECREATED"));
                assetslist.add(assets);
            }
            if (!assetslist.isEmpty()) {
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
            if (!logslist.isEmpty()) {
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
    public ACRGBWSResult GETNCLAIMS(final DataSource dataSource, final String u_accreno, final String u_tags,
            final String u_from, final String u_to, String reqtype) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            ArrayList<NclaimsData> claimsList = new ArrayList<>();
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKG.GETNCLAIMS(:u_accreno,:u_tags,:u_from,:u_to); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("u_accreno", u_accreno);
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
                            nclaimsdata.setAccreno(resultset.getString("PMCC_NO"));
                            nclaimsdata.setClaimamount(resultset.getString("CTOTAL"));
                            nclaimsdata.setTotalclaims(resultset.getString("COUNTVAL"));
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
    public ACRGBWSResult GetManagingBoard(final DataSource dataSource, final String tags) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Methods methods = new Methods();
        try (Connection connection = dataSource.getConnection()) {
            ArrayList<ManagingBoard> mblist = new ArrayList<>();
            //-------------------------------------------------------
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETMB(:tags); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("tags", tags.toUpperCase());
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
                mb.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));
                //System.out.println("Date From DB : "+dateformat.format(resultset.getDate("DATECREATED")));
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
                ACRGBWSResult getPro = methods.GETROLEREVERESEMULTIPLE(dataSource, resultset.getString("CONNUMBER"), tags);
                if (getPro.isSuccess()) {
                    // GetProWithPROID
                    List<String> accesslist = Arrays.asList(getPro.getResult().split(","));
                    for (int u = 0; u < accesslist.size(); u++) {
                        ACRGBWSResult getProValue = methods.GetProWithPROID(dataSource, accesslist.get(u));
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

    public ACRGBWSResult GETROLETWOPARAM(final DataSource dataSource, final String puserid,
            final String paccessid, final String utags) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETROLETWOPARAM(:utags,:puserid,:paccessid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("utags", utags);
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
                result.setMessage("N/A");
            }

        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET TRANCH USING TRANCHID
    public ACRGBWSResult ACR_TRANCHWITHID(final DataSource dataSource, final String ptranchid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.ACR_TRANCHWITHID(:ptranchid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("ptranchid", ptranchid);
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
    public ACRGBWSResult GetHCPNSingleContract(final DataSource dataSource, final String tags, final String pcode) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Methods methods = new Methods();
        ContractMethod contractmethod = new ContractMethod();
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.ACR_CONTRACT(:tags,:pfchid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("tags", tags);
            statement.setString("pfchid", pcode);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                Contract contract = new Contract();
                contract.setConid(resultset.getString("CONID"));
                ACRGBWSResult facility = methods.GETMBWITHID(dataSource, resultset.getString("HCFID"));
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
                contract.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));//resultset.getString("DATECREATED"));
                ACRGBWSResult getcondateA = contractmethod.GETCONDATEBYID(dataSource, resultset.getString("CONTRACTDATE"));
                ContractDate condate = utility.ObjectMapper().readValue(getcondateA.getResult(), ContractDate.class);
                contract.setContractdate(getcondateA.getResult());
                contract.setBaseamount(resultset.getString("BASEAMOUNT"));
                contract.setTranscode(resultset.getString("TRANSCODE"));
                contract.setComittedClaimsVol(resultset.getString("C_CLAIMSVOL"));
                contract.setComputedClaimsVol(resultset.getString("T_CLAIMSVOL"));
                contract.setSb(resultset.getString("SB"));
                contract.setAddamount(resultset.getString("ADDAMOUNT"));
                contract.setQuarter(resultset.getString("QUARTER"));
                //SET OF NUMBER OF CLAIMS AND TRANCHES MOVEMENT
                int numberofclaims = 0;
                double totalclaimsamount = 0.00;
                double percentageA = 0.00;
                double percentageB = 0.00;
                double trancheamount = 0.00;
                int tranches = 0;
                //GET TRANCHE AMOUNT
                ACRGBWSResult totalResult = methods.GETSUMMARY(dataSource, pcode);
                if (totalResult.isSuccess()) {
                    Total getResult = utility.ObjectMapper().readValue(totalResult.getResult(), Total.class);
                    tranches += Integer.parseInt(getResult.getCcount());
                    trancheamount += Double.parseDouble(getResult.getCtotal());
                }
                //GET CLAIMS AMOUNT AND CLAIMS OF FACILITY UNDER HCPN SELECTED
                ACRGBWSResult restA = methods.GETROLEMULITPLE(dataSource, resultset.getString("HCFID"), tags);
                if (restA.isSuccess()) {
                    List<String> hcflist = Arrays.asList(restA.getResult().split(","));
                    for (int v = 0; v < hcflist.size(); v++) {
                        //--------------------------
                        ACRGBWSResult sumresult = this.GETNCLAIMS(dataSource, hcflist.get(v), "G".toUpperCase(), condate.getDatefrom(), condate.getDateto(),
                                "CURRENTSTATUS");
                        if (sumresult.isSuccess()) {
                            List<NclaimsData> nclaimsdata = Arrays.asList(utility.ObjectMapper().readValue(sumresult.getResult(), NclaimsData[].class));
                            for (int i = 0; i < nclaimsdata.size(); i++) {
                                numberofclaims += Integer.parseInt(nclaimsdata.get(i).getTotalclaims());
                                totalclaimsamount += Double.parseDouble(nclaimsdata.get(i).getClaimamount());
                            }
                        }
                    }
                }
                double sumsA = trancheamount / Double.parseDouble(resultset.getString("AMOUNT")) * 100;
                if (sumsA > 100) {
                    double negvalue = 100 - sumsA;
                    percentageA += negvalue;
                } else {
                    percentageA += sumsA;
                }
                //----------------
                double sumsB = totalclaimsamount / trancheamount * 100;
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
                result.setMessage("OK");
                result.setResult(utility.ObjectMapper().writeValueAsString(contract));
                result.setSuccess(true);
            } else {
                result.setMessage("N/A");
            }

        } catch (Exception ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }

    //GET TERMINATED CONTRACT SINGLE RESULT
    public ACRGBWSResult GetTerminateContract(final DataSource dataSource, final String pcode) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Methods methods = new Methods();
        ContractMethod contractmethod = new ContractMethod();
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETTERMINATECON(:pan); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("pan", pcode);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                Contract contract = new Contract();
                contract.setAmount(resultset.getString("AMOUNT"));
                contract.setBaseamount(resultset.getString("BASEAMOUNT"));
                contract.setConid(resultset.getString("CONID"));
                ACRGBWSResult creator = this.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                if (creator.isSuccess()) {
                    contract.setCreatedby(creator.getResult());
                } else {
                    contract.setCreatedby(creator.getMessage());
                }
                contract.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));
                ACRGBWSResult getcondateA = contractmethod.GETCONDATEBYID(dataSource, resultset.getString("CONTRACTDATE"));
                if (getcondateA.isSuccess()) {
                    contract.setContractdate(getcondateA.getResult());
                } else {
                    contract.setContractdate(getcondateA.getMessage());
                }

                contract.setEnddate(dateformat.format(resultset.getDate("ENDDATE")));
                ACRGBWSResult facility = this.GETFACILITYID(dataSource, resultset.getString("HCFID"));
                if (facility.isSuccess()) {
                    HealthCareFacility hcf = utility.ObjectMapper().readValue(facility.getResult(), HealthCareFacility.class);
                    contract.setHcfid(facility.getResult());
                    ACRGBWSResult GetTotalClaimsAmount = methods.GetAmount(dataSource,
                            hcf.getHcfcode(),
                            dateformat.format(resultset.getDate("DATEFROM")),
                            dateformat.format(resultset.getDate("ENDDATE")));
                    if (GetTotalClaimsAmount.isSuccess()) {
                        FacilityComputedAmount fca = utility.ObjectMapper().readValue(GetTotalClaimsAmount.getResult(), FacilityComputedAmount.class);
                        fca.getTotalclaims();
                        fca.getTotalamount();
                        Double conAmount = Double.parseDouble(resultset.getString("AMOUNT"));
                        Double GoodClaimsAmount = Double.parseDouble(fca.getTotalamount());
                        Double product = conAmount - GoodClaimsAmount;
                        contract.setTotalclaims(fca.getTotalclaims());
                        contract.setRemainingbalance(String.valueOf(product));
                    } else {
                        contract.setTotalclaims("N/A");
                        contract.setRemainingbalance(resultset.getString("AMOUNT"));
                    }
                }
                contract.setRemarks(resultset.getString("REMARKS"));
                contract.setStats(resultset.getString("STATS"));
                contract.setTranscode(resultset.getString("TRANSCODE"));
                //GET COUNT OF TRANCHES RELEASED
                ACRGBWSResult Assets = this.GETASSETSWITHPARAM(dataSource, resultset.getString("HCFID"), resultset.getString("CONID"));
                if (Assets.isSuccess()) {
                    List<Assets> assetsList = Arrays.asList(utility.ObjectMapper().readValue(Assets.getResult(), Assets[].class));
                    contract.setTraches(String.valueOf(assetsList.size()));
                } else {
                    contract.setTraches("NO TRANCH RELEASED");
                }
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(contract));
            } else {
                result.setMessage("N/A");
            }
        } catch (Exception ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }

    //GET END OR NONRENEW CONTRACT SINGLE RESULT
    public ACRGBWSResult GetEndContract(final DataSource dataSource, final String pcode) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Methods methods = new Methods();
        ContractMethod contractmethod = new ContractMethod();
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETENDCON(:pan); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("pan", pcode);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                Contract contract = new Contract();
                contract.setAmount(resultset.getString("AMOUNT"));
                contract.setBaseamount(resultset.getString("BASEAMOUNT"));
                contract.setConid(resultset.getString("CONID"));
                ACRGBWSResult creator = this.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                if (creator.isSuccess()) {
                    contract.setCreatedby(creator.getResult());
                } else {
                    contract.setCreatedby(creator.getMessage());
                }
                contract.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));

                ACRGBWSResult getcondateA = contractmethod.GETCONDATEBYID(dataSource, resultset.getString("CONTRACTDATE"));
                if (getcondateA.isSuccess()) {
                    contract.setContractdate(getcondateA.getResult());
                } else {
                    contract.setContractdate(getcondateA.getMessage());
                }

                contract.setEnddate(dateformat.format(resultset.getDate("ENDDATE")));
                ACRGBWSResult facility = this.GETFACILITYID(dataSource, resultset.getString("HCFID"));
                if (facility.isSuccess()) {
                    HealthCareFacility hcf = utility.ObjectMapper().readValue(facility.getResult(), HealthCareFacility.class);
                    contract.setHcfid(facility.getResult());
                    ACRGBWSResult GetTotalClaimsAmount = methods.GetAmount(dataSource,
                            hcf.getHcfcode(),
                            dateformat.format(resultset.getDate("DATEFROM")),
                            dateformat.format(resultset.getDate("ENDDATE")));
                    if (GetTotalClaimsAmount.isSuccess()) {
                        FacilityComputedAmount fca = utility.ObjectMapper().readValue(GetTotalClaimsAmount.getResult(), FacilityComputedAmount.class);
                        fca.getTotalclaims();
                        fca.getTotalamount();
                        Double conAmount = Double.parseDouble(resultset.getString("AMOUNT"));
                        Double GoodClaimsAmount = Double.parseDouble(fca.getTotalamount());
                        Double product = conAmount - GoodClaimsAmount;
                        contract.setTotalclaims(fca.getTotalclaims());
                        contract.setRemainingbalance(String.valueOf(product));
                    } else {
                        contract.setTotalclaims("N/A");
                        contract.setRemainingbalance(resultset.getString("AMOUNT"));
                    }
                }
                contract.setRemarks(resultset.getString("REMARKS"));
                contract.setStats(resultset.getString("STATS"));
                contract.setTranscode(resultset.getString("TRANSCODE"));
                //GET COUNT OF TRANCHES RELEASED
                ACRGBWSResult Assets = this.GETASSETSWITHPARAM(dataSource, resultset.getString("HCFID"), resultset.getString("CONID"));
                if (Assets.isSuccess()) {
                    List<Assets> assetsList = Arrays.asList(utility.ObjectMapper().readValue(Assets.getResult(), Assets[].class));
                    contract.setTraches(String.valueOf(assetsList.size()));
                } else {
                    contract.setTraches("NO TRANCH RELEASED");
                }
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(contract));
            } else {
                result.setMessage("N/A");
            }
        } catch (Exception ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }

    //GET ASSETS BY CONID
    public ACRGBWSResult GETASSETSBYCONID(final DataSource dataSource, final String conid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ArrayList<Assets> listassets = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETASSETSBYCONID(:pconid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("pconid", conid);
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
                assets.setDatereleased(dateformat.format(resultset.getDate("DATERELEASED")));//resultset.getDate("DATERELEASED"));
                assets.setReceipt(resultset.getString("RECEIPT"));
                assets.setAmount(resultset.getString("AMOUNT"));
                ACRGBWSResult creator = this.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                if (creator.isSuccess()) {
                    UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class);
                    assets.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                } else {
                    assets.setCreatedby(creator.getMessage());
                }
                assets.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));
                ACRGBWSResult getcon = this.GETCONTRACTCONID(dataSource, resultset.getString("CONID").trim(), "ACTIVE");
                if (getcon.isSuccess()) {
                    assets.setConid(getcon.getResult());
                } else {
                    assets.setConid(getcon.getMessage());
                }
                assets.setStatus(resultset.getString("STATS"));
                listassets.add(assets);
            }
            if (!listassets.isEmpty()) {
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
    public ACRGBWSResult GETACCREDITATION(final DataSource dataSource, final String uaccreno) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETACCREDITATION(:uaccreno); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("uaccreno", uaccreno);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                Accreditation accree = new Accreditation();
                accree.setAccreno(resultset.getString("ACCRENO"));
                accree.setDatefrom(dateformat.format(resultset.getDate("DATEFROM")));
                accree.setDateto(dateformat.format(resultset.getDate("DATETO")));
                accree.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));
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
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET ACCREDITATION NUMBER BY ACCOUNTCODE
    public ACRGBWSResult GETCONBYCODE(final DataSource dataSource, final String ucode) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ContractMethod contractmethod = new ContractMethod();
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETCONBYCODE(:ucode); end;");
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
                con.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));

                ACRGBWSResult getcondateA = contractmethod.GETCONDATEBYID(dataSource, resultset.getString("CONTRACTDATE"));
                if (getcondateA.isSuccess()) {
                    con.setContractdate(getcondateA.getResult());
                } else {
                    con.setContractdate(getcondateA.getMessage());
                }
                con.setTranscode(resultset.getString("TRANSCODE"));
                con.setBaseamount(resultset.getString("BASEAMOUNT"));
                con.setRemarks(resultset.getString("REMARKS"));
                con.setComittedClaimsVol(resultset.getString("C_CLAIMSVOL"));
                con.setComputedClaimsVol(resultset.getString("T_CLAIMSVOL"));
                con.setAddamount(resultset.getString("ADDAMOUNT"));
                con.setSb(resultset.getString("SB"));

                if (resultset.getDate("ENDDATE") == null) {
                    con.setEnddate("N/A");
                } else {
                    con.setEnddate(dateformat.format(resultset.getDate("ENDDATE")));
                }
                result.setResult(utility.ObjectMapper().writeValueAsString(con));
                result.setMessage("OK");
                result.setSuccess(true);
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET APPELLATE CONTROL
    public ACRGBWSResult GETAPPELLATE(final DataSource dataSource,
            final String controlcode,
            final String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);

        //  ArrayList<UserRoleIndex> rolelist = new ArrayList<>();
        ArrayList<String> accesslist = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETAPPELLATE(:tags,:ucontrolcode); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("tags", tags);
            statement.setString("ucontrolcode", controlcode);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            while (resultset.next()) {
                accesslist.add(resultset.getString("ACCESSCODE"));
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
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult GETASSETBYIDANDCONID(final DataSource dataSource, final String phcfid, final String uconid,
            final String utags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Methods methods = new Methods();
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETASSETBYIDANDCONID(:phcfid,:uconid,:utags); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("phcfid", phcfid);
            statement.setString("uconid", uconid);
            statement.setString("utags", utags);
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
                    ACRGBWSResult getHCPN = methods.GETMBWITHID(dataSource, resultset.getString("HCFID"));
                    if (getHCPN.isSuccess()) {
                        assets.setHcfid(getHCPN.getResult());
                    } else {
                        assets.setHcfid(getHCPN.getMessage());
                    }
                }
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
                    assets.setCreatedby("N/A");
                }
                assets.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));
                ACRGBWSResult getcon = this.GETCONTRACTCONID(dataSource, resultset.getString("CONID").trim(), "ACTIVE");
                if (getcon.isSuccess()) {
                    if (!getcon.getResult().isEmpty()) {
                        assets.setConid(getcon.getResult());
                    } else {
                        assets.setConid(getcon.getMessage());
                    }
                } else {
                    assets.setCreatedby("N/A");
                }
                assets.setStatus(resultset.getString("STATS"));
                listassets.add(assets);
            }
            if (!listassets.isEmpty()) {
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

    public ACRGBWSResult GETASSETSHCFID(final DataSource dataSource, final String phcfid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Methods methods = new Methods();
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :assets_type := ACR_GB.ACRGBPKGFUNCTION.GETASSETSHCFID(:phcfid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
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
                    ACRGBWSResult getHCPN = methods.GETMBWITHID(dataSource, resultset.getString("HCFID"));
                    if (getHCPN.isSuccess()) {
                        assets.setHcfid(getHCPN.getResult());
                    } else {
                        assets.setHcfid(getHCPN.getMessage());
                    }
                }
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
                    assets.setCreatedby("N/A");
                }
                assets.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));
                ACRGBWSResult getcon = this.GETCONTRACTCONID(dataSource, resultset.getString("CONID").trim(), "ACTIVE");
                if (getcon.isSuccess()) {
                    if (!getcon.getResult().isEmpty()) {
                        assets.setConid(getcon.getResult());
                    } else {
                        assets.setConid(getcon.getMessage());
                    }
                } else {
                    assets.setCreatedby("N/A");
                }
                assets.setStatus(resultset.getString("STATS"));
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
    public ACRGBWSResult GETUSERBYID(final DataSource dataSource, final String uUserid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETUSERBYID(:uUserid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("uUserid", uUserid);
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
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET CONTRACT USING HCPN CONTROL CODE
    public ACRGBWSResult GetFacilityContractUsingHCPNCodeS(final DataSource dataSource, final String tags, final String controlcode) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Methods methods = new Methods();
        ContractMethod contractmethod = new ContractMethod();
        try (Connection connection = dataSource.getConnection()) {
            ArrayList<Contract> contractlist = new ArrayList<>();
            //-------------- GET APEX FACILITY
            ACRGBWSResult restA = methods.GETROLEMULITPLE(dataSource, controlcode.trim(), tags);
            List<String> hcflist = Arrays.asList(restA.getResult().split(","));
            //-------------- END OF GET APEX FACILITY
            for (int y = 0; y < hcflist.size(); y++) {
                //--------------------------------------------------------
                CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.ACR_CONTRACT(:tags,:pfchid); end;");
                statement.registerOutParameter("v_result", OracleTypes.CURSOR);
                statement.setString("tags", tags.trim());
                statement.setString("pfchid", hcflist.get(y));
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
                    contract.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));//resultset.getString("DATECREATED"));
                    ACRGBWSResult getcondateA = contractmethod.GETCONDATEBYID(dataSource, resultset.getString("CONTRACTDATE"));
                    contract.setContractdate(getcondateA.getResult());
                    ContractDate condate = utility.ObjectMapper().readValue(getcondateA.getResult(), ContractDate.class);
                    contract.setTranscode(resultset.getString("TRANSCODE"));
                    contract.setBaseamount(resultset.getString("BASEAMOUNT"));
                    contract.setComittedClaimsVol(resultset.getString("C_CLAIMSVOL"));
                    contract.setComputedClaimsVol(resultset.getString("T_CLAIMSVOL"));
                    contract.setSb(resultset.getString("SB"));
                    contract.setAddamount(resultset.getString("ADDAMOUNT"));
                    contract.setQuarter(resultset.getString("QUARTER"));
                    //=============================================
                    int numberofclaims = 0;
                    double totalclaimsamount = 0.00;
                    double percentageA = 0.00;
                    double percentageB = 0.00;
                    double trancheamount = 0.00;
                    int tranches = 0;
                    ACRGBWSResult totalResult = methods.GETSUMMARY(dataSource, hcflist.get(y));
                    if (totalResult.isSuccess()) {
                        Total getResult = utility.ObjectMapper().readValue(totalResult.getResult(), Total.class);
                        tranches += Integer.parseInt(getResult.getCcount());
                        trancheamount += Double.parseDouble(getResult.getCtotal());
                    }
                    //======================================
                    ACRGBWSResult sumresult = this.GETNCLAIMS(dataSource, hcflist.get(y), "G",
                            condate.getDatefrom(), condate.getDateto(), "CURRENTSTATUS");
                    if (sumresult.isSuccess()) {
                        List<NclaimsData> nclaimsdata = Arrays.asList(utility.ObjectMapper().readValue(sumresult.getResult(), NclaimsData[].class));
                        for (int i = 0; i < nclaimsdata.size(); i++) {
                            numberofclaims += Integer.parseInt(nclaimsdata.get(i).getTotalclaims());
                            totalclaimsamount += Double.parseDouble(nclaimsdata.get(i).getClaimamount());
                        }
                    }

                    double sumsA = trancheamount / Double.parseDouble(resultset.getString("AMOUNT")) * 100;
                    if (sumsA > 100) {
                        double negvalue = 100 - sumsA;
                        percentageA += negvalue;
                    } else {
                        percentageA += sumsA;
                    }
                    //----------------
                    double sumsB = totalclaimsamount / trancheamount * 100;
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
    public ACRGBWSResult BOOKCLAIMSDATA(final DataSource dataSource, final String u_accreno, final String u_tags,
            final String u_from, final String u_to) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKG.GETNCLAIMS(:u_accreno,:u_tags,:u_from,:u_to); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("u_accreno", u_accreno);
            statement.setString("u_tags", u_tags);
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
    public ACRGBWSResult GETUSERINFOUSINGEMAIL(final DataSource dataSource, final String uemailadd) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETUSERINFOUSINGEMAIL(:uemailadd); end;");
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
    public ACRGBWSResult GETACCOUNTUSINGEMAIL(final DataSource dataSource, final String uusername) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETACCOUNTUSINGEMAIL(:uusername); end;");
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
    public ACRGBWSResult FORUSERLEVEL(final DataSource dataSource, final String ulevelid) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.FORUSERLEVEL(:ulevelid); end;");
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

    public ACRGBWSResult GETOLDPASSCODE(final DataSource dataSource, final String puserid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETPASSUSINGUSERID(:puserid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("puserid", puserid.replaceAll("\\s", "").toUpperCase());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
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
                user.setUserpassword(resultset.getString("USERPASSWORD"));
                user.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));//resultset.getString("DATECREATED"));
                user.setStatus(resultset.getString("STATS"));
                user.setDid(resultset.getString("DID"));
                ACRGBWSResult creator = this.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                if (creator.isSuccess()) {
                    UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class);
                    user.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                } else {
                    user.setCreatedby(creator.getMessage());
                }
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

    //GET DIRECT CONTRACT OF PRO AND QUARTER
    //GET CONTRACT OF APEX FACILITY
    public ACRGBWSResult CONTRACTWITHQUARTER(final DataSource dataSource, final String tags, final String uprocode) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Methods methods = new Methods();
        ContractMethod contractmethod = new ContractMethod();
        try (Connection connection = dataSource.getConnection()) {
            ArrayList<Contract> contractlist = new ArrayList<>();
            //--------------------------------------------------------
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.ACR_CONTRACT(:tags,:pfchid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("tags", tags);
            statement.setString("pfchid", uprocode);
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
                contract.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));//resultset.getString("DATECREATED"));
                ACRGBWSResult getcondateA = contractmethod.GETCONDATEBYID(dataSource, resultset.getString("CONTRACTDATE"));
                contract.setContractdate(getcondateA.getResult());
                contract.setTranscode(resultset.getString("TRANSCODE"));
                contract.setBaseamount(resultset.getString("BASEAMOUNT"));
                contract.setComittedClaimsVol(resultset.getString("C_CLAIMSVOL"));
                contract.setComputedClaimsVol(resultset.getString("T_CLAIMSVOL"));
                contract.setSb(resultset.getString("SB"));
                contract.setAddamount(resultset.getString("ADDAMOUNT"));
                contract.setQuarter(resultset.getString("QUARTER"));
                //=============================================
                ContractDate condate = utility.ObjectMapper().readValue(getcondateA.getResult(), ContractDate.class);

                int numberofclaims = 0;
                double totalclaimsamount = 0.00;
                double percentageA = 0.00;
                double percentageB = 0.00;
                double trancheamount = 0.00;
                int tranches = 0;

                //GET ALL FACILITY UNDER HCPN
//                ACRGBWSResult GetRoleA = methods.GETROLE(dataSource, uprocode, tags);
//                if (GetRoleA.isSuccess()) {
                ACRGBWSResult GetRoleB = methods.GETROLEMULITPLE(dataSource, uprocode.trim(), tags);
                if (GetRoleB.isSuccess()) {
                    List<String> HCPNList = Arrays.asList(GetRoleB.getResult().split(","));
                    for (int A = 0; A < HCPNList.size(); A++) {
                        ACRGBWSResult GetRoleC = methods.GETROLEMULITPLE(dataSource, HCPNList.get(A).trim(), tags);
                        if (GetRoleC.isSuccess()) {
                            List<String> HCIList = Arrays.asList(GetRoleC.getResult().split(","));
                            for (int B = 0; B < HCIList.size(); B++) {
                                ACRGBWSResult ResultB = methods.GETSUMMARY(dataSource, HCIList.get(B).trim());
                                if (ResultB.isSuccess()) {
                                    Total getResult = utility.ObjectMapper().readValue(ResultB.getResult(), Total.class);
                                    tranches += Integer.parseInt(getResult.getCcount());
                                    trancheamount += Double.parseDouble(getResult.getCtotal());
                                }
                                //======================================
                                ACRGBWSResult sumresultB = this.GETNCLAIMS(dataSource, HCIList.get(B).trim(), "G", condate.getDatefrom(), condate.getDateto(), "CURRENTSTATUS");
                                if (sumresultB.isSuccess()) {
                                    List<NclaimsData> nclaimsdata = Arrays.asList(utility.ObjectMapper().readValue(sumresultB.getResult(), NclaimsData[].class));
                                    for (int i = 0; i < nclaimsdata.size(); i++) {
                                        numberofclaims += Integer.parseInt(nclaimsdata.get(i).getTotalclaims());
                                        totalclaimsamount += Double.parseDouble(nclaimsdata.get(i).getClaimamount());
                                    }
                                }
                            }
                        }
                    }
                }
//                }
                double sumsA = trancheamount / Double.parseDouble(resultset.getString("AMOUNT")) * 100;
                if (sumsA > 100) {
                    double negvalue = 100 - sumsA;
                    percentageA += negvalue;
                } else {
                    percentageA += sumsA;
                }
                //----------------
                double sumsB = totalclaimsamount / trancheamount * 100;
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

}
