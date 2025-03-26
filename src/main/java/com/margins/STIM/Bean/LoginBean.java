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
import com.margins.STIM.entity.Users;
import com.margins.STIM.entity.model.VerificationRequest;
import com.margins.STIM.entity.nia_verify.VerificationResultData;
import com.margins.STIM.entity.websocket.FingerCaptured;
import com.margins.STIM.model.CapturedFinger;
import com.margins.STIM.service.BiometricDataService;
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
import java.io.IOException;
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
        
        private Boolean verificationSuccess;
        
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
        private BiometricDataService bioService;

        String BASE_URL = JSF.getContextURL() + "/";
        
        
        public void redirectIfNotLoggedIn() {
        if (username == null) {
            try{
                FacesContext.getCurrentInstance().getExternalContext().redirect(BASE_URL + "login.xhtml");
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }

        // Regular Login with Password
//        public String login() {
//            boolean isValid = userService.validateUser(ghanaCardNumber, password);
//
//            if (isValid) {
//                FacesContext.getCurrentInstance().addMessage(null,
//                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Login successful!", null));
//                return "/app/dashboard2.xhtml?faces-redirect=true";
//            } else {
//                FacesContext.getCurrentInstance().addMessage(null,
//                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Wrong Credentials", null));
//                return null;
//            }
//        }


        // Reset Fingerprint Selection
        public void reset() {
            capturedFinger = null;
            fingerPosition = null;
            ghanaCardNumber= null;

        }

        public String getSocketData() {
            reload();
            return socketData;
        }

        public void submit() {

            try {

                if (ghanaCardNumber == null || ghanaCardNumber.isBlank()) {
                    JSF.addErrorMessage("Ghana Card Number must be filled");
                    return;
                }

                                
                if (!ValidationUtil.isValidGhanaCardNumber(ghanaCardNumber)) {
                    JSF.addErrorMessage("Invalid Ghana Card Format");
                    return;
                }
                
                Users foundUser = userService.findUserByGhanaCard(ghanaCardNumber);
                if (foundUser == null) {
                    JSF.addErrorMessage("User Not Registered");
                    return;
                }
 

                if (capturedFinger == null || capturedFinger.isEmpty()) {
                   JSF.addErrorMessage("No fingerprint captured. Please scan your fingerprint.");
                    return;
                }

//            BiometricData bioData = bioService.findBiometricDataById(socketData);
//            fingerData = bioData.getFingerprintData().getBytes();
//        
//        if (capturedFinger == null) {
//            errorMessage = "Fingerprint data is missing!";
//            return;
//        }
                System.out.println("GHANACARD3 >>>>>>>>>>>>>> " + ghanaCardNumber);

                VerificationRequest request = new VerificationRequest();
                request.setPosition(fingerPosition);
                request.setPinNumber(ghanaCardNumber);

                String processedImage = FingerprintProcessor.imageDpi(capturedFinger);

//            System.out.println("processedFINGER >>>>>>>>>>>>>> " + processedImage);
//            BufferedImage bi = Functions.createImageFromBytes(fingerData);
//            request.setImage(org.apache.commons.codec.binary.Base64.encodeBase64String(Functions.processData(bi)));
                //request.setImage(capturedFinger);
                request.setImage(processedImage);
//
//            request.setMerchantKey("69af98f5-39fb-44e6-81c7-5e496328cc59");
//            request.setMerchantCode("69af98f5-39fb-44e6-81c7-5e496328cc59");

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

//            HttpResponse<String> response = Unirest.post("https://cscdc.online/apis/test-api.php")
//                    .header("Content-Type", "application/json")
//                    .body(request)
//                    .asString();

                System.out.println("Response Status: " + response.statusCode());
                System.out.println("Response Body: " + response.body());

                String res = response.body();
                Gson g = new Gson();
                callBack = g.fromJson(res, VerificationResultData.class);
                System.out.println("Response from API: " + res);
                if (response.statusCode() == 200 && callBack != null) {
                    if ("TRUE".equals(callBack.getData().getVerified())) {
                        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
                        FacesContext.getCurrentInstance().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_INFO,"Login Successful!" ,null));
                        
                        String forenames = callBack.data.person.forenames;
                        String surname = callBack.data.person.surname;
                        username = forenames + " " + surname;
                        ghanaCardNumber = callBack.data.person.nationalId;
                        
                        userRole = foundUser.getUserRole();
                        
                        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("username", username);
                        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("userRole", userRole);
                        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("ghanaCardNumber", ghanaCardNumber);
                        

                        // Redirect to Dashboard
                        FacesContext.getCurrentInstance().getExternalContext().redirect(BASE_URL + "app/dashboard2.xhtml");
                    } else {
                        JSF.addErrorMessage("Fingerprint Verification Failed!");
                    }
                } else {
                    JSF.addErrorMessage("Fingerprint Verification Failed!" + (callBack != null ? callBack.msg : "No response from server"));

                }
            } //        catch (IOException e) {
            //            System.out.println("ERROR 1");
            //            errorMessage = "IO Exception occurred while processing your request!";
            //            e.printStackTrace(); // Log the error for debugging
            //        } 
            catch (Exception e) {  // Catch any other unexpected errors
                JSF.addErrorMessage("An unexpected error occurred. Please try again!");
                System.out.println("ERROR 3");
                e.printStackTrace(); // Log the error for debugging
            }

        }
        
        
        
        
        public void verifyFace() {
        try {
            if (ghanaCardNumber == null || ghanaCardNumber.isBlank()) {
                JSF.addErrorMessage("Ghana Card Number must be filled");
                return;
            }

            if (!ValidationUtil.isValidGhanaCardNumber(ghanaCardNumber)) {
                JSF.addErrorMessage("Invalid Ghana Card Format");
                return;
            }

            Users foundUser = userService.findUserByGhanaCard(ghanaCardNumber);
            if (foundUser == null) {
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
            if (response.statusCode() == 200 && callBack != null) {
                if ("TRUE".equals(callBack.getData().getVerified())) {
                    verificationSuccess = true;
                    
                    FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO, "Login Successful!", null));
                    
                    String forenames = callBack.data.person.forenames;
                    String surname = callBack.data.person.surname;
                    username = forenames + " " + surname;
                    ghanaCardNumber = callBack.data.person.nationalId;

                    userRole = foundUser.getUserRole();

                    FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("username", username);
                    FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("userRole", userRole);
                    FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("ghanaCardNumber", ghanaCardNumber);

                    FacesContext.getCurrentInstance().getExternalContext().redirect(BASE_URL + "app/dashboard2.xhtml");
                    PrimeFaces.current().ajax().update("theForm");
                    
                    msg = callBack.msg.toString();

                } else {
                    verificationSuccess = false;
                    System.out.println("FAILED>>>>>>");
                    JSF.addErrorMessage("Verification Failed: " + msg);
                }
//            } else {
//                verificationSuccess = false;
//                JSF.addErrorMessage("Verification Failed Reason: " + callBack.getMsg());
           }
        } catch (Exception e) {  // Catch any other unexpected errors
            JSF.addErrorMessage("An unexpected error occurred. Please try again!");
            System.out.println("ERROR 3");
            e.printStackTrace(); // Log the error for debugging
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
                    FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO, "Login Successful!", null));

//
                         FacesContext.getCurrentInstance().getExternalContext().redirect(BASE_URL + "app/dashboard2.xhtml");
//                    PrimeFaces.current().ajax().update("wizardForm");

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
    public String getUserInitials() {
        String username = (String) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("username");

        if (username == null || username.trim().isEmpty()) {
            return "??"; // Return default initials if username is missing
        }

        // Trim and split the name
        String[] nameParts = username.trim().split("\\s+"); // Split by one or more spaces
        if (nameParts.length == 1) {
            return nameParts[0].substring(0, 1).toUpperCase(); // Single name case
        }

        // Take first letter of first and last name
        return nameParts[0].substring(0, 1).toUpperCase() + nameParts[nameParts.length - 1].substring(0, 1).toUpperCase();
    }

        private void reload() {
            if (socketData != null) {
                // Fetch data from database with socketData and populate captured fingers.
            }
        }
    }
