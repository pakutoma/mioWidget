package pakutoma.miowidget.service

import android.app.IntentService
import android.app.job.JobParameters
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import pakutoma.miowidget.R

import java.io.IOException
import android.app.job.JobService
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch

import pakutoma.miowidget.utility.CouponAPI
import pakutoma.miowidget.exception.NotFoundValidTokenException
import pakutoma.miowidget.utility.CouponInfo


/**
 * Created by PAKUTOMA on 2016/06/21.
 * Get Traffic Function
 */
class GetTraffic : JobService() {

    override fun onStartJob(params: JobParameters?): Boolean {
        launch(UI) {
            //処理
            jobFinished(params, false);
        }
        return true
    }

    override fun onStopJob(p0: JobParameters?): Boolean {
        return false
    }

    override fun onHandleIntent(intent: Intent?) {
        val preferences = getSharedPreferences("iijmio_token", Context.MODE_PRIVATE)
        val couponInfo: CouponInfo
        try {
            val developerID = resources.getText(R.string.developerID).toString()
            val accessToken = preferences.getString("X-IIJmio-Authorization", "")
            if (accessToken == "") {
                throw NotFoundValidTokenException("Not found token in preference.")
            }
            val coupon = CouponAPI(developerID, accessToken)
            couponInfo = runBlocking { coupon.fetchCouponInfo() }
        } catch (e: NotFoundValidTokenException) {
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

        val isOn = couponInfo.planInfoList[0].lineInfoList[0].couponUse
        val remains = couponInfo.planInfoList.sumBy { it.remains }

        sendCallback(true, true, remains, isOn)
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