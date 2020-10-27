package com.eight_centimeter.android.upload_avatar_demo.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import com.bumptech.glide.Glide
import com.eight_centimeter.android.upload_avatar_demo.BuildConfig
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.apache.commons.io.IOUtils
import java.io.*
import java.util.*


object ImageFileUtil {

    fun saveUrlInMedia(context: Context, url: String): Disposable {
        return Single.just(Unit)
                .subscribeOn(Schedulers.io())
                .flatMap {
                    val file = Glide.with(context).downloadOnly().load(url).submit().get()
                    Single.just(file)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    saveInMedia(context, it)
                }, {

                })
    }

    private fun saveInMedia(context: Context, file: File): Uri? {
        // insert media
        val contentUri = if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        } else MediaStore.Images.Media.INTERNAL_CONTENT_URI

        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, getTimeFileName())
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/*")
        contentValues.put(MediaStore.Images.Media.DATE_ADDED, Date().time)
        contentValues.put(MediaStore.Images.Media.DATE_TAKEN, Date().time)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_DCIM + File.separator
            )
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val uri = context.contentResolver.insert(contentUri, contentValues) ?: return null

        //save
        var os: OutputStream? = null
        return try {
            os = context.contentResolver.openOutputStream(uri)
            IOUtils.copy(FileInputStream(file), os!!)
            contentValues.clear()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                context.contentResolver.update(uri, contentValues, null, null)
            }
            uri
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            // 失败的时候，删除此 uri 记录
            context.contentResolver.delete(uri, null, null)
            null
        } finally {
            try {
                os?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * if samsung take photo , the image be Rotate 90°， fix it
     */
    fun fixBitmapRotate(context: Context, uri: Uri): Uri {
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
        ///sdcard/Android/data/application/dcim/images/123123123.jpg
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

    fun copyFileFromFileDescriptor(context: Context, uri: Uri): File {
        val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r", null)
        val inputStream = FileInputStream(parcelFileDescriptor?.fileDescriptor)
        val outFile = createNewJPGFile(context)
        val outputStream = FileOutputStream(outFile)
        IOUtils.copy(inputStream, outputStream)
        return outFile
    }


}