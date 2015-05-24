package com.darsh.multipleimageselect.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.darsh.multipleimageselect.R;
import com.darsh.multipleimageselect.adapters.CustomAlbumSelectAdapter;
import com.darsh.multipleimageselect.models.Album;
import com.darsh.multipleimageselect.models.Image;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Darshan on 4/14/2015.
 */
public class AlbumSelectActivity extends AppCompatActivity {
    private HashMap<String, ArrayList<Image>> gallery;
    private ArrayList<Album> albums;

    private GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Select Photo Album");
        setContentView(R.layout.activity_album_select);

        getImages();

        CustomAlbumSelectAdapter customAlbumSelectAdapter = new CustomAlbumSelectAdapter(getApplicationContext(), albums);

        gridView = (GridView) findViewById(R.id.grid_view_album_select);
        gridView.setAdapter(customAlbumSelectAdapter);
        gridView.setOnItemClickListener(aOnItemClickListener);

        orientationBasedUI(getResources().getConfiguration().orientation);
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
            intent.putParcelableArrayListExtra("images", gallery.get(albums.get(position).name));
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
        gridView.setNumColumns(orientation == Configuration.ORIENTATION_PORTRAIT ? 2 : 4);
    }

    private void getImages() {
        gallery = new HashMap<>();
        albums = new ArrayList<>();

        String[] projection = { MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, MediaStore.Images.ImageColumns.DATA, MediaStore.Images.ImageColumns.DATE_ADDED };
        String orderBy = MediaStore.Images.ImageColumns.DATE_ADDED;
        ArrayList<Image> temp;

        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, orderBy);
        if (cursor.getCount() > 0) {
            cursor.moveToLast();
            do {
                String albumName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME));
                String imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));

                /**
                 * It may happen that some image file paths are still present in cache,
                 * though image file does not exist. These last as long as media
                 * scanner is not run again. To avoid get such image file paths, check
                 * if image file exists.
                 */
                if (new File(imagePath).exists()) {

                    if (gallery.containsKey(albumName)) {
                        temp = gallery.get(albumName);
                        temp.add(new Image(imagePath, false));
                        gallery.put(albumName, temp);

                    } else {
                        temp = new ArrayList<>();
                        temp.add(new Image(imagePath, false));
                        gallery.put(albumName, temp);
                        albums.add(new Album(albumName, imagePath));
                    }
                }
            } while (cursor.moveToPrevious());
        }
        cursor.close();
    }
}
