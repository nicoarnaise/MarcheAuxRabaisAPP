package com.geasser.marcheauxrabais;

import android.os.AsyncTask;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Nicolas & Vincent on 27/10/2016.
 */

public class BddExt extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(String... req) {
        InputStream is = null;
        try {
            final HttpURLConnection conn = (HttpURLConnection) new URL("http://n.arnaise.free.fr/mar/request.php").openConnection();
            conn.setRequestMethod("POST");
            Map<String,String> params = new LinkedHashMap<>();
            params.put("query",req[0]);
            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String,String> param : params.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(param.getValue(), "UTF-8"));
            }
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");

            conn.setDoInput(true);
            conn.setDoOutput(true);

            // Starts the query
            conn.getOutputStream().write(postDataBytes); // = conn.connect avec la methode POST
            is = conn.getInputStream();
            // Read the InputStream and save it in a string
            return readIt(is);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        } catch (ProtocolException e) {
            e.printStackTrace();
            return null;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            // Makes sure that the InputStream is closed after the app is
            // finished using it.
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static String readIt(InputStream is) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            response.append(line).append('\n');
        }
        return response.toString();
    }
}
