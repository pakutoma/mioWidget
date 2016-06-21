package pakutoma.iijmiocouponwidget;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.io.BufferedReader;
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
        String regex = "\"volume\": (\\d+)";
        Pattern p = Pattern.compile(regex,Pattern.MULTILINE);
        Matcher m = p.matcher(sb.toString());
        int traffic = 0;
        while (m.find()){
            traffic += Integer.parseInt(m.group(1));
        }
        Intent callbackIntent = new Intent(ACTION_CALLBACK_GET_TRAFFIC);
        callbackIntent.putExtra("TRAFFIC",traffic);
        Log.d("GetTraffic", String.valueOf(traffic));
        Log.d("GetTraffic", String.valueOf(callbackIntent.getAction()));
        callbackIntent.setPackage("pakutoma.iijmiocouponwidget");
        startService(callbackIntent);
    }
}