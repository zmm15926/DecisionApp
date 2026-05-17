package com.example.decisionapp.presentation.decisions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.decisionapp.domain.model.Decision
import com.example.decisionapp.domain.model.User
import com.example.decisionapp.domain.usecase.DeleteDecisionUseCase
import com.example.decisionapp.domain.usecase.GetCurrentUserUseCase
import com.example.decisionapp.domain.usecase.GetDecisionsUseCase
import com.example.decisionapp.domain.usecase.LogoutUseCase
import com.example.decisionapp.ui.theme.DecisionBlue
import com.example.decisionapp.ui.theme.DecisionRed
import com.example.decisionapp.ui.theme.DecisionTeal
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
            try {
                deleteDecisionUseCase(id)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun logout() {
        viewModelScope.launch { logoutUseCase() }
    }
}

// -------------------------------------------------------


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
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Мои Решения", fontWeight = FontWeight.Bold)
                        state.user?.let {
                            Text(it.username, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, "Выйти")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateDecision,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Новое решение") },
                containerColor = DecisionBlue,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DecisionBlue)
            }
        } else if (state.decisions.isEmpty()) {
            EmptyDecisionsPlaceholder(
                modifier = Modifier.fillMaxSize().padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Выйти из аккаунта?") },
            text = { Text("Все ваши решения сохранены и будут доступны после входа.") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    viewModel.logout()
                    onLogout()
                }) { Text("Выйти", color = DecisionRed) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Отмена") }
            }
        )
    }

    decisionToDelete?.let { decision ->
        AlertDialog(
            onDismissRequest = { decisionToDelete = null },
            title = { Text("Удалить решение?") },
            text = { Text("«${decision.title}» будет удалено вместе со всеми критериями и вариантами.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteDecision(decision.id)
                    decisionToDelete = null
                }) { Text("Удалить", color = DecisionRed) }
            },
            dismissButton = {
                TextButton(onClick = { decisionToDelete = null }) { Text("Отмена") }
            }
        )
    }
}

@Composable
fun DecisionCard(decision: Decision, onClick: () -> Unit, onDelete: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale("ru")) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(DecisionBlue, DecisionTeal))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Balance, null, tint = MaterialTheme.colorScheme.onPrimary)
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    decision.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (decision.description.isNotBlank()) {
                    Text(
                        decision.description,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Text(
                    dateFormat.format(Date(decision.updatedAt)),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Row {
                Icon(
                    Icons.Default.ChevronRight,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.DeleteOutline, null, tint = DecisionRed, modifier = Modifier.size(20.dp))
                }
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
        Icon(
            Icons.Default.LightbulbCircle,
            null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(Modifier.height(16.dp))
        Text("Нет решений", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Text(
            "Нажмите «+» чтобы добавить\nпервое решение",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}