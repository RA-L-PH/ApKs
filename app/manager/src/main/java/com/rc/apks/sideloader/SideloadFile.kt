package com.rc.apks.sideloader

import android.graphics.drawable.Drawable
import java.io.File

data class SideloadFile(
    val file: File,
    val name: String,
    val packageName: String,
    val versionName: String,
    val sizeText: String,
    val icon: Drawable?,
    val isApks: Boolean,
    var isInstalled: Boolean = false
)
