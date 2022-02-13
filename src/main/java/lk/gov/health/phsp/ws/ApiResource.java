/*
 * The MIT License
 *
 * Copyright 2021 buddhika.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package lk.gov.health.phsp.ws;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import javax.enterprise.context.Dependent;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import lk.gov.health.phsp.bean.AnalysisController;

import lk.gov.health.phsp.bean.ApplicationController;
import lk.gov.health.phsp.bean.AreaApplicationController;

import lk.gov.health.phsp.bean.CommonController;
import lk.gov.health.phsp.bean.EncounterApplicationController;
import lk.gov.health.phsp.bean.InstitutionApplicationController;
import lk.gov.health.phsp.bean.ItemApplicationController;
import lk.gov.health.phsp.bean.SessionController;

import lk.gov.health.phsp.bean.WebUserApplicationController;
import lk.gov.health.phsp.bean.WebUserController;
import lk.gov.health.phsp.entity.Area;

import lk.gov.health.phsp.entity.Document;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Item;

import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.enums.AreaType;
import lk.gov.health.phsp.enums.DocumentType;
import lk.gov.health.phsp.enums.InstitutionType;

import org.json.JSONArray;
import org.json.JSONObject;

import io.fusionauth.jwt.Signer;
import io.fusionauth.jwt.Verifier;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.hmac.HMACSigner;
import io.fusionauth.jwt.hmac.HMACVerifier;

/**
 * REST Web Service
 *
 * @author buddhika
 */
@Path("")
@Dependent
public class ApiResource {

    @Context
    private UriInfo context;

    @Inject
    AreaApplicationController areaApplicationController;
    @Inject
    InstitutionApplicationController institutionApplicationController;
    @Inject
    AnalysisController analysisController;
    @Inject
    ItemApplicationController itemApplicationController;
    @Inject
    ApplicationController applicationController;
   
    @Inject
    WebUserController webUserController;
   
    @Inject
    WebUserApplicationController webUserApplicationController;
  
    @Inject
    EncounterApplicationController encounterApplicationController;
    @Inject
    SessionController sessionController;

    /**
     * Creates a new instance of GenericResource
     */
    public ApiResource() {
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getJson(@QueryParam("name") String name,
            @QueryParam("year") String year,
            @QueryParam("month") String month,
            @QueryParam("institute_id") String instituteId,
            @QueryParam("id") String id,
            @QueryParam("api_key") String jwt,
            @QueryParam("username") String username,
            @QueryParam("password") String password,
            @QueryParam("test_number") String test_number,
            @QueryParam("referring_lab_id") String referring_lab_id,
            @QueryParam("client_name") String client_name,
            @QueryParam("client_address") String client_address,
            @QueryParam("client_phone_number") String client_phone_number,
            @QueryParam("client_nic") String client_nic,
            @QueryParam("client_passport_number") String client_passport_number,
            @QueryParam("client_age_in_years") String client_age_in_years,
            @QueryParam("client_age_in_months") String client_age_in_months,
            @QueryParam("client_age_in_days") String client_age_in_days,
            @QueryParam("client_gender") String client_gender,
            @QueryParam("client_citizenship") String client_citizenship,
            @QueryParam("ordering_category_id") String ordering_category_id,
            @QueryParam("moh_id") String moh_id,
            @QueryParam("district_id") String district_id,
            @QueryParam("gn_area_id") String gn_area_id,
            @QueryParam("phi_area_id") String phi_area_id,
            @QueryParam("comments") String comments,
            @QueryParam("request_id") String request_id,
            @Context HttpServletRequest requestContext,
            @Context SecurityContext context) {

        String ipadd = requestContext.getHeader("X-FORWARDED-FOR");
        if (ipadd == null) {
            ipadd = requestContext.getRemoteAddr();
        }

        System.out.println("ipadd = " + ipadd);
        System.out.println("name = " + name);
        String requestIp = "";
        if(ipadd!=null){
            requestIp = ipadd.trim().toLowerCase();
        }else{
            requestIp = "IP Address is NULL";
        }
        System.out.println("requestIp = " + requestIp);

        JSONObject jSONObjectOut;
        if (name == null || name.trim().equals("")) {
            jSONObjectOut = errorMessageInstruction();
        } else {
            switch (name) {
                case "get_lab_list":
                    jSONObjectOut = labList();
                    break;
                case "get_moh_list":
                    jSONObjectOut = mohList();
                    break;
                case "get_ordering_category_list":
                    jSONObjectOut = orderingCategoryList();
                    break;
                case "get_province_list":
                    jSONObjectOut = provinceList();
                    break;
                case "get_district_list":
                    jSONObjectOut = districtList();
                    break;
                case "get_institutes_list":
                case "get_institute_list":
                    jSONObjectOut = instituteList();
                    break;
                case "get_gender_list":
                    jSONObjectOut = genderList();
                    break;
                case "get_citizenship_list":
                    jSONObjectOut = citizenshipList();
                    break;
                case "get_gn_area_list":
                    jSONObjectOut = gnAreaList();
                    break;
                case "get_institutes_registered_list":
                    jSONObjectOut = phiAreaList();
                    break;
                case "get_vaccination_statuses":
                    jSONObjectOut = vaccinationStatusesList();
                    break;
                case "get_symptomatic_statuses":
                    jSONObjectOut = symptomaticStatusesList();
                    break;
                case "submit_pcr_request":
                    
                    break;
                case "request_pcr_result":
                    jSONObjectOut = requestPcrResult(ipadd,
                            username,
                            password,
                            request_id);
                    break;
                case "submit_pcr_result":
                    jSONObjectOut = requestPcrResult(ipadd,
                            username,
                            password,
                            test_number);
                    break;
                case "authenticate":
                    jSONObjectOut = authenticate(username, password);
                    break;
                case "submit_rat_result":
                    jSONObjectOut = submitRatResult();
                    break;

                default:
                    jSONObjectOut = errorMessage();
            }
        }

        String json = null ;
        return json;
    }

    private JSONObject authenticate(String username, String password) {
        System.out.println(username);
        System.out.println(password);

        if (username== null || username.trim().length() == 0) {
            return errorMessageLogin();
        }

        if (password == null || password.trim().length() == 0) {
            return errorMessageLogin();
        }

        WebUser wu = webUserApplicationController.getWebUser(username, password);

        if (wu == null) {
            return errorMessageLogin();
        }

        sessionController.setAppKey();
        UUID key = sessionController.getAppKey();
        Signer signer = HMACSigner.newSHA256Signer(key.toString());

        JWT jwt = new JWT();
        jwt.setIssuer("nchis.health.gov.lk");
        jwt.setIssuedAt(ZonedDateTime.now(ZoneOffset.UTC));
        jwt.setSubject(username);
        // Token is set to be valid for 60 minutes
        jwt.setExpiration(ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(60));

        String encodedJwt = JWT.getEncoder().encode(jwt, signer);

        JSONObject json = new JSONObject();
        json.put("api_key", encodedJwt);
        json.put("status", 200);
        json.put("message", "success");
        System.out.println(json.toString());
        return json;
    }

    private boolean authorize(String username, String jwt) {
        if (jwt == null) {
            return false;
        }

        if (username == null || username.trim().length() == 0) {
            return false;
        }

        if (sessionController.getAppKey() == null) {
            return false;
        }
        try {
            Verifier verifier = HMACVerifier.newVerifier(sessionController.getAppKey().toString());
            JWT decodeJwt = JWT.getDecoder().decode(jwt, verifier);

            if (username.equals(decodeJwt.subject)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

    }

   

    private JSONObject requestPcrResult(String ipadd,
            String username,
            String password,
            String request_id) {
        JSONObject jSONObjectOut = new JSONObject();
        JSONArray array = new JSONArray();

        WebUser wu;

        wu = webUserApplicationController.getWebUser(username, password);

        if (wu == null) {
            return errorMessageLogin();
        }

        if (wu.getLoginIPs() == null || wu.getLoginIPs().equals("")) {
            return errorMessageNoIps();
        }

        if (ipadd != null && !wu.getLoginIPs().contains(ipadd)) {
            return errorMessageNotAnAutherizedIp();
        }

        if (request_id == null || request_id.trim().equals("")) {
            return errorMessageNoPcrRequestId();
        }

        System.out.println("request_id = " + request_id);

        Long rid = CommonController.getLongValue(request_id);

        System.out.println("rid = " + rid);

        Document e = encounterApplicationController.getEncounter(rid);

        if (e == null) {
            return errorMessageNoSuchPcrRequestId();
        }
        


        if (!e.getInstitution().equals(wu.getInstitution())) {
            System.out.println("institution wrong");
            return errorMessageNoSuchPcrRequestId();
        }

        JSONObject ja = new JSONObject();

       

        jSONObjectOut.put("data", array);
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
    }

    private JSONObject submitPcrResult() {
        JSONObject jSONObjectOut = new JSONObject();
        JSONArray array = new JSONArray();

        jSONObjectOut.put("data", array);
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
    }

    private JSONObject submitRatResult() {
        JSONObject jSONObjectOut = new JSONObject();
        JSONArray array = new JSONArray();

        jSONObjectOut.put("data", array);
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
    }

    private JSONObject districtList() {
        JSONObject jSONObjectOut = new JSONObject();
        JSONArray array = new JSONArray();
        List<Area> ds = areaApplicationController.getAllAreas(AreaType.District);
        for (Area a : ds) {
            JSONObject ja = new JSONObject();
            ja.put("district_id", a.getId());
            ja.put("district_code", a.getCode());
            ja.put("district_name", a.getName());
            array.put(ja);
        }
        jSONObjectOut.put("data", array);
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
    }

    private JSONObject instituteList() {
        JSONObject jSONObjectOut = new JSONObject();
        JSONArray array = new JSONArray();
        List<Institution> ds = institutionApplicationController.getHospitals();
        for (Institution a : ds) {
            JSONObject ja = new JSONObject();
            ja.put("institute_id", a.getId());
            ja.put("institute_code", a.getCode());
            ja.put("name", a.getName());
            ja.put("hin", a.getPoiNumber());
           ja.put("address", a.getAddress());
            ja.put("type", a.getInstitutionType());
            ja.put("type_label", a.getInstitutionType().getLabel());
            if (a.getEditedAt() != null) {
                ja.put("edited_at", a.getEditedAt());
            } else {
                ja.put("edited_at", a.getCreatedAt());
            }
            if (a.getProvince() != null) {
                ja.put("province_id", a.getProvince().getId());
            }
            if (a.getDistrict() != null) {
                ja.put("district_id", a.getDistrict().getId());
            }
            ja.put("child_institutions", Get_Child_Institutions(a));
            array.put(ja);
        }
        jSONObjectOut.put("data", array);
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
    }

    private JSONObject genderList() {
        JSONObject jSONObjectOut = new JSONObject();
        JSONArray array = new JSONArray();
        List<Item> ds = itemApplicationController.getSexes();
        for (Item a : ds) {
            JSONObject ja = new JSONObject();
            ja.put("gender_id", a.getId());
            ja.put("gender_name", a.getCode());
            array.put(ja);
        }
        jSONObjectOut.put("data", array);
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
    }

    private JSONObject vaccinationStatusesList() {
        JSONObject jSONObjectOut = new JSONObject();
        JSONArray array = new JSONArray();
        List<Item> ds = itemApplicationController.getMinutes();
        for (Item a : ds) {
            JSONObject ja = new JSONObject();
            ja.put("vaccination_status_id", a.getId());
            ja.put("vaccination_status_name", a.getCode());
            array.put(ja);
        }
        jSONObjectOut.put("data", array);
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
    }

    private JSONObject symptomaticStatusesList() {
        JSONObject jSONObjectOut = new JSONObject();
        JSONArray array = new JSONArray();
        List<Item> ds = itemApplicationController.getSymptomaticStatuses();
        for (Item a : ds) {
            JSONObject ja = new JSONObject();
            ja.put("symptomatic_status_id", a.getId());
            ja.put("symptomatic_status_name", a.getCode());
            array.put(ja);
        }
        jSONObjectOut.put("data", array);
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
    }

    private JSONObject gnAreaList() {
        JSONObject jSONObjectOut = new JSONObject();
        JSONArray array = new JSONArray();
        List<Area> ds = areaApplicationController.getAllAreas(AreaType.GN);
        for (Area a : ds) {
            JSONObject ja = new JSONObject();
            ja.put("gn_id", a.getId());
            ja.put("gn_code", a.getCode());
            ja.put("gn_name", a.getCode());
            if (a.getDistrict() != null) {
                ja.put("district_name", a.getDistrict().getName());
                ja.put("district_id", a.getDistrict().getId());
            }
            if (a.getDsd() != null) {
                ja.put("dsd_name", a.getDsd().getName());
                ja.put("dsd_id", a.getDsd().getId());
            }
            array.put(ja);
        }
        jSONObjectOut.put("data", array);
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
    }

    private JSONObject phiAreaList() {
        JSONObject jSONObjectOut = new JSONObject();
        JSONArray array = new JSONArray();
        List<Area> ds = areaApplicationController.getAllAreas(AreaType.GN);
        for (Area a : ds) {
            JSONObject ja = new JSONObject();
            ja.put("phi_id", a.getId());
            ja.put("phi_name", a.getCode());
            if (a.getDistrict() != null) {
                ja.put("district_name", a.getDistrict().getName());
                ja.put("district_id", a.getDistrict().getId());
            }
            if (a.getMoh() != null) {
                ja.put("moh_name", a.getMoh().getName());
                ja.put("moh_id", a.getMoh().getId());
            }
            array.put(ja);
        }
        jSONObjectOut.put("data", array);
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
    }

    private JSONObject citizenshipList() {
        JSONObject jSONObjectOut = new JSONObject();
        JSONArray array = new JSONArray();
        List<Item> ds = itemApplicationController.getCitizenships();
        for (Item a : ds) {
            JSONObject ja = new JSONObject();
            ja.put("citizenship_id", a.getId());
            ja.put("citizenship_name", a.getName());
            array.put(ja);
        }
        jSONObjectOut.put("data", array);
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
    }

    private JSONObject provinceList() {
        JSONObject jSONObjectOut = new JSONObject();
        JSONArray array = new JSONArray();
        List<Area> ds = areaApplicationController.getAllAreas(AreaType.Province);
        for (Area a : ds) {
            JSONObject ja = new JSONObject();
            ja.put("province_id", a.getId());
            ja.put("province_code", a.getCode());
            ja.put("province_name", a.getName());
            array.put(ja);
        }
        jSONObjectOut.put("data", array);
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
    }

    private JSONObject labList() {
        JSONObject jSONObjectOut = new JSONObject();
        JSONArray array = new JSONArray();
        List<InstitutionType> its = new ArrayList<>();
        its.add(InstitutionType.Lab);
        List<Institution> ds = institutionApplicationController.findInstitutions(its);
        for (Institution a : ds) {
            JSONObject ja = new JSONObject();
            ja.put("lab_id", a.getId());
            ja.put("lab_name", a.getName());
            array.put(ja);
        }
        jSONObjectOut.put("data", array);
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
    }

    private JSONObject orderingCategoryList() {
        JSONObject jSONObjectOut = new JSONObject();
        JSONArray array = new JSONArray();
        List<Item> ds = itemApplicationController.getLetterReceiveCategories();
        for (Item a : ds) {
            JSONObject ja = new JSONObject();
            ja.put("ordering_category_id", a.getId());
            ja.put("ordering_category_name", a.getName());
            array.put(ja);
        }
        jSONObjectOut.put("data", array);
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
    }

    private JSONObject mohList() {
        JSONObject jSONObjectOut = new JSONObject();
        JSONArray array = new JSONArray();
        List<Area> ds = areaApplicationController.getAllAreas(AreaType.MOH);
        for (Area a : ds) {
            JSONObject ja = new JSONObject();
            ja.put("moh_id", a.getId());
            ja.put("moh_name", a.getName());
            if (a.getDistrict() != null) {
                ja.put("district_name", a.getDistrict().getName());
                ja.put("district_id", a.getDistrict().getName());
            }
            array.put(ja);
        }
        jSONObjectOut.put("data", array);
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
    }

    private JSONObject successMessage() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 200);
        jSONObjectOut.put("type", "success");
        return jSONObjectOut;
    }

    private JSONObject errorMessage() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 400);
        jSONObjectOut.put("type", "error");
        String e = "Parameter name is not recognized.";
        jSONObjectOut.put("message", "Parameter name is not recognized.");
        return jSONObjectOut;
    }

    private JSONObject errorMessageNoClientName() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 400);
        jSONObjectOut.put("type", "error");
        String e = "A client name is required.";
        jSONObjectOut.put("message", e);
        return jSONObjectOut;
    }

    private JSONObject errorMessageNoTestNumber() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 400);
        jSONObjectOut.put("type", "error");
        String e = "A test number (reference number or barcode) is required.";
        jSONObjectOut.put("message", e);
        return jSONObjectOut;
    }

    private JSONObject errorMessageNoPcrRequestId() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 400);
        jSONObjectOut.put("type", "error");
        String e = "PCR Request ID is required.";
        jSONObjectOut.put("message", e);
        return jSONObjectOut;
    }

    private JSONObject errorMessageNoSuchPcrRequestId() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 400);
        jSONObjectOut.put("type", "error");
        String e = "PCR Request ID is wrong. Please recheck.";
        jSONObjectOut.put("message", e);
        return jSONObjectOut;
    }

    private JSONObject errorMessageNoAge() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 400);
        jSONObjectOut.put("type", "error");
        String e = "An age is required.";
        jSONObjectOut.put("message", e);
        return jSONObjectOut;
    }

    private JSONObject errorMessageNoIndicator() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 411);
        jSONObjectOut.put("type", "error");
        jSONObjectOut.put("message", "Indicator NOT recognized.");
        return jSONObjectOut;
    }

    private JSONObject errorMessageLogin() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 411);
        jSONObjectOut.put("type", "error");
        jSONObjectOut.put("message", "Your username and password combination is wrong.");
        return jSONObjectOut;
    }

    private JSONObject errorMessageNoIps() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 411);
        jSONObjectOut.put("type", "error");
        jSONObjectOut.put("message", "Your have not setup IPs you can login.");
        return jSONObjectOut;
    }

    private JSONObject errorMessageNotAnAutherizedIp() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 411);
        jSONObjectOut.put("type", "error");
        jSONObjectOut.put("message", "Your are using a non autherized IP. Add it to your API IDPs and retry.");
        return jSONObjectOut;
    }

    private JSONObject errorMessageNoGender() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 402);
        jSONObjectOut.put("type", "error");
        jSONObjectOut.put("message", "Gender is not provided.");
        return jSONObjectOut;
    }

    private JSONObject errorMessageNoLab() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 402);
        jSONObjectOut.put("type", "error");
        jSONObjectOut.put("message", "Lab is not provided.");
        return jSONObjectOut;
    }

    private JSONObject errorMessageNoInstituteId() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 402);
        jSONObjectOut.put("type", "error");
        jSONObjectOut.put("message", "Parameter institute_id is not provided or not recognized.");
        return jSONObjectOut;
    }

    private JSONObject errorMessageNoInstituteFound() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 403);
        jSONObjectOut.put("type", "error");
        jSONObjectOut.put("message", "Parameter institute_id is not found.");
        return jSONObjectOut;
    }

    private JSONObject errorMessageInstruction() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 400);
        jSONObjectOut.put("type", "error");
        jSONObjectOut.put("message", "You must provide a value for the parameter name.");
        return jSONObjectOut;
    }

    private JSONObject errorMessageNoId() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 410);
        jSONObjectOut.put("type", "error");
        jSONObjectOut.put("message", "The ID provided is not found.");
        return jSONObjectOut;
    }

    private String Get_Child_Institutions(Institution institution) {
        String childInstitions = null;

        if (institution != null) {
            List<Institution> instList = institutionApplicationController.findChildrenInstitutions(institution);

            for (Institution i_ : instList) {
                if (childInstitions == null) {
                    childInstitions = institution.getCode() + ":" + i_.getCode();
                } else {
                    childInstitions += "^" + institution.getCode() + ":" + i_.getCode();
                }
            }
        }
        return childInstitions;
    }
}
