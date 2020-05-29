package com.example.newsreader;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> arrayList = new ArrayList<>();
    ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DownloadTask task = new DownloadTask();

        try {
            task.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");
        } catch (Exception e) {
            e.printStackTrace();
        }

        ListView listView = findViewById(R.id.listView);

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayList);

        listView.setAdapter(arrayAdapter);
    }

    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {

            String res = "";

            URL url;
            HttpURLConnection httpURLConnection = null;

            try {
                // connecting to the API for news article
                url = new URL(strings[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();

                InputStream inputStream = httpURLConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);

                int data = reader.read();

                while(data != -1) {
                    char inputData = (char) data;
                    res += inputData;

                    data = reader.read();
                }

                // Response as Array of News Id
                JSONArray jsonArray = new JSONArray(res);

                for (int i = 0; i < jsonArray.length(); i++) {
                    // Getting Particular News from id
                    String articleId = jsonArray.getString(i);

                    // connecting to the article
                    url = new URL("https://hacker-news.firebaseio.com/v0/item/" + articleId + ".json?print=pretty");
                    httpURLConnection = (HttpURLConnection) url.openConnection();

                    inputStream = httpURLConnection.getInputStream();
                    reader = new InputStreamReader(inputStream);

                    String articleInfo = "";
                    data = reader.read();

                    // reading the data received by the API
                    while(data != -1) {
                        char inputData = (char) data;
                        articleInfo += inputData;

                        data = reader.read();
                    }

                    JSONObject obj = new JSONObject(articleInfo);

                    // data of the article is not null
                    if(obj.isNull("title") && obj.isNull("url")) {
                        Log.i("String title and url", obj.getString("title") + " " + obj.getString("url"));
                    }
                }

//                Log.i("Response Data", res);

                return res;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
