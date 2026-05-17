package com.cureshare.utils;

import javafx.scene.text.Font;

public class FontManager {
    private static boolean loaded = false;
    public static void loadFonts() {
        if (loaded) return;
        loaded = true;
        String[] paths = {
            "/fonts/DM_Sans/DMSans-Regular.ttf",
            "/fonts/DM_Sans/DMSans-Bold.ttf",
            "/fonts/Syne/Syne-Bold.ttf",
            "/fonts/Syne/Syne-ExtraBold.ttf"
        };
        for (String p : paths) {
            try {
                var is = FontManager.class.getResourceAsStream(p);
                if (is != null) { Font.loadFont(is, 14); is.close(); }
            } catch (Exception ignored) {}
        }
    }
}
