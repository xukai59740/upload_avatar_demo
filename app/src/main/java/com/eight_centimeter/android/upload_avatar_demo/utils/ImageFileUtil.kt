package com.eight_centimeter.android.upload_avatar_demo.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import com.eight_centimeter.android.upload_avatar_demo.BuildConfig
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.*

object ImageFileUtil {

    /**
     * if samsung take photo , the image be Rotate 90°， fix it
     */
    fun fixBitmapRotate(context: Context, uri: Uri): Uri {
        if (DeviceUtils.isSamsung()) {
            var out: OutputStream? = null
            try {
                val degree = getBitmapDegree(context, uri)
                if (degree != 0) {
                    val matrix = Matrix()
                    matrix.setRotate(degree.toFloat())
                    //android:largeHeap="true"
                    val bitmap: Bitmap =
                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    val returnBm =
                        Bitmap.createBitmap(
                            bitmap,
                            0,
                            0,
                            bitmap.width,
                            bitmap.height,
                            matrix,
                            true
                        )
                    val destFile = createNewJPGFile(context)

                    out = FileOutputStream(destFile.path)
                    returnBm.compress(Bitmap.CompressFormat.JPEG, 100, out)
                    return destFile.toUri()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                out?.close()
            }
        }
        return uri
    }

    private fun getBitmapDegree(context: Context, uri: Uri): Int {
        var degree = 0
        var inputStream: InputStream? = null
        try {
            inputStream = context.contentResolver.openInputStream(uri)
            inputStream?.let {
                val exifInterface = ExifInterface(inputStream)
                val orientation: Int = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
            return degree
        }
    }

    fun createNewJPGFile(context: Context): File {
        ///sdcard/Android/data/application/cache/images/123123123.jpg
        val folder = File(context.getExternalFilesDir(Environment.DIRECTORY_DCIM).toString())
        folder.mkdirs()
        val file = File(folder, getTimeFileName())
        if (file.exists())
            file.delete()
        file.createNewFile()
        return file
    }

    private fun getTimeFileName(): String {
        val timeName = Date().time.toString()
        return "${timeName}.jpg"
    }

    fun createNewImageUriByProvider(context: Context): Uri {
        val file = createNewJPGFile(context)
        return convertFileToUriByProvider(context, file)
    }

    fun createNewImageUriToSelfApp(context: Context): Uri {
        val file = createNewJPGFile(context)
        return file.toUri()
    }

    private fun convertFileToUriByProvider(context: Context, file: File): Uri {
        //content:///application.fileprovider/external_file_dcim/123123123.jpg
        val imageUri = FileProvider.getUriForFile(
            context,
            BuildConfig.APPLICATION_ID + ".fileprovider",
            file
        )
        return imageUri!!
    }

}