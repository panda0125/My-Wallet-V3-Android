package com.blockchain.nabu.datamanagers.analytics

import com.blockchain.notifications.analytics.Analytics
import com.blockchain.notifications.analytics.AnalyticsEvent
import com.blockchain.operations.AppStartUpFlushable
import com.blockchain.utils.toUtcIso8601
import info.blockchain.api.AnalyticsService
import info.blockchain.api.NabuAnalyticsEvent
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import piuk.blockchain.androidcore.utils.PersistentPrefs
import piuk.blockchain.androidcore.utils.extensions.emptySubscribe
import piuk.blockchain.androidcore.utils.extensions.then
import java.util.Date

class NabuAnalytics(
    private val analyticsService: AnalyticsService,
    private val prefs: Lazy<PersistentPrefs>,
    private val localAnalyticsPersistence: AnalyticsLocalPersistence
) : Analytics, AppStartUpFlushable {
    private val compositeDisposable = CompositeDisposable()

    private val id: String by lazy {
        prefs.value.deviceId
    }

    override fun logEvent(analyticsEvent: AnalyticsEvent) {
        val nabuEvent = analyticsEvent.toNabuAnalyticsEvent()
        // TODO log failure
        compositeDisposable += localAnalyticsPersistence.save(nabuEvent)
            .subscribeOn(Schedulers.computation())
            .onErrorComplete()
            .then {
                sendToApiAndFlushIfNeeded()
            }
            .emptySubscribe()
    }

    private fun sendToApiAndFlushIfNeeded(): Completable {
        return localAnalyticsPersistence.size().flatMapCompletable {
            if (it >= BATCH_SIZE) {
                batchToApiAndFlush()
            } else {
                Completable.complete()
            }
        }
    }

    override val tag: String
        get() = "nabu_analytics_flush"

    override fun flush(): Completable {
        return localAnalyticsPersistence.getAllItems().flatMapCompletable { events ->
            // Whats happening here is that we split the retrieved items into sublists of size = BATCH_SIZE
            // and then each one of these sublists is converted to the corresponding completable that actually is the
            // api request.

            val listOfSublists = mutableListOf<List<NabuAnalyticsEvent>>()
            for (i in events.indices step BATCH_SIZE) {
                listOfSublists.add(
                    events.subList(i, (i + BATCH_SIZE).coerceAtMost(events.size))
                )
            }

            val completables = listOfSublists.map { list ->
                analyticsService.postEvents(list, id).then {
                    localAnalyticsPersistence.removeOldestItems(list.size)
                }
            }
            Completable.concat(completables)
        }
    }

    private fun batchToApiAndFlush(): Completable {
        return localAnalyticsPersistence.getOldestItems(BATCH_SIZE).flatMapCompletable {
            analyticsService.postEvents(it, id)
        }.then {
            localAnalyticsPersistence.removeOldestItems(BATCH_SIZE)
        }
    }

    override fun logEventOnce(analyticsEvent: AnalyticsEvent) {}

    override fun logEventOnceForSession(analyticsEvent: AnalyticsEvent) {}

    companion object {
        private const val BATCH_SIZE = 10
    }
}

private fun AnalyticsEvent.toNabuAnalyticsEvent(): NabuAnalyticsEvent =
    NabuAnalyticsEvent(
        name = this.event,
        type = "EVENT",
        originalTimestamp = Date().toUtcIso8601(),
        properties = this.params
    )