package piuk.blockchain.android.ui.settings.v2.account

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.blockchain.commonarch.presentation.mvi.MviActivity
import com.blockchain.componentlib.databinding.ToolbarGeneralBinding
import com.blockchain.componentlib.image.ImageResource
import com.blockchain.componentlib.tag.TagType
import com.blockchain.componentlib.tag.TagViewState
import com.blockchain.koin.scopedInject
import info.blockchain.balance.FiatCurrency
import piuk.blockchain.android.BuildConfig
import piuk.blockchain.android.R
import piuk.blockchain.android.databinding.ActivityAccountBinding
import piuk.blockchain.android.simplebuy.CurrencySelectionSheet
import piuk.blockchain.android.ui.airdrops.AirdropCentreActivity
import piuk.blockchain.android.ui.customviews.ErrorBottomDialog
import piuk.blockchain.android.ui.customviews.ToastCustom
import piuk.blockchain.android.ui.customviews.toast
import piuk.blockchain.android.ui.kyc.limits.KycLimitsActivity
import piuk.blockchain.android.ui.settings.SettingsAnalytics
import piuk.blockchain.android.ui.thepit.ExchangeConnectionSheet
import piuk.blockchain.android.ui.thepit.PitPermissionsActivity
import piuk.blockchain.android.urllinks.URL_THE_PIT_LAUNCH_SUPPORT
import piuk.blockchain.android.util.launchUrlInBrowser

class AccountActivity :
    MviActivity<AccountModel, AccountIntent, AccountState, ActivityAccountBinding>(),
    CurrencySelectionSheet.Host {

    override val alwaysDisableScreenshots: Boolean = true

    override val model: AccountModel by scopedInject()

    override val toolbarBinding: ToolbarGeneralBinding
        get() = binding.toolbar

    override fun initBinding(): ActivityAccountBinding =
        ActivityAccountBinding.inflate(layoutInflater, null, false)

    private lateinit var walletId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateToolbar(
            toolbarTitle = getString(R.string.account_toolbar),
            backAction = { onBackPressed() }
        )

        with(binding) {
            settingsLimits.apply {
                primaryText = getString(R.string.account_limits_title)
                secondaryText = getString(R.string.account_limits_subtitle)
                onClick = {
                    startActivity(KycLimitsActivity.newIntent(this@AccountActivity))
                }
            }

            settingsWalletId.apply {
                primaryText = getString(R.string.account_wallet_id_title)
                secondaryText = getString(R.string.account_wallet_id_subtitle)
                endImageResource = ImageResource.Local(R.drawable.ic_copy, null)
                onClick = {
                    analytics.logEvent(SettingsAnalytics.WalletIdCopyClicked)

                    if (::walletId.isInitialized) {
                        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("walletId", walletId)
                        clipboard.setPrimaryClip(clip)
                        toast(R.string.copied_to_clipboard, ToastCustom.TYPE_OK)
                        analytics.logEvent(SettingsAnalytics.WalletIdCopyCopied)
                    } else {
                        toast(R.string.account_wallet_id_copy_error, ToastCustom.TYPE_ERROR)
                    }
                }
            }

            settingsCurrency.apply {
                primaryText = getString(R.string.account_currency_title)
                onClick = {
                    model.process(AccountIntent.LoadFiatList)
                }
            }

            settingsExchange.apply {
                primaryText = getString(R.string.account_exchange_title)
                onClick = {
                    model.process(AccountIntent.LoadExchange)
                }
            }

            settingsAirdrops.apply {
                primaryText = getString(R.string.account_airdrops_title)
                onClick = {
                    startActivity(AirdropCentreActivity.newIntent(this@AccountActivity))
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        model.process(AccountIntent.LoadAccountInformation)
        model.process(AccountIntent.LoadExchangeInformation)
    }

    override fun render(newState: AccountState) {
        if (newState.accountInformation != null) {
            renderWalletInformation(newState.accountInformation)
        }

        if (newState.viewToLaunch != ViewToLaunch.None) {
            renderViewToLaunch(newState)
        }

        renderExchangeInformation(newState.exchangeLinkingState)
        renderErrorState(newState.errorState)
    }

    private fun renderExchangeInformation(exchangeLinkingState: ExchangeLinkingState) =
        when (exchangeLinkingState) {
            ExchangeLinkingState.UNKNOWN,
            ExchangeLinkingState.NOT_LINKED -> {
                binding.settingsExchange.secondaryText = getString(R.string.account_exchange_not_connected)
            }
            ExchangeLinkingState.LINKED -> {
                with(binding.settingsExchange) {
                    secondaryText = null
                    tags = listOf(TagViewState(getString(R.string.account_exchange_connected), TagType.Success()))
                }
            }
        }

    private fun renderErrorState(error: AccountError) =
        when (error) {
            AccountError.ACCOUNT_INFO_FAIL -> {
                toast(getString(R.string.account_load_info_error), ToastCustom.TYPE_ERROR)
            }
            AccountError.FIAT_LIST_FAIL -> {
                toast(getString(R.string.account_load_fiat_error), ToastCustom.TYPE_ERROR)
            }
            AccountError.ACCOUNT_FIAT_UPDATE_FAIL -> {
                toast(getString(R.string.account_fiat_update_error), ToastCustom.TYPE_ERROR)
            }
            AccountError.EXCHANGE_INFO_FAIL -> {
                toast(getString(R.string.account_exchange_info_error), ToastCustom.TYPE_ERROR)
            }
            AccountError.EXCHANGE_LOAD_FAIL -> {
                toast(getString(R.string.account_load_exchange_error), ToastCustom.TYPE_ERROR)
            }
            AccountError.NONE -> {
                // do nothing
            }
        }

    private fun renderWalletInformation(accountInformation: AccountInformation) {
        walletId = accountInformation.walletId

        binding.settingsCurrency.apply {
            secondaryText = accountInformation.userCurrency.nameWithSymbol()
        }
    }

    private fun renderViewToLaunch(newState: AccountState) {
        when (val view = newState.viewToLaunch) {
            is ViewToLaunch.CurrencySelection -> {
                showBottomSheet(
                    CurrencySelectionSheet.newInstance(
                        currencies = view.currencyList,
                        selectedCurrency = view.selectedCurrency,
                        currencySelectionType = CurrencySelectionSheet.Companion.CurrencySelectionType.DISPLAY_CURRENCY
                    )
                )
            }
            is ViewToLaunch.ExchangeLink -> {
                when (view.exchangeLinkingState) {
                    ExchangeLinkingState.UNKNOWN,
                    ExchangeLinkingState.NOT_LINKED -> {
                        showBottomSheet(
                            ExchangeConnectionSheet.newInstance(
                                ErrorBottomDialog.Content(
                                    title = getString(R.string.account_exchange_connect_title),
                                    description = getString(R.string.account_exchange_connect_subtitle),
                                    ctaButtonText = R.string.common_connect,
                                    icon = R.drawable.ic_exchange_logo
                                ),
                                emptyList(),
                                primaryCtaClick = {
                                    PitPermissionsActivity.start(this, "")
                                }
                            )
                        )
                    }
                    ExchangeLinkingState.LINKED -> {
                        showBottomSheet(
                            ExchangeConnectionSheet.newInstance(
                                ErrorBottomDialog.Content(
                                    title = getString(R.string.account_exchange_connected_title),
                                    ctaButtonText = R.string.account_exchange_connected_cta,
                                    dismissText = R.string.contact_support,
                                    icon = R.drawable.ic_exchange_logo
                                ),
                                listOf(TagViewState(getString(R.string.account_exchange_connected), TagType.Success())),
                                // TODO this will be changed to support the Exchange app?
                                primaryCtaClick = {
                                    launchUrlInBrowser(BuildConfig.PIT_LAUNCHING_URL)
                                },
                                secondaryCtaClick = {
                                    launchUrlInBrowser(URL_THE_PIT_LAUNCH_SUPPORT)
                                }
                            )
                        )
                    }
                }
            }
            ViewToLaunch.None -> {
                // do nothing
            }
        }
        model.process(AccountIntent.ResetViewState)
    }

    override fun onCurrencyChanged(currency: FiatCurrency) {
        model.process(AccountIntent.UpdateFiatCurrency(currency))
    }

    override fun onSheetClosed() {
        // do nothing
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, AccountActivity::class.java)
    }
}
