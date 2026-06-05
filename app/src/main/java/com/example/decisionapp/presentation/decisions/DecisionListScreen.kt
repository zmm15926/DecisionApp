package com.example.decisionapp.presentation.decisions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.decisionapp.domain.model.Decision
import com.example.decisionapp.domain.model.User
import com.example.decisionapp.domain.usecase.DeleteDecisionUseCase
import com.example.decisionapp.domain.usecase.GetCurrentUserUseCase
import com.example.decisionapp.domain.usecase.GetDecisionsUseCase
import com.example.decisionapp.domain.usecase.LogoutUseCase
import com.example.decisionapp.ui.theme.CalmBlue
import com.example.decisionapp.ui.theme.CalmBlueSurface
import com.example.decisionapp.ui.theme.SageGreen
import com.example.decisionapp.ui.theme.SageGreenSurface
import com.example.decisionapp.ui.theme.SemanticError
import com.example.decisionapp.ui.theme.WarmAmber
import com.example.decisionapp.ui.theme.WarmAmberSurface
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.*

data class DecisionListState(
    val decisions: List<Decision> = emptyList(),
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DecisionListViewModel @Inject constructor(
    private val getDecisionsUseCase: GetDecisionsUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val deleteDecisionUseCase: DeleteDecisionUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DecisionListState(isLoading = true))
    val state: StateFlow<DecisionListState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { user ->
                _state.value = _state.value.copy(user = user)
                if (user != null) {
                    getDecisionsUseCase(user.id).collect { decisions ->
                        _state.value = _state.value.copy(decisions = decisions, isLoading = false)
                    }
                }
            }
        }
    }

    fun deleteDecision(id: Long) {
        viewModelScope.launch {
            try { deleteDecisionUseCase(id) }
            catch (e: Exception) { _state.value = _state.value.copy(error = e.message) }
        }
    }

    fun logout() = viewModelScope.launch { logoutUseCase() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecisionListScreen(
    onCreateDecision: () -> Unit,
    onOpenDecision: (Long) -> Unit,
    onLogout: () -> Unit,
    viewModel: DecisionListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var decisionToDelete by remember { mutableStateOf<Decision?>(null) }

    LaunchedEffect(state.user) {
        if (state.user == null && !state.isLoading) onLogout()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Мои решения", style = MaterialTheme.typography.titleLarge)
                        state.user?.let {
                            Text(
                                it.username,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Аватар-круг с градиентом CalmBlue → SageGreen
                    state.user?.let { user ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(CalmBlue, SageGreen))
                                )
                                .clickable { showLogoutDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                user.username.first().uppercaseChar().toString(),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            // Компактный квадратный FAB
            FloatingActionButton(
                onClick = onCreateDecision,
                containerColor = CalmBlue,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(24.dp))
            }
        }
    ) { padding ->
        when {
            state.isLoading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = CalmBlue, strokeWidth = 2.dp)
            }

            state.decisions.isEmpty() -> EmptyDecisionsPlaceholder(
                Modifier.fillMaxSize().padding(padding)
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    StatsRow(count = state.decisions.size)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Все решения",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(4.dp))
                }
                items(state.decisions, key = { it.id }) { decision ->
                    DecisionCard(
                        decision = decision,
                        onClick = { onOpenDecision(decision.id) },
                        onDelete = { decisionToDelete = decision }
                    )
                }
                item { Spacer(Modifier.height(72.dp)) }
            }
        }
    }

    if (showLogoutDialog) {
        CalmDialog(
            title = "Выйти из аккаунта?",
            text = "Все ваши решения сохранены и будут доступны при следующем входе.",
            confirmText = "Выйти",
            onConfirm = { showLogoutDialog = false; viewModel.logout(); onLogout() },
            onDismiss = { showLogoutDialog = false },
            isDestructive = true
        )
    }

    decisionToDelete?.let { d ->
        CalmDialog(
            title = "Удалить решение?",
            text = "«${d.title}» будет удалено вместе со всеми данными.",
            confirmText = "Удалить",
            onConfirm = { viewModel.deleteDecision(d.id); decisionToDelete = null },
            onDismiss = { decisionToDelete = null },
            isDestructive = true
        )
    }
}

@Composable
fun StatsRow(count: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatMiniCard(value = count.toString(), label = "Всего", color = CalmBlue, modifier = Modifier.weight(1f))
        StatMiniCard(value = count.toString(), label = "Сохранено", color = SageGreen, modifier = Modifier.weight(1f))
    }
}

@Composable
fun StatMiniCard(
    value: String,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Medium, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun DecisionCard(decision: Decision, onClick: () -> Unit, onDelete: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("d MMM yyyy", Locale("ru")) }

    val iconColors = listOf(
        CalmBlueSurface  to CalmBlue,
        SageGreenSurface to SageGreen,
        WarmAmberSurface to WarmAmber
    )
    val (bgColor, iconColor) = iconColors[(decision.id % iconColors.size).toInt()]

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Balance, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    decision.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (decision.description.isNotBlank()) {
                    Text(
                        decision.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Text(
                    dateFormat.format(Date(decision.updatedAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = null,
                        tint = SemanticError.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyDecisionsPlaceholder(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(CalmBlueSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.LightbulbCircle,
                contentDescription = null,
                modifier = Modifier.size(44.dp),
                tint = CalmBlue
            )
        }
        Spacer(Modifier.height(20.dp))
        Text("Нет решений", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(
            "Нажмите + чтобы добавить\nпервое решение",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CalmDialog(
    title: String,
    text: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDestructive: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = {
            Text(
                text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    confirmText,
                    color = if (isDestructive) SemanticError else CalmBlue,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", style = MaterialTheme.typography.labelLarge)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}