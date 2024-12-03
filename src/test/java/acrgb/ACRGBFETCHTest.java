/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb;

/**
 *
 * @author MinoSun
 */
public class ACRGBFETCHTest {

    public ACRGBFETCHTest() {
    }
    private final String token = "eyJhbGciOiJIUzI1NiJ9.eyJDb2RlMSI6IkNRTmpQZmNucW5WQ3BrRVp6Q2RhZmYvVFVtbzU5YTc5cFlQeFhJU1ljc1UiLCJDb2RlMiI6InEvclJJaUVYMk1pdGloUlRERDBpa2ciLCJleHAiOjE3MjMwOTczMTl9.MhT197uMhY5g-oN1X6tGibWNQR0nU0SS16eZ1nWabek";
//

//    @Before
//    public void setUp() {
//        RestAssured.basePath = "http://localhost:7001/ACRGB/ACRGBFETCH";
//    }
//    @Test
//    public void testGetAssets() throws IOException {
//        ObjectMapper mapper = new ObjectMapper();
//////        expect().statusCode(200).contentType(ContentType.JSON).when()
//////                .get("/AcquaGETRequest/GetAcqua/NATIONALITYALL/ACTIVE/00");
//////        TEST FOR GETT ALL NATIONALITY
//////        if (given().headers("token", token).when().get("/GetAssets/ACTIVE/525").getStatusCode() != 200) {
//////            System.out.println("Response Code : " + given().headers("token", token).when().get("/GetAssets/ACTIVE/525").getStatusCode());
//////        } else {
//////            ACRGBWSResult getAssets = mapper.readValue(given().headers("token", token).when().get("/GetAssets/ACTIVE/525").getBody().asString(), ACRGBWSResult.class);
//////            System.out.println(mapper.writeValueAsString(getAssets));
//////        }
////
//////        String recode = "300806";
//////        String conid = "57";
//////        String tags = "FACILITY";
//////        String reqtype = "00";
//////        ACRGBWSResult getAssets = mapper.readValue(given().headers("token", token).when().get("/PerContractLedger/300806/57/FACILITY/INACTIVE").getBody().asString(), ACRGBWSResult.class);
//        ACRGBWSResult getAssets = mapper.readValue(given().headers("token", token).when().get("/GetCaptchaCode").getBody().asString(), ACRGBWSResult.class);
//        if (getAssets.isSuccess()) {
//            System.out.println("Captcha Code Result " + getAssets.getResult());
//        } else {
//            System.out.println(getAssets.getMessage());
//        }
//
//    }
//    @BeforeClass
//    public static void setUpClass() {
//    }
//
//    @AfterClass
//    public static void tearDownClass() {
//    }
//
//    @Before
//    public void setUp() {
//    }
//
//    @After
//    public void tearDown() {
//    }
    /**
     * Test of GetAssets method, of class ACRGBFETCH.
     */
//    @Test
//    public void testGetAssets() {
//        System.out.println("GetAssets");
//        String token = "";
//        String tags = "";
//        String phcfid = "";
//        ACRGBFETCH instance = new ACRGBFETCH();
//        ACRGBWSResult expResult = null;
//        ACRGBWSResult result = instance.GetAssets(token, tags, phcfid);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of GetContract method, of class ACRGBFETCH.
//     */
//    @Test
//    public void testGetContract() {
//        System.out.println("GetContract");
//        String token = "";
//        String tags = "";
//        String puserid = "";
//        String level = "";
//        ACRGBFETCH instance = new ACRGBFETCH();
//        ACRGBWSResult expResult = null;
//        ACRGBWSResult result = instance.GetContract(token, tags, puserid, level);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of GetTranch method, of class ACRGBFETCH.
//     */
//    @Test
//    public void testGetTranch() {
//        System.out.println("GetTranch");
//        String token = "";
//        String tags = "";
//        ACRGBFETCH instance = new ACRGBFETCH();
//        ACRGBWSResult expResult = null;
//        ACRGBWSResult result = instance.GetTranch(token, tags);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of GetUserInfo method, of class ACRGBFETCH.
//     */
//    @Test
//    public void testGetUserInfo() {
//        System.out.println("GetUserInfo");
//        String token = "";
//        String tags = "";
//        ACRGBFETCH instance = new ACRGBFETCH();
//        ACRGBWSResult expResult = null;
//        ACRGBWSResult result = instance.GetUserInfo(token, tags);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    /**
     * Test of GetUserLevel method, of class ACRGBFETCH.
     */
//    @Test
//    public void testGetUserLevel() {
//        System.out.println("GetUserLevel");
//        String token = "asdasdasdasdasd";
//        String tags = "";
//        ACRGBFETCH instance = new ACRGBFETCH();
//        ACRGBWSResult expResult = null;
//        ACRGBWSResult result = instance.GetUserLevel(token, "ACTIVE");
//       // assertEquals(expResult, result);
//        System.out.println(result.toString());
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    /**
     * Test of GetUser method, of class ACRGBFETCH.
     */
//    @Test
//    public void testGetUser() {
//        System.out.println("GetUser");
//        String token = "";
//        String tags = "";
//        ACRGBFETCH instance = new ACRGBFETCH();
//        ACRGBWSResult expResult = null;
//        ACRGBWSResult result = instance.GetUser(token, tags);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of GetPro method, of class ACRGBFETCH.
//     */
//    @Test
//    public void testGetPro() {
//        System.out.println("GetPro");
//        String token = "";
//        String tags = "";
//        ACRGBFETCH instance = new ACRGBFETCH();
//        ACRGBWSResult expResult = null;
//        ACRGBWSResult result = instance.GetPro(token, tags);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of GetRoleIndex method, of class ACRGBFETCH.
//     */
//    @Test
//    public void testGetRoleIndex() {
//        System.out.println("GetRoleIndex");
//        String token = "";
//        String puserid = "";
//        ACRGBFETCH instance = new ACRGBFETCH();
//        ACRGBWSResult expResult = null;
//        ACRGBWSResult result = instance.GetRoleIndex(token, puserid);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of GetSummary method, of class ACRGBFETCH.
//     */
//    @Test
//    public void testGetSummary() {
//        System.out.println("GetSummary");
//        String token = "";
//        String datefrom = "";
//        String dateto = "";
//        String tags = "";
//        String userid = "";
//        String type = "";
//        String hcilist = "";
//        ACRGBFETCH instance = new ACRGBFETCH();
//        ACRGBWSResult expResult = null;
//        ACRGBWSResult result = instance.GetSummary(token, datefrom, dateto, tags, userid, type, hcilist);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of GetLevel method, of class ACRGBFETCH.
//     */
//    @Test
//    public void testGetLevel() {
//        System.out.println("GetLevel");
//        String token = "";
//        String levid = "";
//        ACRGBFETCH instance = new ACRGBFETCH();
//        ACRGBWSResult expResult = null;
//        ACRGBWSResult result = instance.GetLevel(token, levid);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of GETFULLDETAILS method, of class ACRGBFETCH.
//     */
//    @Test
//    public void testGETFULLDETAILS() {
//        System.out.println("GETFULLDETAILS");
//        String token = "";
//        String userid = "";
//        ACRGBFETCH instance = new ACRGBFETCH();
//        ACRGBWSResult expResult = null;
//        ACRGBWSResult result = instance.GETFULLDETAILS(token, userid);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of GETASSETSWITHPARAM method, of class ACRGBFETCH.
//     */
//    @Test
//    public void testGETASSETSWITHPARAM() {
//        System.out.println("GETASSETSWITHPARAM");
//        String token = "";
//        String phcfid = "";
//        String conid = "";
//        ACRGBFETCH instance = new ACRGBFETCH();
//        ACRGBWSResult expResult = null;
//        ACRGBWSResult result = instance.GETASSETSWITHPARAM(token, phcfid, conid);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of GetActivityLogs method, of class ACRGBFETCH.
//     */
//    @Test
//    public void testGetActivityLogs() {
//        System.out.println("GetActivityLogs");
//        String token = "";
//        ACRGBFETCH instance = new ACRGBFETCH();
//        ACRGBWSResult expResult = null;
//        ACRGBWSResult result = instance.GetActivityLogs(token);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of GetLogWithParam method, of class ACRGBFETCH.
//     */
//    @Test
//    public void testGetLogWithParam() {
//        System.out.println("GetLogWithParam");
//        String token = "";
//        String userid = "";
//        ACRGBFETCH instance = new ACRGBFETCH();
//        ACRGBWSResult expResult = null;
//        ACRGBWSResult result = instance.GetLogWithParam(token, userid);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of GetMBRequest method, of class ACRGBFETCH.
//     */
//    @Test
//    public void testGetMBRequest() {
//        System.out.println("GetMBRequest");
//        String token = "";
//        String userid = "";
//        ACRGBFETCH instance = new ACRGBFETCH();
//        ACRGBWSResult expResult = null;
//        ACRGBWSResult result = instance.GetMBRequest(token, userid);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of GetManagingBoard method, of class ACRGBFETCH.
//     */
//    @Test
//    public void testGetManagingBoard() throws Exception {
//        System.out.println("GetManagingBoard");
//        String token = "";
//        String tags = "";
//        ACRGBFETCH instance = new ACRGBFETCH();
//        ACRGBWSResult expResult = null;
//        ACRGBWSResult result = instance.GetManagingBoard(token, tags);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of GetFacilityUsingProAccountUserID method, of class ACRGBFETCH.
//     */
//    @Test
//    public void testGetFacilityUsingProAccountUserID() throws Exception {
//        System.out.println("GetFacilityUsingProAccountUserID");
//        String token = "";
//        String pid = "";
//        ACRGBFETCH instance = new ACRGBFETCH();
//        ACRGBWSResult expResult = null;
//        ACRGBWSResult result = instance.GetFacilityUsingProAccountUserID(token, pid);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of GETALLFACILITY method, of class ACRGBFETCH.
//     */
//    @Test
//    public void testGETALLFACILITY() {
//        System.out.println("GETALLFACILITY");
//        String token = "";
//        String tags = "";
//        String userid = "";
//        ACRGBFETCH instance = new ACRGBFETCH();
//        ACRGBWSResult expResult = null;
//        ACRGBWSResult result = instance.GETALLFACILITY(token, tags, userid);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of GetManagingBoardWithProID method, of class ACRGBFETCH.
//     */
//    @Test
//    public void testGetManagingBoardWithProID() throws Exception {
//        System.out.println("GetManagingBoardWithProID");
//        String token = "";
//        String proid = "";
//        String levelname = "";
//        ACRGBFETCH instance = new ACRGBFETCH();
//        ACRGBWSResult expResult = null;
//        ACRGBWSResult result = instance.GetManagingBoardWithProID(token, proid, levelname);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of GetMBUsingMBID method, of class ACRGBFETCH.
//     */
//    @Test
//    public void testGetMBUsingMBID() throws Exception {
//        System.out.println("GetMBUsingMBID");
//        String token = "";
//        String pid = "";
//        ACRGBFETCH instance = new ACRGBFETCH();
//        ACRGBWSResult expResult = null;
//        ACRGBWSResult result = instance.GetMBUsingMBID(token, pid);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of GetBalanceTerminatedContract method, of class ACRGBFETCH.
//     */
//    @Test
//    public void testGetBalanceTerminatedContract() throws Exception {
//        System.out.println("GetBalanceTerminatedContract");
//        String token = "";
//        String userid = "";
//        String levelname = "";
//        String tags = "";
//        ACRGBFETCH instance = new ACRGBFETCH();
//        ACRGBWSResult expResult = null;
//        ACRGBWSResult result = instance.GetBalanceTerminatedContract(token, userid, levelname, tags);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of PerContractLedger method, of class ACRGBFETCH.
//     */
//    @Test
//    public void testPerContractLedger() {
//        System.out.println("PerContractLedger");
//        String token = "eyJhbGciOiJIUzI1NiJ9.eyJDb2RlMSI6IklkZ29PZUI5YXM3UGxyd2RJNXY1V1EiLCJDb2RlMiI6InZReVc2QjVFVHJNU1dhM2FhZjBWZlEiLCJleHAiOjE3MjA1MDQ1MTd9.NTerlmh4rQV1HKeg7tUepucLCxvSj99jSOipnVIz_SY";
//        String type = "ACTIVE";
//        String hcpncode = "330061";
//        String contract = "288";
//        String tags = "FACILITY";
//        ACRGBFETCH instance = new ACRGBFETCH();
//        //  ACRGBWSResult expResult = null;
//        ACRGBWSResult result = instance.PerContractLedger(token, type, hcpncode, contract, tags);
//        System.out.println(result);
//        //   assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        // fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of GetContractDate method, of class ACRGBFETCH.
//     */
//    @Test
//    public void testGetContractDate() {
//        System.out.println("GetContractDate");
//        String token = "";
//        String tags = "";
//        ACRGBFETCH instance = new ACRGBFETCH();
//        ACRGBWSResult expResult = null;
//        ACRGBWSResult result = instance.GetContractDate(token, tags);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of AutoEndContractDate method, of class ACRGBFETCH.
//     */
//    @Test
//    public void testAutoEndContractDate() throws Exception {
//        System.out.println("AutoEndContractDate");
//        String token = "";
//        String ucondateid = "";
//        ACRGBFETCH instance = new ACRGBFETCH();
//        ACRGBWSResult expResult = null;
//        ACRGBWSResult result = instance.AutoEndContractDate(token, ucondateid);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of GetRandomPasscode method, of class ACRGBFETCH.
//     */
//    @Test
//    public void testGetRandomPasscode() {
//        System.out.println("GetRandomPasscode");
//        String token = "";
//        ACRGBFETCH instance = new ACRGBFETCH();
//        ACRGBWSResult expResult = null;
//        ACRGBWSResult result = instance.GetRandomPasscode(token);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of GetPreviousContract method, of class ACRGBFETCH.
//     */
//    @Test
//    public void testGetPreviousContract() {
//        System.out.println("GetPreviousContract");
//        String token = "";
//        String paccount = "";
//        String contractid = "";
//        ACRGBFETCH instance = new ACRGBFETCH();
//        ACRGBWSResult expResult = null;
//        ACRGBWSResult result = instance.GetPreviousContract(token, paccount, contractid);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of GetClaims method, of class ACRGBFETCH.
//     */
//    @Test
//    public void testGetClaims() {
//        System.out.println("GetClaims");
//        String token = "";
//        String hcpncode = "";
//        String contractid = "";
//        String tags = "";
//        ACRGBFETCH instance = new ACRGBFETCH();
//        ACRGBWSResult expResult = null;
//        ACRGBWSResult result = instance.GetClaims(token, hcpncode, contractid, tags);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of GetEmailCredentials method, of class ACRGBFETCH.
//     */
//    @Test
//    public void testGetEmailCredentials() {
//        System.out.println("GetEmailCredentials");
//        String token = "";
//        ACRGBFETCH instance = new ACRGBFETCH();
//        ACRGBWSResult expResult = null;
//        ACRGBWSResult result = instance.GetEmailCredentials(token);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of GETOLDPASSCODE method, of class ACRGBFETCH.
//     */
//    @Test
//    public void testGETOLDPASSCODE() {
//        System.out.println("GETOLDPASSCODE");
//        String token = "";
//        String puserid = "";
//        ACRGBFETCH instance = new ACRGBFETCH();
//        ACRGBWSResult expResult = null;
//        ACRGBWSResult result = instance.GETOLDPASSCODE(token, puserid);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of CONTRACTWITHQUARTER method, of class ACRGBFETCH.
//     */
//    @Test
//    public void testCONTRACTWITHQUARTER() {
//        System.out.println("CONTRACTWITHQUARTER");
//        String token = "";
//        String tags = "";
//        String uprocode = "";
//        ACRGBFETCH instance = new ACRGBFETCH();
//        ACRGBWSResult expResult = null;
//        ACRGBWSResult result = instance.CONTRACTWITHQUARTER(token, tags, uprocode);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of GETACR_BOOKING method, of class ACRGBFETCH.
//     */
//    @Test
//    public void testGETACR_BOOKING() {
//        System.out.println("GETACR_BOOKING");
//        String token = "";
//        ACRGBFETCH instance = new ACRGBFETCH();
//        ACRGBWSResult expResult = null;
//        ACRGBWSResult result = instance.GETACR_BOOKING(token);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of PostEmailCredentials method, of class ACRGBFETCH.
//     */
//    @Test
//    public void testPostEmailCredentials() {
//        System.out.println("PostEmailCredentials");
//        String token = "";
//        ACRGBFETCH instance = new ACRGBFETCH();
//        ACRGBWSResult expResult = null;
//        ACRGBWSResult result = instance.PostEmailCredentials(token);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of GetllContract method, of class ACRGBFETCH.
//     */
//    @Test
//    public void testGetllContract() {
//        System.out.println("GetllContract");
//        String token = "";
//        String tags = "";
//        ACRGBFETCH instance = new ACRGBFETCH();
//        ACRGBWSResult expResult = null;
//        ACRGBWSResult result = instance.GetllContract(token, tags);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of GetContractHistory method, of class ACRGBFETCH.
//     */
//    @Test
//    public void testGetContractHistory() throws Exception {
//        System.out.println("GetContractHistory");
//        String token = "";
//        String puserid = "";
//        String accountlevel = "";
//        ACRGBFETCH instance = new ACRGBFETCH();
//        ACRGBWSResult expResult = null;
//        ACRGBWSResult result = instance.GetContractHistory(token, puserid, accountlevel);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}
