package pakutoma.miowidget.service

import android.content.Context
import android.widget.Toast
import com.github.kittinunf.fuel.core.HttpException
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.withContext
import pakutoma.miowidget.R
import pakutoma.miowidget.exception.NotFoundValidTokenException
import pakutoma.miowidget.utility.CouponAPI
import pakutoma.miowidget.widget.updateSwitchStatus

/**
 * Created by PAKUTOMA on 2018/03/13.
 * Functions to use CouponAPI.
 */
suspend fun fetchRemains(context: Context) {
    accessCoupon(context,false)
}

suspend fun switchCoupon(context: Context) {
    accessCoupon(context,true)
}

private suspend fun accessCoupon(context: Context, change: Boolean) {
    val developerID = context.resources.getText(R.string.developerID).toString()
    val preferences = context.getSharedPreferences("iijmio_token", Context.MODE_PRIVATE)
    val editor = preferences.edit()
    val accessToken = preferences.getString("X-IIJmio-Authorization", "")
    try {
        if (accessToken == "") {
            throw NotFoundValidTokenException("Not found token in preference.")
        }
        val coupon = CouponAPI(developerID, accessToken)
        val (isCouponEnabled, remains, serviceCodeList) = fetchCouponInfo(coupon, change)
        if (change) {
            coupon.changeCouponUse(isCouponEnabled, serviceCodeList)
            editor.putBoolean("is_coupon_enabled", isCouponEnabled)
        }
        showResult(context, change, true, true, remains, isCouponEnabled)
    } catch (e: NotFoundValidTokenException) {
        editor.putString("X-IIJmio-Authorization", "")
        showResult(context, change, false)
    } catch (e: HttpException) {
        showResult(context, change, true, false)
    }
    if (change) {
        editor.putLong("last_click_time", System.currentTimeMillis())
    }
    editor.apply()
}

private suspend fun showResult(context: Context, change: Boolean, hasToken: Boolean, couldGet: Boolean = false, remains: Int = -1, isCouponEnabled: Boolean = false) {
    withContext(UI) {
        if (change) {
            sendToast(context, hasToken, couldGet, isCouponEnabled)
        }
        updateSwitchStatus(context, hasToken, couldGet, remains, isCouponEnabled)
    }
}

private suspend fun fetchCouponInfo(coupon: CouponAPI, change: Boolean): Triple<Boolean, Int, List<String>> {
    val couponInfo = coupon.fetchCouponInfo()
    val nowCouponValue = couponInfo.planInfoList[0].lineInfoList[0].couponUse
    val isCouponEnabled = if (!change) nowCouponValue else !nowCouponValue
    val remains = couponInfo.planInfoList.sumBy { it.remains }
    val serviceCodeList = if (!change) emptyList<String>() else couponInfo.planInfoList.flatMap { it.lineInfoList.map { it.serviceCode } }
    return Triple(isCouponEnabled, remains, serviceCodeList)
}

private fun sendToast(context: Context, hasToken: Boolean, couldChange: Boolean = false, isCouponEnabled: Boolean = false) {
    if (!hasToken) {
        Toast.makeText(context, "認証が行われていません。", Toast.LENGTH_SHORT).show()
        return
    }
    if (!couldChange) {
        Toast.makeText(context, "切り替えに失敗しました。", Toast.LENGTH_SHORT).show()
    }
    Toast.makeText(context, "クーポンを" + (if (isCouponEnabled) "ON" else "OFF") + "に変更しました。", Toast.LENGTH_SHORT).show()
}