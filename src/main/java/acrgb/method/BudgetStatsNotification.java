/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.method;

import acrgb.structure.ACRGBWSResult;
import acrgb.structure.Contract;
import acrgb.utility.Utility;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;

/**
 *
 * @author MinoSun
 */
@RequestScoped
public class BudgetStatsNotification {

    public BudgetStatsNotification() {
    }
    private final Utility utility = new Utility();

    public ACRGBWSResult Notify(final DataSource dataSource, final Contract contract) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setSuccess(false);
        result.setResult("");
        // try (Connection connection = dataSource.getConnection()) {
        try {
//             ACRGBWSResult restA = new Methods().GETROLEREVERESE(dataSource, contract.getHcfid(), "ACTIVE");//GET (PROID) USING (USERID)

            // } catch (SQLException ex) {
        } catch (Exception ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(BudgetStatsNotification.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
