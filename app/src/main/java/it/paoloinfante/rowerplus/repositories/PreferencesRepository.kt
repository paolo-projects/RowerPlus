package it.paoloinfante.rowerplus.repositories

import android.content.Context
import androidx.preference.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import it.paoloinfante.rowerplus.R
import javax.inject.Inject

class PreferencesRepository @Inject constructor(@ApplicationContext private val context: Context) {
    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    val floatingWindowEnabled
        get() = preferences.getBoolean(context.getString(R.string.preferenceFloatingWindow), false)

    val userAge: Int?
        get() = preferences.getString(context.getString(R.string.preferenceUserAge), null)
            ?.toIntOrNull()
}