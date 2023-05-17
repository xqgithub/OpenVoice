package com.shannon.openvoice.extended

import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.shannon.openvoice.R

/**
 *
 * @ProjectName:    OpenVoice
 * @Package:        com.shannon.openvoice.extended
 * @ClassName:      ImageView
 * @Description:     java类作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/2 9:56
 */

fun ImageView.loadAvatar(url: String, _placeholder: Int = -1, _error: Int = -1) {
    Glide.with(this)
        .load(url)
        .placeholder(if (_placeholder == -1) R.drawable.icon_default_avatar else _placeholder)
        .error(if (_error == -1) R.drawable.icon_default_avatar else _error)
        .centerCrop()
        .into(this)
}

fun ImageView.loadAvatar2(url: String) {
    Glide.with(this)
        .load(url)
        .centerCrop()
        .into(this)
}

fun ImageView.loadUri(uri: Uri) {
    Glide.with(this)
        .load(uri)
        .placeholder(R.drawable.icon_default_avatar)
        .error(R.drawable.icon_default_avatar)
        .centerCrop()
        .into(this)
}

fun ImageView.loadImage(uri: Uri) {
    Glide.with(this)
        .load(uri)
        .centerCrop()
        .into(this)
}

fun ImageView.loadImage(url: String) {
    Glide.with(this)
        .load(url)
        .centerCrop()
        .into(this)
}