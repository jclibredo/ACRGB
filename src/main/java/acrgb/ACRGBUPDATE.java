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
import acrgb.structure.Archived;
import acrgb.structure.Area;
import acrgb.structure.AreaType;
import acrgb.structure.Assets;
import acrgb.structure.Contract;
import acrgb.structure.HealthCareFacility;
import acrgb.structure.ManagingBoard;
import acrgb.structure.Pro;
import acrgb.structure.Tranch;
import acrgb.structure.User;
import acrgb.structure.UserLevel;
import acrgb.utility.Utility;
import java.sql.SQLException;
import java.text.ParseException;
import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
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
     * @param area
     * @return an instance of java.lang.String
     * @throws java.sql.SQLException
     */
    @PUT
    @Path("UPDATEAREA")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult UPDATEAREA(final Area area) throws SQLException {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        ACRGBWSResult insertresult = updatemethods.UPDATEAREA(dataSource, area);
        result.setMessage(insertresult.getMessage());
        result.setSuccess(insertresult.isSuccess());
        result.setResult(insertresult.getResult());
        return result;
    }

    @PUT
    @Path("UPDATEAREATYPE")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult UPDATEAREATYPE(final AreaType areatype) throws SQLException {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        ACRGBWSResult insertresult = updatemethods.UPDATEAREATYPE(dataSource, areatype);
        result.setMessage(insertresult.getMessage());
        result.setSuccess(insertresult.isSuccess());
        result.setResult(insertresult.getResult());
        return result;
    }

    @PUT
    @Path("UPDATEASSETS")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult UPDATEASSETS(final Assets assets) throws SQLException {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        ACRGBWSResult insertresult = updatemethods.UPDATEASSETS(dataSource, assets);
        result.setMessage(insertresult.getMessage());
        result.setSuccess(insertresult.isSuccess());
        result.setResult(insertresult.getResult());
        return result;
    }

    @PUT
    @Path("UPDATECONTRACT")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult UPDATECONTRACT(final Contract contract) throws SQLException {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        ACRGBWSResult insertresult = updatemethods.UPDATECONTRACT(dataSource, contract);
        result.setMessage(insertresult.getMessage());
        result.setSuccess(insertresult.isSuccess());
        result.setResult(insertresult.getResult());
        return result;
    }

    @PUT
    @Path("UPDATEHCF")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult UPDATEHCF(final HealthCareFacility healthcarefacility) throws SQLException {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        ACRGBWSResult insertresult = updatemethods.UPDATEHCF(dataSource, healthcarefacility);
        result.setMessage(insertresult.getMessage());
        result.setSuccess(insertresult.isSuccess());
        result.setResult(insertresult.getResult());
        return result;
    }

    @PUT
    @Path("UPDATETRANCH")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult UPDATETRANCH(final Tranch tranch) throws SQLException {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        ACRGBWSResult insertresult = updatemethods.UPDATETRANCH(dataSource, tranch);
        result.setMessage(insertresult.getMessage());
        result.setSuccess(insertresult.isSuccess());
        result.setResult(insertresult.getResult());
        return result;
    }

    @PUT
    @Path("UPDATEUSERLEVEL")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult UPDATEUSERLEVEL(final UserLevel userlevel) throws SQLException {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        ACRGBWSResult insertresult = updatemethods.UPDATEUSERLEVEL(dataSource, userlevel);
        result.setMessage(insertresult.getMessage());
        result.setSuccess(insertresult.isSuccess());
        result.setResult(insertresult.getResult());
        return result;
    }

    @PUT
    @Path("UPDATEPRO")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult UPDATEPRO(final Pro pro) throws SQLException {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        ACRGBWSResult insertresult = updatemethods.UPDATEPRO(dataSource, pro);
        result.setMessage(insertresult.getMessage());
        result.setSuccess(insertresult.isSuccess());
        result.setResult(insertresult.getResult());
        return result;
    }
// UPDATE MANAGING BOARD
    @PUT
    @Path("UPDATEMB")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult UPDATEMB(final ManagingBoard managingboard) throws SQLException {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        ACRGBWSResult insertresult = updatemethods.UPDATEMB(dataSource, managingboard);
        result.setMessage(insertresult.getMessage());
        result.setSuccess(insertresult.isSuccess());
        result.setResult(insertresult.getResult());
        return result;
    }

    
    @PUT
    @Path("UPDATEUSERCREDENTIALS")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult UPDATEUSERCREDENTIALS(final User user) throws SQLException {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        if (user.getUsername().isEmpty()) {
            ACRGBWSResult insertresult = methods.CHANGEPASSWORD(dataSource, user.getUserid(), user.getUserpassword());
            result.setMessage(insertresult.getMessage());
            result.setSuccess(insertresult.isSuccess());
            result.setResult(insertresult.getResult());
        } else if (user.getUserpassword().isEmpty()) {
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
        return result;
    }

    @PUT
    @Path("UPDATEUSERLEVELID")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult UPDATEUSERLEVEL(final User user) throws SQLException {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        ACRGBWSResult insertresult = methods.CHANGEUSELEVELID(dataSource, user.getUserid(), user.getLeveid());
        result.setMessage(insertresult.getMessage());
        result.setSuccess(insertresult.isSuccess());
        result.setResult(insertresult.getResult());
        return result;
    }

    @PUT
    @Path("RESETPASSWORD")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult RESETPASSWORD(final User user) throws SQLException {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        ACRGBWSResult insertresult = methods.RESETPASSWORD(dataSource, user.getUserid(), user.getUserpassword());
        result.setMessage(insertresult.getMessage());
        result.setSuccess(insertresult.isSuccess());
        result.setResult(insertresult.getResult());
        return result;
    }

    @PUT
    @Path("INACTIVE")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult INACTIVE(final Archived arhived) throws SQLException, ParseException {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        ACRGBWSResult insertresult = insertmethods.INACTIVEDATA(dataSource, arhived.getTags(), arhived.getDataid());
        result.setMessage(insertresult.getMessage());
        result.setSuccess(insertresult.isSuccess());
        result.setResult(insertresult.getResult());
        return result;
    }

    @PUT
    @Path("ACTIVE")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ACRGBWSResult ACTIVE(final Archived arhived) throws SQLException, ParseException {
        //TODO return proper representation object
        ACRGBWSResult result = utility.ACRGBWSResult();
        ACRGBWSResult insertresult = insertmethods.ACTIVEDATA(dataSource, arhived.getTags(), arhived.getDataid());
        result.setMessage(insertresult.getMessage());
        result.setSuccess(insertresult.isSuccess());
        result.setResult(insertresult.getResult());
        return result;
    }

}
