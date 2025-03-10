/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Bean;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.margins.STIM.entity.nia_verify.VerificationResultData;
import com.margins.STIM.entity.websocket.FingerCaptured;
import com.margins.STIM.model.CapturedFinger;
import com.margins.STIM.util.FingerprintProcessor;
import com.margins.STIM.util.JSF;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.awt.image.BufferedImage;
import java.io.IOException;
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
import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author PhilipManteAsare
 */
@SessionScoped
@Named("multiFingerBean")
public class multiFingerBean implements Serializable {
 
     @Getter
    private String fingerPosition;
     
    @Getter
    @Setter
    private CapturedFinger capturedFinger = new CapturedFinger();
    
    List<FingerCaptured> capturedFingers = new ArrayList<>();

    @Getter
    @Setter
    VerificationResultData callBack = new VerificationResultData();
    
    @Getter
    @Setter
    private boolean verified = false;
    
    @Setter
    private String socketData;
    
    @Getter
    private StreamedContent fingerImage;
    
    String BASE_URL = JSF.getContextURL() + "/";


    public String requestData(List<FingerCaptured> fingersesList) throws IOException {
        
//        Gson gson = new GsonBuilder().create();
//        String capturedFingersJson = gson.toJson(capturedFingers);
        
        JSONObject data = new JSONObject();
        data.put("fingers", new JSONArray(capturedFingers));
        data.put("merchantCode", "69af98f5-39fb-44e6-81c7-5e496328cc59");
        
//        System.out.println("Captured Finger>>>>>>>>>>>>>>>>>>" + capturedFingersJson);
        
        return data.toString();
    }
    public void sendForVerification()throws IOException {
        try{
            
            if (capturedFingers.isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "No fingerprints captured. Please scan fingers before submitting.", null));
                return;
            }
            
            capturedFingers.add(new FingerCaptured("PNG", "LL", capturedFinger.getLeftLittle()));
            capturedFingers.add(new FingerCaptured("PNG","LR", capturedFinger.getLeftRing()));
            capturedFingers.add(new FingerCaptured("PNG","LM", capturedFinger.getLeftMiddle()));
            capturedFingers.add(new FingerCaptured("PNG","LI", capturedFinger.getLeftIndex()));
            capturedFingers.add(new FingerCaptured("PNG","LT", capturedFinger.getLeftThumb()));
            capturedFingers.add(new FingerCaptured("PNG","RT", capturedFinger.getRightThumb()));
            capturedFingers.add(new FingerCaptured("PNG","RI", capturedFinger.getRightIndex()));
            capturedFingers.add(new FingerCaptured("PNG","RM", capturedFinger.getRightMiddle()));
            capturedFingers.add(new FingerCaptured("PNG","RR", capturedFinger.getRightRing()));
            capturedFingers.add(new FingerCaptured("PNG","RL", capturedFinger.getRightLittle()));
            
            if (capturedFingers.isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "No fingerprints captured. Please scan fingers before submitting.", null));
                return;
            }
            
       
            
            String request = requestData(capturedFingers);
            
            if (request == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed to generate request. No valid fingerprint data.", null));
            return;
        }

//            System.out.println("THE DATA SENT>>>>>>>>>>>>>>>>>>>>>>> \n" + request);
            
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
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "API Error: Bad Request. Please check fingerprint data.", null));
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
                            verified = true;
                            FacesContext.getCurrentInstance().addMessage(null,
                                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Verification Successful!", null));

                            // Redirect to Dashboard
                            FacesContext.getCurrentInstance().getExternalContext().redirect(BASE_URL + "signup.xhtml");
                        } else {
                            verified = false;
                            FacesContext.getCurrentInstance().addMessage(null,
                                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Verification Failed: " + message, null));
                        }
                    } else {
                        verified = false; 
                        FacesContext.getCurrentInstance().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR, "API Error: " + message, null));
                    }
        }
       catch (Exception e) {  // Catch any other unexpected errors
            FacesContext.getCurrentInstance().addMessage(null,
                   new FacesMessage(FacesMessage.SEVERITY_ERROR, "An unexpected error occurred. Please try again!", null));
            System.out.println("ERROR 3");
            e.printStackTrace(); // Log the error for debugging
        }

    }

    public void reset() {
    }

    public String getSocketData() {
        reload();
        return socketData;
    }

    private void reload() {
        if (socketData != null) {
            // Fetch data from database with socketData and populate captured fingers.
        }
    }
   
}
