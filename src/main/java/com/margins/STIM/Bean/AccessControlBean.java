/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Bean;

import com.google.gson.Gson;
import com.margins.STIM.Interface.DeviceService;
import com.margins.STIM.entity.AccessAnomaly;
import com.margins.STIM.entity.AccessLog;
import com.margins.STIM.entity.Devices;

import com.margins.STIM.entity.Employee;
import com.margins.STIM.entity.Entrances;
import com.margins.STIM.entity.enums.ActionResult;
import com.margins.STIM.entity.enums.AnomalyType;
import com.margins.STIM.entity.enums.AuditActionType;
import com.margins.STIM.entity.enums.EntranceMode;
import com.margins.STIM.entity.model.AccessEvaluationResult;
import com.margins.STIM.entity.model.VerificationRequest;
import com.margins.STIM.entity.nia_verify.VerificationResultData;
import com.margins.STIM.service.AccessLogService;
import com.margins.STIM.service.AnomalyDetectionService;
import com.margins.STIM.service.AuditLogService;
import com.margins.STIM.service.EmployeeEntranceStateService;

import com.margins.STIM.service.EmployeeRole_Service;
import com.margins.STIM.service.Employee_Service;
import com.margins.STIM.service.EntrancesService;
import com.margins.STIM.util.FingerprintProcessor;
import com.margins.STIM.util.JSF;
import com.margins.STIM.util.ValidationUtil;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author PhilipManteAsare
 */
@Named("accessBean")
@Getter
@Setter
@SessionScoped
public class AccessControlBean implements Serializable {

    @EJB
    private EntrancesService entrancesService;

    @Inject
    private BreadcrumbBean breadcrumbBean;

    @EJB
    private Employee_Service employeeService;

    @EJB
    private EmployeeRole_Service employeeRoleService;

    @EJB
    private AccessLogService accessLogService;

    @Inject
    private AuditLogService auditLogService;
    @Inject
    private UserSession userSession;

    @Inject
    private DeviceService deviceService;

    @Inject
    private AnomalyDetectionService anomalyDetectionService;

    @Inject
    private EmployeeEntranceStateService employeeEntranceStateService;

    private List<Entrances> entrances;
    private List<Devices> allDevices;
    private String selectedEntrance;
    private String statusMessage;
    private String ghanaCardNumber;
    private String capturedFinger;
    private String socketData;
    private String fingerPosition;
    private Devices selectedDevice;

    String BASE_URL = JSF.getContextURL() + "/";
    VerificationResultData callBack = new VerificationResultData();

    @PostConstruct
    public void init() {
        entrances = getAllEntrances();
        allDevices = getAllDevices();
        statusMessage = "Awaiting Action";
        ghanaCardNumber = "";
        fingerPosition = "";
        capturedFinger = "";
        selectedEntrance = "";
    }

    public List<Entrances> getAllEntrances() {
        return entrancesService.findAllEntrances(); // Fetch from DB once
    }

    public List<Devices> getAllDevices() {
        return deviceService.getAllDevices();
    }

    public void setupBreadcrumb() {
        breadcrumbBean.setAccessControlBreadcrumb();
    }

    public List<String> completeEntrances(String query) {
        List<Entrances> results;
        if (query == null || query.trim().isEmpty()) {
            results = entrancesService.findAllEntrances(); // Fetch all
        } else {
            results = entrancesService.searchEntrances(query); // Filter from service
        }
        return results.stream()
                .map(e -> e.getEntranceName() + " (" + e.getEntranceDeviceId() + ")")
                .collect(Collectors.toList());
    }

    // Reset Fingerprint Selection
    public void reset() {
        ghanaCardNumber = null;
        selectedEntrance = null;
        capturedFinger = null;
        fingerPosition = null;
        statusMessage = "Awaiting Action";
        JSF.addWarningMessage("Reset successful.");

    }

    public void resetForm() {
        ghanaCardNumber = "";
        fingerPosition = "";
        capturedFinger = "";
        selectedEntrance = "";
        statusMessage = "Awaiting Action";
    }

    public String getSocketData() {
        reload();
        return socketData;
    }

    public void submit() {
        String result = "DENIED"; // Default
        Double verificationTime = null;
//        String entranceId = null;
        Employee employee = null;
        AccessEvaluationResult evaluation = null;
         AccessLog log = new AccessLog();
        try {
            System.out.println("GHANACARD2 >>>>>>>>>>>>>> " + ghanaCardNumber);
            if (!ValidationUtil.isValidGhanaCardNumber(ghanaCardNumber)) {
                statusMessage = "Invalid Ghana Card";
                JSF.addErrorMessage("Invalid Ghana Card Format");
                return;
            }

            if (capturedFinger == null || capturedFinger.isEmpty()) {
                statusMessage = "No Fingerprint";
                JSF.addErrorMessage("No fingerprint captured. Please scan your fingerprint.");
                return;
            }

            if (selectedDevice == null) {
                statusMessage = "Device Not Available.";
                JSF.addErrorMessage("Device Not Available.");
                return;
            }
            if (selectedDevice.getEntrance() == null) {
                statusMessage = "No Entrance Assigned to device.";
                JSF.addErrorMessage("No Entrance Assigned to device.");
                return;
            }
            System.out.println("GHANACARD3 >>>>>>>>>>>>>> " + ghanaCardNumber);

//            entranceId = extractEntranceId(selectedEntrance);
            VerificationRequest request = new VerificationRequest();
            request.setPosition(fingerPosition);
            request.setPinNumber(ghanaCardNumber);
            String processedImage = FingerprintProcessor.imageDpi(capturedFinger);
            request.setImage(processedImage);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            X509TrustManager trustManager = new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[]{};
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            };
            sslContext.init(null, new TrustManager[]{trustManager}, new SecureRandom());

            String requestString = new Gson().toJson(request);
            HttpClient client = HttpClient.newBuilder().sslContext(sslContext).build();
            HttpRequest httpRequest = HttpRequest
                    .newBuilder(new URI("https://selfie.imsgh.org:2035/skyface/api/v1/third-party/verification/base_64/verification/kyc/finger"))
                    .POST(HttpRequest.BodyPublishers.ofString(requestString))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .build();

            long startTime = System.nanoTime();
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            verificationTime = (System.nanoTime() - startTime) / 1_000_000_000.0;

            System.out.println("Response Status: " + response.statusCode());
            System.out.println("Response Body: " + response.body());

            log.setVerificationTime(verificationTime);
            log.setTimestamp(LocalDateTime.now());
            log.setDevice(selectedDevice);

            callBack = new Gson().fromJson(response.body(), VerificationResultData.class);

            if (response.statusCode() != 200 || callBack == null) {
                String errorMessage = "Verification Error: " + (callBack != null ? callBack.getMsg() : "No response from server");
                auditLogService.logActivity(AuditActionType.ACCESS_CHECK, "Access Control Test", ActionResult.FAILED, errorMessage, userSession.getCurrentUser());
                statusMessage = "Verification Error";
                JSF.addErrorMessage(errorMessage);
                log.setResult(result);
            } else if (!"TRUE".equals(callBack.getData().getVerified())) {
                statusMessage = "Access Denied";
                String errorMessage = "Fingerprint Verification Failed!";
                JSF.addErrorMessage(errorMessage);
                auditLogService.logActivity(AuditActionType.ACCESS_CHECK, "Access Control Test", ActionResult.FAILED, errorMessage, userSession.getCurrentUser());
            log.setResult(result);
            } else {
                Entrances entrance = selectedDevice.getEntrance();
                if (entrance == null) {
                    statusMessage = "Invalid Entrance";
                    String errorMessage = "Selected entrance not found.";
                    JSF.addErrorMessage(errorMessage);
                    auditLogService.logActivity(AuditActionType.ACCESS_CHECK, "Access Control Test", ActionResult.FAILED, errorMessage, userSession.getCurrentUser());
                log.setResult(result);
                } else {
                    employee = employeeService.findEmployeeByGhanaCard(ghanaCardNumber);
                    if (employee == null) {
                        statusMessage = "Access Denied";
                        String errorMessage = "Employee not found.";
                        JSF.addErrorMessage(errorMessage);
                        auditLogService.logActivity(AuditActionType.ACCESS_CHECK, "Access Control Test", ActionResult.FAILED, errorMessage, userSession.getCurrentUser());
                    log.setResult(result);
                    } else {
                        log.setEmployee(employee);
                        evaluation = accessLogService.evaluateAccess(employee, selectedDevice);
                        result = evaluation.getResult();
                        log.setResult(result);

                        String displayMessage = evaluation.getMessage();

                        if (evaluation.isGranted()) {
                            String successMessage = "Access granted to " + selectedDevice.getEntrance().getEntranceName();
                            JSF.addSuccessMessage(successMessage);
                            auditLogService.logActivity(AuditActionType.ACCESS_CHECK, "Access Control Test", ActionResult.SUCCESS, successMessage, userSession.getCurrentUser());
                            statusMessage = "Access Granted";

                            try {
                                employeeEntranceStateService.recordEntryOrExit(
                                        employee, selectedDevice.getEntrance(), selectedDevice.getDevicePosition(), "SYSTEM", selectedDevice
                                );
                            } catch (Exception e) {
                                // Log error but don't change access result since door already opened
                                auditLogService.logActivity(AuditActionType.ACCESS_CHECK,
                                        "Access Control Test",
                                        ActionResult.FAILED,
                                        "State update failed after successful access",
                                        userSession.getCurrentUser());
                            }
                        } else {
                            String errorMessage = "Access denied: " + evaluation.getMessage();
                            JSF.addErrorMessage(errorMessage);
                            statusMessage = "Access Denied";
                            auditLogService.logActivity(AuditActionType.ACCESS_CHECK, "Access Control Test", ActionResult.FAILED, errorMessage, userSession.getCurrentUser());
                            anomalyDetectionService.checkFrequentDeniedAccess(employee, selectedDevice);

                        }
                    }
                }
            }

        } catch (Exception e) {
            statusMessage = "Error";
            String errorDetail = "Exception during access control check: " + e.getMessage();
            JSF.addErrorMessage("Error during access control check: " + e.getLocalizedMessage());
            auditLogService.logActivity(AuditActionType.ACCESS_CHECK, "Access Control Test", ActionResult.FAILED, errorDetail, userSession.getCurrentUser());
            log.setResult(result);
            e.printStackTrace();

        } finally {
            // Log every attempt   
            accessLogService.logAccess(log);

            if (evaluation != null && evaluation.getAnomalies() != null) {
                for (AnomalyType anomalyType : evaluation.getAnomalies()) {
                    AccessAnomaly anomaly = new AccessAnomaly();
                    anomaly.setAnomalyType(anomalyType);
                    anomaly.setAnomalySeverity(anomalyType.getSeverity());
                    anomaly.setEmployee(employee);
                    anomaly.setMessage(evaluation.getMessage());
                    anomaly.setDevice(selectedDevice);
                    anomaly.setEntrance(selectedDevice.getEntrance());
                    anomaly.setTimestamp(LocalDateTime.now());
                    anomaly.setAccessLog(log);
                    accessLogService.logAnomalies(anomaly);
                }
            } else {
            }
        }
    }

    public List<Devices> completeDevices(String query) {
        if (query == null || query.isBlank()) {
            return deviceService.getAllDevices(); // fallback to all
        }
        return deviceService.searchDevices(query);
    }

    private void reload() {
        if (socketData != null) {
            // Fetch data from database with socketData and populate captured fingers.
        }
    }
}
