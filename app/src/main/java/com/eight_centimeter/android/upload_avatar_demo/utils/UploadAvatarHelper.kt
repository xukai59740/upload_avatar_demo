package com.eight_centimeter.android.upload_avatar_demo.utils

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.core.net.toFile
import com.yalantis.ucrop.UCrop
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.io.File

/**
 * need this function:
 * permissionsdispatcher
 * Rxjava
 * file_provide_path.xml
 * proguard-rules.pro
 * yalantis:ucrop library
 * ImageFileUtil
 * CompressUtil
 */
class UploadAvatarHelper(
    private val activity: Activity,
    private val callBackFirst: (uri: Uri) -> Unit,
    private val callBackFinish: (uri: File) -> Unit,
    private val callBackError: (throwable: Throwable) -> Unit
) {

    private val compositeDisposable = CompositeDisposable()

    companion object {
        const val CHOOSE_IMAGE = 1023
        const val TAKE_IMAGE = 1024
    }

    private var takeUri: Uri? = null
    private var cutUri: Uri? = null

    fun getChooseImageIntent() {
        cleanUri()
        val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(pickIntent, CHOOSE_IMAGE)
    }

    fun takeImageIntent() {
        cleanUri()
        val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val outUri = ImageFileUtil.createNewImageUriByProvider(activity)
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, outUri)
        activity.startActivityForResult(takePhotoIntent, TAKE_IMAGE)
        takeUri = outUri
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CHOOSE_IMAGE -> {
                    data?.data?.let {
                        setPortraitCut(it)
                    }
                }
                TAKE_IMAGE -> {
                    takeUri?.let {
                        setPortraitCut(it)
                    }
                }
                UCrop.REQUEST_CROP -> {
                    cutUri?.let {
                        callback(it)
                    }
                }
            }
        }
    }

    /**
     *  cut image
     */
    private fun setPortraitCut(uri: Uri) {
        //UCrop auto handle the samsung rotate
        //val imageUri = ImageFileUtil.fixBitmapRotate(activity, uri)
        val outUri = ImageFileUtil.createNewImageUriToSelfApp(activity)
        UCrop.of(uri, outUri)
            .withAspectRatio(1f, 1f)
            .withOptions(UCrop.Options().apply {
                setCircleDimmedLayer(true)
                setCompressionFormat(Bitmap.CompressFormat.JPEG)
            }).start(activity)
        cutUri = outUri
    }

    /**
     * handle the cut result, and call back
     */
    private fun callback(uri: Uri) {
        callBackFirst.invoke(uri)
        val disposable = Single.just(Unit)
            .subscribeOn(Schedulers.io())
            .flatMap {
                val file = uri.toFile()
                if (!file.exists()) {
                    Single.error<Throwable>(Throwable(""))
                } else {
                    val compressionFile = CompressUtil.compression(file, activity)
                    Single.just(compressionFile)
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                callBackFinish.invoke(it as File)
            }, {
                callBackError.invoke(it)
            })
        compositeDisposable.add(disposable)
    }

    private fun cleanUri() {
        takeUri = null
        cutUri = null
    }

    fun clean() {
        cleanUri()
        compositeDisposable.dispose()
    }
}