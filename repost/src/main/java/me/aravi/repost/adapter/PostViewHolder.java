/*
 * Copyright (c) 2020. Aravind Chowdary
 */

package me.aravi.repost.adapter;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import de.hdodenhof.circleimageview.CircleImageView;
import me.aravi.repost.R;

public class PostViewHolder extends RecyclerView.ViewHolder {

    public TextView username, timestamp, caption;
    public DotsIndicator indicator;
    public RelativeLayout indicator_holder;
    public ViewPager pager;
    public CircleImageView user_image;
    public ImageView delete;
    public Button download, copy;
    public LinearLayout buttonsLayout;

    public PostViewHolder(View itemView) {
        super(itemView);
        username = itemView.findViewById(R.id.post_username);
        timestamp = itemView.findViewById(R.id.post_timestamp);
        download = itemView.findViewById(R.id.download);
        copy = itemView.findViewById(R.id.copy);
        buttonsLayout = itemView.findViewById(R.id.buttonsLayout);
        caption = itemView.findViewById(R.id.post_desc);
        indicator = itemView.findViewById(R.id.indicator);
        indicator_holder = itemView.findViewById(R.id.indicator_holder);
        pager = itemView.findViewById(R.id.pager);
        user_image = itemView.findViewById(R.id.post_user_image);
        delete = itemView.findViewById(R.id.delete_button);
    }
}
