package io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.screen.detail

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.usecase.GetPokemonByIdUseCase
import kotlinx.coroutines.launch

interface DetailStore : Store<DetailIntent, DetailState, DetailLabel>

class DetailStoreFactory(
    private val storeFactory: StoreFactory,
    private val getPokemonById: GetPokemonByIdUseCase,
    private val historyId: Long,
) {
    fun create(): DetailStore =
        object :
            DetailStore,
            Store<DetailIntent, DetailState, DetailLabel> by storeFactory.create(
                name = "DetailStore",
                initialState = DetailState(),
                bootstrapper = BootstrapperImpl(),
                executorFactory = { ExecutorImpl() },
                reducer = ReducerImpl,
            ) {}

    private inner class BootstrapperImpl : CoroutineBootstrapper<DetailAction>() {
        override fun invoke() {
            dispatch(DetailAction.LoadPokemon)
        }
    }

    private inner class ExecutorImpl : CoroutineExecutor<DetailIntent, DetailAction, DetailState, DetailMessage, DetailLabel>() {
        override fun executeAction(action: DetailAction) {
            when (action) {
                DetailAction.LoadPokemon -> loadPokemon()
            }
        }

        override fun executeIntent(intent: DetailIntent) {
            // No user intents on this screen; the store only ever reacts to the initial load action.
        }

        private fun loadPokemon() {
            scope.launch {
                // Intentionally a one-shot read, not Flow-observed: if the history is cleared
                // (via Main's "Clear list") while this screen is open, the already-loaded
                // Pokemon below simply goes stale instead of reacting live. That's an accepted
                // tradeoff for this template, not an oversight.
                val pokemon = getPokemonById(historyId)
                dispatch(DetailMessage.PokemonLoaded(pokemon))
            }
        }
    }

    // internal (not private) so DetailReducerTest can exercise it directly
    internal object ReducerImpl : Reducer<DetailState, DetailMessage> {
        override fun DetailState.reduce(msg: DetailMessage): DetailState =
            when (msg) {
                is DetailMessage.PokemonLoaded -> copy(isLoading = false, pokemon = msg.pokemon)
            }
    }
}
