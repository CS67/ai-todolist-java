package com.example.tasks.ui.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;

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
        
        // 设置API密钥的摘要显示和密码输入
        EditTextPreference apiKeyPref = findPreference("deepseek_api_key");
        if (apiKeyPref != null) {
            // 设置输入框为密码类型
            apiKeyPref.setOnBindEditTextListener(editText -> {
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            });
            
            // 设置摘要显示（隐藏真实密钥）
            apiKeyPref.setSummaryProvider(preference -> {
                String value = ((EditTextPreference) preference).getText();
                if (value == null || value.isEmpty()) {
                    return "未配置API密钥";
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
        
        // 设置AI帮助点击事件 - 显示教学对话框
        Preference aiHelpPref = findPreference("ai_help");
        if (aiHelpPref != null) {
            aiHelpPref.setOnPreferenceClickListener(preference -> {
                showApiKeyHelp();
                return true;
            });
        }
    }
    
    /**
     * 显示API密钥获取教程
     */
    private void showApiKeyHelp() {
        String helpMessage = "📝 获取DeepSeek API密钥步骤：\n\n" +
                "1️⃣ 访问官网\n" +
                "   打开浏览器访问：\n" +
                "   https://platform.deepseek.com\n\n" +
                "2️⃣ 注册/登录账号\n" +
                "   使用邮箱或手机号注册\n\n" +
                "3️⃣ 进入API管理\n" +
                "   登录后点击「API Keys」\n\n" +
                "4️⃣ 创建新密钥\n" +
                "   点击「Create API Key」按钮\n\n" +
                "5️⃣ 复制密钥\n" +
                "   复制生成的密钥并粘贴到本应用\n\n" +
                "⚠️ 注意事项：\n" +
                "• 密钥只显示一次，请妥善保存\n" +
                "• 不要将密钥分享给他人\n" +
                "• DeepSeek提供免费额度供测试使用";
        
        new AlertDialog.Builder(requireContext())
                .setTitle("🤖 如何获取API密钥")
                .setMessage(helpMessage)
                .setPositiveButton("我知道了", null)
                .setNeutralButton("复制网址", (dialog, which) -> {
                    android.content.ClipboardManager clipboard = 
                        (android.content.ClipboardManager) requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("DeepSeek URL", "https://platform.deepseek.com");
                    clipboard.setPrimaryClip(clip);
                    android.widget.Toast.makeText(requireContext(), "网址已复制到剪贴板", android.widget.Toast.LENGTH_SHORT).show();
                })
                .show();
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