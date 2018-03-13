package pakutoma.miowidget.service

import android.content.Context
import android.database.sqlite.SQLiteException
import android.widget.Toast
import com.github.kittinunf.fuel.core.HttpException
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import org.jetbrains.anko.db.*
import org.jetbrains.anko.defaultSharedPreferences
import pakutoma.miowidget.R
import pakutoma.miowidget.exception.NotFoundValidTokenException
import pakutoma.miowidget.utility.CouponAPI
import pakutoma.miowidget.utility.CouponInfo
import pakutoma.miowidget.utility.database
import pakutoma.miowidget.widget.updateSwitchStatus

/**
 * Created by PAKUTOMA on 2018/03/13.
 * Functions to use CouponAPI.
 */
suspend fun fetchRemains(context: Context) {
    accessCoupon(context, false)
}

suspend fun switchCoupon(context: Context) {
    accessCoupon(context, true)
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
        val (isCouponEnabled, remains, serviceCodeList) = fetchCouponInfo(context, coupon, change)
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

private suspend fun showResult(context: Context, change: Boolean, hasToken: Boolean, couldGet: Boolean = false, remains: Int = -1, isCouponEnabled: Boolean = false) = withContext(UI) {
    if (change) {
        sendToast(context, hasToken, couldGet, isCouponEnabled)
    }
    updateSwitchStatus(context, hasToken, couldGet, remains, isCouponEnabled)
}

private suspend fun fetchCouponInfo(context: Context, coupon: CouponAPI, change: Boolean): Triple<Boolean, Int, List<String>> {
    val couponInfo = coupon.fetchCouponInfo()
    launch { updateDb(context, couponInfo) }
    val (enablePlans, enableLines) = getEnablePlansAndLines(context, couponInfo)
    val isCouponEnabledNow = if (enableLines.isEmpty()) {
        couponInfo.planInfoList[0].lineInfoList[0].couponUse
    } else {
        couponInfo.planInfoList.flatMap { it.lineInfoList.filter { it.number == enableLines.first() }.map { it.couponUse } }.first()
    }
    val isCouponEnabled = if (!change) isCouponEnabledNow else !isCouponEnabledNow
    val remains = if (enablePlans.isEmpty()) {
        couponInfo.planInfoList.sumBy { it.remains }
    } else {
        couponInfo.planInfoList.filter { x -> enablePlans.any { it == x.serviceCode } }.sumBy { it.remains }
    }
    val serviceCodeList = if (!change) emptyList<String>() else {
        if (enableLines.isEmpty()) {
            couponInfo.planInfoList.flatMap { it.lineInfoList.map { it.serviceCode } }
        } else {
            couponInfo.planInfoList.flatMap { it.lineInfoList.filter { x -> enableLines.any { it == x.number } }.map { it.serviceCode } }
        }
    }
    return Triple(isCouponEnabled, remains, serviceCodeList)
}

private fun updateDb(context: Context, couponInfo: CouponInfo) {
    context.database.use {
        val dbLineList = select("coupon_info", "number")
                .distinct()
                .exec { parseList(StringParser) }
        val lineInsert = couponInfo.planInfoList.flatMap { it.lineInfoList.map { it.number } }.subtract(dbLineList)
        val lineDelete = dbLineList.subtract(couponInfo.planInfoList.flatMap { it.lineInfoList.map { it.number } })
        val lineUpdate = couponInfo.planInfoList.flatMap { it.lineInfoList.map { it.number } }.intersect(dbLineList)
        for (line in lineInsert) {
            val planInfo = couponInfo.planInfoList.find { it.lineInfoList.any { it.number == line } }!!
            val lineInfo = couponInfo.planInfoList.flatMap { it.lineInfoList.filter { it.number == line } }.first()
            insert("coupon_info",
                    "service_code" to planInfo.serviceCode,
                    "plan_name" to planInfo.plan,
                    "number" to lineInfo.number,
                    "regulation" to lineInfo.regulation
            )
        }
        for (line in lineDelete) {
            delete("coupon_info",
                    "number = {number}",
                    "number" to line)
        }
        for (line in lineUpdate) {
            val planInfo = couponInfo.planInfoList.find { it.lineInfoList.any { it.number == line } }!!
            val lineInfo = couponInfo.planInfoList.flatMap { it.lineInfoList.filter { it.number == line } }.first()
            update("coupon_info",
                    "service_code" to planInfo.serviceCode,
                    "plan_name" to planInfo.plan,
                    "regulation" to lineInfo.regulation)
        }
    }
}

private fun getEnablePlansAndLines(context: Context, couponInfo: CouponInfo): Pair<List<String>, List<String>> {
    val preference = context.defaultSharedPreferences
    val enablePlans = couponInfo.planInfoList.filter { preference.getBoolean(it.serviceCode, false) }.map { it.serviceCode }
    val enableLines = couponInfo.planInfoList.flatMap { it.lineInfoList.filter { preference.getBoolean(it.number, false) }.map { it.number } }
    return Pair(enablePlans, enableLines)
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