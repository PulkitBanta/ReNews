package com.example.newsreader;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> titles = new ArrayList<>();
    ArrayList<String> urls = new ArrayList<>();

    ArrayAdapter<String> arrayAdapter;
    SQLiteDatabase articlesDB;
    ListView listView;
    ProgressBar progressBar;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // creating a new database
        articlesDB = this.openOrCreateDatabase("Articles", MODE_PRIVATE, null);

        // creating a table in the database with column names
        articlesDB.execSQL("CREATE TABLE IF NOT EXISTS articles (id INTEGER PRIMARY KEY, articleID INTEGER, title VARCHAR, url VARCHAR)");


        // Downloading the articles in the background
        DownloadTask task = new DownloadTask();
        try {
            task.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");
        } catch (Exception e) {
            e.printStackTrace();
        }

        listView = findViewById(R.id.listView);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, titles);
        listView.setAdapter(arrayAdapter);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.textView);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), ArticleActivity.class);
                intent.putExtra("url", urls.get(position));

                startActivity(intent);
            }
        });

        checkList();
    }

    public void checkList() {
        if(titles.size() > 0) {
            progressBar.setVisibility(View.GONE);
            textView.setText("Today's top 30 News");
        } else {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    public void updateListView() {
        Cursor c = articlesDB.rawQuery("SELECT * FROM articles", null);

        int urlsIndex = c.getColumnIndex("url");
        int titleIndex = c.getColumnIndex("title");

        if(c.moveToFirst()) {
            titles.clear();
            urls.clear();

            do {
                titles.add(c.getString(titleIndex));
                urls.add(c.getString(urlsIndex));
            } while(c.moveToNext());

            arrayAdapter.notifyDataSetChanged();
        }
        c.close();

        checkList();
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

                // Clearing Database
                articlesDB.execSQL("DELETE FROM articles");

                int newsLength = 20;

                if(newsLength > jsonArray.length())
                    newsLength = jsonArray.length();

                for (int i = 0; i < newsLength; i++) {
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
                    if(!obj.isNull("title") && !obj.isNull("url")) {

                        // url received from the API
                        String articleUrl = obj.getString("url");
                        String articleTitle = obj.getString("title");

                        // adding data to the SQL table
                        String sql = "INSERT INTO articles (articleID, title, url) VALUES (?, ?, ?)";
                        SQLiteStatement statement = articlesDB.compileStatement(sql);
                        statement.bindString(1, articleId);
                        statement.bindString(2, articleTitle);
                        statement.bindString(3, articleUrl);

                        statement.execute();
                    }
                }
                return res;

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            updateListView();
        }
    }
}
