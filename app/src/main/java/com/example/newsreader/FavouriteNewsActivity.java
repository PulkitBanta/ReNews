package com.example.newsreader;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Objects;

public class FavouriteNewsActivity extends AppCompatActivity {

    ListView listView;
    ArrayAdapter arrayAdapter;
    ArrayList<String> titles;
    ArrayList<String> urls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite_news);

        Intent intent = getIntent();

        titles = new ArrayList<String>(Objects.requireNonNull(intent.getStringArrayListExtra("favItems")));
        urls = new ArrayList<String>(Objects.requireNonNull(intent.getStringArrayListExtra("favUrls")));

        listView = findViewById(R.id.listView);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, titles);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent urlIntent = new Intent(getApplicationContext(), ArticleActivity.class);
                urlIntent.putExtra("url", urls.get(position));
                startActivity(urlIntent);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                titles.remove(position);
                urls.remove(position);

                arrayAdapter.notifyDataSetChanged();
                Toast.makeText(FavouriteNewsActivity.this, "Item deleted from favourites", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    public void back(View view) {
        finish();
    }
}