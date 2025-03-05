/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.margins.STIM.entity.model;

/**
 *
 * @author Darlington
 */
public class VerificationRequest {

    private String pinNumber;

    private String image;

    private String dataType;

    private String center;

    private String merchantKey;
    
    private String merchantCode;
    
    private String position;
    
    private String userUD;
    
    private String deviceOs;

    public VerificationRequest() {
    }

    public String getPinNumber() {
        return pinNumber;
    }

    public void setPinNumber(String pinNumber) {
        this.pinNumber = pinNumber;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getCenter() {
        return center;
    }

    public void setCenter(String center) {
        this.center = center;
    }

    public String getMerchantKey() {
        return merchantKey;
    }

    public void setMerchantKey(String merchantKey) {
        this.merchantKey = merchantKey;
    }

    public String getUserUD() {
        return userUD;
    }

    public void setUserUD(String userUD) {
        this.userUD = userUD;
    }

    public String getDeviceOs() {
        return deviceOs;
    }

    public void setDeviceOs(String deviceOs) {
        this.deviceOs = deviceOs;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getMerchantCode() {
        return merchantCode;
    }

    public void setMerchantCode(String merchantCode) {
        this.merchantCode = merchantCode;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("VerificationRequest{");
        sb.append("pinNumber=").append(pinNumber);
        sb.append(", image=").append(image);
        sb.append(", dataType=").append(dataType);
        sb.append(", center=").append(center);
        sb.append(", merchantKey=").append(merchantKey);
        sb.append(", merchantCode=").append(merchantCode);
        sb.append(", position=").append(position);
        sb.append(", userUD=").append(userUD);
        sb.append(", deviceOs=").append(deviceOs);
        sb.append('}');
        return sb.toString();
    }
    
    


}
