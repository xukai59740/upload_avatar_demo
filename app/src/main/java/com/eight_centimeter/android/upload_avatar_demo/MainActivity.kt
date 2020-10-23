package com.eight_centimeter.android.upload_avatar_demo

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.eight_centimeter.android.upload_avatar_demo.utils.ImageFileUtil
import com.eight_centimeter.android.upload_avatar_demo.utils.UploadAvatarHelper
import kotlinx.android.synthetic.main.activity_main.*
import permissions.dispatcher.*

@RuntimePermissions
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ivPortrait.setOnClickListener {
            showImagePickerDialog()
        }
        btSave.setOnClickListener {
            val disposable = ImageFileUtil.saveUrlInMedia(this,  "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1603436697044&di=dece60f3c9e73e07043e3651dd3da3be&imgtype=0&src=http%3A%2F%2Fwww.ruiqi6.com%2Fzb_users%2Fupload%2F2017%2F04%2F20170428115034526536876.jpg")
            uploadAvatarHelper.addCompositeDisposable(disposable)
        }
    }

    private fun showImagePickerDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("please_select_picture")
                .setItems(arrayOf("take", "choose")) { _, which ->
                    if (which == 0) {
                        openCameraWithPermissionCheck()
                    } else {
                        uploadAvatarHelper.getChooseImageIntent()
                    }
                }
        builder.create().show()
    }

    private val uploadAvatarHelper by lazy {
        UploadAvatarHelper(this, { uri ->
            portraitLoadingProgressBar.isVisible = true
        }, { file ->
            portraitLoadingProgressBar.isVisible = false
            ivPortrait.loadChatUserAvatar(file)
        }) { _ ->
            portraitLoadingProgressBar.isVisible = false
            Toast.makeText(this, "Image not found", Toast.LENGTH_LONG).show()
        }
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    fun openCamera() {
        uploadAvatarHelper.takeImageIntent()
    }

    @OnShowRationale(Manifest.permission.CAMERA)
    fun showRationaleForCamera(request: PermissionRequest) {
        request.proceed()
    }

    @OnPermissionDenied(Manifest.permission.CAMERA)
    fun onCameraDenied() {
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA)
    fun onCameraNeverAskAgain() {
    }

    @SuppressLint("NeedOnRequestPermissionsResult")
    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        uploadAvatarHelper.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        uploadAvatarHelper.clean()
        super.onDestroy()
    }

}