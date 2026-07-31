package com.fmcg.app.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * JWT storage backed by EncryptedSharedPreferences (AES-256), satisfying the
 * "encrypted local storage for sensitive data" requirement. Getters are
 * synchronous so the OkHttp interceptor/authenticator can read tokens off the
 * network thread without suspending.
 */
@Singleton
class TokenManager @Inject constructor(@ApplicationContext context: Context) {

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "fmcg_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    var accessToken: String?
        get() = prefs.getString(KEY_ACCESS, null)
        set(value) = prefs.edit().putString(KEY_ACCESS, value).apply()

    var refreshToken: String?
        get() = prefs.getString(KEY_REFRESH, null)
        set(value) = prefs.edit().putString(KEY_REFRESH, value).apply()

    /** Id of the signed-in user, needed to address the GPS batch endpoint. */
    var userId: Int?
        get() = prefs.getInt(KEY_UID, -1).let { if (it < 0) null else it }
        set(value) = prefs.edit().putInt(KEY_UID, value ?: -1).apply()

    /** Role name (ADMIN/MARKETING/DELIVERY) used to gate admin-only UI. */
    var role: String?
        get() = prefs.getString(KEY_ROLE, null)
        set(value) = prefs.edit().putString(KEY_ROLE, value).apply()

    fun save(access: String, refresh: String) {
        prefs.edit().putString(KEY_ACCESS, access).putString(KEY_REFRESH, refresh).apply()
    }

    fun clear() = prefs.edit().clear().apply()

    fun isLoggedIn(): Boolean = !accessToken.isNullOrBlank()

    private companion object {
        const val KEY_ACCESS = "access_token"
        const val KEY_REFRESH = "refresh_token"
        const val KEY_UID = "user_id"
        const val KEY_ROLE = "user_role"
    }
}
