package io.nicolaszurbuchen.appname.feature.numbergenerator.di

import io.nicolaszurbuchen.appname.cache.AppDatabase
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.local.GeneratedNumberLocalDataSource
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.local.GeneratedNumberLocalDataSourceImpl
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.NumberFactRemoteDataSource
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.NumberFactRemoteDataSourceImpl
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.RandomNumberRemoteDataSource
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.RandomNumberRemoteDataSourceImpl
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.api.NumberFactApi
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.api.NumberFactApiImpl
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.api.RandomNumberApi
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.datasource.remote.api.RandomNumberApiImpl
import io.nicolaszurbuchen.appname.feature.numbergenerator.data.repository.NumberGeneratorRepositoryImpl
import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.repository.NumberGeneratorRepository
import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.usecase.GenerateNumberUseCase
import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.usecase.ObserveHistoryUseCase
import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.usecase.SyncPendingUseCase
import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.usecase.ToggleFavoriteUseCase
import io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.generate.GenerateStoreFactory
import io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.generate.GenerateViewModel
import io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.history.HistoryStoreFactory
import io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.history.HistoryViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val numberGeneratorModule =
    module {
        single { get<AppDatabase>().generatedNumberQueries }

        singleOf(::RandomNumberApiImpl) bind RandomNumberApi::class
        singleOf(::NumberFactApiImpl) bind NumberFactApi::class

        singleOf(::RandomNumberRemoteDataSourceImpl) bind RandomNumberRemoteDataSource::class
        singleOf(::NumberFactRemoteDataSourceImpl) bind NumberFactRemoteDataSource::class
        singleOf(::GeneratedNumberLocalDataSourceImpl) bind GeneratedNumberLocalDataSource::class

        singleOf(::NumberGeneratorRepositoryImpl) bind NumberGeneratorRepository::class

        factoryOf(::GenerateNumberUseCase)
        factoryOf(::ObserveHistoryUseCase)
        factoryOf(::ToggleFavoriteUseCase)
        factoryOf(::SyncPendingUseCase)

        factoryOf(::GenerateStoreFactory)
        viewModelOf(::GenerateViewModel)

        factoryOf(::HistoryStoreFactory)
        viewModelOf(::HistoryViewModel)
    }
