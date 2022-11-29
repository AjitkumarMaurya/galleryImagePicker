package com.ajitmaurya.galleryimagepicker.data;

import androidx.annotation.Nullable;

import com.ajitmaurya.galleryimagepicker.bean.ImageItem;

public interface ICameraExecutor {

    void takePhoto();

    void takeVideo();

    void onTakePhotoResult(@Nullable ImageItem imageItem);
}
