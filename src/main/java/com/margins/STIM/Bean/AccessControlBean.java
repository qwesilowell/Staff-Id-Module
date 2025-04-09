/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Bean;

import com.google.gson.Gson;
import com.margins.STIM.entity.Employee;
import com.margins.STIM.entity.EmployeeRole;
import com.margins.STIM.entity.Entrances;
import com.margins.STIM.entity.model.VerificationRequest;
import com.margins.STIM.entity.nia_verify.VerificationResultData;
import com.margins.STIM.service.EmployeeRole_Service;
import com.margins.STIM.service.Employee_Service;
import com.margins.STIM.service.EntrancesService;
import com.margins.STIM.util.FingerprintProcessor;
import com.margins.STIM.util.JSF;
import com.margins.STIM.util.ValidationUtil;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
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

    @EJB
    private Employee_Service employeeService;

    @EJB
    private EmployeeRole_Service employeeRoleService;

    private List<Entrances> entrances;
    private String selectedEntrance;
    private String statusMessage;
    private String ghanaCardNumber;
    private String capturedFinger;
    private String socketData;
    private String fingerPosition;
    String BASE_URL = JSF.getContextURL() + "/";
    VerificationResultData callBack = new VerificationResultData();

    @PostConstruct
    public void init() {
        entrances = getAllEntrances();
        statusMessage = "Awaiting Action";
        ghanaCardNumber = "";
        fingerPosition = "";
        capturedFinger = "";
        selectedEntrance = "";
    }
    
    public List<Entrances> getAllEntrances() {
        return entrancesService.findAllEntrances(); // Fetch from DB once
    }

    public List<String> completeEntrances(String query) {
        List<Entrances> results;
        if (query == null || query.trim().isEmpty()) {
            results = entrancesService.findAllEntrances(); // Fetch all
        } else {
            results = entrancesService.searchEntrances(query); // Filter from service
        }
        return results.stream()
                .map(e -> e.getEntrance_Name() + " (" + e.getEntrance_Device_ID() + ")")
                .collect(Collectors.toList());
    }

    // Reset Fingerprint Selection
    public void reset() {
         ghanaCardNumber = null; 
         selectedEntrance= null;
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

        try {
            System.out.println("GHANACARD2 >>>>>>>>>>>>>> " + ghanaCardNumber);
            if (!ValidationUtil.isValidGhanaCardNumber(ghanaCardNumber)) {
                JSF.addErrorMessage("Invalid Ghana Card Format");
                return;
            }

            if (capturedFinger == null || capturedFinger.isEmpty()) {
                JSF.addErrorMessage("No fingerprint captured. Please scan your fingerprint.");
                return;
            }
            if (selectedEntrance == null || selectedEntrance.trim().isEmpty()) {

                JSF.addErrorMessage("Please select an entrance.");
                return;
            }
            System.out.println("GHANACARD3 >>>>>>>>>>>>>> " + ghanaCardNumber);

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
            HttpClient client = HttpClient
                    .newBuilder()
                    .sslContext(sslContext)
                    .build();
            HttpRequest httpRequest = HttpRequest
                    .newBuilder(new URI("https://selfie.imsgh.org:2035/skyface/api/v1/third-party/verification/base_64/verification/kyc/finger"))
                    .POST(HttpRequest.BodyPublishers.ofString(requestString))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .build();
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            System.out.println("Response Status: " + response.statusCode());
            System.out.println("Response Body: " + response.body());

            callBack = new Gson().fromJson(response.body(), VerificationResultData.class);

            if (response.statusCode() != 200 || callBack == null) {
                statusMessage = "Verification Error";
                JSF.addErrorMessage("Verification Error: " + (callBack != null ? callBack.getMsg() : "No response from server"));
                return;
            }

            if (!"TRUE".equals(callBack.getData().getVerified())) {
                statusMessage = "Access Denied";
                JSF.addErrorMessage("Fingerprint Verification Failed!");
                return;
            }

            String entranceId = extractEntranceId(selectedEntrance);
            Entrances entrance = entrancesService.findEntranceById( entranceId);
            if (entrance == null) {
                statusMessage = "Invalid Entrance";
                JSF.addErrorMessage("Selected entrance not found.");
                return;
            }

            Employee employee = employeeService.findEmployeeByGhanaCard(ghanaCardNumber);
            if (employee == null) {
                statusMessage = "Access Denied";
                JSF.addErrorMessage("Employee not found.");
                return;
            }

            if (hasAccess(employee, entrance)) {
                statusMessage = "Access Granted";
                JSF.addSuccessMessage("Access granted to " + entrance.getEntrance_Name());
            } else {
                statusMessage = "Access Denied";
                JSF.addErrorMessage("Access denied to " + entrance.getEntrance_Name());
            }

          
        } catch (Exception e) {
            statusMessage = "Error";
            JSF.addErrorMessage("An unexpected error occurred. Please try again!");
            e.printStackTrace();
        }
    }

    private String extractEntranceId(String entranceString) {
        if (entranceString != null && entranceString.contains("(") && entranceString.contains(")")) {
            return entranceString.substring(entranceString.lastIndexOf("(") + 1, entranceString.lastIndexOf(")"));
        }
        return entranceString;
    }
    
    private boolean hasAccess(Employee employee, Entrances entrance) {
        EmployeeRole role = employee.getRole();
        if (role == null) {
            return false;
        }
        return entrance.getAllowedRoles().stream()
                .anyMatch(r -> r.getId() == role.getId()); 
    }

    private void reload() {
        if (socketData != null) {
            // Fetch data from database with socketData and populate captured fingers.
        }
    }
}
