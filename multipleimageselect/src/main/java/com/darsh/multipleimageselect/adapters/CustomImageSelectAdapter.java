package com.darsh.multipleimageselect.adapters;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.darsh.multipleimageselect.R;
import com.darsh.multipleimageselect.models.Image;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Darshan on 4/18/2015.
 */
public class CustomImageSelectAdapter extends CustomGenericAdapter<Image> {
    public int countSelected;

    public CustomImageSelectAdapter(Context context, ArrayList<Image> images) {
        super(context, images);
        this.countSelected = 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.grid_view_item_image_select, null);
            convertView.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.MATCH_PARENT));
            imageView = (ImageView) convertView.findViewById(R.id.image_view_image_select);

        } else {
            imageView = (ImageView) convertView.getTag(R.id.image_view_image_select);
        }

        convertView.setTag(R.id.image_view_image_select, imageView);

        final WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
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

        imageView.getLayoutParams().width = width;
        imageView.getLayoutParams().height = height;

        ((FrameLayout) convertView).setForeground(arrayList.get(position).isSelected ? context.getResources().getDrawable(R.drawable.highlight_image_view) : null);

        File file = new File(arrayList.get(position).imagePath);
        Picasso.with(context).load(file).placeholder(R.drawable.image_placeholder).fit().centerCrop().into(imageView);

        return convertView;
    }

    public void toggleSelection(int position, boolean isSelected) {
        if (arrayList.get(position).isSelected) {
            countSelected--;
        } else {
            countSelected++;
        }
        arrayList.get(position).isSelected = isSelected;
        this.notifyDataSetChanged();
    }

    public void deselectAll() {
        for (Image image : arrayList) {
            image.isSelected = false;
        }
        this.notifyDataSetChanged();
    }

    public ArrayList<String> getSelectedImages() {
        ArrayList<String> selectedImages = new ArrayList<>();
        for (Image image : arrayList) {
            if (image.isSelected) {
                selectedImages.add(image.imagePath);
            }
        }

        return selectedImages;
    }
}
