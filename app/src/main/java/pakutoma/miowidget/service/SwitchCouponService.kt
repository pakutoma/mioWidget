package pakutoma.miowidget.service

import android.annotation.TargetApi
import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext

import pakutoma.miowidget.R
import pakutoma.miowidget.activity.OpenBrowserActivity
import pakutoma.miowidget.exception.NotFoundValidTokenException
import pakutoma.miowidget.utility.CouponAPI
import pakutoma.miowidget.widget.changeToWaitMode
import pakutoma.miowidget.widget.setSwitchPendingIntent
import pakutoma.miowidget.widget.updateSwitchStatus
import android.os.Build
import com.github.kittinunf.fuel.core.HttpException


/**
 * Created by PAKUTOMA on 2016/12/10.
 * switch coupon service
 */
class SwitchCouponService : Service() {

    companion object {
        private const val SWITCH_COUPON_NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        @TargetApi(Build.VERSION_CODES.O)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notification = Notification
                    .Builder(applicationContext, "switch_service")
                    .setContentTitle(applicationContext.getString(R.string.switch_notification))
                    .build()
            startForeground(SWITCH_COUPON_NOTIFICATION_ID, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val preferences = getSharedPreferences("iijmio_token", Context.MODE_PRIVATE)
        val accessToken = preferences.getString("X-IIJmio-Authorization", "")

        if (accessToken == "") {
            val authIntent = Intent(applicationContext, OpenBrowserActivity::class.java)
            authIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(authIntent)
            setSwitchPendingIntent(applicationContext)
            stopSelf()
            return START_NOT_STICKY
        }

        val lastClickTime = preferences.getLong("last_click_time", 0)
        if (System.currentTimeMillis() - lastClickTime < 1000 * 60) {
            Toast.makeText(this, "API limit: あと" + java.lang.Long.toString((1000 * 60 - (System.currentTimeMillis() - lastClickTime)) / 1000) + "秒お待ち下さい。", Toast.LENGTH_SHORT).show()
            setSwitchPendingIntent(applicationContext)
            stopSelf()
            return START_NOT_STICKY
        }

        changeToWaitMode(applicationContext, preferences.getBoolean("is_coupon_enabled", false))
        launch {
            switchCoupon(applicationContext)
            stopSelf()
        }
        return Service.START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

}
