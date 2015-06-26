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
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.darsh.multipleimageselect.R;
import com.darsh.multipleimageselect.adapters.CustomImageSelectAdapter;
import com.darsh.multipleimageselect.helpers.Constants;
import com.darsh.multipleimageselect.models.Image;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Darshan on 4/18/2015.
 */
public class ImageSelectActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private GridView gridView;
    private CustomImageSelectAdapter customImageSelectAdapter;

    private ArrayList<Image> images;
    private String album;

    private ContentObserver contentObserver;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Select images");
        setContentView(R.layout.activity_image_select);

        Intent intent = getIntent();
        if (intent == null) {
            finish();
        }
        album = intent.getStringExtra(Constants.INTENT_EXTRA_ALBUM);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar_image_select);
        gridView = (GridView) findViewById(R.id.grid_view_image_select);
    }

    @Override
    protected void onStart() {
        super.onStart();

        handler = new Handler();
        contentObserver = new ContentObserver(handler) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                super.onChange(selfChange, uri);
                loadImages();
            }
        };
        getContentResolver().registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false, contentObserver);
        loadImages();
    }

    @Override
    protected void onStop() {
        super.onStop();

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        sendIntent(null);
    }

    private AbsListView.MultiChoiceModeListener multiChoiceModeListener = new AbsListView.MultiChoiceModeListener() {
        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            customImageSelectAdapter.toggleSelection(position);
            mode.setTitle(String.valueOf(customImageSelectAdapter.getCountSelected()) + " selected");
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater menuInflater = mode.getMenuInflater();
            menuInflater.inflate(R.menu.menu_contextual_action_bar, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int i = item.getItemId();
            if (i == R.id.menu_item_add_image) {
                if (customImageSelectAdapter.getCountSelected() > Constants.limit) {
                    Toast.makeText(getApplicationContext(), Constants.toastDisplayLimitExceed, Toast.LENGTH_LONG).show();
                    return false;
                }
                sendIntent(customImageSelectAdapter.getSelectedImages());
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            customImageSelectAdapter.deselectAll();
        }
    };

    private void orientationBasedUI(int orientation) {
        /*
        Set width and height for image view
         */
        final WindowManager windowManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        final DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);

        int width;
        int height;
        if (metrics.widthPixels < metrics.heightPixels) {
            width = (metrics.widthPixels - 4) / 3;
            height = (metrics.widthPixels - 4) / 3;
        } else {
            width = (metrics.widthPixels - 6) / 5;
            height = (metrics.widthPixels - 6) / 5;
        }
        customImageSelectAdapter.setLayoutParams(width, height);
        gridView.setNumColumns(orientation == Configuration.ORIENTATION_PORTRAIT ? 3 : 5);
    }

    private void sendIntent(ArrayList<String> selectedImages) {
        Intent intent = new Intent();
        if (selectedImages != null) {
            intent.putStringArrayListExtra(Constants.INTENT_EXTRA_IMAGES, selectedImages);
            setResult(RESULT_OK, intent);
        } else {
            setResult(RESULT_CANCELED, intent);
        }
        finish();
    }

    private void loadImages() {
        new ImageLoaderTask().execute();
    }

    private class ImageLoaderTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            gridView.setVisibility(View.INVISIBLE);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            getImagesInAlbum();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressBar.setVisibility(View.INVISIBLE);
            gridView.setVisibility(View.VISIBLE);

            if (customImageSelectAdapter == null) {
                customImageSelectAdapter = new CustomImageSelectAdapter(getApplicationContext(), images);
                gridView.setAdapter(customImageSelectAdapter);
                gridView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
                gridView.setMultiChoiceModeListener(multiChoiceModeListener);
            } else {
                customImageSelectAdapter.addAll(images);
            }
            gridView.setAdapter(customImageSelectAdapter);

            orientationBasedUI(getResources().getConfiguration().orientation);
            super.onPostExecute(aVoid);
        }
    }

    private void getImagesInAlbum() {
        HashSet<String> selectedImages = new HashSet<>();
        if (customImageSelectAdapter != null) {
            ArrayList<String> arrayList = customImageSelectAdapter.getSelectedImages();
            if (arrayList != null) {
                for (String image : arrayList) {
                    selectedImages.add(image);
                }
            }
        }

        images = new ArrayList<>();
        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{ MediaStore.Images.Media.DATA },
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " =?", new String[]{ album }, MediaStore.Images.Media.DATE_ADDED);
        if (cursor.moveToLast()) {
            do {
                String image = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                if (new File(image).exists()) {
                    images.add(new Image(image, selectedImages.contains(image)));
                }
            } while (cursor.moveToPrevious());
        }
        cursor.close();
    }
}
