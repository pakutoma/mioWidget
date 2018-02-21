package pakutoma.miowidget.utility

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPut
import com.github.kittinunf.fuel.moshi.moshiDeserializerOf
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
        FuelManager.instance.baseParams = listOf(
                "X-IIJmio-Developer" to developerID,
                "X-IIJmio-Authorization" to accessToken
        )
    }

    fun fetchCouponInfo(): CouponInfo {
        val result = "/coupon".httpGet().responseObject<CouponDataFromJson>(moshiDeserializerOf<CouponDataFromJson>()).third
        val fetchedData = validateResult(result.component1(), result.component2())
        return convert(fetchedData)
    }

    fun changeCouponUse(isOn: Boolean,serviceCodeList: List<String>): Boolean {
        val couponDataToJson = packJsonClass(isOn,serviceCodeList)
        val moshi = Moshi.Builder().build()
        val adapter = moshi.adapter(CouponDataToJson::class.java)
        val json = adapter.toJson(couponDataToJson)
        val result = "/coupon".httpPut()
                .header("Content-type" to  "application/json")
                .body(json)
                .responseObject<ReturnCodeFromJson>(moshiDeserializerOf<ReturnCodeFromJson>()).third
        return result is Result.Success
    }

    private fun packJsonClass(isOn: Boolean,serviceCodeList: List<String>) : CouponDataToJson {
        val typeList = serviceCodeList.groupBy { if(it.substring(0..2) == "hdo") "hdo" else "hda"}
        val hdoInfoToJsonList = typeList["hdo"]?.map{HdoInfoToJson(it,isOn)} ?: ArrayList<HdoInfoToJson>()
        val hduInfoToJsonList = typeList["hdu"]?.map{HduInfoToJson(it,isOn)} ?: ArrayList<HduInfoToJson>()
        val couponInfoToJson = CouponInfoToJson(hdoInfoToJsonList,hduInfoToJsonList)
        return CouponDataToJson(couponInfoToJson)
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
        return when (data.couponInfo.plan) {
            "Family Share", "Minimum Start", "Light Start"
            -> convertNormalData(data)
            "Eco Minimum", "Eco Standard"
            -> convertEcoData(data)
            else -> throw UndefinedPlanException("Undefined plan name")
        }
    }

    private fun convertNormalData(data: CouponDataFromJson): CouponInfo {
        val normalInfo = data.couponInfo
        val hddServiceCode = normalInfo.hddServiceCode
        val plan = normalInfo.plan
        val lineInfoList = ArrayList<LineInfo>()
        lineInfoList.addAll(normalInfo.hdoInfo!!.map {
            LineInfo(
                    it.hdoServiceCode,
                    ServiceType.HDO,
                    it.number,
                    it.regulation,
                    it.couponUse
            )
        })
        lineInfoList.addAll(normalInfo.hduInfo.map {
            LineInfo(
                    it.hduServiceCode,
                    ServiceType.HDU,
                    it.number,
                    it.regulation,
                    it.couponUse
            )
        })
        val hdoRemains = normalInfo.hdoInfo.sumBy{it.coupon!!.sumBy { it.volume }}
        val hduRemains = normalInfo.hduInfo.sumBy{it.coupon!!.sumBy { it.volume }}
        val normalInfoRemains = normalInfo.coupon!!.sumBy { it.volume }
        val remains = hdoRemains + hduRemains + normalInfoRemains
        return CouponInfo(hddServiceCode,plan,lineInfoList,remains)
    }

    private fun convertEcoData(data: CouponDataFromJson): CouponInfo {
        val ecoInfo = data.couponInfo
        val hddServiceCode = ecoInfo.hddServiceCode
        val plan = ecoInfo.plan
        val remains = ecoInfo.remains!!
        val lineInfoList = ArrayList<LineInfo>()
        lineInfoList.addAll(ecoInfo.hduInfo.map {
            LineInfo(
                    it.hduServiceCode,
                    ServiceType.HDU,
                    it.number,
                    it.regulation,
                    it.couponUse
            )
        })
        return CouponInfo(hddServiceCode,plan,lineInfoList,remains)
    }

}