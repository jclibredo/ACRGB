/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb;

import acrgb.method.FetchMethods;
import acrgb.method.Methods;
import acrgb.structure.ACRGBWSResult;
import acrgb.structure.UserLevel;
import acrgb.utility.Utility;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
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

    //GET ASSETS TYPE TBL
    @GET
    @Path("GetAssets/{tags}/{phcfid}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetAssets(
            @PathParam("tags") String tags,
            @PathParam("phcfid") String phcfid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (tags.isEmpty()) {
            result.setMessage("PATH PARAMETER IS EMPTY");
            result.setSuccess(false);
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
    public ACRGBWSResult GetContract(@PathParam("tags") String tags,
            @PathParam("puserid") String puserid,
            @PathParam("level") String level) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setSuccess(false);
        if (tags.isEmpty()) {
            result.setMessage("PATH PARAMETER IS EMPTY");
            result.setSuccess(false);
        } else {
            switch (level.toUpperCase()) {
                case "PRO": {//puserid = prouseraccount ID
                    ACRGBWSResult getResult = fetchmethods.ACR_CONTRACTPROID(dataSource, tags, puserid);//GET CONTRACT USING USERID OF PRO USER ACCOUNT
                    result.setMessage(getResult.getMessage());
                    result.setResult(getResult.getResult());
                    result.setSuccess(getResult.isSuccess());
                    break;
                }
                case "MB": {//puserid = hcpnuseraccount ID
                    ACRGBWSResult getResult = fetchmethods.GETCONTRACTUNDERMB(dataSource, tags, puserid);//GET CONTRACT USING USERID OF HCPN USER ACCOUNT
                    result.setMessage(getResult.getMessage());
                    result.setResult(getResult.getResult());
                    result.setSuccess(getResult.isSuccess());
                    break;
                }
                case "PHICAPEX": {//puserid = 0
                    ACRGBWSResult getResult = fetchmethods.ACR_CONTRACT(dataSource, tags, puserid);//GET CONTRACT OF ALL APEX FACILITY
                    result.setMessage(getResult.getMessage());
                    result.setResult(getResult.getResult());
                    result.setSuccess(getResult.isSuccess());
                    break;
                }
                case "PHICHCPN": { //puserid = 0
                    ACRGBWSResult getResult = fetchmethods.GETALLHCPNCONTRACT(dataSource, tags, puserid);//GET CONTRACT OF ALL APEX FACILITY
                    result.setMessage(getResult.getMessage());
                    result.setResult(getResult.getResult());
                    result.setSuccess(getResult.isSuccess());
                    break;
                }//GetFacilityContractUsingHCPNCode
                case "HCPN":
                    ACRGBWSResult getResult = fetchmethods.GetFacilityContractUsingHCPNCode(dataSource, tags, puserid);//GET CONTRACT OF ALL APEX FACILITY
                    result.setMessage(getResult.getMessage());
                    result.setResult(getResult.getResult());
                    result.setSuccess(getResult.isSuccess());
                    break;
                default:
                    result.setMessage(level + " IS NOT VALID");
                    break;
            }
        }
        return result;
    }

    //GET  ACR USER INDEX TBL
    @GET
    @Path("GetTranch/{tags}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetTranch(@PathParam("tags") String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (tags.isEmpty()) {
            result.setMessage("PATH PARAMETER IS EMPTY");
            result.setSuccess(false);
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
    public ACRGBWSResult GetUserInfo(@PathParam("tags") String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (tags.isEmpty()) {
            result.setMessage("PATH PARAMETER IS EMPTY");
            result.setSuccess(false);
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
    public ACRGBWSResult GetUserLevel(@PathParam("tags") String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (tags.isEmpty()) {
            result.setMessage("PATH PARAMETER IS EMPTY");
            result.setSuccess(false);
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
    public ACRGBWSResult GetUser(@PathParam("tags") String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (tags.isEmpty()) {
            result.setMessage("PATH PARAMETER IS EMPTY");
            result.setSuccess(false);
        } else {
            ACRGBWSResult getResult = fetchmethods.ACR_USER(dataSource, tags);
            result.setMessage(getResult.getMessage());
            result.setResult(getResult.getResult());
            result.setSuccess(getResult.isSuccess());
        }
        return result;
    }

    @GET
    @Path("GetPro")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetPro() {
        ACRGBWSResult result = utility.ACRGBWSResult();
        ACRGBWSResult getResult = fetchmethods.ACR_PRO(dataSource);
        result.setMessage(getResult.getMessage());
        result.setResult(getResult.getResult());
        result.setSuccess(getResult.isSuccess());
        return result;
    }

    @GET
    @Path("GetRoleIndex/{puserid}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetRoleIndex(@PathParam("puserid") String puserid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        ACRGBWSResult getResult = fetchmethods.GETUSERROLEINDEX(dataSource, puserid);
        result.setMessage(getResult.getMessage());
        result.setResult(getResult.getResult());
        result.setSuccess(getResult.isSuccess());
        return result;
    }

    @GET
    @Path("GetSummary")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetSummary() {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult getResult = methods.JOINCONHCFTBL(dataSource);
        result.setMessage(getResult.getMessage());
        result.setResult(getResult.getResult());
        result.setSuccess(getResult.isSuccess());
        return result;
    }

    //GET LEVELNAME
    @GET
    @Path("GetLevel/{levid}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetLevel(@PathParam("levid") String levid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (levid.isEmpty()) {
            result.setMessage("PATH PARAMETER IS EMPTY");
            result.setSuccess(false);
        } else if (!utility.IsValidNumber(levid)) {
            result.setMessage("LEVEL ID IS NOT VALID");
            result.setSuccess(false);
        } else {
            ACRGBWSResult getResult = fetchmethods.GETUSERLEVEL(dataSource, levid);
            result.setMessage(getResult.getMessage());
            result.setResult(getResult.getResult());
            result.setSuccess(getResult.isSuccess());
        }
        return result;
    }

    //GET USER DETAILS
    @GET
    @Path("GETFULLDETAILS/{userid}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GETFULLDETAILS(@PathParam("userid") String userid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (userid.isEmpty()) {
            result.setMessage("PATH PARAMETER IS EMPTY");
            result.setSuccess(false);
        } else if (!utility.IsValidNumber(userid)) {
            result.setMessage("USER ID IS NOT VALID");
            result.setSuccess(false);
        } else {
            ACRGBWSResult getResult = fetchmethods.GETFULLDETAILS(dataSource, userid);
            result.setMessage(getResult.getMessage());
            result.setResult(getResult.getResult());
            result.setSuccess(getResult.isSuccess());
        }
        return result;
    }

    //GET ASSETS WITH PARAMETER
    @GET
    @Path("GETASSETSWITHPARAM/{phcfid}/{conid}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GETASSETSWITHPARAM(@PathParam("phcfid") String phcfid, @PathParam("conid") String conid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (phcfid.isEmpty()) {
            result.setMessage("PATH PARAMETER IS EMPTY");
            result.setSuccess(false);
        } else if (!utility.IsValidNumber(phcfid)) {
            result.setMessage("FACILITY ID IS NOT VALID");
            result.setSuccess(false);
        } else {
            ACRGBWSResult getResult = fetchmethods.GETASSETSWITHPARAM(dataSource, phcfid, conid);
            result.setMessage(getResult.getMessage());
            result.setResult(getResult.getResult());
            result.setSuccess(getResult.isSuccess());
        }
        return result;
    }

    //
    @GET
    @Path("GetActivityLogs")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetActivityLogs() {
        ACRGBWSResult result = utility.ACRGBWSResult();
        ACRGBWSResult getResult = fetchmethods.ACRACTIVTYLOGS(dataSource);
        result.setMessage(getResult.getMessage());
        result.setResult(getResult.getResult());
        result.setSuccess(getResult.isSuccess());
        return result;
    }

    //GET ASSETS WITH PARAMETER
    @GET
    @Path("GetLogWithParam/{userid}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetLogWithParam(@PathParam("userid") String userid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (userid.isEmpty()) {
            result.setMessage("PATH PARAMETER IS EMPTY");
            result.setSuccess(false);
        } else if (!utility.IsValidNumber(userid)) {
            result.setMessage("USERID IS NOT VALID");
            result.setSuccess(false);
        } else {
            ACRGBWSResult getResult = methods.GetLogsWithID(dataSource, userid);
            result.setMessage(getResult.getMessage());
            result.setResult(getResult.getResult());
            result.setSuccess(getResult.isSuccess());
        }
        return result;
    }

    //Get Facility using FHID
//    @GET
//    @Path("GETFACILITY/{uhcfid}")
//    @Produces(MediaType.APPLICATION_JSON)
//    public ACRGBWSResult GETFACILITY(@PathParam("uhcfid") String uhcfid) {
//        ACRGBWSResult result = utility.ACRGBWSResult();
//        ACRGBWSResult hcf = fetchmethods.GETFACILITYID(dataSource, uhcfid);
//        result.setMessage(hcf.getMessage());
//        result.setSuccess(hcf.isSuccess());
//        result.setResult(hcf.getResult());
//        return result;
//
//    }
    @GET
    @Path("GetHealthFacilityBadget/{accesstags}/{puserid}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetHealthFacilityBadget(
            @PathParam("accesstags") String accesstags,
            @PathParam("puserid") String puserid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setSuccess(false);
        switch (accesstags.trim().toUpperCase()) {
            case "PHICPRO": {
                //GET FACILITY BUDGET USING MB SELECTED MBID
                ACRGBWSResult getResult = methods.MethodGetHealthFacilityBadgetUisngMBID(dataSource, puserid);
                result.setMessage(getResult.getMessage());
                result.setResult(getResult.getResult());
                result.setSuccess(getResult.isSuccess());
                break;
            }
            case "MB": {
                //GET FACILITY BUDGET USING MB USERACCOUNT ID
                ACRGBWSResult getResult = methods.MethodGetHealthFacilityBadget(dataSource, puserid);
                result.setMessage(getResult.getMessage());
                result.setResult(getResult.getResult());
                result.setSuccess(getResult.isSuccess());
                break;
            }
            default:
                result.setMessage("TAGS IS INVALID");
                break;
        }
        return result;
    }

    //GET  REQUEST USING MB USER ID
    @GET
    @Path("GetMBRequest/{userid}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetMBRequest(@PathParam("userid") String userid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        ACRGBWSResult getResult = methods.FetchMBRequest(dataSource, userid);
        result.setMessage(getResult.getMessage());
        result.setResult(getResult.getResult());
        result.setSuccess(getResult.isSuccess());
        return result;
    }

    //GET HCPN
    @GET
    @Path("GetManagingBoard")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetManagingBoard() throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult getResult = fetchmethods.GetManagingBoard(dataSource);
        result.setMessage(getResult.getMessage());
        result.setResult(getResult.getResult());
        result.setSuccess(getResult.isSuccess());
        return result;
    }

    @GET
    @Path("ValidateUserLevel/{levelname}/{tags}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult ValidateUserLevel(@PathParam("levelname") String levelname, @PathParam("tags") String tags) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            if (levelname.isEmpty()) {
                result.setMessage("LEVEL NAME IS REQUIRED");
                result.setSuccess(false);
            } else {
                int levcounter = 0;
                ACRGBWSResult getResult = fetchmethods.ACR_USER_LEVEL(dataSource, tags);
                if (getResult.isSuccess()) {
                    if (!getResult.getResult().isEmpty()) {
                        List<UserLevel> levelist = Arrays.asList(utility.ObjectMapper().readValue(getResult.getResult(), UserLevel[].class));
                        for (int g = 0; g < levelist.size(); g++) {
                            if (levelname.toUpperCase().equals(levelist.get(g).getLevname())) {
                                levcounter++;
                                break;
                            }
                        }
                        if (levcounter > 0) {
                            result.setMessage("OK");
                            result.setResult(levelname);
                            result.setSuccess(true);
                        } else {
                            result.setMessage("USER LEVEL NOT VALID");
                        }
                    } else {
                        result.setMessage("USER LEVEL NOT FOUND");
                    }
                } else {
                    result.setMessage(getResult.getMessage());
                    result.setResult(getResult.getResult());
                    result.setSuccess(getResult.isSuccess());
                }
            }
        } catch (IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(ACRGBFETCH.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET GET FACILITY USING PRO USERID
    @GET
    @Path("GetFacilityUsingProAccountUserID/{pid}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetFacilityUsingProAccountUserID(@PathParam("pid") String pid) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (pid.isEmpty()) {
            result.setMessage("PATH PARAMETER IS EMPTY");
            result.setSuccess(false);
        } else if (!utility.IsValidNumber(pid)) {
            result.setMessage("NUMBER FORMAT IS NOT VALID");
        } else {
            ACRGBWSResult getResult = methods.GETROLEWITHID(dataSource, pid);
            result.setMessage(getResult.getMessage());
            result.setResult(getResult.getResult());
            result.setSuccess(getResult.isSuccess());
        }
        return result;
    }

    //GET GET FACILITY USING MB USERID
    @GET
    @Path("GetMBUsingUserIDMBID/{pid}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetMBUsingUserIDMBID(@PathParam("pid") String pid) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (pid.isEmpty()) {
            result.setMessage("PATH PARAMETER IS EMPTY");
            result.setSuccess(false);
        } else if (!utility.IsValidNumber(pid)) {
            result.setMessage("NUMBER FORMAT IS NOT VALID");
        } else {
            ACRGBWSResult getResult = methods.GETFACILITYUNDERMBUSER(dataSource, pid);
            result.setMessage(getResult.getMessage());
            result.setResult(getResult.getResult());
            result.setSuccess(getResult.isSuccess());
        }
        return result;
    }

    //Get all facility (multiple)
    @GET
    @Path("GETALLFACILITY")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GETALLFACILITY() {
        ACRGBWSResult result = utility.ACRGBWSResult();
        ACRGBWSResult hcf = fetchmethods.GETALLFACILITY(dataSource);
        result.setMessage(hcf.getMessage());
        result.setSuccess(hcf.isSuccess());
        result.setResult(hcf.getResult());
        return result;

    }

    //GET ASSETS WITH PARAMETER
    @GET
    @Path("GetManagingBoardWithProID/{proid}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetManagingBoardWithProID(@PathParam("proid") String proid) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.IsValidNumber(proid)) {
            result.setMessage("NUMBER FORMAT IS NOT VALID");
        } else {
            ACRGBWSResult getResult = methods.GETALLMBWITHPROID(dataSource, proid);
            result.setMessage(getResult.getMessage());
            result.setResult(getResult.getResult());
            result.setSuccess(getResult.isSuccess());
        }
        return result;
    }
    

    @GET
    @Path("GetMBUsingMBID/{pid}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetMBUsingMBID(@PathParam("pid") String pid) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (pid.isEmpty()) {
            result.setMessage("PATH PARAMETER IS EMPTY");
        } else if (!utility.IsValidNumber(pid)) {
            result.setMessage("NUMBER FORMAT IS NOT VALID");
        } else {
            ACRGBWSResult getResult = methods.GETALLFACILITYWITHMBID(dataSource, pid);
            result.setMessage(getResult.getMessage());
            result.setResult(getResult.getResult());
            result.setSuccess(getResult.isSuccess());
        }
        return result;
    }

    //GENERATE REPORTS userid is userid of account
    @GET
    @Path("ReportsForMB/{tags}/{userid}/{level}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult ReportsForMB(@PathParam("tags") String tags,
            @PathParam("userid") String userid, @PathParam("level") String level) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        if (tags.equals("ACTIVE")) {
            switch (level.toUpperCase()) {
                case "MB":
                    ACRGBWSResult getResult = methods.GetReportsOfSelectedHCPN(dataSource, tags, userid);
                    result.setMessage(getResult.getMessage());
                    result.setResult(getResult.getResult());
                    result.setSuccess(getResult.isSuccess());
                    break;
                case "FACILITY":

                    break;
                default:
                    break;
            }
        } else {

        }
        return result;
    }

    //GETTING THE DATE SETTINGS
    @GET
    @Path("GetDateSettings")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetDateSettings() throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult getResult = fetchmethods.GETDATESETTINGS(dataSource);
        result.setMessage(getResult.getMessage());
        result.setResult(getResult.getResult());
        result.setSuccess(getResult.isSuccess());
        return result;
    }

    @GET
    @Path("GetBalanceTerminatedContract/{userid}/{levelname}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetBalanceTerminatedContract(@PathParam("userid") String userid,
            @PathParam("levelname") String levelname) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        switch (levelname.toUpperCase().trim()) {
            case "PRO":
                //GET TERMINATED CONTRACT OF FACILITY UNDER HCPN UNDER PRO USING PRO ACCOUNT USERID
                ACRGBWSResult getResult = methods.GetRemainingBalanceForTerminatedContract(dataSource, userid);
                result.setMessage(getResult.getMessage());
                result.setResult(getResult.getResult());
                result.setSuccess(getResult.isSuccess());
                break;
            case "APEX":
                  //GET TERMINATED CONTRACT OF APEX
                ACRGBWSResult getResultApex = methods.GetRemainingBalanceForTerminatedContractApex(dataSource);
                result.setMessage(getResultApex.getMessage());
                result.setResult(getResultApex.getResult());
                result.setSuccess(getResultApex.isSuccess());
                break;
            default:
                result.setMessage("LEVEL STATUS NOT VALID");
                break;
        }
        return result;
    }

}
