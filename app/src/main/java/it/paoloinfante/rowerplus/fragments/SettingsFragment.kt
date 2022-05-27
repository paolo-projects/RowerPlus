package it.paoloinfante.rowerplus.fragments

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import it.paoloinfante.rowerplus.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val userAgePreference =
            findPreference<EditTextPreference>(getString(R.string.preferenceUserAge))
        userAgePreference?.onPreferenceChangeListener = userAgePreferenceChangeListener
    }

    private val userAgePreferenceChangeListener =
        Preference.OnPreferenceChangeListener { _, newValue ->
            val age = newValue.toString().toIntOrNull() ?: 0
            age in 6..120
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val typedArray =
            requireContext().theme.obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize))
        val actionBarSize = typedArray.getDimensionPixelSize(0, 0)
        typedArray.recycle()

        view.updateLayoutParams<FrameLayout.LayoutParams> {
            topMargin = actionBarSize
        }
    }
}