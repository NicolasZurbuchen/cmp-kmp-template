package io.nicolaszurbuchen.appname.feature.numbergenerator.presentation.screen.generate

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import io.nicolaszurbuchen.appname.common.error.AppError
import io.nicolaszurbuchen.appname.common.error.AppException
import io.nicolaszurbuchen.appname.feature.numbergenerator.domain.usecase.GenerateNumberUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

interface GenerateStore : Store<GenerateIntent, GenerateState, GenerateLabel>

class GenerateStoreFactory(
    private val storeFactory: StoreFactory,
    private val generateNumber: GenerateNumberUseCase,
) {
    fun create(): GenerateStore =
        object :
            GenerateStore,
            Store<GenerateIntent, GenerateState, GenerateLabel> by storeFactory.create(
                name = "GenerateStore",
                initialState = GenerateState(),
                executorFactory = { ExecutorImpl() },
                reducer = GenerateReducer,
            ) {}

    private inner class ExecutorImpl : CoroutineExecutor<GenerateIntent, GenerateAction, GenerateState, GenerateMessage, GenerateLabel>() {
        override fun executeIntent(intent: GenerateIntent) {
            when (intent) {
                GenerateIntent.GenerateClicked -> performGenerate()
            }
        }

        private fun performGenerate() {
            dispatch(GenerateMessage.GenerationStarted)
            scope.launch {
                try {
                    val number = generateNumber()
                    if (number.fact == null && !number.isSynced) {
                        dispatch(GenerateMessage.GenerationPartiallyFailed(number))
                    } else {
                        dispatch(GenerateMessage.GenerationSucceeded(number))
                    }
                } catch (e: AppException) {
                    dispatch(GenerateMessage.GenerationFailed(e.error))
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    dispatch(GenerateMessage.GenerationFailed(AppError.Unexpected(e)))
                }
            }
        }
    }
}
