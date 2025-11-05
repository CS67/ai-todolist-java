package com.example.tasks.preferences;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * AI设置管理器
 */
public class AIPreferences {
    private static final String PREFS_NAME = "ai_preferences";
    private static final String KEY_API_KEY = "deepseek_api_key";
    private static final String KEY_AI_ENABLED = "ai_enabled";
    
    private final SharedPreferences prefs;
    
    public AIPreferences(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * 保存API密钥
     */
    public void saveApiKey(String apiKey) {
        prefs.edit()
                .putString(KEY_API_KEY, apiKey)
                .putBoolean(KEY_AI_ENABLED, apiKey != null && !apiKey.trim().isEmpty())
                .apply();
    }
    
    /**
     * 获取API密钥
     */
    public String getApiKey() {
        return prefs.getString(KEY_API_KEY, null);
    }
    
    /**
     * 检查AI是否已启用
     */
    public boolean isAIEnabled() {
        boolean enabled = prefs.getBoolean(KEY_AI_ENABLED, false);
        String apiKey = getApiKey();
        return enabled && apiKey != null && !apiKey.trim().isEmpty();
    }
    
    /**
     * 清除API密钥
     */
    public void clearApiKey() {
        prefs.edit()
                .remove(KEY_API_KEY)
                .putBoolean(KEY_AI_ENABLED, false)
                .apply();
    }
}