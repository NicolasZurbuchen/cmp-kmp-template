package io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.screen.main

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import io.nicolaszurbuchen.appname.common.error.AppError
import io.nicolaszurbuchen.appname.common.error.AppException
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.usecase.ClearHistoryUseCase
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.usecase.GetRandomPokemonUseCase
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.usecase.ObserveHistoryUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

interface MainStore : Store<MainIntent, MainState, MainLabel>

class MainStoreFactory(
    private val storeFactory: StoreFactory,
    private val getRandomPokemon: GetRandomPokemonUseCase,
    private val observeHistory: ObserveHistoryUseCase,
    private val clearHistory: ClearHistoryUseCase,
) {
    fun create(): MainStore =
        object :
            MainStore,
            Store<MainIntent, MainState, MainLabel> by storeFactory.create(
                name = "MainStore",
                initialState = MainState(isLoading = true),
                bootstrapper = BootstrapperImpl(),
                executorFactory = { ExecutorImpl() },
                reducer = ReducerImpl,
            ) {}

    private class BootstrapperImpl : CoroutineBootstrapper<MainAction>() {
        override fun invoke() {
            dispatch(MainAction.ObserveHistory)
        }
    }

    private inner class ExecutorImpl : CoroutineExecutor<MainIntent, MainAction, MainState, MainMessage, MainLabel>() {
        private var generateJob: Job? = null

        override fun executeAction(action: MainAction) {
            when (action) {
                MainAction.ObserveHistory -> observeHistoryChanges()
            }
        }

        override fun executeIntent(intent: MainIntent) {
            when (intent) {
                MainIntent.GenerateClicked, MainIntent.RetryClicked -> generate()
                is MainIntent.ItemClicked -> publish(MainLabel.NavigateToDetail(intent.historyId))
                MainIntent.ClearClicked -> scope.launch { clearHistory() }
                MainIntent.DismissErrorClicked -> dispatch(MainMessage.ErrorDismissed)
            }
        }

        private fun observeHistoryChanges() {
            scope.launch {
                observeHistory().collect { items ->
                    dispatch(MainMessage.HistoryUpdated(items))
                }
            }
        }

        private fun generate() {
            generateJob?.cancel()
            dispatch(MainMessage.GenerationStarted)
            generateJob =
                scope.launch {
                    try {
                        getRandomPokemon()
                    } catch (e: AppException) {
                        dispatch(MainMessage.GenerationFailed(e.error))
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        dispatch(MainMessage.GenerationFailed(AppError.Unexpected(e)))
                    }
                }
        }
    }

    // internal (not private) so MainReducerTest can exercise it directly
    internal object ReducerImpl : Reducer<MainState, MainMessage> {
        override fun MainState.reduce(msg: MainMessage): MainState =
            when (msg) {
                MainMessage.GenerationStarted -> copy(isLoading = true, error = null)
                is MainMessage.HistoryUpdated -> copy(isLoading = false, history = msg.items)
                is MainMessage.GenerationFailed -> copy(isLoading = false, error = msg.error)
                MainMessage.ErrorDismissed -> copy(error = null)
            }
    }
}
