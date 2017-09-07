package com.zed.imagecompressor.compressor;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


class ImageCompressor {

    private ImageCompressor() {

    }

    static String getPath(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) {
            return uri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            String string = cursor.getString(index);
            cursor.close();
            return string;
        }
    }

    static Bitmap getBitmap(Context context, Uri uri, float mWidth, float mHight, Bitmap.Config config) {
        String path = getPath(context, uri);
        Bitmap bitmap = null;

        BitmapFactory.Options optionsFactory = new BitmapFactory.Options();

        optionsFactory.inJustDecodeBounds = true;
        Bitmap bitmapOld = BitmapFactory.decodeFile(path, optionsFactory);
        if (bitmapOld == null) {

            InputStream stream = null;
            try {
                stream = new FileInputStream(path);
                BitmapFactory.decodeStream(stream, null, optionsFactory);
                stream.close();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

        int height = optionsFactory.outHeight;
        int width = optionsFactory.outWidth;

        if (width < 0 || height < 0) {
            Bitmap bitmapNew = BitmapFactory.decodeFile(path);
            width = bitmapNew.getWidth();
            height = bitmapNew.getHeight();
        }

        float imgR = (float) width / height;
        float maxR = mWidth / mHight;

        if (height > mHight || width > mWidth) {
            if (imgR < maxR) {
                imgR = mHight / height;
                width = (int) (imgR * width);
                height = (int) mHight;
            } else if (imgR > maxR) {
                imgR = mWidth / width;
                height = (int) (imgR * height);
                width = (int) mWidth;
            } else {
                height = (int) mHight;
                width = (int) mWidth;
            }
        }

        optionsFactory.inSampleSize = calSize(optionsFactory, width, height);

        optionsFactory.inJustDecodeBounds = false;

        optionsFactory.inPurgeable = true;
        optionsFactory.inInputShareable = true;
        optionsFactory.inTempStorage = new byte[16 * 1024];

        try {
            bitmapOld = BitmapFactory.decodeFile(path, optionsFactory);
            if (bitmapOld == null) {

                InputStream inputStream = null;
                try {
                    inputStream = new FileInputStream(path);
                    BitmapFactory.decodeStream(inputStream, null, optionsFactory);
                    inputStream.close();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }
        try {
            bitmap = Bitmap.createBitmap(width, height, config);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float widthNew = width / (float) optionsFactory.outWidth;
        float hightNew = height / (float) optionsFactory.outHeight;

        Matrix matrixold = new Matrix();
        matrixold.setScale(widthNew, hightNew, 0, 0);

        Canvas c = new Canvas(bitmap);
        c.setMatrix(matrixold);
        c.drawBitmap(bitmapOld, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));

        //check the rotation of the image and display it properly
        ExifInterface exifInterface;
        try {
            exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
            Matrix matrixNew = new Matrix();
            if (orientation == 6) {
                matrixNew.postRotate(90);
            } else if (orientation == 3) {
                matrixNew.postRotate(180);
            } else if (orientation == 8) {
                matrixNew.postRotate(270);
            }
            bitmap = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(),
                    matrixNew, true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    static File compressImage(Context context, Uri uri, float mwidth, float mhight, Bitmap.CompressFormat format, Bitmap.Config config, int i, String path) {
        FileOutputStream fileOutputStream = null;
        String file = getFilePath(context, path, uri, format.name().toLowerCase());
        try {
            fileOutputStream = new FileOutputStream(file);
            ImageCompressor.getBitmap(context, uri, mwidth, mhight, config).compress(format, i, fileOutputStream);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return new File(file);
    }

    private static String getFilePath(Context context, String stringPath, Uri uri, String stringFilePath) {
        File file = new File(stringPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath() + File.separator + splitFileName(getFileName(context, uri))[0] + "." + stringFilePath;
    }

    private static int calSize(BitmapFactory.Options options, int width1, int hight1) {
        final int hight2 = options.outHeight;
        final int width2 = options.outWidth;
        int outputRatio = 1;

        if (hight2 > hight1 || width2 > width1) {
            final int hight3 = Math.round((float) hight2 / (float) hight1);
            final int width3 = Math.round((float) width2 / (float) width1);
            outputRatio = hight3 < width3 ? hight3 : width3;
        }

        final float ratioOld = width2 * hight2;
        final float ratioNew = width1 * hight1 * 2;

        while (ratioOld / (outputRatio * outputRatio) > ratioNew) {
            outputRatio++;
        }

        return outputRatio;
    }



    static String[] splitFileName(String file) {
        String fileNew = file;
        String fileExtention = "";
        int i =file.lastIndexOf(".");
        if (i != -1) {
            fileNew =file.substring(0, i);
            fileExtention = file.substring(i);
        }

        return new String[]{fileNew, fileExtention};
    }

    static String getFileName(Context context, Uri uri) {
        String resultString = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    resultString = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (resultString == null) {
            resultString = uri.getPath();
            int cut = resultString.lastIndexOf(File.separator);
            if (cut != -1) {
                resultString = resultString.substring(cut + 1);
            }
        }
        return resultString;
    }
}
