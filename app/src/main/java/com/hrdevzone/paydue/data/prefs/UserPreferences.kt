package com.hrdevzone.paydue.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.hrdevzone.paydue.util.CurrencyOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "paydue_settings")

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class UserSettings(
    val businessName: String = "",
    val currencyCode: String = "INR",
    val currencySymbol: String = "₹",
    val reminderHour: Int = 9,
    val reminderMinute: Int = 0,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isPro: Boolean = false,
    val defaultPaymentNote: String = "",
    val appLockEnabled: Boolean = false,
    val pinHash: String? = null,
    val pinSalt: String? = null,
    val biometricEnabled: Boolean = false
)

class UserPreferences(private val context: Context) {

    private object Keys {
        val BUSINESS_NAME = stringPreferencesKey("business_name")
        val CURRENCY_CODE = stringPreferencesKey("currency_code")
        val CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val IS_PRO = booleanPreferencesKey("is_pro")
        val PAYMENT_NOTE = stringPreferencesKey("default_payment_note")
        val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        val PIN_HASH = stringPreferencesKey("pin_hash")
        val PIN_SALT = stringPreferencesKey("pin_salt")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
    }

    val settings: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        UserSettings(
            businessName = prefs[Keys.BUSINESS_NAME] ?: "",
            currencyCode = prefs[Keys.CURRENCY_CODE] ?: "INR",
            currencySymbol = prefs[Keys.CURRENCY_SYMBOL] ?: "₹",
            reminderHour = prefs[Keys.REMINDER_HOUR] ?: 9,
            reminderMinute = prefs[Keys.REMINDER_MINUTE] ?: 0,
            themeMode = prefs[Keys.THEME_MODE]?.let {
                runCatching { ThemeMode.valueOf(it) }.getOrDefault(ThemeMode.SYSTEM)
            } ?: ThemeMode.SYSTEM,
            isPro = prefs[Keys.IS_PRO] ?: false,
            defaultPaymentNote = prefs[Keys.PAYMENT_NOTE] ?: "",
            appLockEnabled = prefs[Keys.APP_LOCK_ENABLED] ?: false,
            pinHash = prefs[Keys.PIN_HASH],
            pinSalt = prefs[Keys.PIN_SALT],
            biometricEnabled = prefs[Keys.BIOMETRIC_ENABLED] ?: false
        )
    }

    suspend fun setBusinessName(value: String) {
        context.dataStore.edit { it[Keys.BUSINESS_NAME] = value }
    }

    suspend fun setCurrency(option: CurrencyOption) {
        context.dataStore.edit {
            it[Keys.CURRENCY_CODE] = option.code
            it[Keys.CURRENCY_SYMBOL] = option.symbol
        }
    }

    suspend fun setReminderTime(hour: Int, minute: Int) {
        context.dataStore.edit {
            it[Keys.REMINDER_HOUR] = hour
            it[Keys.REMINDER_MINUTE] = minute
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setPro(value: Boolean) {
        context.dataStore.edit { it[Keys.IS_PRO] = value }
    }

    suspend fun setDefaultPaymentNote(value: String) {
        context.dataStore.edit { it[Keys.PAYMENT_NOTE] = value }
    }

    suspend fun setPin(hash: String, salt: String) {
        context.dataStore.edit {
            it[Keys.PIN_HASH] = hash
            it[Keys.PIN_SALT] = salt
            it[Keys.APP_LOCK_ENABLED] = true
        }
    }

    suspend fun clearPin() {
        context.dataStore.edit {
            it.remove(Keys.PIN_HASH)
            it.remove(Keys.PIN_SALT)
            it[Keys.APP_LOCK_ENABLED] = false
            it[Keys.BIOMETRIC_ENABLED] = false
        }
    }

    suspend fun setAppLockEnabled(value: Boolean) {
        context.dataStore.edit { it[Keys.APP_LOCK_ENABLED] = value }
    }

    suspend fun setBiometricEnabled(value: Boolean) {
        context.dataStore.edit { it[Keys.BIOMETRIC_ENABLED] = value }
    }
}
