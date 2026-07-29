package io.nicolaszurbuchen.appname.feature.pokemonexplorer.di

import io.nicolaszurbuchen.appname.cache.AppDatabase
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.data.datasource.local.PokemonLocalDataSource
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.data.datasource.local.PokemonLocalDataSourceImpl
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.data.datasource.remote.PokemonRemoteDataSource
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.data.datasource.remote.PokemonRemoteDataSourceImpl
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.data.datasource.remote.api.PokemonApi
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.data.datasource.remote.api.PokemonApiImpl
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.data.repository.PokemonExplorerRepositoryImpl
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.repository.PokemonExplorerRepository
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.usecase.ClearHistoryUseCase
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.usecase.GetPokemonByIdUseCase
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.usecase.GetRandomPokemonUseCase
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.domain.usecase.ObserveHistoryUseCase
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.screen.detail.DetailStoreFactory
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.screen.detail.DetailViewModel
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.screen.main.MainStoreFactory
import io.nicolaszurbuchen.appname.feature.pokemonexplorer.presentation.screen.main.MainViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val pokemonExplorerModule =
    module {
        single { get<AppDatabase>().cachedPokemonQueries }

        singleOf(::PokemonApiImpl) bind PokemonApi::class
        singleOf(::PokemonRemoteDataSourceImpl) bind PokemonRemoteDataSource::class
        singleOf(::PokemonLocalDataSourceImpl) bind PokemonLocalDataSource::class

        single<PokemonExplorerRepository> { PokemonExplorerRepositoryImpl(get(), get()) }

        factoryOf(::GetRandomPokemonUseCase)
        factoryOf(::ObserveHistoryUseCase)
        factoryOf(::ClearHistoryUseCase)
        factoryOf(::GetPokemonByIdUseCase)

        factoryOf(::MainStoreFactory)
        viewModelOf(::MainViewModel)

        viewModel { (historyId: Long) -> DetailViewModel(DetailStoreFactory(get(), get(), historyId)) }
    }
