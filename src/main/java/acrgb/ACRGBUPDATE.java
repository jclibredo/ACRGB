/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb;

import acrgb.method.ContractTagging;
import acrgb.method.InsertMethods;
import acrgb.method.Methods;
import acrgb.method.UpdateMethods;
import acrgb.structure.ACRGBWSResult;
import acrgb.structure.Accreditation;
import acrgb.structure.Archived;
import acrgb.structure.Assets;
import acrgb.structure.Contract;
import acrgb.structure.ContractDate;
import acrgb.structure.ManagingBoard;
import acrgb.structure.Tranch;
import acrgb.structure.User;
import acrgb.structure.UserInfo;
import acrgb.structure.UserLevel;
import acrgb.structure.UserRoleIndex;
import acrgb.utility.Utility;
import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.mail.Session;
import javax.sql.DataSource;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

/**
 * REST Web Service
 *
 * @author MinoSun
 */
@Path("ACRGBUPDATE")
@RequestScoped
public class ACRGBUPDATE {

    @Resource(lookup = "mail/acrgbmail")
    private Session session;
    //-----------------------------------
    @Resource(lookup = "jdbc/acrgb")
    private DataSource dataSource;

    private final Utility utility = new Utility();

    /**
     * Retrieves representation of an instance of ACRGB
     *
     * @param token
     * @param assets
     * @return an instance of java.String
     */
    @PUT
    @Path("UPDATEASSETS")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult UPDATEASSETS(@HeaderParam("token") String token,
            final Assets assets) {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(dataSource, token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult insertresult = new UpdateMethods().UPDATEASSETS(dataSource, assets);
            result.setMessage(insertresult.getMessage());
            result.setSuccess(insertresult.isSuccess());
            result.setResult(insertresult.getResult());
        }
        return result;
    }

    @PUT
    @Path("UPDATECONTRACT")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult UPDATECONTRACT(@HeaderParam("token") String token,
            final Contract contract) {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(dataSource, token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult insertresult = new UpdateMethods().UPDATECONTRACT(dataSource, contract);
            result.setMessage(insertresult.getMessage());
            result.setSuccess(insertresult.isSuccess());
            result.setResult(insertresult.getResult());
        }
        return result;
    }

    @PUT
    @Path("UPDATETRANCH")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult UPDATETRANCH(@HeaderParam("token") String token,
            final Tranch tranch) {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(dataSource, token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult insertresult = new UpdateMethods().UPDATETRANCH(dataSource, tranch);
            result.setMessage(insertresult.getMessage());
            result.setSuccess(insertresult.isSuccess());
            result.setResult(insertresult.getResult());
        }
        return result;
    }

    //UPDATE USER LEVEL
    @PUT
    @Path("UPDATEUSERLEVEL")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult UPDATEUSERLEVEL(
            @HeaderParam("token") String token,
            final UserLevel userlevel) {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(dataSource, token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult insertresult = new UpdateMethods().UPDATEUSERLEVEL(dataSource, userlevel);
            result.setMessage(insertresult.getMessage());
            result.setSuccess(insertresult.isSuccess());
            result.setResult(insertresult.getResult());
        }
        return result;
    }

    //UPDATE USER CREDENTIALS
    @PUT
    @Path("UPDATEUSERCREDENTIALS")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult UPDATEUSERCREDENTIALS(
            @HeaderParam("token") String token,
            final User user) {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(dataSource, token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            if (user.getUsername().isEmpty() && !user.getUserpassword().isEmpty()) {
                ACRGBWSResult insertresult = new Methods().CHANGEPASSWORD(dataSource, user.getUserid(), user.getUserpassword(), session);
                result.setMessage(insertresult.getMessage());
                result.setSuccess(insertresult.isSuccess());
                result.setResult(insertresult.getResult());
            } else if (user.getUserpassword().isEmpty() && !user.getUsername().isEmpty()) {
                ACRGBWSResult insertresult = new Methods().CHANGEUSERNAME(dataSource, user.getUserid(), user.getUsername(), user.getCreatedby());
                result.setMessage(insertresult.getMessage());
                result.setSuccess(insertresult.isSuccess());
                result.setResult(insertresult.getResult());
            } else {
                ACRGBWSResult insertresult = new Methods().UPDATEUSERCREDENTIALS(dataSource, user.getUserid(), user.getUsername(), user.getUserpassword(), user.getCreatedby(), session);
                result.setMessage(insertresult.getMessage());
                result.setSuccess(insertresult.isSuccess());
                result.setResult(insertresult.getResult());
            }
        }
        return result;
    }

    @PUT
    @Path("UPDATEUSERLEVELID")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult UPDATEUSERLEVEL(@HeaderParam("token") String token,
            final User user) {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(dataSource, token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult insertresult = new Methods().CHANGEUSELEVELID(dataSource, user.getUserid(), user.getLeveid());
            result.setMessage(insertresult.getMessage());
            result.setSuccess(insertresult.isSuccess());
            result.setResult(insertresult.getResult());
        }
        return result;
    }

    //RESET PASSWORD
//    @PUT
//    @Path("RESETPASSWORD")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public ACRGBWSResult RESETPASSWORD(@HeaderParam("token") String token,
//            final User user) {
//        //TODO return proper representation object
//        ACRGBWSResult result = utility.ACRGBWSResult();
//        result.setMessage("");
//        result.setResult("");
//        result.setSuccess(false);
//        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
//        if (!GetPayLoad.isSuccess()) {
//            result.setMessage(GetPayLoad.getMessage());
//        } else {
//            ACRGBWSResult insertresult = new Methods().RESETPASSWORD(dataSource, user.getUserid(), user.getUserpassword());
//            result.setMessage(insertresult.getMessage());
//            result.setSuccess(insertresult.isSuccess());
//            result.setResult(insertresult.getResult());
//        }
//        return result;
//    }
    //SET INACTIVE DATA
    @PUT
    @Path("INACTIVE")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult INACTIVE(@HeaderParam("token") String token,
            final Archived arhived) {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(dataSource, token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult insertresult = new InsertMethods().INACTIVEDATA(dataSource,
                    arhived.getTags(), arhived.getDataid(), arhived.getCreatedby(), "ACTIVE");
            result.setMessage(insertresult.getMessage());
            result.setSuccess(insertresult.isSuccess());
            result.setResult(insertresult.getResult());
        }
        return result;
    }

    //SET DATA TO ACTIVE
    @PUT
    @Path("ACTIVE")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult ACTIVE(@HeaderParam("token") String token,
            final Archived arhived) {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(dataSource, token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult insertresult = new InsertMethods().ACTIVEDATA(dataSource,
                    arhived.getTags(),
                    arhived.getDataid(),
                    arhived.getCreatedby(),
                    "INACTIVE");
            result.setMessage(insertresult.getMessage());
            result.setSuccess(insertresult.isSuccess());
            result.setResult(insertresult.getResult());
        }
        return result;
    }

    //REMOVED ACCESS ROLE INDEX
    @PUT
    @Path("RemoveAccessRole")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult RemoveAccessRole(@HeaderParam("token") String token,
            final UserRoleIndex userroleindex) {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(dataSource, token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult insertresult = new Methods().REMOVEDROLEINDEX(dataSource, userroleindex.getUserid(), userroleindex.getAccessid(), userroleindex.getCreatedby());
            result.setMessage(insertresult.getMessage());
            result.setSuccess(insertresult.isSuccess());
            result.setResult(insertresult.getResult());
        }
        return result;
    }

    //TAGGING PROCESS
//    @PUT
//    @Path("TAGGINGFACILITY")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public ACRGBWSResult TAGGINGFACILITY(@HeaderParam("token") String token,
//            final HealthCareFacility hcf) throws SQLException {
//        //TODO return proper representation object
//        ACRGBWSResult result = utility.ACRGBWSResult();
//        result.setMessage("");
//        result.setResult("");
//        result.setSuccess(false);
//        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
//        if (!GetPayLoad.isSuccess()) {
//            result.setMessage(GetPayLoad.getMessage());
//        } else {
//            ACRGBWSResult taggingresult = new UpdateMethods().FACILITYTAGGING(dataSource, hcf);
//            result.setMessage(taggingresult.getMessage());
//            result.setSuccess(taggingresult.isSuccess());
//            result.setResult(taggingresult.getResult());
//        }
//        return result;
//    }
    //CONTRACT TAGGING PROCESS
    @PUT
    @Path("TAGGINGCONTRACT")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult TAGGINGCONTRACT(
            @HeaderParam("token") String token,
            @HeaderParam("datatags") String datatags,
            final Contract contract) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ContractTagging contractTagging = new ContractTagging();
        ACRGBWSResult GetPayLoad = utility.GetPayload(dataSource, token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            Contract con = new Contract();
            con.setConid(contract.getConid());
            con.setRemarks(contract.getRemarks());
            con.setEnddate(contract.getEnddate());
            con.setCreatedby(contract.getCreatedby());
            switch (contract.getStats()) {
                case "NONRENEW": {
                    con.setStats("3");//END CONTRACT
                    break;
                }
                case "RENEW": {
                    con.setStats("4");//RENEW
                    break;
                }
                case "TERMINATE": {
                    con.setStats("5");
                    break;
                }
            }
            ACRGBWSResult taggingresult = contractTagging.TAGGINGCONTRACT(dataSource, datatags.toUpperCase().trim(), con);
            result.setMessage(taggingresult.getMessage());
            result.setSuccess(taggingresult.isSuccess());
            result.setResult(taggingresult.getResult());
        }
        return result;
    }

    //REMOVED AFFILIATE
    @PUT
    @Path("RemoveAppellate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult RemoveAppellate(@HeaderParam("token") String token,
            final UserRoleIndex userroleindex) {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(dataSource, token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult insertresult = new UpdateMethods().RemoveAppellate(dataSource, userroleindex.getUserid(), userroleindex.getAccessid());
            result.setMessage(insertresult.getMessage());
            result.setSuccess(insertresult.isSuccess());
            result.setResult(insertresult.getResult());
        }
        return result;
    }

    //UPDATE USER DETAILS
    @PUT
    @Path("UPDATEUSERDETAILS")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult UPDATEUSERDETAILS(@HeaderParam("token") String token,
            final UserInfo userinfo) {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(dataSource, token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult insertresult = new Methods().UPDATEUSERDETAILS(dataSource, userinfo);
            result.setMessage(insertresult.getMessage());
            result.setSuccess(insertresult.isSuccess());
            result.setResult(insertresult.getResult());
        }
        return result;
    }

    //APPROVEDHCPN
    @PUT
    @Path("APPROVEDHCPN")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult APPROVEDHCPN(
            @HeaderParam("token") String token,
            final ManagingBoard mb) {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(dataSource, token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult insertresult = new UpdateMethods().APPROVEDHCPN(dataSource, mb);
            result.setMessage(insertresult.getMessage());
            result.setSuccess(insertresult.isSuccess());
            result.setResult(insertresult.getResult());
        }
        return result;
    }
//UPDATE CONTRACT DATE

    @PUT
    @Path("UPDATECONTRACTDATE")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult APPROVEDHCPN(@HeaderParam("token") String token,
            final ContractDate contractdate) {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(dataSource, token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult insertresult = new UpdateMethods().UPDATECONDATE(dataSource, contractdate);
            result.setMessage(insertresult.getMessage());
            result.setSuccess(insertresult.isSuccess());
            result.setResult(insertresult.getResult());
        }
        return result;
    }

    //UPDATE HCPN
    @PUT
    @Path("UPDATEHCPN")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult UPDATEHCPN(@HeaderParam("token") String token,
            final ManagingBoard mb) {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(dataSource, token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult insertresult = new UpdateMethods().UPDATEHCPN(dataSource, mb);
            result.setMessage(insertresult.getMessage());
            result.setSuccess(insertresult.isSuccess());
            result.setResult(insertresult.getResult());
        }
        return result;
    }

//    @PUT
//    @Path("UPDATEAPELLATE")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public ACRGBWSResult UPDATEAPELLATE(@HeaderParam("token") String token,
//            final Appellate appellate) throws SQLException, ParseException {
//        //TODO return proper representation object
//        ACRGBWSResult result = utility.ACRGBWSResult();
//        result.setMessage("");
//        result.setResult("");
//        result.setSuccess(false);
//        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
//        if (!GetPayLoad.isSuccess()) {
//            result.setMessage(GetPayLoad.getMessage());
//        } else {
//            ACRGBWSResult insertresult = new UpdateMethods().UPDATEAPELLATE(dataSource, "UPDATE", appellate);
//            result.setMessage(insertresult.getMessage());
//            result.setSuccess(insertresult.isSuccess());
//            result.setResult(insertresult.getResult());
//        }
//        return result;
//    }
    //
    //UPDATE HCPN
    @PUT
    @Path("UPDATEUSERROLE")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult UPDATEUSERROLE(
            @HeaderParam("token") String token,
            final User user) {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(dataSource, token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult insertresult = new UpdateMethods().UPDATEUSEROLE(dataSource,
                    user.getCreatedby(),
                    user.getDatecreated(),
                    user.getLeveid(),
                    user.getUserid());
            result.setMessage(insertresult.getMessage());
            result.setSuccess(insertresult.isSuccess());
            result.setResult(insertresult.getResult());
        }
        return result;
    }

//    @DELETE
//    @Path("DELETEDATA")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public ACRGBWSResult DELETEDATA(@HeaderParam("token") String token,
//            final Archived arhived) {
//        //TODO return proper representation object
//        ACRGBWSResult result = utility.ACRGBWSResult();
//        result.setMessage("");
//        result.setResult("");
//        result.setSuccess(false);
//        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
//        if (!GetPayLoad.isSuccess()) {
//            result.setMessage(GetPayLoad.getMessage());
//        } else {
//            ACRGBWSResult insertresult = new UpdateMethods().DELETEDATA(dataSource, arhived.getTags(),
//                    arhived.getDataid(), arhived.getCreatedby());
//            result.setMessage(insertresult.getMessage());
//            result.setSuccess(insertresult.isSuccess());
//            result.setResult(insertresult.getResult());
//        }
//        return result;
//    }
    @PUT
    @Path("UPDATEACCREDITATION")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult UPDATEACCREDITATION(
            @HeaderParam("token") String token,
            final Accreditation accreditation) {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(dataSource, token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult updateResult = new Methods().UpdateHCPNAccreditation(dataSource, accreditation);
            result.setMessage(updateResult.getMessage());
            result.setSuccess(updateResult.isSuccess());
            result.setResult(updateResult.getResult());
        }
        return result;
    }

}
