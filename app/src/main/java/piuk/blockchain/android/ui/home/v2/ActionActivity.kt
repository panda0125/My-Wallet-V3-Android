package piuk.blockchain.android.ui.home.v2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.blockchain.coincore.AssetAction
import piuk.blockchain.android.databinding.ActivityActionBinding
import piuk.blockchain.android.ui.base.BlockchainActivity
import piuk.blockchain.android.ui.base.showFragment
import piuk.blockchain.android.ui.swap.SwapFragment
import piuk.blockchain.android.ui.transfer.receive.ReceiveFragment
import piuk.blockchain.android.ui.transfer.send.TransferSendFragment
import piuk.blockchain.android.util.gone
import piuk.blockchain.android.util.visible

class ActionActivity : BlockchainActivity() {

    override val alwaysDisableScreenshots: Boolean
        get() = true

    private val binding: ActivityActionBinding by lazy {
        ActivityActionBinding.inflate(layoutInflater)
    }

    private val action: AssetAction by lazy {
        intent.getSerializableExtra(ACTION) as AssetAction
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportFragmentManager.showFragment(
            fragment = loadFragment(),
            loadingView = binding.progress
        )
    }

    private fun loadFragment(): Fragment {
        showLoading()
        return when (action) {
            AssetAction.Send -> {
                TransferSendFragment.newInstance()
            }
            AssetAction.Swap -> {
                SwapFragment.newInstance()
            }
            // TODO
            //            AssetAction.FiatDeposit -> {
            //                //                supportFragmentManager.showFragment(
            //                //                    fragment = Deposit.newInstance(),
            //                //                    loadingView = binding.progress
            //                //                )
            //            }
            //
            //            AssetAction.Withdraw -> {
            //                //                supportFragmentManager.showFragment(
            //                //                    fragment = Withdrawfr.newInstance(),
            //                //                    loadingView = binding.progress
            //                //                )
            //            }
            AssetAction.Receive -> {
                ReceiveFragment.newInstance()
            }
            else -> {
                throw IllegalStateException("$action is not supported")
            }
        }
    }

    override fun showLoading() {
        binding.progress.visible()
        binding.progress.playAnimation()
    }

    override fun hideLoading() {
        binding.progress.gone()
        binding.progress.pauseAnimation()
    }

    companion object {
        const val ACTION = "action"
        fun start(context: Context, action: AssetAction) {
            Intent(context, ActionActivity::class.java).apply {
                putExtra(ACTION, action)
                context.startActivity(this)
            }
        }
    }
}