package com.example.decisionapp.presentation.criteria

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.decisionapp.domain.model.Choice
import com.example.decisionapp.domain.model.Criterion
import com.example.decisionapp.domain.model.Score
import com.example.decisionapp.domain.usecase.GetDecisionWithDetailsUseCase
import com.example.decisionapp.domain.usecase.UpsertScoreUseCase
import com.example.decisionapp.ui.theme.DecisionBlue
import com.example.decisionapp.ui.theme.DecisionGold
import com.example.decisionapp.ui.theme.DecisionGreen
import com.example.decisionapp.ui.theme.DecisionRed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

data class ScoringState(
    val decisionTitle: String = "",
    val criteria: List<Criterion> = emptyList(),
    val choices: List<Choice> = emptyList(),
    val scores: Map<Pair<Long, Long>, Float> = emptyMap(),
    val isLoading: Boolean = true,
    val currentChoiceIndex: Int = 0
)

@HiltViewModel
class ScoringViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getDecisionWithDetailsUseCase: GetDecisionWithDetailsUseCase,
    private val upsertScoreUseCase: UpsertScoreUseCase
) : ViewModel() {

    private val decisionId: Long = checkNotNull(savedStateHandle["decisionId"])

    private val _state = MutableStateFlow(ScoringState())
    val state: StateFlow<ScoringState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val details = getDecisionWithDetailsUseCase(decisionId)
            if (details != null) {
                val existingScores = details.scores.associate {
                    (it.choiceId to it.criterionId) to it.value
                }
                _state.value = _state.value.copy(
                    decisionTitle = details.decision.title,
                    criteria = details.criteria,
                    choices = details.choices,
                    scores = existingScores,
                    isLoading = false
                )
            }
        }
    }

    fun setScore(choiceId: Long, criterionId: Long, value: Float) {
        val key = choiceId to criterionId
        _state.value = _state.value.copy(
            scores = _state.value.scores + (key to value)
        )
        viewModelScope.launch {
            upsertScoreUseCase(
                Score(choiceId = choiceId, criterionId = criterionId, value = value)
            )
        }
    }

    fun nextChoice() {
        if (_state.value.currentChoiceIndex < _state.value.choices.size - 1) {
            _state.value = _state.value.copy(currentChoiceIndex = _state.value.currentChoiceIndex + 1)
        }
    }

    fun prevChoice() {
        if (_state.value.currentChoiceIndex > 0) {
            _state.value = _state.value.copy(currentChoiceIndex = _state.value.currentChoiceIndex - 1)
        }
    }

    fun isAllScored(): Boolean {
        val state = _state.value
        return state.choices.all { choice ->
            state.criteria.all { criterion ->
                (choice.id to criterion.id) in state.scores
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoringScreen(
    decisionId: Long,
    onCalculate: () -> Unit,
    onBack: () -> Unit,
    viewModel: ScoringViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Оценка вариантов", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DecisionBlue)
            }
            return@Scaffold
        }

        if (state.choices.isEmpty() || state.criteria.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Нет вариантов или критериев", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return@Scaffold
        }

        val currentChoice = state.choices.getOrNull(state.currentChoiceIndex) ?: return@Scaffold

        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
        ) {
            // Progress bar
            Text(
                "Вариант ${state.currentChoiceIndex + 1} из ${state.choices.size}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LinearProgressIndicator(
                progress = { (state.currentChoiceIndex + 1f) / state.choices.size },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clip(RoundedCornerShape(4.dp)),
                color = DecisionBlue
            )

            Spacer(Modifier.height(12.dp))

            // Choice name
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DecisionBlue)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color.White.copy(alpha = 0.8f))
                    Spacer(Modifier.width(12.dp))
                    Text(currentChoice.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            Text("Оцените по каждому критерию (1 = плохо, 10 = отлично):", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.criteria, key = { it.id }) { criterion ->
                    val score = state.scores[currentChoice.id to criterion.id] ?: 0f
                    ScoreCriterionItem(
                        criterionName = criterion.name,
                        weight = criterion.weight,
                        score = score,
                        onScoreChange = { viewModel.setScore(currentChoice.id, criterion.id, it) }
                    )
                }
                item { Spacer(Modifier.height(8.dp)) }
            }

            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.currentChoiceIndex > 0) {
                    OutlinedButton(
                        onClick = { viewModel.prevChoice() },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                        Spacer(Modifier.width(4.dp))
                        Text("Назад")
                    }
                }

                if (state.currentChoiceIndex < state.choices.size - 1) {
                    Button(
                        onClick = { viewModel.nextChoice() },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DecisionBlue)
                    ) {
                        Text("Следующий")
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
                    }
                } else {
                    Button(
                        onClick = onCalculate,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DecisionGreen)
                    ) {
                        Icon(Icons.Default.Calculate, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Рассчитать!", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


@Composable
fun ScoreCriterionItem(
    criterionName: String,
    weight: Float,
    score: Float,
    onScoreChange: (Float) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(criterionName, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Surface(color = DecisionBlue.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                    Text(
                        "Вес: ${"%.0f".format(weight)}",
                        fontSize = 11.sp,
                        color = DecisionBlue,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                for (value in 1..10) {
                    val isSelected = score.toInt() == value
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) scoreColor(value)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { onScoreChange(value.toFloat()) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "$value",
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            if (score > 0) {
                Text(
                    scoreLabel(score.toInt()),
                    fontSize = 12.sp,
                    color = scoreColor(score.toInt()),
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

fun scoreColor(value: Int) = when {
    value >= 8 -> DecisionGreen
    value >= 5 -> DecisionGold
    else -> DecisionRed
}

fun scoreLabel(value: Int) = when {
    value >= 9 -> "Отлично"
    value >= 7 -> "Хорошо"
    value >= 5 -> "Удовлетворительно"
    value >= 3 -> "Плохо"
    else -> "Очень плохо"
}