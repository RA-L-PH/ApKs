package com.rc.apks.sideloader

import android.graphics.drawable.Drawable
import java.io.File

data class SideloadFile(
    val file: File,
    val name: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val sizeText: String,
    val fileSize: Long,
    val icon: Drawable?,
    val isApks: Boolean,
    var isInstalled: Boolean = false
)
