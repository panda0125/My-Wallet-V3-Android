package com.blockchain.componentlib.image

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.ColorFilter

sealed class ImageResource(
    val contentDescription: String? = null
) {

    class Local(
        @DrawableRes val id: Int,
        contentDescription: String?,
        val colorFilter: ColorFilter? = null,
    ) : ImageResource(contentDescription) {

        fun withColorFilter(colorFilter: ColorFilter) = Local(
            id = id,
            contentDescription = contentDescription,
            colorFilter = colorFilter
        )
    }

    class LocalWithBackground(
        @DrawableRes val id: Int,
        @ColorRes val filterColorId: Int,
        @ColorRes val tintColorId: Int,
        val alpha: Float = 0.15F,
        contentDescription: String?
    ) : ImageResource(contentDescription)

    class Remote(
        val url: String,
        contentDescription: String?,
        val colorFilter: ColorFilter? = null,
    ) : ImageResource(contentDescription)

    object None : ImageResource(null)
}
