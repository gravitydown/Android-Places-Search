package com.example.sanesean.csci571_hw9;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by sanesean on 2018/4/7.
 */

class GetUrlContentTask extends AsyncTask<String, Integer, String> {

    protected String doInBackground(String... urls) {//two strings: 0=url,1=params
        String inputLine,result;
        try {
            String payload=urls[1];
            URL url = new URL(urls[0]);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setDoInput(true);
            connection.connect();
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
            writer.write(payload);
            writer.close();


            InputStreamReader streamReader = new
                    InputStreamReader(connection.getInputStream());
            BufferedReader reader = new BufferedReader(streamReader);
            StringBuilder stringBuilder = new StringBuilder();
            while((inputLine = reader.readLine()) != null){
                stringBuilder.append(inputLine);
            }
            reader.close();
            streamReader.close();
            result = stringBuilder.toString();

            return result;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    protected void onProgressUpdate(Integer... progress) {
    }
    protected void onPostExecute(String result) {
        // this is executed on the main thread after the process is over
        // update your UI here

    }
    @Override
    protected void onPreExecute() {
        //show a dialog
        super.onPreExecute();
    }
}
