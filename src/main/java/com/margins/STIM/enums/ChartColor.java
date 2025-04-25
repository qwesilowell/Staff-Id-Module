/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.margins.STIM.enums;

/**
 *
 * @author BryanAnyanful
 */
public enum ChartColor {
    BLUE("rgb(49, 122, 193)", "rgba(49, 122, 193, 0.2)"),
    YELLOW("rgb(255, 205, 86)", "rgba(255, 205, 86, 0.2)"),
    GREEN("rgb(64, 175, 115)", "rgba(64, 175, 115, 0.2)"),
    RED("rgb(216, 63, 63)", "rgba(216, 63, 63, 0.2)"),
    PURPLE("rgb(153, 102, 255)", "rgba(153, 102, 255, 0.2)"),
    ORANGE("rgb(255, 159, 64)", "rgba(255, 159, 64, 0.2)"),
    TEAL("rgb(75, 192, 192)", "rgba(75, 192, 192, 0.2)"),
    GRAY("rgb(170, 170, 170)", "rgba(170, 170, 170, 0.2)");

    private final String colorCode;
    private final String colorCodeWithOpacity;

    ChartColor(String colorCode, String colorCodeWithOpacity) {
        this.colorCode = colorCode;
        this.colorCodeWithOpacity = colorCodeWithOpacity;
    }

    @Override
    public String toString() {
        return colorCode;
    }

    public String withOpacity() {
        return colorCodeWithOpacity;
    }
}
