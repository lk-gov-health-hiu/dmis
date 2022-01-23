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
import javax.ws.rs.PathParam;
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
import lk.gov.health.phsp.bean.StoredQueryResultController;
import lk.gov.health.phsp.bean.WebUserApplicationController;
import lk.gov.health.phsp.bean.WebUserController;
import lk.gov.health.phsp.entity.ApiRequest;
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.Client;
import lk.gov.health.phsp.entity.ClientEncounterComponentItem;
import lk.gov.health.phsp.entity.Document;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.entity.QueryComponent;
import lk.gov.health.phsp.entity.Relationship;
import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.enums.AreaType;
import lk.gov.health.phsp.enums.EncounterType;
import lk.gov.health.phsp.enums.InstitutionType;
import lk.gov.health.phsp.enums.RelationshipType;
import lk.gov.health.phsp.enums.WebUserRole;

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
    StoredQueryResultController storedQueryResultController;
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
                    jSONObjectOut = submitPcrRequest(ipadd,
                            username,
                            password,
                            test_number,
                            referring_lab_id,
                            client_name,
                            client_address,
                            client_phone_number,
                            client_nic,
                            client_passport_number,
                            client_age_in_years,
                            client_age_in_months,
                            client_age_in_days,
                            client_gender,
                            client_citizenship,
                            ordering_category_id,
                            moh_id,
                            district_id,
                            gn_area_id,
                            phi_area_id,
                            comments);
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

        String json = jSONObjectOut.toString();
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

    private JSONObject submitPcrRequest(String ip,
            String username,
            String password,
            String test_number,
            String referring_lab_id,
            String client_name,
            String client_address,
            String client_phone_number,
            String client_nic,
            String client_passport_number,
            String client_age_in_years,
            String client_age_in_months,
            String client_age_in_days,
            String client_gender,
            String client_citizenship,
            String ordering_category_id,
            String moh_id,
            String district_id,
            String gn_area_id,
            String phi_area_id,
            String comments) {


        System.out.println("submitPcrRequest");
        System.out.println("ip = " + ip);

        JSONObject jSONObjectOut = new JSONObject();
        JSONArray array = new JSONArray();

        Integer intAgeInYears = 0;
        Integer intAgeInMonths = 0;
        Integer intAgeInDays = 0;
        Area cDistrict;
        Area cMoh;
        Area cGn;
        Area cPhi;

        WebUser wu;

        Item cGender = null;
        Item cCitizenship = null;
        Item eOrderingCategory = null;
        Institution lab;

        wu = webUserApplicationController.getWebUser(username, password);

        if (wu == null) {
            return errorMessageLogin();
        }

        if (wu.getLoginIPs() == null || wu.getLoginIPs().equals("")) {
            return errorMessageNoIps();
        }

        if (ip != null && !wu.getLoginIPs().contains(ip)) {
            return errorMessageNotAnAutherizedIp();
        }

        if (test_number == null || test_number.trim().equals("")) {
            return errorMessageNoTestNumber();
        }

        lab = institutionApplicationController.findInstitutionById(CommonController.getLongValue(referring_lab_id));

        if (lab == null) {
            return errorMessageNoLab();
        }

        cDistrict = areaApplicationController.getArea(CommonController.getLongValue(district_id));
        cMoh = areaApplicationController.getArea(CommonController.getLongValue(moh_id));
        cGn = areaApplicationController.getArea(CommonController.getLongValue(gn_area_id));
        cPhi = areaApplicationController.getArea(CommonController.getLongValue(phi_area_id));
        eOrderingCategory = itemApplicationController.findItemById(CommonController.getLongValue(ordering_category_id));

        if (client_name == null || client_name.trim().equals("")) {
            return errorMessageNoClientName();
        }

        if (client_gender == null || client_gender.trim().equals("")) {
            return errorMessageNoGender();
        } else {
            if (client_gender.toLowerCase().contains("f")) {
                cGender = itemApplicationController.getFemale();
            } else {
                cGender = itemApplicationController.getMale();
            }
        }

        if (client_citizenship != null) {
            if (client_citizenship.toLowerCase().contains("f")) {
                cCitizenship = itemApplicationController.findItemByCode("citizenship_foreign");
            } else {
                cCitizenship = itemApplicationController.findItemByCode("citizenship_local");
            }
        }

        if (client_age_in_years != null || !client_name.trim().equals("")) {
            intAgeInYears = CommonController.getIntegerValue(client_age_in_years);
        } else if (client_age_in_months != null || !client_name.trim().equals("")) {
            intAgeInMonths = CommonController.getIntegerValue(client_age_in_months);
        } else if (client_age_in_days != null || !client_name.trim().equals("")) {
            intAgeInDays = CommonController.getIntegerValue(client_age_in_days);
        } else {
            return errorMessageNoAge();
        }
        if (intAgeInYears == null || intAgeInYears < 1) {
            if (intAgeInMonths == null || intAgeInMonths < 1) {
                if (intAgeInDays == null || intAgeInDays < 1) {
                    return errorMessageNoAge();
                }
            }
        }

        Client c = new Client();
        c.getPerson().setName(client_name);
        c.getPerson().setAgeYears(intAgeInYears);
        c.getPerson().setAgeMonths(intAgeInMonths);
        c.getPerson().setAgeDays(intAgeInDays);
        c.getPerson().setPhone1(client_phone_number);
        c.getPerson().setAddress(client_address);
        c.getPerson().setDistrict(cDistrict);
        c.getPerson().setMohArea(cMoh);
        c.getPerson().setGnArea(cGn);
        c.getPerson().setPhiArea(cPhi);
        c.getPerson().setSex(cGender);
        c.getPerson().setCitizenship(cCitizenship);
        if (client_nic == null) {
            client_nic = "";
        } else {
            client_nic = client_nic.trim().toUpperCase();
        }
        if (client_passport_number == null) {
            client_passport_number = "";
        } else {
            client_passport_number = client_passport_number.trim().toUpperCase();
        }

        if (!client_nic.equals("") && !client_passport_number.equals("")) {
            c.getPerson().setNic(client_nic);
            c.getPerson().setPassportNumber(client_passport_number);
        } else if (client_nic.equals("") && !client_passport_number.equals("")) {
            c.getPerson().setNic(client_passport_number);
            c.getPerson().setPassportNumber(client_passport_number);
        } else if (!client_nic.equals("") && client_passport_number.equals("")) {
            c.getPerson().setNic(client_nic);
            c.getPerson().setPassportNumber(client_nic);
        }

        c.setCreateInstitution(wu.getInstitution());
        c.setCreatedAt(new Date());
        c.setCreatedBy(wu);
        c.setCreatedAt(new Date());
     

        Document e = new Document();
        e.setPcrTestType(itemApplicationController.getPcr());
        e.setClient(c);
        e.setComments(comments);
        e.setCreatedAt(new Date());
        e.setPcrOrderingCategory(eOrderingCategory);
        e.setCreatedAt(new Date());
        e.setCreatedBy(wu);
        e.setCreatedInstitution(wu.getInstitution());
        e.setInstitution(wu.getInstitution());
        e.setPcrTestType(itemApplicationController.getPcr());
        e.setReferalInstitution(lab);
        e.setBht(test_number);
        e.setEncounterNumber(test_number);
        e.setEncounterDate(new Date());
        e.setEncounterType(EncounterType.Test_Enrollment);
        e.setSampled(true);
        e.setSampledAt(new Date());
        e.setSampledBy(wu);
        e.setSentToLab(Boolean.TRUE);
        e.setSentToLabAt(new Date());
        e.setSentToLabBy(wu);

        encounterApplicationController.save(e);

        JSONObject ja = new JSONObject();
        ja.put("request_id", e.getId());

        jSONObjectOut.put("data", ja);
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
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
        if (!e.getEncounterType().equals(EncounterType.Test_Enrollment) ||
                !e.getPcrTestType().equals(itemApplicationController.getPcr())) {
            System.out.println("Type wrong");
            return errorMessageNoSuchPcrRequestId();
        }


        if (!e.getInstitution().equals(wu.getInstitution())) {
            System.out.println("institution wrong");
            return errorMessageNoSuchPcrRequestId();
        }

        JSONObject ja = new JSONObject();

        if (e.getSampleMissing()!=null && e.getSampleMissing()== true) {
            ja.put("pcr_result_status", "Sample is Missing");
            jSONObjectOut.put("data", ja);
            jSONObjectOut.put("status", successMessage());
            return jSONObjectOut;
        }
        if (e.getSampleRejectedAtLab()!=null && e.getSampleRejectedAtLab() == true) {
            ja.put("pcr_result_status", "Sample Rejected.");
            jSONObjectOut.put("data", ja);
            jSONObjectOut.put("status", successMessage());
            return jSONObjectOut;
        }

        if (e.getReceivedAtLab() == null || e.getReceivedAtLab() == false) {
            ja.put("pcr_result_status", "Sample is awaiting to be received at the lab.");
            jSONObjectOut.put("data", ja);
            jSONObjectOut.put("status", successMessage());
            return jSONObjectOut;
        } else {
            if (e.getResultEntered() == null || e.getResultEntered() == false) {
                ja.put("pcr_result_status", "Sample is Processing");
                jSONObjectOut.put("data", ja);
                jSONObjectOut.put("status", successMessage());
                return jSONObjectOut;
            } else {
                if (e.getResultReviewed() == null || e.getResultReviewed() == false) {
                    ja.put("pcr_result_status", "Awaiting reviewing results.");
                    jSONObjectOut.put("data", ja);
                    jSONObjectOut.put("status", successMessage());
                    return jSONObjectOut;
                } else {
                    if (e.getResultConfirmed() == null || e.getResultConfirmed() == false) {
                        ja.put("pcr_result_status", "Awaiting confirming results.");
                        jSONObjectOut.put("data", ja);
                        jSONObjectOut.put("status", successMessage());
                        return jSONObjectOut;
                    } else {
                        if (e.getResultConfirmed() == true) {
                            ja.put("pcr_result_status", "Results available.");
                            ja.put("result", e.getPcrResult().getName());
                            ja.put("result_display", e.getPcrResultStr());
                            ja.put("ct1_value", e.getCtValue().toString());
                            ja.put("ct2_value", e.getCtValue2().toString());
                            ja.put("comments", e.getResultComments());
                            ja.put("report", e.getResultPrintHtml());
                            jSONObjectOut.put("data", ja);
                            jSONObjectOut.put("status", successMessage());
                            return jSONObjectOut;
                        }
                    }

                }
            }
        }

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
            ja.put("latitude", a.getCoordinate().getLatitude());
            ja.put("longitude", a.getCoordinate().getLongitude());
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
        List<Item> ds = itemApplicationController.getVaccinationStatuses();
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
        List<Item> ds = itemApplicationController.getCovidTestOrderingCategories();
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
