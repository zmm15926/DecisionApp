package com.example.decisionapp.presentation.results

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.decisionapp.domain.model.DecisionResult
import com.example.decisionapp.domain.usecase.CalculateDecisionResultUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ResultState(
    val result: DecisionResult? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val calculateDecisionResultUseCase: CalculateDecisionResultUseCase
) : ViewModel() {

    private val decisionId: Long = checkNotNull(savedStateHandle["decisionId"])

    private val _state = MutableStateFlow(ResultState())
    val state: StateFlow<ResultState> = _state.asStateFlow()

    init {
        calculate()
    }

    fun calculate() {
        viewModelScope.launch {
            android.util.Log.d("ResultVM", "calculate() started, decisionId=$decisionId")
            _state.value = ResultState(isLoading = true)
            try {
                android.util.Log.d("ResultVM", "calling useCase...")
                val result = calculateDecisionResultUseCase(decisionId)
                android.util.Log.d("ResultVM", "useCase returned: $result")
                _state.value = result.fold(
                    onSuccess = { ResultState(result = it) },
                    onFailure = {
                        android.util.Log.e("ResultVM", "failure: ${it.message}", it)
                        ResultState(error = it.message ?: "Ошибка")
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("ResultVM", "exception: ${e.message}", e)
                _state.value = ResultState(error = e.message ?: "Ошибка")
            }
        }
    }
}
