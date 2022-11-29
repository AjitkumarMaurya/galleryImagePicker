package com.ajitmaurya.demo;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.ajitmaurya.galleryimagepicker.ImagePicker;
import com.ajitmaurya.galleryimagepicker.RedBookPresenter;
import com.ajitmaurya.galleryimagepicker.bean.MimeType;
import com.ajitmaurya.galleryimagepicker.data.OnImagePickCompleteListener;


public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        ImagePicker.withCrop(new RedBookPresenter())
                .setMaxCount(5)
                .showCamera(true)
                .setColumnCount(4)
                .mimeTypes(MimeType.ofImage())
                .filterMimeTypes(MimeType.GIF)
                .assignGapState(true)
                .setFirstImageItem(null)
                .setFirstImageItemSize(1, 1)
                .setVideoSinglePick(true)
                .setMaxVideoDuration(60000L)
                .setMinVideoDuration(3000L)
                .pick(MainActivity.this, (OnImagePickCompleteListener) items -> {



                });
    }


}