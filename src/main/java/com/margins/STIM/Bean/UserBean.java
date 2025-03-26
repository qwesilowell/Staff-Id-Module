package com.margins.STIM.Bean;

import com.google.gson.Gson;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.validation.constraints.NotEmpty;
import java.io.Serializable;
import jakarta.ejb.EJB;
import com.margins.STIM.service.User_Service;
import com.margins.STIM.entity.Users;
import com.margins.STIM.entity.model.VerificationRequest;
import com.margins.STIM.entity.nia_verify.VerificationResultData;
import com.margins.STIM.entity.websocket.FingerCaptured;
import com.margins.STIM.model.CapturedFinger;
import com.margins.STIM.util.DateFormatter;
import com.margins.STIM.util.FingerprintProcessor;
import com.margins.STIM.util.JSF;
import com.margins.STIM.util.ValidationUtil;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.primefaces.PrimeFaces;
import org.primefaces.model.StreamedContent;

@Named("userBean")
@SessionScoped
public class UserBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private User_Service userService;

    @Getter
    @Setter
    private String ghanaCardNumber;

    private String username;

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

    private byte[] fingerData;

    @Setter
    private String socketData;

    @Getter
    private StreamedContent fingerImage;

  

    String BASE_URL = JSF.getContextURL() + "/";

    private int currentStep = 0;
    private String verificationType;
    private boolean verificationSuccess = false;

    @Getter
    @Setter
    private String empImage;

    @Getter
    @Setter
    private String employeeName;

    @Getter
    @Setter
    private Date empDOB;

    @Getter
    @Setter
    private String employeeDOB;

    @Getter
    @Setter
    private String forenames;

    @Getter
    @Setter
    private String surname;

    @Getter
    @Setter
    private String nationality;

    @Getter
    @Setter
    private String gender;

    @Getter
    @Setter
    private String faceImageData;

//    @NotEmpty(message = "Please select a Role")
    private String userRole;

    @Getter
    @Setter
    private List<Users> users = new ArrayList<>();

    public void getUserss() {
        users = userService.findAllUsers();
        System.out.println("USERS>>>>>>>>>>>>>> " + users.toString());
    }

    

    private void clearForm() {
        this.ghanaCardNumber = null;
        this.username = null;
        this.userRole = null;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    // Step Navigation
    public int getCurrentStep() {
        return currentStep;
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

    // Step 1: Selecting Verification Type
    public String getVerificationType() {
        return verificationType;
    }

    public void setVerificationType(String verificationType) {
        this.verificationType = verificationType;
    }

    // Step 2: Perform Verification
    public boolean isVerificationSuccess() {
        return verificationSuccess;
    }


    // Reset Fingerprint Selection
    public void reset() {
        capturedFinger = null;
        fingerPosition = null;

    }

    public void resetMulti() {
    }

    public String getSocketData() {
        reload();
        return socketData;
    }

    public void submit() {

        try {
            if (ghanaCardNumber == null || ghanaCardNumber.isBlank() || !ValidationUtil.isValidGhanaCardNumber(ghanaCardNumber)) {
                JSF.addErrorMessage("Invalid Ghana Card Format");
                return;
            }

            if (capturedFinger == null || capturedFinger.isEmpty()) {
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
            if (response.statusCode() == 200 && callBack != null) {
                if ("TRUE".equals(callBack.getData().getVerified())) {
                    JSF.addSuccessMessage("Single Finger Verification Successful!");

                    verificationSuccess = true;
                    currentStep++;

                    empImage = "data:image/png;base64," + callBack.data.person.biometricFeed.face.data;
                    forenames = callBack.data.person.forenames;
                    surname = callBack.data.person.surname;
                    username = callBack.data.person.forenames + " " + callBack.data.person.surname;
                    employeeDOB = DateFormatter.formatDate(callBack.data.person.birthDate);
                    empDOB = callBack.data.person.birthDate;
                    nationality = callBack.data.person.nationality;
                    gender = callBack.data.person.gender;

                    PrimeFaces.current().ajax().update("wizardForm");

//                    FacesContext.getCurrentInstance().getExternalContext().redirect(BASE_URL + "signup.xhtml");
                } else {
                    JSF.addErrorMessage("Fingerprint Verification Failed!");
                }
            } else {
                JSF.addErrorMessage("Verification Error:" + (callBack != null ? callBack.msg : "No response from server"));
            }
        } catch (Exception e) {  // Catch any other unexpected errors
            JSF.addErrorMessage("An unexpected error occurred. Please try again!");
            System.out.println("ERROR 3");
            e.printStackTrace(); // Log the error for debugging
        }

    }

//    <for multi finger>
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

//                    verificationSuccess = true;
                    currentStep++;

                    empImage = "data:image/png;base64," + callBack.data.person.biometricFeed.face.data;
                    forenames = callBack.data.person.forenames;
                    surname = callBack.data.person.surname;
                    username = callBack.data.person.forenames + " " + callBack.data.person.surname;
                    employeeDOB = DateFormatter.formatDate(callBack.data.person.birthDate);
                    empDOB = callBack.data.person.birthDate;
                    nationality = callBack.data.person.nationality;
                    gender = callBack.data.person.gender;

                    PrimeFaces.current().ajax().update("wizardForm");

                } else {
                    verificationSuccess = false;
                    JSF.addErrorMessage("Verification Failed: " + message);
                }
            } else {
                verificationSuccess = false;
                JSF.addErrorMessage("Verification Failed Reason: " + message);
            }
        } catch (Exception e) {  // Catch any other unexpected errors
            JSF.addErrorMessage("An unexpected error occurred. Please try again!");
            System.out.println("ERROR 3");
            e.printStackTrace(); // Log the error for debugging
        }

    }

    public void saveUserToDatabase() {
        if (!verificationSuccess) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Verification not completed!", null));
            return;
        }

        // Check if Ghana Card Number is already registered
        if (userService.findUserByGhanaCard(ghanaCardNumber) != null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Employee with Ghana Card Number already registered", null));
            return;
        }
        if (userRole == null || userRole.isBlank()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select a user role before proceeding.", null));
            return;
        }
        

        try {

            // Create new user entity
            Users newUser = new Users();
            newUser.setGhanaCardNumber(ghanaCardNumber);
            newUser.setUsername(username);  
            newUser.setUserRole(userRole);

            // Save user to database
            userService.createUser(newUser);

            // Show success message using flash scope
            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "User " + username + " created successfully!", null));
            
            
            // Clear form fields
            clearForm();

            // Redirect to login page
            FacesContext.getCurrentInstance().getExternalContext().redirect(BASE_URL + "login.xhtml");

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error creating new user: " + e.getMessage(), null));
            e.printStackTrace();
        }
    }


    public void verifyFace() {
        try {
            if (ghanaCardNumber == null || ghanaCardNumber.isBlank() || !ValidationUtil.isValidGhanaCardNumber(ghanaCardNumber)) {
                JSF.addErrorMessage("Invalid Ghana Card Format");
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
            if (response.statusCode() == 200 && callBack != null) {
                if ("TRUE".equals(callBack.getData().getVerified())) {
                    verificationSuccess = true;
                    JSF.addSuccessMessage("Facial Verification Successful!");

//                    verificationSuccess = true;
                    currentStep++;

                    empImage = "data:image/png;base64," + callBack.data.person.biometricFeed.face.data;
                    forenames = callBack.data.person.forenames;
                    surname = callBack.data.person.surname;
                    username = callBack.data.person.forenames + " " + callBack.data.person.surname;
                    employeeDOB = DateFormatter.formatDate(callBack.data.person.birthDate);
                    empDOB = callBack.data.person.birthDate;
                    nationality = callBack.data.person.nationality;
                    gender = callBack.data.person.gender;

                    PrimeFaces.current().ajax().update("wizardForm");

                } else {
                    verificationSuccess = false;
                    JSF.addErrorMessage("Verification Failed: " + callBack.getMsg());
                }
            } else {
                verificationSuccess = false;
                JSF.addErrorMessage("Verification Failed Reason: " + callBack.getMsg());
            }
        } catch (Exception e) {  // Catch any other unexpected errors
            JSF.addErrorMessage("An unexpected error occurred. Please try again!");
            System.out.println("ERROR 3");
            e.printStackTrace(); // Log the error for debugging
        }

    }

    private void reload() {
        if (socketData != null) {
            // Fetch data from database with socketData and populate captured fingers.
        }
    }
}
