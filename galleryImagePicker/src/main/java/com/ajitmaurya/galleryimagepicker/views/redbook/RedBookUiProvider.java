package com.ajitmaurya.galleryimagepicker.views.redbook;
import android.content.Context;
import android.graphics.Color;

import com.ajitmaurya.galleryimagepicker.views.PickerUiProvider;
import com.ajitmaurya.galleryimagepicker.views.base.PickerControllerView;
import com.ajitmaurya.galleryimagepicker.views.base.PickerFolderItemView;
import com.ajitmaurya.galleryimagepicker.views.base.PickerItemView;
import com.ajitmaurya.galleryimagepicker.views.wx.WXFolderItemView;

public class RedBookUiProvider extends PickerUiProvider {
    @Override
    public PickerControllerView getBottomBar(Context context) {
        return null;
    }

    @Override
    public PickerControllerView getTitleBar(Context context) {
        return new RedBookTitleBar(context);
    }

    @Override
    public PickerItemView getItemView(Context context) {
        return new RedBookItemView(context);
    }

    @Override
    public PickerFolderItemView getFolderItemView(Context context) {
        WXFolderItemView itemView = (WXFolderItemView) super.getFolderItemView(context);
        itemView.setIndicatorColor(Color.RED);
        itemView.setBackgroundColor(Color.BLACK);
        itemView.setNameTextColor(Color.WHITE);
        itemView.setCountTextColor(Color.parseColor("#50F5f5f5"));
        itemView.setDividerColor(Color.parseColor("#50F5f5f5"));
        return itemView;
    }
}
