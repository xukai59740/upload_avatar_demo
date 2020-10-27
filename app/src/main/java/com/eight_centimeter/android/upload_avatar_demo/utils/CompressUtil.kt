package com.eight_centimeter.android.upload_avatar_demo.utils

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.OutputStream

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
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
        var out: OutputStream? = null
        try {
            out = FileOutputStream(destination.path)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
    }

    /**
     * check the max size, and compress it
     */
    private fun getQuality(file: File): Int {
        val mb :Float = file.length().toFloat() / 1024 / 1024
        return when {
            mb > 8 -> {
                10
            }
            mb > 7 -> {
                20
            }
            mb > 6 -> {
                30
            }
            mb > 4.5 -> {
                40
            }
            mb > 3.5 -> {
                50
            }
            mb > 2.5 -> {
                60
            }
            mb > 1.5 -> {
                65
            }
            mb > 1 -> {
                70
            }
            else -> {
                75
            }
        }
    }
}