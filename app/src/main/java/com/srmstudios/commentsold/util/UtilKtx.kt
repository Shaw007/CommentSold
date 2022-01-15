package com.srmstudios.commentsold.util

import android.widget.EditText
import android.widget.ImageView
import coil.load
import com.srmstudios.commentsold.R

fun ImageView.loadImageUrl(httpsUrl: String) {
    var imageUrl = httpsUrl
    if(imageUrl.isBlank()){
        imageUrl = "https://picsum.photos/200"
    }
    load(imageUrl) {
        placeholder(R.drawable.loading_img)
        error(R.drawable.ic_broken_image)
    }
}

fun EditText.updateText(text: String) {
    setText(text)
    setSelection(text.length)
}