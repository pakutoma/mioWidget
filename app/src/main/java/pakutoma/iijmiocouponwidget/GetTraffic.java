package pakutoma.iijmiocouponwidget;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by PAKUTOMA on 2016/06/21.
 * Get Traffic Function
 */
public class GetTraffic extends IntentService {
    private static final String ACTION_CALLBACK_GET_TRAFFIC = "pakutoma.iijmiocouponwidget.SwitchWidget.ACTION_CALLBACK_GET_TRAFFIC";

    public GetTraffic() {
        super("GetTraffic");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        HttpURLConnection connection;
        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL("https://api.iijmio.jp/mobile/d/v1/coupon/");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("X-IIJmio-Developer", "IilCI1xrAgqKrXV9Zt4");
            connection.setRequestProperty("X-IIJmio-Authorization", "0O1PXxXNIfdPLeP4A04NJ9C3J1OTBVL1466469891");
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
        JsonNode node = null;
        try {
            node = mapper.readTree(sb.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
        int traffic = 0;
        if (node != null && node.get("returnCode").asText().equals("OK")) {
            for (JsonNode item : node.get("couponInfo").get(0).get("coupon")) {
                traffic += item.get("volume").asInt();
            }
            for (JsonNode item : node.get("couponInfo").get(0).get("hdoInfo")) {
                traffic += item.get("coupon").get(0).get("volume").asInt();
            }
        }
        Intent callbackIntent = new Intent(ACTION_CALLBACK_GET_TRAFFIC);
        callbackIntent.putExtra("TRAFFIC",traffic);
        callbackIntent.setPackage("pakutoma.iijmiocouponwidget");
        startService(callbackIntent);
    }
}