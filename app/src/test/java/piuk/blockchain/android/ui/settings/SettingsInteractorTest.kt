package piuk.blockchain.android.ui.settings

import com.blockchain.core.Database
import com.blockchain.nabu.BasicProfileInfo
import com.blockchain.nabu.Feature
import com.blockchain.nabu.Tier
import com.blockchain.nabu.UserIdentity
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import exchangerate.HistoricRateQueries
import io.reactivex.rxjava3.core.Single
import org.junit.Before
import org.junit.Test
import piuk.blockchain.android.ui.home.CredentialsWiper
import piuk.blockchain.android.ui.settings.v2.SettingsInteractor

class SettingsInteractorTest {

    private lateinit var interactor: SettingsInteractor
    private val userIdentity: UserIdentity = mock()
    private val database: Database = mock()
    private val credentialsWiper: CredentialsWiper = mock()

    @Before
    fun setup() {
        interactor = SettingsInteractor(
            userIdentity = userIdentity,
            database = database,
            credentialsWiper = credentialsWiper
        )
    }

    @Test
    fun `Load eligibility`() {
        val userInformation = mock<BasicProfileInfo>()

        whenever(userIdentity.isVerifiedFor(Feature.TierLevel(Tier.GOLD))).thenReturn(Single.just(true))
        whenever(userIdentity.getBasicProfileInformation()).thenReturn(Single.just(userInformation))
        val observer = interactor.getSupportEligibilityAndBasicInfo().test()
        observer.assertValueAt(0) {
            it.first && it.second == userInformation
        }

        verify(userIdentity).isVerifiedFor(Feature.TierLevel(Tier.GOLD))
        verify(userIdentity).getBasicProfileInformation()

        verifyNoMoreInteractions(userIdentity)
    }

    @Test
    fun unpairWallet() {
        val mockQueries: HistoricRateQueries = mock()

        doNothing().whenever(credentialsWiper).wipe()
        whenever(database.historicRateQueries).thenReturn(mockQueries)
        doNothing().whenever(mockQueries).clear()

        val observer = interactor.unpairWallet().test()
        observer.assertComplete()

        verify(credentialsWiper).wipe()
        verify(database.historicRateQueries).clear()
    }
}