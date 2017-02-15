package com.jokrapp.android.data;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

/**
 * Created by pat on 10/28/2015.
 */
public class RestClient {

    public RestResponse get(String url) {
        RestResponse response = new RestResponse();
        HttpURLConnection con = null;
        try {
            URL obj = new URL(url);
            con = (HttpURLConnection) obj.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();

            response.setCode(responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer responseBody = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                responseBody.append(inputLine);
            }
            in.close();
            response.setBody(responseBody.toString());


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (con != null)
                con.disconnect();
        }

        return response;
    }

    public RestResponse post(String url, String content) {
        RestResponse response = new RestResponse();
        HttpURLConnection con = null;
        try {
            URL obj = new URL(url);
            con = (HttpURLConnection) obj.openConnection();

            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestMethod("POST");
            con.getOutputStream();

            // Send post request
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.write(Charset.defaultCharset().encode(content).array());
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();

            response.setCode(responseCode);


            try {
                InputStream is = con.getInputStream();
                if (is != null) {
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(is));
                    String inputLine;
                    StringBuffer responseBody = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        responseBody.append(inputLine);
                    }
                    in.close();
                    response.setBody(responseBody.toString());
                }
            } catch (IOException e) {
                System.out.println("Failed to read response: " + e.getMessage());
            }


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (con != null)
                con.disconnect();
        }

        return response;
    }
}
