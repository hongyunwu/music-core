package com.autoio.core_sdk.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.autoio.core_sdk.R;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by wuhongyun on 17-7-20.
 */

public class CoverPathUtils {

    //获取专辑封面的Uri
    private static final Uri albumArtUri = Uri.parse("content://media/external/audio/albumart");
    /**
     * 从文件当中获取专辑封面位图
     * @param context
     * @param songid
     * @param albumid
     * @return
     */
    public static Bitmap getArtworkFromFile(Context context, long songid, long albumid){
        Log.i("CoverPathUtils","getArtworkFromFile->"+" ,songId:"+songid+" ,albumId:"+albumid);
        Bitmap bm = null;
        if(albumid < 0 && songid < 0) {
            throw new IllegalArgumentException("Must specify an album or a song id");
        }
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            FileDescriptor fd = null;
            if(albumid < 0){
                Uri uri = Uri.parse("content://media/external/audio/media/"
                        + songid + "/albumart");
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if(pfd != null) {
                    fd = pfd.getFileDescriptor();
                }
            } else {
                Uri uri = ContentUris.withAppendedId(albumArtUri, albumid);
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if(pfd != null) {
                    fd = pfd.getFileDescriptor();
                }
            }
            options.inSampleSize = 1;
            // 只进行大小判断
            options.inJustDecodeBounds = true;
            // 调用此方法得到options得到图片大小
            BitmapFactory.decodeFileDescriptor(fd, null, options);
            // 我们的目标是在800pixel的画面上显示
            // 所以需要调用computeSampleSize得到图片缩放的比例
            options.inSampleSize = 100;
            // 我们得到了缩放的比例，现在开始正式读入Bitmap数据
            options.inJustDecodeBounds = false;
            options.inDither = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            //根据options参数，减少所需要的内存
            bm = BitmapFactory.decodeFileDescriptor(fd, null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bm;
    }


    /**
     * 获取默认专辑图片
     * @param context
     * @return
     */
    public static Bitmap getDefaultArtwork(Context context,boolean small) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.RGB_565;
        if(small){  //返回小图片
            return BitmapFactory.decodeResource(context.getResources(),R.drawable.play_page_default_cover,opts);
        }

        return BitmapFactory.decodeResource(context.getResources(),R.drawable.play_page_default_cover,opts);
    }

    /**
     * 获取专辑封面位图对象
     * @param context
     * @param song_id
     * @param album_id
     * @param allowdefalut
     * @return
     */
    public static Bitmap getArtwork(Context context, long song_id, long album_id, boolean allowdefalut, boolean small){
        if(album_id < 0) {
            if(song_id < 0) {
                Bitmap bm = getArtworkFromFile(context, song_id, -1);
                if(bm != null) {
                    return bm;
                }
            }
            if(allowdefalut) {
                return getDefaultArtwork(context, small);
            }
            return null;
        }
        ContentResolver res = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(albumArtUri, album_id);
        if(uri != null) {
            InputStream in = null;
            try {
                in = res.openInputStream(uri);
                BitmapFactory.Options options = new BitmapFactory.Options();
                //先制定原始大小
                options.inSampleSize = 1;
                //只进行大小判断
                options.inJustDecodeBounds = true;
                //调用此方法得到options得到图片的大小
                BitmapFactory.decodeStream(in, null, options);
                /** 我们的目标是在你N pixel的画面上显示。 所以需要调用computeSampleSize得到图片缩放的比例 **/
                /** 这里的target为800是根据默认专辑图片大小决定的，800只是测试数字但是试验后发现完美的结合 **/
                if(small){
                    options.inSampleSize = computeSampleSize(options, 250);
                } else{
                    options.inSampleSize = computeSampleSize(options, 1000);
                }
                // 我们得到了缩放比例，现在开始正式读入Bitmap数据
                options.inJustDecodeBounds = false;
                options.inDither = false;
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                in = res.openInputStream(uri);
                return BitmapFactory.decodeStream(in, null, options);
            } catch (FileNotFoundException e) {
                Bitmap bm = getArtworkFromFile(context, song_id, album_id);
                if(bm != null) {
                    if(bm.getConfig() == null) {
                        bm = bm.copy(Bitmap.Config.RGB_565, false);
                        if(bm == null && allowdefalut) {
                            return getDefaultArtwork(context, small);
                        }
                    }
                } else if(allowdefalut) {
                    bm = getDefaultArtwork(context, small);
                }
                return bm;
            } finally {
                try {
                    if(in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 对图片进行合适的缩放
     * @param options
     * @param target
     * @return
     */
    public static int computeSampleSize(BitmapFactory.Options options, int target) {
        int w = options.outWidth;
        int h = options.outHeight;
        int candidateW = w / target;
        int candidateH = h / target;
        int candidate = Math.max(candidateW, candidateH);
        if(candidate == 0) {
            return 1;
        }
        if(candidate > 1) {
            if((w > target) && (w / candidate) < target) {
                candidate -= 1;
            }
        }
        if(candidate > 1) {
            if((h > target) && (h / candidate) < target) {
                candidate -= 1;
            }
        }
        return candidate;
    }


    /**
     * 将图片转换为3d倒影图
     * @param srcBitmap
     * @return
     */
    public static Bitmap create3DReflectBitMap(Bitmap srcBitmap) {

        int height = srcBitmap.getHeight();
        int width = srcBitmap.getWidth();

        //283*246

        int bitmapGap = 7;//倒影与原图之间的间距
        Matrix matrix = new Matrix();
        // 1表示放大比例，不放大也不缩小。
        // -1表示在y轴上相反，即旋转180度
        matrix.preScale(1, -1);

        // 将矩阵应用到该原图之中，返回一个宽度不变，高度为原图1/2的倒影位图

        Bitmap reflectionBitmap = Bitmap.createBitmap(srcBitmap, 0,
                height / 2, width, height / 2, matrix, false);
        Bitmap desBitmap = Bitmap.createBitmap(width, (height
                + height / 2 + bitmapGap), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(desBitmap);
        canvas.drawBitmap(srcBitmap, 0, 0, null);

        Paint deafaultPaint = new Paint();
        canvas.drawRect(0, height, width, height + bitmapGap,
                deafaultPaint);
        canvas.drawBitmap(reflectionBitmap, 0, height + bitmapGap, null);

        Paint paint = new Paint();
        /**创建LinearGradient，从而给定一个由上到下的渐变色。
         * 参数一:为渐变起初点坐标x位置， 参数二:为y轴位置， 参数三和四:分辨对应渐变终点， 最后参数为平铺方式，
         * 这里设置为镜像Gradient是基于Shader类，所以我们通过Paint的setShader方法来设置这个渐变
         */
        LinearGradient shader = new LinearGradient(0,
                height + bitmapGap, 0,
                desBitmap.getHeight(),
                0x30ffffff/*0.5 - alpha*/, 0x00ffffff, Shader.TileMode.MIRROR);
        // 设置阴影
        paint.setShader(shader);
        paint.setXfermode(new PorterDuffXfermode(
                android.graphics.PorterDuff.Mode.DST_IN));
        // 用已经定义好的画笔构建一个矩形阴影渐变效果
        canvas.drawRect(0, height, width, desBitmap.getHeight()
               , paint);
        return desBitmap;
    }
}

