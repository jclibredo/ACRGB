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
import acrgb.structure.HealthCareFacility;
import acrgb.structure.NclaimsData;
import acrgb.structure.Tranch;
import acrgb.structure.UserActivity;
import acrgb.utility.Utility;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
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
 * @author DRG_SHADOWBILLING
 */
@RequestScoped
public class BookingMethod {

    public BookingMethod() {
    }

    private final Utility utility = new Utility();
    private final SimpleDateFormat dateformat = utility.SimpleDateFormat("MM-dd-yyyy");
    private final String DaysExt = utility.webXml(utility.GetString("DaysExtension"));

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
            result.setMessage("Something went wrong");
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
                    + ":ubooknum,:ucondateid,:uaccount,:uconbalance,:uconamount,:uconutilized,:udatecreated,:ucreatedby,:uconid,:uclaimscount)");
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
            getinsertresult.setString("uclaimscount", conBalance.getClaimscount().trim());
            getinsertresult.execute();
            if (getinsertresult.getString("Message").equals("SUCC")) {
                result.setSuccess(true);
                result.setMessage(getinsertresult.getString("Message"));
            } else {
                result.setMessage(getinsertresult.getString("Message"));
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
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
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKG.GETALLCLAIMSFORBOOK("
                    + ":upmccno,"
                    + ":u_tags,"
                    + ":u_from,:u_to); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("upmccno", upmccno.trim());
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
                    ACRGBWSResult hci = new FetchMethods().GETFACILITYID(dataSource, resultset.getString("PMCC_NO").trim());
                    if (hci.isSuccess()) {
                        nclaims.setPmccno(hci.getResult());
                    } else {
                        nclaims.setPmccno("N/A");
                    }
                }
                nclaims.setAccreno(resultset.getString("ACCRENO"));
                nclaims.setClaimamount(resultset.getString("CLAIMAMOUNT"));
                // nclaims.setDatesubmitted(resultset.getString("DATESUBMITTED"));
                nclaims.setDatesubmitted(resultset.getString("DATESUBMITTED") == null
                        || resultset.getString("DATESUBMITTED").isEmpty()
                        || resultset.getString("DATESUBMITTED").equals("") ? "" : dateformat.format(resultset.getTimestamp("DATESUBMITTED")));
                nclaims.setSeries(resultset.getString("SERIES"));
                // nclaims.setDateadmission(resultset.getString("DATE_ADM"));
                nclaims.setDateadmission(resultset.getString("DATE_ADM") == null
                        || resultset.getString("DATE_ADM").isEmpty()
                        || resultset.getString("DATE_ADM").equals("") ? "" : dateformat.format(resultset.getTimestamp("DATE_ADM")));
                // nclaims.setDateadmission(resultset.getString("DATE_ADM"));
                nclaims.setRefiledate(resultset.getString("REFILEDATE") == null
                        || resultset.getString("REFILEDATE").isEmpty()
                        || resultset.getString("REFILEDATE").equals("") ? "" : dateformat.format(resultset.getTimestamp("REFILEDATE")));
                nclaims.setTrn(resultset.getString("TRN"));
                nclaims.setTags(resultset.getString("TAGS"));
                nclaims.setHcfname(resultset.getString("HCFNAME"));
                //C1 RVS CODE
                nclaims.setC1rvcode(resultset.getString("C1_RVS_CODE") == null
                        || resultset.getString("C1_RVS_CODE").equals("")
                        || resultset.getString("C1_RVS_CODE").isEmpty() ? "" : resultset.getString("C1_RVS_CODE"));
                //C2 RVS CODE
                nclaims.setC2rvcode(resultset.getString("C2_RVS_CODE") == null
                        || resultset.getString("C2_RVS_CODE").isEmpty()
                        || resultset.getString("C2_RVS_CODE").equals("") ? "" : resultset.getString("C2_RVS_CODE"));
                //C1 ICD CODE
                nclaims.setC1icdcode(resultset.getString("C1_ICD_CODE") == null
                        || resultset.getString("C1_ICD_CODE").isEmpty()
                        || resultset.getString("C1_ICD_CODE").equals("") ? "" : resultset.getString("C1_ICD_CODE"));
                //C2 ICD CODE
                nclaims.setC2icdcode(resultset.getString("C2_ICD_CODE") == null
                        || resultset.getString("C2_ICD_CODE").isEmpty()
                        || resultset.getString("C2_ICD_CODE").equals("") ? "" : resultset.getString("C2_ICD_CODE"));
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
            result.setMessage("Something went wrong");
            Logger.getLogger(BookingMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET CONTRACT WHERE STATUS IS INACTIVE
    public ACRGBWSResult PROCESSENDEDCONTRACT(
            final DataSource dataSource,
            final Book book,
            final String utags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ArrayList<String> errorList = new ArrayList<>();
        try {
            ACRGBWSResult getConResult = new FetchMethods().GETCONTRACTCONID(dataSource, book.getConid().trim(), utags.trim().toUpperCase());
            if (getConResult.isSuccess()) {
                switch (book.getTags().toUpperCase()) {
                    case "FACILITY": {
                        double totalClaimAmount = 0.00;
                        double totalAssets = 0.00;
                        double totalnumberofclaims = 0.00;
                        Contract HCIContract = utility.ObjectMapper().readValue(getConResult.getResult(), Contract.class);
                        //ACRGBWSResult get
                        if (HCIContract.getContractdate() != null) {
                            ContractDate contractdate = utility.ObjectMapper().readValue(HCIContract.getContractdate(), ContractDate.class);
                            //------------------------------------------------------
                            ACRGBWSResult autoInsert = this.AUTOBOOKDATA(dataSource,
                                    book.getBooknum(),
                                    book.getHcpncode().trim(), "G",
                                    contractdate.getDatefrom().trim(),
                                    contractdate.getDateto().trim(),
                                    book.getCreatedby());
                            if (!autoInsert.isSuccess()) {
                                errorList.add(autoInsert.getMessage());
                            }
                            //-------------------------------------------------------------------
                            ArrayList<HealthCareFacility> testHCIlist = new ArrayList<>();
                            ACRGBWSResult getMainAccre = new GetHCFMultiplePMCCNO().GETFACILITYBYMAINACCRE(dataSource, book.getHcpncode().trim());
                            if (getMainAccre.isSuccess()) {
                                testHCIlist.addAll(Arrays.asList(utility.ObjectMapper().readValue(getMainAccre.getResult(), HealthCareFacility[].class)));
                            } else if (new FetchMethods().GETFACILITYID(dataSource, book.getHcpncode().trim()).isSuccess()) {
                                testHCIlist.add(utility.ObjectMapper().readValue(new FetchMethods().GETFACILITYID(dataSource, book.getHcpncode().trim()).getResult(), HealthCareFacility.class));
                            }
                            if (testHCIlist.size() > 0) {
                                //----------------------------------------
                                for (int yu = 0; yu < testHCIlist.size(); yu++) {
                                    //------------------------------------------------------------------------
                                    ACRGBWSResult getClaimsAmount = this.CLAIMSAMOUNTBOOK(dataSource,
                                            testHCIlist.get(yu).getHcfcode().trim(), "G", contractdate.getDatefrom().trim(), utility.AddMinusDaysDate(contractdate.getDateto().trim(), DaysExt));
                                    if (getClaimsAmount.isSuccess()) {
                                        List<NclaimsData> nclaimsdata = Arrays.asList(utility.ObjectMapper().readValue(getClaimsAmount.getResult(), NclaimsData[].class));
                                        for (int i = 0; i < nclaimsdata.size(); i++) {
                                            if (nclaimsdata.get(i).getRefiledate().isEmpty() || nclaimsdata.get(i).getRefiledate() == null || nclaimsdata.get(i).getRefiledate().equals("")) {
                                                if (HCIContract.getEnddate().isEmpty() || HCIContract.getEnddate().equals("") || HCIContract.getEnddate() == null) {
                                                    if (dateformat.parse(nclaimsdata.get(i).getDatesubmitted()).compareTo(dateformat.parse(utility.AddMinusDaysDate(contractdate.getDateto(), DaysExt))) <= 0) {
                                                        totalnumberofclaims += Integer.parseInt(nclaimsdata.get(i).getTotalclaims());
                                                        totalClaimAmount += Double.parseDouble(nclaimsdata.get(i).getClaimamount());
                                                    }
                                                } else {
                                                    if (dateformat.parse(nclaimsdata.get(i).getDatesubmitted()).compareTo(dateformat.parse(HCIContract.getEnddate().trim())) <= 0) {
                                                        totalnumberofclaims += Integer.parseInt(nclaimsdata.get(i).getTotalclaims());
                                                        totalClaimAmount += Double.parseDouble(nclaimsdata.get(i).getClaimamount());
                                                    }
                                                }
                                            } else {
                                                if (HCIContract.getEnddate().isEmpty() || HCIContract.getEnddate().equals("") || HCIContract.getEnddate() == null) {
                                                    if (dateformat.parse(nclaimsdata.get(i).getRefiledate()).compareTo(dateformat.parse(utility.AddMinusDaysDate(contractdate.getDateto(), DaysExt))) <= 0) {
                                                        totalnumberofclaims += Integer.parseInt(nclaimsdata.get(i).getTotalclaims());
                                                        totalClaimAmount += Double.parseDouble(nclaimsdata.get(i).getClaimamount());
                                                    }
                                                } else {
                                                    if (dateformat.parse(nclaimsdata.get(i).getRefiledate()).compareTo(dateformat.parse(HCIContract.getEnddate().trim())) <= 0) {
                                                        totalnumberofclaims += Integer.parseInt(nclaimsdata.get(i).getTotalclaims());
                                                        totalClaimAmount += Double.parseDouble(nclaimsdata.get(i).getClaimamount());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            ACRGBWSResult restA = new FetchMethods().GETASSETBYIDANDCONID(dataSource, book.getHcpncode().trim(), book.getConid().trim(), utags.trim().toUpperCase());
                            if (restA.isSuccess()) {
                                List<Assets> assetlist = Arrays.asList(utility.ObjectMapper().readValue(restA.getResult(), Assets[].class));
                                for (int g = 0; g < assetlist.size(); g++) {
                                    if (assetlist.get(g).getPreviousbalance() != null) {
                                        Tranch tranch = utility.ObjectMapper().readValue(assetlist.get(g).getTranchid(), Tranch.class);
                                        switch (tranch.getTranchtype()) {
                                            case "1ST": {
                                                totalAssets += Double.parseDouble(assetlist.get(g).getPreviousbalance());
                                                totalAssets += Double.parseDouble(assetlist.get(g).getReleasedamount());
                                                break;
                                            }
                                            case "1STFINAL": {
                                                totalAssets += Double.parseDouble(assetlist.get(g).getReleasedamount());
                                                break;
                                            }
                                            default: {
                                                totalAssets += Double.parseDouble(assetlist.get(g).getReleasedamount());
                                                break;
                                            }
                                        }
                                    } else {
                                        totalAssets += Double.parseDouble(assetlist.get(g).getReleasedamount());
                                    }
                                }
                            }
                            ACRGBWSResult bookReference = this.ACRBOOKING(dataSource, book);
                            if (!bookReference.isSuccess()) {
                                errorList.add(bookReference.getMessage());
                            }
                            //INSERT CON BALANCE 
                            ConBalance conbal = new ConBalance();
                            conbal.setBooknum(book.getBooknum());
                            conbal.setCondateid(contractdate.getCondateid());
                            conbal.setAccount(book.getHcpncode());
                            conbal.setConbalance(String.valueOf(totalAssets - totalClaimAmount));
                            conbal.setConamount(HCIContract.getBaseamount());
                            conbal.setConutilized(String.valueOf(totalClaimAmount));
                            conbal.setDatecreated(book.getDatecreated());
                            conbal.setCreatedby(book.getCreatedby());
                            conbal.setConid(book.getConid());
                            conbal.setClaimscount(String.valueOf(totalnumberofclaims));
                            ACRGBWSResult InsertPreviousba = this.INSERTCONBALANCE(dataSource, conbal);
                            if (!InsertPreviousba.isSuccess()) {
                                errorList.add(InsertPreviousba.getMessage());
                            }
                        }
                        break;
                    }
                    case "HCPN": {
                        double totalClaimAmount = 0.00;
                        double totalAssets = 0.00;
                        double totalnumberofclaims = 0.00;
                        Contract HCPNContract = utility.ObjectMapper().readValue(getConResult.getResult(), Contract.class);
                        if (HCPNContract.getContractdate() != null) {
                            ContractDate contractdate = utility.ObjectMapper().readValue(HCPNContract.getContractdate(), ContractDate.class);   //GETROLEMULITPLEFORENDROLE
                            ACRGBWSResult hciList = new Methods().GETROLEMULITPLEFORENDROLE(dataSource, utags.trim().toUpperCase(), book.getHcpncode().trim(), contractdate.getCondateid());
                            if (hciList.isSuccess()) {
                                List<String> hciCodeList = Arrays.asList(hciList.getResult().split(","));
                                for (int u = 0; u < hciCodeList.size(); u++) {
                                    ACRGBWSResult getHCIContract = new ContractMethod().GETCONTRACT(dataSource, utags, hciCodeList.get(u).trim());
                                    if (getHCIContract.isSuccess()) {
                                        //BOOK PER FACILITY
                                        ACRGBWSResult nonapexBooking = this.NONAPEXBOOKINGPROCESS(dataSource, book, hciCodeList.get(u).trim(), utags);
                                        if (!nonapexBooking.isSuccess()) {
                                            errorList.add(nonapexBooking.getMessage());
                                        }
                                        //END OF BOOKING PER FACILITY
                                        //GET CLAIMS TOTAL AMOUNT UNDER FACILITY
                                        ACRGBWSResult getClaimsAmount = this.CLAIMSAMOUNTBOOK(dataSource,
                                                hciCodeList.get(u).trim(), "G",
                                                contractdate.getDatefrom().trim(),
                                                utility.AddMinusDaysDate(contractdate.getDateto().trim(), DaysExt));
                                        if (getClaimsAmount.isSuccess()) {
                                            Contract hcicon = utility.ObjectMapper().readValue(getHCIContract.getResult(), Contract.class);
                                            List<NclaimsData> nclaimsdata = Arrays.asList(utility.ObjectMapper().readValue(getClaimsAmount.getResult(), NclaimsData[].class));
                                            for (int i = 0; i < nclaimsdata.size(); i++) {
                                                if (nclaimsdata.get(i).getRefiledate().isEmpty() || nclaimsdata.get(i).getRefiledate().equals("") || nclaimsdata.get(i).getRefiledate() == null) {
                                                    if (hcicon.getEnddate().isEmpty() || hcicon.getEnddate() == null || hcicon.getEnddate().equals("")) {
                                                        if (dateformat.parse(nclaimsdata.get(i).getDatesubmitted()).compareTo(dateformat.parse(utility.AddMinusDaysDate(contractdate.getDateto(), DaysExt))) <= 0) {
                                                            totalnumberofclaims += Integer.parseInt(nclaimsdata.get(i).getTotalclaims());
                                                            totalClaimAmount += Double.parseDouble(nclaimsdata.get(i).getClaimamount());
                                                        }
                                                    } else {
                                                        if (dateformat.parse(nclaimsdata.get(i).getDatesubmitted()).compareTo(dateformat.parse(hcicon.getEnddate().trim())) <= 0) {
                                                            totalnumberofclaims += Integer.parseInt(nclaimsdata.get(i).getTotalclaims());
                                                            totalClaimAmount += Double.parseDouble(nclaimsdata.get(i).getClaimamount());
                                                        }
                                                    }
                                                } else {
                                                    if (hcicon.getEnddate().isEmpty() || hcicon.getEnddate() == null || hcicon.getEnddate().equals("")) {
                                                        if (dateformat.parse(nclaimsdata.get(i).getRefiledate()).compareTo(dateformat.parse(utility.AddMinusDaysDate(contractdate.getDateto(), DaysExt))) <= 0) {
                                                            totalnumberofclaims += Integer.parseInt(nclaimsdata.get(i).getTotalclaims());
                                                            totalClaimAmount += Double.parseDouble(nclaimsdata.get(i).getClaimamount());
                                                        }
                                                    } else {
                                                        if (dateformat.parse(nclaimsdata.get(i).getRefiledate()).compareTo(dateformat.parse(hcicon.getEnddate().trim())) <= 0) {
                                                            totalnumberofclaims += Integer.parseInt(nclaimsdata.get(i).getTotalclaims());
                                                            totalClaimAmount += Double.parseDouble(nclaimsdata.get(i).getClaimamount());
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                ACRGBWSResult restA = new FetchMethods().GETASSETBYIDANDCONID(dataSource, book.getHcpncode().trim(), book.getConid().trim(), utags.trim().toUpperCase());
                                if (restA.isSuccess()) {
                                    List<Assets> assetlist = Arrays.asList(utility.ObjectMapper().readValue(restA.getResult(), Assets[].class));
                                    for (int g = 0; g < assetlist.size(); g++) {
                                        if (assetlist.get(g).getPreviousbalance() != null) {
                                            Tranch tranch = utility.ObjectMapper().readValue(assetlist.get(g).getTranchid(), Tranch.class);
                                            switch (tranch.getTranchtype()) {
                                                case "1ST": {
                                                    totalAssets += Double.parseDouble(assetlist.get(g).getPreviousbalance());
                                                    totalAssets += Double.parseDouble(assetlist.get(g).getReleasedamount());
                                                    break;
                                                }
                                                case "1STFINAL": {
                                                    totalAssets += Double.parseDouble(assetlist.get(g).getReleasedamount());
                                                    break;
                                                }
                                                default: {
                                                    totalAssets += Double.parseDouble(assetlist.get(g).getReleasedamount());
                                                    break;
                                                }
                                            }
                                        } else {
                                            totalAssets += Double.parseDouble(assetlist.get(g).getReleasedamount());
                                        }
                                    }
                                }
                                //INSERT BOOKING REFERENCES
                                ACRGBWSResult bookReference = this.ACRBOOKING(dataSource, book);
                                if (!bookReference.isSuccess()) {
                                    errorList.add(bookReference.getMessage());
                                }
                                ConBalance conbal = new ConBalance();
                                conbal.setBooknum(book.getBooknum());
                                conbal.setCondateid(contractdate.getCondateid());
                                conbal.setAccount(book.getHcpncode());
                                conbal.setConbalance(String.valueOf(totalAssets - totalClaimAmount));
                                conbal.setConamount(HCPNContract.getBaseamount());
                                conbal.setConutilized(String.valueOf(totalClaimAmount));
                                conbal.setDatecreated(book.getDatecreated());
                                conbal.setCreatedby(book.getCreatedby());
                                conbal.setConid(book.getConid());
                                conbal.setClaimscount(String.valueOf(totalnumberofclaims));
                                ACRGBWSResult InsertPreviousba = this.INSERTCONBALANCE(dataSource, conbal);
                                if (!InsertPreviousba.isSuccess()) {
                                    errorList.add(InsertPreviousba.getMessage());
                                }
                            } else {
                                result.setMessage(hciList.getMessage());
                            }
                        }
                        break;
                    }
                    case "ALLENDEDCONTRACT":

                        break;
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

        } catch (IOException | ParseException ex) {
            result.setMessage("Something went wrong");
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
        ArrayList<String> errorList = new ArrayList<>();
        ArrayList<NclaimsData> claimslist = new ArrayList<>();
        try {
            ACRGBWSResult getConResult = new FetchMethods().GETCONTRACTCONID(dataSource, contractid, tags.trim().toUpperCase());
            if (getConResult.isSuccess()) {
                switch (type.toUpperCase()) {
                    case "FACILITY": {
                        Contract HciContract = utility.ObjectMapper().readValue(getConResult.getResult(), Contract.class);
                        if (HciContract.getContractdate() != null) {
                            ContractDate contractdate = utility.ObjectMapper().readValue(HciContract.getContractdate(), ContractDate.class);
                            //-------------------------------------------------------------------
                            ArrayList<HealthCareFacility> testHCIlist = new ArrayList<>();
                            ACRGBWSResult getMainAccre = new GetHCFMultiplePMCCNO().GETFACILITYBYMAINACCRE(dataSource, HciContract.getHcfid().trim());
                            if (getMainAccre.isSuccess()) {
                                testHCIlist.addAll(Arrays.asList(utility.ObjectMapper().readValue(getMainAccre.getResult(), HealthCareFacility[].class)));
                            } else if (new FetchMethods().GETFACILITYID(dataSource, HciContract.getHcfid().trim()).isSuccess()) {
                                testHCIlist.add(utility.ObjectMapper().readValue(new FetchMethods().GETFACILITYID(dataSource, HciContract.getHcfid().trim()).getResult(), HealthCareFacility.class));
                            }
                            if (testHCIlist.size() > 0) {
                                //----------------------------------------
                                for (int yu = 0; yu < testHCIlist.size(); yu++) {
                                    //------------------------------------------------------------------------
                                    ACRGBWSResult claimstList = this.GETALLCLAIMSFORBOOK(dataSource,
                                            testHCIlist.get(yu).getHcfcode().trim(), contractdate.getDatefrom(), contractdate.getDateto());
                                    if (claimstList.isSuccess()) {
                                        List<NclaimsData> claimstListResult = Arrays.asList(utility.ObjectMapper().readValue(claimstList.getResult(), NclaimsData[].class));
                                        for (int conb = 0; conb < claimstListResult.size(); conb++) {
                                            if (claimstListResult.get(conb).getRefiledate().isEmpty() || claimstListResult.get(conb).getRefiledate() == null || claimstListResult.get(conb).getRefiledate().equals("")) {
                                                if (HciContract.getEnddate().isEmpty() || HciContract.getEnddate() == null || HciContract.getEnddate().equals("")) {
                                                    if (dateformat.parse(claimstListResult.get(conb).getDatesubmitted()).compareTo(dateformat.parse(utility.AddMinusDaysDate(contractdate.getDateto(), DaysExt))) <= 0) {
                                                        claimslist.add(claimstListResult.get(conb));
                                                    }
                                                } else {
                                                    if (dateformat.parse(claimstListResult.get(conb).getDatesubmitted()).compareTo(dateformat.parse(HciContract.getEnddate().trim())) <= 0) {
                                                        claimslist.add(claimstListResult.get(conb));
                                                    }
                                                }
                                            } else {
                                                if (HciContract.getEnddate().isEmpty() || HciContract.getEnddate() == null || HciContract.getEnddate().equals("")) {
                                                    if (dateformat.parse(claimstListResult.get(conb).getRefiledate()).compareTo(dateformat.parse(utility.AddMinusDaysDate(contractdate.getDateto(), DaysExt))) <= 0) {
                                                        claimslist.add(claimstListResult.get(conb));
                                                    }
                                                } else {
                                                    if (dateformat.parse(claimstListResult.get(conb).getRefiledate()).compareTo(dateformat.parse(HciContract.getEnddate().trim())) <= 0) {
                                                        claimslist.add(claimstListResult.get(conb));
                                                    }

                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    }
                    case "HCPN": {
                        Contract HCPNContract = utility.ObjectMapper().readValue(getConResult.getResult(), Contract.class);
                        if (HCPNContract.getContractdate() != null) {
                            ContractDate contractdate = utility.ObjectMapper().readValue(HCPNContract.getContractdate(), ContractDate.class);
                            ACRGBWSResult FacilityList = new Methods().GETROLEMULITPLE(dataSource, hcpncode.trim(), tags.trim().toUpperCase());
                            if (FacilityList.isSuccess()) {
                                List<String> HCFCodeList = Arrays.asList(FacilityList.getResult().split(","));
                                for (int u = 0; u < HCFCodeList.size(); u++) {
                                    //-------------------------------------------------------------------
                                    ArrayList<HealthCareFacility> testHCIlist = new ArrayList<>();
                                    ACRGBWSResult getMainAccre = new GetHCFMultiplePMCCNO().GETFACILITYBYMAINACCRE(dataSource, HCFCodeList.get(u).trim());
                                    if (getMainAccre.isSuccess()) {
                                        testHCIlist.addAll(Arrays.asList(utility.ObjectMapper().readValue(getMainAccre.getResult(), HealthCareFacility[].class)));
                                    } else if (new FetchMethods().GETFACILITYID(dataSource, HCFCodeList.get(u).trim()).isSuccess()) {
                                        testHCIlist.add(utility.ObjectMapper().readValue(new FetchMethods().GETFACILITYID(dataSource, HCFCodeList.get(u).trim()).getResult(), HealthCareFacility.class));
                                    }
                                    if (testHCIlist.size() > 0) {
                                        for (int yu = 0; yu < testHCIlist.size(); yu++) {
                                            //------------------------------------------------------------------------
                                            ACRGBWSResult claimstList = this.GETALLCLAIMSFORBOOK(dataSource,
                                                    testHCIlist.get(yu).getHcfcode().trim(), contractdate.getDatefrom(), contractdate.getDateto());
                                            if (claimstList.isSuccess()) {
                                                List<NclaimsData> claimstListResult = Arrays.asList(utility.ObjectMapper().readValue(claimstList.getResult(), NclaimsData[].class));
                                                for (int conb = 0; conb < claimstListResult.size(); conb++) {
                                                    if (claimstListResult.get(conb).getRefiledate().isEmpty() || claimstListResult.get(conb).getRefiledate().equals("") || claimstListResult.get(conb).getRefiledate() == null) {
                                                        if (HCPNContract.getEnddate().isEmpty() || HCPNContract.getEnddate() == null || HCPNContract.getEnddate().equals("")) {
                                                            if (dateformat.parse(claimstListResult.get(conb).getDatesubmitted()).compareTo(dateformat.parse(utility.AddMinusDaysDate(contractdate.getDateto(), DaysExt))) <= 0) {
                                                                claimslist.add(claimstListResult.get(conb));
                                                            }
                                                        } else {
                                                            if (dateformat.parse(claimstListResult.get(conb).getDatesubmitted()).compareTo(dateformat.parse(HCPNContract.getEnddate().trim())) <= 0) {
                                                                claimslist.add(claimstListResult.get(conb));
                                                            }
                                                        }
                                                    } else {
                                                        if (HCPNContract.getEnddate().isEmpty() || HCPNContract.getEnddate().equals("") || HCPNContract.getEnddate() == null) {
                                                            if (dateformat.parse(claimstListResult.get(conb).getRefiledate()).compareTo(dateformat.parse(utility.AddMinusDaysDate(contractdate.getDateto(), DaysExt))) <= 0) {
                                                                claimslist.add(claimstListResult.get(conb));
                                                            }
                                                        } else {
                                                            if (dateformat.parse(claimstListResult.get(conb).getRefiledate()).compareTo(dateformat.parse(HCPNContract.getEnddate().trim())) <= 0) {
                                                                claimslist.add(claimstListResult.get(conb));
                                                            }
                                                        }
                                                    }
                                                }
                                            }
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

        } catch (IOException | ParseException ex) {
            result.setMessage("Something went wrong");
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
            ArrayList<NclaimsData> nclaimsdataList = new ArrayList<>();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            while (resultset.next()) {
                NclaimsData nclaimsdata = new NclaimsData();
                nclaimsdata.setClaimamount(resultset.getString("CLAIMSTOTAL"));
                nclaimsdata.setTotalclaims(resultset.getString("CLAIMSVOLUME"));
                // nclaims.setDatesubmitted(resultset.getString("DATESUBMITTED"));
                nclaimsdata.setDatesubmitted(resultset.getString("DATESUB") == null
                        || resultset.getString("DATESUB").isEmpty()
                        || resultset.getString("DATESUB").equals("") ? "" : dateformat.format(resultset.getTimestamp("DATESUB")));
                nclaimsdata.setDateadmission(resultset.getString("DATEADM") == null
                        || resultset.getString("DATEADM").isEmpty()
                        || resultset.getString("DATEADM").equals("") ? "" : dateformat.format(resultset.getTimestamp("DATEADM")));
                // nclaims.setDateadmission(resultset.getString("DATE_ADM"));
                nclaimsdata.setRefiledate(resultset.getString("DATEREFILE") == null
                        || resultset.getString("DATEREFILE").isEmpty()
                        || resultset.getString("DATEREFILE").equals("") ? "" : dateformat.format(resultset.getTimestamp("DATEREFILE")));
                nclaimsdataList.add(nclaimsdata);
            }
            if (nclaimsdataList.size() > 0) {
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(nclaimsdataList));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage("Something went wrong");
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
            //-----------------------------------------------------------------
            ArrayList<HealthCareFacility> testHCIlist = new ArrayList<>();
            ACRGBWSResult getMainAccre = new GetHCFMultiplePMCCNO().GETFACILITYBYMAINACCRE(dataSource, upmmcno.trim());
            if (getMainAccre.isSuccess()) {
                testHCIlist.addAll(Arrays.asList(utility.ObjectMapper().readValue(getMainAccre.getResult(), HealthCareFacility[].class)));
            } else if (new FetchMethods().GETFACILITYID(dataSource, upmmcno.trim()).isSuccess()) {
                testHCIlist.add(utility.ObjectMapper().readValue(new FetchMethods().GETFACILITYID(dataSource, upmmcno.trim()).getResult(), HealthCareFacility.class));
            }
            if (testHCIlist.size() > 0) {
                for (int yu = 0; yu < testHCIlist.size(); yu++) {
                    //------------------------------------------------------
                    CallableStatement statement = connection.prepareCall("call DRG_SHADOWBILLING.ACRGBPKG.AUTOBOOKDATA(:Message,:Code,"
                            + ":ubooknum,:upmmcno,:utags,:udatefrom,:udateto)");
                    statement.registerOutParameter("Message", OracleTypes.VARCHAR);
                    statement.registerOutParameter("Code", OracleTypes.INTEGER);
                    statement.setString("ubooknum", ubooknum.trim().toUpperCase());
                    statement.setString("upmmcno", testHCIlist.get(yu).getHcfcode().trim());
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
                }
            }

        } catch (SQLException | IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(BookingMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult NONAPEXBOOKINGPROCESS(final DataSource dataSource,
            final Book book,
            final String upmmcno,
            final String utags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            //MANAGE FINAL BALANCE OF FACILITY CONTRACT 
            ACRGBWSResult getHCIContract = new ContractMethod().GETCONTRACTWITHOPENSTATE(dataSource,
                    utags.trim().toUpperCase(),
                    upmmcno.trim().toUpperCase(),
                    "OPEN".trim().toUpperCase());
            if (getHCIContract.isSuccess()) {
                double totalnumberofclaims = 0.00;
                int totalClaimAmount = 0;
                double totalAssets = 0.00;
                ArrayList<String> errorList = new ArrayList<>();
                Contract HCIContract = utility.ObjectMapper().readValue(getHCIContract.getResult(), Contract.class);
                if (HCIContract.getContractdate() != null) {
                    ContractDate conDate = utility.ObjectMapper().readValue(HCIContract.getContractdate(), ContractDate.class);
                    //AUTOBOOK AREA
                    ACRGBWSResult autoInsert = this.AUTOBOOKDATA(dataSource,
                            book.getBooknum(),
                            upmmcno.trim(),
                            "G",
                            conDate.getDatefrom().trim(),
                            utility.AddMinusDaysDate(conDate.getDateto().trim(), DaysExt),
                            book.getCreatedby());
                    if (!autoInsert.isSuccess()) {
                        errorList.add(autoInsert.getMessage());
                    }
                    //END OF AUTO BOOK
                    ArrayList<HealthCareFacility> testHCIlist = new ArrayList<>();
                    ACRGBWSResult getMainAccre = new GetHCFMultiplePMCCNO().GETFACILITYBYMAINACCRE(dataSource, upmmcno.trim());
                    if (getMainAccre.isSuccess()) {
                        testHCIlist.addAll(Arrays.asList(utility.ObjectMapper().readValue(getMainAccre.getResult(), HealthCareFacility[].class)));
                    } else if (new FetchMethods().GETFACILITYID(dataSource, upmmcno.trim()).isSuccess()) {
                        testHCIlist.add(utility.ObjectMapper().readValue(new FetchMethods().GETFACILITYID(dataSource, upmmcno.trim()).getResult(), HealthCareFacility.class));
                    }
                    if (testHCIlist.size() > 0) {
                        for (int yu = 0; yu < testHCIlist.size(); yu++) {
                            //------------------------------------------------------------------------
                            ACRGBWSResult getClaimsAmount = this.CLAIMSAMOUNTBOOK(dataSource,
                                    testHCIlist.get(yu).getHcfcode().trim(), "G",
                                    conDate.getDatefrom().trim(),
                                    utility.AddMinusDaysDate(conDate.getDateto().trim(), DaysExt));
                            if (getClaimsAmount.isSuccess()) {
                                List<NclaimsData> nclaimsdata = Arrays.asList(utility.ObjectMapper().readValue(getClaimsAmount.getResult(), NclaimsData[].class));
                                for (int i = 0; i < nclaimsdata.size(); i++) {
                                    if (nclaimsdata.get(i).getRefiledate().isEmpty() || nclaimsdata.get(i).getRefiledate() == null || nclaimsdata.get(i).getRefiledate().equals("")) {
                                        if (HCIContract.getEnddate().isEmpty() || HCIContract.getEnddate().equals("") || HCIContract.getEnddate() == null) {
                                            if (dateformat.parse(nclaimsdata.get(i).getDatesubmitted()).compareTo(dateformat.parse(utility.AddMinusDaysDate(conDate.getDateto(), DaysExt))) <= 0) {
                                                totalnumberofclaims += Integer.parseInt(nclaimsdata.get(i).getTotalclaims());
                                                totalClaimAmount += Double.parseDouble(nclaimsdata.get(i).getClaimamount());
                                            }
                                        } else {
                                            if (dateformat.parse(nclaimsdata.get(i).getDatesubmitted()).compareTo(dateformat.parse(HCIContract.getEnddate())) <= 0) {
                                                totalnumberofclaims += Integer.parseInt(nclaimsdata.get(i).getTotalclaims());
                                                totalClaimAmount += Double.parseDouble(nclaimsdata.get(i).getClaimamount());
                                            }
                                        }
                                    } else {
                                        if (HCIContract.getEnddate().isEmpty() || HCIContract.getEnddate().equals("") || HCIContract.getEnddate() == null) {
                                            if (dateformat.parse(nclaimsdata.get(i).getRefiledate()).compareTo(dateformat.parse(utility.AddMinusDaysDate(conDate.getDateto(), DaysExt))) <= 0) {
                                                totalnumberofclaims += Integer.parseInt(nclaimsdata.get(i).getTotalclaims());
                                                totalClaimAmount += Double.parseDouble(nclaimsdata.get(i).getClaimamount());
                                            }
                                        } else {
                                            if (dateformat.parse(nclaimsdata.get(i).getRefiledate()).compareTo(dateformat.parse(HCIContract.getEnddate())) <= 0) {
                                                totalnumberofclaims += Integer.parseInt(nclaimsdata.get(i).getTotalclaims());
                                                totalClaimAmount += Double.parseDouble(nclaimsdata.get(i).getClaimamount());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    ACRGBWSResult restA = new FetchMethods().GETASSETBYIDANDCONID(dataSource, upmmcno.trim(), HCIContract.getConid().trim(), utags.trim().toUpperCase());
                    if (restA.isSuccess()) {
                        List<Assets> assetlist = Arrays.asList(utility.ObjectMapper().readValue(restA.getResult(), Assets[].class));
                        for (int g = 0; g < assetlist.size(); g++) {
                            if (assetlist.get(g).getPreviousbalance() != null) {
                                Tranch tranch = utility.ObjectMapper().readValue(assetlist.get(g).getTranchid(), Tranch.class);
                                switch (tranch.getTranchtype()) {
                                    case "1ST": {
                                        totalAssets += Double.parseDouble(assetlist.get(g).getPreviousbalance());
                                        totalAssets += Double.parseDouble(assetlist.get(g).getReleasedamount());
                                        break;
                                    }
                                    case "1STFINAL": {
                                        totalAssets += Double.parseDouble(assetlist.get(g).getReleasedamount());
                                        break;
                                    }
                                    default: {
                                        totalAssets += Double.parseDouble(assetlist.get(g).getReleasedamount());
                                        break;
                                    }
                                }
                            } else {
                                totalAssets += Double.parseDouble(assetlist.get(g).getReleasedamount());
                            }
                        }
                    }
                    ACRGBWSResult bookReference = this.ACRBOOKING(dataSource, book);
                    if (!bookReference.isSuccess()) {
                        errorList.add(bookReference.getMessage());
                    }
                    ConBalance conbal = new ConBalance();
                    conbal.setBooknum(book.getBooknum());
                    conbal.setCondateid(conDate.getCondateid());
                    conbal.setAccount(HCIContract.getHcfid());
                    conbal.setConbalance(String.valueOf(totalAssets - totalClaimAmount));
                    conbal.setConamount(HCIContract.getBaseamount());
                    conbal.setConutilized(String.valueOf(totalClaimAmount));
                    conbal.setDatecreated(book.getDatecreated());
                    conbal.setCreatedby(book.getCreatedby());
                    conbal.setConid(HCIContract.getConid());
                    conbal.setClaimscount(String.valueOf(totalnumberofclaims));
                    ACRGBWSResult InsertPreviousba = this.INSERTCONBALANCE(dataSource, conbal);
                    if (!InsertPreviousba.isSuccess()) {
                        errorList.add(InsertPreviousba.getMessage());
                    }
                    if (errorList.size() > 0) {
                        result.setMessage(String.join(",", errorList));
                    } else {
                        result.setMessage("OK");
                        result.setSuccess(true);
                    }
                } else {
                    result.setMessage("NO CONTRACT DATE");
                }
            } else {
                result.setMessage("NO CONTRACT");
            }
            //END MANAGE FINAL BALANCE OF FACILITY CONTRACT 

        } catch (IOException | ParseException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(BookingMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
