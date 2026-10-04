package com.example.data.api

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig

object ApiConfigManager {
    private const val PREFS_NAME = "zyxo_api_config"
    private const val KEY_CUSTOM_API_KEY = "custom_api_key"
    private const val KEY_CUSTOM_BASE_URL = "custom_base_url"

    const val DEFAULT_BASE_URL = "https://generativelanguage.googleapis.com/"

    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    private fun getPrefs(context: Context? = appContext): SharedPreferences? {
        val ctx = context ?: appContext ?: return null
        return ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getApiKey(context: Context? = null): String {
        val prefs = getPrefs(context)
        val custom = prefs?.getString(KEY_CUSTOM_API_KEY, "")?.trim() ?: ""
        if (custom.isNotBlank()) {
            return custom
        }
        val buildKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
        if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") {
            return buildKey
        }
        return ""
    }

    fun saveApiKey(context: Context, key: String) {
        getPrefs(context)?.edit()?.putString(KEY_CUSTOM_API_KEY, key.trim())?.apply()
    }

    fun getBaseUrl(context: Context? = null): String {
        val prefs = getPrefs(context)
        val custom = prefs?.getString(KEY_CUSTOM_BASE_URL, "")?.trim() ?: ""
        if (custom.isNotBlank()) {
            return if (custom.endsWith("/")) custom else "$custom/"
        }
        return DEFAULT_BASE_URL
    }

    fun saveBaseUrl(context: Context, url: String) {
        getPrefs(context)?.edit()?.putString(KEY_CUSTOM_BASE_URL, url.trim())?.apply()
    }

    fun resetToDefaults(context: Context) {
        getPrefs(context)?.edit()?.clear()?.apply()
    }
}
