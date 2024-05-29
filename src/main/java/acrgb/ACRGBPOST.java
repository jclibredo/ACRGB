/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb;

import acrgb.method.Forgetpassword;
import acrgb.method.InsertMethods;
import acrgb.method.Methods;
import acrgb.structure.ACRGBWSResult;
import acrgb.structure.Assets;
import acrgb.structure.Contract;
import acrgb.structure.ContractDate;
import acrgb.structure.DateSettings;
import acrgb.structure.ForgetPassword;
import acrgb.structure.MBRequestSummary;
import acrgb.structure.ManagingBoard;
import acrgb.structure.Tranch;
import acrgb.structure.User;
import acrgb.structure.UserActivity;
import acrgb.structure.UserInfo;
import acrgb.structure.UserLevel;
import acrgb.structure.UserRoleIndex;
import acrgb.utility.Utility;
import com.sun.jersey.multipart.FormDataParam;
import java.sql.SQLException;
import java.text.ParseException;
import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
//--------------------------------------

//---------------------------------------
/**
 * REST Web Service
 *
 * @author MinoSun
 */
@Path("ACRGBINSERT")
@RequestScoped
public class ACRGBPOST {

    /**
     * Creates a new instance of ACRGB
     */
    @Resource(lookup = "jdbc/acrgb")
    private DataSource dataSource;

    private final Utility utility = new Utility();
    private final InsertMethods insertmethods = new InsertMethods();
    private final Methods methods = new Methods();

    /**
     * Retrieves representation of an instance of acrgb.ACRGB
     *
     * @param assets
     * @return an instance of java.lang.String
     * @throws java.sql.SQLException
     */
    //INSERTASSETS
    @POST
    @Path("INSERTASSETS")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult INSERTASSETS(final Assets assets) throws SQLException {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        ACRGBWSResult insertresult = insertmethods.INSERTASSETS(dataSource, assets);
        result.setMessage(insertresult.getMessage());
        result.setSuccess(insertresult.isSuccess());
        result.setResult(insertresult.getResult());
        return result;
    }

    //INSERTASSETS
    @POST
    @Path("INSERTHCPN")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult INSERTHCPN(final ManagingBoard mb) throws SQLException, ParseException {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        ACRGBWSResult insertresult = insertmethods.INSERTHCPN(dataSource, mb);
        result.setMessage(insertresult.getMessage());
        result.setSuccess(insertresult.isSuccess());
        result.setResult(insertresult.getResult());
        return result;
    }

    //INSERT TYPE ASSETS
    @POST
    @Path("INSERTCONTRACT")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult INSERTCONTRACT(final Contract contract) throws SQLException, ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        ACRGBWSResult insertresult = insertmethods.INSERTCONTRACT(dataSource, contract);
        result.setMessage(insertresult.getMessage());
        result.setSuccess(insertresult.isSuccess());
        result.setResult(insertresult.getResult());
        return result;
    }

    //INSERT ACCOUNT ROLE
    @POST
    @Path("INSERTTRANCH")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult INSERTTRANCH(final Tranch tranch) throws SQLException {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        ACRGBWSResult insertresult = insertmethods.INSERTTRANCH(dataSource, tranch);
        result.setMessage(insertresult.getMessage());
        result.setSuccess(insertresult.isSuccess());
        result.setResult(insertresult.getResult());
        return result;
    }

    //INSERT ACCOUT ROLE INDEX
    @POST
    @Path("INSERTUSERDETAILS")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult INSERTUSERDETAILS(final UserInfo userinfo) throws SQLException {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        ACRGBWSResult insertresult = insertmethods.INSERTUSERDETAILS(dataSource, userinfo);
        result.setMessage(insertresult.getMessage());
        result.setSuccess(insertresult.isSuccess());
        result.setResult(insertresult.getResult());
        return result;
    }

    //INSERT HCI FACILITY
    @POST
    @Path("INSERTUSERLEVEL")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult INSERTUSERLEVEL(final UserLevel userlevel) throws SQLException, ParseException {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        ACRGBWSResult insertresult = insertmethods.INSERTUSERLEVEL(dataSource, userlevel);
        result.setMessage(insertresult.getMessage());
        result.setSuccess(insertresult.isSuccess());
        result.setResult(insertresult.getResult());
        return result;
    }

    @POST
    @Path("INSERTDATESETTINGS")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult INSERTDATESETTINGS(final DateSettings datesettings) throws SQLException, ParseException {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        switch (datesettings.getTags().toUpperCase()) {
            case "DATEDIF":
                ACRGBWSResult insertresult = insertmethods.INSERTDATESETTINGS(dataSource, datesettings);
                result.setMessage(insertresult.getMessage());
                result.setSuccess(insertresult.isSuccess());
                result.setResult(insertresult.getResult());
                break;
            case "DATESKIP":
                ACRGBWSResult insertresultDate = insertmethods.INSERTSKIPYEAR(dataSource, datesettings);
                result.setMessage(insertresultDate.getMessage());
                result.setSuccess(insertresultDate.isSuccess());
                result.setResult(insertresultDate.getResult());
                break;
            default:
                result.setMessage("TAGS NOT FOUND");
                break;
        }

        return result;
    }

    @POST
    @Path("INSERTUSER")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult INSERTUSER(final User user) throws SQLException, ParseException {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        ACRGBWSResult insertresult = insertmethods.INSERTUSER(dataSource, user);
        result.setMessage(insertresult.getMessage());
        result.setSuccess(insertresult.isSuccess());
        result.setResult(insertresult.getResult());
        return result;
    }

    @POST
    @Path("UserLogin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult UserLogin(final User user) throws SQLException {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        ACRGBWSResult insertresult = methods.ACRUSERLOGIN(dataSource, user.getUsername(), user.getUserpassword());
        result.setMessage(insertresult.getMessage());
        result.setSuccess(insertresult.isSuccess());
        result.setResult(insertresult.getResult());
        return result;
    }

    @POST
    @Path("logs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult logs(final UserActivity logs) throws SQLException, ParseException {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        ACRGBWSResult insertresult = methods.ActivityLogs(dataSource, logs);
        result.setMessage(insertresult.getMessage());
        result.setSuccess(insertresult.isSuccess());
        result.setResult(insertresult.getResult());
        return result;
    }

    @POST
    @Path("INSERTMBREQUEST")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult MBREQUEST(final MBRequestSummary mbrequestsummry) throws SQLException, ParseException {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        ACRGBWSResult insertresult = methods.InsertMBRequest(dataSource, mbrequestsummry);
        result.setMessage(insertresult.getMessage());
        result.setSuccess(insertresult.isSuccess());
        result.setResult(insertresult.getResult());
        return result;
    }

    @POST
    @Path("INSERTROLEINDEX")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult ROLEINDEX(final UserRoleIndex userroleindex) throws SQLException, ParseException {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        ACRGBWSResult insertresult = insertmethods.INSEROLEINDEX(dataSource, userroleindex);
        result.setMessage(insertresult.getMessage());
        result.setSuccess(insertresult.isSuccess());
        result.setResult(insertresult.getResult());
        return result;
    }

    @POST
    @Path("INSERTAPPELLATE")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult INSERTAPPELLATE(final UserRoleIndex userroleindex) throws SQLException, ParseException {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        ACRGBWSResult insertresult = insertmethods.INSERTAPPELLATE(dataSource,
                userroleindex.getUserid(), userroleindex.getAccessid());
        result.setMessage(insertresult.getMessage());
        result.setSuccess(insertresult.isSuccess());
        result.setResult(insertresult.getResult());
        return result;
    }

    @POST
    @Path("INSERTCONDATE")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult INSERTCONDATE(final ContractDate contractdate) throws SQLException, ParseException {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        ACRGBWSResult insertresult = insertmethods.INSERTCONDATE(dataSource, contractdate);
        result.setMessage(insertresult.getMessage());
        result.setSuccess(insertresult.isSuccess());
        result.setResult(insertresult.getResult());
        return result;
    }

    //TEST PASSWORD RESETTER
    @POST
    @Path("FORGETPASSWORD")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult FORGETPASSWORD(final ForgetPassword emailto) {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        Forgetpassword pass = new Forgetpassword();
        ACRGBWSResult insertresult = pass.Forgetpassword(emailto.getEmailto(),"");
        result.setMessage(insertresult.getMessage());
        result.setSuccess(insertresult.isSuccess());
        result.setResult(insertresult.getResult());
        return result;
    }

}
