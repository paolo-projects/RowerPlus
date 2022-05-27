package it.paoloinfante.rowerplus.repositories

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import it.paoloinfante.rowerplus.R
import javax.inject.Inject

class SharedSettingsRepository @Inject constructor(@ApplicationContext private val context: Context) {
    companion object {
        private const val HR_BLE_DEVICE_NAME = "hr_ble_device_name"
    }

    private val mSharedPreferences = context.getSharedPreferences(
        context.getString(R.string.preferences_key),
        Context.MODE_PRIVATE
    )

    fun getDefaultHRBleName(): String? {
        return mSharedPreferences.getString(HR_BLE_DEVICE_NAME, null)
    }

    fun setDefaultHRBleName(deviceName: String) {
        mSharedPreferences.edit()
            .putString(HR_BLE_DEVICE_NAME, deviceName)
            .apply()
    }
}