package pakutoma.iijmiocouponwidget;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * CouponAPI can access iijmio api.
 * Created by PAKUTOMA on 2016/09/29.
 */
public class CouponAPI {
    HttpURLConnection connection;
    StringBuilder sb;
    public String getCouponStatus(String accessToken) throws IOException {
        return accessCouponStatus("GET",accessToken,null);
    }

    public String putCouponStatus(String accessToken,ObjectNode sendJson) throws IOException {
        return accessCouponStatus("PUT",accessToken,sendJson);
    }

    private String accessCouponStatus(String request,String accessToken,ObjectNode sendJson) throws IOException {
        sb = new StringBuilder();
        URL url = new URL("https://api.iijmio.jp/mobile/d/v1/coupon/");
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(request);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestProperty("X-IIJmio-Developer", "IilCI1xrAgqKrXV9Zt4");
        connection.setRequestProperty("X-IIJmio-Authorization", accessToken);
        if (request.equals("PUT")) {
            connection.setRequestProperty("Accept-Language", "jp");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            OutputStream os = connection.getOutputStream();
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(os,sendJson);
        } else if (request.equals("GET")) {
            connection.connect();
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        connection.disconnect();
        return sb.toString();
    }
}
