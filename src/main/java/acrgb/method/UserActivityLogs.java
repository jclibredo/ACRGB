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
import acrgb.structure.UserLevel;
import acrgb.utility.Utility;
import java.io.IOException;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;

/**
 *
 * @author MinoSun
 */
@RequestScoped
public class UserActivityLogs {

    private final FetchMethods fm = new FetchMethods();
    private final Methods m = new Methods();
    private final Utility utility = new Utility();

    public void UserLogsMethod(final DataSource dataSource, final String tags, final UserActivity userActivity, final String objectid1, final String objectid2) throws IOException {

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
            case "EDIT-CONTRACT-DATE": {

                break;
            }
            //TRANCHE CATEGORY
            case "ADD-TRANCHE": {
                m.ActivityLogs(dataSource, userActivity.getActby(), "ADD " + GetSubjectData(dataSource, "ACCOUNT", userActivity.getActby()) + " insert " + userActivity.getActdetails(), userActivity.getActstatus());
                break;
            }
            //ADD USER LEVEL 
            case "ADD-USER-LEVEL": {
                m.ActivityLogs(dataSource, userActivity.getActby(), "ADD " + GetSubjectData(dataSource, "ACCOUNT", userActivity.getActby()) + " insert " + userActivity.getActdetails(), userActivity.getActstatus());
                break;
            }
            case "EDIT-USER-LEVEL": {

                break;
            }
            case "DELETE-USER-LEVEL": {

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
            case "EDIT-USERACCOUNT": {

                break;
            }
            case "DELETE-USERACCOUNT": {

                break;
            }
            case "LOGIN-USERACCOUNT": {

                break;
            }
            //USER INFO ACTIVITY
            case "ADD-USERINFO": {
                m.ActivityLogs(dataSource, userActivity.getActby(), "CREATE " + GetSubjectData(dataSource, "ACCOUNT", userActivity.getActby()) + " insert " + userActivity.getActdetails(), userActivity.getActstatus());
                break;
            }
            case "ADD-USERINFO-BATCH": {
                String account = "";
                if (GetSubjectData(dataSource, "PRO", objectid2).equals("false")) {
                    if (GetSubjectData(dataSource, "HCPN", objectid2).equals("false")) {
                        account = GetSubjectData(dataSource, "HCI", objectid2);
                    } else {
                        account = GetSubjectData(dataSource, "HCPN", objectid2);
                    }
                } else {
                    account = GetSubjectData(dataSource, "PRO", objectid2);
                }
                m.ActivityLogs(dataSource, userActivity.getActby(), "CREATE " + GetSubjectData(dataSource, "ACCOUNT", userActivity.getActby()) + " inserted new user info " + userActivity.getActdetails() + " for " + account, userActivity.getActstatus());
                break;
            }
            case "EDIT-USERINFO": {
                m.ActivityLogs(dataSource, userActivity.getActby(), "UPDATE " + GetSubjectData(dataSource, "ACCOUNT", userActivity.getActby()) + " edited " + GetSubjectData(dataSource, "USERINFO", objectid1) + " " + userActivity.getActdetails(), userActivity.getActstatus());
                break;
            }
            case "DELETE-USERINFO": {

                break;
            }
            //HCPN ACTIVITY
            case "ADD-HCPN": {
                m.ActivityLogs(dataSource, userActivity.getActby(), "CREATE " + GetSubjectData(dataSource, "ACCOUNT", userActivity.getActby()) + " create " + userActivity.getActdetails() + " Under to :" + GetSubjectData(dataSource, "PRO", userActivity.getActby()), userActivity.getActstatus());
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
            case "DELETE-HCPN": {

                break;
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
            case "EDIT-TRANCHE-HCPN": {
                m.ActivityLogs(dataSource, userActivity.getActby(), "ADD " + GetSubjectData(dataSource, "ACCOUNT", userActivity.getActby()) + " edit " + GetSubjectData(dataSource, "TRANCHE", objectid2) + " tranche from " + GetSubjectData(dataSource, "HCPN", objectid1), userActivity.getActstatus());
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
            //DATE CONTRACT PERIOD ACTIVITY
            case "ADD-CONTRACT-PERIOD": {

                break;
            }
            case "EDIT-CONTRACT-PERIOD": {

                break;
            }
            case "DELETE-CONTRACT-PERIOD": {

                break;
            }
            //MAP FACILITY TO HCPN
            case "ADD-HCI-HCPN": {

                break;
            }
            case "DELETE-HCI-HCPN": {

                break;
            }

            //ACCOUNT ADD ACCESS
            case "ADD-ACCOUNT-PRO": {

                break;
            }
            case "DELETE-ACCOUNT-PRO": {

                break;
            }

            case "ADD-ACCOUNT-HCPN": {

                break;
            }
            case "DELETE-ACCOUNT-HCPN": {

                break;
            }
            case "ADD-ACCOUNT-HCI": {

                break;
            }
            case "DELETE-ACCOUNT-HCI": {

                break;
            }

            //CONTRACT TAGGING ACTIVITY
            case "END-CONTRACT-TAG": {

                break;
            }
            case "TERMINATE-CONTRACT-TAG": {

                break;
            }

        }

    }

    public String GetSubjectData(final DataSource dataSource, final String tags, final String id) throws IOException {
        String result = "";
        switch (tags.toUpperCase().trim()) {
            case "ACCOUNT": { //USERID
                if (fm.GETUSERBYUSERID(dataSource, id).isSuccess()) {
                    User user = utility.ObjectMapper().readValue(fm.GETUSERBYUSERID(dataSource, id).getResult(), User.class);
                    if (fm.GETUSERDETAILSBYDID(dataSource, user.getDid()).isSuccess()) {
                        UserInfo userInfo = utility.ObjectMapper().readValue(fm.GETUSERBYUSERID(dataSource, user.getDid()).getResult(), UserInfo.class);
                        result = userInfo.getLastname() + " , " + userInfo.getFirstname();
                    } else {
                        result = "false";
                    }
                } else {
                    result = "false";
                }
                break;
            }
            case "PRO": { //PRO CODE
                if (m.GetProWithPROID(dataSource, id).isSuccess()) {
                    Pro pro = utility.ObjectMapper().readValue(m.GetProWithPROID(dataSource, id).getResult(), Pro.class);
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
                    UserLevel userLevel = utility.ObjectMapper().readValue(fm.GETUSERLEVEL(dataSource, id).getResult(), UserLevel.class);
                    result = userLevel.getLevname() + " | " + userLevel.getLevdetails();
                } else {
                    result = "false";
                }
                break;
            }
            case "USERINFO": { //USER DID
                if (fm.GETUSERDETAILSBYDID(dataSource, id).isSuccess()) {
                    UserInfo userInfo = utility.ObjectMapper().readValue(fm.GETUSERDETAILSBYDID(dataSource, id).getResult(), UserInfo.class);
                    result = "LastName: " + userInfo.getLastname() + " | FirstName :" + userInfo.getFirstname() + " | Username :" + userInfo.getEmail();
                } else {
                    result = "false";
                }
                break;
            }
        }
        return result;
    }

}
