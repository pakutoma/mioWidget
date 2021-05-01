package pakutoma.iijmiocouponwidget.utility


/**
 * Created by PAKUTOMA on 2018/02/20.
 */

data class CouponDataFromJson(
        val returnCode: String?,
        val couponInfo: List<CouponInfoFromJson>
)

data class CouponInfoFromJson(
        val hddServiceCode: String,
        val plan: String?,
        val hdoInfo: List<HdoInfoFromJson>?,
        val hduInfo: List<HduInfoFromJson>?,
        val coupon: List<CouponFromJson>?,
        val history: List<HistoryFromJson>?,
        val remains: Int?
)

data class HdoInfoFromJson(
        val hdoServiceCode: String,
        val number: String,
        val iccid: String,
        val regulation: Boolean,
        val sms: Boolean,
        val voice: Boolean,
        val couponUse: Boolean,
        val coupon: List<CouponFromJson>?
)

data class HduInfoFromJson(
        val hduServiceCode: String,
        val number: String,
        val iccid: String,
        val regulation: Boolean,
        val sms: Boolean,
        val voice: Boolean,
        val couponUse: Boolean,
        val coupon: List<CouponFromJson>?
)

data class CouponFromJson(
        val volume: Int,
        val expire: String?,
        val type: String
)

data class HistoryFromJson(
        val date: String,
        val event: String,
        val volume: Int,
        val type: String
)

data class CouponDataToJson(
        val couponInfo: List<CouponInfoToJson>
)

data class CouponInfoToJson(
        val hdoInfo: List<HdoInfoToJson>,
        val hduInfo: List<HduInfoToJson>
)

data class HdoInfoToJson(
        val hdoServiceCode: String,
        val couponUse: Boolean
)

data class HduInfoToJson(
        val hduServiceCode: String,
        val couponUse: Boolean
)

data class ReturnCodeFromJson(
        val returnCode: String?
)