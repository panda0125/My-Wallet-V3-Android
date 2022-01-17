package com.blockchain.componentlib.card.abstract

import android.content.Context
import android.util.AttributeSet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.blockchain.componentlib.card.CallOutCard
import com.blockchain.componentlib.card.CardButton
import com.blockchain.componentlib.image.ImageResource
import com.blockchain.componentlib.theme.AppSurface
import com.blockchain.componentlib.theme.AppTheme
import com.blockchain.componentlib.utils.BaseAbstractComposeView

class CallOutCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseAbstractComposeView(context, attrs, defStyleAttr) {

    var title by mutableStateOf("")
    var subtitle by mutableStateOf("")
    var iconResource: ImageResource by mutableStateOf(ImageResource.None)
    var callToActionButton by mutableStateOf(null as? CardButton?)

    @Composable
    override fun Content() {
        AppTheme {
            AppSurface {
                CallOutCard(
                    title = title,
                    subtitle = subtitle,
                    iconResource = iconResource,
                    callToActionButton = callToActionButton
                )
            }
        }
    }

    fun clearState() {
        title = ""
        subtitle = ""
        iconResource = ImageResource.None
        callToActionButton = null
    }
}
