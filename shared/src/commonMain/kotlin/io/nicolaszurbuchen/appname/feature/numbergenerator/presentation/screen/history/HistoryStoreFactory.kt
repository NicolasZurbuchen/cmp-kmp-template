package io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.history

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import io.nicolaszurbuchen.appname.common.error.AppError
import io.nicolaszurbuchen.appname.common.error.AppException
import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.usecase.ObserveHistoryUseCase
import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.usecase.SyncPendingUseCase
import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

interface HistoryStore : Store<HistoryIntent, HistoryState, HistoryLabel>

class HistoryStoreFactory(
    private val storeFactory: StoreFactory,
    private val observeHistory: ObserveHistoryUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
    private val syncPending: SyncPendingUseCase,
) {
    fun create(): HistoryStore =
        object :
            HistoryStore,
            Store<HistoryIntent, HistoryState, HistoryLabel> by storeFactory.create(
                name = "HistoryStore",
                initialState = HistoryState(),
                bootstrapper = BootstrapperImpl(),
                executorFactory = { ExecutorImpl() },
                reducer = HistoryReducer,
            ) {}

    private class BootstrapperImpl : CoroutineBootstrapper<HistoryAction>() {
        override fun invoke() {
            dispatch(HistoryAction.ObserveHistory)
        }
    }

    private inner class ExecutorImpl : CoroutineExecutor<HistoryIntent, HistoryAction, HistoryState, HistoryMessage, HistoryLabel>() {
        override fun executeAction(action: HistoryAction) {
            when (action) {
                HistoryAction.ObserveHistory -> {
                    scope.launch {
                        observeHistory().collect { items ->
                            dispatch(HistoryMessage.HistoryUpdated(items))
                        }
                    }
                }
            }
        }

        override fun executeIntent(intent: HistoryIntent) {
            when (intent) {
                is HistoryIntent.ToggleFavorite -> {
                    scope.launch { toggleFavorite(intent.id) }
                }

                HistoryIntent.SyncNowClicked -> {
                    performSync()
                }
            }
        }

        private fun performSync() {
            dispatch(HistoryMessage.SyncStarted)
            scope.launch {
                try {
                    syncPending()
                    dispatch(HistoryMessage.SyncFinished(error = null))
                } catch (e: AppException) {
                    dispatch(HistoryMessage.SyncFinished(error = e.error))
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    dispatch(HistoryMessage.SyncFinished(error = AppError.Unexpected(e)))
                }
            }
        }
    }
}
