package kr.co.core.firebase.provider

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRemoteConfigProvider @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig,
) {
    fun setMinimumFetchIntervalInSeconds(seconds: Long) =
        remoteConfig.setConfigSettingsAsync(
            remoteConfigSettings {
                minimumFetchIntervalInSeconds = seconds
            }
        )

    fun fetchAndActivate() = remoteConfig.fetchAndActivate()

    fun getBoolean(key: String) = remoteConfig.getBoolean(key)

    fun getString(key: String) = remoteConfig.getString(key)

    fun getLong(key: String) = remoteConfig.getLong(key)
}
