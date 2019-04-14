package pakutoma.iijmiocouponwidget.utility

import kotlinx.coroutines.runBlocking
import org.junit.Test

import org.junit.Assert.*

/**
 * Created by PAKUTOMA on 2018/02/21.
 */
class CouponAPITest {
    private val accessToken = "HfXcx3zYSrG3ukkQtZjsQATkqX191aw1553251721"
    private val developerID = "IilCI1xrAgqKrXV9Zt4"
    @Test
    fun fetchCouponInfo() {
        runBlocking {
            val coupon = CouponAPI(developerID, accessToken)
            println(coupon.fetchCouponInfo())
        }
    }

    @Test
    fun changeCouponUse() {
        runBlocking {
            val coupon = CouponAPI(developerID, accessToken)
            assertTrue(coupon.changeCouponUse(true, listOf("hdo22456045")).returnCode.equals("OK"))
        }
    }

}