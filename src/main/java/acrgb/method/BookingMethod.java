/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.method;

import acrgb.structure.ACRGBWSResult;
import acrgb.structure.ConBalance;
import acrgb.structure.Contract;
import acrgb.utility.Utility;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;
import oracle.jdbc.OracleTypes;

/**
 *
 * @author MinoSun
 */
@RequestScoped
public class BookingMethod {

    public BookingMethod() {
    }

    private final Utility utility = new Utility();

    public ACRGBWSResult INSERTCONBALANCE(final DataSource dataSource, final ConBalance conBalance) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.INSERTCONBALANCE(:Message,:Code,"
                    + ":ubooknum,:ucondateid,:uaccount,:uconbalance,:uconutilized,:udatecreated,:ucreatedby,:uconid)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setString("ubooknum", conBalance.getBooknum());
            getinsertresult.setString("ucondateid", conBalance.getCondateid());
            getinsertresult.setString("uaccount", conBalance.getAccount());
            getinsertresult.setString("uconbalance", conBalance.getConbalance());
            getinsertresult.setString("uconutilized", conBalance.getConutilized());
            getinsertresult.setDate("udatecreated", (Date) new Date(utility.StringToDate(conBalance.getDatecreated()).getTime()));
            getinsertresult.setString("ucreatedby", conBalance.getCreatedby());
            getinsertresult.setString("uconid", conBalance.getConid());
            getinsertresult.execute();
            if (getinsertresult.getString("Message").equals("SUCC")) {
                result.setSuccess(true);
                result.setMessage(getinsertresult.getString("Message"));
            } else {
                result.setMessage(getinsertresult.getString("Message"));
            }
        } catch (SQLException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(BookingMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET CONTRACT WHERE STATUS IS ACTIVE
    public ACRGBWSResult GETACTIVECONTRACT(final DataSource dataSource,
            final String condateid,
            final String conid,
            final String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        FetchMethods fm = new FetchMethods();
        Methods methods = new Methods();
        try {
            ACRGBWSResult getConResult = fm.GETCONTRACTCONID(dataSource, conid);
            if (getConResult.isSuccess()) {
                switch (tags.toUpperCase()) {
                    case "FACILITY":
                        Contract HciContract = utility.ObjectMapper().readValue(getConResult.getResult(), Contract.class);
                        //ACRGBWSResult get

                        break;
                    case "HCPN":
                        Contract HCPNContract = utility.ObjectMapper().readValue(getConResult.getResult(), Contract.class);
                        ACRGBWSResult FacilityList = methods.GETROLEMULITPLE(dataSource, HCPNContract.getHcfid());
                        if (FacilityList.isSuccess()) {
                            List<String> HCFCodeList = Arrays.asList(FacilityList.getResult().split(","));
                            for(int u=0; u<HCFCodeList.size(); u++){
                            
                                
                                
                            }

                        } else {
                            result.setMessage(FacilityList.getMessage());
                        }
                        break;
                }

            } else {
                result.setMessage(getConResult.getMessage());
            }

        } catch (IOException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(BookingMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
