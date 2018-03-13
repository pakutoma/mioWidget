package pakutoma.iijmiocouponwidget.activity

import android.os.Bundle

import pakutoma.iijmiocouponwidget.R
import android.preference.PreferenceFragment
import android.support.v7.app.AppCompatActivity


class MainSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentManager.beginTransaction()
                .replace(android.R.id.content, SettingsPreferenceFragment())
                .commit()
    }

    class SettingsPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.activity_main_settings)
        }
    }
}