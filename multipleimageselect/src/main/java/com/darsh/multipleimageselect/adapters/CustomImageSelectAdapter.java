package com.darsh.multipleimageselect.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.darsh.multipleimageselect.R;
import com.darsh.multipleimageselect.models.Image;

import java.util.ArrayList;

/**
 * Created by Darshan on 4/18/2015.
 */
public class CustomImageSelectAdapter extends CustomGenericAdapter<Image> {
    public CustomImageSelectAdapter(Context context, ArrayList<Image> images) {
        super(context, images);
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

        Glide.with(context)
                .load(arrayList.get(position).path)
                .placeholder(R.drawable.image_placeholder).centerCrop().into(imageView);

        return convertView;
    }
}
