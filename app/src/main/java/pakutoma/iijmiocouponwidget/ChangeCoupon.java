package pakutoma.iijmiocouponwidget;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by PAKUTOMA on 2016/06/21.
 */
public class ChangeCoupon extends IntentService {
    private static final String ACTION_CALLBACK_CHANGE_COUPON = "pakutoma.iijmiocouponwidget.SwitchWidget.ACTION_CALLBACK_CHANGE_COUPON";

    public ChangeCoupon() {
        super("ChangeCoupon");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences preferences = getSharedPreferences("iijmio_token", MODE_PRIVATE);
        String accessToken = preferences.getString("X-IIJmio-Authorization","");
        if (accessToken.equals("")) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("has_token",false);
            editor.apply();

            Intent callbackIntent = new Intent(ACTION_CALLBACK_CHANGE_COUPON);
            callbackIntent.putExtra("HAS_TOKEN",false);
            callbackIntent.setPackage("pakutoma.iijmiocouponwidget");
            startService(callbackIntent);
            return;
        }
        HttpURLConnection connection;
        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL("https://api.iijmio.jp/mobile/d/v1/coupon/");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("X-IIJmio-Developer", "IilCI1xrAgqKrXV9Zt4");
            connection.setRequestProperty("X-IIJmio-Authorization", accessToken);
            connection.connect();
            BufferedReader br = new BufferedReader( new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ObjectMapper mapper = new ObjectMapper();
        JsonNode getNode = null;
        try {
            getNode = mapper.readTree(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        ObjectNode putNode = mapper.createObjectNode();
        ArrayNode hdoNode = putNode.putArray("couponInfo").addObject().putArray("hdoInfo");
        for (JsonNode item : getNode.get("couponInfo").get(0).get("hdoInfo")) {
            ObjectNode sim = hdoNode.addObject();
            sim.put("hdoServiceCode",item.get("hdoServiceCode").asText());
            sim.put("couponUse",intent.getBooleanExtra("SWITCH",false));
        }
        try {
            URL url = new URL("https://api.iijmio.jp/mobile/d/v1/coupon/");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("Accept-Language", "jp");
            connection.setDoOutput(true);
            connection.setRequestProperty("X-IIJmio-Developer", "IilCI1xrAgqKrXV9Zt4");
            connection.setRequestProperty("X-IIJmio-Authorization", accessToken);
            connection.setRequestProperty("Content-Type", "application/json");
            OutputStream os = connection.getOutputStream();
            mapper.writeValue(os,putNode);
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
            String line;
            sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String returnCode = "";
        try {
            returnCode = mapper.readTree(sb.toString()).get("returnCode").asText();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent callbackIntent = new Intent(ACTION_CALLBACK_CHANGE_COUPON);
        callbackIntent.putExtra("CHANGE",returnCode.equals("OK"));
        callbackIntent.putExtra("HAS_TOKEN",true);
        callbackIntent.putExtra("RETURN_CODE",returnCode);
        callbackIntent.setPackage("pakutoma.iijmiocouponwidget");
        startService(callbackIntent);
    }
}
