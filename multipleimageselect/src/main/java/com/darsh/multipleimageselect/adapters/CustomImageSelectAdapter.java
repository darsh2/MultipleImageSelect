package com.darsh.multipleimageselect.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
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

        imageView.getLayoutParams().width = size;
        imageView.getLayoutParams().height = size;

        if (arrayList.get(position).isSelected) {
            imageView.setAlpha((float) 0.5);
            ((FrameLayout) convertView).setForeground(context.getResources().getDrawable(R.drawable.ic_cab_done_mtrl_alpha));

        } else {
            imageView.setAlpha((float) 1.0);
            ((FrameLayout) convertView).setForeground(null);
        }

        File file = new File(arrayList.get(position).imagePath);
        Picasso.with(context).load(file).placeholder(R.drawable.image_placeholder).fit().centerCrop().into(imageView);

        return convertView;
    }

    public void toggleSelection(int position) {
        arrayList.get(position).isSelected = !arrayList.get(position).isSelected;
        if (arrayList.get(position).isSelected) {
            countSelected++;
        } else {
            countSelected--;
        }
        this.notifyDataSetChanged();
    }

    public void update(ArrayList<Image> images) {
        arrayList.clear();
        arrayList.addAll(images);

        //update number of selected images
        countSelected = 0;
        for (Image image : arrayList) {
            if (image.isSelected) {
                countSelected++;
            }
        }

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
