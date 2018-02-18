package pakutoma.miowidget.service

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences

import java.io.IOException

import pakutoma.miowidget.utility.CouponAPI
import pakutoma.miowidget.utility.CouponData
import pakutoma.miowidget.exception.NotFoundValidTokenException


/**
 * Created by PAKUTOMA on 2016/06/21.
 * Get Traffic Function
 */
class GetTraffic : IntentService("GetTraffic") {

    override fun onHandleIntent(intent: Intent?) {
        val coupon: CouponAPI
        val cd: CouponData
        try {
            coupon = CouponAPI(applicationContext)
            cd = coupon.couponData
        } catch (e: NotFoundValidTokenException) {
            val preferences = getSharedPreferences("iijmio_token", Context.MODE_PRIVATE)
            val editor = preferences.edit()
            editor.putString("X-IIJmio-Authorization", "")
            editor.putBoolean("has_token", false)
            editor.apply()
            sendCallback(false, false, -1, false)
            return
        } catch (e: IOException) {
            sendCallback(true, false, -1, false)
            return
        }

        val isOnCoupon = cd.switch
        val traffic = cd.traffic

        sendCallback(true, true, traffic, isOnCoupon)
    }


    private fun sendCallback(hasToken: Boolean, couldGet: Boolean, traffic: Int, isOnCoupon: Boolean) {
        val callbackIntent = Intent(ACTION_CALLBACK_GET_TRAFFIC)
        callbackIntent.putExtra("HAS_TOKEN", hasToken)
        if (hasToken) {
            callbackIntent.putExtra("GET", couldGet)
        }
        if (couldGet) {
            callbackIntent.putExtra("TRAFFIC", traffic)
            callbackIntent.putExtra("COUPON", isOnCoupon)
        }
        callbackIntent.`package` = "pakutoma.miowidget"
        sendBroadcast(callbackIntent)
    }

    companion object {
        private val ACTION_CALLBACK_GET_TRAFFIC = "pakutoma.miowidget.widget.SwitchWidget.ACTION_CALLBACK_GET_TRAFFIC"
    }
}