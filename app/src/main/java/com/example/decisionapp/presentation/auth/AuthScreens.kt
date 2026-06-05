package com.example.decisionapp.presentation.auth

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.decisionapp.ui.theme.CalmBlue
import com.example.decisionapp.ui.theme.CalmBlueSurface
import com.example.decisionapp.ui.theme.SageGreen
import com.example.decisionapp.ui.theme.SemanticError

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.success) { if (uiState.success) onLoginSuccess() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Декоративный туманный круг — почти незаметен (alpha 0.08)
        Box(
            modifier = Modifier
                .size(280.dp)
                .offset(x = 80.dp, y = (-80).dp)
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.radialGradient(
                        listOf(CalmBlue.copy(alpha = 0.08f), CalmBlue.copy(alpha = 0f))
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .padding(top = 72.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Логотип — диагональный градиент CalmBlue → SageGreen
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(CalmBlue, SageGreen),
                            start = Offset(0f, 0f),
                            end = Offset(72f, 72f)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Balance,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(Modifier.height(20.dp))
            Text(
                "Принятие решений",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Осознанный выбор каждый раз",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(40.dp))

            // Карточка формы — без тени, с тонкой рамкой 0.5dp
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = androidx.compose.foundation.BorderStroke(
                    0.5.dp, MaterialTheme.colorScheme.outline
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    CalmTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = "Логин",
                        leadingIcon = Icons.Default.Person,
                        modifier = Modifier.testTag("LoginUsernameInput")
                    )
                    CalmTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Пароль",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onPasswordToggle = { passwordVisible = !passwordVisible },
                        modifier = Modifier.testTag("LoginPasswordInput")
                    )

                    AnimatedVisibility(visible = uiState.error != null) {
                        Text(
                            text = uiState.error ?: "",
                            color = SemanticError,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Кнопка — без elevation в покое, 2dp при нажатии
                    Button(
                        onClick = { viewModel.login(username, password) },
                        enabled = !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CalmBlue),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 2.dp
                        )
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Войти", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            TextButton(onClick = onNavigateToRegister) {
                Text(
                    "Нет аккаунта? Зарегистрироваться",
                    color = CalmBlue,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.success) { if (uiState.success) onRegisterSuccess() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Декоративный круг — шалфей вверху слева
        Box(
            modifier = Modifier
                .size(260.dp)
                .offset(x = (-60).dp, y = (-60).dp)
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.radialGradient(
                        listOf(SageGreen.copy(alpha = 0.07f), SageGreen.copy(alpha = 0f))
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .padding(top = 72.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(SageGreen, CalmBlue),
                            start = Offset(0f, 0f),
                            end = Offset(72f, 72f)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(Modifier.height(20.dp))
            Text(
                "Регистрация",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Создайте ваш личный профиль",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(36.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = androidx.compose.foundation.BorderStroke(
                    0.5.dp, MaterialTheme.colorScheme.outline
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    CalmTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = "Логин (минимум 3 символа)",
                        leadingIcon = Icons.Default.Person,
                        modifier = Modifier.testTag("RegisterUsernameInput")
                    )
                    CalmTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Пароль (минимум 6 символов)",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onPasswordToggle = { passwordVisible = !passwordVisible },
                        modifier = Modifier.testTag("RegisterPasswordInput")
                    )
                    CalmTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = "Подтвердите пароль",
                        leadingIcon = Icons.Default.LockOpen,
                        isPassword = true,
                        modifier = Modifier.testTag("RegisterConfirmPasswordInput")
                    )

                    AnimatedVisibility(visible = uiState.error != null) {
                        Text(
                            text = uiState.error ?: "",
                            color = SemanticError,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Button(
                        onClick = { viewModel.register(username, password, confirmPassword) },
                        enabled = !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth().height(52.dp).testTag("RegisterButton"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SageGreen),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 2.dp
                        )
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Создать аккаунт", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            TextButton(onClick = onNavigateToLogin) {
                Text(
                    "Уже есть аккаунт? Войти",
                    color = CalmBlue,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun CalmTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordToggle: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        leadingIcon = {
            Icon(
                leadingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        },
        trailingIcon = if (isPassword && onPasswordToggle != null) {
            {
                IconButton(onClick = onPasswordToggle, modifier = Modifier.size(36.dp)) {
                    Icon(
                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else null,
        visualTransformation = if (isPassword && !passwordVisible)
            PasswordVisualTransformation() else VisualTransformation.None,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CalmBlue,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedLabelColor = CalmBlue
        ),
        keyboardOptions = if (isPassword)
            KeyboardOptions(keyboardType = KeyboardType.Password)
        else KeyboardOptions.Default
    )
}