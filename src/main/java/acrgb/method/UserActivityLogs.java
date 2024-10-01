/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.method;

import acrgb.structure.Contract;
import acrgb.structure.ContractDate;
import acrgb.structure.HealthCareFacility;
import acrgb.structure.ManagingBoard;
import acrgb.structure.Pro;
import acrgb.structure.Tranch;
import acrgb.structure.User;
import acrgb.structure.UserActivity;
import acrgb.structure.UserInfo;
import acrgb.utility.Utility;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;

/**
 *
 * @author MinoSun
 */
@RequestScoped
public class UserActivityLogs {

    private final FetchMethods fm = new FetchMethods();
    private final Utility utility = new Utility();

    public void UserLogsMethod(final DataSource dataSource, final String tags, final UserActivity userActivity, final String objectid1, final String objectid2) {
        Methods m = new Methods();
        switch (tags.toUpperCase().trim()) {
            //ACTIVE AND INACTIVE
            case "ACTIVE-DATA": {
                m.ActivityLogs(dataSource, userActivity.getActby(), "UPDATE " + GetSubjectData(dataSource, "ACCOUNT", userActivity.getActby()) + " set " + userActivity.getActdetails() + " data to active with id " + objectid1, userActivity.getActstatus());
                break;
            }
            case "INACTIVE-DATA": {
                m.ActivityLogs(dataSource, userActivity.getActby(), "UPDATE " + GetSubjectData(dataSource, "ACCOUNT", userActivity.getActby()) + " set " + userActivity.getActdetails() + " data to inactive with id " + objectid1, userActivity.getActstatus());
                break;
            }
            //INSERT BOOK CLAIMS DATA
            case "INSERT-CLAIMS-BOOK-DATA": {
                m.ActivityLogs(dataSource, userActivity.getActby(), "SAVE " + GetSubjectData(dataSource, "ACCOUNT", userActivity.getActby()) + " book calims data " + userActivity.getActdetails() + " " + GetSubjectData(dataSource, "HCI", objectid1), userActivity.getActstatus());
                break;
            }
            case "INSERT-BOOK-REF": {
                m.ActivityLogs(dataSource, userActivity.getActby(), "CREATE " + GetSubjectData(dataSource, "ACCOUNT", userActivity.getActby()) + " booking reference " + userActivity.getActdetails() + " for contract:" + GetSubjectData(dataSource, "CONTRACT", objectid1) + " of " + GetSubjectData(dataSource, "HCPN", objectid2), userActivity.getActstatus());
                break;
            }
            //CONTRACT DATE PERIOD
            case "ADD-CONTRACT-DATE": {
                m.ActivityLogs(dataSource, userActivity.getActby(), "CREATE " + GetSubjectData(dataSource, "ACCOUNT", userActivity.getActby()) + " insert " + userActivity.getActdetails(), userActivity.getActstatus());
                break;
            }
            //TRANCHE CATEGORY
            case "ADD-TRANCHE": {
                m.ActivityLogs(dataSource, userActivity.getActby(), "ADD " + GetSubjectData(dataSource, "ACCOUNT", userActivity.getActby()) + " insert " + userActivity.getActdetails(), userActivity.getActstatus());
                break;
            }
            case "EDIT-TRANCHE": {
                m.ActivityLogs(dataSource, userActivity.getActby(), "UPDATE " + GetSubjectData(dataSource, "ACCOUNT", userActivity.getActby()) + " tranche category edited " + userActivity.getActdetails(), userActivity.getActstatus());
                break;
            }
            //ADD USER LEVEL
            case "ADD-USER-LEVEL": {
                m.ActivityLogs(dataSource, userActivity.getActby(), "ADD " + GetSubjectData(dataSource, "ACCOUNT", userActivity.getActby()) + " insert " + userActivity.getActdetails(), userActivity.getActstatus());
                break;
            }
            case "EDIT-USER-LEVEL": {
                m.ActivityLogs(dataSource, userActivity.getActby(), "UPDATE " + GetSubjectData(dataSource, "ACCOUNT", userActivity.getActby()) + " update user level data  " + userActivity.getActdetails(), userActivity.getActstatus());
                break;
            }
            //USER ACCOUNT ACTIVITY
            case "ADD-USERACCOUNT": {
                m.ActivityLogs(dataSource, userActivity.getActby(), "ADD " + GetSubjectData(dataSource, "ACCOUNT", userActivity.getActby()) + " create " + GetSubjectData(dataSource, "USERINFO", objectid2) + " with Role " + GetSubjectData(dataSource, "USERLEVEL", objectid1) + " " + userActivity.getActdetails(), userActivity.getActstatus());
                break;
            }
            case "ADD-USERACCOUNT-BATCH": {
                m.ActivityLogs(dataSource, userActivity.getActby(), "ADD " + GetSubjectData(dataSource, "ACCOUNT", userActivity.getActby()) + " create new account with " + userActivity.getActdetails() + " with Role " + GetSubjectData(dataSource, "USERLEVEL", objectid1), userActivity.getActstatus());
                break;
            }
            case "EDIT-USERINFO-EMAIL": {
                m.ActivityLogs(dataSource, userActivity.getActby(), "UPDATE " + GetSubjectData(dataSource, "ACCOUNT", userActivity.getActby()) + " edited " + userActivity.getActdetails(), userActivity.getActstatus());
                break;
            }
            //USER INFO ACTIVITY
            case "ADD-USERINFO": {
                m.ActivityLogs(dataSource, userActivity.getActby(), "CREATE " + GetSubjectData(dataSource, "ACCOUNT", userActivity.getActby()) + " insert " + userActivity.getActdetails(), userActivity.getActstatus());
                break;
            }
            case "ADD-USERINFO-BATCH": {
                m.ActivityLogs(dataSource, userActivity.getActby(), "CREATE " + GetSubjectData(dataSource, "ACCOUNT", userActivity.getActby()) + " inserted new user info " + userActivity.getActdetails() + " for " + GetAccount(dataSource, objectid2), userActivity.getActstatus());
                break;
            }
            case "EDIT-USERINFO": {
                m.ActivityLogs(dataSource, userActivity.getActby(), "UPDATE " + GetSubjectData(dataSource, "ACCOUNT", userActivity.getActby()) + " edited " + GetSubjectData(dataSource, "USERINFO", objectid1) + " " + userActivity.getActdetails(), userActivity.getActstatus());
                break;
            }
            //HCPN ACTIVITY
            case "ADD-HCPN": {
                m.ActivityLogs(dataSource, userActivity.getActby(), "CREATE " + GetSubjectData(dataSource, "ACCOUNT", userActivity.getActby()) + " create " + userActivity.getActdetails() + " Under to :" + GetSubjectData(dataSource, "PRO", userActivity.getActby()), userActivity.getActstatus());
                break;
            }
            case "EDIT-ACCREDITATION-HCPN": {
                m.ActivityLogs(dataSource, userActivity.getActby(), "UPDATE " + GetSubjectData(dataSource, "ACCOUNT", userActivity.getActby()) + " UPDATE " + userActivity.getActdetails() + " for :" + GetSubjectData(dataSource, "HCPN", objectid2), userActivity.getActstatus());
                break;
            }
            case "ADD-ACCREDITATION-HCPN": {
                m.ActivityLogs(dataSource, userActivity.getActby(), "CREATE " + GetSubjectData(dataSource, "ACCOUNT", userActivity.getActby()) + " insert " + userActivity.getActdetails() + " for :" + GetSubjectData(dataSource, "HCPN", objectid1), userActivity.getActstatus());
                break;
            }
            //APPELIATE MODULE
            case "ADD-APPELIATE": {
                m.ActivityLogs(dataSource, userActivity.getActby(), "ADD " + GetSubjectData(dataSource, "ACCOUNT", userActivity.getActby()) + " affiliate " + GetSubjectData(dataSource, "HCI", objectid1) + " " + userActivity.getActdetails() + " to :" + GetSubjectData(dataSource, "HCPN", objectid2), userActivity.getActstatus());
                break;
            }
            case "REMOVED-APPELIATE": {
                m.ActivityLogs(dataSource, userActivity.getActby(), "REMOVED " + GetSubjectData(dataSource, "ACCOUNT", userActivity.getActby()) + " removed affiliate from " + GetSubjectData(dataSource, "HCI", objectid1) + " " + userActivity.getActdetails() + " to :" + GetSubjectData(dataSource, "HCPN", objectid2), userActivity.getActstatus());
                break;
            }
            case "EDIT-HCPN": {
                m.ActivityLogs(dataSource, userActivity.getActby(), "UPDATE " + GetSubjectData(dataSource, "ACCOUNT", userActivity.getActby()) + " edit " + objectid2 + "  to :" + userActivity.getActdetails(), userActivity.getActstatus());
                break;
            }
            //CONTRACT TAGGING
            case "TAGGING-CONTRACT": {
                m.ActivityLogs(dataSource, userActivity.getActby(), "UPDATE " + GetSubjectData(dataSource, "ACCOUNT", userActivity.getActby()) + " tagg contract of " + GetAccount(dataSource, objectid1.trim()) + "  acction perform :" + userActivity.getActdetails(), userActivity.getActstatus());
                break;
                // break;
            }
            //CONTRACT ACTIVITY   CONTRACT-DATE
            case "ADD-CONTRACT-PRO": {
                m.ActivityLogs(dataSource, userActivity.getActby(), "ADD " + GetSubjectData(dataSource, "ACCOUNT", userActivity.getActby()) + " insert " + userActivity.getActdetails() + " date covered :" + GetSubjectData(dataSource, "CONTRACT-DATE", objectid2) + " quarter contract to " + GetSubjectData(dataSource, "PRO", objectid1) + " Quarter", userActivity.getActstatus());
                break;
            }
            case "ADD-CONTRACT-HCPN": {
                m.ActivityLogs(dataSource, userActivity.getActby(), "ADD " + GetSubjectData(dataSource, "ACCOUNT", userActivity.getActby()) + " insert " + userActivity.getActdetails() + " date covered :" + GetSubjectData(dataSource, "CONTRACT-DATE", objectid2) + " contract to " + GetSubjectData(dataSource, "HCPN", objectid1), userActivity.getActstatus());
                break;
            }
            case "ADD-CONTRACT-HCI": {
                m.ActivityLogs(dataSource, userActivity.getActby(), "ADD " + GetSubjectData(dataSource, "ACCOUNT", userActivity.getActby()) + " insert " + userActivity.getActdetails() + " date covered :" + GetSubjectData(dataSource, "CONTRACT-DATE", objectid2) + " contract to " + GetSubjectData(dataSource, "HCI", objectid1), userActivity.getActstatus());
                break;
            }
            case "EDIT-CONTRACT-PRO": {
                m.ActivityLogs(dataSource, userActivity.getActby(), "EDIT " + GetSubjectData(dataSource, "ACCOUNT", userActivity.getActby()) + " update " + userActivity.getActdetails() + " date covered :" + GetSubjectData(dataSource, "CONTRACT-DATE", objectid2) + " quarter contract to " + GetSubjectData(dataSource, "PRO", objectid1) + " Quarter", userActivity.getActstatus());
                break;
            }
            case "EDIT-CONTRACT-HCPN": {
                m.ActivityLogs(dataSource, userActivity.getActby(), "EDIT " + GetSubjectData(dataSource, "ACCOUNT", userActivity.getActby()) + " update " + userActivity.getActdetails() + " date covered :" + GetSubjectData(dataSource, "CONTRACT-DATE", objectid2) + " contract to " + GetSubjectData(dataSource, "HCPN", objectid1), userActivity.getActstatus());
                break;
            }
            case "EDIT-CONTRACT-HCI": {
                m.ActivityLogs(dataSource, userActivity.getActby(), "EDIT " + GetSubjectData(dataSource, "ACCOUNT", userActivity.getActby()) + " update " + userActivity.getActdetails() + " date covered :" + GetSubjectData(dataSource, "CONTRACT-DATE", objectid2) + " contract to " + GetSubjectData(dataSource, "HCI", objectid1), userActivity.getActstatus());
                break;
            }
            case "DELETE-CONTRACT-PRO": {
                m.ActivityLogs(dataSource, userActivity.getActby(), "DELETE " + GetSubjectData(dataSource, "ACCOUNT", userActivity.getActby()) + " deleted " + userActivity.getActdetails() + " date covered :" + GetSubjectData(dataSource, "CONTRACT-DATE", objectid2) + " quarter contract to " + GetSubjectData(dataSource, "PRO", objectid1) + " Quarter", userActivity.getActstatus());
                break;
            }
            case "DELETE-CONTRACT-HCPN": {
                m.ActivityLogs(dataSource, userActivity.getActby(), "DELETE " + GetSubjectData(dataSource, "ACCOUNT", userActivity.getActby()) + " deleted " + userActivity.getActdetails() + " date covered :" + GetSubjectData(dataSource, "CONTRACT-DATE", objectid2) + " contract to " + GetSubjectData(dataSource, "HCPN", objectid1), userActivity.getActstatus());
                break;
            }
            case "DELETE-CONTRACT-HCI": {
                m.ActivityLogs(dataSource, userActivity.getActby(), "DELETE " + GetSubjectData(dataSource, "ACCOUNT", userActivity.getActby()) + " deleted " + userActivity.getActdetails() + " date covered :" + GetSubjectData(dataSource, "CONTRACT-DATE", objectid2) + " contract to " + GetSubjectData(dataSource, "HCI", objectid1), userActivity.getActstatus());
                break;
            }
            //TRANCHE ACTIVITY  ASSETS
            case "ADD-TRANCHE-HCPN": {
                m.ActivityLogs(dataSource, userActivity.getActby(), "ADD " + GetSubjectData(dataSource, "ACCOUNT", userActivity.getActby()) + " released " + GetSubjectData(dataSource, "TRANCHE", objectid2) + " tranche to " + GetSubjectData(dataSource, "HCPN", objectid1) + " " + userActivity.getActdetails(), userActivity.getActstatus());
                break;
            }
            case "ADD-TRANCHE-HCI": {
                m.ActivityLogs(dataSource, userActivity.getActby(), "ADD " + GetSubjectData(dataSource, "ACCOUNT", userActivity.getActby()) + " released " + GetSubjectData(dataSource, "TRANCHE", objectid2) + " tranche to " + GetSubjectData(dataSource, "HCI", objectid1) + " " + userActivity.getActdetails(), userActivity.getActstatus());
                break;
            }
            case "EDIT-TRANCHE-HCI": {
                m.ActivityLogs(dataSource, userActivity.getActby(), "UPDATE " + GetSubjectData(dataSource, "ACCOUNT", userActivity.getActby()) + " edit " + GetSubjectData(dataSource, "TRANCHE", objectid2) + " tranche from " + GetSubjectData(dataSource, "HCI", objectid1), userActivity.getActstatus());
                break;
            }
            case "DELETE-TRANCHE-HCPN": {
                m.ActivityLogs(dataSource, userActivity.getActby(), "DELETE " + GetSubjectData(dataSource, "ACCOUNT", userActivity.getActby()) + " removed " + GetSubjectData(dataSource, "TRANCHE", objectid2) + " tranche from " + GetSubjectData(dataSource, "HCPN", objectid1), userActivity.getActstatus());
                break;
            }
            case "DELETE-TRANCHE-HCI": {
                m.ActivityLogs(dataSource, userActivity.getActby(), "DELETE " + GetSubjectData(dataSource, "ACCOUNT", userActivity.getActby()) + " removed " + GetSubjectData(dataSource, "TRANCHE", objectid2) + " tranche from " + GetSubjectData(dataSource, "HCI", objectid1), userActivity.getActstatus());
                break;
            }
            //CONTRACT TAGGING ACTIVITY
            case "ADD-ACCESS": {
                m.ActivityLogs(dataSource, userActivity.getActby(), "ADD " + GetSubjectData(dataSource, "ACCOUNT", userActivity.getActby()) + " add access to " + GetAccount(dataSource, objectid2) + " for " + GetAccount(dataSource, objectid1) + " | " + userActivity.getActdetails(), userActivity.getActstatus());
                break;
            }
            case "REMOVED-ACCESS": {
                m.ActivityLogs(dataSource, userActivity.getActby(), "REMOVED " + GetSubjectData(dataSource, "ACCOUNT", userActivity.getActby()) + " removed access from " + GetAccount(dataSource, objectid2) + " for " + GetAccount(dataSource, objectid1) + " | " + userActivity.getActdetails(), userActivity.getActstatus());
                break;
            }

        }

    }

    public String GetAccount(final DataSource dataSource, final String accountid) {
        String account = "";
        if (GetSubjectData(dataSource, "HCPN", accountid).equals("false")) {
            if (GetSubjectData(dataSource, "HCI", accountid).equals("false")) {
                account = GetSubjectData(dataSource, "PRO", accountid);
            } else {
                account = GetSubjectData(dataSource, "HCI", accountid);
            }
        } else {
            account = GetSubjectData(dataSource, "HCPN", accountid);
        }
        return account;
    }

    public String GetSubjectData(final DataSource dataSource, final String tags, final String id) {
        Methods m = new Methods();
        String result = "";
        try {
            switch (tags.toUpperCase().trim()) {
                case "ACCOUNT": { //USERID
                    if (fm.GETUSERBYUSERID(dataSource, id, "INACTIVE").isSuccess()) {
                        User user = utility.ObjectMapper().readValue(fm.GETUSERBYUSERID(dataSource, id, "INACTIVE").getResult(), User.class);
                        if (!user.getDid().equals("N/A")) {
                            UserInfo userInfo = utility.ObjectMapper().readValue(user.getDid(), UserInfo.class);
                            result = userInfo.getLastname() + " , " + userInfo.getFirstname();
                        } else {
                            result = "false";
                        }
                    } else {
                        if (fm.GETUSERBYUSERID(dataSource, id, "ACTIVE").isSuccess()) {
                            User user = utility.ObjectMapper().readValue(fm.GETUSERBYUSERID(dataSource, id, "ACTIVE").getResult(), User.class);
                            if (!user.getDid().equals("N/A")) {
                                UserInfo userInfo = utility.ObjectMapper().readValue(user.getDid(), UserInfo.class);
                                result = userInfo.getLastname() + " , " + userInfo.getFirstname();
                            } else {
                                result = "false";
                            }
                        } else {
                            result = "false";
                        }
                    }

                    break;
                }
                case "PRO": { //PRO CODE
                    if (m.GetProWithPROID(dataSource, id.trim()).isSuccess()) {
                        Pro pro = utility.ObjectMapper().readValue(m.GetProWithPROID(dataSource, id.trim()).getResult(), Pro.class);
                        result = pro.getProname();
                    } else {
                        result = "false";
                    }
                    break;
                }
                case "HCPN": { //HCPN CODE
                    if (m.GETMBWITHID(dataSource, id).isSuccess()) {
                        ManagingBoard mb = utility.ObjectMapper().readValue(m.GETMBWITHID(dataSource, id).getResult(), ManagingBoard.class);
                        result = mb.getMbname();
                    } else {
                        result = "false";
                    }
                    break;
                }
                case "HCI": { //HCI PMCC NO
                    if (fm.GETFACILITYID(dataSource, id).isSuccess()) {
                        HealthCareFacility hci = utility.ObjectMapper().readValue(fm.GETFACILITYID(dataSource, id).getResult(), HealthCareFacility.class);
                        result = hci.getHcfname();
                    } else {
                        result = "false";
                    }
                    break;
                }
                case "CONTRACT": { //CONTRACT ID
                    if (fm.GETCONTRACTCONID(dataSource, id.trim(), "ACTIVE").isSuccess()) {
                        Contract contract = utility.ObjectMapper().readValue(fm.GETCONTRACTCONID(dataSource, id.trim(), "ACTIVE").getResult(), Contract.class);
                        result = contract.getTranscode() + " | " + contract.getAmount();
                    } else {
                        result = "false";
                    }
                    break;
                }
                case "TRANCHE": { //TRANCHE ID
                    if (fm.ACR_TRANCHWITHID(dataSource, id).isSuccess()) {
                        Tranch tranch = utility.ObjectMapper().readValue(fm.ACR_TRANCHWITHID(dataSource, id).getResult(), Tranch.class);
                        result = tranch.getTranchtype();
                    } else {
                        result = "false";
                    }
                    break;
                }

                case "CONTRACT-DATE": { //CONTRACT DATE ID
                    ContractMethod cm = new ContractMethod();
                    if (cm.GETCONDATEBYID(dataSource, id).isSuccess()) {
                        ContractDate conDate = utility.ObjectMapper().readValue(cm.GETCONDATEBYID(dataSource, id).getResult(), ContractDate.class);
                        result = conDate.getDatefrom() + " - " + conDate.getDateto();
                    } else {
                        result = "false";
                    }
                    break;
                }

                case "USERLEVEL": { //LEVEL ID
                    if (fm.GETUSERLEVEL(dataSource, id).isSuccess()) {
                        result = fm.GETUSERLEVEL(dataSource, id).getResult();
                    } else {
                        result = "false";
                    }
                    break;
                }
                case "USERINFO": { //USER DID
                    if (fm.GETUSERDETAILSBYDID(dataSource, id, "ACTIVE").isSuccess()) {
                        UserInfo userInfo = utility.ObjectMapper().readValue(fm.GETUSERDETAILSBYDID(dataSource, id, "ACTIVE").getResult(), UserInfo.class);
                        result = "LastName: " + userInfo.getLastname() + " | FirstName :" + userInfo.getFirstname() + " | Username :" + userInfo.getEmail();
                    } else {
                        if (fm.GETUSERDETAILSBYDID(dataSource, id, "INACTIVE").isSuccess()) {
                            UserInfo userInfo = utility.ObjectMapper().readValue(fm.GETUSERDETAILSBYDID(dataSource, id, "INACTIVE").getResult(), UserInfo.class);
                            result = "LastName: " + userInfo.getLastname() + " | FirstName :" + userInfo.getFirstname() + " | Username :" + userInfo.getEmail();
                        } else {
                            result = "false";
                        }
                    }
                    break;
                }
            }
        } catch (IOException ex) {
            ex.getLocalizedMessage();
            Logger.getLogger(UserActivityLogs.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
