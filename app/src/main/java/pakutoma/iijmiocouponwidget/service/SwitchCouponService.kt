package pakutoma.iijmiocouponwidget.service

import android.annotation.TargetApi
import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.IBinder
import android.widget.Toast
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import pakutoma.iijmiocouponwidget.R
import pakutoma.iijmiocouponwidget.activity.OpenBrowserActivity
import pakutoma.iijmiocouponwidget.widget.changeToWaitMode
import pakutoma.iijmiocouponwidget.widget.setSwitchPendingIntent
import android.os.Build
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.withTimeoutOrNull
import kotlin.coroutines.experimental.suspendCoroutine


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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notification = Notification
                    .Builder(applicationContext, "switch_service")
                    .setContentTitle(applicationContext.getString(R.string.switch_notification))
                    .build()
            startForeground(SWITCH_COUPON_NOTIFICATION_ID, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val preferences = getSharedPreferences("iijmio_token", Context.MODE_PRIVATE);
        if (preferences == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        val timeFromFinish = System.currentTimeMillis() - preferences.getLong("last_click_time", 0);
        val editor = preferences.edit()
        if (timeFromFinish > 3000 * 60) {
            // サービス異常終了時への対処
            editor.putBoolean("SwitchCouponServiceIsRunning", false)
            editor.apply()
        }

        val running = preferences.getBoolean("SwitchCouponServiceIsRunning", false)
        if (running) {
            Toast.makeText(this, "クーポン切替中です", Toast.LENGTH_SHORT).show()
            return START_NOT_STICKY
        }
        editor.putBoolean("SwitchCouponServiceIsRunning", true)
        editor.apply()

        val accessToken = preferences.getString("X-IIJmio-Authorization", "")
        if (accessToken == "") {
            val authIntent = Intent(applicationContext, OpenBrowserActivity::class.java)
            authIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(authIntent)
            setSwitchPendingIntent(applicationContext)
            exit(editor)
            return START_NOT_STICKY
        }

        changeToWaitMode(applicationContext, preferences.getBoolean("is_coupon_enabled", false))

        val cm = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkRequest = NetworkRequest.Builder().build()
        launch {
            if (timeFromFinish < 1000 * 60) {
                val remainingTime = 1000 * 60 - timeFromFinish
                withContext(UI) {
                    Toast.makeText(applicationContext, "API limit: " + java.lang.Long.toString(remainingTime / 1000) + "秒後に切替を行います", Toast.LENGTH_SHORT).show()
                }
                delay(1000 * 60 - timeFromFinish)
            }
            val network = withTimeoutOrNull(1000 * 60) {
                waitNetworkAvailable(cm, networkRequest)
            }
            if (network != null) {
                switchCoupon(applicationContext)
            } else {
                Toast.makeText(applicationContext, "接続に失敗しました", Toast.LENGTH_SHORT).show()
            }
            exit(editor)
        }
        return Service.START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun exit(editor: SharedPreferences.Editor) {
        editor.putBoolean("SwitchCouponServiceIsRunning", false);
        editor.apply()
        stopSelf()
    }

    private suspend fun waitNetworkAvailable(cm: ConnectivityManager, networkRequest: NetworkRequest): Network? = suspendCoroutine { cont ->
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network?) {
                super.onAvailable(network)
                cm.unregisterNetworkCallback(this);
                cont.resume(network)
            }
        }
        cm.registerNetworkCallback(networkRequest, callback)
    }

}
