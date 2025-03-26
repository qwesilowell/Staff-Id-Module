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
import com.margins.STIM.service.BiometricDataService;
import com.margins.STIM.service.User_Service;
import com.margins.STIM.util.FingerprintProcessor;
import com.margins.STIM.util.JSF;
import com.margins.STIM.util.ValidationUtil;
import java.awt.image.BufferedImage;
import java.io.IOException;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import lombok.Getter;
import lombok.Setter;

@Named(value = "biometricBean")
@SessionScoped
public class BiometricLoginBean implements Serializable {

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
    VerificationResultData callBack = new VerificationResultData();

    private byte[] fingerData;
    private String socketData;

    @EJB
    private User_Service userService;

    @EJB
    private BiometricDataService bioService;
    
    String BASE_URL = JSF.getContextURL() + "/";

   

    // Biometric Verification (Fingerprint)
    public void sendForVerification() {
        System.out.println("Selected Finger Position: " + fingerPosition);
        System.out.println("Captured Fingerprint Data: " + capturedFinger);
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

    public void submit() {

        try {

            System.out.println("Sending fingerprint verification request...");
            System.out.println("Ghana Card Number: " + ghanaCardNumber);
            System.out.println("Finger Position: " + fingerPosition);
            System.out.println("Captured Fingerprint: " + (capturedFinger != null ? "Present" : "Missing"));

//            List<Users> foundUsers = userService.findAllUsersByGhanaCard(ghanaCardNumber);
//            if (foundUsers == null || foundUsers.isEmpty()) {
//
//                errorMessage = "User Not Registered";
//                return;
//            }
            System.out.println("GHANACARD2 >>>>>>>>>>>>>> " + ghanaCardNumber);
            if (!ValidationUtil.isValidGhanaCardNumber(ghanaCardNumber)) {
                errorMessage = "Invalid Ghana Card Format";
                return;
            }

            if (capturedFinger == null || capturedFinger.isEmpty()) {
                errorMessage = "No fingerprint captured. Please scan your fingerprint.";
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

            String res  = response.body();
            Gson g = new Gson();
            callBack = g.fromJson(res, VerificationResultData.class);
            System.out.println("Response from API: " + res);
            if (response.statusCode() == 200 && callBack != null) {
                if ("TRUE".equals(callBack.getData().getVerified())) {
                    JSF.addSuccessMessage("Login Successful!");

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

    private void reload() {
        if (socketData != null) {
            // Fetch data from database with socketData and populate captured fingers.
        }
    }
}
