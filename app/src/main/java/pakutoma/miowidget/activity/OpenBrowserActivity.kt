package pakutoma.miowidget.activity

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.RemoteViews
import android.widget.Toast

import pakutoma.miowidget.R
import pakutoma.miowidget.exception.NotFoundValidTokenException
import pakutoma.miowidget.utility.CouponAPI
import pakutoma.miowidget.widget.SwitchWidget

/**
 * Created by PAKUTOMA on 2017/03/20.
 */

class OpenBrowserActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        val intent = intent
        val extras = intent.extras
        var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID)
        }
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
        }

        val appWidgetManager = AppWidgetManager.getInstance(this)
        val views = RemoteViews(this.packageName, R.layout.switch_widget)
        appWidgetManager.updateAppWidget(appWidgetId, views)

        var isAuth = false
        try {
            CouponAPI(this)
        } catch (e: NotFoundValidTokenException) {
            isAuth = true
            Toast.makeText(this, "認証を開始します。", Toast.LENGTH_SHORT).show()
            val builder = Uri.Builder()
            builder.scheme("https")
            builder.authority("api.iijmio.jp")
            builder.path("/mobile/d/v1/authorization")
            builder.encodedQuery("response_type=token&client_id=IilCI1xrAgqKrXV9Zt4&state=example_state&redirect_uri=pakutoma.miowidget://callback")
            val uri = builder.build()
            val authIntent = Intent(Intent.ACTION_VIEW, uri)
            authIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            this.startActivity(authIntent)
        }

        if (!isAuth) {
            setWidgetButtonIntent()
        }
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(Activity.RESULT_OK, resultValue)
        finish()

    }

    private fun setWidgetButtonIntent() {
        val intent = Intent(applicationContext, SwitchWidget::class.java)
        intent.action = ACTION_WIDGET_ENABLE
        val sender = PendingIntent.getBroadcast(this, 0, intent, 0)
        val am = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val interval: Long = 1000
        val nextAlarm = System.currentTimeMillis() + interval
        am.set(AlarmManager.RTC, nextAlarm, sender)
    }

    companion object {
        private val ACTION_WIDGET_ENABLE = "pakutoma.miowidget.widget.SwitchWidget.ACTION_WIDGET_ENABLE"
    }
}


