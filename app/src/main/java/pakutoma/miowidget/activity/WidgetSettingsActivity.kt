package pakutoma.miowidget.activity

import android.os.Bundle
import android.preference.CheckBoxPreference
import android.preference.PreferenceCategory
import android.preference.PreferenceFragment
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import org.jetbrains.anko.db.classParser
import org.jetbrains.anko.db.parseList
import org.jetbrains.anko.db.select
import pakutoma.miowidget.R
import pakutoma.miowidget.utility.CouponInfoFromDb
import pakutoma.miowidget.utility.database

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
            activity.database.use {
                val couponInfo = select("coupon_info",
                        "service_code",
                        "plan_name",
                        "number",
                        "regulation")
                        .exec { parseList(classParser<CouponInfoFromDb>()) }
                val showRemainsPlans = preferenceScreen.findPreference("show_remains_plans")!! as PreferenceCategory
                couponInfo.map { it.plan to it.serviceCode }.distinct().forEach {
                    showRemainsPlans.addPreference(CheckBoxPreference(activity).apply {
                        title = it.first
                        key = it.second
                    })
                }
                val switchLines = preferenceScreen.findPreference("switch_lines")!! as PreferenceCategory
                couponInfo.map { (it.line to it.regulation) to it.number }.distinct().forEach {
                    switchLines.addPreference(CheckBoxPreference(activity).apply {
                        title = it.first.first
                        summary = if(it.first.second) resources.getText(R.string.regulation).toString() else ""
                        key = it.second
                    })
                }
            }
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