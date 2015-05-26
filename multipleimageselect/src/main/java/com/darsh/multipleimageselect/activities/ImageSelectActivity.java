package com.darsh.multipleimageselect.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.Toast;

import com.darsh.multipleimageselect.R;
import com.darsh.multipleimageselect.adapters.CustomImageSelectAdapter;
import com.darsh.multipleimageselect.helpers.Constants;
import com.darsh.multipleimageselect.models.Image;

import java.util.ArrayList;

/**
 * Created by Darshan on 4/18/2015.
 */
public class ImageSelectActivity extends AppCompatActivity {
    private CustomImageSelectAdapter customImageSelectAdapter;
    private GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Select images");
        setContentView(R.layout.activity_image_select);

        final WindowManager windowManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        final DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);

        Intent intent = getIntent();
        ArrayList<Image> images = intent.getParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES);

        customImageSelectAdapter = new CustomImageSelectAdapter(getApplicationContext(), images);

        gridView = (GridView) findViewById(R.id.grid_view_image_select);
        gridView.setAdapter(customImageSelectAdapter);
        gridView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        gridView.setMultiChoiceModeListener(multiChoiceModeListener);

        orientationBasedUI(getResources().getConfiguration().orientation);
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
            customImageSelectAdapter.toggleSelection(position, checked);
            mode.setTitle(String.valueOf(customImageSelectAdapter.countSelected) + " selected");
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
        }
    };

    private void orientationBasedUI(int orientation) {
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
}
