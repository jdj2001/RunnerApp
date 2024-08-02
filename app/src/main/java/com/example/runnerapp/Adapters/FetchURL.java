package com.example.runnerapp.Adapters;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FetchURL extends AsyncTask<String, Void, String> {

    private Context mContext;
    private String directionMode = "driving";

    public FetchURL(Context context) {
        mContext = context;
    }

    @Override
    protected String doInBackground(String... strings) {
        String data = "";
        try {
            data = downloadUrl(strings[0]);
            Log.d("FetchURL", "URL obtenida: " + data);
        } catch (Exception e) {
            Log.d("Background Task", e.toString());
        }
        return data;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        ParserTask parserTask = new ParserTask(mContext);
        parserTask.execute(s);
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();

            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
}

