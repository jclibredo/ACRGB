/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb;

import acrgb.method.FetchMethods;
import acrgb.method.Methods;
import acrgb.structure.ACRGBWSResult;
import acrgb.utility.Utility;
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

    //GET ACCOUNT PAYABLE
    @GET
    @Path("GetArea/{tags}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetArea(@PathParam("tags") String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (tags.isEmpty()) {
            result.setMessage("PATH PARAMETER IS EMPTY");
            result.setSuccess(false);
        } else {
            ACRGBWSResult getResult = fetchmethods.ACR_AREA(dataSource, tags);
            result.setMessage(getResult.getMessage());
            result.setResult(getResult.getResult());
            result.setSuccess(getResult.isSuccess());
        }
        return result;
    }

    //GET ASSETS TABLE
    @GET
    @Path("GetAreaType/{tags}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetAreaType(@PathParam("tags") String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (tags.isEmpty()) {
            result.setMessage("PATH PARAMETER IS EMPTY");
            result.setSuccess(false);
        } else {
            ACRGBWSResult getResult = fetchmethods.ACR_AREA_TYPE(dataSource, tags);
            result.setMessage(getResult.getMessage());
            result.setResult(getResult.getResult());
            result.setSuccess(getResult.isSuccess());
        }
        return result;
    }

    //GET ASSETS TYPE TBL
    @GET
    @Path("GetAssets/{tags}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetAssets(@PathParam("tags") String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (tags.isEmpty()) {
            result.setMessage("PATH PARAMETER IS EMPTY");
            result.setSuccess(false);
        } else {
            ACRGBWSResult getResult = fetchmethods.ACR_ASSETS(dataSource, tags);
            result.setMessage(getResult.getMessage());
            result.setResult(getResult.getResult());
            result.setSuccess(getResult.isSuccess());
        }
        return result;
    }

    //GET  HCI NET ASSETS TBL
    @GET
    @Path("GetContract/{tags}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetContract(@PathParam("tags") String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (tags.isEmpty()) {
            result.setMessage("PATH PARAMETER IS EMPTY");
            result.setSuccess(false);
        } else {
            ACRGBWSResult getResult = fetchmethods.ACR_CONTRACT(dataSource, tags);
            result.setMessage(getResult.getMessage());
            result.setResult(getResult.getResult());
            result.setSuccess(getResult.isSuccess());
        }
        return result;
    }

    //GET  ACR ROLE TBLE
    @GET
    @Path("GetHealthCareFacility/{tags}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetHealthCareFacility(@PathParam("tags") String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (tags.isEmpty()) {
            result.setMessage("PATH PARAMETER IS EMPTY");
            result.setSuccess(false);
        } else {
            ACRGBWSResult getResult = fetchmethods.ACR_HCF(dataSource, tags);
            result.setMessage(getResult.getMessage());
            result.setResult(getResult.getResult());
            result.setSuccess(getResult.isSuccess());
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
    @Path("GetPro/{tags}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetPro(@PathParam("tags") String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (tags.isEmpty()) {
            result.setMessage("PATH PARAMETER IS EMPTY");
            result.setSuccess(false);
        } else {
            ACRGBWSResult getResult = fetchmethods.ACR_PRO(dataSource, tags);
            result.setMessage(getResult.getMessage());
            result.setResult(getResult.getResult());
            result.setSuccess(getResult.isSuccess());
        }
        return result;
    }

    @GET
    @Path("GetRoleIndex")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GetRoleIndex() {
        ACRGBWSResult result = utility.ACRGBWSResult();
        ACRGBWSResult getResult = fetchmethods.GETUSERROLEINDEX(dataSource);
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
    @Path("GETASSETSWITHPARAM/{phcfid}")
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult GETASSETSWITHPARAM(@PathParam("phcfid") String phcfid) {
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
            ACRGBWSResult getResult = fetchmethods.GETASSETSWITHPARAM(dataSource, phcfid);
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
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
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

}
