package com.darsh.multipleimageselect.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
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

    private ActionMode actionMode;

    private ContentObserver contentObserver;
    private static Handler handler;
    private ImageLoaderThread imageLoaderThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_select);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.image_view);
        }

        Intent intent = getIntent();
        if (intent == null) {
            finish();
        }
        album = intent.getStringExtra(Constants.INTENT_EXTRA_ALBUM);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar_image_select);
        gridView = (GridView) findViewById(R.id.grid_view_image_select);
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

                        /**
                         * If adapter is null, this implies that the loaded images will be shown
                         * for the first time, hence send FETCH_COMPLETED message.
                         * However, if adapter has been initialised, this thread was run either
                         * due to the activity being restarted or content being changed.
                         */
                        if (customImageSelectAdapter == null) {
                            customImageSelectAdapter = new CustomImageSelectAdapter(getApplicationContext(), images);
                            gridView.setAdapter(customImageSelectAdapter);

                            progressBar.setVisibility(View.INVISIBLE);
                            gridView.setVisibility(View.VISIBLE);
                            orientationBasedUI(getResources().getConfiguration().orientation);

                        } else {
                            customImageSelectAdapter.update(images);

                            /**
                             * Some selected images may have been deleted
                             * hence update action mode title
                             */
                            if (actionMode != null) {
                                actionMode.setTitle(customImageSelectAdapter.countSelected + " " + getString(R.string.selected));
                            }
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
            public void onChange(boolean selfChange) {
                startImageLoading();
            }
        };
        getContentResolver().registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false, contentObserver);

        startImageLoading();
    }

    @Override
    protected void onStop() {
        super.onStop();

        abortImageLoading();

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

        int size  = orientation == Configuration.ORIENTATION_PORTRAIT ? metrics.widthPixels / 3 : metrics.widthPixels / 5;
        customImageSelectAdapter.setLayoutParams(size);
        gridView.setNumColumns(orientation == Configuration.ORIENTATION_PORTRAIT ? 3 : 5);
    }

    @Override
    public void onBackPressed() {
        sendIntent(null);
    }

    private AbsListView.OnItemClickListener onItemClickListener = new AbsListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (actionMode == null) {
                actionMode = ImageSelectActivity.this.startActionMode(callback);
            }
            customImageSelectAdapter.toggleSelection(position);
            actionMode.setTitle(customImageSelectAdapter.countSelected + " " + getString(R.string.selected));

            if (customImageSelectAdapter.countSelected == 0) {
                actionMode.finish();
            }
        }
    };

    private ActionMode.Callback callback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater menuInflater = mode.getMenuInflater();
            menuInflater.inflate(R.menu.menu_contextual_action_bar, menu);

            actionMode = mode;

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {int i = item.getItemId();
            if (i == R.id.menu_item_add_image) {
                if (customImageSelectAdapter.countSelected > Constants.limit) {
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
            actionMode = null;
        }
    };

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

    private void startImageLoading() {
        abortImageLoading();

        imageLoaderThread = new ImageLoaderThread();
        imageLoaderThread.start();
    }

    private void abortImageLoading() {
        //No thread is running
        if (imageLoaderThread == null) {
            return;
        }

        //Interrupt thread if running and wait till it joins
        imageLoaderThread.interrupt();
        try {
            imageLoaderThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class ImageLoaderThread extends Thread {
        @Override
        public void run() {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            Message message;
            if (customImageSelectAdapter == null) {
                message = handler.obtainMessage();
                /**
                 * If the adapter is null, this is first time this activity's view is
                 * being shown, hence send FETCH_STARTED message to show progress bar
                 * while images are loaded from phone
                 */
                message.what = Constants.FETCH_STARTED;
                message.sendToTarget();
            }

            if (Thread.interrupted()) {
                return;
            }

            File file;
            HashSet<String> selectedImages = new HashSet<>();
            if (images != null) {
                for (Image image : images) {
                    file = new File(image.imagePath);
                    if (file.exists() && image.isSelected) {
                        selectedImages.add(image.imagePath);
                    }
                }
            }

            images = new ArrayList<>();
            Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{ MediaStore.Images.Media.DATA },
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " =?", new String[]{ album }, MediaStore.Images.Media.DATE_ADDED);
            if (cursor.moveToLast()) {
                do {
                    if (Thread.interrupted()) {
                        return;
                    }

                    String image = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    file = new File(image);
                    if (file.exists()) {
                        images.add(new Image(image, selectedImages.contains(image)));
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
