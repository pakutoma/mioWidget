package pakutoma.miowidget.utility

import android.content.Context
import android.content.SharedPreferences

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

import pakutoma.miowidget.exception.NotFoundValidTokenException

/**
 * CouponAPI access iijmio api.
 * Created by PAKUTOMA on 2016/09/29.
 */
class OldCouponAPI @Throws(NotFoundValidTokenException::class)
constructor(context: Context) {
    private val accessToken: String

    init {
        val preferences = context.getSharedPreferences("iijmio_token", Context.MODE_PRIVATE)
        val accessToken = preferences.getString("X-IIJmio-Authorization", "")
        if (accessToken == "") {
            throw NotFoundValidTokenException("Not found token in preference.")
        }
        this.accessToken = accessToken
    }

    @Throws(IOException::class, NotFoundValidTokenException::class)
    fun fetchCouponData(): CouponData {
        val couponStatus = fetchCouponStatus()
        val mapper = ObjectMapper()
        val statusNode = mapper.readTree(couponStatus)

        var isOnCoupon = false
        for (hddServiceNode in statusNode.get("couponInfo")) {
            for (hdoServiceNode in hddServiceNode.get("hdoInfo")) {
                isOnCoupon = hdoServiceNode.get("couponUse").asBoolean()
            }
        }
        statusNode.get("couponInfo");
        val traffic = sumTraffic(statusNode)
        return CouponData(traffic, isOnCoupon)
    }


    @Throws(IOException::class, NotFoundValidTokenException::class)
    private fun fetchCouponStatus(): String {
        val url = URL("https://api.iijmio.jp/mobile/d/v1/coupon/")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.instanceFollowRedirects = false
        connection.setRequestProperty("X-IIJmio-Developer", "IilCI1xrAgqKrXV9Zt4")
        connection.setRequestProperty("X-IIJmio-Authorization", accessToken)
        connection.connect()
        val result = readStream(connection)
        connection.disconnect()
        return result
    }

    @Throws(IOException::class, NotFoundValidTokenException::class)
    fun changeCouponStatus(): Boolean {
        val couponStatus = fetchCouponStatus()
        val mapper = ObjectMapper()
        val getNode = mapper.readTree(couponStatus)
        val putNode = mapper.createObjectNode()
        val hdoNode = putNode.putArray("couponInfo").addObject().putArray("hdoInfo")
        var nowStatus = false
        for (item in getNode.get("couponInfo").get(0).get("hdoInfo")) {
            val sim = hdoNode.addObject()
            val hdoServiceCode = item.get("hdoServiceCode").asText()
            sim.put("hdoServiceCode", hdoServiceCode)
            nowStatus = !item.get("couponUse").asBoolean()
            sim.put("couponUse", nowStatus)
        }
        putCouponStatus(putNode)
        return nowStatus
    }

    private fun sumTraffic(statusNode: JsonNode?): Int {
        var traffic = 0
        if (statusNode != null && statusNode.get("returnCode").asText() == "OK") {
            for (item in statusNode.get("couponInfo").get(0).get("coupon")) {
                traffic += item.get("volume").asInt()
            }
            for (item in statusNode.get("couponInfo").get(0).get("hdoInfo")) {
                traffic += item.get("coupon").get(0).get("volume").asInt()
            }
        }
        return traffic
    }

    @Throws(IOException::class, NotFoundValidTokenException::class)
    private fun putCouponStatus(sendJson: ObjectNode) {

        val url = URL("https://api.iijmio.jp/mobile/d/v1/coupon/")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "PUT"
        connection.instanceFollowRedirects = false
        connection.setRequestProperty("X-IIJmio-Developer", "IilCI1xrAgqKrXV9Zt4")
        connection.setRequestProperty("X-IIJmio-Authorization", accessToken)
        connection.setRequestProperty("Accept-Language", "jp")
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json")
        val os = connection.outputStream
        val mapper = ObjectMapper()
        mapper.writeValue(os, sendJson)
        os.close()
        readStream(connection)
        connection.disconnect()
    }

    @Throws(IOException::class, NotFoundValidTokenException::class)
    private fun readStream(connection: HttpURLConnection): String {
        val sb = StringBuilder()
        if (connection.responseCode / 100 == 4 || connection.responseCode / 100 == 5) {
            val br = BufferedReader(InputStreamReader(connection.errorStream, "UTF-8"))
            var line: String
            while (true) {
                line = br.readLine()
                if (line == null) {
                    break
                }
                sb.append(line)
            }
            br.close()
            connection.disconnect()
            if (sb.toString().contains("User Authorization Failure")) {
                throw NotFoundValidTokenException("User Authorization Failure")
            } else {
                throw IOException()
            }
        } else {
            val br = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8"))
            var line: String
            while (true) {
                line = br.readLine()
                if (line == null) {
                    break
                }
                sb.append(line)
            }
            br.close()
            connection.disconnect()
            return sb.toString()
        }
    }
}


