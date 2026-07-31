package com.fmcg.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.fmcg.app.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = Constants.TOKEN_PREFS)

/**
 * Persists the JWT pair. Suspend APIs are used from coroutines; the *Blocking
 * accessors exist only for the synchronous OkHttp interceptor/authenticator,
 * which run off the main thread.
 */
@Singleton
class TokenDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val accessKey = stringPreferencesKey(Constants.KEY_ACCESS_TOKEN)
    private val refreshKey = stringPreferencesKey(Constants.KEY_REFRESH_TOKEN)

    val accessTokenFlow: Flow<String?> = context.dataStore.data.map { it[accessKey] }

    val isLoggedIn: Flow<Boolean> =
        context.dataStore.data.map { !it[accessKey].isNullOrEmpty() }

    suspend fun save(access: String, refresh: String) {
        context.dataStore.edit {
            it[accessKey] = access
            it[refreshKey] = refresh
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }

    suspend fun accessToken(): String? = context.dataStore.data.first()[accessKey]
    suspend fun refreshToken(): String? = context.dataStore.data.first()[refreshKey]

    fun accessTokenBlocking(): String? = runBlocking { accessToken() }
    fun refreshTokenBlocking(): String? = runBlocking { refreshToken() }
    fun saveBlocking(access: String, refresh: String) = runBlocking { save(access, refresh) }
    fun clearBlocking() = runBlocking { clear() }
}
