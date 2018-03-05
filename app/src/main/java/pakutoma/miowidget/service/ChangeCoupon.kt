package pakutoma.miowidget.service

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import pakutoma.miowidget.R

import pakutoma.miowidget.utility.CouponAPI
import pakutoma.miowidget.exception.NotFoundValidTokenException


/**
 * ChangeCoupon change iijmio's coupon switch on/off.
 * Created by PAKUTOMA on 2016/06/21.
 */
class ChangeCoupon : IntentService("ChangeCoupon") {

    override fun onHandleIntent(intent: Intent?) {
        val preferences = applicationContext.getSharedPreferences("iijmio_token", Context.MODE_PRIVATE)
        try {
            val developerID = resources.getText(R.string.developerID).toString()
            val accessToken = preferences.getString("X-IIJmio-Authorization", "")
            if (accessToken == "") {
                throw NotFoundValidTokenException("Not found token in preference.")
            }
            val coupon = CouponAPI(developerID, accessToken)
            val couponInfo = runBlocking { coupon.fetchCouponInfo() }
            val isOn = !couponInfo.planInfoList[0].lineInfoList[0].couponUse
            val serviceCodeList = couponInfo.planInfoList.flatMap { it.lineInfoList.map { it.serviceCode } }
            runBlocking { coupon.changeCouponUse(isOn, serviceCodeList) }
            sendCallback(true, true, isOn)
        } catch (e: NotFoundValidTokenException) {
            val editor = preferences.edit()
            editor.putString("X-IIJmio-Authorization", "")
            editor.putBoolean("has_token", false)
            editor.apply()
            sendCallback(false, false, false)
        } catch (e: Exception) {
            sendCallback(true, false, false)
        }

    }

    private fun sendCallback(hasToken: Boolean, isChanged: Boolean, nowSwitch: Boolean) {
        val callbackIntent = Intent(ACTION_CALLBACK_CHANGE_COUPON)
        callbackIntent.putExtra("HAS_TOKEN", hasToken)
        if (hasToken) {
            callbackIntent.putExtra("CHANGE", isChanged)
        }
        if (isChanged) {
            callbackIntent.putExtra("NOW_SWITCH", nowSwitch)
        }
        callbackIntent.`package` = "pakutoma.miowidget"
        sendBroadcast(callbackIntent)
    }

    companion object {
        private val ACTION_CALLBACK_CHANGE_COUPON = "pakutoma.miowidget.widget.SwitchWidget.ACTION_CALLBACK_CHANGE_COUPON"
    }

}
