/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb;

import acrgb.method.UpdateMethods;
import acrgb.structure.ACRGBWSResult;
import acrgb.structure.Area;
import acrgb.structure.AreaType;
import acrgb.structure.Assets;
import acrgb.structure.Contract;
import acrgb.structure.HealthCareFacility;
import acrgb.structure.Tranch;
import acrgb.structure.UserLevel;
import acrgb.utility.Utility;
import java.sql.SQLException;
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

}
