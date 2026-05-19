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
import com.example.decisionapp.ui.theme.CalmBlue
import com.example.decisionapp.ui.theme.CalmBlueSurface
import com.example.decisionapp.ui.theme.SageGreen
import com.example.decisionapp.ui.theme.SemanticError
import com.example.decisionapp.ui.theme.SemanticSuccess
import com.example.decisionapp.ui.theme.WarmAmber
import com.example.decisionapp.ui.theme.WarmAmberSurface

// Плавный easing: быстрый старт → медленное торможение
private val EaseOutCubic = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    decisionId: Long,
    onBack: () -> Unit,
    viewModel: ResultViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Результат", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Home, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.calculate() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Пересчитать",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        when {
            state.isLoading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(color = CalmBlue, strokeWidth = 2.dp)
                    Text(
                        "Анализируем данные...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            state.error != null -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null,
                        tint = SemanticError, modifier = Modifier.size(48.dp))
                    Text(state.error ?: "Ошибка", color = SemanticError,
                        style = MaterialTheme.typography.bodyMedium)
                    OutlinedButton(
                        onClick = { viewModel.calculate() },
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Повторить") }
                }
            }

            state.result != null -> {
                val result = state.result!!
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(result.decision.title, style = MaterialTheme.typography.headlineSmall)
                        if (result.decision.description.isNotBlank()) {
                            Text(
                                result.decision.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    item { WinnerCard(result.rankedChoices.firstOrNull()) }

                    item {
                        Text("Рейтинг вариантов", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "От лучшего к наименее подходящему",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    itemsIndexed(result.rankedChoices) { index, choiceResult ->
                        RankedChoiceItem(
                            index = index,
                            choiceResult = choiceResult,
                            criteria = result.criteria
                        )
                    }

                    item { CriteriaSensitivityCard(result.criteria) }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

// ── Карточка победителя ──────────────────────────────────

@Composable
fun WinnerCard(winner: ChoiceResult?) {
    if (winner == null) return
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(CalmBlue, SageGreen)))
            .padding(20.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Иконка кубка в полупрозрачном боксе
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFFFD580), // золотой
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        "Лучший выбор",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                    Text(
                        winner.choice.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Мини-прогресс-бар победителя
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(winner.percentage / 100f)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White.copy(alpha = 0.85f))
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "Взвешенный балл: ${"%.1f".format(winner.totalScore)}  ·  " +
                        "${"%.0f".format(winner.percentage)}% от максимума",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

// ── Элемент рейтинга ─────────────────────────────────────

@Composable
fun RankedChoiceItem(index: Int, choiceResult: ChoiceResult, criteria: List<Criterion>) {
    // Анимация с задержкой — cascade-эффект
    val animatedProgress by animateFloatAsState(
        targetValue = choiceResult.percentage / 100f,
        animationSpec = tween(
            durationMillis = 900,
            delayMillis = index * 120,
            easing = EaseOutCubic
        ),
        label = "bar_$index"
    )

    // Цвета по рангу
    val cardBg = when (index) {
        0    -> WarmAmberSurface
        1    -> CalmBlueSurface
        else -> MaterialTheme.colorScheme.surface
    }
    val barColor = when (index) {
        0    -> CalmBlue
        1    -> CalmBlue
        else -> MaterialTheme.colorScheme.outline
    }
    val rankBg = when (index) {
        0    -> Color(0xFFF5EFE2)
        1    -> CalmBlueSurface
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val rankTextColor = when (index) {
        0    -> Color(0xFF8A7030)
        1    -> Color(0xFF4A7AA8)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val borderColor = when (index) {
        0    -> WarmAmber.copy(alpha = 0.4f)
        else -> MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(0.5.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(rankBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${index + 1}",
                        style = MaterialTheme.typography.labelLarge,
                        color = rankTextColor
                    )
                }
                Text(
                    choiceResult.choice.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${"%.0f".format(choiceResult.percentage)}%",
                    style = MaterialTheme.typography.titleMedium,
                    color = barColor,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(10.dp))

            // Тонкий 6dp бар (было 8dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .clip(RoundedCornerShape(3.dp))
                        .background(barColor)
                )
            }

            // Детали по критериям
            if (criteria.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                criteria.take(3).forEach { criterion ->
                    val score = choiceResult.scoresByCriterion[criterion.id] ?: 0f
                    Row(modifier = Modifier.padding(vertical = 1.dp)) {
                        Text(
                            criterion.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "${"%.0f".format(score)}/10",
                            style = MaterialTheme.typography.labelSmall,
                            color = when {
                                score >= 7f -> SemanticSuccess
                                score >= 4f -> WarmAmber
                                else        -> SemanticError
                            }
                        )
                    }
                }
                if (criteria.size > 3) {
                    Text(
                        "... и ещё ${criteria.size - 3} критерий",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ── Веса критериев ───────────────────────────────────────

@Composable
fun CriteriaSensitivityCard(criteria: List<Criterion>) {
    val total = criteria.sumOf { it.weight.toDouble() }.toFloat().coerceAtLeast(1f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Tune, contentDescription = null,
                    tint = CalmBlue, modifier = Modifier.size(18.dp))
                Text("Веса критериев", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(14.dp))

            criteria.forEach { criterion ->
                val pct = (criterion.weight / total) * 100f
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Row {
                        Text(
                            criterion.name,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "${"%.0f".format(pct)}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = CalmBlue
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(CalmBlueSurface)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(pct / 100f)
                                .clip(RoundedCornerShape(2.dp))
                                .background(CalmBlue)
                        )
                    }
                }
            }
        }
    }
}