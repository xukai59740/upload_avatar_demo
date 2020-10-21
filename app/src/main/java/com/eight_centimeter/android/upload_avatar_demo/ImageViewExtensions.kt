package com.eight_centimeter.android.upload_avatar_demo

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import jp.wasabeef.glide.transformations.CropCircleTransformation

fun ImageView.load(
    absolutePath: Any,
    transformation: Transformation<Bitmap>? = null,
    imageSize: Int? = null,
    placeholder: Int? = null,
    listener: RequestListener<Drawable>? = null
) {
    val glideRequest = GlideApp.with(this).load(absolutePath).dontAnimate().skipMemoryCache(false)
    imageSize?.let {
        glideRequest.override(imageSize)
    }
    placeholder?.let {
        glideRequest.error(it)

        if (this.drawable != null) {
            glideRequest.placeholder(this.drawable)
        } else {
        }
    }
    transformation?.let {
        glideRequest.apply(RequestOptions.bitmapTransform(it))
    }
    if (listener != null) {
        glideRequest.listener(listener).into(this)
    } else {
        glideRequest.listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {

                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                return false
            }
        })
            .into(this)
    }

}

//// Avatar
fun ImageView.loadChatUserAvatar(path: Any) {
    val multiTransformation = MultiTransformation(CropCircleTransformation())
    load(path, multiTransformation)
}

