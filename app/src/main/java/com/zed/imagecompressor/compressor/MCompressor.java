package com.zed.imagecompressor.compressor;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import java.io.File;


public class MCompressor {
    private static volatile MCompressor object;
    private Context Mcontext;
    private float mWidth = 600.0f;
    private float mHeight = 800.0f;
    private Bitmap.CompressFormat imageFormat = Bitmap.CompressFormat.PNG;
    private Bitmap.Config bitmapConfig = Bitmap.Config.ARGB_8888;
    private int quality = 81;
    private String finalPath;

    private MCompressor(Context Mcontext) {
        this.Mcontext = Mcontext;
        finalPath = Mcontext.getCacheDir().getPath() + File.pathSeparator + "image";
    }

    public static MCompressor getDefault(Context context) {
        if (object == null) {
            synchronized (MCompressor.class) {
                if (object == null) {
                    object = new MCompressor(context);
                }
            }
        }
        return object;
    }

    public File compressAsFile(File file) {
        return ImageCompressor.compressImage(Mcontext, Uri.fromFile(file), mWidth, mHeight, imageFormat, bitmapConfig, quality, finalPath);
    }

    public Bitmap compressAsBitmap(File file) {
        return ImageCompressor.getBitmap(Mcontext, Uri.fromFile(file), mWidth, mHeight, bitmapConfig);
    }


}

