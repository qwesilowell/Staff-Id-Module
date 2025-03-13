/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Bean;

import com.google.gson.Gson;
import com.margins.STIM.entity.Employee;
import com.margins.STIM.entity.model.VerificationRequest;
import com.margins.STIM.entity.nia_verify.VerificationResultData;
import com.margins.STIM.service.BiometricDataService;
import com.margins.STIM.service.Employee_Service;
import com.margins.STIM.util.DateFormatter;
import com.margins.STIM.util.FingerprintProcessor;
import com.margins.STIM.util.JSF;
import com.margins.STIM.util.ValidationUtil;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.ZoneId;
import java.util.Date;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.PrimeFaces;

/**
 *
 * @author PhilipManteAsare
 */
@Named("wizardBean")
@SessionScoped
public class onboardEmployeeController implements Serializable {

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
    private Employee_Service employeeService;

    @EJB
    private BiometricDataService bioService;

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
    private String address;
    
    @Getter
    @Setter
    private String email= "try123@hotmail.com";
    

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

                    verificationSuccess = true;
                    currentStep++;

                    empImage = "data:image/png;base64," + callBack.data.person.biometricFeed.face.data;
                    forenames = callBack.data.person.forenames;
                    surname = callBack.data.person.surname;
                    employeeName = callBack.data.person.forenames + " " + callBack.data.person.surname;
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
                JSF.errorMessage("Verification Error:" + (callBack != null ? callBack.msg : "No response from server"));
            }
        } catch (Exception e) {  // Catch any other unexpected errors
            JSF.addErrorMessage("An unexpected error occurred. Please try again!");
            System.out.println("ERROR 3");
            e.printStackTrace(); // Log the error for debugging
        }

    }
    
    

    public void saveEmployeeToDatabase() {
        if (!verificationSuccess) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Verification not completed!", null));
            return;
        }

        try {
            Employee newEmployee = new Employee();
            newEmployee.setGhanaCardNumber(ghanaCardNumber);
            newEmployee.setFirstname(forenames);
            newEmployee.setLastname(surname);
            newEmployee.setDateOfBirth(empDOB.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            newEmployee.setGender(gender);
            newEmployee.setAddress(address); 
            newEmployee.setEmail(email);

            employeeService.saveEmployee(newEmployee); //

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Employee saved successfully!", null));
            
            FacesContext.getCurrentInstance().getExternalContext().redirect(BASE_URL + "app/employeeList");

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error saving employee: " + e.getMessage(), null));
            e.printStackTrace();
        }
    }
    
    private void reload() {
        if (socketData != null) {
            // Fetch data from database with socketData and populate captured fingers.
        }
    }

    // Step 4: Submit
    public void submitE() {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Employee Registered Successfully!", null));
    }
}
