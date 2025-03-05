/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class CapturedFinger {
    private String leftIndex;
    private String leftMiddle;
    private String leftRing;
    private String leftLittle;
    private String leftThumb;
    private String rightIndex;
    private String rightMiddle;
    private String rightRing;
    private String rightLittle;
    private String rightThumb;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CapturedFinger{");
        sb.append("leftIndex=").append(leftIndex);
        sb.append(", leftMiddle=").append(leftMiddle);
        sb.append(", leftRing=").append(leftRing);
        sb.append(", leftLittle=").append(leftLittle);
        sb.append(", leftThumb=").append(leftThumb);
        sb.append(", rightIndex=").append(rightIndex);
        sb.append(", rightMiddle=").append(rightMiddle);
        sb.append(", rightRing=").append(rightRing);
        sb.append(", rightLittle=").append(rightLittle);
        sb.append(", rightThumb=").append(rightThumb);
        sb.append('}');
        return sb.toString();
    }

    
    
}
