/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.method;

import acrgb.structure.ACRGBWSResult;
import acrgb.structure.Assets;
import acrgb.structure.Book;
import acrgb.structure.ConBalance;
import acrgb.structure.Contract;
import acrgb.structure.ContractDate;
import acrgb.structure.NclaimsData;
import acrgb.structure.UserActivity;
import acrgb.utility.Utility;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
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
public class BookingMethod {

    public BookingMethod() {
    }

    private final Utility utility = new Utility();
    private final SimpleDateFormat dateformat = utility.SimpleDateFormat("MM-dd-yyyy");

    public ACRGBWSResult ACRBOOKING(final DataSource dataSource, final Book book) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement getinsertresult = connection.prepareCall("call DRG_SHADOWBILLING.ACRGBPKGPROCEDURE.ACRBOOKING(:Message,:Code,"
                    + ":ubooknum,:uconid,:udatecreated,:ucreatedby)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setString("ubooknum", book.getBooknum().trim());
            getinsertresult.setString("uconid", book.getConid().trim());
            getinsertresult.setDate("udatecreated", (Date) new Date(utility.StringToDate(book.getDatecreated()).getTime()));
            getinsertresult.setString("ucreatedby", book.getCreatedby().trim());
            getinsertresult.execute();
            if (getinsertresult.getString("Message").equals("SUCC")) {
                result.setSuccess(true);
                result.setMessage(getinsertresult.getString("Message"));
            } else {
                result.setMessage(getinsertresult.getString("Message"));
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(BookingMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult INSERTCONBALANCE(final DataSource dataSource, final ConBalance conBalance) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement getinsertresult = connection.prepareCall("call DRG_SHADOWBILLING.ACRGBPKGPROCEDURE.INSERTCONBALANCE(:Message,:Code,"
                    + ":ubooknum,:ucondateid,:uaccount,:uconbalance,:uconamount,:uconutilized,:udatecreated,:ucreatedby,:uconid)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setString("ubooknum", conBalance.getBooknum().trim());
            getinsertresult.setString("ucondateid", conBalance.getCondateid().trim());
            getinsertresult.setString("uaccount", conBalance.getAccount().trim());
            getinsertresult.setString("uconbalance", conBalance.getConbalance());
            getinsertresult.setString("uconamount", conBalance.getConamount().trim());
            getinsertresult.setString("uconutilized", conBalance.getConutilized().trim());
            getinsertresult.setDate("udatecreated", (Date) new Date(utility.StringToDate(conBalance.getDatecreated()).getTime()));
            getinsertresult.setString("ucreatedby", conBalance.getCreatedby().trim());
            getinsertresult.setString("uconid", conBalance.getConid().trim());
            getinsertresult.execute();
            if (getinsertresult.getString("Message").equals("SUCC")) {
                result.setSuccess(true);
                result.setMessage(getinsertresult.getString("Message"));
            } else {
                result.setMessage(getinsertresult.getString("Message"));
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(BookingMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET CONTRACT WHERE STATUS IS ACTIVE
    public ACRGBWSResult GETALLCLAIMSFORBOOK(final DataSource dataSource,
            final String upmccno,
            final String datestart,
            final String dateend) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        FetchMethods fm = new FetchMethods();
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := "
                    + "DRG_SHADOWBILLING.ACRGBPKG.GETALLCLAIMSFORBOOK("
                    + ":u_accreno,"
                    + ":u_tags,"
                    + ":u_from,:u_to); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("u_accreno", upmccno.trim());
            statement.setString("u_tags", "G".trim());
            statement.setDate("u_from", (Date) new Date(utility.StringToDate(datestart).getTime()));
            statement.setDate("u_to", (Date) new Date(utility.StringToDate(dateend).getTime()));
            statement.execute();
            ArrayList<NclaimsData> nclaimsList = new ArrayList<>();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            while (resultset.next()) {
                NclaimsData nclaims = new NclaimsData();
                nclaims.setClaimid(resultset.getString("CLAIMID"));
                if (resultset.getString("PMCC_NO") == null) {
                    nclaims.setPmccno("N/A");
                } else {
                    ACRGBWSResult hci = fm.GETFACILITYID(dataSource, resultset.getString("PMCC_NO").trim());
                    if (hci.isSuccess()) {
                        nclaims.setPmccno(hci.getResult());
                    } else {
                        nclaims.setPmccno("N/A");
                    }
                }
                nclaims.setAccreno(resultset.getString("ACCRENO"));
                nclaims.setClaimamount(resultset.getString("CLAIMAMOUNT"));
                // nclaims.setDatesubmitted(resultset.getString("DATESUBMITTED"));
                if (resultset.getString("DATESUBMITTED") == null) {
                    nclaims.setDatesubmitted(resultset.getString("DATESUBMITTED"));
                } else {
                    nclaims.setDatesubmitted(dateformat.format(resultset.getDate("DATESUBMITTED")));
                }
                nclaims.setSeries(resultset.getString("SERIES"));
                // nclaims.setDateadmission(resultset.getString("DATE_ADM"));
                if (resultset.getString("DATE_ADM") == null) {
                    nclaims.setDateadmission(resultset.getString("DATE_ADM"));
                } else {
                    nclaims.setDateadmission(dateformat.format(resultset.getDate("DATE_ADM")));
                }
                nclaims.setTrn(resultset.getString("TRN"));
                nclaims.setTags(resultset.getString("TAGS"));
                nclaims.setHcfname(resultset.getString("HCFNAME"));
                //C1 RVS CODE
                if (resultset.getString("C1_RVS_CODE") == null) {
                    nclaims.setC1rvcode("");
                } else {
                    nclaims.setC1rvcode(resultset.getString("C1_RVS_CODE"));
                }
                //C2 RVS CODE
                if (resultset.getString("C2_RVS_CODE") == null) {
                    nclaims.setC2rvcode("");
                } else {
                    nclaims.setC2rvcode(resultset.getString("C2_RVS_CODE"));
                }
                //C1 ICD CODE
                if (resultset.getString("C1_ICD_CODE") == null) {
                    nclaims.setC1icdcode("");
                } else {
                    nclaims.setC1icdcode(resultset.getString("C1_ICD_CODE"));
                }
                //C2 ICD CODE
                if (resultset.getString("C2_ICD_CODE") == null) {
                    nclaims.setC2icdcode("");
                } else {
                    nclaims.setC2icdcode(resultset.getString("C2_ICD_CODE"));
                }
                nclaimsList.add(nclaims);
            }
            if (nclaimsList.size() > 0) {
                result.setResult(utility.ObjectMapper().writeValueAsString(nclaimsList));
                result.setMessage("OK");
                result.setSuccess(true);
            } else {
                result.setMessage("N/A");
            }

        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET CONTRACT WHERE STATUS IS INACTIVE
    public ACRGBWSResult GETENDEDCONTRACT(final DataSource dataSource, final Book book, final String utags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        FetchMethods fm = new FetchMethods();
        Methods methods = new Methods();
        ArrayList<String> errorList = new ArrayList<>();
        try {
            ACRGBWSResult getConResult = fm.GETCONTRACTCONID(dataSource, book.getConid(), utags.trim().toUpperCase());
            if (getConResult.isSuccess()) {
                switch (book.getTags().toUpperCase()) {
                    case "FACILITY": {
                        double totalClaimAmount = 0.00;
                        double totalClaimAssets = 0.00;
                        Contract HCIContract = utility.ObjectMapper().readValue(getConResult.getResult(), Contract.class);
                        //ACRGBWSResult get
                        if (HCIContract.getContractdate() != null) {
                            ContractDate contractdate = utility.ObjectMapper().readValue(HCIContract.getContractdate(), ContractDate.class);
                            ACRGBWSResult autoInsert = this.AUTOBOOKDATA(dataSource,
                                    book.getBooknum(),
                                    book.getHcpncode().trim(), "G",
                                    contractdate.getDatefrom().trim(),
                                    contractdate.getDateto().trim(),
                                    book.getCreatedby());
                            if (!autoInsert.isSuccess()) {
                                errorList.add(autoInsert.getMessage());
                            }
                            //GET CLAIMS TOTAL AMOUNT UNDER FACILITY
                            ACRGBWSResult getClaimsAmount = this.CLAIMSAMOUNTBOOK(dataSource,
                                    book.getHcpncode().trim(), "G", contractdate.getDatefrom().trim(), contractdate.getDateto().trim());
                            if (getClaimsAmount.isSuccess()) {
                                totalClaimAmount += Double.parseDouble(getClaimsAmount.getResult());
                            }
                            //INSERT BOOKING REFERENCES
                            ACRGBWSResult bookReference = this.ACRBOOKING(dataSource, book);
                            if (!bookReference.isSuccess()) {
                                errorList.add(bookReference.getMessage());
                            }
                            //INSERT PREVIOUS BALANCE
                            ACRGBWSResult GetAssetsByConID = fm.GETASSETSBYCONID(dataSource, HCIContract.getConid());
                            if (GetAssetsByConID.isSuccess()) {
                                List<Assets> listOfAssets = Arrays.asList(utility.ObjectMapper().readValue(GetAssetsByConID.getResult(), Assets[].class));
                                for (int u = 0; u < listOfAssets.size(); u++) {
                                    totalClaimAssets += Double.parseDouble(listOfAssets.get(u).getAmount());
                                }
                                //INSERT CON BALANCE 
                                ConBalance conbal = new ConBalance();
                                conbal.setBooknum(book.getBooknum());
                                conbal.setCondateid(contractdate.getCondateid());
                                conbal.setAccount(book.getHcpncode());
                                conbal.setConbalance(String.valueOf(totalClaimAssets - totalClaimAmount));
                                conbal.setConamount(HCIContract.getBaseamount());
                                conbal.setConutilized(String.valueOf(totalClaimAmount));
                                conbal.setDatecreated(book.getDatecreated());
                                conbal.setCreatedby(book.getCreatedby());
                                conbal.setConid(book.getConid());
                                ACRGBWSResult InsertPreviousba = this.INSERTCONBALANCE(dataSource, conbal);
                                if (!InsertPreviousba.isSuccess()) {
                                    errorList.add(InsertPreviousba.getMessage());
                                }
                            }
                        }
                        break;
                    }
                    case "HCPN": {
                        double totalClaimAmount = 0.00;
                        double totalClaimAssets = 0.00;
                        Contract HCPNContract = utility.ObjectMapper().readValue(getConResult.getResult(), Contract.class);
                        if (HCPNContract.getContractdate() != null) {
                            ContractDate contractdate = utility.ObjectMapper().readValue(HCPNContract.getContractdate(), ContractDate.class);
                            ACRGBWSResult hciList = methods.GETROLEMULITPLE(dataSource, book.getHcpncode().trim(), utags.trim().toUpperCase());
                            if (hciList.isSuccess()) {
                                List<String> hciCodeList = Arrays.asList(hciList.getResult().split(","));
                                for (int u = 0; u < hciCodeList.size(); u++) {
                                    ACRGBWSResult autoInsert = this.AUTOBOOKDATA(dataSource,
                                            book.getBooknum(),
                                            hciCodeList.get(u).trim(), "G",
                                            contractdate.getDatefrom().trim(),
                                            contractdate.getDateto().trim(),
                                            book.getCreatedby());
                                    if (!autoInsert.isSuccess()) {
                                        errorList.add(autoInsert.getMessage());
                                    }
                                    //GET CLAIMS TOTAL AMOUNT UNDER FACILITY
                                    ACRGBWSResult getClaimsAmount = this.CLAIMSAMOUNTBOOK(dataSource,
                                            hciCodeList.get(u).trim(), "G", contractdate.getDatefrom().trim(), contractdate.getDateto().trim());
                                    if (getClaimsAmount.isSuccess()) {
                                        totalClaimAmount += Double.parseDouble(getClaimsAmount.getResult());
                                    }
                                }
                                //INSERT BOOKING REFERENCES
                                ACRGBWSResult bookReference = this.ACRBOOKING(dataSource, book);
                                if (!bookReference.isSuccess()) {
                                    errorList.add(bookReference.getMessage());
                                }
                                //INSERT PREVIOUS BALANCE
                                ACRGBWSResult GetAssetsByConID = fm.GETASSETSBYCONID(dataSource, HCPNContract.getConid());
                                if (GetAssetsByConID.isSuccess()) {
                                    List<Assets> listOfAssets = Arrays.asList(utility.ObjectMapper().readValue(GetAssetsByConID.getResult(), Assets[].class));
                                    for (int u = 0; u < listOfAssets.size(); u++) {
                                        totalClaimAssets += Double.parseDouble(listOfAssets.get(u).getAmount());
                                    }
                                    //INSERT CON BALANCE 
                                    ConBalance conbal = new ConBalance();
                                    conbal.setBooknum(book.getBooknum());
                                    conbal.setCondateid(contractdate.getCondateid());
                                    conbal.setAccount(book.getHcpncode());
                                    conbal.setConbalance(String.valueOf(totalClaimAssets - totalClaimAmount));
                                    conbal.setConamount(HCPNContract.getBaseamount());
                                    conbal.setConutilized(String.valueOf(totalClaimAmount));
                                    conbal.setDatecreated(book.getDatecreated());
                                    conbal.setCreatedby(book.getCreatedby());
                                    conbal.setConid(book.getConid());
                                    ACRGBWSResult InsertPreviousba = this.INSERTCONBALANCE(dataSource, conbal);
                                    if (!InsertPreviousba.isSuccess()) {
                                        errorList.add(InsertPreviousba.getMessage());
                                    }
                                }
                            } else {
                                result.setMessage(hciList.getMessage());
                            }
                        }
                        break;
                    }
                }
            } else {
                result.setMessage(getConResult.getMessage());
            }
            if (errorList.isEmpty()) {
                result.setMessage("OK");
                result.setSuccess(true);
            } else {
                result.setMessage("N/A");
                result.setResult(utility.ObjectMapper().writeValueAsString(errorList));
            }

        } catch (IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(BookingMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET CONTRACT WHERE STATUS IS ACTIVE
    public ACRGBWSResult GETALLCLAIMS(final DataSource dataSource,
            final String hcpncode,
            final String contractid,
            final String type,
            final String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        FetchMethods fm = new FetchMethods();
        Methods methods = new Methods();
        ArrayList<String> errorList = new ArrayList<>();
        ArrayList<NclaimsData> claimslist = new ArrayList<>();
        try {
            ACRGBWSResult getConResult = fm.GETCONTRACTCONID(dataSource, contractid, tags.trim().toUpperCase());
            if (getConResult.isSuccess()) {
                switch (type.toUpperCase()) {
                    case "FACILITY": {
                        Contract HciContract = utility.ObjectMapper().readValue(getConResult.getResult(), Contract.class);
                        if (HciContract.getContractdate() != null) {
                            ContractDate contractdate = utility.ObjectMapper().readValue(HciContract.getContractdate(), ContractDate.class);
                            ACRGBWSResult claimstList = this.GETALLCLAIMSFORBOOK(dataSource,
                                    HciContract.getHcfid(), contractdate.getDatefrom(), contractdate.getDateto());
                            if (claimstList.isSuccess()) {
                                List<NclaimsData> claimstListResult = Arrays.asList(utility.ObjectMapper().readValue(claimstList.getResult(), NclaimsData[].class));
                                for (int conb = 0; conb < claimstListResult.size(); conb++) {
                                    claimslist.add(claimstListResult.get(conb));
                                }
                            }
                        }
                        break;
                    }
                    case "HCPN": {
                        Contract HCPNContract = utility.ObjectMapper().readValue(getConResult.getResult(), Contract.class);
                        if (HCPNContract.getContractdate() != null) {
                            ContractDate contractdate = utility.ObjectMapper().readValue(HCPNContract.getContractdate(), ContractDate.class);
                            ACRGBWSResult FacilityList = methods.GETROLEMULITPLE(dataSource, hcpncode.trim(), tags.trim().toUpperCase());
                            if (FacilityList.isSuccess()) {
                                List<String> HCFCodeList = Arrays.asList(FacilityList.getResult().split(","));
                                for (int u = 0; u < HCFCodeList.size(); u++) {
                                    ACRGBWSResult claimstList = this.GETALLCLAIMSFORBOOK(dataSource,
                                            HCFCodeList.get(u), contractdate.getDatefrom(), contractdate.getDateto());
                                    if (claimstList.isSuccess()) {
                                        List<NclaimsData> claimstListResult = Arrays.asList(utility.ObjectMapper().readValue(claimstList.getResult(), NclaimsData[].class));
                                        for (int conb = 0; conb < claimstListResult.size(); conb++) {
                                            claimslist.add(claimstListResult.get(conb));
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    }
                }
            } else {
                result.setMessage(getConResult.getMessage());
            }

            if (errorList.isEmpty()) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(claimslist));
            } else {
                result.setMessage("N/A");
                result.setResult(utility.ObjectMapper().writeValueAsString(errorList));
            }

        } catch (IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(BookingMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult CLAIMSAMOUNTBOOK(final DataSource dataSource,
            final String upmmcno,
            final String utags,
            final String udatefrom,
            final String udateto) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKG.CLAIMSAMOUNTBOOK(:upmmcno,:utags,"
                    + ":udatefrom,:udateto); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("upmmcno", upmmcno.trim().toUpperCase());
            statement.setString("utags", utags.trim().toUpperCase());
            statement.setDate("udatefrom", (Date) new Date(utility.StringToDate(udatefrom.trim().toUpperCase()).getTime()));
            statement.setDate("udateto", (Date) new Date(utility.StringToDate(udateto.trim().toUpperCase()).getTime()));
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                result.setMessage(resultset.getString("CLAIMSVOLUME"));
                result.setSuccess(true);
                result.setResult(resultset.getString("CLAIMSTOTAL"));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(BookingMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult AUTOBOOKDATA(final DataSource dataSource,
            final String ubooknum,
            final String upmmcno,
            final String utags,
            final String udatefrom,
            final String udateto,
            final String createdby) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        UserActivityLogs logs = new UserActivityLogs();
        try (Connection connection = dataSource.getConnection()) {
            UserActivity userlogs = utility.UserActivity();
            CallableStatement statement = connection.prepareCall("call DRG_SHADOWBILLING.ACRGBPKG.AUTOBOOKDATA(:Message,:Code,"
                    + ":ubooknum,:upmmcno,:utags,:udatefrom,:udateto)");
            statement.registerOutParameter("Message", OracleTypes.VARCHAR);
            statement.registerOutParameter("Code", OracleTypes.INTEGER);
            statement.setString("ubooknum", ubooknum.trim().toUpperCase());
            statement.setString("upmmcno", upmmcno.trim().toUpperCase());
            statement.setString("utags", utags.trim().toUpperCase());
            statement.setDate("udatefrom", (Date) new Date(utility.StringToDate(udatefrom.trim().toUpperCase()).getTime()));
            statement.setDate("udateto", (Date) new Date(utility.StringToDate(udateto.trim().toUpperCase()).getTime()));
            statement.execute();
            //------------------------------------------------------------------------------------------------
            if (statement.getString("Message").equals("SUCC")) {
                result.setSuccess(true);
                result.setMessage(statement.getString("Message"));
                userlogs.setActstatus("SUCCESS");
            } else {
                result.setMessage(statement.getString("Message"));
                userlogs.setActstatus("FAILED");
            }
            userlogs.setActdetails(" book " + upmmcno + " | " + statement.getString("Message"));
            userlogs.setActby(createdby); // 1,2,2,APEX,HCPN
            logs.UserLogsMethod(dataSource, "INSERT-CLAIMS-BOOK-DATA", userlogs, upmmcno, "0");
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(BookingMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
