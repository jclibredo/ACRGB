/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.method;

import acrgb.structure.ACRGBWSResult;
import acrgb.structure.Contract;
import acrgb.structure.HealthCareFacility;
import acrgb.structure.ManagingBoard;
import acrgb.utility.Utility;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
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
public class ContractHistory {

    public ContractHistory() {
    }

    private final Utility utility = new Utility();
    private final FetchMethods fm = new FetchMethods();
    private final Methods m = new Methods();

    //GET CONTRACT HISTORY OF HCPN
    public ACRGBWSResult GetHCPNContract(final DataSource dataSource, final String hcpncode, final String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ArrayList<Contract> contractList = new ArrayList<>();
        try {
            ACRGBWSResult GetContract = fm.GETALLCONTRACT(dataSource, tags.toUpperCase().trim(), hcpncode.trim());
            if (GetContract.isSuccess()) {
                List<Contract> mapContractList = Arrays.asList(utility.ObjectMapper().readValue(GetContract.getResult(), Contract[].class));
                for (int y = 0; y < mapContractList.size(); y++) {
                    contractList.add(mapContractList.get(y));
                }
            }
            if (contractList.size() > 0) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(contractList));
            } else {
                result.setMessage("N/A");
            }
        } catch (IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(ContractHistory.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET CONTRACT HISTORY OF FACILITY
    public ACRGBWSResult GetHCIContract(final DataSource dataSource, final String facilitycode, final String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ArrayList<Contract> contractList = new ArrayList<>();
        try {
            ACRGBWSResult GetContract = fm.GETALLCONTRACT(dataSource, tags.toUpperCase().trim(), facilitycode.trim());
            if (GetContract.isSuccess()) {
                List<Contract> mapContractList = Arrays.asList(utility.ObjectMapper().readValue(GetContract.getResult(), Contract[].class));
                for (int y = 0; y < mapContractList.size(); y++) {
                    contractList.add(mapContractList.get(y));
                }
            }
            if (contractList.size() > 0) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(contractList));
            } else {
                result.setMessage("N/A");
            }

        } catch (IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(ContractHistory.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET CONTRACT HISTORY OF PRO
    public ACRGBWSResult GetPROContract(final DataSource dataSource, final String procode, final String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ArrayList<Contract> contractList = new ArrayList<>();
        try {
            ACRGBWSResult GetContract = fm.GETALLCONTRACT(dataSource, tags.toUpperCase().trim(), procode.trim());
            if (GetContract.isSuccess()) {
                List<Contract> mapContractList = Arrays.asList(utility.ObjectMapper().readValue(GetContract.getResult(), Contract[].class));
                for (int y = 0; y < mapContractList.size(); y++) {
                    contractList.add(mapContractList.get(y));
                }
            }
            if (contractList.size() > 0) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(contractList));
            } else {
                result.setMessage("N/A");
            }

        } catch (IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(ContractHistory.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET CONTRACT HISTORY OF APEX FACILITY
    public ACRGBWSResult GetAllAPEXContract(final DataSource dataSource, final String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ArrayList<Contract> contractList = new ArrayList<>();
        try {
            ACRGBWSResult resultApex = m.GETAPEXFACILITY(dataSource);
            if (resultApex.isSuccess()) {
                List<HealthCareFacility> apexList = Arrays.asList(utility.ObjectMapper().readValue(resultApex.getResult(), HealthCareFacility[].class));
                for (int x = 0; x < apexList.size(); x++) {
                    ACRGBWSResult GetContract = fm.GETALLCONTRACT(dataSource, tags.toUpperCase().trim(), apexList.get(x).getHcfcode().trim());
                    if (GetContract.isSuccess()) {
                        List<Contract> mapContractList = Arrays.asList(utility.ObjectMapper().readValue(GetContract.getResult(), Contract[].class));
                        for (int y = 0; y < mapContractList.size(); y++) {
                            contractList.add(mapContractList.get(y));
                        }
                    }
                }
            }
            if (contractList.size() > 0) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(contractList));
            } else {
                result.setMessage("N/A");
            }
        } catch (IOException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(ContractHistory.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET ALL CONTRACT HISTORY OF APEX FACILITY
    public ACRGBWSResult GetAllHCPNContract(final DataSource dataSource, final String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ArrayList<Contract> contractList = new ArrayList<>();
        try {
            ACRGBWSResult mblist = fm.GetManagingBoard(dataSource, "ACTIVE");
            if (mblist.isSuccess()) {
                List<ManagingBoard> HCPNList = Arrays.asList(utility.ObjectMapper().readValue(mblist.getResult(), ManagingBoard[].class));
                for (int x = 0; x < HCPNList.size(); x++) {
                    ACRGBWSResult GetContract = fm.GETALLCONTRACT(dataSource, tags.toUpperCase().trim(), HCPNList.get(x).getControlnumber().trim());
                    if (GetContract.isSuccess()) {
                        List<Contract> mapContractList = Arrays.asList(utility.ObjectMapper().readValue(GetContract.getResult(), Contract[].class));
                        for (int y = 0; y < mapContractList.size(); y++) {
                            contractList.add(mapContractList.get(y));
                        }
                    }
                }
            }
            if (contractList.size() > 0) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(contractList));
            } else {
                result.setMessage("N/A");
            }
        } catch (IOException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(ContractHistory.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

//     GET ALL CONTRACT HISTORY OF PRO
    public ACRGBWSResult GetAllPROContract(final DataSource dataSource, final String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKG.ACR_PRO(); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            ArrayList<Contract> contractList = new ArrayList<>();
            while (resultset.next()) {
                String procode = "2024" + resultset.getString("PROCODE").trim();
                ACRGBWSResult GetProContract = fm.GETALLCONTRACT(dataSource, tags.trim().toUpperCase(), procode.trim());
                if (GetProContract.isSuccess()) {
                    List<Contract> mapContractList = Arrays.asList(utility.ObjectMapper().readValue(GetProContract.getResult(), Contract[].class));
                    for (int y = 0; y < mapContractList.size(); y++) {
                        contractList.add(mapContractList.get(y));
                    }
                }
            }
            if (contractList.size() > 0) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(contractList));
            } else {
                result.setMessage("N/A");
            }

        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(ContractHistory.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET CONTRACT HISTORY OF FACILITY
    public ACRGBWSResult GetHCPNContractUnderPRO(final DataSource dataSource, final String procode, final String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Methods methods = new Methods();
        ArrayList<Contract> contractList = new ArrayList<>();
        try {
            ACRGBWSResult getAllHCPNCode = methods.GETROLEMULITPLE(dataSource, procode.trim().toUpperCase(), tags.trim().toUpperCase());
            if (getAllHCPNCode.isSuccess()) {
                List<String> hcpnCodeList = Arrays.asList(getAllHCPNCode.getResult().split(","));
                for (int x = 0; x < hcpnCodeList.size(); x++) {
                    ACRGBWSResult GetContract = fm.GETALLCONTRACT(dataSource, tags.toUpperCase().trim(), hcpnCodeList.get(x).trim());
                    if (GetContract.isSuccess()) {
                        List<Contract> mapContractList = Arrays.asList(utility.ObjectMapper().readValue(GetContract.getResult(), Contract[].class));
                        for (int y = 0; y < mapContractList.size(); y++) {
                            contractList.add(mapContractList.get(y));
                        }
                    }
                }
            }
            if (contractList.size() > 0) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(contractList));
            } else {
                result.setMessage("N/A");
            }
        } catch (IOException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(ContractHistory.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult GetHCIContractUnderHCPN(final DataSource dataSource, final String hcpncode, final String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Methods methods = new Methods();
        ArrayList<Contract> contractList = new ArrayList<>();
        try {
            ACRGBWSResult getAllHCPNCode = methods.GETROLEMULITPLE(dataSource, hcpncode.trim().toUpperCase(), tags.trim().toUpperCase());
            if (getAllHCPNCode.isSuccess()) {
                List<String> hcpnCodeList = Arrays.asList(getAllHCPNCode.getResult().split(","));
                for (int x = 0; x < hcpnCodeList.size(); x++) {
                    ACRGBWSResult GetContract = fm.GETALLCONTRACT(dataSource, tags.toUpperCase().trim(), hcpnCodeList.get(x).trim());
                    if (GetContract.isSuccess()) {
                        List<Contract> mapContractList = Arrays.asList(utility.ObjectMapper().readValue(GetContract.getResult(), Contract[].class));
                        for (int y = 0; y < mapContractList.size(); y++) {
                            contractList.add(mapContractList.get(y));
                        }
                        break;
                    }
                }
            }
            if (contractList.size() > 0) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(contractList));
            } else {
                result.setMessage("N/A");
            }
        } catch (IOException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(ContractHistory.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET CONTRACT HISTORY OF FACILITY
    public ACRGBWSResult GetHCIContractUnderPROUsingHCPNCODE(final DataSource dataSource, final String procode, final String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Methods methods = new Methods();
        ArrayList<Contract> contractList = new ArrayList<>();
        try {
            //GET ALL HCPN CODE UNDER PRO
            ACRGBWSResult getAllHCPNCode = methods.GETROLEMULITPLE(dataSource, procode.trim().toUpperCase(), tags.trim().toUpperCase());
            if (getAllHCPNCode.isSuccess()) {
                List<String> hcpnCodeList = Arrays.asList(getAllHCPNCode.getResult().split(","));
                //----------------------------------
                for (int x = 0; x < hcpnCodeList.size(); x++) {
                    //GET ALL HCI CODE USING HCPN CODE
                    ACRGBWSResult getAllHCICode = methods.GETROLEMULITPLE(dataSource, hcpnCodeList.get(x).trim().toUpperCase(), tags.trim().toUpperCase());
                    if (getAllHCICode.isSuccess()) {
                        List<String> hciCodeList = Arrays.asList(getAllHCICode.getResult().split(","));
                        //----------------------------------------
                        for (int w = 0; w < hciCodeList.size(); w++) {
                            ACRGBWSResult GetContract = fm.GETALLCONTRACT(dataSource, tags.toUpperCase().trim(), hciCodeList.get(w).trim());
                            if (GetContract.isSuccess()) {
                                List<Contract> mapContractList = Arrays.asList(utility.ObjectMapper().readValue(GetContract.getResult(), Contract[].class));
                                for (int y = 0; y < mapContractList.size(); y++) {
                                    contractList.add(mapContractList.get(y));
                                }
                            }
                        }
                        //------------------------------------------------
                    }
                }
                //-----------------------------------
            }
            if (contractList.size() > 0) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(contractList));
            } else {
                result.setMessage("N/A");
            }
        } catch (IOException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(ContractHistory.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult GetAPEXContract(final DataSource dataSource, final String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Methods methods = new Methods();
        ArrayList<Contract> contractList = new ArrayList<>();
        try {
            ACRGBWSResult GetApex = methods.GETAPEXFACILITY(dataSource);
            if (GetApex.isSuccess()) {
                List<HealthCareFacility> hciList = Arrays.asList(utility.ObjectMapper().readValue(GetApex.getResult(), HealthCareFacility[].class));
                for (int h = 0; h < hciList.size(); h++) {
                    ACRGBWSResult GetContract = fm.GETALLCONTRACT(dataSource, tags.toUpperCase().trim(), hciList.get(h).getHcfcode().trim());
                    if (GetContract.isSuccess()) {
                        List<Contract> mapContractList = Arrays.asList(utility.ObjectMapper().readValue(GetContract.getResult(), Contract[].class));
                        for (int y = 0; y < mapContractList.size(); y++) {
                            contractList.add(mapContractList.get(y));
                        }
                    }
                }
            }
            if (contractList.size() > 0) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(contractList));
            } else {
                result.setMessage("N/A");
            }

        } catch (IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(ContractHistory.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(ContractHistory.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
