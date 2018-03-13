package pakutoma.iijmiocouponwidget.utility

/**
 * CouponAPI用データクラス
 * Created by PAKUTOMA on 2018/02/20.
 */

data class CouponInfo(
        val planInfoList: List<PlanInfo>
)

data class PlanInfo(
        val serviceCode: String = "",
        val plan: String = "",
        val lineInfoList: List<LineInfo>,
        val remains: Int = 0
)

data class LineInfo(
        val serviceCode: String,
        val serviceType: ServiceType,
        val number: String = "",
        val regulation: Boolean = true,
        val couponUse: Boolean
)

enum class ServiceType {
    HDO,HDU
}