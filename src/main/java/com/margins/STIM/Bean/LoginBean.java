/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Bean;

/**
 *
 * @author PhilipManteAsare
 */
import com.google.gson.Gson;
import com.margins.STIM.entity.ActivityLog;
import com.margins.STIM.entity.Users;
import com.margins.STIM.entity.enums.ActionResult;
import com.margins.STIM.entity.enums.AuditActionType;
import com.margins.STIM.entity.model.VerificationRequest;
import com.margins.STIM.entity.nia_verify.VerificationResultData;
import com.margins.STIM.entity.websocket.FingerCaptured;
import com.margins.STIM.model.CapturedFinger;
import com.margins.STIM.service.ActivityLogService;
import com.margins.STIM.service.AuditLogService;
import com.margins.STIM.service.User_Service;
import com.margins.STIM.util.FingerprintProcessor;
import com.margins.STIM.util.JSF;
import com.margins.STIM.util.ValidationUtil;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.primefaces.PrimeFaces;

@Named("loginBean")
@SessionScoped
public class LoginBean implements Serializable {

    @Getter
    @Setter
    private String ghanaCardNumber;

    @Getter
    @Setter
    private String password;

    @Getter
    @Setter
    private String capturedFinger;

    @Getter
    @Setter
    private String fingerPosition;

    @Getter
    @Setter
    private String errorMessage;

    @Getter
    @Setter
    private String faceImageData;

    @Getter
    @Setter
    private String msg;

    @Getter
    @Setter
    private CapturedFinger capturedMultiFinger = new CapturedFinger();

    List<FingerCaptured> capturedFingers = new ArrayList<>();

    @Getter
    @Setter
    VerificationResultData callBack = new VerificationResultData();

    private byte[] fingerData;
    private String socketData;

    @Getter
    @Setter
    private String userRole;

    @Getter
    @Setter
    private String username;

    @EJB
    private User_Service userService;

    @EJB
    private ActivityLogService activityLogService;

    @Inject
    private AuditLogService auditLogService;

    @Inject
    private UserSession userSession;

    String BASE_URL = JSF.getContextURL() + "/";

    // Reset Fingerprint Selection
    public void reset() {
        capturedFinger = null;
        fingerPosition = null;
        ghanaCardNumber = null;

    }

    public String getSocketData() {
        reload();
        return socketData;
    }

//    public void checkAccess(UserType requiredRole) {
//        String sessionRoleStr = (String) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("userRole");
//        if (sessionRoleStr == null) {
//            redirectToLogin();
//            return;
//        }
//
//        UserType sessionRole;
//        try {
//            sessionRole = UserType.valueOf(sessionRoleStr);
//        } catch (IllegalArgumentException e) {
//            // Invalid role string in session
//            redirectToLogin();
//            return;
//        }
//
//        if (!sessionRole.equals(requiredRole)) {
//            redirectToLogin();
//        }
//    }
    private void redirectToLogin() {
        try {
            FacesContext.getCurrentInstance().getExternalContext()
                    .redirect(FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/login.xhtml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void submit() {

        try {

            if (ghanaCardNumber == null || ghanaCardNumber.isBlank()) {
                handleBiometricError("Facial Login - GhanaCard is Empty", null, null);
                JSF.addErrorMessage("Ghana Card Number must be filled");
                return;
            }

            if (!ValidationUtil.isValidGhanaCardNumber(ghanaCardNumber)) {
                handleBiometricError("SingleFinger Login - Invalid GhanaCard Format", null, null);
                JSF.addErrorMessage("Invalid Ghana Card Format");
                return;
            }

            Users currentUser = userService.findUserByGhanaCard(ghanaCardNumber);
            if (currentUser == null) {
                handleBiometricError("Facial Login - User not registered in system", null, null);
                JSF.addErrorMessage("User Not Registered");
                return;
            }

            if (capturedFinger == null || capturedFinger.isEmpty()) {
                handleBiometricError("Single Finger Login, Captured Finger is null", null, null);

                JSF.addErrorMessage("No fingerprint captured. Please scan your fingerprint.");
                return;
            }

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
            if (response.statusCode() != 200) {
                handleBiometricError("Server returned status: " + response.statusCode(), null, currentUser);
                return;
            }
            if (callBack == null) {
                handleBiometricError("No response received from callback ", null, currentUser);
                return;
            }
            // Check if biometric verification was successful
            if (!"TRUE".equals(callBack.getData().getVerified())) {
                String errorMsg = callBack.msg != null ? callBack.msg.toString() : "Biometric verification failed";
                handleBiometricError(errorMsg, null, null);
                return;
            }

            if (callBack.data == null || callBack.data.person == null) {
                handleBiometricError("Invalid response data structure", null, currentUser);
                return;
            }

            String forenames = callBack.data.person.forenames;
            String surname = callBack.data.person.surname;
            String nationalId = callBack.data.person.nationalId;

            if (forenames == null || surname == null || nationalId == null) {
                handleBiometricError("Incomplete user data in response", null, currentUser);
                return;
            }

            username = forenames.trim() + " " + surname.trim();
            ghanaCardNumber = nationalId;

            userSession.loginUser(currentUser);

            if (userSession.userActive()) {
                // Successful login
                auditLogService.logActivity(
                        AuditActionType.LOGIN,
                        "Biometric Login Page",
                        ActionResult.SUCCESS,
                        currentUser.getUsername() + " Single Finger Login Successful",
                        currentUser
                );
                FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
                JSF.addSuccessMessageWithSummary("Welcome! ", currentUser.getUsername());

                // Redirect to dashboard
                redirectToDashboard();
            } else {
                // User inactive
                auditLogService.logActivity(
                        AuditActionType.LOGIN,
                        "Biometric Login Page",
                        ActionResult.FAILED,
                        currentUser.getUsername() + " Single Finger Login Failed - User Inactive",
                        currentUser
                );

                JSF.addErrorMessage("Login Failed - Your account is inactive. Please contact administrator.");
                updateForm();
            }
        } catch (Exception e) {
            handleBiometricError("System error during Single Finger login: " + e.getMessage(), e, null);

        }
    }

    public void verifyFace() {
        try {
            if (ghanaCardNumber == null || ghanaCardNumber.isBlank()) {
                JSF.addErrorMessage("Ghana Card Number must be filled");
                return;
            }

            if (!ValidationUtil.isValidGhanaCardNumber(ghanaCardNumber)) {
                handleBiometricError("Facial Login - Invalid GhanaCard Format", null, null);

                JSF.addErrorMessage("Invalid Ghana Card Format");
                return;
            }

            Users currentUser = userService.findUserByGhanaCard(ghanaCardNumber);
            if (currentUser == null) {
                handleBiometricError("Facial Login - User not registered in system", null, null);
                JSF.addErrorMessage("User Not Registered");
                return;
            }

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
            if (response.statusCode() != 200) {
                handleBiometricError("Server returned status: " + response.statusCode(), null, currentUser);
                return;
            }
            if (callBack == null) {
                handleBiometricError("No response received from callback ", null, currentUser);
                return;
            }
            // Check if biometric verification was successful
            if (!"TRUE".equals(callBack.getData().getVerified())) {
                String errorMsg = callBack.msg != null ? callBack.msg.toString() : "Biometric verification failed";
                handleBiometricError(errorMsg, null, null);
                return;
            }

            if (callBack.data == null || callBack.data.person == null) {
                handleBiometricError("Invalid response data structure", null, currentUser);
                return;
            }

            String forenames = callBack.data.person.forenames;
            String surname = callBack.data.person.surname;
            String nationalId = callBack.data.person.nationalId;

            if (forenames == null || surname == null || nationalId == null) {
                handleBiometricError("Incomplete user data in response", null, currentUser);
                return;
            }

            username = forenames.trim() + " " + surname.trim();
            ghanaCardNumber = nationalId;

            userSession.loginUser(currentUser);

            if (userSession.userActive()) {
                // Successful login
                auditLogService.logActivity(
                        AuditActionType.LOGIN,
                        "Biometric Login Page",
                        ActionResult.SUCCESS,
                        currentUser.getUsername() + " Facial Login Successful",
                        currentUser
                );
                FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
                JSF.addSuccessMessageWithSummary("Welcome! ", currentUser.getUsername());

                // Redirect to dashboard
                redirectToDashboard();
            } else {
                // User inactive
                auditLogService.logActivity(
                        AuditActionType.LOGIN,
                        "Biometric Login Page",
                        ActionResult.FAILED,
                        currentUser.getUsername() + " Facial Login Failed - User Inactive",
                        currentUser
                );

                JSF.addErrorMessage("Login Failed - Your account is inactive. Please contact administrator.");
                updateForm();
            }
        } catch (Exception e) {
            handleBiometricError("System error during Facial login: " + e.getMessage(), e, null);

        }
    }

//        for multi finger login
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

//            System.out.println("TRY >>>>>>>>>>>>>>>>>>>>>>>>>" + request);
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
                handleBiometricError("API Error: Bad Request. Response Status Code is " + response.statusCode(), null, null);
                JSF.addErrorMessage("API Error: Bad Request. Please check fingerprint data.");

                return;
            }
            String res = response.body();
            Gson g = new Gson();
            callBack = g.fromJson(res, VerificationResultData.class);

            System.out.println("Response Data: " + res);
            if (response.statusCode() != 200) {
                handleBiometricError("Try Again", null, null);
                return;
            }
            if (callBack == null) {
                handleBiometricError("No response received from biometric server", null, null);
                return;
            }
            // Check if biometric verification was successful
            if (!"TRUE".equals(callBack.getData().getVerified())) {
                String errorMsg = callBack.msg != null ? callBack.msg.toString() : "Biometric verification failed";
                handleBiometricError("Biometric verification failed: " + errorMsg, null, null);
                return;
            }

            if (callBack.data == null || callBack.data.person == null) {
                handleBiometricError("Invalid response data structure", null, null);
                return;
            }

            String forenames = callBack.data.person.forenames;
            String surname = callBack.data.person.surname;
            String nationalId = callBack.data.person.nationalId;

            if (forenames == null || surname == null || nationalId == null) {
                handleBiometricError("Incomplete user data in response", null, null);
                return;
            }

            username = forenames.trim() + " " + surname.trim();
            ghanaCardNumber = nationalId;

            Users currentUser = userService.findUserByGhanaCard(ghanaCardNumber);
            if (currentUser == null) {
                handleBiometricError("User not registered in system", null, currentUser);
                return;
            }

            userSession.loginUser(currentUser);

            if (userSession.userActive()) {
                // Successful login
                auditLogService.logActivity(
                        AuditActionType.LOGIN,
                        "Biometric Login Page",
                        ActionResult.SUCCESS,
                        currentUser.getUsername() + " MultiFinger Login Successful",
                        currentUser
                );
                FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
                JSF.addSuccessMessageWithSummary("Welcome! ", currentUser.getUsername());

                // Redirect to dashboard
                redirectToDashboard();
            } else {
                // User inactive
                auditLogService.logActivity(
                        AuditActionType.LOGIN,
                        "Biometric Login Page",
                        ActionResult.FAILED,
                        currentUser.getUsername() + " MultiFinger Login Failed - User Inactive",
                        currentUser
                );

                JSF.addErrorMessage("Login Failed - Your account is inactive. Please contact administrator.");
                updateForm();
            }
        } catch (Exception e) {
            handleBiometricError("System error during biometric login: " + e.getMessage(), e, null);

        }
    }

    private void reload() {
        if (socketData != null) {
            // Fetch data from database with socketData and populate captured fingers.
        }
    }

    public void loginWithEmail() {
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("sublogin.xhtml");
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception appropriately
        }
    }

    private void handleBiometricError(String message, Exception e, Users currentUser) {
        // Log the error
        auditLogService.logActivity(
                AuditActionType.LOGIN,
                "Biometric Login Page",
                ActionResult.FAILED,
                "Biometric Login failed for Ghana Card: " + ghanaCardNumber + " - " + message,
                (currentUser != null ? currentUser : null)
        );

        // Show error to user
        JSF.addErrorMessage("Biometric Login Failed: " + message);

        // Print stack trace if exception provided
        if (e != null) {
            e.printStackTrace();
        }

        // Update form
        updateForm();
    }

    private void updateForm() {
        PrimeFaces.current().ajax().update("theForm");
    }

    private void redirectToDashboard() {
        try {
            FacesContext.getCurrentInstance().getExternalContext()
                    .redirect(BASE_URL + "app/dashboard2.xhtml");
        } catch (IOException e) {
            System.err.println("Failed to redirect to dashboard: " + e.getMessage());
            JSF.addErrorMessage("Login successful but failed to redirect. Please navigate manually.");
            updateForm();
        }
    }
}
