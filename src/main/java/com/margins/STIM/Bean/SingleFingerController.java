/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Bean;

import com.google.gson.Gson;
import com.margins.STIM.entity.model.VerificationRequest;
import com.margins.STIM.entity.nia_verify.VerificationResultData;
import com.margins.STIM.service.BiometricDataService;
import com.margins.STIM.service.User_Service;
import com.margins.STIM.util.FingerprintProcessor;
import com.margins.STIM.util.JSF;
import static com.margins.STIM.util.JSF.context;
import com.margins.STIM.util.ValidationUtil;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 *
 * @author PhilipManteAsare
 */

@Named("singlefingerBean")
@SessionScoped
public class SingleFingerController implements Serializable {
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

            System.out.println("GHANACARD2 >>>>>>>>>>>>>> " + ghanaCardNumber);
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
                    JSF.addSuccessMessage("Single Finger Verification Successful!");
                    // Redirect to Employee SignUp Page
                    FacesContext.getCurrentInstance().getExternalContext().redirect(BASE_URL + "app/dashboard2.xhtml");
                } else {
                    JSF.addErrorMessage("Fingerprint Verification Failed!");                }
            } else {
                JSF.errorMessage("Verification Error:" + (callBack != null ? callBack.msg : "No response from server"));
            }
        } 
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
