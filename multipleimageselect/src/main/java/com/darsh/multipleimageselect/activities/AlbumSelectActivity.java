package com.darsh.multipleimageselect.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;

import com.darsh.multipleimageselect.R;
import com.darsh.multipleimageselect.adapters.CustomAlbumSelectAdapter;
import com.darsh.multipleimageselect.helpers.Constants;
import com.darsh.multipleimageselect.models.Album;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Darshan on 4/14/2015.
 */
public class AlbumSelectActivity extends AppCompatActivity {
    private ArrayList<Album> albums;

    private ProgressBar progressBar;
    private GridView gridView;
    private CustomAlbumSelectAdapter customAlbumSelectAdapter;

    private ContentObserver contentObserver;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Select Photo Album");
        setContentView(R.layout.activity_album_select);

        Intent intent = getIntent();
        if (intent == null) {
            finish();
        }
        Constants.limit = intent.getIntExtra(Constants.INTENT_EXTRA_LIMIT, Constants.DEFAULT_LIMIT);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar_album_select);
        gridView = (GridView) findViewById(R.id.grid_view_album_select);
    }

    @Override
    protected void onStart() {
        super.onStart();

        /*
        Create and register content observer for observing changes in MediaStore.Images.Media.EXTERNAL_CONTENT_URI
         */
        handler = new Handler();
        contentObserver = new ContentObserver(handler) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                loadAlbums();
            }
        };
        getContentResolver().registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false, contentObserver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAlbums();
    }

    @Override
    protected void onStop() {
        super.onStop();

        /*
        Unregister content observer
         */
        getContentResolver().unregisterContentObserver(contentObserver);
        handler.removeCallbacksAndMessages(null);
        handler = null;
        contentObserver = null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        orientationBasedUI(newConfig.orientation);
    }

    private AdapterView.OnItemClickListener aOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(getApplicationContext(), ImageSelectActivity.class);
            intent.putExtra(Constants.INTENT_EXTRA_ALBUM, albums.get(position).name);
            intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
            startActivity(intent);
            finish();
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        finish();
    }

    private void orientationBasedUI(int orientation) {
        final WindowManager windowManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        final DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);

        int width;
        int height;
        if (metrics.widthPixels < metrics.heightPixels) {
            width = (metrics.widthPixels - 3) / 2;
            height = (metrics.widthPixels - 3) / 2;
        } else {
            width = (metrics.widthPixels - 5) / 4;
            height = (metrics.widthPixels - 5) / 4;
        }
        customAlbumSelectAdapter.setLayoutParams(width, height);
        gridView.setNumColumns(orientation == Configuration.ORIENTATION_PORTRAIT ? 2 : 4);
    }

    private void loadAlbums() {
        new AlbumLoaderTask().execute();
    }

    private class AlbumLoaderTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            gridView.setVisibility(View.INVISIBLE);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            getImages();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressBar.setVisibility(View.INVISIBLE);
            gridView.setVisibility(View.VISIBLE);

            if (customAlbumSelectAdapter == null) {
                customAlbumSelectAdapter = new CustomAlbumSelectAdapter(getApplicationContext(), albums);
            } else {
                customAlbumSelectAdapter.addAll(albums);
            }
            gridView.setAdapter(customAlbumSelectAdapter);
            gridView.setOnItemClickListener(aOnItemClickListener);

            orientationBasedUI(getResources().getConfiguration().orientation);
            super.onPostExecute(aVoid);
        }
    }

    private void getImages() {
        HashSet<String> hashSet = new HashSet<>();
        albums = new ArrayList<>();

        String[] projection = { MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, MediaStore.Images.ImageColumns.DATA, MediaStore.Images.ImageColumns.DATE_ADDED };
        String orderBy = MediaStore.Images.ImageColumns.DATE_ADDED;

        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, orderBy);
        if (cursor.getCount() > 0) {
            cursor.moveToLast();
            do {
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME));
                String image = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));

                /**
                 * It may happen that some image file paths are still present in cache,
                 * though image file does not exist. These last as long as media
                 * scanner is not run again. To avoid get such image file paths, check
                 * if image file exists.
                 */
                if (new File(image).exists() && !hashSet.contains(album)) {
                    albums.add(new Album(album, image));
                    hashSet.add(album);
                }
            } while (cursor.moveToPrevious());
        }
        cursor.close();
    }

}
