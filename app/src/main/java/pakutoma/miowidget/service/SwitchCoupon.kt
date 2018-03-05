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
import android.support.v4.content.LocalBroadcastManager
import android.widget.RemoteViews
import android.widget.Toast
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking

import pakutoma.miowidget.R
import pakutoma.miowidget.activity.OpenBrowserActivity
import pakutoma.miowidget.exception.NotFoundValidTokenException
import pakutoma.miowidget.utility.CouponAPI
import pakutoma.miowidget.widget.SwitchWidget

/**
 * Created by PAKUTOMA on 2016/12/10.
 */
class SwitchCoupon : Service() {

    companion object {
        private const val ACTION_CALLBACK_SWITCH_COUPON = "pakutoma.miowidget.widget.SwitchWidget.ACTION_CALLBACK_SWITCH_COUPON"
        private const val ACTION_SWITCH_COUPON = "pakutoma.miowidget.widget.SwitchWidget.ACTION_SWITCH_COUPON"
        private const val ACTION_WAIT_CHANGE_SWITCH = "pakutoma.miowidget.widget.SwitchWidget.ACTION_WAIT_CHANGE_SWITCH"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val thisWidget = ComponentName(this, SwitchWidget::class.java)
        val manager = AppWidgetManager.getInstance(applicationContext)
        val remoteViews = RemoteViews(packageName, R.layout.switch_widget)

        val preferences = getSharedPreferences("iijmio_token", Context.MODE_PRIVATE)
        val accessToken = preferences.getString("X-IIJmio-Authorization", "")

        if (accessToken == ""){
            val authIntent = Intent(applicationContext, OpenBrowserActivity::class.java)
            authIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(authIntent)
            stopSelf()
            return START_NOT_STICKY
        }

        val lastClickTime = preferences.getLong("last_click_time",0)
        if (System.currentTimeMillis() - lastClickTime < 1000 * 60) {
            Toast.makeText(this, "API limit: あと" + java.lang.Long.toString((1000 * 60 - (System.currentTimeMillis() - lastClickTime)) / 1000) + "秒お待ち下さい。", Toast.LENGTH_SHORT).show()
            stopSelf()
            return START_NOT_STICKY
        }

        val waitIntent = Intent(ACTION_WAIT_CHANGE_SWITCH)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(waitIntent)
        launch {
            val developerID = resources.getText(R.string.developerID).toString()
            val editor = preferences.edit()
            try {
                val coupon = CouponAPI(developerID, accessToken)
                val couponInfo = coupon.fetchCouponInfo()
                val isOn = !couponInfo.planInfoList[0].lineInfoList[0].couponUse
                val serviceCodeList = couponInfo.planInfoList.flatMap { it.lineInfoList.map { it.serviceCode } }
                coupon.changeCouponUse(isOn, serviceCodeList)
                sendCallback(true, true, isOn)
            } catch (e: NotFoundValidTokenException) {
                editor.putString("X-IIJmio-Authorization", "")
                sendCallback(false,false)
            } catch (e: Exception) {
                sendCallback(true,false)
            }
            editor.putLong("last_click_time", System.currentTimeMillis())
            editor.apply()
            stopSelf()
        }

        val clickIntent = Intent()
        clickIntent.action = ACTION_SWITCH_COUPON
        val pendingIntent = PendingIntent.getService(this, 0, clickIntent, 0)
        remoteViews.setOnClickPendingIntent(R.id.translate_button, pendingIntent)
        manager.updateAppWidget(thisWidget, remoteViews)
        return Service.START_NOT_STICKY
    }

    private fun sendCallback(hasToken: Boolean, isChanged: Boolean, nowSwitch: Boolean = false) {
        val callbackIntent = Intent(ACTION_CALLBACK_SWITCH_COUPON)
        callbackIntent.putExtra("HAS_TOKEN", hasToken)
        if (hasToken) {
            callbackIntent.putExtra("CHANGE", isChanged)
        }
        if (isChanged) {
            callbackIntent.putExtra("NOW_SWITCH", nowSwitch)
        }
        callbackIntent.`package` = "pakutoma.miowidget"
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(callbackIntent)
    }


    override fun onBind(intent: Intent): IBinder? {
        return null
    }

}
