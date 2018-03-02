package pakutoma.miowidget.utility

import org.junit.Test

import org.junit.Assert.*

/**
 * Created by PAKUTOMA on 2018/02/21.
 */
class CouponAPITest {
    private val accessToken = "Pns2pgN62W6po8wv23v2YHzW87E0kPn1519200200"
    private val developerID = "IilCI1xrAgqKrXV9Zt4"
    @Test
    fun fetchCouponInfo() {
        val coupon = CouponAPI(developerID,accessToken)
        coupon.fetchCouponInfo()
    }

    @Test
    fun changeCouponUse() {
        val coupon = CouponAPI(developerID,accessToken)
        assertTrue(coupon.changeCouponUse(true, listOf("hdo22456045")))
    }

}