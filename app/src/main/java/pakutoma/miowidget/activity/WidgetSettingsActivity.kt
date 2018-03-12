package pakutoma.miowidget.activity

import android.os.Bundle
import android.preference.CheckBoxPreference
import android.preference.PreferenceCategory
import android.preference.PreferenceFragment
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import pakutoma.miowidget.R

/**
 * Created by PAKUTOMA on 2018/03/13.
 * widget settings
 */

class WidgetSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        fragmentManager.beginTransaction()
                .replace(android.R.id.content, SettingsPreferenceFragment())
                .commit()
    }

    class SettingsPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.activity_widget_settings)
            (preferenceScreen.findPreference("show_remains_plans") as? PreferenceCategory)?.addPreference(CheckBoxPreference(this.activity).apply {
                title = "hddXXXXXXXX (Light Start)"
                key="add"
            })
            (preferenceScreen.findPreference("switch_lines") as? PreferenceCategory)?.addPreference(CheckBoxPreference(this.activity).apply {
                title = "080XXXXXXXX (hddXXXXXXXX)"
                key="add"
            })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}