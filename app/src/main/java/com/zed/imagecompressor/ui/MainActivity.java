package com.zed.imagecompressor.ui;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.zed.imagecompressor.R;
import com.zed.imagecompressor.compressor.MCompressor;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView imageView = (ImageView)findViewById(R.id.iv_image);

        String picturePath = "";// picture path

        File file = new File(picturePath);
        Bitmap photo = MCompressor.getDefault(this).compressAsBitmap(file);

        File file2 = new File(picturePath);
       File fileCompressed = MCompressor.getDefault(this).compressAsFile(file);

        // save fileCompressed in internal/external storage

        imageView.setImageBitmap(photo);
    }
}
