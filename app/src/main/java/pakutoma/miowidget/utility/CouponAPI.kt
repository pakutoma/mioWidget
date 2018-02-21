package pakutoma.miowidget.utility

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPut
import com.github.kittinunf.fuel.moshi.moshiDeserializerOf
import com.github.kittinunf.fuel.moshi.responseObject
import com.github.kittinunf.result.Result
import com.squareup.moshi.Moshi
import pakutoma.miowidget.exception.NotFoundValidTokenException
import pakutoma.miowidget.exception.UndefinedPlanException
import java.io.IOException

/**
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

    fun fetchCouponInfo(): CouponInfo {
        val (_, _, result) = "/coupon/".httpGet().responseObject<CouponDataFromJson>()
        val fetchedData = validateResult(result.component1(), result.component2())
        return convert(fetchedData)
    }

    fun changeCouponUse(isOn: Boolean, serviceCodeList: List<String>): Boolean {
        val couponDataToJson = packJsonClass(isOn, serviceCodeList)
        val moshi = Moshi.Builder().build()
        val adapter = moshi.adapter(CouponDataToJson::class.java)
        val json = adapter.toJson(couponDataToJson)
        val (_, _, result) = "/coupon/".httpPut()
                .header("Content-Type" to "application/json")
                .body(json)
                .responseObject<ReturnCodeFromJson>(moshiDeserializerOf<ReturnCodeFromJson>())
        return result is Result.Success
    }

    private fun packJsonClass(isOn: Boolean, serviceCodeList: List<String>): CouponDataToJson {
        val typeList = serviceCodeList.groupBy { if (it.substring(0..2) == "hdo") "hdo" else "hda" }
        val hdoInfoToJsonList = typeList["hdo"]?.map { HdoInfoToJson(it, isOn) }
                ?: ArrayList<HdoInfoToJson>()
        val hduInfoToJsonList = typeList["hdu"]?.map { HduInfoToJson(it, isOn) }
                ?: ArrayList<HduInfoToJson>()
        val couponInfoToJson = CouponInfoToJson(hdoInfoToJsonList, hduInfoToJsonList)
        return CouponDataToJson(listOf(couponInfoToJson))
    }


    private fun validateResult(data: CouponDataFromJson?, error: FuelError?): CouponDataFromJson {
        if (error != null) {
            if (data?.returnCode == null) {
                throw IOException()
            }
            if (data.returnCode.contains("User Authorization Failure")) {
                throw NotFoundValidTokenException("User Authorization Failure")
            } else {
                throw IOException()
            }
        } else if (data == null) {
            throw IOException()
        }
        return data
    }

    private fun convert(data: CouponDataFromJson): CouponInfo {
        return CouponInfo(data.couponInfo.map {
            when (it.plan) {
                "Family Share", "Minimum Start", "Light Start"
                -> convertNormalData(it)
                "Eco Minimum", "Eco Standard"
                -> convertEcoData(it)
                else
                -> throw UndefinedPlanException("Undefined plan name")
            }
        })
    }

    private fun convertNormalData(normalInfo: CouponInfoFromJson): PlanInfo {
        val serviceCode = normalInfo.hddServiceCode
        val plan = normalInfo.plan
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
        val plan = ecoInfo.plan
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