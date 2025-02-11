package com.blockchain.componentlib.sectionheader

import android.content.Context
import android.util.AttributeSet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.blockchain.componentlib.R
import com.blockchain.componentlib.image.ImageResource
import com.blockchain.componentlib.theme.AppSurface
import com.blockchain.componentlib.theme.AppTheme
import com.blockchain.componentlib.utils.BaseAbstractComposeView

class BalanceSectionHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseAbstractComposeView(context, attrs, defStyleAttr) {

    var defaultIcon = ImageResource.Local(R.drawable.ic_star, null)
    var primaryText by mutableStateOf("")
    var secondaryText by mutableStateOf("")
    var iconResource: ImageResource by mutableStateOf(defaultIcon)
    var onIconClick by mutableStateOf({})

    @Composable
    override fun Content() {
        AppTheme {
            AppSurface {
                BalanceSectionHeader(
                    primaryText = primaryText,
                    secondaryText = secondaryText,
                    iconResource = iconResource,
                    onIconClick = onIconClick
                )
            }
        }
    }

    fun clearState() {
        primaryText = ""
        secondaryText = ""
        iconResource = defaultIcon
        onIconClick = {}
    }
}
