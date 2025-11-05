package com.example.tasks.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.tasks.R;

/**
 * 设置Fragment
 */
public class SettingsFragment extends PreferenceFragmentCompat {
    
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        
        // 设置API密钥的摘要显示
        EditTextPreference apiKeyPref = findPreference("deepseek_api_key");
        if (apiKeyPref != null) {
            apiKeyPref.setSummaryProvider(preference -> {
                String value = ((EditTextPreference) preference).getText();
                if (value == null || value.isEmpty()) {
                    return "未配置";
                } else {
                    // 只显示前几位和后几位，中间用*号代替
                    if (value.length() > 8) {
                        return value.substring(0, 4) + "****" + value.substring(value.length() - 4);
                    } else {
                        return "****";
                    }
                }
            });
        }
        
        // 设置主题切换
        ListPreference themePref = findPreference("theme_mode");
        if (themePref != null) {
            themePref.setOnPreferenceChangeListener((preference, newValue) -> {
                String themeValue = (String) newValue;
                applyTheme(themeValue);
                return true;
            });
        }
        
        // 设置AI帮助点击事件
        Preference aiHelpPref = findPreference("ai_help");
        if (aiHelpPref != null) {
            aiHelpPref.setOnPreferenceClickListener(preference -> {
                // 可以在这里打开帮助页面或显示对话框
                return true;
            });
        }
    }
    
    /**
     * 应用主题设置
     */
    private void applyTheme(String themeValue) {
        switch (themeValue) {
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "system":
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }
}