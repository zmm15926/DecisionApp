package com.example.decisionapp.presentation.decisions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.decisionapp.domain.usecase.CreateDecisionUseCase
import com.example.decisionapp.domain.usecase.GetCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.decisionapp.ui.theme.DecisionBlue

data class CreateDecisionState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val createdId: Long? = null
)

@HiltViewModel
class CreateDecisionViewModel @Inject constructor(
    private val createDecisionUseCase: CreateDecisionUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CreateDecisionState())
    val state: StateFlow<CreateDecisionState> = _state.asStateFlow()

    fun createDecision(title: String, description: String) {
        viewModelScope.launch {
            val user = getCurrentUserUseCase().first() ?: run {
                _state.value = _state.value.copy(error = "Пользователь не найден")
                return@launch
            }
            _state.value = _state.value.copy(isLoading = true, error = null)
            val result = createDecisionUseCase(user.id, title, description)
            _state.value = result.fold(
                onSuccess = { CreateDecisionState(createdId = it) },
                onFailure = { CreateDecisionState(error = it.message) }
            )
        }
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDecisionScreen(
    onDecisionCreated: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: CreateDecisionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    LaunchedEffect(state.createdId) {
        state.createdId?.let { onDecisionCreated(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Новое решение", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                "Что вам нужно решить?",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Название решения *") },
                placeholder = { Text("Например: Выбор работы") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                leadingIcon = { Icon(Icons.Default.Title, null) },
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Описание (необязательно)") },
                placeholder = { Text("Дополнительный контекст...") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(14.dp),
                maxLines = 4
            )

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = { viewModel.createDecision(title, description) },
                enabled = title.isNotBlank() && !state.isLoading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DecisionBlue)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Далее: Критерии", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}