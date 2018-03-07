package pakutoma.miowidget.service

import android.app.job.JobParameters
import android.content.Context
import kotlinx.coroutines.experimental.launch
import pakutoma.miowidget.R

import java.io.IOException
import android.app.job.JobService
import com.github.kittinunf.fuel.core.HttpException
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.withContext

import pakutoma.miowidget.utility.CouponAPI
import pakutoma.miowidget.exception.NotFoundValidTokenException
import pakutoma.miowidget.utility.CouponInfo
import pakutoma.miowidget.widget.updateSwitchStatus


/**
 * Created by PAKUTOMA on 2016/06/21.
 * Get Traffic Function
 */
class FetchRemainsJobService : JobService() {

    override fun onStartJob(params: JobParameters?): Boolean {
        launch {
            val preferences = getSharedPreferences("iijmio_token", Context.MODE_PRIVATE)
            val couponInfo: CouponInfo
            val developerID = resources.getText(R.string.developerID).toString()
            val accessToken = preferences.getString("X-IIJmio-Authorization", "")
            try {
                if (accessToken == "") {
                    throw NotFoundValidTokenException("Not found token in preference.")
                }
                val coupon = CouponAPI(developerID, accessToken)
                couponInfo = coupon.fetchCouponInfo()
                val isCouponEnable = couponInfo.planInfoList[0].lineInfoList[0].couponUse
                val remains = couponInfo.planInfoList.sumBy { it.remains }
                val editor = preferences.edit()
                editor.putBoolean("is_coupon_enable", isCouponEnable)
                editor.apply()
                withContext(UI) {
                    updateSwitchStatus(applicationContext, true, true, remains, isCouponEnable)
                }
            } catch (e: NotFoundValidTokenException) {
                val editor = preferences.edit()
                editor.putString("X-IIJmio-Authorization", "")
                editor.apply()
                withContext(UI) {
                    updateSwitchStatus(applicationContext, false, false)
                }
            } catch (e: HttpException) {
                withContext(UI) {
                    updateSwitchStatus(applicationContext, true, false)
                }
            }
            jobFinished(params, true)
        }
        return true
    }

    override fun onStopJob(p0: JobParameters?): Boolean {
        return false
    }
}