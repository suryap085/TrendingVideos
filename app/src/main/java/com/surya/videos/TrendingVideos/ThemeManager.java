package com.surya.videos.TrendingVideos;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

public class ThemeManager {
    private static final String PREF_NAME = "ThemePrefs";
    private static final String KEY_DARK_THEME = "dark_theme_enabled";
    
    private SharedPreferences preferences;
    
    public ThemeManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    public boolean isDarkThemeEnabled() {
        return preferences.getBoolean(KEY_DARK_THEME, false);
    }
    
    public void setDarkThemeEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_DARK_THEME, enabled).apply();
    }
    
    public void toggleTheme() {
        boolean currentTheme = isDarkThemeEnabled();
        setDarkThemeEnabled(!currentTheme);
    }
    
    public static boolean isDarkTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_DARK_THEME, false);
    }
}
