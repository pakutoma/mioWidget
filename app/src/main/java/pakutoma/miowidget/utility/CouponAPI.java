package pakutoma.miowidget.utility;

import android.content.Context;
import android.content.SharedPreferences;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import pakutoma.miowidget.exception.NotFoundValidTokenException;

/**
 * CouponAPI access iijmio api.
 * Created by PAKUTOMA on 2016/09/29.
 */
public class CouponAPI {
    private String accessToken;

    public CouponAPI(Context context) throws NotFoundValidTokenException{
        SharedPreferences preferences = context.getSharedPreferences("iijmio_token", Context.MODE_PRIVATE);
        String accessToken = preferences.getString("X-IIJmio-Authorization","");
        if (accessToken.equals("")) {
            throw new NotFoundValidTokenException("Not found token in preference.");
        }
        this.accessToken = accessToken;
    }

    public CouponData getCouponData() throws IOException , NotFoundValidTokenException {
        String couponStatus = getCouponStatus();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode statusNode = mapper.readTree(couponStatus);

        boolean isOnCoupon = false;
        for (JsonNode hddServiceNode : statusNode.get("couponInfo")) {
            for (JsonNode hdoServiceNode : hddServiceNode.get("hdoInfo")) {
                isOnCoupon = hdoServiceNode.get("couponUse").asBoolean();
            }
        }
        int traffic = sumTraffic(statusNode);
        return new CouponData(traffic,isOnCoupon);
    }

    public boolean changeCouponStatus() throws IOException , NotFoundValidTokenException {
        String couponStatus = getCouponStatus();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode getNode = mapper.readTree(couponStatus);
        ObjectNode putNode = mapper.createObjectNode();
        ArrayNode hdoNode = putNode.putArray("couponInfo").addObject().putArray("hdoInfo");
        boolean nowStatus = false;
        for (JsonNode item : getNode.get("couponInfo").get(0).get("hdoInfo")) {
            ObjectNode sim = hdoNode.addObject();
            String hdoServiceCode = item.get("hdoServiceCode").asText();
            sim.put("hdoServiceCode",hdoServiceCode);
            nowStatus = !item.get("couponUse").asBoolean();
            sim.put("couponUse",nowStatus);
        }
        putCouponStatus(putNode);
        return nowStatus;
    }

    private int sumTraffic (JsonNode statusNode) {
        int traffic = 0;
        if (statusNode != null && statusNode.get("returnCode").asText().equals("OK")) {
            for (JsonNode item : statusNode.get("couponInfo").get(0).get("coupon")) {
                traffic += item.get("volume").asInt();
            }
            for (JsonNode item : statusNode.get("couponInfo").get(0).get("hdoInfo")) {
                traffic += item.get("coupon").get(0).get("volume").asInt();
            }
        }
        return traffic;
    }

    private String getCouponStatus() throws IOException , NotFoundValidTokenException{
        URL url = new URL("https://api.iijmio.jp/mobile/d/v1/coupon/");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setInstanceFollowRedirects(false);
        connection.setRequestProperty("X-IIJmio-Developer", "IilCI1xrAgqKrXV9Zt4");
        connection.setRequestProperty("X-IIJmio-Authorization", accessToken);
        connection.connect();
        String result = readStream(connection);
        connection.disconnect();
        return result;
    }

    private void putCouponStatus(ObjectNode sendJson) throws IOException , NotFoundValidTokenException{

        URL url = new URL("https://api.iijmio.jp/mobile/d/v1/coupon/");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setInstanceFollowRedirects(false);
        connection.setRequestProperty("X-IIJmio-Developer", "IilCI1xrAgqKrXV9Zt4");
        connection.setRequestProperty("X-IIJmio-Authorization", accessToken);
        connection.setRequestProperty("Accept-Language", "jp");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        OutputStream os = connection.getOutputStream();
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(os,sendJson);
        os.close();
        readStream(connection);
        connection.disconnect();
    }

    private String readStream(HttpURLConnection connection) throws IOException , NotFoundValidTokenException {
        StringBuilder sb = new StringBuilder();
        if (connection.getResponseCode() / 100 == 4 || connection.getResponseCode() / 100 == 5)
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "UTF-8"));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            connection.disconnect();
            if (sb.toString().contains("User Authorization Failure")) {
                throw new NotFoundValidTokenException("User Authorization Failure");
            } else {
                throw new IOException();
            }
        } else {
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            connection.disconnect();
            return sb.toString();
        }
    }
}


