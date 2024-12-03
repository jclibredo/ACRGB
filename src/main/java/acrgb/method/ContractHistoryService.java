/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.method;

import acrgb.structure.ACRGBWSResult;
import acrgb.utility.Utility;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;

/**
 *
 * @author ACR_GB
 */
@RequestScoped
public class ContractHistoryService {

    public ContractHistoryService() {
    }

    public ACRGBWSResult GetHistoryResult(
            final DataSource dataSource,
            final String userId,
            final String tags,
            final String requestCode,
            final String targetData) {
        ACRGBWSResult result = new Utility().ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ACRGBWSResult GetRole = new Methods().GETROLE(dataSource, userId.trim(), "ACTIVE".trim()); // RETRIEVING CODE CONTROLLER
        switch (targetData.trim().toUpperCase()) {
            case "ALLPRO": { // THIS PART IS FOR PHIC CENTRAL LEVEL
                ACRGBWSResult getAllPro = new ContractHistory().GetAllPROContract(dataSource, tags.trim().toUpperCase());
                result.setMessage(getAllPro.getMessage());
                result.setResult(getAllPro.getResult());
                result.setSuccess(getAllPro.isSuccess());
                break;
            }
            case "ALLHCPN": {// THIS PART IS FOR PHIC CENTRAL LEVEL
                ACRGBWSResult getAllHcpn = new ContractHistory().GetAllHCPNContract(dataSource, tags.trim().toUpperCase());
                result.setMessage(getAllHcpn.getMessage());
                result.setResult(getAllHcpn.getResult());
                result.setSuccess(getAllHcpn.isSuccess());
                break;
            }
            case "ALLFACILITY": {// THIS PART IS FOR PHIC CENTRAL LEVEL
                ACRGBWSResult getAllHci = new ContractHistory().GetAllAPEXContract(dataSource, tags.trim().toUpperCase());
                result.setMessage(getAllHci.getMessage());
                result.setResult(getAllHci.getResult());
                result.setSuccess(getAllHci.isSuccess());
                break;
            }
            case "PRO": {// GET CONTRACT USING PROCODE
                ACRGBWSResult getPro = new ContractHistory().GetPROContract(dataSource, requestCode, tags.trim().toUpperCase());
                result.setMessage(getPro.getMessage());
                result.setResult(getPro.getResult());
                result.setSuccess(getPro.isSuccess());
                break;
            }
            case "HCPN": {//GET CONTRACT USING HCPN CODE
                ACRGBWSResult getHcpn = new ContractHistory().GetHCPNContract(dataSource, requestCode, tags.trim().toUpperCase());
                result.setMessage(getHcpn.getMessage());
                result.setResult(getHcpn.getResult());
                result.setSuccess(getHcpn.isSuccess());
                break;
            }
            case "HCPNUNDERPRO": {//GET CONTRACT USING HCPN CODE
                ACRGBWSResult getHcpnuPro = new ContractHistory().GetHCPNContractUnderPRO(dataSource, GetRole.getResult().trim(), tags.trim().toUpperCase());
                result.setMessage(getHcpnuPro.getMessage());
                result.setResult(getHcpnuPro.getResult());
                result.setSuccess(getHcpnuPro.isSuccess());
                break;
            }
            case "HCIUNDERHCPN": {//GET CONTRACT USING HCPN CODE
                ACRGBWSResult getHcinuHcpn = new ContractHistory().GetHCIContractUnderHCPN(dataSource, GetRole.getResult().trim(), tags.trim().toUpperCase());
                result.setMessage(getHcinuHcpn.getMessage());
                result.setResult(getHcinuHcpn.getResult());
                result.setSuccess(getHcinuHcpn.isSuccess());
                break;
            }
            case "HCIUNDERPROUSINGHCPNCODE": {//GET CONTRACT USING HCPN CODE
                ACRGBWSResult getHcinuProunderHcpn = new ContractHistory().GetHCIContractUnderPROUsingHCPNCODE(dataSource, GetRole.getResult().trim(), tags.trim().toUpperCase());
                result.setMessage(getHcinuProunderHcpn.getMessage());
                result.setResult(getHcinuProunderHcpn.getResult());
                result.setSuccess(getHcinuProunderHcpn.isSuccess());
                break;
            }

            case "FACILITY": {//GET PHIC USING HOSPITAL CODE
                ACRGBWSResult getHci = new ContractHistory().GetHCIContract(dataSource, requestCode, tags.trim().toUpperCase());
                result.setMessage(getHci.getMessage());
                result.setResult(getHci.getResult());
                result.setSuccess(getHci.isSuccess());
                break;
            }
            case "OWNPRO": {//GET PRO CONTRACT USING ACCOUNT USERID
                ACRGBWSResult getOwnPro = new ContractHistory().GetPROContract(dataSource, GetRole.getResult().trim(), tags.trim().toUpperCase());
                result.setMessage(getOwnPro.getMessage());
                result.setResult(getOwnPro.getResult());
                result.setSuccess(getOwnPro.isSuccess());
                break;
            }
            case "OWNHCPN": {//GET HCPN CONTRACT USING ACCOUNT USERID
                ACRGBWSResult getOwnHcpn = new ContractHistory().GetHCPNContract(dataSource, GetRole.getResult().trim(), tags.trim().toUpperCase());
                result.setMessage(getOwnHcpn.getMessage());
                result.setResult(getOwnHcpn.getResult());
                result.setSuccess(getOwnHcpn.isSuccess());
                break;
            }
            case "APEX": {//GET HCPN CONTRACT USING ACCOUNT USERID
                ACRGBWSResult getOwnHcpn = new ContractHistory().GetAPEXContract(dataSource, tags.trim().toUpperCase());
                result.setMessage(getOwnHcpn.getMessage());
                result.setResult(getOwnHcpn.getResult());
                result.setSuccess(getOwnHcpn.isSuccess());
                break;
            }
            case "OWNFACILITY": {//GET HCI CONTRACT USING ACCOUNT USERID
                ACRGBWSResult getOwnHci = new ContractHistory().GetHCIContract(dataSource, GetRole.getResult().trim(), tags.trim().toUpperCase());
                result.setMessage(getOwnHci.getMessage());
                result.setResult(getOwnHci.getResult());
                result.setSuccess(getOwnHci.isSuccess());
                break;
            }
            default: {
                result.setMessage(targetData + "TARGET DATA NOT FOUND");
                break;
            }
        }
        return result;

    }

}
