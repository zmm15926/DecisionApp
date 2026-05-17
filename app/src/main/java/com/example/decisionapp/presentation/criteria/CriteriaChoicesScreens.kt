package com.example.decisionapp.presentation.criteria

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.decisionapp.domain.model.Choice
import com.example.decisionapp.domain.model.Criterion
import com.example.decisionapp.domain.usecase.DeleteChoiceUseCase
import com.example.decisionapp.domain.usecase.DeleteCriterionUseCase
import com.example.decisionapp.domain.usecase.GetChoicesUseCase
import com.example.decisionapp.domain.usecase.GetCriteriaUseCase
import com.example.decisionapp.domain.usecase.GetDecisionWithDetailsUseCase
import com.example.decisionapp.domain.usecase.SaveChoiceUseCase
import com.example.decisionapp.domain.usecase.SaveCriterionUseCase
import com.example.decisionapp.ui.theme.DecisionBlue
import com.example.decisionapp.ui.theme.DecisionGold
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

// ========== CRITERIA VIEWMODEL ==========

data class CriteriaState(
    val decisionId: Long = 0,
    val decisionTitle: String = "",
    val criteria: List<Criterion> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class CriteriaViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCriteriaUseCase: GetCriteriaUseCase,
    private val saveCriterionUseCase: SaveCriterionUseCase,
    private val deleteCriterionUseCase: DeleteCriterionUseCase,
    private val getDecisionWithDetailsUseCase: GetDecisionWithDetailsUseCase
) : ViewModel() {

    private val decisionId: Long = checkNotNull(savedStateHandle["decisionId"])

    private val _state = MutableStateFlow(CriteriaState(decisionId = decisionId))
    val state: StateFlow<CriteriaState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val details = getDecisionWithDetailsUseCase(decisionId)
            _state.value = _state.value.copy(decisionTitle = details?.decision?.title ?: "")
        }
        viewModelScope.launch {
            getCriteriaUseCase(decisionId).collect { criteria ->
                _state.value = _state.value.copy(criteria = criteria, isLoading = false)
            }
        }
    }

    fun addCriterion(name: String, weight: Float = 1f) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val criterion = Criterion(
                decisionId = decisionId,
                name = name.trim(),
                weight = weight,
                orderIndex = _state.value.criteria.size
            )
            saveCriterionUseCase(criterion)
        }
    }

    fun updateWeight(criterion: Criterion, weight: Float) {
        viewModelScope.launch {
            saveCriterionUseCase(criterion.copy(weight = weight))
        }
    }

    fun deleteCriterion(id: Long) {
        viewModelScope.launch { deleteCriterionUseCase(id) }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}

// ========== CHOICES VIEWMODEL ==========

data class ChoicesState(
    val decisionId: Long = 0,
    val decisionTitle: String = "",
    val choices: List<Choice> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ChoicesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getChoicesUseCase: GetChoicesUseCase,
    private val saveChoiceUseCase: SaveChoiceUseCase,
    private val deleteChoiceUseCase: DeleteChoiceUseCase,
    private val getDecisionWithDetailsUseCase: GetDecisionWithDetailsUseCase
) : ViewModel() {

    private val decisionId: Long = checkNotNull(savedStateHandle["decisionId"])

    private val _state = MutableStateFlow(ChoicesState(decisionId = decisionId))
    val state: StateFlow<ChoicesState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val details = getDecisionWithDetailsUseCase(decisionId)
            _state.value = _state.value.copy(decisionTitle = details?.decision?.title ?: "")
        }
        viewModelScope.launch {
            getChoicesUseCase(decisionId).collect { choices ->
                _state.value = _state.value.copy(choices = choices, isLoading = false)
            }
        }
    }

    fun addChoice(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val choice = Choice(
                decisionId = decisionId,
                name = name.trim(),
                orderIndex = _state.value.choices.size
            )
            saveChoiceUseCase(choice)
        }
    }

    fun deleteChoice(id: Long) {
        viewModelScope.launch { deleteChoiceUseCase(id) }
    }
}

// ========== CRITERIA SCREEN ==========



val SUGGESTED_CRITERIA = listOf(
    "Стоимость", "Качество", "Удобство", "Престиж", "Карьерный рост",
    "Баланс жизни", "Доход", "Семья", "Ментальное здоровье", "Риск"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CriteriaScreen(
    decisionId: Long,
    onNext: () -> Unit,
    onBack: () -> Unit,
    viewModel: CriteriaViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var newCriterionName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Критерии", fontWeight = FontWeight.Bold)
                        state.decisionTitle.takeIf { it.isNotBlank() }?.let {
                            Text(it, fontSize = 13.sp)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = onNext,
                    enabled = state.criteria.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DecisionBlue)
                ) {
                    Text("Далее: Варианты", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text("Какие критерии важны?", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Text("Добавьте критерии и настройте их вес (1-5)", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp, bottom = 12.dp))

                // Input
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newCriterionName,
                        onValueChange = { newCriterionName = it },
                        placeholder = { Text("Название критерия") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    FilledIconButton(
                        onClick = {
                            viewModel.addCriterion(newCriterionName)
                            newCriterionName = ""
                        },
                        enabled = newCriterionName.isNotBlank(),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = DecisionBlue)
                    ) { Icon(Icons.Default.Add, null) }
                }

                Spacer(Modifier.height(12.dp))

                // Suggestions
                Text("или выберите:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SUGGESTED_CRITERIA.filter { suggestion ->
                        state.criteria.none { it.name.equals(suggestion, ignoreCase = true) }
                    }.forEach { suggestion ->
                        SuggestionChip(
                            onClick = { viewModel.addCriterion(suggestion) },
                            label = { Text(suggestion, fontSize = 13.sp) }
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            if (state.criteria.isNotEmpty()) {
                item {
                    Text("Добавлено (${state.criteria.size}):", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                }
                items(state.criteria, key = { it.id }) { criterion ->
                    CriterionItem(
                        criterion = criterion,
                        onWeightChange = { viewModel.updateWeight(criterion, it) },
                        onDelete = { viewModel.deleteCriterion(criterion.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun CriterionItem(criterion: Criterion, onWeightChange: (Float) -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, null, tint = DecisionGold, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(criterion.name, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Вес:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(8.dp))
                Slider(
                    value = criterion.weight,
                    onValueChange = onWeightChange,
                    valueRange = 1f..5f,
                    steps = 3,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(thumbColor = DecisionBlue, activeTrackColor = DecisionBlue)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "%.0f".format(criterion.weight),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DecisionBlue,
                    modifier = Modifier.width(20.dp)
                )
            }
        }
    }
}

// ========== CHOICES SCREEN ==========

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChoicesScreen(
    decisionId: Long,
    onNext: () -> Unit,
    onBack: () -> Unit,
    viewModel: ChoicesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var newChoiceName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Варианты", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = onNext,
                    enabled = state.choices.size >= 2,
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DecisionBlue)
                ) {
                    Text("Далее: Оценки", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text("Какие есть варианты?", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Text("Добавьте минимум 2 варианта для сравнения", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp, bottom = 12.dp))

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newChoiceName,
                        onValueChange = { newChoiceName = it },
                        placeholder = { Text("Название варианта") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    FilledIconButton(
                        onClick = {
                            viewModel.addChoice(newChoiceName)
                            newChoiceName = ""
                        },
                        enabled = newChoiceName.isNotBlank(),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = DecisionBlue)
                    ) { Icon(Icons.Default.Add, null) }
                }

                if (state.choices.size < 2) {
                    Text(
                        "Нужно добавить ещё ${2 - state.choices.size} вариант(а)",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            items(state.choices, key = { it.id }) { choice ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${choice.orderIndex + 1}", fontWeight = FontWeight.Bold, color = DecisionBlue)
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(choice.name, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                        IconButton(onClick = { viewModel.deleteChoice(choice.id) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}