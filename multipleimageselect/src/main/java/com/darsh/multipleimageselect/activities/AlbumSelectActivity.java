package com.darsh.multipleimageselect.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
    private static Handler handler;

    private AlbumLoaderThread albumLoaderThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_select);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.album_view);
        }

        Intent intent = getIntent();
        if (intent == null) {
            finish();
        }
        Constants.limit = intent.getIntExtra(Constants.INTENT_EXTRA_LIMIT, Constants.DEFAULT_LIMIT);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar_album_select);
        gridView = (GridView) findViewById(R.id.grid_view_album_select);
        gridView.setOnItemClickListener(onItemClickListener);
    }

    @Override
    protected void onStart() {
        super.onStart();

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case Constants.FETCH_STARTED: {
                        progressBar.setVisibility(View.VISIBLE);
                        gridView.setVisibility(View.INVISIBLE);
                        break;
                    }

                    case Constants.FETCH_COMPLETED: {
                        if (customAlbumSelectAdapter == null) {
                            customAlbumSelectAdapter = new CustomAlbumSelectAdapter(getApplicationContext(), albums);
                            gridView.setAdapter(customAlbumSelectAdapter);

                            progressBar.setVisibility(View.INVISIBLE);
                            gridView.setVisibility(View.VISIBLE);
                            orientationBasedUI(getResources().getConfiguration().orientation);

                        } else {
                            customAlbumSelectAdapter.update(albums);
                        }
                        break;
                    }

                    default: {
                        super.handleMessage(msg);
                    }
                }
            }
        };
        contentObserver = new ContentObserver(handler) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                loadAlbums();
            }
        };
        getContentResolver().registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false, contentObserver);

        loadAlbums();
    }

    @Override
    protected void onStop() {
        super.onStop();

        abortLoading();

        getContentResolver().unregisterContentObserver(contentObserver);
        contentObserver = null;

        handler.removeCallbacksAndMessages(null);
        handler = null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        orientationBasedUI(newConfig.orientation);
    }

    private void orientationBasedUI(int orientation) {
        final WindowManager windowManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        final DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);

        int size = orientation == Configuration.ORIENTATION_PORTRAIT ? metrics.widthPixels / 2 : metrics.widthPixels / 4;
        customAlbumSelectAdapter.setLayoutParams(size);
        gridView.setNumColumns(orientation == Configuration.ORIENTATION_PORTRAIT ? 2 : 4);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        finish();
    }

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(getApplicationContext(), ImageSelectActivity.class);
            intent.putExtra(Constants.INTENT_EXTRA_ALBUM, albums.get(position).name);
            intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
            startActivity(intent);
            finish();
        }
    };

    private void loadAlbums() {
        abortLoading();

        albumLoaderThread = new AlbumLoaderThread();
        albumLoaderThread.start();
    }

    private void abortLoading() {
        if (albumLoaderThread == null) {
            return;
        }

        albumLoaderThread.interrupt();
        try {
            albumLoaderThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class AlbumLoaderThread extends Thread {
        @Override
        public void run() {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            Message message;
            if (customAlbumSelectAdapter == null) {
                message = handler.obtainMessage();
                message.what = Constants.FETCH_STARTED;
                message.sendToTarget();
            }

            if (Thread.interrupted()) {
                return;
            }

            albums = new ArrayList<>();
            HashSet<String> albumSet = new HashSet<>();
            File file;
            Cursor cursor = getApplicationContext().getContentResolver().
                    query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{ MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.DATA},
                            null, null, MediaStore.Images.Media.DATE_ADDED);
            if (cursor.moveToLast()) {
                do {
                    if (Thread.interrupted()) {
                        return;
                    }

                    String album = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                    String image = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));

                    /**
                     * It may happen that some image file paths are still present in cache,
                     * though image file does not exist. These last as long as media
                     * scanner is not run again. To avoid get such image file paths, check
                     * if image file exists.
                     */
                    file = new File(image);
                    if (file.exists() && !albumSet.contains(album)) {
                        albums.add(new Album(album, image));
                        albumSet.add(album);
                    }

                } while (cursor.moveToPrevious());
            }
            cursor.close();

            message = handler.obtainMessage();
            message.what = Constants.FETCH_COMPLETED;
            message.sendToTarget();

            Thread.interrupted();
        }
    }
}
