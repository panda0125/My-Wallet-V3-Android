package piuk.blockchain.android.simplebuy

import com.blockchain.coincore.ExchangePriceWithDelta
import com.blockchain.commonarch.presentation.mvi.MviIntent
import com.blockchain.core.custodial.models.BrokerageQuote
import com.blockchain.core.limits.TxLimits
import com.blockchain.core.payments.model.LinkBankTransfer
import com.blockchain.core.payments.model.LinkedBank
import com.blockchain.core.payments.model.Partner
import com.blockchain.core.price.ExchangeRate
import com.blockchain.nabu.datamanagers.BuySellOrder
import com.blockchain.nabu.datamanagers.OrderState
import com.blockchain.nabu.datamanagers.PaymentMethod
import com.blockchain.nabu.datamanagers.custodialwalletimpl.PaymentMethodType
import com.blockchain.nabu.models.data.EligibleAndNextPaymentRecurringBuy
import com.blockchain.nabu.models.data.RecurringBuyFrequency
import com.blockchain.nabu.models.data.RecurringBuyState
import info.blockchain.balance.AssetInfo
import info.blockchain.balance.CryptoValue
import info.blockchain.balance.FiatCurrency
import info.blockchain.balance.FiatValue
import java.math.BigInteger
import piuk.blockchain.android.cards.CardAcquirerCredentials
import piuk.blockchain.android.ui.transactionflow.engine.TransactionErrorState

sealed class SimpleBuyIntent : MviIntent<SimpleBuyState> {

    override fun isValidFor(oldState: SimpleBuyState): Boolean {
        return oldState.buyErrorState == null
    }

    override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
        oldState

    class AmountUpdated(val amount: FiatValue) : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            oldState.copy(amount = amount)
    }

    object ResetLinkBankTransfer : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            oldState.copy(linkBankTransfer = null, newPaymentMethodToBeAdded = null)
    }

    class UpdateErrorState(private val errorState: TransactionErrorState) : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            oldState.copy(errorState = errorState)
    }

    class Open3dsAuth(private val cardAcquirerCredentials: CardAcquirerCredentials) : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            oldState.copy(cardAcquirerCredentials = cardAcquirerCredentials)
    }

    class AuthorisePaymentExternalUrl(private val url: String, private val linkedBank: LinkedBank) : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            oldState.copy(authorisePaymentUrl = url, linkedBank = linkedBank)
    }

    class ExchangePriceWithDeltaUpdated(private val exchangePriceWithDelta: ExchangePriceWithDelta) :
        SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            oldState.copy(exchangePriceWithDelta = exchangePriceWithDelta)
    }

    class PaymentMethodChangeRequested(val paymentMethod: PaymentMethod) : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState = oldState.copy(
            errorState = TransactionErrorState.NONE
        )
    }

    class FetchPaymentDetails(val fiatCurrency: FiatCurrency, val selectedPaymentMethodId: String) : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState = oldState
    }

    class PaymentMethodsUpdated(
        private val paymentOptions: PaymentOptions,
        val selectedPaymentMethod: SelectedPaymentMethod?
    ) : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState {
            return oldState.copy(
                isLoading = false,
                selectedPaymentMethod = selectedPaymentMethod,
                paymentOptions = paymentOptions
            )
        }
    }

    class SelectedPaymentMethodUpdate(
        private val paymentMethod: PaymentMethod
    ) : SimpleBuyIntent() {
        // UndefinedBankAccount has no visual representation in the UI, it just opens WireTransferDetails,
        // hence why we're ignoring it and keeping the current selectedPaymentMethod
        override fun isValidFor(oldState: SimpleBuyState): Boolean =
            paymentMethod !is PaymentMethod.UndefinedBankAccount

        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            oldState.copy(
                selectedPaymentMethod = SelectedPaymentMethod(
                    id = paymentMethod.id,
                    // no partner for bank transfer or ui label. Ui label for bank transfer is coming from resources
                    partner = (paymentMethod as? PaymentMethod.Card)?.partner,
                    label = paymentMethod.detailedLabel(),
                    paymentMethodType = when (paymentMethod) {
                        is PaymentMethod.UndefinedBankTransfer -> PaymentMethodType.BANK_TRANSFER
                        is PaymentMethod.UndefinedCard -> PaymentMethodType.PAYMENT_CARD
                        is PaymentMethod.Bank -> PaymentMethodType.BANK_TRANSFER
                        is PaymentMethod.Funds -> PaymentMethodType.FUNDS
                        is PaymentMethod.UndefinedBankAccount -> PaymentMethodType.FUNDS
                        is PaymentMethod.GooglePay -> PaymentMethodType.GOOGLE_PAY
                        else -> PaymentMethodType.PAYMENT_CARD
                    },
                    isEligible = paymentMethod.isEligible
                )
            )
    }

    object BuyButtonClicked : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            oldState.copy(confirmationActionRequested = true, orderState = OrderState.INITIALISED)
    }

    object LinkBankTransferRequested : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState = oldState.copy(
            newPaymentMethodToBeAdded = null
        )
    }

    object TryToLinkABankTransfer : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState {
            return oldState.copy(isLoading = true)
        }
    }

    object ClearAnySelectedPaymentMethods : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            oldState.copy(
                selectedPaymentMethod = null
            )
    }

    object ValidateAmount : SimpleBuyIntent()

    data class UpdatedBuyLimits(
        val limits: TxLimits
    ) : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState {
            return oldState.copy(
                transferLimits = limits
            )
        }
    }

    data class SupportedCurrenciesUpdated(private val currencies: List<FiatCurrency>) : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            oldState.copy(supportedFiatCurrencies = currencies)
    }

    data class WithdrawLocksTimeUpdated(private val time: BigInteger = BigInteger.ZERO) : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState {
            return oldState.copy(withdrawalLockPeriod = time)
        }
    }

    data class InitialiseSelectedCryptoAndFiat(val asset: AssetInfo, val fiatCurrency: FiatCurrency) :
        SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            oldState.copy(selectedCryptoAsset = asset, fiatCurrency = fiatCurrency)
    }

    data class UpdateExchangeRate(val fiatCurrency: FiatCurrency, val asset: AssetInfo) : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            oldState
    }

    data class ExchangeRateUpdated(private val exchangeRate: ExchangeRate) : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            oldState.copy(fiatRate = exchangeRate)
    }

    data class FlowCurrentScreen(val flowScreen: FlowScreen) : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            oldState.copy(currentScreen = flowScreen)
    }

    data class FetchSuggestedPaymentMethod(
        val fiatCurrency: FiatCurrency,
        val selectedPaymentMethodId: String? = null
    ) : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            oldState.copy(paymentOptions = PaymentOptions(), selectedPaymentMethod = null)
    }

    object FetchSupportedFiatCurrencies : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            oldState.copy(supportedFiatCurrencies = emptyList())
    }

    object CancelOrder : SimpleBuyIntent() {
        override fun isValidFor(oldState: SimpleBuyState) = true
    }

    object CancelOrderAndResetAuthorisation : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            oldState.copy(
                authorisePaymentUrl = null,
                linkedBank = null
            )
    }

    object ClearState : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            SimpleBuyState()

        override fun isValidFor(oldState: SimpleBuyState): Boolean {
            return oldState.orderState < OrderState.PENDING_CONFIRMATION ||
                oldState.orderState > OrderState.PENDING_EXECUTION
        }
    }

    object ConfirmOrder : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            oldState.copy(
                confirmationActionRequested = true,
                isLoading = true
            )
    }

    object FetchWithdrawLockTime : SimpleBuyIntent()

    object NavigationHandled : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            oldState.copy(confirmationActionRequested = false, newPaymentMethodToBeAdded = null)
    }

    object KycStarted : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            oldState.copy(
                kycStartedButNotCompleted = true,
                currentScreen = FlowScreen.KYC,
                kycVerificationState = null
            )
    }

    class ErrorIntent(private val error: ErrorState = ErrorState.GenericError) : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            oldState.copy(
                buyErrorState = error,
                isLoading = false,
                confirmationActionRequested = false
            )

        override fun isValidFor(oldState: SimpleBuyState): Boolean {
            return true
        }
    }

    object KycCompleted : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            oldState.copy(kycStartedButNotCompleted = false)
    }

    object FetchKycState : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            oldState.copy(kycVerificationState = KycState.PENDING)
    }

    class KycStateUpdated(val kycState: KycState) : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            oldState.copy(kycVerificationState = kycState)
    }

    class BankLinkProcessStarted(private val bankTransfer: LinkBankTransfer) : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState {
            return oldState.copy(
                linkBankTransfer = bankTransfer,
                confirmationActionRequested = false,
                newPaymentMethodToBeAdded = null,
                isLoading = false
            )
        }
    }

    object OrderCanceled : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            SimpleBuyState(
                orderState = OrderState.CANCELED
            )
    }

    object FinishedFirstBuy : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            oldState.copy(showRecurringBuyFirstTimeFlow = true)
    }

    class OrderCreated(
        private val buyOrder: BuySellOrder,
        private val quote: BrokerageQuote
    ) : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            oldState.copy(
                orderState = buyOrder.state,
                id = buyOrder.id,
                quote = BuyQuote.fromBrokerageQuote(quote, buyOrder.source.currency as FiatCurrency, buyOrder.fee),
                orderValue = buyOrder.orderValue as CryptoValue,
                paymentSucceeded = buyOrder.state == OrderState.FINISHED,
                isLoading = false,
                recurringBuyState = if (buyOrder.recurringBuyId.isNullOrBlank()) {
                    RecurringBuyState.UNINITIALISED
                } else {
                    RecurringBuyState.ACTIVE
                }
            )
    }

    class OrderConfirmed(
        private val buyOrder: BuySellOrder,
        private val showInAppRating: Boolean = false
    ) : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            oldState.copy(
                orderState = buyOrder.state,
                paymentSucceeded = buyOrder.state == OrderState.FINISHED,
                isLoading = false,
                orderValue = buyOrder.orderValue as CryptoValue,
                showRating = showInAppRating,
                recurringBuyState = if (buyOrder.recurringBuyId.isNullOrBlank()) {
                    RecurringBuyState.UNINITIALISED
                } else {
                    RecurringBuyState.ACTIVE
                }
            )
    }

    object AppRatingShown : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            oldState.copy(showRating = false)
    }

    class UpdateSelectedPaymentCard(
        private val id: String,
        private val label: String?,
        private val partner: Partner,
        private val isEligible: Boolean
    ) : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            oldState.copy(
                selectedPaymentMethod = SelectedPaymentMethod(
                    id, partner, label, PaymentMethodType.PAYMENT_CARD, isEligible
                )
            )
    }

    object ClearError : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            oldState.copy(buyErrorState = null)

        override fun isValidFor(oldState: SimpleBuyState): Boolean {
            return oldState.buyErrorState != null
        }
    }

    object ResetCardPaymentAuth : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            oldState.copy(cardAcquirerCredentials = null)
    }

    object CancelOrderIfAnyAndCreatePendingOne : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            oldState.copy(
                isLoading = true
            )

        override fun isValidFor(oldState: SimpleBuyState): Boolean {
            return oldState.selectedCryptoAsset != null &&
                oldState.order.amount != null &&
                oldState.orderState != OrderState.AWAITING_FUNDS &&
                oldState.orderState != OrderState.PENDING_EXECUTION
        }
    }

    class MakePayment(val orderId: String) : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            oldState.copy(isLoading = true)
    }

    class GetAuthorisationUrl(val orderId: String) : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            oldState.copy(isLoading = true)
    }

    object CheckOrderStatus : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            oldState.copy(isLoading = true)
    }

    object PaymentSucceeded : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            oldState.copy(paymentSucceeded = true, isLoading = false)
    }

    object UnlockHigherLimits : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            oldState.copy(shouldShowUnlockHigherFunds = true)
    }

    object PaymentPending : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            oldState.copy(paymentPending = true, isLoading = false)
    }

    class AddNewPaymentMethodRequested(private val paymentMethod: PaymentMethod) : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            oldState.copy(newPaymentMethodToBeAdded = paymentMethod)
    }

    class UpdatePaymentMethodsAndAddTheFirstEligible(val fiatCurrency: FiatCurrency) : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            oldState.copy(
                paymentOptions = PaymentOptions(),
                errorState = TransactionErrorState.NONE,
                selectedPaymentMethod = null
            )
    }

    object AddNewPaymentMethodHandled : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            oldState.copy(newPaymentMethodToBeAdded = null)
    }

    class RecurringBuyIntervalUpdated(private val recurringBuyFrequency: RecurringBuyFrequency) :
        SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            oldState.copy(recurringBuyFrequency = recurringBuyFrequency)
    }

    class RecurringBuySelectedFirstTimeFlow(val recurringBuyFrequency: RecurringBuyFrequency) :
        SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            oldState.copy(recurringBuyFrequency = recurringBuyFrequency)
    }

    object RecurringBuyCreatedFirstTimeFlow : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            oldState.copy(recurringBuyState = RecurringBuyState.ACTIVE)
    }

    class RecurringBuyEligibilityUpdated(
        private val eligibilityNextPaymentList: List<EligibleAndNextPaymentRecurringBuy>
    ) : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            oldState.copy(
                eligibleAndNextPaymentRecurringBuy = eligibilityNextPaymentList
            )
    }

    class TxLimitsUpdated(private val limits: TxLimits) : SimpleBuyIntent() {
        override fun reduce(oldState: SimpleBuyState): SimpleBuyState =
            oldState.copy(
                transferLimits = limits
            )
    }
}
