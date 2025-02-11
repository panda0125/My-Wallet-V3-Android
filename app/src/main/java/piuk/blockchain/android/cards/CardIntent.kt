package piuk.blockchain.android.cards

import com.blockchain.commonarch.presentation.mvi.MviIntent
import com.blockchain.nabu.datamanagers.BillingAddress
import com.blockchain.nabu.datamanagers.PaymentMethod

sealed class CardIntent : MviIntent<CardState> {

    class UpdateBillingAddress(private val billingAddress: BillingAddress) : CardIntent() {
        override fun reduce(oldState: CardState): CardState =
            oldState.copy(billingAddress = billingAddress)
    }

    class CardUpdated(val cardDetails: PaymentMethod.Card) : CardIntent() {
        override fun reduce(oldState: CardState): CardState = oldState.copy(cardStatus = cardDetails.status)
    }

    class ActivateCard(val card: CardData, val cardId: String) : CardIntent() {
        override fun reduce(oldState: CardState): CardState =
            oldState
    }

    class AuthoriseCard(private val credentials: CardAcquirerCredentials) : CardIntent() {
        override fun reduce(oldState: CardState): CardState =
            oldState.copy(authoriseCard = credentials)
    }

    class UpdateCardId(private val cardId: String) : CardIntent() {
        override fun reduce(oldState: CardState): CardState =
            oldState.copy(cardId = cardId)
    }

    class AddNewCard(val cardData: CardData) : CardIntent() {
        override fun reduce(oldState: CardState): CardState =
            oldState

        override fun isValidFor(oldState: CardState): Boolean {
            return oldState.billingAddress != null
        }
    }

    class UpdateRequestState(private val status: CardRequestStatus) : CardIntent() {
        override fun reduce(oldState: CardState): CardState =
            oldState.copy(cardRequestStatus = status)
    }

    object ResetCardAuth : CardIntent() {
        override fun reduce(oldState: CardState): CardState =
            oldState.copy(authoriseCard = null)
    }

    object ReadyToAddNewCard : CardIntent() {
        override fun reduce(oldState: CardState): CardState =
            oldState.copy(addCard = true)
    }

    object CardAddRequested : CardIntent() {
        override fun reduce(oldState: CardState): CardState =
            oldState.copy(addCard = false)
    }

    object CheckCardStatus : CardIntent() {
        override fun reduce(oldState: CardState): CardState =
            oldState
    }
}
