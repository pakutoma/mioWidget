package pakutoma.miowidget.service

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

/**
 * Created by PAKUTOMA on 2016/12/10.
 */
class SwitchCoupon : Service() {

    companion object {
        private const val ACTION_SWITCH_COUPON = "pakutoma.miowidget.widget.SwitchWidget.ACTION_SWITCH_COUPON"
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


        changeToWaitMode(applicationContext, preferences.getBoolean("is_coupon_enable", false))
        launch {
            val developerID = resources.getText(R.string.developerID).toString()
            val editor = preferences.edit()
            try {
                val coupon = CouponAPI(developerID, accessToken)
                val couponInfo = coupon.fetchCouponInfo()
                val isCouponEnable = !couponInfo.planInfoList[0].lineInfoList[0].couponUse
                val remains = couponInfo.planInfoList.sumBy { it.remains }
                val serviceCodeList = couponInfo.planInfoList.flatMap { it.lineInfoList.map { it.serviceCode } }
                coupon.changeCouponUse(isCouponEnable, serviceCodeList)
                editor.putBoolean("is_coupon_enable", isCouponEnable)
                withContext(UI) {
                    sendToast(true, true, isCouponEnable)
                    updateSwitchStatus(applicationContext, true, true, remains, isCouponEnable)
                }
            } catch (e: NotFoundValidTokenException) {
                editor.putString("X-IIJmio-Authorization", "")
                withContext(UI) {
                    sendToast(false)
                    updateSwitchStatus(applicationContext, false, false)
                }
            } catch (e: Exception) {
                withContext(UI) {
                    sendToast(true,false)
                    updateSwitchStatus(applicationContext, true, false)
                }
            }
            editor.putLong("last_click_time", System.currentTimeMillis())
            editor.apply()
            setSwitchPendingIntent(applicationContext)
            stopSelf()
        }
        return Service.START_NOT_STICKY
    }

    private fun sendToast(hasToken: Boolean, couldChange: Boolean = false, isCouponEnable: Boolean = false) {
        if (!hasToken) {
            Toast.makeText(applicationContext, "認証が行われていません。", Toast.LENGTH_SHORT).show()
            return
        }
        if (!couldChange) {
            Toast.makeText(applicationContext, "切り替えに失敗しました。", Toast.LENGTH_SHORT).show()
        }
        Toast.makeText(applicationContext, "クーポンを" + (if (isCouponEnable) "ON" else "OFF") + "に変更しました。", Toast.LENGTH_SHORT).show()

    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

}
