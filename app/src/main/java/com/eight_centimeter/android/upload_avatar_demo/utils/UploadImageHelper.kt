package com.eight_centimeter.android.upload_avatar_demo.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.core.net.toUri
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.File


class UploadImageHelper(
        private val activity: Activity,
        private val callBackFirst: (uri: Uri) -> Unit
) {

    private val compositeDisposable = CompositeDisposable()

    companion object {
        const val CHOOSE_IMAGE = 1023
        const val TAKE_IMAGE = 1024
    }

    private var takeUri: Uri? = null

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
                        setPortrait(it)
                    }
                }
                TAKE_IMAGE -> {
                    takeUri?.let {
                        setPortrait(it)
                    }
                }
            }
        }
    }

    private fun setPortrait(uri: Uri) {
        val disposable = Single.just(Unit)
                .subscribeOn(Schedulers.io())
                .flatMap {
                    try {
                        val imageUri = ImageFileUtil.fixBitmapRotate(activity, uri)
                        val copyFile = ImageFileUtil.copyFileFromFileDescriptor(activity, imageUri)
                        Single.just(copyFile)
                    } catch (e: Exception) {
                        Single.error<Throwable>(Throwable(""))
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    callBackFirst.invoke((it as File).toUri())
                }, {
                })
        addCompositeDisposable(disposable)
    }

    private fun cleanUri() {
        takeUri = null
    }

    private fun addCompositeDisposable(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    fun clean() {
        cleanUri()
        compositeDisposable.dispose()
    }
}