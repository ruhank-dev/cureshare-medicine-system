package com.cureshare.utils;

public class Theme {

    public static final String TEAL_50  = "#e0f7f2";
    public static final String TEAL_100 = "#b3ede0";
    public static final String TEAL_200 = "#7fdcca";
    public static final String TEAL_300 = "#4bcab3";
    public static final String TEAL_400 = "#1fbda0";
    public static final String TEAL_500 = "#00b090";
    public static final String TEAL_600 = "#009e7f";
    public static final String TEAL_700 = "#008a6a";
    public static final String TEAL_800 = "#007656";
    public static final String TEAL_900 = "#005540";

    public static final String ACCENT       = "#00b090";
    public static final String ACCENT_LIGHT = "#e0f7f2";
    public static final String ACCENT_GLOW  = "#00e0b8";

    public static final String SUCCESS = "#22c97e";
    public static final String WARNING = "#f0a500";
    public static final String DANGER  = "#e05252";
    public static final String INFO    = "#378add";

    public static final String BG_PAGE    = "#f0faf7";
    public static final String SIDEBAR_BG = "#00473a";

    public static final String TEXT_PRIMARY   = "#0d2e28";
    public static final String TEXT_SECONDARY = "#2e7060";
    public static final String TEXT_MUTED     = "#7ab5a8";

    public static final double SIDEBAR_WIDTH = 240;
    public static final double HEADER_HEIGHT = 64;

    public static final String FONT_HEADING = "Syne";
    public static final String FONT_BODY    = "DM Sans";

    // ── Style helpers ─────────────────────────────────────────────────────
    public static String glassCardStyle() {
        return "-fx-background-color: rgba(255,255,255,0.60);" +
               "-fx-background-radius: 20;" +
               "-fx-border-color: rgba(255,255,255,0.72);" +
               "-fx-border-width: 1;" +
               "-fx-border-radius: 20;" +
               "-fx-effect: dropshadow(gaussian, rgba(0,150,120,0.10), 8, 0, 0, 3);";
    }
    public static String glassDarkStyle() {
        return "-fx-background-color: rgba(0,80,64,0.09);" +
               "-fx-background-radius: 14;" +
               "-fx-border-color: rgba(0,176,144,0.16);" +
               "-fx-border-width: 1;" +
               "-fx-border-radius: 14;";
    }
    public static String primaryButtonStyle() {
        return "-fx-background-color: linear-gradient(to bottom right, #00b090, #007056);" +
               "-fx-text-fill: white;" +
               "-fx-font-family: '" + FONT_BODY + "';" +
               "-fx-font-size: 13px;" +
               "-fx-font-weight: bold;" +
               "-fx-background-radius: 40;" +
               "-fx-padding: 10 22 10 22;" +
               "-fx-cursor: hand;" +
               "-fx-effect: dropshadow(gaussian, rgba(0,176,144,0.30), 6, 0, 0, 2);";
    }
    public static String primaryButtonHoverStyle() {
        return "-fx-background-color: linear-gradient(to bottom right, #00c8a5, #008a6a);" +
               "-fx-text-fill: white;" +
               "-fx-font-family: '" + FONT_BODY + "';" +
               "-fx-font-size: 13px;" +
               "-fx-font-weight: bold;" +
               "-fx-background-radius: 40;" +
               "-fx-padding: 10 22 10 22;" +
               "-fx-cursor: hand;" +
               "-fx-effect: dropshadow(gaussian, rgba(0,176,144,0.45), 8, 0, 0, 3);";
    }
    public static String glassButtonStyle() {
        return "-fx-background-color: rgba(255,255,255,0.65);" +
               "-fx-text-fill: #008a6a;" +
               "-fx-font-family: '" + FONT_BODY + "';" +
               "-fx-font-size: 13px;" +
               "-fx-font-weight: bold;" +
               "-fx-background-radius: 40;" +
               "-fx-border-color: rgba(0,176,144,0.28);" +
               "-fx-border-width: 1;" +
               "-fx-border-radius: 40;" +
               "-fx-padding: 10 22 10 22;" +
               "-fx-cursor: hand;";
    }
    public static String glassButtonHoverStyle() {
        return "-fx-background-color: rgba(255,255,255,0.92);" +
               "-fx-text-fill: #005540;" +
               "-fx-font-family: '" + FONT_BODY + "';" +
               "-fx-font-size: 13px;" +
               "-fx-font-weight: bold;" +
               "-fx-background-radius: 40;" +
               "-fx-border-color: rgba(0,176,144,0.50);" +
               "-fx-border-width: 1.5;" +
               "-fx-border-radius: 40;" +
               "-fx-padding: 10 22 10 22;" +
               "-fx-cursor: hand;";
    }
    public static String inputStyle() {
        return "-fx-background-color: rgba(255,255,255,0.70);" +
               "-fx-border-color: rgba(0,176,144,0.22);" +
               "-fx-border-width: 1;" +
               "-fx-border-radius: 10;" +
               "-fx-background-radius: 10;" +
               "-fx-padding: 10 14 10 14;" +
               "-fx-font-family: '" + FONT_BODY + "';" +
               "-fx-font-size: 13px;" +
               "-fx-text-fill: #0d2e28;";
    }
    public static String inputFocusStyle() {
        return "-fx-background-color: rgba(255,255,255,0.93);" +
               "-fx-border-color: #00b090;" +
               "-fx-border-width: 1.5;" +
               "-fx-border-radius: 10;" +
               "-fx-background-radius: 10;" +
               "-fx-padding: 10 14 10 14;" +
               "-fx-font-family: '" + FONT_BODY + "';" +
               "-fx-font-size: 13px;" +
               "-fx-text-fill: #0d2e28;" +
               "-fx-effect: dropshadow(gaussian, rgba(0,176,144,0.12), 4, 0, 0, 0);";
    }
    public static String labelStyle(String size, String color, boolean bold) {
        return "-fx-font-family: '" + FONT_BODY + "';" +
               "-fx-font-size: " + size + ";" +
               "-fx-text-fill: " + color + ";" +
               (bold ? "-fx-font-weight: bold;" : "");
    }
    public static String headingStyle(String size) {
        return "-fx-font-family: '" + FONT_HEADING + "';" +
               "-fx-font-size: " + size + ";" +
               "-fx-text-fill: " + TEXT_PRIMARY + ";" +
               "-fx-font-weight: 800;";
    }
    public static String sidebarHeadingStyle() {
        return "-fx-font-family: '" + FONT_HEADING + "';" +
               "-fx-font-size: 20px;" +
               "-fx-text-fill: white;" +
               "-fx-font-weight: 800;";
    }
    public static String badgeStyle(String bg, String fg) {
        return "-fx-background-color: " + bg + ";" +
               "-fx-text-fill: " + fg + ";" +
               "-fx-font-size: 11px;" +
               "-fx-font-weight: bold;" +
               "-fx-background-radius: 20;" +
               "-fx-padding: 3 10 3 10;" +
               "-fx-font-family: '" + FONT_BODY + "';";
    }
    public static String badgeSuccess() { return badgeStyle("rgba(34,201,126,0.14)", "#127a50"); }
    public static String badgeWarning() { return badgeStyle("rgba(240,165,0,0.14)",   "#8a5c00"); }
    public static String badgeDanger()  { return badgeStyle("rgba(224,82,82,0.14)",   "#a02020"); }
    public static String badgeInfo()    { return badgeStyle("rgba(55,138,221,0.14)",  "#0a4080"); }
    public static String badgePending() { return badgeStyle("rgba(100,120,240,0.14)", "#3040b0"); }
}
