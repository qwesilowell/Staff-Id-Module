/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Bean;

import com.google.gson.Gson;
import com.margins.STIM.entity.Employee;
import com.margins.STIM.entity.EmploymentStatus;
import com.margins.STIM.entity.enums.ActionResult;
import com.margins.STIM.entity.enums.AuditActionType;
import com.margins.STIM.entity.model.VerificationRequest;
import com.margins.STIM.entity.nia_verify.VerificationResultData;
import com.margins.STIM.entity.websocket.FingerCaptured;
import com.margins.STIM.DTO.CapturedFinger;
import com.margins.STIM.service.AuditLogService;
import com.margins.STIM.service.Employee_Service;
import com.margins.STIM.util.FingerprintProcessor;
import com.margins.STIM.util.JSF;
import com.margins.STIM.util.ValidationUtil;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.primefaces.PrimeFaces;

/**
 *
 * @author PhilipManteAsare
 */
@Named("singlefingerBean")
@SessionScoped
public class SingleFingerController implements Serializable {

    @EJB
    private Employee_Service employeeService;
    
    @Inject
    private AuditLogService auditLogService;
    @Inject
    private UserSession userSession;

    @Inject 
    private BreadcrumbBean breadcrumbBean;
    
    @Getter
    private int currentStep = 0;;

    @Getter
    @Setter
    private String verificationType;

    private boolean verificationSuccess = false;
    @Getter
    @Setter
    private String ghanaCardNumber;

    @Getter
    @Setter
    private String capturedFinger;

    @Getter
    @Setter
    private CapturedFinger capturedMultiFinger = new CapturedFinger();

    List<FingerCaptured> capturedFingers = new ArrayList<>();

    @Getter
    @Setter
    private String fingerPosition;

    @Getter
    @Setter
    VerificationResultData callBack = new VerificationResultData();

    @Getter
    @Setter
    private Employee foundEmployee;

    @Getter
    @Setter
    private boolean showUpdateButton = false;

    @Getter
    @Setter
    private String faceImageData;

    @Getter
    @Setter
    private String updatedEmail;

    @Getter
    @Setter
    private String updatedAddress;

    @Getter
    @Setter
    private int updatedStatus;
    
    @Getter
    @Setter
    private String primaryPhoneNumber;
    
    @Getter
    @Setter
    private String secondaryPhoneNumber;

    private byte[] fingerData;
    private String socketData;

    String BASE_URL = JSF.getContextURL() + "/";

    @Getter
    @Setter
    private List<EmploymentStatus> availableStatuses;

    @PostConstruct
    public void init() {
        // Load available roles from database
        availableStatuses = employeeService.findAllEmploymentStatuses();
    }

    public void setupBreadcrumb() {
        breadcrumbBean.setUpdateEmployeeBreadcrumb();
    }
    
    public void nextStep() {
        if (currentStep == 0 && verificationType == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Select a verification type!", null));
            return;
        }
        if (currentStep == 1 && !verificationSuccess) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Complete verification first!", null));
            return;
        }
        if (currentStep < 3) {
            currentStep++;
        }
    }

    public void prevStep() {
        if (currentStep > 0) {
            currentStep--;
        }
    }

    // Reset Fingerprint Selection
    public void reset() {
        capturedFinger = null;
        fingerPosition = null;

    }

    public String getSocketData() {
        reload();
        return socketData;
    }

    //For Multi Finger
    public String requestData(List<FingerCaptured> fingersesList) throws IOException {
        JSONObject data = new JSONObject();
        data.put("fingers", new JSONArray(capturedFingers));
        data.put("merchantCode", "69af98f5-39fb-44e6-81c7-5e496328cc59");
        return data.toString();
    }

    public void sendForVerification() throws IOException {
        try {

            capturedFingers.add(new FingerCaptured("PNG", "LL", capturedMultiFinger.getLeftLittle()));
            capturedFingers.add(new FingerCaptured("PNG", "LR", capturedMultiFinger.getLeftRing()));
            capturedFingers.add(new FingerCaptured("PNG", "LM", capturedMultiFinger.getLeftMiddle()));
            capturedFingers.add(new FingerCaptured("PNG", "LI", capturedMultiFinger.getLeftIndex()));
            capturedFingers.add(new FingerCaptured("PNG", "LT", capturedMultiFinger.getLeftThumb()));
            capturedFingers.add(new FingerCaptured("PNG", "RT", capturedMultiFinger.getRightThumb()));
            capturedFingers.add(new FingerCaptured("PNG", "RI", capturedMultiFinger.getRightIndex()));
            capturedFingers.add(new FingerCaptured("PNG", "RM", capturedMultiFinger.getRightMiddle()));
            capturedFingers.add(new FingerCaptured("PNG", "RR", capturedMultiFinger.getRightRing()));
            capturedFingers.add(new FingerCaptured("PNG", "RL", capturedMultiFinger.getRightLittle()));

            System.out.println("capturedFingers length: " + capturedFingers.size());
            capturedFingers.forEach((fc) -> {
                System.out.println("capturedFinger: " + fc.getImage() + " type: " + fc.getDataType() + " pos: " + fc.getPosition());
            });

            if (capturedFingers.isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "No fingerprints captured. Please rescan fingers before submitting.", null));
                return;
            }

            String request = requestData(capturedFingers);

            if (request == null) {
                JSF.addErrorMessage("Failed to generate request. No valid fingerprint data.");
                return;
            }
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

            HttpClient client = HttpClient
                    .newBuilder()
                    .sslContext(sslContext)
                    .build();
            HttpRequest httpRequest = HttpRequest
                    .newBuilder(new URI("https://selfie.imsgh.org:2035/skyface/api/v1/third-party/verification/base_64/search/kyc/no_card"))
                    .POST(HttpRequest.BodyPublishers.ofString(request))
                    .header("Content-Type", "application/json")
                    .build();
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            System.out.println("Response Status: " + response.statusCode());
            System.out.println("Response Body: " + response.body());

            if (response.statusCode() == 400) {
                JSF.addErrorMessage("API Error: Bad Request. Please check fingerprint data.");
                return;
            }
            String res = response.body();
            Gson g = new Gson();
            callBack = g.fromJson(res, VerificationResultData.class);

            String message;
            if (callBack.msg != null) {
                message = callBack.msg.toString();
            } else {
                message = "Callback returned null";
            }

            System.out.println("Response Data: " + response.body());
            if (response.statusCode() == 200 && callBack != null) {
                if (callBack.isSuccess() && "TRUE".equals(callBack.getData().getVerified())) {
                    verificationSuccess = true;
                    JSF.addSuccessMessage("Multi Finger Verification Successful!");

                   

                    String cardNumber = callBack.getData().getPerson().nationalId;
//                    String newFirstName = callBack.getData().getPerson().getForenames();
//                    String newLastName = callBack.getData().getPerson().getSurname();
                    foundEmployee = employeeService.findEmployeeByGhanaCard(cardNumber);

                    if (foundEmployee != null) {
                         currentStep++;
                        updatedAddress = foundEmployee.getAddress();
                        updatedEmail = foundEmployee.getEmail();
                        updatedStatus = foundEmployee.getEmploymentStatus().getId();
                        showUpdateButton = true;
                        JSF.addSuccessMessage("Verification successful. Employee found: " + foundEmployee.getFullName());
                        PrimeFaces.current().ajax().update("wizardForm");
                    } else {
                        resetWizard();
                        JSF.addErrorMessage("Verification successful but no matching employee found.");
                    }
                }
            } else {
                JSF.errorMessage("Verification Error:" + (callBack != null ? callBack.msg : "No response from server"));
            }
        } catch (Exception e) {  // Catch any other unexpected errors
            JSF.addErrorMessage("An unexpected error occurred. Please try again!");
            System.out.println("ERROR 3");
            e.printStackTrace(); // Log the error for debugging
            resetWizard();
        }

    }

    public void verifyFace() {
        try {
            VerificationRequest request = new VerificationRequest();
            request.setImage(faceImageData);
            request.setPinNumber(ghanaCardNumber);

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
                    .newBuilder(new URI("https://selfie.imsgh.org:2035/skyface/api/v1/third-party/verification/base_64"))
                    .POST(HttpRequest.BodyPublishers.ofString(requestString))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .build();
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            System.out.println("Response Status: " + response.statusCode());
            System.out.println("Response Body: " + response.body());

            String res = response.body();
            Gson g = new Gson();
            callBack = g.fromJson(res, VerificationResultData.class);
            System.out.println("Response from API: " + res);
            if (response.statusCode() == 200 && callBack != null) {
                if ("TRUE".equals(callBack.getData().getVerified())) {
                    verificationSuccess = true;
                    JSF.addSuccessMessage("Facial Verification Successful!");

                    
                    String cardNumber = callBack.getData().getPerson().nationalId;
//                    String newFirstName = callBack.getData().getPerson().getForenames();
//                    String newLastName = callBack.getData().getPerson().getSurname();
                    foundEmployee = employeeService.findEmployeeByGhanaCard(cardNumber);

                    if (foundEmployee != null) {
                        currentStep++;
                        updatedAddress = foundEmployee.getAddress();
                        updatedEmail = foundEmployee.getEmail();
                        updatedStatus = foundEmployee.getEmploymentStatus().getId();
                        primaryPhoneNumber = foundEmployee.getPrimaryPhone();
                        secondaryPhoneNumber= foundEmployee.getSecondaryPhone();
                        
                        showUpdateButton = true;
                        JSF.addSuccessMessage("Verification successful. Employee found: " + foundEmployee.getFullName());
                        PrimeFaces.current().ajax().update("wizardForm");
                        
                    } else {
                        resetWizard();
                        JSF.addErrorMessage("Verification successful but no matching employee found.");
                    }
                }
            } else {
                JSF.errorMessage("Verification Error:" + (callBack != null ? callBack.msg : "No response from server"));
            }
        } catch (Exception e) {  // Catch any other unexpected errors
            JSF.addErrorMessage("An unexpected error occurred. Please try again!");
            System.out.println("ERROR 3");
            e.printStackTrace(); // Log the error for debugging
            resetWizard();
        }

    }

    public void submit() {

        try {

            System.out.println("Sending fingerprint verification request...");
            System.out.println("Ghana Card Number: " + ghanaCardNumber);
            System.out.println("Finger Position: " + fingerPosition);
            System.out.println("Captured Fingerprint: " + (capturedFinger != null ? "Present" : "Missing"));

            System.out.println("GHANACARD2 >>>>>>>>>>>>>> " + ghanaCardNumber);
            if (ghanaCardNumber == null || ghanaCardNumber.isEmpty()) {
                JSF.addErrorMessage("Please Enter Ghana Card Number");
                return;
            }

            if (!ValidationUtil.isValidGhanaCardNumber(ghanaCardNumber)) {
                JSF.addErrorMessage("Invalid Ghana Card Format");
                return;
            }

            if (capturedFinger == null || capturedFinger.isEmpty()) {
                JSF.addErrorMessage("No fingerprint captured. Please scan your fingerprint.");
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

            String res = response.body();
            Gson g = new Gson();
            callBack = g.fromJson(res, VerificationResultData.class);
            System.out.println("Response from API: " + res);
            if (response.statusCode() == 200 && callBack != null) {
                if ("TRUE".equals(callBack.getData().getVerified())) {
                    verificationSuccess = true;
                    JSF.addSuccessMessage("Single Finger Verification Successful!");

                   

                    String cardNumber = callBack.getData().getPerson().nationalId;
//                    String newFirstName = callBack.getData().getPerson().getForenames();
//                    String newLastName = callBack.getData().getPerson().getSurname();
                    foundEmployee = employeeService.findEmployeeByGhanaCard(cardNumber);

                    if (foundEmployee != null) {
                         currentStep++;
                        updatedAddress = foundEmployee.getAddress();
                        updatedEmail = foundEmployee.getEmail();
                        updatedStatus = foundEmployee.getEmploymentStatus().getId();
                        showUpdateButton = true;
                        JSF.addSuccessMessage("Verification successful. Employee found: " + foundEmployee.getFullName());
                        PrimeFaces.current().ajax().update("wizardForm");
                    } else {
                        JSF.addErrorMessage("Verification successful but no matching employee found.");
                        resetWizard();
                    }
                }
            } else {
                JSF.errorMessage("Verification Error:" + (callBack != null ? callBack.msg : "No response from server"));
            }
        } catch (Exception e) {  // Catch any other unexpected errors
            JSF.addErrorMessage("An unexpected error occurred. Please try again!");
            System.out.println("ERROR 3");
            e.printStackTrace(); // Log the error for debugging
            resetWizard();
        }
    }

    public void updateEmployeeName() {
        if (callBack == null || callBack.getData() == null) {
            JSF.addErrorMessage("No verification result available.");
            return;
        }

        String cardNumber = callBack.getData().getPerson().nationalId;
        String newFirstName = callBack.getData().getPerson().getForenames();
        String newLastName = callBack.getData().getPerson().getSurname();

        try {
            Employee existingEmployee = employeeService.findEmployeeByGhanaCard(cardNumber);

            if (existingEmployee == null) {
                JSF.addErrorMessage("No employee found with Ghana Card Number: " + cardNumber);
                return;
            }

            // Update only these fields
            existingEmployee.setFirstname(newFirstName);
            existingEmployee.setLastname(newLastName);
            existingEmployee.setEmail(updatedEmail);
            existingEmployee.setAddress(updatedAddress);
            existingEmployee.setPrimaryPhone(primaryPhoneNumber);
            existingEmployee.setSecondaryPhone(secondaryPhoneNumber);
            EmploymentStatus newStatus = findEmploymentStatusById(updatedStatus);
            existingEmployee.setEmploymentStatus(newStatus);
            

            employeeService.updateEmployee(cardNumber, existingEmployee);
            foundEmployee = existingEmployee; // Update UI reference
            
            String detail= "Personnel Details Updated for " + existingEmployee.getFullName() + ".";
            auditLogService.logActivity(AuditActionType.UPDATE, "Update Employee Details", ActionResult.SUCCESS, detail, userSession.getCurrentUser());
            JSF.addSuccessMessage("Employee " + newFirstName + " " + newLastName + " updated succesfully ");
            resetWizard();

        } catch (Exception e) {
            String detail = "Failed to Update Personnel Details for " + newFirstName + " " + newLastName + "(" +cardNumber+ ").";
            auditLogService.logActivity(AuditActionType.UPDATE, "Update Employee Details", ActionResult.SUCCESS, detail, userSession.getCurrentUser());
            JSF.addErrorMessage("Error updating employee.");
            e.printStackTrace();
            resetWizard();
        }
    }

    private EmploymentStatus findEmploymentStatusById(int id) {
        return availableStatuses.stream()
                .filter(status -> status.getId() == id)
                .findFirst()
                .orElse(null);
    }

    private void reload() {
        if (socketData != null) {
            // Fetch data from database with socketData and populate captured fingers.
        }
    }

    public void resetWizard() {
        currentStep = 0;
        verificationType = null;
        verificationSuccess = false;
        ghanaCardNumber = null;
        capturedFinger = null;
        capturedMultiFinger = new CapturedFinger();
        capturedFingers.clear();
        fingerPosition = null;
        callBack = new VerificationResultData();
        updatedAddress = null;
        updatedEmail = null;
        faceImageData = null;
        updatedStatus = 0;
        foundEmployee = null;

        PrimeFaces.current().ajax().update("wizardForm");
    }
}
