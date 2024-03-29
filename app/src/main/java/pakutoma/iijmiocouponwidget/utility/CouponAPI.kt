package pakutoma.iijmiocouponwidget.utility

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPut
import com.github.kittinunf.fuel.moshi.responseObject
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.getAs
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pakutoma.iijmiocouponwidget.exception.NotFoundValidTokenException
import pakutoma.iijmiocouponwidget.exception.UndefinedPlanException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * iijmio coupon switch api wrapper
 * Created by PAKUTOMA on 2018/02/20.
 */

class CouponAPI constructor(developerID: String, accessToken: String) {

    init {
        FuelManager.instance.basePath = "https://api.iijmio.jp/mobile/d/v2"
        FuelManager.instance.baseHeaders = mapOf(
                "X-IIJmio-Developer" to developerID,
                "X-IIJmio-Authorization" to accessToken
        )
    }

    suspend fun fetchCouponInfo(): CouponInfo {
        val fetchedData = withContext(Dispatchers.Default) { sendHttpGetCouponInfo() }
        return convert(fetchedData)
    }

    private suspend fun sendHttpGetCouponInfo(): CouponDataFromJson = suspendCoroutine { cont ->
        "/coupon/".httpGet()
                .responseObject<CouponDataFromJson> { _, response, result ->
                    when (result) {
                        is Result.Failure -> {
                            if (response.statusCode == 403) {
                                cont.resumeWithException(NotFoundValidTokenException("User Authorization Failure"))
                            }
                            cont.resumeWithException(result.getAs()!!)
                        }
                        is Result.Success -> {
                            cont.resume(result.getAs()!!)
                        }
                    }
                }
    }

    suspend fun changeCouponUse(isOn: Boolean, serviceCodeList: List<String>): ReturnCodeFromJson {
        val couponDataToJson = packJsonClass(isOn, serviceCodeList)
        val moshi = Moshi.Builder().build()
        val adapter = moshi.adapter(CouponDataToJson::class.java)
        val json = adapter.toJson(couponDataToJson)
        return withContext(Dispatchers.Default) { sendHttpPutCouponStatus(json) }
    }

    private suspend fun sendHttpPutCouponStatus(json: String): ReturnCodeFromJson = suspendCoroutine { cont ->
        "/coupon/".httpPut()
                .jsonBody(json)
                .responseObject<ReturnCodeFromJson> { _, response, result ->
                    when (result) {
                        is Result.Failure -> {
                            if (response.statusCode == 403) {
                                cont.resumeWithException(NotFoundValidTokenException("User Authorization Failure"))
                            }
                            cont.resumeWithException(result.getAs()!!)
                        }
                        is Result.Success -> {
                            cont.resume(result.getAs()!!)
                        }
                    }
                }
    }

    private fun packJsonClass(isOn: Boolean, serviceCodeList: List<String>): CouponDataToJson {
        val typeList = serviceCodeList.groupBy { if (it.substring(0..2) == "hdo") "hdo" else "hdu" }
        val hdoInfoToJsonList = typeList["hdo"]?.map { HdoInfoToJson(it, isOn) }
                ?: ArrayList<HdoInfoToJson>()
        val hduInfoToJsonList = typeList["hdu"]?.map { HduInfoToJson(it, isOn) }
                ?: ArrayList<HduInfoToJson>()
        val couponInfoToJson = CouponInfoToJson(hdoInfoToJsonList, hduInfoToJsonList)
        return CouponDataToJson(listOf(couponInfoToJson))
    }

    private fun convert(data: CouponDataFromJson): CouponInfo {
        return CouponInfo(data.couponInfo.map {
            when (it.plan) {
                "Family Share", "Minimum Start", "Light Start"
                -> convertNormalData(it)
                "Eco Minimum", "Eco Standard", "Pay as you go"
                -> convertEcoData(it)
                null // TODO: remove null if api returns "giga plan"
                -> convertNormalData(it)
                else
                -> throw UndefinedPlanException("Undefined plan name")
            }
        })
    }

    private fun convertNormalData(normalInfo: CouponInfoFromJson): PlanInfo {
        val serviceCode = normalInfo.hddServiceCode
        val plan = normalInfo.plan ?: "unknown plan"
        val lineInfoList = ArrayList<LineInfo>()
        lineInfoList.addAll(normalInfo.hdoInfo?.map {
            LineInfo(
                    it.hdoServiceCode,
                    ServiceType.HDO,
                    it.number,
                    it.regulation,
                    it.couponUse
            )
        } ?: emptyList())
        lineInfoList.addAll(normalInfo.hduInfo?.map {
            LineInfo(
                    it.hduServiceCode,
                    ServiceType.HDU,
                    it.number,
                    it.regulation,
                    it.couponUse
            )
        } ?: emptyList())
        val hdoRemains = normalInfo.hdoInfo?.sumBy { it.coupon!!.sumBy { it.volume } } ?: 0
        val hduRemains = normalInfo.hduInfo?.sumBy { it.coupon!!.sumBy { it.volume } } ?: 0
        val normalInfoRemains = normalInfo.coupon?.sumBy { it.volume } ?: 0
        val remains = hdoRemains + hduRemains + normalInfoRemains
        return PlanInfo(serviceCode, plan, lineInfoList, remains)
    }

    private fun convertEcoData(ecoInfo: CouponInfoFromJson): PlanInfo {
        val serviceCode = ecoInfo.hddServiceCode
        val plan = ecoInfo.plan ?: "unknown plan"
        val remains = ecoInfo.remains!!
        val lineInfoList = ArrayList<LineInfo>()
        lineInfoList.addAll(ecoInfo.hduInfo?.map {
            LineInfo(
                    it.hduServiceCode,
                    ServiceType.HDU,
                    it.number,
                    it.regulation,
                    it.couponUse
            )
        } ?: emptyList())
        return PlanInfo(serviceCode, plan, lineInfoList, remains)
    }
}