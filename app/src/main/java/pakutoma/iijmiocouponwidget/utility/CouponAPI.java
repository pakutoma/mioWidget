package pakutoma.iijmiocouponwidget.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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
import java.util.ArrayList;
import java.util.List;

import pakutoma.iijmiocouponwidget.exception.NotFoundValidTokenException;
import pakutoma.iijmiocouponwidget.utility.CouponData;

/**
 * CouponAPI access iijmio api.
 * Created by PAKUTOMA on 2016/09/29.
 */
public class CouponAPI {
    private String accessToken;
    private String useAccountId;

    public CouponAPI(Context context) throws NotFoundValidTokenException {
        this(context,true);
    }

    public CouponAPI(Context context,boolean canSqlite) throws NotFoundValidTokenException{
        SharedPreferences preferences = context.getSharedPreferences("iijmio_token", context.MODE_PRIVATE);
        String accessToken = preferences.getString("X-IIJmio-Authorization","");
        if (accessToken.equals("")) {
            throw new NotFoundValidTokenException("Not found token in preference.");
        }
        this.accessToken = accessToken;
        if (canSqlite) {
            AccountDBOpenHelper helper = new AccountDBOpenHelper(context);
            SQLiteDatabase db = helper.getReadableDatabase();
            Cursor cursor = db.rawQuery("select * from accounttable where canSwitch = 1",null);
            cursor.moveToFirst();

            useAccountId = cursor.getString(cursor.getColumnIndex("hdoServiceCode"));
            cursor.close();
            db.close();
        }
    }

    public CouponData getCouponData() throws IOException , NotFoundValidTokenException {
        String couponStatus = getCouponStatus();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode statusNode = mapper.readTree(couponStatus);

        boolean isOnCoupon = false;
        for (JsonNode hddServiceNode : statusNode.get("couponInfo")) {
            for (JsonNode hdoServiceNode : hddServiceNode.get("hdoInfo")) {
                String hdoServiceCode = hdoServiceNode.get("hdoServiceCode").asText();
                if (hdoServiceCode.equals(useAccountId)) {
                    isOnCoupon = hdoServiceNode.get("couponUse").asBoolean();
                }
            }
        }
        int traffic = sumTraffic(statusNode);
        return new CouponData(traffic,isOnCoupon);
    }

    public List<AccountData> getAccountsData() throws IOException,NotFoundValidTokenException{
        String couponStatus = getCouponStatus();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode statusNode = mapper.readTree(couponStatus);
        List<AccountData> accountDataList = new ArrayList<>();
        for (JsonNode hddServiceNode : statusNode.get("couponInfo")) {
            for (JsonNode hdoServiceNode : hddServiceNode.get("hdoInfo")) {
                String hdoServiceCode = hdoServiceNode.get("hdoServiceCode").asText();
                String number = hdoServiceNode.get("number").asText();
                boolean regulation = hdoServiceNode.get("regulation").asBoolean();
                boolean couponUse = hdoServiceNode.get("couponUse").asBoolean();
                AccountData ad = new AccountData(hdoServiceCode,number,regulation,couponUse);
                accountDataList.add(ad);
            }
        }

        return accountDataList;
    }

    public void changeCouponStatus(CouponData cd) throws IOException , NotFoundValidTokenException {
        String couponStatus = getCouponStatus();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode getNode = mapper.readTree(couponStatus);
        ObjectNode putNode = mapper.createObjectNode();
        ArrayNode hdoNode = putNode.putArray("couponInfo").addObject().putArray("hdoInfo");
        for (JsonNode item : getNode.get("couponInfo").get(0).get("hdoInfo")) {
            ObjectNode sim = hdoNode.addObject();
            String hdoServiceCode = item.get("hdoServiceCode").asText();
            sim.put("hdoServiceCode",hdoServiceCode);
            sim.put("couponUse",hdoServiceCode.equals(useAccountId) ? cd.getSwitch() : item.get("couponUse").asBoolean());
        }
        putCouponStatus(putNode);
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
        StringBuilder sb = new StringBuilder();
        URL url = new URL("https://api.iijmio.jp/mobile/d/v1/coupon/");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setInstanceFollowRedirects(false);
        connection.setRequestProperty("X-IIJmio-Developer", "IilCI1xrAgqKrXV9Zt4");
        connection.setRequestProperty("X-IIJmio-Authorization", accessToken);
        connection.connect();
        return readStream(connection);
    }

    private String putCouponStatus(ObjectNode sendJson) throws IOException , NotFoundValidTokenException{

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
        return readStream(connection);
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
            if (sb.toString().indexOf("User Authorization Failure") != -1) {
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


