/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb;

import acrgb.method.BookingMethod;
import acrgb.method.FetchMethods;
import acrgb.method.EmailSender;
import acrgb.method.InsertMethods;
import acrgb.method.Methods;
import acrgb.structure.ACRGBWSResult;
import acrgb.structure.Assets;
import acrgb.structure.Book;
import acrgb.structure.Contract;
import acrgb.structure.ContractDate;
import acrgb.structure.Email;
import acrgb.structure.ManagingBoard;
import acrgb.structure.Tranch;
import acrgb.structure.User;
import acrgb.structure.UserInfo;
import acrgb.structure.UserLevel;
import acrgb.structure.UserRoleIndex;
import acrgb.utility.Utility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.mail.Session;
import javax.sql.DataSource;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

/**
 * REST Web Service
 *
 * @author DRG_SHADOWBILLING
 */
@Path("ACRGBINSERT")
@RequestScoped
public class ACRGBPOST {

    public ACRGBPOST() {
    }
    /**
     * Creates a new instance of ACRGB
     */

    //------------------------------------
    @Resource(lookup = "jdbc/acgbuser")
    private DataSource dataSource;

    @Resource(lookup = "mail/acrgbmail")
    private Session acrgbmail;

    private final Utility utility = new Utility();

    /**
     * Retrieves representation of an instance of acrgb.ACRGB
     *
     * @param token
     * @param assets
     * @return an instance of java.lang.String
     */
    //INSERTASSETS
    @POST
    @Path("INSERTASSETS")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult INSERTASSETS(@HeaderParam("token") String token,
            final Assets assets) {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            result = new InsertMethods().INSERTASSETS(dataSource, assets);
        }
        return result;
    }

    //INSERTASSETS
    @POST
    @Path("INSERTHCPN")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult INSERTHCPN(
            @HeaderParam("token") String token,
            final ManagingBoard mb) {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            result = new InsertMethods().INSERTHCPN(dataSource, mb);
        }
        return result;
    }

    //INSERT TYPE ASSETS
    @POST
    @Path("INSERTCONTRACT")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult INSERTCONTRACT(
            @HeaderParam("token") String token,
            final Contract contract) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            result = new InsertMethods().INSERTCONTRACT(dataSource, contract);
        }
        return result;
    }

    //INSERT ACCOUNT ROLE
    @POST
    @Path("INSERTTRANCH")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult INSERTTRANCH(@HeaderParam("token") String token, final Tranch tranch) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            result = new InsertMethods().INSERTTRANCH(dataSource, tranch);
        }
        return result;
    }

    //INSERT ACCOUT ROLE INDEX
    @POST
    @Path("INSERTUSERDETAILS")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult INSERTUSERDETAILS(@HeaderParam("token") String token, final UserInfo userinfo) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            result = new InsertMethods().INSERTUSERDETAILS(dataSource, userinfo);
        }
        return result;
    }

    //INSERT HCI FACILITY
    @POST
    @Path("INSERTUSERLEVEL")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult INSERTUSERLEVEL(
            @HeaderParam("token") String token,
            final UserLevel userlevel) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            result = new InsertMethods().INSERTUSERLEVEL(dataSource, userlevel);
        }
        return result;
    }

    @POST
    @Path("INSERTUSER")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult INSERTUSER(
            @HeaderParam("token") String token,
            final User user) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            result = new InsertMethods().INSERTUSER(dataSource, user, acrgbmail);
        }
        return result;
    }

    @POST
    @Path("UserLogin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult UserLogin(final User user) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result = new Methods().ACRUSERLOGIN(dataSource,
                user.getUsername(),
                user.getUserpassword());
        return result;
    }

//    @POST
//    @Path("logs")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public ACRGBWSResult logs(@HeaderParam("token") String token, final UserActivity logs) {
//        //TODO return proper representation object
//        ACRGBWSResult result = utility.ACRGBWSResult();
//        result.setMessage("");
//        result.setResult("");
//        result.setSuccess(false);
//        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
//        if (!GetPayLoad.isSuccess()) {
//            result.setMessage(GetPayLoad.getMessage());
//        } else {
//            ACRGBWSResult insertresult =new Methods() methods.ActivityLogs(dataSource, logs);
//            result.setMessage(insertresult.getMessage());
//            result.setSuccess(insertresult.isSuccess());
//            result.setResult(insertresult.getResult());
//        }
//        return result;
//    }
//    @POST
//    @Path("INSERTMBREQUEST")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public ACRGBWSResult MBREQUEST(@HeaderParam("token") String token, final MBRequestSummary mbrequestsummry) {
//        //TODO return proper representation object
//        ACRGBWSResult result = utility.ACRGBWSResult();
//        result.setMessage("");
//        result.setResult("");
//        result.setSuccess(false);
//        ACRGBWSResult GetPayLoad = utility.GetPayload(dataSource, token);
//        if (!GetPayLoad.isSuccess()) {
//            result.setMessage(GetPayLoad.getMessage());
//        } else {
//            ACRGBWSResult insertresult = new Methods().InsertMBRequest(dataSource, mbrequestsummry);
//            result.setMessage(insertresult.getMessage());
//            result.setSuccess(insertresult.isSuccess());
//            result.setResult(insertresult.getResult());
//        }
//        return result;
//    }
    @POST
    @Path("INSERTROLEINDEX")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult ROLEINDEX(@HeaderParam("token") String token, final UserRoleIndex userroleindex) {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            result = new InsertMethods().INSEROLEINDEX(dataSource, userroleindex);
        }
        return result;
    }

    @POST
    @Path("INSERTAPPELLATE")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult INSERTAPPELLATE(@HeaderParam("token") String token, final UserRoleIndex userroleindex) {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            result = new InsertMethods().INSERTAPPELLATE(dataSource,
                    userroleindex.getUserid(), //SONGLE
                    userroleindex.getAccessid(),//MULTIPLE
                    userroleindex.getCreatedby(),
                    userroleindex.getDatecreated());
        }
        return result;
    }

    @POST
    @Path("INSERTCONDATE")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult INSERTCONDATE(@HeaderParam("token") String token, final ContractDate contractdate) {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            result = new InsertMethods().INSERTCONDATE(dataSource, contractdate);
        }
        return result;
    }

    //PASSWORD RESETTER
    @POST
    @Path("FORGETPASSWORD")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult FORGETPASSWORD(
            final Email email) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result = new EmailSender().EmailSender(dataSource, email.getEmailto(), "", acrgbmail);
        //-------------------------------------
        return result;
    }

    //USER ACCOUNT BATCH UPLOAD
    @POST
    @Path("USERACCOUNTBATCH")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult USERACCOUNTBATCH(
            @HeaderParam("token") String token,
            final List<UserInfo> userinfo) {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Collection errorList = new ArrayList<>();
        try {
            if (!utility.GetPayload(dataSource, token).isSuccess()) {
                result.setMessage(utility.GetPayload(dataSource, token).getMessage());
            } else {
                for (int x = 0; x < userinfo.size(); x++) {
                    ArrayList<String> error = new ArrayList<>();
                    //CHECK DATE VALID FORMAT
                    if (!utility.IsValidDate(userinfo.get(x).getDatecreated())) {
                        error.add(userinfo.get(x).getDatecreated() + " NOT VALID DATE");
                    } else if (userinfo.get(x).getContact().trim().isEmpty()) {
                        error.add("DATE CREATED IS REQUIRED");
                    }
                    //CHECK USERNAME
                    if (!utility.isValidEmail(userinfo.get(x).getEmail().trim())) {
                        error.add("| EMAIL IS INVALID");
                    } else if (!new Methods().ACRUSERNAME(dataSource, userinfo.get(x).getEmail().trim()).isSuccess()) {
                        error.add(userinfo.get(x).getEmail().trim() + " DUPLICATE");
                    } else if (userinfo.get(x).getContact().trim().isEmpty()) {
                        error.add("| EMAIL IS REQUIRED");
                    }
                    //CHECK VALID NUMBER FORMAT
                    if (!utility.isValidPhoneNumber(userinfo.get(x).getContact().trim())) {
                        error.add(userinfo.get(x).getContact() + " INVALID CONTACT");
                    } else if (userinfo.get(x).getContact().trim().isEmpty()) {
                        error.add("| CONTACT NUMBER IS REQUIRED");
                    }
                    //CHECK FIRSTNAME
                    if (userinfo.get(x).getFirstname().trim().isEmpty()) {
                        error.add("| FIRSTNAME IS REQUIRED");
                    }
                    //CHECK LASTNAME
                    if (userinfo.get(x).getLastname().trim().isEmpty()) {
                        error.add("| LASTNAME IS REQUIRED");
                    }
                    //CHECK ROLE
                    if (userinfo.get(x).getRole().toUpperCase().trim().isEmpty()) {
                        error.add("| ROLE IS REQUIRED");
                    }
                    //CHECK DESIGNATION
                    if (userinfo.get(x).getDesignation().isEmpty()) {
                        error.add("| DESIGNATION IS REQUIRED");
                    }
                    //VALIDATE USER LEVEL
                    if (!new FetchMethods().GETLEVELBYLEVNAME(dataSource, userinfo.get(x).getRole().toUpperCase().trim()).isSuccess()) {
                        error.add("| ROLE NOT VALID");
                    } else {
                        UserLevel level = utility.ObjectMapper().readValue(new FetchMethods().GETLEVELBYLEVNAME(dataSource, userinfo.get(x).getRole().toUpperCase().trim()).getResult(), UserLevel.class);
                        switch (level.getLevname().toLowerCase().trim()) {
                            case "PRO": {
                                if (!new Methods().GetProWithPROID(dataSource, userinfo.get(x).getDesignation().toUpperCase().trim()).isSuccess()) {
                                    error.add("| PRO CODE " + userinfo.get(x).getDesignation() + " NOT VALID");
                                }
                                break;
                            }
                            case "HCPN": {
                                if (!new Methods().GETMBWITHID(dataSource, userinfo.get(x).getDesignation().trim().toUpperCase()).isSuccess()) {
                                    error.add("| HCPN CODE " + userinfo.get(x).getDesignation() + " NOT VALID");
                                }
                                break;
                            }
                            case "HCF": {
                                if (!new FetchMethods().GETFACILITYID(dataSource, userinfo.get(x).getDesignation().toUpperCase().trim()).isSuccess()) {
                                    error.add("| HCF CODE NOT " + userinfo.get(x).getDesignation() + " VALID");
                                }
                                break;
                            }
                        }
                    }
                    if (error.size() > 0) {
                        error.add("| LINE NUMBER[" + userinfo.get(x).getId() + "]");
                        errorList.add(error);
                    } else {
                        //CALL THE METHOD OF INSERTION OF USER
                        UserInfo userInfo = new UserInfo();
                        userInfo.setContact(userinfo.get(x).getContact());
                        userInfo.setCreatedby(userinfo.get(x).getCreatedby());
                        userInfo.setDatecreated(userinfo.get(x).getDatecreated());
                        userInfo.setFirstname(userinfo.get(x).getFirstname());
                        userInfo.setEmail(userinfo.get(x).getEmail());
                        userInfo.setMiddlename(userinfo.get(x).getMiddlename());
                        userInfo.setLastname(userinfo.get(x).getLastname());
                        switch (userinfo.get(x).getRole().trim().toUpperCase()) {
                            case "HCI": {
                                userInfo.setRole(new FetchMethods().GETLEVELBYLEVNAME(dataSource, userinfo.get(x).getRole().trim().toUpperCase()).getMessage());
                                break;
                            }
                            case "HCPN": {
                                userInfo.setRole(new FetchMethods().GETLEVELBYLEVNAME(dataSource, userinfo.get(x).getRole().trim().toUpperCase()).getMessage());
                                break;
                            }
                            case "PRO": {
                                userInfo.setRole(new FetchMethods().GETLEVELBYLEVNAME(dataSource, userinfo.get(x).getRole().trim().toUpperCase()).getMessage());
                                break;
                            }
                            default: {
                                break;
                            }
                        }
                        if (new FetchMethods().GETLEVELBYLEVNAME(dataSource, userinfo.get(x).getRole()).isSuccess()) {
                            UserLevel level = utility.ObjectMapper().readValue(new FetchMethods().GETLEVELBYLEVNAME(dataSource, userinfo.get(x).getRole().trim().toUpperCase()).getResult(), UserLevel.class);
                            if (level.getLevname().toUpperCase().equals("PRO")) {
                                userInfo.setDesignation("2024" + userinfo.get(x).getDesignation().trim().toUpperCase());
                            } else {
                                userInfo.setDesignation(userinfo.get(x).getDesignation().trim().toUpperCase());
                            }
                        }
                        ACRGBWSResult InsertCleanData = new InsertMethods().INSERTUSERACCOUNTBATCHUPLOAD(dataSource, userInfo, acrgbmail);
                        if (!InsertCleanData.isSuccess()) {
                            error.add("| LINE NUMBER[" + userinfo.get(x).getId() + "]");
                            error.add(InsertCleanData.getMessage());
                        }
                    }
                }
            }
            result.setMessage("PLEASE SEE UPLOAD RESULT STATUS");
            result.setSuccess(true);
            result.setResult(utility.ObjectMapper().writeValueAsString(errorList));
        } catch (IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(ACRGBPOST.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET TRIGGER AUTOEND CONTRACT DATE
    // 1.) CONTRACT ID
    // 2.) DATE PERIOD 
    // 3.) SELECT FACILITY AND FACILITY
    @POST
    @Path("BookData")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ACRGBWSResult BookData(
            @HeaderParam("token") String token,
            final Book book) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            // CHECK PROCESS ALL ENDED CONTRACT             
            result = new BookingMethod().PROCESSENDEDCONTRACT(dataSource, book, "INACTIVE");
        }
        return result;
    }

    //TEST INSERT CLAIMS DATA
//    @POST
//    @Path("InsertNclaims")
//    @Produces(MediaType.APPLICATION_JSON)
//    @Consumes(MediaType.APPLICATION_JSON)
//    public ACRGBWSResult InsertNclaims(final NclaimsData nclaims) {
//        ACRGBWSResult result = utility.ACRGBWSResult();
//        result.setMessage("");
//        result.setResult("");
//        result.setSuccess(false);
//        ArrayList<String> errorList = new ArrayList<>();
//        try (Connection connection = dataSource.getConnection()) {
//            for (int x = 0; x < Integer.parseInt(nclaims.getTotalclaims()); x++) {
//                CallableStatement getinsertresult = connection.prepareCall("call DRG_SHADOWBILLING.INSERTCLAIMS(:Message,:Code,"
//                        + ":useries,:uaccreno,:upmccno,:udateadmission,:udatesubmitted,:uclaimamount,"
//                        + ":utags,:utrn,:uclaimid,:uhcfname,:c1rvcode,:c2rvcode,:c1icdcode,:c2icdcode,:uopdtst)");
//                getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
//                getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
//                getinsertresult.setString("useries", nclaims.getSeries());
//                getinsertresult.setString("uaccreno", nclaims.getAccreno());
//                getinsertresult.setString("upmccno", nclaims.getPmccno());
//                getinsertresult.setString("udateadmission", nclaims.getDateadmission().trim());
//                getinsertresult.setDate("udateadmission", (Date) new Date(utility.StringToDate(nclaims.getDateadmission().trim()).getTime()));
//                getinsertresult.setDate("udatesubmitted", (Date) new Date(utility.StringToDate(nclaims.getDatesubmitted()).getTime()));
//                getinsertresult.setString("uclaimamount", nclaims.getClaimamount());
//                getinsertresult.setString("utags", nclaims.getTags());
//                getinsertresult.setString("utrn", nclaims.getTrn());
//                getinsertresult.setString("uclaimid", nclaims.getClaimid());
//                getinsertresult.setString("uhcfname", nclaims.getHcfname());
//                getinsertresult.setString("c1rvcode", nclaims.getC1rvcode());
//                getinsertresult.setString("c2rvcode", nclaims.getC2rvcode());
//                getinsertresult.setString("c1icdcode", nclaims.getC1icdcode());
//                getinsertresult.setString("c2icdcode", nclaims.getC2icdcode());
//                getinsertresult.setString("uopdtst", nclaims.getUopdtst());
//                getinsertresult.execute();
//                if (!getinsertresult.getString("Message").equals("SUCC")) {
//                    errorList.add(getinsertresult.getString("Message"));
//                }
//            }
//            //------------------------------------------------------------------------------------------------
//            if (errorList.size() > 0) {
//                result.setMessage("THERE'S AN ERROR");
//            } else {
//                result.setMessage("NO ERROR");
//            }
//            result.setResult(utility.ObjectMapper().writeValueAsString(errorList));
//            result.setSuccess(true);
//
//        } catch (SQLException | IOException ex) {
//            result.setMessage("Something went wrong");
//            Logger.getLogger(ACRGBPOST.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return result;
//    }
    @POST
    @Path("Remap")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ACRGBWSResult Remap(@HeaderParam("token") String token,
            final UserRoleIndex roleIndex) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (!utility.GetPayload(dataSource, token).isSuccess()) {
            result.setMessage(utility.GetPayload(dataSource, token).getMessage());
        } else {
            result = new InsertMethods().INSEROLEINDEX(dataSource, roleIndex);
        }
        return result;
    }

    //ACCOUNTING TOKEN AREA
//    @POST
//    @Path("AccountingToken")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public ACRGBWSResult AccountingToken(
//            @HeaderParam("phicusername") String phicusername,
//            @HeaderParam("phicpassword") String phicpassword) {
//        //TODO return proper representation object
//        ACRGBWSResult result = utility.ACRGBWSResult();
//        String token = utility.GenerateToken(phicusername, phicpassword);
//        result.setResult(token);
//        result.setSuccess(true);
//        return result;
//    }
    //ACCOUNTING LGOIN AREA
//    @POST
//    @Path("AccountingUserLogin")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public ACRGBWSResult AccountingUserLogin(final User user) {
//        //TODO return proper representation object
//        ACRGBWSResult result = utility.ACRGBWSResult();
//        try {
//            ACRGBWSResult insertresult = new Methods().ACRUSERLOGIN(dataSource,
//                    user.getUsername(),
//                    user.getUserpassword());
//            if (insertresult.isSuccess()) {
//                ACRGBWSResult GetPayLoad = utility.GetPayload(dataSource, insertresult.getMessage());
//                if (!GetPayLoad.isSuccess()) {
//                    result.setMessage(GetPayLoad.getMessage());
//                } else {
//                    result.setMessage("OK");
//                    result.setSuccess(true);
//                }
//            } else {
//                result.setMessage(insertresult.getMessage());
//                result.setSuccess(insertresult.isSuccess());
//                result.setResult(insertresult.getResult());
//            }
//        } catch (Exception ex) {
//            result.setMessage(ex.toString());
//            Logger.getLogger(ACRGBPOST.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        return null;
//    }
}
