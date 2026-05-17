package com.example.decisionapp.presentation.results

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.decisionapp.domain.model.ChoiceResult
import com.example.decisionapp.domain.model.Criterion
import com.example.decisionapp.presentation.criteria.scoreColor
import com.example.decisionapp.ui.theme.DecisionBlue
import com.example.decisionapp.ui.theme.DecisionGold
import com.example.decisionapp.ui.theme.DecisionPurple
import com.example.decisionapp.ui.theme.DecisionRed
import com.example.decisionapp.ui.theme.DecisionTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    decisionId: Long,
    onBack: () -> Unit,
    viewModel: ResultViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Результат", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.Home, null) }
                },
                actions = {
                    IconButton(onClick = { viewModel.calculate() }) {
                        Icon(Icons.Default.Refresh, "Пересчитать")
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = DecisionBlue)
                    Spacer(Modifier.height(12.dp))
                    Text("Анализируем данные...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            state.error != null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Error, null, tint = DecisionRed, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text(state.error ?: "Ошибка", color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { viewModel.calculate() }) { Text("Попробовать снова") }
                }
            }
            state.result != null -> {
                val result = state.result!!
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Decision title
                    item {
                        Text(result.decision.title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        if (result.decision.description.isNotBlank()) {
                            Text(result.decision.description, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                        }
                    }

                    // Winner card
                    item {
                        val winner = result.rankedChoices.firstOrNull()
                        if (winner != null) {
                            WinnerCard(winner)
                        }
                    }

                    // All results
                    item {
                        Text("Все варианты по рейтингу", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Ранжированы по убыванию — от лучшего к худшему",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                        )
                    }

                    itemsIndexed(result.rankedChoices) { index, choiceResult ->
                        RankedChoiceItem(index = index, choiceResult = choiceResult, criteria = result.criteria)
                    }

                    // Criteria weights
                    item {
                        CriteriaSensitivityCard(result.criteria)
                    }

                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
fun WinnerCard(winner: ChoiceResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(DecisionBlue, DecisionTeal)))
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EmojiEvents, null, tint = DecisionGold, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Лучший выбор", color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp)
                        Text(winner.choice.name, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "Итоговый балл: ${"%.1f".format(winner.totalScore)} — ${"%.0f".format(winner.percentage)}% от максимума",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun RankedChoiceItem(index: Int, choiceResult: ChoiceResult, criteria: List<Criterion>) {
    val animatedProgress by animateFloatAsState(
        targetValue = choiceResult.percentage / 100f,
        animationSpec = tween(durationMillis = 800, delayMillis = index * 100),
        label = "progress"
    )

    val barColor = when (index) {
        0 -> DecisionBlue
        1 -> DecisionTeal
        2 -> DecisionPurple
        else -> MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (index == 0) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(if (index == 0) 4.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Rank badge
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (index == 0) DecisionGold else MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${index + 1}",
                        fontWeight = FontWeight.Bold,
                        color = if (index == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.width(12.dp))
                Text(choiceResult.choice.name, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Text(
                    "${"%.0f".format(choiceResult.percentage)}%",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = barColor
                )
            }

            Spacer(Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = barColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // Score details per criterion
            if (criteria.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                criteria.take(3).forEach { criterion ->
                    val score = choiceResult.scoresByCriterion[criterion.id] ?: 0f
                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                        Text(
                            criterion.name,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "%.0f/10".format(score),
                            fontSize = 12.sp,
                            color = scoreColor(score.toInt()),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                if (criteria.size > 3) {
                    Text(
                        "... и ещё ${criteria.size - 3} критерий",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun CriteriaSensitivityCard(criteria: List<Criterion>) {
    val totalWeight = criteria.sumOf { it.weight.toDouble() }.toFloat().takeIf { it > 0f } ?: 1f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Tune, null, tint = DecisionBlue)
                Spacer(Modifier.width(8.dp))
                Text("Веса критериев", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }

            Spacer(Modifier.height(12.dp))

            criteria.forEach { criterion ->
                val pct = (criterion.weight / totalWeight) * 100f
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Row {
                        Text(criterion.name, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        Text("${"%.0f".format(pct)}%", fontSize = 13.sp, color = DecisionBlue, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { pct / 100f },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = DecisionBlue,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}