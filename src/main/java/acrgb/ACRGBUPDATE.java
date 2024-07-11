/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb;

import acrgb.method.InsertMethods;
import acrgb.method.Methods;
import acrgb.method.UpdateMethods;
import acrgb.structure.ACRGBWSResult;
import acrgb.structure.Appellate;
import acrgb.structure.Archived;
import acrgb.structure.Assets;
import acrgb.structure.Contract;
import acrgb.structure.ContractDate;
import acrgb.structure.HealthCareFacility;
import acrgb.structure.ManagingBoard;
import acrgb.structure.Tranch;
import acrgb.structure.User;
import acrgb.structure.UserInfo;
import acrgb.structure.UserLevel;
import acrgb.structure.UserRoleIndex;
import acrgb.utility.Utility;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
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

    @Resource(lookup = "jdbc/acrgb")
    private DataSource dataSource;

    private final Utility utility = new Utility();
    private final UpdateMethods updatemethods = new UpdateMethods();
    private final Methods methods = new Methods();
    private final InsertMethods insertmethods = new InsertMethods();

    /**
     * Retrieves representation of an instance of acrgb.ACRGB
     *
     * @param token
     * @param assets
     * @return an instance of java.lang.String
     * @throws java.sql.SQLException
     */
    @PUT
    @Path("UPDATEASSETS")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult UPDATEASSETS(@HeaderParam("token") String token,
            final Assets assets) throws SQLException {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult insertresult = updatemethods.UPDATEASSETS(dataSource, assets);
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
            final Contract contract) throws SQLException {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult insertresult = updatemethods.UPDATECONTRACT(dataSource, contract);
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
            final Tranch tranch) throws SQLException {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult insertresult = updatemethods.UPDATETRANCH(dataSource, tranch);
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
    public ACRGBWSResult UPDATEUSERLEVEL(@HeaderParam("token") String token,
            final UserLevel userlevel) throws SQLException {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult insertresult = updatemethods.UPDATEUSERLEVEL(dataSource, userlevel);
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
    public ACRGBWSResult UPDATEUSERCREDENTIALS(@HeaderParam("token") String token,
            final User user) throws SQLException, IOException {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            if (user.getUsername().isEmpty() && !user.getUserpassword().isEmpty()) {
                ACRGBWSResult insertresult = methods.CHANGEPASSWORD(dataSource, user.getUserid(), user.getUserpassword());
                result.setMessage(insertresult.getMessage());
                result.setSuccess(insertresult.isSuccess());
                result.setResult(insertresult.getResult());
            } else if (user.getUserpassword().isEmpty() && !user.getUsername().isEmpty()) {
                ACRGBWSResult insertresult = methods.CHANGEUSERNAME(dataSource, user.getUserid(), user.getUsername());
                result.setMessage(insertresult.getMessage());
                result.setSuccess(insertresult.isSuccess());
                result.setResult(insertresult.getResult());
            } else {
                ACRGBWSResult insertresult = methods.UPDATEUSERCREDENTIALS(dataSource, user.getUserid(), user.getUsername(), user.getUserpassword());
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
            final User user) throws SQLException {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult insertresult = methods.CHANGEUSELEVELID(dataSource, user.getUserid(), user.getLeveid());
            result.setMessage(insertresult.getMessage());
            result.setSuccess(insertresult.isSuccess());
            result.setResult(insertresult.getResult());
        }
        return result;
    }

    //RESET PASSWORD
    @PUT
    @Path("RESETPASSWORD")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult RESETPASSWORD(@HeaderParam("token") String token,
            final User user) throws SQLException {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult insertresult = methods.RESETPASSWORD(dataSource, user.getUserid(), user.getUserpassword());
            result.setMessage(insertresult.getMessage());
            result.setSuccess(insertresult.isSuccess());
            result.setResult(insertresult.getResult());
        }
        return result;
    }

    //SET INACTIVE DATA
    @PUT
    @Path("INACTIVE")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult INACTIVE(@HeaderParam("token") String token,
            final Archived arhived) throws SQLException, ParseException {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult insertresult = insertmethods.INACTIVEDATA(dataSource,
                    arhived.getTags(), arhived.getDataid(), arhived.getCreatedby(), arhived.getDatecreated());
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
            final Archived arhived) throws SQLException, ParseException {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult insertresult = insertmethods.ACTIVEDATA(dataSource, arhived.getTags(),
                    arhived.getDataid(), arhived.getCreatedby(), arhived.getDatecreated());
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
            final UserRoleIndex userroleindex) throws SQLException, ParseException {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult insertresult = methods.REMOVEDROLEINDEX(dataSource, userroleindex.getUserid(), userroleindex.getAccessid());
            result.setMessage(insertresult.getMessage());
            result.setSuccess(insertresult.isSuccess());
            result.setResult(insertresult.getResult());
        }
        return result;
    }

    //TAGGING PROCESS
    @PUT
    @Path("TAGGINGFACILITY")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult TAGGINGFACILITY(@HeaderParam("token") String token,
            final HealthCareFacility hcf) throws SQLException {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult taggingresult = updatemethods.FACILITYTAGGING(dataSource, hcf);
            result.setMessage(taggingresult.getMessage());
            result.setSuccess(taggingresult.isSuccess());
            result.setResult(taggingresult.getResult());
        }
        return result;
    }

    //CONTRACT TAGGING PROCESS
    @PUT
    @Path("TAGGINGCONTRACT")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult TAGGINGCONTRACT(@HeaderParam("token") String token,
            final Contract contract) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            Contract con = new Contract();
            con.setConid(contract.getConid());
            con.setRemarks(contract.getRemarks());
            con.setEnddate(contract.getEnddate());
            switch (contract.getStats()) {
                case "NONRENEW":
                    con.setStats("3");//END CONTRACT
                    break;
                case "RENEW":
                    con.setStats("4");//RENEW
                    break;
                case "TERMINATE":
                    con.setStats("5");
                    break;
            }
            ACRGBWSResult taggingresult = updatemethods.TAGGINGCONTRACT(dataSource, con);
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
            final UserRoleIndex userroleindex) throws SQLException, ParseException {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult insertresult = updatemethods.RemoveAppellate(dataSource, userroleindex.getUserid(), userroleindex.getAccessid());
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
            final UserInfo userinfo) throws SQLException, ParseException {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult insertresult = methods.UPDATEUSERDETAILS(dataSource, userinfo);
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
    public ACRGBWSResult APPROVEDHCPN(@HeaderParam("token") String token,
            final ManagingBoard mb) throws SQLException, ParseException {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult insertresult = updatemethods.APPROVEDHCPN(dataSource, mb);
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
            final ContractDate contractdate) throws SQLException, ParseException {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult insertresult = updatemethods.UPDATECONDATE(dataSource, contractdate);
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
            final ManagingBoard mb) throws SQLException, ParseException {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult insertresult = updatemethods.UPDATEHCPN(dataSource, mb);
            result.setMessage(insertresult.getMessage());
            result.setSuccess(insertresult.isSuccess());
            result.setResult(insertresult.getResult());
        }
        return result;
    }

    @PUT
    @Path("UPDATEAPELLATE")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult UPDATEAPELLATE(@HeaderParam("token") String token,
            final Appellate appellate) throws SQLException, ParseException {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetPayLoad = utility.GetPayload(token);
        if (!GetPayLoad.isSuccess()) {
            result.setMessage(GetPayLoad.getMessage());
        } else {
            ACRGBWSResult insertresult = updatemethods.UPDATEAPELLATE(dataSource, "UPDATE", appellate);
            result.setMessage(insertresult.getMessage());
            result.setSuccess(insertresult.isSuccess());
            result.setResult(insertresult.getResult());
        }
        return result;
    }

}
