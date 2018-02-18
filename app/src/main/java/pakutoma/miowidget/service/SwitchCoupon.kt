package pakutoma.miowidget.service

import android.app.PendingIntent
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.IBinder
import android.widget.RemoteViews
import android.widget.Toast

import pakutoma.miowidget.R
import pakutoma.miowidget.widget.SwitchWidget

/**
 * Created by PAKUTOMA on 2016/12/10.
 */
class SwitchCoupon : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val thisWidget = ComponentName(this, SwitchWidget::class.java!!)
        val manager = AppWidgetManager.getInstance(this)
        val remoteViews = RemoteViews(packageName, R.layout.switch_widget)

        if (intent != null && ACTION_SWITCH_COUPON == intent.action) {
            val preferences = getSharedPreferences("iijmio_token", Context.MODE_PRIVATE)
            if (!preferences.getBoolean("has_token", false)) {
                val builder = Uri.Builder()
                builder.scheme("https")
                builder.authority("api.iijmio.jp")
                builder.path("/mobile/d/v1/authorization")
                builder.encodedQuery("response_type=token&client_id=IilCI1xrAgqKrXV9Zt4&state=example_state&redirect_uri=pakutoma.miowidget://callback")
                val uri = builder.build()
                val authIntent = Intent(Intent.ACTION_VIEW, uri)
                authIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(authIntent)
            } else {
                if (System.currentTimeMillis() - lastClickTime > 1000 * 60) {
                    lastClickTime = System.currentTimeMillis()
                    val waitIntent = Intent(ACTION_WAIT_CHANGE_SWITCH)
                    sendBroadcast(waitIntent)
                    val changeIntent = Intent(ACTION_CHANGE_COUPON)
                    changeIntent.`package` = "pakutoma.miowidget"
                    startService(changeIntent)
                } else {
                    Toast.makeText(this, "API limit: あと" + java.lang.Long.toString((1000 * 60 - (System.currentTimeMillis() - lastClickTime)) / 1000) + "秒お待ち下さい。", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val clickIntent = Intent()
        clickIntent.action = ACTION_SWITCH_COUPON
        val pendingIntent = PendingIntent.getService(this, 0, clickIntent, 0)
        remoteViews.setOnClickPendingIntent(R.id.translate_button, pendingIntent)
        manager.updateAppWidget(thisWidget, remoteViews)
        return Service.START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    companion object {
        private val ACTION_SWITCH_COUPON = "pakutoma.miowidget.widget.SwitchWidget.ACTION_SWITCH_COUPON"
        private val ACTION_CHANGE_COUPON = "pakutoma.miowidget.widget.SwitchWidget.ACTION_CHANGE_COUPON"
        private val ACTION_WAIT_CHANGE_SWITCH = "pakutoma.miowidget.widget.SwitchWidget.ACTION_WAIT_CHANGE_SWITCH"
        private var lastClickTime: Long = 0
    }
}
