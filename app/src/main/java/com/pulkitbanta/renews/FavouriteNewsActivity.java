package com.pulkitbanta.renews;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class FavouriteNewsActivity extends AppCompatActivity {

    ListView listView;
    ArrayAdapter<String> arrayAdapter;
    SQLiteDatabase database;
    ArrayList<String> titles = new ArrayList<>();
    ArrayList<String> urls = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite_news);

        database = MainActivity.articlesDB;

        listView = findViewById(R.id.listView);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, titles);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), ArticleActivity.class);
                intent.putExtra("url", urls.get(position));

                startActivity(intent);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                String[] whereArgs = {titles.get(position)};

                database.delete("favourites", "title=?", whereArgs);

                titles.remove(position);
                urls.remove(position);

                arrayAdapter.notifyDataSetChanged();
                Toast.makeText(FavouriteNewsActivity.this, "Item Removed", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        updateListView();
    }

    public void back(View view) {
        finish();
    }

    public void updateListView() {
        Cursor c = database.rawQuery("SELECT * FROM favourites", null);

        int urlsIndex = c.getColumnIndex("url");
        int titleIndex = c.getColumnIndex("title");

        if (c.moveToFirst()) {
            titles.clear();
            urls.clear();

            do {
                titles.add(c.getString(titleIndex));
                urls.add(c.getString(urlsIndex));
            } while (c.moveToNext());

            arrayAdapter.notifyDataSetChanged();
        }
        c.close();
    }
}