package com.eight_centimeter.android.upload_avatar_demo.utils

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import com.eight_centimeter.android.upload_avatar_demo.BuildConfig
import org.apache.commons.io.IOUtils
import java.io.*
import java.util.*

object ImageFileUtil {

    /**
     * if samsung take photo , the image be Rotate 90°， fix it
     */
    fun fixBitmapRotate(context: Context, uri: Uri, quality: Int = 100): Uri {
        var out: OutputStream? = null
        var bitmap: Bitmap? = null
        var returnBm: Bitmap? = null
        try {
            val degree = getBitmapDegree(context, uri)
            if (degree != 0) {
                val matrix = Matrix()
                matrix.setRotate(degree.toFloat())

                // compress measurementCompression bitmap
                bitmap = CompressUtil.measurementCompression(context.contentResolver, uri)

                if (bitmap != null) {
                    returnBm = Bitmap.createBitmap(
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
                    returnBm.compress(Bitmap.CompressFormat.JPEG, quality, out)
                    return destFile.toUri()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            out?.close()
            returnBm?.recycle()
            bitmap?.recycle()
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
        ///sdcard/Android/data/asia.mworks.impulso.dev/cache/images/123123123.jpg
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
        //content:///asia.mworks.impulso.dev.fileprovider/external_file_dcim/123123123.jpg
        val imageUri = FileProvider.getUriForFile(
                context,
                BuildConfig.APPLICATION_ID + ".fileprovider",
                file
        )
        return imageUri!!
    }

    fun copyFileFromFileDescriptor(context: Context, uri: Uri): File {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            inputStream = getInputStreamByUri(context.contentResolver, uri)
            val outFile = createNewJPGFile(context)
            outputStream = FileOutputStream(outFile)
            IOUtils.copy(inputStream, outputStream)
            return outFile
        } catch (e: Exception) {
            e.printStackTrace()
            return createNewJPGFile(context)
        } finally {
            inputStream?.close()
            outputStream?.close()
        }
    }

    fun getInputStreamByUri(contentResolver: ContentResolver, uri: Uri): InputStream {
        val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r", null)
        return FileInputStream(parcelFileDescriptor?.fileDescriptor)
    }

}