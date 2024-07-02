/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb;

import acrgb.method.BookingMethod;
import acrgb.method.ContractHistory;
import acrgb.method.ContractMethod;
import acrgb.method.FetchMethods;
import acrgb.method.Forgetpassword;
import acrgb.method.GenerateRandomPassword;
import acrgb.method.LedgerMethod;
import acrgb.method.Methods;
import acrgb.method.UpdateMethods;
import acrgb.structure.ACRGBWSResult;
import acrgb.utility.Utility;
import java.sql.SQLException;
import java.text.ParseException;
import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
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
 * @author MinoSun
 */
@Path("ACRGBFETCH")
@RequestScoped
public class ACRGBFETCH {

    /**
     * Creates a new instance of ACRGBFETCH
     */
    public ACRGBFETCH() {
    }

    @Resource(lookup = "jdbc/acrgb")
    private DataSource dataSource;

    private final Utility utility = new Utility();
    private final FetchMethods fetchmethods = new FetchMethods();
    private final Methods methods = new Methods();
    private final LedgerMethod lm = new LedgerMethod();
    private final ContractMethod con = new ContractMethod();
    private final UpdateMethods um = new UpdateMethods();
    private final BookingMethod bm = new BookingMethod();

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
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult getResult = fetchmethods.ACR_ASSETS(dataSource, tags, phcfid);
            result.setMessage(getResult.getMessage());
            result.setResult(getResult.getResult());
            result.setSuccess(getResult.isSuccess());
        }

        return result;
    }

    //GET  HCI NET ASSETS TBL FINAL
    @GET
    @Path("GetContract/{tags}/{puserid}/{level}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetContract(@HeaderParam("token") String token, @PathParam("tags") String tags,
            @PathParam("puserid") String puserid,
            @PathParam("level") String level) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            switch (level.toUpperCase()) {
                case "PRO": {//puserid = prouseraccount ID
                    ACRGBWSResult getResultA = fetchmethods.ACR_CONTRACTPROID(dataSource, tags.trim().toUpperCase(), puserid);//GET CONTRACT USING USERID OF PRO USER ACCOUNT
                    result.setMessage(getResultA.getMessage());
                    result.setResult(getResultA.getResult());
                    result.setSuccess(getResultA.isSuccess());
                    break;
                }
                case "MB": { //puserid = hcpnuseraccount ID
                    ACRGBWSResult getResultB = fetchmethods.GETCONTRACTUNDERMB(dataSource, tags.trim().toUpperCase(), puserid);//GET CONTRACT USING USERID OF HCPN USER ACCOUNT
                    result.setMessage(getResultB.getMessage());
                    result.setResult(getResultB.getResult());
                    result.setSuccess(getResultB.isSuccess());
                    break;
                }
                case "PHICAPEX": {//puserid = 0
                    ACRGBWSResult getResultC = fetchmethods.ACR_CONTRACT(dataSource, tags.trim().toUpperCase(), puserid);//GET CONTRACT OF ALL APEX FACILITY
                    result.setMessage(getResultC.getMessage());
                    result.setResult(getResultC.getResult());
                    result.setSuccess(getResultC.isSuccess());
                    break;
                }
                case "PHICHCPN": {//puserid = 0 
                    ACRGBWSResult getResultD = fetchmethods.GETALLHCPNCONTRACT(dataSource, tags.trim().toUpperCase(), puserid);//GET CONTRACT OF ALL HCPN/NETWORK
                    result.setMessage(getResultD.getMessage());
                    result.setResult(getResultD.getResult());
                    result.setSuccess(getResultD.isSuccess());
                    break;
                }
                //GetFacilityContractUsingHCPNCode  GET FACILITY CONTRACT USING MB ACCOUNT USERID
                case "HCPN": {
                    ACRGBWSResult getResultE = fetchmethods.GetFacilityContractUsingHCPNAccountUserID(dataSource, tags.trim().toUpperCase(), puserid);//GET CONTRACT OF ALL APEX FACILITY
                    result.setMessage(getResultE.getMessage());
                    result.setResult(getResultE.getResult());
                    result.setSuccess(getResultE.isSuccess());
                    break;
                }
                // GET ALL FACILITY CONTRACT USING HCPN CONTROL CODE   IN PRO LEVEL  
                case "HCIHCPNCON": {
                    ACRGBWSResult getResultF = fetchmethods.GetFacilityContractUsingHCPNCodeS(dataSource, tags.trim().toUpperCase(), puserid.toUpperCase());//GET CONTRACT OF ALL APEX FACILITY
                    result.setMessage(getResultF.getMessage());
                    result.setResult(getResultF.getResult());
                    result.setSuccess(getResultF.isSuccess());
                    break;
                }
                // GET FACILITY CONTRACT USING ACCOUNT USERID
                case "FACILITYCONOWN": {
                    ACRGBWSResult GetResult = con.GETCONTRACTOFFACILITY(dataSource, tags, puserid);
                    result.setMessage(GetResult.getMessage());
                    result.setResult(GetResult.getResult());
                    result.setSuccess(false);
                    break;
                }
                // GET HCPN CONTRACT USING ACCOUNT USERID
                case "HCPNCONOWN": {
                    ACRGBWSResult GetResult = con.GETCONTRACTOFHCPN(dataSource, tags, puserid);
                    result.setMessage(GetResult.getMessage());
                    result.setResult(GetResult.getResult());
                    result.setSuccess(false);
                    break;
                }
                // GET PRO CONTRACT USING ACCOUNT USERID
                case "PROCONOWN": {
                    ACRGBWSResult GetResult = con.GETCONTRACTOFPRO(dataSource, tags, puserid);
                    result.setMessage(GetResult.getMessage());
                    result.setResult(GetResult.getResult());
                    result.setSuccess(false);
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

    //GET  ACR USER INDEX TBL
    @GET
    @Path("GetTranch/{tags}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetTranch(@HeaderParam("token") String token, @PathParam("tags") String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult getResult = fetchmethods.ACR_TRANCH(dataSource, tags);
            result.setMessage(getResult.getMessage());
            result.setResult(getResult.getResult());
            result.setSuccess(getResult.isSuccess());
        }
        return result;
    }

    //GET  ACR USER TABLE
    @GET
    @Path("GetUserInfo/{tags}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetUserInfo(@HeaderParam("token") String token, @PathParam("tags") String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult getResult = fetchmethods.ACR_USER_DETAILS(dataSource, tags);
            result.setMessage(getResult.getMessage());
            result.setResult(getResult.getResult());
            result.setSuccess(getResult.isSuccess());
        }
        return result;
    }

    //GET  HCI FACILITY
    @GET
    @Path("GetUserLevel/{tags}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetUserLevel(@HeaderParam("token") String token, @PathParam("tags") String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult getResult = fetchmethods.ACR_USER_LEVEL(dataSource, tags);
            result.setMessage(getResult.getMessage());
            result.setResult(getResult.getResult());
            result.setSuccess(getResult.isSuccess());
        }
        return result;
    }

    //GET  HCI FACILITY
    @GET
    @Path("GetUser/{tags}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetUser(@HeaderParam("token") String token, @PathParam("tags") String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult getResult = fetchmethods.ACR_USER(dataSource, tags);
            result.setMessage(getResult.getMessage());
            result.setResult(getResult.getResult());
            result.setSuccess(getResult.isSuccess());
        }
        return result;
    }

    @GET
    @Path("GetPro/{tags}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetPro(@HeaderParam("token") String token, @PathParam("tags") String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setSuccess(false);
        result.setResult("");
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult getResult = fetchmethods.ACR_PRO(dataSource, tags.trim());
            result.setMessage(getResult.getMessage());
            result.setResult(getResult.getResult());
            result.setSuccess(getResult.isSuccess());
        }
        return result;
    }

    @GET
    @Path("GetRoleIndex/{puserid}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetRoleIndex(@HeaderParam("token") String token, @PathParam("puserid") String puserid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult getResult = fetchmethods.GETUSERROLEINDEX(dataSource, puserid);
            result.setMessage(getResult.getMessage());
            result.setResult(getResult.getResult());
            result.setSuccess(getResult.isSuccess());
        }
        return result;
    }
//SUMMARY  / CONTRACT

    @GET
    @Path("GetSummary/{tags}/{userid}/{datefrom}/{dateto}/{type}/{hcilist}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetSummary(@HeaderParam("token") String token,
            @PathParam("datefrom") String datefrom,
            @PathParam("dateto") String dateto,
            @PathParam("tags") String tags,
            @PathParam("userid") String userid,
            @PathParam("type") String type,
            @PathParam("hcilist") String hcilist) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (utility.IsValidDate(datefrom) && utility.IsValidDate(dateto)) {
            ACRGBWSResult GetPayLoad = utility.GetPayload(token);
            if (!GetPayLoad.isSuccess()) {
                result.setMessage(GetPayLoad.getMessage());
            } else {
                switch (type.toUpperCase().trim()) {
                    case "SUMMARY": {
                        ACRGBWSResult getResult = methods.GetBaseAmountForSummary(dataSource, tags, userid, datefrom, dateto, "ACTIVE".toUpperCase().trim(), hcilist);
                        result.setMessage(getResult.getMessage());
                        result.setResult(getResult.getResult());
                        result.setSuccess(getResult.isSuccess());
                        break;
                    }
                    case "CONTRACT": {
                        ACRGBWSResult getResult = methods.GetBaseAmountForContract(dataSource, tags, userid, datefrom, dateto, "ACTIVE".toUpperCase().trim());
                        result.setMessage(getResult.getMessage());
                        result.setResult(getResult.getResult());
                        result.setSuccess(getResult.isSuccess());
                        break;
                    }
                    default:
                        result.setMessage("REQUEST TYPE NOT FOUND");
                        break;
                }
            }
        } else {
            result.setMessage("DATE FORMAT IS NOT VALID");
        }
        return result;
    }
//------------------------------------------------------------
    //GET LEVELNAME

    @GET
    @Path("GetLevel/{levid}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetLevel(@HeaderParam("token") String token, @PathParam("levid") String levid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult getResult = fetchmethods.GETUSERLEVEL(dataSource, levid);
            result.setMessage(getResult.getMessage());
            result.setResult(getResult.getResult());
            result.setSuccess(getResult.isSuccess());
        }

        return result;
    }
//------------------------------------------------------------
    //GET USER DETAILS

    @GET
    @Path("GETFULLDETAILS/{userid}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GETFULLDETAILS(@HeaderParam("token") String token, @PathParam("userid") String userid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult getResult = fetchmethods.GETFULLDETAILS(dataSource, userid);
            result.setMessage(getResult.getMessage());
            result.setResult(getResult.getResult());
            result.setSuccess(getResult.isSuccess());
        }
        return result;
    }
//------------------------------------------------------------
    //GET ASSETS WITH PARAMETER

    @GET
    @Path("GETASSETSWITHPARAM/{phcfid}/{conid}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GETASSETSWITHPARAM(@HeaderParam("token") String token, @PathParam("phcfid") String phcfid, @PathParam("conid") String conid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult getResult = fetchmethods.GETASSETSWITHPARAM(dataSource, phcfid, conid);
            result.setMessage(getResult.getMessage());
            result.setResult(getResult.getResult());
            result.setSuccess(getResult.isSuccess());
        }
        return result;
    }
//------------------------------------------------------------
    //

    @GET
    @Path("GetActivityLogs")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetActivityLogs(@HeaderParam("token") String token) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult getResult = fetchmethods.ACRACTIVTYLOGS(dataSource);
            result.setMessage(getResult.getMessage());
            result.setResult(getResult.getResult());
            result.setSuccess(getResult.isSuccess());
        }
        return result;
    }
//------------------------------------------------------------
    //GET ASSETS WITH PARAMETER

    @GET
    @Path("GetLogWithParam/{userid}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetLogWithParam(@HeaderParam("token") String token, @PathParam("userid") String userid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult getResult = methods.GetLogsWithID(dataSource, userid);
            result.setMessage(getResult.getMessage());
            result.setResult(getResult.getResult());
            result.setSuccess(getResult.isSuccess());
        }
        return result;
    }
//------------------------------------------------------------
    //GET  REQUEST USING MB USER ID

    @GET
    @Path("GetMBRequest/{userid}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetMBRequest(@HeaderParam("token") String token, @PathParam("userid") String userid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult getResult = methods.FetchMBRequest(dataSource, userid);
            result.setMessage(getResult.getMessage());
            result.setResult(getResult.getResult());
            result.setSuccess(getResult.isSuccess());
        }
        return result;
    }
//------------------------------------------------------------
    //GET HCPN

    @GET
    @Path("GetManagingBoard/{tags}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetManagingBoard(@HeaderParam("token") String token, @PathParam("tags") String tags) throws ParseException, SQLException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult getResult = fetchmethods.GetManagingBoard(dataSource, tags);
            result.setMessage(getResult.getMessage());
            result.setResult(getResult.getResult());
            result.setSuccess(getResult.isSuccess());
        }
        return result;
    }
//------------------------------------------------------------    
    //GET GET FACILITY USING PRO USERID

    @GET
    @Path("GetFacilityUsingProAccountUserID/{pid}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetFacilityUsingProAccountUserID(@HeaderParam("token") String token,
            @PathParam("pid") String pid) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult getResult = methods.GETROLEWITHID(dataSource, pid, "ACTIVE");
            result.setMessage(getResult.getMessage());
            result.setResult(getResult.getResult());
            result.setSuccess(getResult.isSuccess());
        }
        return result;
    }
//------------------------------------------------------------

    @GET
    @Path("GETALLFACILITY/{tags}/{userid}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GETALLFACILITY(@HeaderParam("token") String token, @PathParam("tags") String tags,
            @PathParam("userid") String userid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            switch (tags.toUpperCase()) {
                case "ALL":
                    ACRGBWSResult resultAll = fetchmethods.GETALLFACILITY(dataSource, "ACTIVE");
                    result.setMessage(resultAll.getMessage());
                    result.setSuccess(resultAll.isSuccess());
                    result.setResult(resultAll.getResult());
                    break;
                case "HCPN"://GET FACILITY USINNG HCPN ACCOUNT USERID
                    ACRGBWSResult resultHCPN = fetchmethods.GETFACILITYUNDERMB(dataSource, userid, "ACTIVE");
                    result.setMessage(resultHCPN.getMessage());
                    result.setSuccess(resultHCPN.isSuccess());
                    result.setResult(resultHCPN.getResult());
                    break;
                case "FACILITY"://GET FACILITY USINNG HCF CODE
                    break;
            }
        }
        return result;
    }
//------------------------------------------------------------
    //GET ASSETS WITH PARAMETER

    @GET
    @Path("GetManagingBoardWithProID/{proid}/{levelname}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetManagingBoardWithProID(@HeaderParam("token") String token, @PathParam("proid") String proid,
            @PathParam("levelname") String levelname) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            switch (levelname.toUpperCase().trim()) {
                case "PRO":
                    ACRGBWSResult getResult = methods.GETALLMBWITHPROID(dataSource, proid);
                    result.setMessage(getResult.getMessage());
                    result.setResult(getResult.getResult());
                    result.setSuccess(getResult.isSuccess());
                    break;
                case "PROLEDGER":
                    ACRGBWSResult getResultLedger = methods.GETALLMBWITHPROIDFORLEDGER(dataSource, proid);
                    result.setMessage(getResultLedger.getMessage());
                    result.setResult(getResultLedger.getResult());
                    result.setSuccess(getResultLedger.isSuccess());
                    break;
                case "PHIC":
                    ACRGBWSResult getphicResult = methods.GETALLMBWITHPROCODE(dataSource, proid, "ACTIVE");
                    result.setMessage(getphicResult.getMessage());
                    result.setResult(getphicResult.getResult());
                    result.setSuccess(getphicResult.isSuccess());
                    break;
                default:
                    result.setMessage("LEVEL TAGS NOT FOUND");
                    break;
            }
        }
        return result;
    }
//------------------------------------------------------------

    @GET
    @Path("GetMBUsingMBID/{pid}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetMBUsingMBID(@HeaderParam("token") String token, @PathParam("pid") String pid) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult getResult = methods.GETALLFACILITYWITHMBID(dataSource, pid, "ACTIVE");
            result.setMessage(getResult.getMessage());
            result.setResult(getResult.getResult());
            result.setSuccess(getResult.isSuccess());
        }
        return result;
    }

    @GET
    @Path("GetBalanceTerminatedContract/{userid}/{levelname}/{tags}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetBalanceTerminatedContract(@HeaderParam("token") String token, @PathParam("userid") String userid,
            @PathParam("levelname") String levelname,
            @PathParam("tags") String tags) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            switch (levelname.toUpperCase().trim()) {
                case "PRO":
                    //GET TERMINATED CONTRACT OF FACILITY UNDER HCPN UNDER PRO USING PRO ACCOUNT USERID
                    ACRGBWSResult getResult = methods.GetRemainingBalanceForTerminatedContract(dataSource, userid, tags);
                    result.setMessage(getResult.getMessage());
                    result.setResult(getResult.getResult());
                    result.setSuccess(getResult.isSuccess());
                    break;
                case "TERMINATEAPEX":
                    //GET TERMINATED CONTRACT OF APEX
                    ACRGBWSResult getResultTerminateApex = methods.GetRemainingBalanceForTerminatedContractApex(dataSource);
                    result.setMessage(getResultTerminateApex.getMessage());
                    result.setResult(getResultTerminateApex.getResult());
                    result.setSuccess(getResultTerminateApex.isSuccess());
                    break;
                case "ENDCONAPEX":
                    //GET END CONTRACT OF APEX
                    ACRGBWSResult getResultEndApex = methods.GetRemainingBalanceForEndContractApex(dataSource);
                    result.setMessage(getResultEndApex.getMessage());
                    result.setResult(getResultEndApex.getResult());
                    result.setSuccess(getResultEndApex.isSuccess());
                    break;
                case "NONRENEW":
                    //GET END CONTRACT OF FACILITY UNDER HCPN UNDER PRO USING PRO ACCOUNT USERID
                    ACRGBWSResult getResultnNonRenew = methods.GetRemainingBalanceForEndContract(dataSource, userid, tags);
                    result.setMessage(getResultnNonRenew.getMessage());
                    result.setResult(getResultnNonRenew.getResult());
                    result.setSuccess(getResultnNonRenew.isSuccess());
                    break;
                default:
                    result.setMessage("LEVEL STATUS NOT VALID");
                    break;
            }
        }
        return result;
    }

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
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            switch (tags.toUpperCase().trim()) {
                case "HCPN": //GET LEDGER PER HCPN
                    //hcpncode  MUST BE HCPNCONTROLCODE
                    //CONTRACT MUST BE CONID
                    //TAGS MUST BE HCPN
                    switch (type.toUpperCase()) {
                        case "ACTIVE": {
                            ACRGBWSResult getResultHCPN = lm.GETLedgerPerContractHCPN(dataSource, hcpncode, contract, type);
                            result.setMessage(getResultHCPN.getMessage());
                            result.setResult(getResultHCPN.getResult());
                            result.setSuccess(getResultHCPN.isSuccess());
                            break;
                        }
                        case "INACTIVE": {
                            ACRGBWSResult getResultHCPN = lm.GETLedgerPerContractHCPNLedger(dataSource, hcpncode, contract);
                            result.setMessage(getResultHCPN.getMessage());
                            result.setResult(getResultHCPN.getResult());
                            result.setSuccess(getResultHCPN.isSuccess());
                            break;
                        }
                        default: {
                            result.setMessage("Contract type not found " + type);
                            break;
                        }
                    }
                    break;
                case "FACILITY"://GET LEDGER PER FACILITY
                    //hcpncode  MUST BE THE USERID OF PRO ACCOUNT
                    //CONTRACT DECLARE 0 VALUE
                    //TAGS MUST BE HCPNALL
                    switch (type.toUpperCase()) {
                        case "ACTIVE": {
                            ACRGBWSResult getResultAllHCPN = lm.GETLedgerAllContractAPEXActive(dataSource, hcpncode, contract);//hcpncode  user ID of account of PROUSER
                            result.setMessage(getResultAllHCPN.getMessage());
                            result.setResult(getResultAllHCPN.getResult());
                            result.setSuccess(getResultAllHCPN.isSuccess());
                            break;
                        }
                        case "INACTIVE": {
                            ACRGBWSResult getResultAllHCPN = lm.GETLedgerAllContractAPEXInactive(dataSource, hcpncode, contract);//hcpncode  user ID of account of PROUSER
                            result.setMessage(getResultAllHCPN.getMessage());
                            result.setResult(getResultAllHCPN.getResult());
                            result.setSuccess(getResultAllHCPN.isSuccess());
                            break;
                        }
                        default: {
                            result.setMessage("Contract type not found " + type);
                            break;
                        }

                    }
                    break;
                default:
                    result.setMessage("TAGS NOT FOUND " + tags);
                    break;
            }
        }
        return result;
    }

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
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult getConDateResult = con.GETCONDATE(dataSource, tags);
            result.setMessage(getConDateResult.getMessage());
            result.setResult(getConDateResult.getResult());
            result.setSuccess(getConDateResult.isSuccess());
        }
        return result;
    }

    //GET TRIGGER AUTOEND CONTRACT DATE
    @GET
    @Path("AutoEndContractDate/{ucondateid}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult AutoEndContractDate(
            @HeaderParam("token") String token,
            @PathParam("ucondateid") String ucondateid) throws ParseException {   //TAGS MUST BE LEVELACCESS
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult updatecondate = um.UPDATEROLEINDEX(dataSource, "00", ucondateid, "NONUPDATE"); //ENDCONDATE
            result.setMessage(updatecondate.getMessage());
            result.setResult(updatecondate.getResult());
            result.setSuccess(updatecondate.isSuccess());
        }
        return result;
    }

    //GET TRIGGER AUTOEND CONTRACT DATE
    @GET
    @Path("GetRandomPasscode")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetRandomPasscode(@HeaderParam("token") String token) {   //TAGS MUST BE LEVELACCESS
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            GenerateRandomPassword generaterandompasscode = new GenerateRandomPassword();
            result.setMessage("Generated random passcode");
            result.setResult(generaterandompasscode.GenerateRandomPassword(10));
            result.setSuccess(true);
        }
        return result;
    }

    //GET TRIGGER AUTOEND CONTRACT DATE
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
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult getResult = con.GETPREVIOUSBALANCE(dataSource, paccount, contractid);
            result.setMessage(getResult.getMessage());
            result.setResult(getResult.getResult());
            result.setSuccess(getResult.isSuccess());
        }
        return result;
    }

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
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult BookingResult = bm.GETALLCLAIMS(dataSource, hcpncode, contractid, tags);
            result.setMessage(BookingResult.getMessage());
            result.setResult(BookingResult.getResult());
            result.setSuccess(BookingResult.isSuccess());
        }
        return result;
    }

    //GET EMAIL CREDEDNTIALS
    @GET
    @Path("GetEmailCredentials")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetEmailCredentials(@HeaderParam("token") String token) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            Forgetpassword forgetpass = new Forgetpassword();
            ACRGBWSResult BookingResult = forgetpass.GetEmailSender(dataSource);
            result.setMessage(BookingResult.getMessage());
            result.setResult(BookingResult.getResult());
            result.setSuccess(BookingResult.isSuccess());
        }
        return result;
    }

    @GET
    @Path("GETOLDPASSCODE/{puserid}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GETOLDPASSCODE(
            @HeaderParam("token") String token,
            @PathParam("puserid") String puserid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult BookingResult = fetchmethods.GETOLDPASSCODE(dataSource, puserid);
            result.setMessage(BookingResult.getMessage());
            result.setResult(BookingResult.getResult());
            result.setSuccess(BookingResult.isSuccess());
        }
        return result;
    }

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
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult getResult = fetchmethods.CONTRACTWITHQUARTER(dataSource, tags.toUpperCase().trim(), uprocode.trim());
            result.setMessage(getResult.getMessage());
            result.setResult(getResult.getResult());
            result.setSuccess(getResult.isSuccess());
        }
        return result;
    }

    @GET
    @Path("GETACR_BOOKING")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GETACR_BOOKING(
            @HeaderParam("token") String token) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult BookingResult = fetchmethods.GETACR_BOOKING(dataSource);
            result.setMessage(BookingResult.getMessage());
            result.setResult(BookingResult.getResult());
            result.setSuccess(BookingResult.isSuccess());
        }
        return result;
    }

    //INSERT EMAIL CREDEDNTIALS
    @GET
    @Path("ValidateToken")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult PostEmailCredentials(@HeaderParam("token") String token) {
        ACRGBWSResult result = utility.GetPayload(token);
        return result;
    }

    //GET ALL CONTRACT
    //INSERT EMAIL CREDEDNTIALS
    @GET
    @Path("GetllContract/{tags}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetllContract(
            @HeaderParam("token") String token, @PathParam("tags") String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult GetAllContract = fetchmethods.GETALLCONTRACT(dataSource, tags, "0");
            result.setMessage(GetAllContract.getMessage());
            result.setResult(GetAllContract.getResult());
            result.setSuccess(GetAllContract.isSuccess());
        }
        return result;
    }

    @GET
    @Path("GetContractHistory/{puserid}/{accountlevel}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetContractHistory(
            @HeaderParam("token") String token,
            @PathParam("puserid") String puserid,
            @PathParam("accountlevel") String accountlevel) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ContractHistory ch = new ContractHistory();
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            if (puserid.equals("0")) {
                switch (accountlevel.toUpperCase().trim()) {
                    case "ALLHCPN"://GET ALL ENDED CONTRACT OF HCPN  USING HCPN CODE
                    {
                        ACRGBWSResult getContractResult = ch.GetAllHCPNContract(dataSource);
                        result.setMessage(getContractResult.getMessage());
                        result.setResult(getContractResult.getResult());
                        result.setSuccess(getContractResult.isSuccess());
                        break;
                    }
                    case "ALLAPEX"://GET ALL ENDED CONTRACT OF APEX  USERID IS 0
                    {
                        ACRGBWSResult getContractResult = ch.GetAllAPEXContract(dataSource);
                        result.setMessage(getContractResult.getMessage());
                        result.setResult(getContractResult.getResult());
                        result.setSuccess(getContractResult.isSuccess());
                        break;
                    }
                    case "ALLAPRO"://GET ALL ENDED CONTRACT OF PRO
                    {
                        ACRGBWSResult getContractResult = ch.GetAllPROContract(dataSource);
                        result.setMessage(getContractResult.getMessage());
                        result.setResult(getContractResult.getResult());
                        result.setSuccess(getContractResult.isSuccess());
                        break;
                    }

                    default:
                        result.setMessage("TAGS NOT FOUND");
                        break;
                }
            } else {
                ACRGBWSResult GetRole = methods.GETROLE(dataSource, puserid, "ACTIVE");
                if (GetRole.isSuccess()) {
                    switch (accountlevel.toUpperCase().trim()) {
                        case "HCPN": //GET ENDED CONTRACT OF HCPN USING HCPN CODE
                        {
                            ACRGBWSResult GetContract = ch.GetHCPNContract(dataSource, GetRole.getResult().trim());
                            result.setMessage(GetContract.getMessage());
                            result.setResult(GetContract.getResult());
                            result.setSuccess(GetContract.isSuccess());
                            break;
                        }
                        case "FACILITY"://GET ENDED CONTRACT OF FACILITY USING PMCC NO
                        {
                            ACRGBWSResult GetContract = ch.GetHCIContract(dataSource, GetRole.getResult().trim());
                            result.setMessage(GetContract.getMessage());
                            result.setResult(GetContract.getResult());
                            result.setSuccess(GetContract.isSuccess());
                            break;
                        }
                        case "PRO"://GET ENDED CONTRACT OF PRO  USING PROCODE
                        {
                            ACRGBWSResult GetContract = ch.GetPROContract(dataSource, GetRole.getResult().trim());
                            result.setMessage(GetContract.getMessage());
                            result.setResult(GetContract.getResult());
                            result.setSuccess(GetContract.isSuccess());
                            break;
                        }
                        default:
                            result.setMessage("TAGS NOT FOUND");
                            break;
                    }

                } else {
                    result.setMessage(GetRole.getMessage());
                }

            }
        }

        return result;
    }

}
