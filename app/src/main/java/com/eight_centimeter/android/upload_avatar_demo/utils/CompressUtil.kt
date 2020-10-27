package com.eight_centimeter.android.upload_avatar_demo.utils

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import java.io.*

object CompressUtil {

    val MB_2: Int = 1024 * 1024 * 2
    val MB_3: Int = 1024 * 1024 * 3
    val MB_4: Int = 1024 * 1024 * 4
    val MB_5: Int = 1024 * 1024 * 5

    fun compression(
            file: File,
            context: Context,
            once: Boolean = false,
            minSizeKB: Int = 614400 // size > 600 KB
    ): File {
        val quality = getQuality(file)
        if (file.length() > minSizeKB) {
            val destFile = ImageFileUtil.createNewJPGFile(context)
            massCompression(Uri.fromFile(file), context.contentResolver, destFile, quality)
            if (once) return destFile
            return if (destFile.length() < MB_5) {
                destFile
            } else {
                compression(destFile, context)
            }
        }
        return file
    }

    private fun massCompression(
            uri: Uri,
            contentResolver: ContentResolver,
            destination: File,
            quality: Int = 50
    ) {
        val bitmap = measurementCompression(contentResolver, uri)
        var out: OutputStream? = null
        try {
            out = FileOutputStream(destination.path)
            bitmap?.compress(Bitmap.CompressFormat.JPEG, quality, out)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } finally {
            bitmap?.recycle()
            out?.close()
        }
    }

    fun measurementCompression(contentResolver: ContentResolver, uri: Uri): Bitmap? {
        var input: InputStream = ImageFileUtil.getInputStreamByUri(contentResolver, uri)
        val onlyBoundsOptions: BitmapFactory.Options = BitmapFactory.Options()
        try {
            onlyBoundsOptions.inJustDecodeBounds = true;
            onlyBoundsOptions.inDither = true;
            onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
            BitmapFactory.decodeStream(input, null, onlyBoundsOptions)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            input.close()
        }

        input = ImageFileUtil.getInputStreamByUri(contentResolver, uri)
        try {
            val originalWidth = onlyBoundsOptions.outWidth
            val originalHeight = onlyBoundsOptions.outHeight
            if ((originalWidth == -1) || (originalHeight == -1)) return null

            Log.d("kevins", "originalHeight ${originalHeight}")
            Log.d("kevins", "originalHeight originalWidth ${originalWidth}")
            //图片分辨率以480x800为标准
            var hh = 1080f
            var ww = 1080f
            //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
            var be = 1 //be=1表示不缩放
            if (originalWidth > originalHeight && originalWidth > ww) {//如果宽度大的话根据宽度固定大小缩放
                be = (originalWidth / ww).toInt()
            } else if (originalWidth < originalHeight && originalHeight > hh) {//如果高度高的话根据宽度固定大小缩放
                be = (originalHeight / hh).toInt()
            }
            if (be <= 0) be = 1

            //比例压缩
            var bitmapOptions = BitmapFactory.Options()
            bitmapOptions.inSampleSize = be
            bitmapOptions.inDither = true
            bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888

            return BitmapFactory.decodeStream(input, null, bitmapOptions)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            input.close()
        }
    }

    /**
     * check the max size, and compress it
     */
    private fun getQuality(file: File): Int {
        val mb: Float = file.length().toFloat() / 1024 / 1024
        return when {
            mb > 8 -> {
                30
            }
            mb > 7 -> {
                40
            }
            mb > 6 -> {
                45
            }
            mb > 4.5 -> {
                50
            }
            mb > 3.5 -> {
                60
            }
            mb > 2.5 -> {
                70
            }
            mb > 1.5 -> {
                75
            }
            mb > 1 -> {
                80
            }
            else -> {
                85
            }
        }
    }
}