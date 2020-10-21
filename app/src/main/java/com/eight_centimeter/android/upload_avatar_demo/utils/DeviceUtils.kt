package com.eight_centimeter.android.upload_avatar_demo.utils

import android.os.Build

object DeviceUtils {
    private const val SAMSUNG = "samsung"
    fun isSamsung(): Boolean {
        return getManufacturer()?.toLowerCase()?.contains(SAMSUNG) ?: false
    }

    private fun getManufacturer(): String? {
        return if (Build.MANUFACTURER == null) "" else Build.MANUFACTURER.trim()
    }
}