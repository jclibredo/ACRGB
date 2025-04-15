/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb;

import acrgb.method.BookingMethod;
import acrgb.method.ContractHistoryService;
import acrgb.method.ContractMethod;
import acrgb.method.ContractTagging;
import acrgb.method.CurrentBalance;
import acrgb.method.FetchMethods;
import acrgb.method.GenerateRandomPassword;
import acrgb.method.LedgerMethod;
import acrgb.method.Methods;
import acrgb.method.ProcessAffiliate;
import acrgb.method.cf5.CF5Data;
import acrgb.method.claimsvalidator.ValidateClaims;
import acrgb.structure.ACRGBWSResult;
import acrgb.structure.Appellate;
import acrgb.structure.HcfPro;
import acrgb.structure.HealthCareFacility;
import acrgb.structure.ManagingBoard;
import acrgb.structure.User;
import acrgb.utility.NamedParameterStatement;
import acrgb.utility.Utility;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
//import javax.mail.Session;
import javax.sql.DataSource;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

/**
 * REST Web Service
 *
 * @author DRG_SHADOWBILLING
 */
@Path("ACRGBFETCH")
@RequestScoped
public class ACRGBFETCH {

    /**
     * Creates a new instance of ACRGBFETCH
     */
    public ACRGBFETCH() {
    }

    @Resource(lookup = "jdbc/acgbuser")
    private DataSource dataSource;

//    @Resource(lookup = "mail/acrgbmail")
//    private Session acrgbmail;
    private final Utility utility = new Utility();

    @GET
    @Path("EmailNotification/{conid}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult EmailNotification(
            @HeaderParam("token") String token,
            @PathParam("conid") String conid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayloadNODB(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayloadNODB(dataSource, token).getMessage());
        } else {
            
            
        }
        return result;
    }

    @GET
    @Path("ValidateClaims/{useries}/{uaction}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult ValidateClaims(
            @HeaderParam("token") String token,
            @PathParam("useries") String useries,
            @PathParam("uaction") String uaction) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayloadNODB(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayloadNODB(dataSource, token).getMessage());
        } else {
            result = new ValidateClaims().GETNCLAIMS(dataSource, useries.trim(), uaction.trim().toUpperCase());
        }
        return result;
    }

    //GET ASSETS TYPE TBL
//    @GET
//    @Path("TESTDate/{datefrom}/{dateto}")
//    @Produces(MediaType.APPLICATION_JSON)
//    public ACRGBWSResult TESTDate(
//            @PathParam("datefrom") String datefrom,
//            @PathParam("dateto") String dateto) {
//        return utility.ProcessDateAmountComputation(datefrom, dateto);
//    }
//
//    @GET
//    @Path("TESTGetClaimsAverage/{pmccno}/{datefrom}/{dateto}")  //300806/01-01-2025/12-31-2025
//    @Produces(MediaType.APPLICATION_JSON)
//    public ACRGBWSResult TESTGetClaimsAverage(
//            @PathParam("pmccno") String pmccno,
//            @PathParam("datefrom") String datefrom,
//            @PathParam("dateto") String dateto) {
//        return new Methods().GETAVERAGECLAIMSS(dataSource, pmccno, datefrom, dateto);
//    }
    //GET ASSETS TYPE TBL
    @GET
    @Path("GetAssets/{tags}/{phcfid}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetAssets(
            @HeaderParam("token") String token,
            @PathParam("tags") String tags,
            @PathParam("phcfid") String phcfid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            result = new FetchMethods().ACR_ASSETS(dataSource, tags, phcfid);
        }
        return result;
    }
//------------------------------------------------------------
    //GET FACILITY UNDER PRO USING PRO ACCOUNT USERID
    @GET
    @Path("GetAppexUnderProByUserid/{userid}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetAppexUnderProByUserid(
            @HeaderParam("token") String token,
            @PathParam("userid") String userid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            ArrayList<HealthCareFacility> hcfList = new ArrayList<>();
            ArrayList<String> errorList = new ArrayList<>();
            if (!utility.GetPayload(dataSource, token).isSuccess()) {
                result.setMessage(utility.GetPayload(dataSource, token).getMessage());
            } else {
                ACRGBWSResult restA = new Methods().GETROLE(dataSource, userid, "ACTIVE");//GET (PROID) USING (USERID)
                if (restA.isSuccess()) {
                    ACRGBWSResult gethcf = new CF5Data().GETHCFPRO(dataSource, restA.getResult().replace("2024", ""));
                    if (gethcf.isSuccess()) {
                        List<HcfPro> hcfproList = Arrays.asList(utility.ObjectMapper().readValue(gethcf.getResult(), HcfPro[].class));
                        for (int x = 0; x < hcfproList.size(); x++) {
                            ACRGBWSResult getHciByPmccNo = new FetchMethods().GETFACILITYID(dataSource, hcfproList.get(x).getPmccno());
                            if (getHciByPmccNo.isSuccess()) {
                                HealthCareFacility hcf = utility.ObjectMapper().readValue(getHciByPmccNo.getResult(), HealthCareFacility.class);
                                hcfList.add(hcf);
                            }
                        }
                    } else {
                        errorList.add("Hcf under pro " + gethcf.getMessage());
                    }
                } else {
                    errorList.add("User Role Index " + restA.getMessage());
                }
                if (hcfList.size() > 0) {
                    result.setResult(utility.ObjectMapper().writeValueAsString(hcfList));
                    result.setSuccess(true);
                } else {
                    result.setMessage(errorList.toString());
                }
            }
        } catch (IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(ACRGBFETCH.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
//------------------------------------------------------------
    //GET FACILITY UNDER PRO USING PRO ACCOUNT USERID
    @GET
    @Path("GetAppexUnderProByProCode/{procode}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetAppexUnderProByProCode(
            @HeaderParam("token") String token,
            @PathParam("procode") String procode) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            ArrayList<HealthCareFacility> hcfList = new ArrayList<>();
            ArrayList<String> errorList = new ArrayList<>();
            ACRGBWSResult GetPayLoad = utility.GetPayload(dataSource, token);
            if (!GetPayLoad.isSuccess()) {
                result.setMessage(GetPayLoad.getMessage());
            } else {
                ACRGBWSResult gethcf = new CF5Data().GETHCFPRO(dataSource, procode.replace("2024", ""));
                if (gethcf.isSuccess()) {
                    List<HcfPro> hcfproList = Arrays.asList(utility.ObjectMapper().readValue(gethcf.getResult(), HcfPro[].class));
                    for (int x = 0; x < hcfproList.size(); x++) {
                        ACRGBWSResult getHciByPmccNo = new FetchMethods().GETFACILITYID(dataSource, hcfproList.get(x).getPmccno());
                        if (getHciByPmccNo.isSuccess()) {
                            HealthCareFacility hcf = utility.ObjectMapper().readValue(getHciByPmccNo.getResult(), HealthCareFacility.class);
                            hcfList.add(hcf);
                        }
                    }
                } else {
                    errorList.add("Hcf under pro " + gethcf.getMessage());
                }
                if (hcfList.size() > 0) {
                    result.setResult(utility.ObjectMapper().writeValueAsString(hcfList));
                    result.setSuccess(true);
                } else {
                    result.setMessage(errorList.toString());
                }
            }
        } catch (IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(ACRGBFETCH.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
//------------------------------------------------------------
    //GET  HCI NET ASSETS TBL FINAL
    @GET
    @Path("GetContract/{tags}/{puserid}/{level}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetContract(
            @HeaderParam("token") String token,
            @PathParam("tags") String tags,
            @PathParam("puserid") String puserid,
            @PathParam("level") String level) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            switch (level.toUpperCase().trim()) {
                case "PRO": {//puserid = prouseraccount ID
                    result = new FetchMethods().ACR_CONTRACTPROID(dataSource, tags.trim().toUpperCase(), puserid, "OPEN");//GET CONTRACT USING USERID OF PRO USER ACCOUNT
                    break;
                }
                case "MB": { //puserid = hcpnuseraccount ID
                    result = new FetchMethods().GETCONTRACTUNDERMB(dataSource, tags.trim().toUpperCase(), puserid, "OPEN");//GET CONTRACT USING USERID OF HCPN USER ACCOUNT
                    break;
                }
                case "PHICAPEX": {//puserid = 0
                    result = new ContractMethod().APEXFACILITYCONTRACT(dataSource, tags.trim().toUpperCase(), puserid, "OPEN");//GET CONTRACT OF ALL APEX FACILITY
                    break;
                }
                case "PHICHCPN": {//puserid = 0 
                    result = new FetchMethods().GETALLHCPNCONTRACT(dataSource, tags.trim().toUpperCase(), puserid, "OPEN");//GET CONTRACT OF ALL HCPN/NETWORK
                    break;
                }
                //GetFacilityContractUsingHCPNCode  GET FACILITY CONTRACT USING MB ACCOUNT USERID
                case "HCPN": {
                    result = new FetchMethods().GetFacilityContractUsingHCPNAccountUserID(dataSource, tags.trim().toUpperCase(), puserid, "OPEN");//GET CONTRACT OF ALL APEX FACILITY
                    break;
                }
                // GET ALL FACILITY CONTRACT USING HCPN CONTROL CODE   IN PRO LEVEL  
                case "HCIHCPNCON": {
                    result = new FetchMethods().GetFacilityContractUsingHCPNCodeS(dataSource, tags.trim().toUpperCase(), puserid.toUpperCase(), "OPEN");//GET CONTRACT OF ALL APEX FACILITY
                    break;
                }
                // GET FACILITY CONTRACT USING ACCOUNT USERID
                case "FACILITYCONOWN": {
                    result = new ContractMethod().GETCONTRACTOFFACILITY(dataSource, tags, puserid, "OPEN");
                    break;
                }
                // GET HCPN CONTRACT USING ACCOUNT USERID
                case "HCPNCONOWN": {
                    result = new ContractMethod().GETCONTRACTOFHCPN(dataSource, tags, puserid, "OPEN");
                    break;
                }
                // GET PRO CONTRACT USING ACCOUNT USERID
                case "PROCONOWN": {
                    result = new ContractMethod().GETCONTRACTOFPRO(dataSource, tags, puserid, "OPEN");
                    break;
                }
                default: {
                    result.setMessage(level + " IS NOT VALID");
                    break;
                }
            }
        }
        return result;
    }
//------------------------------------------------------------
    //GET  ACR USER INDEX TBL
    @GET
    @Path("GetTranch/{tags}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetTranch(
            @HeaderParam("token") String token,
            @PathParam("tags") String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            result = new FetchMethods().ACR_TRANCH(dataSource, tags);
        }
        return result;
    }
//------------------------------------------------------------
    //GET  ACR USER TABLE
    @GET
    @Path("GetUserInfo/{tags}/{pdid}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetUserInfo(
            @HeaderParam("token") String token,
            @PathParam("tags") String tags,
            @PathParam("pdid") String pdid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            result = new FetchMethods().ACR_USER_DETAILS(dataSource, tags, pdid);
        }
        return result;
    }
//------------------------------------------------------------
    //GET  HCI FACILITY
    @GET
    @Path("GetUserLevel/{tags}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetUserLevel(
            @HeaderParam("token") String token,
            @PathParam("tags") String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            result = new FetchMethods().ACR_USER_LEVEL(dataSource, tags);
        }
        return result;
    }
//------------------------------------------------------------
    //GET  HCI FACILITY
    @GET
    @Path("GetUser/{tags}/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetUser(
            @HeaderParam("token") String token,
            @PathParam("tags") String tags,
            @PathParam("id") String id) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            result = new FetchMethods().ACR_USER(dataSource, tags, id);
        }
        return result;
    }
//------------------------------------------------------------
    @GET
    @Path("GetPro/{tags}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetPro(
            @HeaderParam("token") String token,
            @PathParam("tags") String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setSuccess(false);
        result.setResult("");
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            result = new FetchMethods().ACR_PRO(dataSource, tags.trim());
        }
        return result;
    }
//------------------------------------------------------------
    @GET
    @Path("GetRoleIndex/{puserid}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetRoleIndex(
            @HeaderParam("token") String token,
            @PathParam("puserid") String puserid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            result = new FetchMethods().GETUSERROLEINDEX(dataSource, puserid);
        }
        return result;
    }
//------------------------------------------------------------
//SUMMARY 
    @GET
    @Path("GetSummary/{tags}/{userid}/{datefrom}/{dateto}/{type}/{hcilist}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetSummary(@HeaderParam("token") String token,
            @PathParam("tags") String tags,
            @PathParam("userid") String userid,
            @PathParam("datefrom") String datefrom,
            @PathParam("dateto") String dateto,
            @PathParam("type") String type,
            @PathParam("hcilist") String hcilist) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (utility.IsValidDate(datefrom) && utility.IsValidDate(dateto)) {
            if (!utility.GetPayload(dataSource, token).isSuccess()) {
                result.setMessage(utility.GetPayload(dataSource, token).getMessage());
            } else {
                switch (type.toUpperCase().trim()) {
                    case "SUMMARY": {
                        result = new Methods().GetBaseAmountForSummary(dataSource, tags, userid, datefrom, dateto, "ACTIVE".toUpperCase().trim(), hcilist);
                        break;
                    }
                    case "CONTRACT": {
                        result = new Methods().GetBaseAmountForContract(dataSource, tags, userid, datefrom, dateto, "ACTIVE".toUpperCase().trim());
                        break;
                    }
                    default: {
                        result.setMessage("REQUEST TYPE NOT FOUND");
                        break;
                    }
                }
            }
        } else {
            result.setMessage("DATE FORMAT IS NOT VALID");
        }
        return result;
    }
//------------------------------------------------------------
    @GET
    @Path("GetLevel/{levid}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetLevel(
            @HeaderParam("token") String token,
            @PathParam("levid") String levid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            result = new FetchMethods().GETUSERLEVEL(dataSource, levid);
        }

        return result;
    }
//------------------------------------------------------------
//GET USER DETAILS
    @GET
    @Path("GETFULLDETAILS/{userid}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GETFULLDETAILS(
            @HeaderParam("token") String token,
            @PathParam("userid") String userid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            result = new FetchMethods().GETFULLDETAILS(dataSource, userid);
        }
        return result;
    }

//------------------------------------------------------------
//GET ASSETS WITH PARAMETER
    @GET
    @Path("GETASSETSWITHPARAM/{phcfid}/{conid}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GETASSETSWITHPARAM(
            @HeaderParam("token") String token,
            @PathParam("phcfid") String phcfid,
            @PathParam("conid") String conid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            result = new FetchMethods().GETASSETSWITHPARAM(dataSource, phcfid, conid);
        }
        return result;
    }
//------------------------------------------------------------
    @GET
    @Path("GetActivityLogs")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetActivityLogs(
            @HeaderParam("token") String token) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            result = new FetchMethods().ACRACTIVTYLOGS(dataSource);
        }
        return result;
    }
//------------------------------------------------------------

    @GET
    @Path("GetLogWithParam/{userid}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetLogWithParam(
            @HeaderParam("token") String token,
            @PathParam("userid") String userid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            result = new Methods().GetLogsWithID(dataSource, userid);
        }
        return result;
    }
//------------------------------------------------------------
//GET  REQUEST USING MB USER ID

//    @GET
//    @Path("GetMBRequest/{userid}")
//    @Produces(MediaType.APPLICATION_JSON)
//    public ACRGBWSResult GetMBRequest(
//            @HeaderParam("token") String token,
//            @PathParam("userid") String userid) {
//        ACRGBWSResult result = utility.ACRGBWSResult();
//        result.setMessage("");
//        result.setResult("");
//        result.setSuccess(false);
//        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
//        if (!GetPayLoad.isSuccess()) {
//            result.setMessage(GetPayLoad.getMessage());
//        } else {
//            ACRGBWSResult getResult = new Methods().FetchMBRequest(dataSource, userid);
//            result.setMessage(getResult.getMessage());
//            result.setResult(getResult.getResult());
//            result.setSuccess(getResult.isSuccess());
//        }
//        return result;
//    }
//------------------------------------------------------------
    @GET
    @Path("GetManagingBoard/{tags}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetManagingBoard(
            @HeaderParam("token") String token,
            @PathParam("tags") String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            result = new FetchMethods().GetManagingBoard(dataSource, tags);
        }
        return result;
    }
//------------------------------------------------------------    

    @GET
    @Path("GetFacilityUsingProAccountUserID/{pid}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetFacilityUsingProAccountUserID(
            @HeaderParam("token") String token,
            @PathParam("pid") String pid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            result = new Methods().GETROLEWITHID(dataSource, pid.trim(), "ACTIVE");
        }
        return result;
    }
//------------------------------------------------------------

    @GET
    @Path("GETALLFACILITY/{tags}/{userid}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GETALLFACILITY(
            @HeaderParam("token") String token,
            @PathParam("tags") String tags,
            @PathParam("userid") String userid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            if (!utility.GetPayload(dataSource, token).isSuccess()) {
                result.setMessage(utility.GetPayload(dataSource, token).getMessage());
            } else {
                switch (tags.toUpperCase()) {
                    case "ALL": {
                        result = new FetchMethods().GETALLFACILITY(dataSource, "ACTIVE");
                        break;
                    }
                    case "HCPN": {//GET FACILITY USINNG HCPN ACCOUNT USERID
                        result = new FetchMethods().GETFACILITYUNDERMB(dataSource, userid, "ACTIVE");
                        break;
                    }
                    case "APEX": {// TAGS = APEX  USERID  = HOSPITAL CODE
                        ACRGBWSResult restA = new FetchMethods().GetAffiliate(dataSource, userid, "ACTIVE");
                        List<String> hcpnlist = Arrays.asList(restA.getResult().split(","));
                        ArrayList<ManagingBoard> mblist = new ArrayList<>();
                        for (int h = 0; h < hcpnlist.size(); h++) {
                            ACRGBWSResult mgresult = new Methods().GETMBWITHID(dataSource, hcpnlist.get(h).trim());
                            if (mgresult.isSuccess()) {
                                ManagingBoard mb = utility.ObjectMapper().readValue(mgresult.getResult(), ManagingBoard.class);
                                mblist.add(mb);
                            }
                        }

                        if (mblist.size() > 0) {
                            result.setMessage("OK");
                            result.setSuccess(true);
                            result.setResult(utility.ObjectMapper().writeValueAsString(mblist));
                        } else {
                            result.setMessage("N/A");
                        }
                        break;
                    }
                }
            }
        } catch (IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(ACRGBFETCH.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
//------------------------------------------------------------
    @GET
    @Path("GetManagingBoardWithProID/{proid}/{levelname}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetManagingBoardWithProID(
            @HeaderParam("token") String token,
            @PathParam("proid") String proid,
            @PathParam("levelname") String levelname) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            switch (levelname.toUpperCase().trim()) {
                case "PRO": {
                    result = new Methods().GETALLMBWITHPROID(dataSource, proid);
                    break;
                }
                case "PROLEDGER": {
                    result = new Methods().GETALLMBWITHPROIDFORLEDGER(dataSource, proid);
                    break;
                }
                case "PHIC": {
                    result = new Methods().GETALLMBWITHPROCODE(dataSource, proid, "ACTIVE");
                    break;
                }
                default: {
                    result.setMessage("LEVEL TAGS NOT FOUND");
                    break;
                }
            }
        }
        return result;
    }

//------------------------------------------------------------
    @GET
    @Path("GetMBUsingMBID/{pid}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetMBUsingMBID(
            @HeaderParam("token") String token,
            @PathParam("pid") String pid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            result = new Methods().GETALLFACILITYWITHMBID(dataSource, pid, "ACTIVE");
        }
        return result;
    }
//------------------------------------------------------------
//    @GET
//    @Path("GetBalanceTerminatedContract/{userid}/{levelname}/{tags}")
//    @Produces(MediaType.APPLICATION_JSON)
//    public ACRGBWSResult GetBalanceTerminatedContract(
//            @HeaderParam("token") String token,
//            @PathParam("userid") String userid,
//            @PathParam("levelname") String levelname,
//            @PathParam("tags") String tags) {
//        ACRGBWSResult result = utility.ACRGBWSResult();
//        result.setMessage("");
//        result.setResult("");
//        result.setSuccess(false);
//        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
//        if (!GetPayLoad.isSuccess()) {
//            result.setMessage(GetPayLoad.getMessage());
//        } else {
//            switch (levelname.toUpperCase().trim()) {
//                case "PRO":
//                    //GET TERMINATED CONTRACT OF FACILITY UNDER HCPN UNDER PRO USING PRO ACCOUNT USERID
//                    ACRGBWSResult getResult = new Methods().GetRemainingBalanceForTerminatedContract(dataSource, userid, tags);
//                    result.setMessage(getResult.getMessage());
//                    result.setResult(getResult.getResult());
//                    result.setSuccess(getResult.isSuccess());
//                    break;
//                case "TERMINATEAPEX":
//                    //GET TERMINATED CONTRACT OF APEX
//                    ACRGBWSResult getResultTerminateApex = new Methods().GetRemainingBalanceForTerminatedContractApex(dataSource);
//                    result.setMessage(getResultTerminateApex.getMessage());
//                    result.setResult(getResultTerminateApex.getResult());
//                    result.setSuccess(getResultTerminateApex.isSuccess());
//                    break;
//                case "ENDCONAPEX":
//                    //GET END CONTRACT OF APEX
//                    ACRGBWSResult getResultEndApex = new Methods().GetRemainingBalanceForEndContractApex(dataSource);
//                    result.setMessage(getResultEndApex.getMessage());
//                    result.setResult(getResultEndApex.getResult());
//                    result.setSuccess(getResultEndApex.isSuccess());
//                    break;
//                case "NONRENEW":
//                    //GET END CONTRACT OF FACILITY UNDER HCPN UNDER PRO USING PRO ACCOUNT USERID
//                    ACRGBWSResult getResultnNonRenew = new Methods().GetRemainingBalanceForEndContract(dataSource, userid, tags);
//                    result.setMessage(getResultnNonRenew.getMessage());
//                    result.setResult(getResultnNonRenew.getResult());
//                    result.setSuccess(getResultnNonRenew.isSuccess());
//                    break;
//                default:
//                    result.setMessage("LEVEL STATUS NOT VALID");
//                    break;
//            }
//        }
//        return result;
//    }

    //LEDGER 
    @GET
    @Path("PerContractLedger/{hcpncode}/{contract}/{tags}/{type}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult PerContractLedger(
            @HeaderParam("token") String token,
            @PathParam("type") String type,
            @PathParam("hcpncode") String hcpncode, //hcpncode  MUST BE HCPNCONTROLCODE
            @PathParam("contract") String contract, //CONTRACT MUST BE CONID
            @PathParam("tags") String tags) {   //TAGS MUST BE LEVELACCESS
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            switch (tags.toUpperCase().trim()) {
                case "HCPN": {//GET LEDGER PER HCPN
                    //hcpncode  MUST BE HCPNCONTROLCODE
                    //CONTRACT MUST BE CONID
                    //TAGS MUST BE HCPN
                    switch (type.toUpperCase()) {
                        case "ACTIVE": {
                            result = new LedgerMethod().GETLedgerPerContractHCPN(dataSource, hcpncode, contract, type);
                            break;
                        }
                        case "INACTIVE": {
                            result = new LedgerMethod().GETLedgerPerContractHCPNLedger(dataSource, hcpncode, contract, type, "CLOSED");
                            break;
                        }
                        default: {
                            result.setMessage("Contract type not found " + type);
                            break;
                        }
                    }
                    break;
                }
                case "FACILITY": {
                    //GET LEDGER PER FACILITY
                    //hcpncode  MUST BE THE USERID OF PRO ACCOUNT
                    //CONTRACT DECLARE 0 VALUE
                    //TAGS MUST BE HCPNALL
                    switch (type.toUpperCase()) {
                        case "ACTIVE": {
                            result = new LedgerMethod().GETLedgerAllContractAPEXActive(dataSource, hcpncode, contract);//hcpncode  user ID of account of PROUSER
                            break;
                        }
                        case "INACTIVE": {
                            result = new LedgerMethod().GETLedgerAllContractAPEXInactive(dataSource, hcpncode, contract, type);//hcpncode  user ID of account of PROUSER
                            break;
                        }
                        default: {
                            result.setMessage("Contract type not found " + type);
                            break;
                        }
                    }
                    break;
                }
                default: {
                    result.setMessage("TAGS NOT FOUND " + tags);
                    break;
                }
            }
        }
        return result;
    }

//------------------------------------------------------------------------------
    @GET
    @Path("GetContractDate/{tags}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetContractDate( //hcpncode  MUST BE HCPNCONTROLCODE
            @HeaderParam("token") String token,
            @PathParam("tags") String tags) {   //TAGS MUST BE LEVELACCESS
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            result = new ContractMethod().GETCONDATE(dataSource, tags);
        }
        return result;
    }

    //GET TRIGGER AUTOEND CONTRACT DATE
    @GET
    @Path("AutoEndContractDate/{ucondateid}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult AutoEndContractDate(
            @HeaderParam("token") String token,
            @PathParam("ucondateid") String ucondateid) {   //TAGS MUST BE LEVELACCESS
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            result = new ContractTagging().EndContractUsingDateid(dataSource, ucondateid);
        }
        return result;
    }

    //GET RANDOM PASSWORD
    @GET
    @Path("GetRandomPasscode")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetRandomPasscode(
            @HeaderParam("token") String token) {   //TAGS MUST BE LEVELACCESS
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            result.setMessage("Generated random passcode");
            result.setResult(new GenerateRandomPassword().GenerateRandomPassword(10));
            result.setSuccess(true);
        }
        return result;
    }

    //GET PREVIOS ENDED CONTRACT
    @GET
    @Path("GetPreviousContract/{paccount}/{contractid}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetPreviousContract(
            @HeaderParam("token") String token,
            @PathParam("paccount") String paccount,
            @PathParam("contractid") String contractid) {   //TAGS MUST BE LEVELACCESS
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            result = new ContractMethod().GETPREVIOUSBALANCE(dataSource, paccount, contractid);
        }
        return result;
    }

    // 
    @GET
    @Path("GetClaims/{hcpncode}/{contractid}/{tags}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetClaims(
            @HeaderParam("token") String token,
            @PathParam("hcpncode") String hcpncode,
            @PathParam("contractid") String contractid,
            @PathParam("tags") String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            result = new BookingMethod().GETALLCLAIMS(dataSource, hcpncode, contractid, tags.trim().toUpperCase(), "INACTIVE");
        }
        return result;
    }

//    //GET EMAIL CREDEDNTIALS
//    @GET
//    @Path("GetEmailCredentials")
//    @Produces(MediaType.APPLICATION_JSON)
//    public ACRGBWSResult GetEmailCredentials(@HeaderParam("token") String token) {
//        ACRGBWSResult result = utility.ACRGBWSResult();
//        result.setMessage("");
//        result.setResult("");
//        result.setSuccess(false);
//        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
//        if (!GetPayLoad.isSuccess()) {
//            result.setMessage(GetPayLoad.getMessage());
//        } else {
//            Forgetpassword forgetpass = new Forgetpassword();
//            ACRGBWSResult BookingResult = forgetpass.GetEmailSender(dataSource);
//            result.setMessage(BookingResult.getMessage());
//            result.setResult(BookingResult.getResult());
//            result.setSuccess(BookingResult.isSuccess());
//        }
//        return result;
//    }
    @GET
    @Path("GETOLDPASSCODE")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GETOLDPASSCODE(
            @HeaderParam("token") String token,
            @HeaderParam("userid") String userid,
            @HeaderParam("passcode") String passcode) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            result = new FetchMethods().GETOLDPASSCODE(dataSource, userid, passcode);
        }
        return result;
    }
//------------------------------------------------------------------------------

    @GET
    @Path("CONTRACTWITHQUARTER/{tags}/{uprocode}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult CONTRACTWITHQUARTER(
            @HeaderParam("token") String token,
            @PathParam("tags") String tags,
            @PathParam("uprocode") String uprocode) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            result = new FetchMethods().CONTRACTWITHQUARTER(dataSource, tags.toUpperCase().trim(), uprocode.trim());
        }
        return result;
    }

    //INSERT EMAIL CREDEDNTIALS
    @GET
    @Path("ValidateToken")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult PostEmailCredentials(@HeaderParam("token") String token) {
        ACRGBWSResult result = utility.GetPayload(dataSource, token);
        return result;
    }

    //GET ALL CONTRACT
    @GET
    @Path("GetAllContract/{tags}/{target}")    //BOOK    //NOTBOOK
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetllContract(
            @HeaderParam("token") String token,
            @PathParam("tags") String tags,
            @PathParam("target") String target) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            switch (target.toUpperCase().trim()) {
                case "NOTBOOK": {
                    result = new FetchMethods().GETALLCONTRACTNOTBBOK(dataSource, tags, "0");
                    break;
                }
                case "BOOK": {
                    result = new FetchMethods().GETBOOKCONTRACT(dataSource, tags, "0");
                    break;
                }
                case "ALL": {
                    result = new FetchMethods().GETALLCONTRACT(dataSource, tags, "0");
                    break;
                }
                default: {
                    result.setMessage("NOT FOUND REQUEST TYPE");
                    break;
                }
            }
        }
        return result;
    }
//------------------------------------------------------------------------------

    @GET
    @Path("GetContractHistory/{userId}/{requestCode}/{targetData}")//PRO LEVEL AND PHIC LEVEL
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetContractHistory(
            @HeaderParam("token") String token,
            @PathParam("userId") String userId,
            @PathParam("requestCode") String requestCode,
            @PathParam("targetData") String targetData) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            result = new ContractHistoryService().GetHistoryResult(dataSource, userId, "INACTIVE", requestCode, targetData);
        }
        return result;
    }
//-----------------------------------------------------------------------------
//    @GET
//    @Path("Validate2FA")
//    @Produces(MediaType.APPLICATION_JSON)
//    public ACRGBWSResult Validate2FA(
//            @HeaderParam("userid") String userid,
//            @HeaderParam("code") String code) {
//        ACRGBWSResult result = utility.ACRGBWSResult();
//        result.setMessage("");
//        result.setResult("");
//        result.setSuccess(false);
//        try {
//            ACRGBWSResult GetResult = new FetchMethods().GETUSERBYUSERID(dataSource, userid, "ACTIVE");
//            if (GetResult.isSuccess()) {
//                User user = utility.ObjectMapper().readValue(GetResult.getResult(), User.class);
//                if (user.getFa2code() == null) {
//                    result.setMessage("NO EXISTING 2FA CODE FOR SELECTED USER");
//                } else if (code.isEmpty()) {
//                    result.setMessage("2FA CODE IS REQUIRED");
//                } else if (user.getFa2code().trim().equals(code.trim())) {
//                    result.setMessage(GetResult.getMessage());
//                    result.setResult(GetResult.getResult());
//                    result.setSuccess(GetResult.isSuccess());
//                } else {
//                    result.setMessage(code + " Code is invalid");
//                }
//            } else {
//                result.setMessage(GetResult.getMessage());
//            }
//        } catch (IOException ex) {
//            result.setMessage(ex.getLocalizedMessage());
//            Logger.getLogger(ACRGBFETCH.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return result;
//    }
//    @GET
//    @Path("Create2FA")
//    @Produces(MediaType.APPLICATION_JSON)
//    public ACRGBWSResult Create2FA(
//            @HeaderParam("userid") String userid,
//            @HeaderParam("email") String email,
//            @HeaderParam("mailuser") String mailuser,
//            @HeaderParam("mailapikey") String mailapikey,
//            @HeaderParam("mailhost") String mailhost,
//            @HeaderParam("mailport") String mailport,
//            @HeaderParam("mailfrom") String mailfrom) {
//        ACRGBWSResult result = utility.ACRGBWSResult();
//        result.setMessage("");
//        result.setResult("");
//        result.setSuccess(false);
//        ForgetPassword forgetPassword = new ForgetPassword();
//        UpdateMethods um = new UpdateMethods();
//              
//        forgetPassword.setAppuser(mailuser.trim());
//        forgetPassword.setApppass(mailapikey);
//        forgetPassword.setMailfrom(mailfrom.trim());
//        forgetPassword.setMailhost(mailhost.trim());
//        forgetPassword.setMailport(mailport.trim());
//        //--------------------------------------
//        ACRGBWSResult GetResult = um.UPDATEUSERFOR2FA(dataSource, forgetPassword, email, userid);
//        result.setMessage(GetResult.getMessage());
//        result.setResult(GetResult.getResult());
//        result.setSuccess(GetResult.isSuccess());
//        //--------------------------------------
//        return result;
//    }

    @GET
    @Path("GetCaptchaCode")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetCaptchaCode() {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("OK");
        result.setSuccess(true);
        result.setResult(utility.Create2FACode());
        return result;
    }

    @GET
    @Path("AutoProcessData")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult AutoProcessData(@HeaderParam("token") String token) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("OK");
        result.setSuccess(true);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            //CHECKING OF ENDED ACCREDITATION
//            new Methods().GETACTIVEACCREDITATION(dataSource, "ACTIVE");
            //CHECKING OF ENDED CONTRACT PERIOD

            new Methods().PROCESSENDPERIODDATE(dataSource, "ACTIVE");
        }
        return result;
    }
//    @GET
//    @Path("TestEmailSender/{recipient}/{newpass}")
//    @Produces(MediaType.TEXT_PLAIN)
//    public ACRGBWSResult TestEmailSender(
//            @PathParam("recipient") String recipient,
//            @PathParam("newpass") String newpass) {
//        ACRGBWSResult result = utility.ACRGBWSResult();
//        //--------------------------------
//        EmailSender pass = new EmailSender();
//        //--------------------------------
//        Email email = new Email();
//        email.setRecipient(recipient);
//        email.setSubject("ACR-GB");
//        //---------------------------------
//        ACRGBWSResult insertresult = pass.EmailSender(dataSource, email, newpass);
//        result.setMessage(insertresult.getMessage());
//        result.setSuccess(insertresult.isSuccess());
//        result.setResult(insertresult.getResult());
//        return result;
//    }
//    @GET
//    @Path("ValidateClaims/{useries}")
//    @Produces(MediaType.APPLICATION_JSON)
//    public ACRGBWSResult ValidateClaims(
//            @HeaderParam("token") String token,
//            @PathParam("useries") String useries) {
//        ACRGBWSResult result = utility.ACRGBWSResult();
//        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
//        if (!GetPayLoad.isSuccess()) {
//            result.setMessage(GetPayLoad.getMessage());
//        } else {
//            ValidateClaims vc = new ValidateClaims();
//            ACRGBWSResult vcResult = vc.ValidateClaims(dataSource, useries);
//            result.setMessage(vcResult.getMessage());
//            result.setSuccess(vcResult.isSuccess());
//            result.setResult(vcResult.getResult());
//        }
//        return result;
//    }

    @GET
    @Path("GetUserAccount/{puserid}/{tags}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetUserAccount(
            @HeaderParam("token") String token,
            @PathParam("puserid") String puserid,
            @PathParam("tags") String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            if (!utility.GetPayload(dataSource, token).isSuccess()) {
                result.setMessage(utility.GetPayload(dataSource, token).getMessage());
            } else {
                ArrayList<User> userList = new ArrayList<>();
                ACRGBWSResult getResult = new Methods().GETROLE(dataSource, puserid.trim(), "ACTIVE");
                if (getResult.isSuccess()) {
                    ACRGBWSResult getMultiResult = new Methods().GETROLEREVERESEMULTIPLE(dataSource, getResult.getResult().trim(), tags.toUpperCase().trim());
                    if (getMultiResult.isSuccess()) {
                        List<String> restBList = Arrays.asList(getMultiResult.getResult().split(","));
                        for (int i = 0; i < restBList.size(); i++) {
                            ACRGBWSResult getUser = new FetchMethods().ACR_USER(dataSource, tags.toUpperCase().trim(), restBList.get(i));
                            if (getUser.isSuccess()) {
                                List<User> mapUserList = Arrays.asList(utility.ObjectMapper().readValue(getUser.getResult(), User[].class));
                                for (int x = 0; x < mapUserList.size(); x++) {
                                    userList.add(mapUserList.get(x));
                                }
                            }
                        }
                    }
                }
                if (userList.size() > 0) {
                    result.setMessage("OK");
                    result.setResult(utility.ObjectMapper().writeValueAsString(userList));
                    result.setSuccess(true);
                } else {
                    result.setMessage("N/A");
                }
            }
        } catch (IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(ACRGBFETCH.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    @GET
    @Path("GetAffiliate/{puserid}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetAffiliate(
            @HeaderParam("token") String token,
            @PathParam("puserid") String puserid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            ACRGBWSResult getResult = new Methods().GETROLE(dataSource, puserid.trim(), "ACTIVE");
            if (getResult.isSuccess()) {
                ArrayList<HealthCareFacility> hciList = new ArrayList<>();
                ACRGBWSResult getRestA = new ProcessAffiliate().GETAFFILIATE(dataSource, "0", getResult.getResult(), "0");
                if (getRestA.isSuccess()) {
                    List<Appellate> affiliateList = Arrays.asList(utility.ObjectMapper().readValue(getRestA.getResult(), Appellate[].class));
                    for (int x = 0; x < affiliateList.size(); x++) {
                        ACRGBWSResult getFacility = new FetchMethods().GETFACILITYID(dataSource, affiliateList.get(x).getAccesscode());
                        if (getFacility.isSuccess()) {
                            HealthCareFacility facility = utility.ObjectMapper().readValue(getFacility.getResult(), HealthCareFacility.class);
                            hciList.add(facility);
                        }
                    }
                    if (hciList.size() > 0) {
                        result.setMessage("OK");
                        result.setResult(utility.ObjectMapper().writeValueAsString(hciList));
                        result.setSuccess(true);
                    } else {
                        result.setMessage("N/A");
                    }
                } else {
                    result.setMessage(getResult.getMessage());
                }
            } else {
                result.setMessage(getResult.getMessage());
            }
        } catch (IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(ACRGBFETCH.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET PREVIOS ENDED CONTRACT
    // ustate  =  OPEN | CLOSED
    //reqtype = HCPN | HCI | FINALBALANCE
    //utags = ACTIVE | INACTIVE
    @GET
    @Path("GetCurrentRunningBalance/{paccount}/{reqtype}/{ustate}/{utags}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetCurrentRunningBalance(
            @HeaderParam("token") String token,
            @PathParam("paccount") String paccount,
            @PathParam("reqtype") String reqtype,
            @PathParam("ustate") String ustate,
            @PathParam("utags") String utags) {   //TAGS MUST BE LEVELACCESS
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            CurrentBalance cb = new CurrentBalance();
            //CHECK IF THERE'S AN OPEN PREVOIUS CONTRACT
            switch (reqtype.toUpperCase()) {
                case "HCPN": {
                    result = cb.OpenEndedHCPNContract(dataSource, utags, paccount, ustate);
                    break;
                }
                case "HCI": {
                    result = cb.OpenEndedHCIContract(dataSource, utags, paccount, ustate);
                    break;
                }
                case "FINAL": {
                    result = cb.GETFINALBALANCE(dataSource, utags, paccount);
                    break;
                }
                default: {
                    result.setMessage("NO DATA FOUND");
                    break;
                }
            }
        }
        return result;
    }

//    @GET
//    @Path("GetApexFacility")
//    @Produces(MediaType.APPLICATION_JSON)
//    public ACRGBWSResult GetAllApexFacility(
//            @HeaderParam("token") String token) {
//        ACRGBWSResult result = utility.ACRGBWSResult();
//        result.setMessage("");
//        result.setResult("");
//        result.setSuccess(false);
//        ACRGBWSResult GetPayLoad = utility.GetPayload(dataSource, token);
//        if (!GetPayLoad.isSuccess()) {
//            result.setMessage(GetPayLoad.getMessage());
//        } else {
//            ACRGBWSResult getResult = new FetchMethods().ACR_HCF(dataSource);
//            result.setMessage(getResult.getMessage());
//            result.setResult(getResult.getResult());
//            result.setSuccess(getResult.isSuccess());
//        }
//        return result;
//    }
    //GET ASSETS TYPE TBL
    @GET
    @Path("GetCF5Data")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetCF5Data(
            @HeaderParam("token") String token,
            @HeaderParam("series") String series) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            result = new CF5Data().INFO(dataSource, series);
        }
        return result;
    }

    @GET
    @Path("GetServerDateTime")
    @Produces(MediaType.APPLICATION_JSON)
    public String GetServerDateTime() {
        String result = "";
        SimpleDateFormat sdf = utility.SimpleDateFormat("MM-dd-yyyy hh:mm:ss a");
        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT SYSDATE FROM DUAL";
            NamedParameterStatement SDxVal = new NamedParameterStatement(connection, query);
            SDxVal.execute();
            ResultSet rest = SDxVal.executeQuery();
            if (rest.next()) {
                result = "SERVER DATE AND TIME : " + String.valueOf(sdf.format(rest.getDate("SYSDATE")));
            }
        } catch (SQLException ex) {
            result = ex.toString();
            Logger.getLogger(ACRGBFETCH.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

//    TEST METHOD EXPOSED
//    @GET
//    @Path("TEST")
//    @Produces(MediaType.APPLICATION_JSON)
//    public ACRGBWSResult TEST() {
//        return new FetchMethods().ACR_USER_LEVEL(dataSource, "ACTIVE");
//    }
//    
//    @GET
//    @Path("GetFilePath")
//    @Produces(MediaType.APPLICATION_JSON)
//    public ACRGBWSResult GetFilePath() {
//        ACRGBWSResult result = utility.ACRGBWSResult();
//        result.setMessage(utility.GetString("Path"));
//        result.setResult("");
//        result.setSuccess(false);
//        return result;
//    }
}
