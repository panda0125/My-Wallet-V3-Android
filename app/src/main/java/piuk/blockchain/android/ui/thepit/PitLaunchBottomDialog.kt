package piuk.blockchain.android.ui.thepit

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.blockchain.ui.urllinks.URL_THE_PIT_LAUNCH_SUPPORT
import piuk.blockchain.android.BuildConfig
import piuk.blockchain.android.R
import piuk.blockchain.android.util.launchUrlInBrowser
import piuk.blockchain.android.ui.customviews.ErrorBottomDialog

class PitLaunchBottomDialog : ErrorBottomDialog() {
    override val layout: Int
        get() = R.layout.pit_launch_bottom_dialog

    companion object {
        private const val ARG_CONTENT = "arg_content"

        private fun newInstance(content: Content): PitLaunchBottomDialog {
            return PitLaunchBottomDialog().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_CONTENT, content)
                }
            }
        }

        fun launch(activity: FragmentActivity) {
            newInstance(
                Content(
                    activity.getString(R.string.the_exchange_title),
                    "",
                    R.string.launch_the_exchange,
                    R.string.the_exchange_contact_support,
                    R.drawable.ic_the_exchange_colour
                )
            ).apply {
                onCtaClick = { activity.launchUrlInBrowser(BuildConfig.PIT_LAUNCHING_URL) }
                onDismissClick = { activity.launchUrlInBrowser(URL_THE_PIT_LAUNCH_SUPPORT) }
                show(activity.supportFragmentManager, "BottomDialog")
            }
        }
    }
}
