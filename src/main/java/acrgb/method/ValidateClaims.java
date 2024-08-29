/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.method;

import acrgb.structure.ACRGBWSResult;
import acrgb.structure.ContractDate;
import acrgb.structure.UserRoleIndex;
import acrgb.utility.Utility;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
public class ValidateClaims {

    private final Utility utility = new Utility();

    public ACRGBWSResult ValidateClaims(final DataSource dataSource, final String upmccno, final String useries) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        FetchMethods fm = new FetchMethods();
        try {
            int counter = 0;
            ACRGBWSResult getResult = this.GETROLEREVERESE(dataSource, upmccno, "ACTIVE");
            if (getResult.isSuccess()) {
                List<UserRoleIndex> userRoleList = Arrays.asList(utility.ObjectMapper().readValue(getResult.getResult(), UserRoleIndex[].class));
                for (int i = 0; i < userRoleList.size(); i++) {
                    if (!userRoleList.get(i).getContractdate().isEmpty()) {
                        ContractDate contractDate = utility.ObjectMapper().readValue(userRoleList.get(i).getContractdate(), ContractDate.class);
                        ACRGBWSResult getClaims = fm.GETNCLAIMS(dataSource, upmccno, "G", contractDate.getDatefrom(), contractDate.getDateto(), "CURRENTSTATUS");
                        if (getClaims.isSuccess()) {
                            counter++;
                        }
                    }
                }
            }
            if (counter > 0) {
                result.setMessage("Claims is under from GB Facility");
                result.setSuccess(true);
            }
        } catch (IOException ex) {
            Logger.getLogger(ValidateClaims.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }

    public ACRGBWSResult GETROLEREVERESE(final DataSource dataSource, final String puserid, final String utags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ContractMethod cm = new ContractMethod();
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETROLEWITHIDREVERSE(:utags,:pid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("utags", utags.trim());
            statement.setString("pid", puserid.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            ArrayList<UserRoleIndex> userRoleList = new ArrayList<>();
            while (resultset.next()) {
                if (resultset.getString("CONDATE") != null) {
                    UserRoleIndex userRole = new UserRoleIndex();
                    userRole.setUserid(resultset.getString("USERID"));
                    userRole.setAccessid(resultset.getString("ACCESSID"));
                    ACRGBWSResult getContract = cm.GETCONDATEBYID(dataSource, resultset.getString("CONDATE"));
                    if (getContract.isSuccess()) {
                        userRole.setContractdate(getContract.getResult());
                    } else {
                        userRole.setContractdate("");
                    }
                    userRoleList.add(userRole);
                }
            }
            if (userRoleList.size() > 0) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(userRoleList));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(ValidateClaims.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
