package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dailyapp_prefs")

data class AppSettings(
    val languageCode: String = "en", // "en" or "si"
    val monthlyBudget: Double = 60000.0,
    val themeMode: String = "system", // "system", "light", "dark"
    val biometricLockEnabled: Boolean = false,
    val voiceAlertsEnabled: Boolean = true,
    val currencySymbol: String = "Rs."
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
        val MONTHLY_BUDGET = doublePreferencesKey("monthly_budget")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val BIOMETRIC_LOCK = booleanPreferencesKey("biometric_lock")
        val VOICE_ALERTS = booleanPreferencesKey("voice_alerts")
        val CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            languageCode = prefs[Keys.LANGUAGE_CODE] ?: "en",
            monthlyBudget = prefs[Keys.MONTHLY_BUDGET] ?: 60000.0,
            themeMode = prefs[Keys.THEME_MODE] ?: "system",
            biometricLockEnabled = prefs[Keys.BIOMETRIC_LOCK] ?: false,
            voiceAlertsEnabled = prefs[Keys.VOICE_ALERTS] ?: true,
            currencySymbol = prefs[Keys.CURRENCY_SYMBOL] ?: "Rs."
        )
    }

    suspend fun setLanguage(code: String) {
        context.dataStore.edit { it[Keys.LANGUAGE_CODE] = code }
    }

    suspend fun setMonthlyBudget(budget: Double) {
        context.dataStore.edit { it[Keys.MONTHLY_BUDGET] = budget.coerceAtLeast(0.0) }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode }
    }

    suspend fun setBiometricLock(enabled: Boolean) {
        context.dataStore.edit { it[Keys.BIOMETRIC_LOCK] = enabled }
    }

    suspend fun setVoiceAlertsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.VOICE_ALERTS] = enabled }
    }
}
