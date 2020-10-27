package com.eight_centimeter.android.upload_avatar_demo

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.eight_centimeter.android.upload_avatar_demo.utils.UploadImageHelper
import kotlinx.android.synthetic.main.activity_save.*
import permissions.dispatcher.*

@RuntimePermissions
class SaveActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save)
        ivPortrait.setOnClickListener {
            showImagePickerDialog()
        }

    }


    private fun showImagePickerDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("please_select_picture")
                .setItems(arrayOf("take", "choose")) { _, which ->
                    if (which == 0) {
                        openCameraWithPermissionCheck()
                    } else {
                        uploadImageHelper.getChooseImageIntent()
                    }
                }
        builder.create().show()
    }

    private val uploadImageHelper by lazy {
        UploadImageHelper(this) { uri ->
            ivPortrait.loadChatUserAvatar(uri)
        }
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    fun openCamera() {
        uploadImageHelper.takeImageIntent()
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
        uploadImageHelper.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

}