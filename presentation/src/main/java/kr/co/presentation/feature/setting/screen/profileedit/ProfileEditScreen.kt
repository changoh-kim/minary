package kr.co.presentation.feature.setting.screen.profileedit

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.core.common.model.Gender
import kr.co.core.ui.common.text.getString
import kr.co.core.ui.design.preview.ThemePreviews
import kr.co.core.ui.design.theme.MinaryTheme
import kr.co.presentation.R
import kr.co.presentation.feature.setting.screen.profileedit.preview.ProfileEditScreenPreviewParameterProvider
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import java.time.Instant
import java.time.ZoneId

@Composable
fun ProfileEditScreen(
    viewModel: ProfileEditViewModel = hiltViewModel(),
    onBackClicked: () -> Boolean,
    onProfileUpdateSucceeded: () -> Boolean,
) {
    val state by viewModel.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                viewModel.handleAction(ProfileEditAction.ProfilePhotoPicked(it))
            }
        }
    )

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is ProfileEditSideEffect.BackClicked -> withContext(Dispatchers.Main.immediate) {
                onBackClicked()
            }
            is ProfileEditSideEffect.PhotoEditClicked -> withContext(Dispatchers.Main.immediate) {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
            is ProfileEditSideEffect.ProfileUpdateSucceeded -> withContext(Dispatchers.Main.immediate) {
                onProfileUpdateSucceeded()
            }
            is ProfileEditSideEffect.ShowMessage -> coroutineScope.launch {
                snackbarHostState.showSnackbar(context.getString(sideEffect.uiText))
            }
        }
    }

    ProfileEditContent(
        state = state,
        onAction = viewModel::handleAction,
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditContent(
    state: ProfileEditScreenState,
    onAction: (ProfileEditAction) -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val profile = state.userProfile

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.profile_edit_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onAction(ProfileEditAction.BackClicked) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.profile_common_back_desc)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.Center) {
                    AsyncImage(
                        model = profile.profilePhotoUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(160.dp)
                            .border(4.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape)
                            .padding(4.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        alpha = if (state.isPhotoLoading) 0.6f else 1f
                    )

                    if (state.isPhotoLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 4.dp
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (state.isPhotoLoading) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary)
                            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                            .clickable(enabled = !state.isPhotoLoading) { onAction(ProfileEditAction.PhotoEditClicked) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.profile_edit_photo_cd),
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = profile.name.ifEmpty { stringResource(R.string.profile_placeholder_name) },
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                Text(
                    text = stringResource(R.string.profile_change_photo),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clickable { onAction(ProfileEditAction.PhotoEditClicked) }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                ProfileEditField(
                    label = stringResource(R.string.profile_label_full_name),
                    value = profile.name,
                    onValueChange = { onAction(ProfileEditAction.NameChanged(it)) },
                    placeholder = stringResource(R.string.profile_placeholder_name)
                )

                ProfileEditField(
                    label = stringResource(R.string.profile_label_nickname),
                    value = profile.nickname,
                    onValueChange = { onAction(ProfileEditAction.NicknameChanged(it)) },
                    placeholder = stringResource(R.string.profile_placeholder_nickname)
                )

                ProfileEditField(
                    label = stringResource(R.string.profile_label_email),
                    value = profile.email,
                    onValueChange = { onAction(ProfileEditAction.EmailChanged(it)) },
                    placeholder = stringResource(R.string.profile_placeholder_email)
                )

                ProfileEditField(
                    label = stringResource(R.string.profile_label_phone),
                    value = profile.phoneNumber,
                    onValueChange = { onAction(ProfileEditAction.PhoneNumberChanged(it)) },
                    placeholder = stringResource(R.string.profile_placeholder_phone),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Column {
                    Text(
                        text = stringResource(R.string.profile_label_gender),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectableGroup(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Gender.entries.filter { it != Gender.NONE }.forEach { genderOption ->
                            Row(
                                modifier = Modifier
                                    .selectable(
                                        selected = (profile.gender == genderOption),
                                        onClick = { onAction(ProfileEditAction.GenderChanged(genderOption)) },
                                        role = Role.RadioButton
                                    )
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (profile.gender == genderOption),
                                    onClick = null
                                )
                                Text(
                                    text = when (genderOption) {
                                        Gender.MALE -> stringResource(R.string.profile_gender_male)
                                        Gender.FEMALE -> stringResource(R.string.profile_gender_female)
                                        Gender.OTHER -> stringResource(R.string.profile_gender_other)
                                        else -> ""
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }

                Column {
                    Text(
                        text = stringResource(R.string.profile_label_birthday),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    var showDatePicker by remember { mutableStateOf(false) }
                    val datePickerState = rememberDatePickerState(
                        initialSelectedDateMillis = (profile.birthday)
                            .atStartOfDay(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()
                    )

                    if (showDatePicker) {
                        DatePickerDialog(
                            onDismissRequest = { showDatePicker = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    datePickerState.selectedDateMillis?.let { millis ->
                                        val date = Instant.ofEpochMilli(millis)
                                            .atZone(ZoneId.systemDefault())
                                            .toLocalDate()
                                        onAction(ProfileEditAction.BirthdayChanged(date))
                                    }
                                    showDatePicker = false
                                }) {
                                    Text(stringResource(id = android.R.string.ok))
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDatePicker = false }) {
                                    Text(stringResource(id = android.R.string.cancel))
                                }
                            }
                        ) {
                            DatePicker(state = datePickerState)
                        }
                    }

                    val birthdayInteractionSource = remember { MutableInteractionSource() }
                    val isBirthdayPressed by birthdayInteractionSource.collectIsPressedAsState()

                    LaunchedEffect(isBirthdayPressed) {
                        if (isBirthdayPressed) {
                            showDatePicker = true
                        }
                    }

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = profile.birthday.toString(),
                        onValueChange = { },
                        readOnly = true,
                        interactionSource = birthdayInteractionSource,
                        placeholder = { Text(stringResource(R.string.profile_placeholder_birthday)) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                ProfileEditField(
                    label = stringResource(R.string.profile_label_address),
                    value = profile.address,
                    onValueChange = { onAction(ProfileEditAction.AddressChanged(it)) },
                    placeholder = stringResource(R.string.profile_placeholder_address)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { onAction(ProfileEditAction.SaveClicked) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = stringResource(R.string.profile_save_button),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ProfileEditField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary
            ),
            singleLine = true,
            keyboardOptions = keyboardOptions
        )
    }
}

@ThemePreviews
@Composable
private fun ProfileEditScreenPreview(
    @PreviewParameter(ProfileEditScreenPreviewParameterProvider::class)
    state: ProfileEditScreenState
) {
    MinaryTheme {
        ProfileEditContent(state = state)
    }
}
