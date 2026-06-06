package kr.co.presentation.feature.setting.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import kr.co.domain.feature.setting.model.AppTheme
import kr.co.presentation.BuildConfig
import kr.co.presentation.R
import kr.co.presentation.common.extension.getString
import kr.co.presentation.common.model.UiText
import kr.co.presentation.design.ThemePreviews
import kr.co.presentation.feature.setting.model.UserProfileUiModel
import kr.co.presentation.feature.setting.preview.SettingsScreenStatePreviewParameterProvider
import kr.co.presentation.feature.setting.preview.SignOutConfirmationDialogPreviewDataProvider
import kr.co.presentation.feature.setting.viewmodel.SettingsAction
import kr.co.presentation.feature.setting.viewmodel.SettingsScreenState
import kr.co.presentation.feature.setting.viewmodel.SettingsSideEffect
import kr.co.presentation.feature.setting.viewmodel.SettingsViewModel
import kr.co.presentation.theme.MinaryTheme
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onUserProfileClicked: () -> Unit,
    onSignOutSucceeded: () -> Unit
) {
    val state by viewModel.collectAsState()
    var showSignOutDialog by remember { mutableStateOf(false) }
    var signOutDialogMessage by remember { mutableStateOf<UiText?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is SettingsSideEffect.UserProfileClicked -> onUserProfileClicked()
            is SettingsSideEffect.SignOutSucceeded -> onSignOutSucceeded()
            is SettingsSideEffect.ShowSignOutDialog -> {
                signOutDialogMessage = sideEffect.uiText
                showSignOutDialog = true
            }

            is SettingsSideEffect.ShowMessage -> coroutineScope.launch {
                snackbarHostState.showSnackbar(context.getString(sideEffect.uiText))
            }
        }
    }

    MinaryTheme(appTheme = state.userSettings.appTheme) {
        if (showSignOutDialog) {
            SignOutConfirmationDialog(
                contentMessage = signOutDialogMessage?.let { context.getString(it) },
                onConfirm = {
                    showSignOutDialog = false
                    viewModel.handleAction(SettingsAction.SignOutConfirmed)
                },
                onDismiss = { showSignOutDialog = false }
            )
        }

        SettingsContent(
            state = state,
            onAction = viewModel::handleAction
        )
    }
}

@Composable
fun SettingsContent(
    state: SettingsScreenState,
    onAction: (SettingsAction) -> Unit = {},
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))

            ProfileCard(
                userProfile = state.userProfile,
                onClick = { onAction(SettingsAction.UserProfileClicked) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            SettingsSectionTitle(title = stringResource(R.string.settings_appearance))
            Spacer(modifier = Modifier.height(16.dp))
            SettingsItem(
                icon = Icons.Default.Palette,
                title = stringResource(R.string.settings_theme)
            )
            Spacer(modifier = Modifier.height(12.dp))
            ThemeSelector(
                currentTheme = state.userSettings.appTheme,
                onThemeSelected = { onAction(SettingsAction.ThemeChanged(it)) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            SettingsSectionTitle(title = stringResource(R.string.settings_backup_cloud))
            Spacer(modifier = Modifier.height(16.dp))
            SyncSettingItem(
                isSyncEnabled = state.userSettings.isDiarySyncEnabled,
                onCheckedChange = { onAction(SettingsAction.DiarySyncEnabledChanged(it)) }
            )

            Spacer(modifier = Modifier.height(40.dp))

            OutlinedButton(
                onClick = { onAction(SettingsAction.SignOutClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(width = 1.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.settings_sign_out),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SignOutConfirmationDialog(
    contentMessage: String? = null,
    onConfirm: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.settings_sign_out),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        },
        text = {
            Text(
                text = contentMessage ?: stringResource(R.string.settings_sign_out_confirm_message),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text(text = stringResource(R.string.dialog_confirm), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.dialog_cancel))
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.error
            )
        }
    )
}

@Composable
private fun ProfileCard(
    userProfile: UserProfileUiModel,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = userProfile.profilePhotoUrl,
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = userProfile.name.ifEmpty { stringResource(R.string.settings_default_user_name) },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = userProfile.email.ifEmpty { stringResource(R.string.settings_default_user_email) },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 1.sp
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ThemeSelector(
    currentTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ThemeOption(
            icon = Icons.Default.LightMode,
            label = stringResource(R.string.settings_theme_light),
            isSelected = currentTheme == AppTheme.LIGHT,
            onClick = { onThemeSelected(AppTheme.LIGHT) }
        )
        ThemeOption(
            icon = Icons.Default.SettingsSuggest,
            label = stringResource(R.string.settings_theme_system),
            isSelected = currentTheme == AppTheme.SYSTEM,
            onClick = { onThemeSelected(AppTheme.SYSTEM) }
        )
        ThemeOption(
            icon = Icons.Default.DarkMode,
            label = stringResource(R.string.settings_theme_dark),
            isSelected = currentTheme == AppTheme.DARK,
            onClick = { onThemeSelected(AppTheme.DARK) }
        )
    }
}

@Composable
private fun ThemeOption(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(40.dp)
            .width(100.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SyncSettingItem(
    isSyncEnabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CloudSync,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = stringResource(R.string.settings_diary_sync),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.settings_diary_sync_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 40.dp)
            )
        }
        Switch(
            checked = isSyncEnabled,
            onCheckedChange = onCheckedChange
        )
    }
}

@ThemePreviews
@Composable
private fun SettingsScreenPreview(
    @PreviewParameter(SettingsScreenStatePreviewParameterProvider::class)
    state: SettingsScreenState
) {
    SettingsScreenPreviewContent(state = state)
}

@ThemePreviews
@Composable
private fun SignOutConfirmationDialogPreview(
    @PreviewParameter(SignOutConfirmationDialogPreviewDataProvider::class)
    uiText: UiText
) {
    SignOutConfirmationDialogPreviewContent(uiText = uiText)
}

@Composable
fun SettingsScreenPreviewContent(
    state: SettingsScreenState
) {
    MinaryTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            SettingsContent(
                state = state
            )
        }
    }
}

@Composable
fun SignOutConfirmationDialogPreviewContent(
    uiText: UiText
) {
    MinaryTheme {
        val context = LocalContext.current
        val message = context.getString(uiText)
        SignOutConfirmationDialog(contentMessage = message)
    }
}