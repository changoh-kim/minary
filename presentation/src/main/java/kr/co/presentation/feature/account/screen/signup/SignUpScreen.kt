package kr.co.presentation.feature.account.screen.signup

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import kr.co.core.common.model.Gender
import kr.co.core.ui.common.text.getString
import kr.co.core.ui.design.component.LoadingButton
import kr.co.core.ui.design.preview.ThemePreviews
import kr.co.core.ui.design.theme.MinaryTheme
import kr.co.presentation.R
import kr.co.presentation.feature.account.screen.signup.preview.SignUpScreenPreviewParameterProvider
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun SignUpScreen(
    onSignUpSucceeded: () -> Unit,
    onBackClicked: () -> Unit,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val state: SignUpScreenState by viewModel.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is SignUpSideEffect.SignUpSucceeded -> onSignUpSucceeded()
            is SignUpSideEffect.ShowMessage -> coroutineScope.launch {
                snackbarHostState.showSnackbar(context.getString(sideEffect.uiText))
            }
        }
    }

    SignUpContent(
        email = state.email,
        name = state.name,
        password = state.password,
        confirmPassword = state.confirmPassword,
        gender = state.gender,
        birthday = state.birthday,
        address = state.address,
        phoneNumber = state.phoneNumber,
        isSigningUp = state.isSigningUp,
        isEmailAvailable = state.isEmailAvailable,
        isCheckingEmail = state.isCheckingEmail,
        emailError = state.emailError?.let { context.getString(it) },
        passwordError = state.passwordError?.let { context.getString(it) },
        confirmPasswordError = state.confirmPasswordError?.let { context.getString(it) },
        nameError = state.nameError?.let { context.getString(it) },
        addressError = state.addressError?.let { context.getString(it) },
        phoneNumberError = state.phoneNumberError?.let { context.getString(it) },
        snackbarHostState = snackbarHostState,
        onAction = viewModel::handleAction,
        onBackClicked = onBackClicked
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpContent(
    email: String = "",
    password: String = "",
    confirmPassword: String = "",
    name: String = "",
    gender: Gender = Gender.NONE,
    birthday: LocalDate = LocalDate.now(),
    address: String = "",
    phoneNumber: String = "",
    isSigningUp: Boolean = false,
    isEmailAvailable: Boolean? = null,
    isCheckingEmail: Boolean = false,
    emailError: String? = null,
    passwordError: String? = null,
    confirmPasswordError: String? = null,
    nameError: String? = null,
    addressError: String? = null,
    phoneNumberError: String? = null,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onAction: (SignUpAction) -> Unit = {},
    onBackClicked: () -> Unit = {}
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(
                snackbarHostState,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp, bottom = paddingValues.calculateBottomPadding())
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
                    .background(
                        MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clip(RoundedCornerShape(12.dp))
            ) {
                // Top App Bar
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.create_account_title),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClicked) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header
                    Text(
                        text = stringResource(R.string.create_an_account),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = (-0.75).sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.sign_up_subtitle),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 24.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                    // Full Name
                    SignUpTextField(
                        label = stringResource(R.string.profile_label_full_name),
                        value = name,
                        onValueChange = { onAction(SignUpAction.NameChanged(it)) },
                        placeholder = stringResource(R.string.placeholder_name),
                        leadingIcon = Icons.Default.Person,
                        error = nameError
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    // Email
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.profile_label_email),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("sign_up_email_field"),
                                value = email,
                                onValueChange = { onAction(SignUpAction.EmailChanged(it)) },
                                placeholder = {
                                    Text(
                                        text = stringResource(R.string.placeholder_email),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary
                                ),
                                isError = emailError != null,
                                singleLine = true
                            )
                            LoadingButton(
                                text = stringResource(R.string.btn_check),
                                onClick = { onAction(SignUpAction.EmailCheckClicked) },
                                isLoading = isCheckingEmail,
                                modifier = Modifier
                                    .height(56.dp)
                                    .testTag("sign_up_email_check_button"),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                        if (emailError != null) {
                            Text(
                                text = emailError,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                            )
                        }
                        if (isEmailAvailable != null) {
                            Text(
                                text = if (isEmailAvailable) stringResource(R.string.email_is_available) else stringResource(
                                    R.string.email_is_not_available
                                ),
                                color = if (isEmailAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    // Phone Number
                    SignUpTextField(
                        label = stringResource(R.string.profile_label_phone),
                        value = phoneNumber,
                        onValueChange = { onAction(SignUpAction.PhoneNumberChanged(it)) },
                        placeholder = stringResource(R.string.placeholder_phone_us),
                        leadingIcon = Icons.Default.Phone,
                        error = phoneNumberError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    // Password
                    var isPasswordVisible by remember { mutableStateOf(false) }
                    SignUpTextField(
                        label = stringResource(R.string.password),
                        value = password,
                        onValueChange = { onAction(SignUpAction.PasswordChanged(it)) },
                        placeholder = stringResource(R.string.placeholder_password),
                        leadingIcon = Icons.Default.Lock,
                        trailingIcon = {
                            val icon =
                                if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        error = passwordError,
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation()
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    // Confirm Password
                    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
                    SignUpTextField(
                        label = stringResource(R.string.confirm_password),
                        value = confirmPassword,
                        onValueChange = { onAction(SignUpAction.ConfirmPasswordChanged(it)) },
                        placeholder = stringResource(R.string.user_confirm_password),
                        leadingIcon = Icons.Default.Lock,
                        trailingIcon = {
                            val icon =
                                if (isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            IconButton(onClick = {
                                isConfirmPasswordVisible = !isConfirmPasswordVisible
                            }) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        error = confirmPasswordError,
                        visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation()
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    // Gender
                    Text(
                        text = stringResource(R.string.profile_label_gender),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
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
                                        selected = (gender == genderOption),
                                        onClick = { onAction(SignUpAction.GenderChanged(genderOption)) },
                                        role = Role.RadioButton
                                    )
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (gender == genderOption),
                                    onClick = null
                                )
                                Text(
                                    text = when (genderOption) {
                                        Gender.MALE -> stringResource(R.string.gender_male)
                                        Gender.FEMALE -> stringResource(R.string.gender_female)
                                        Gender.OTHER -> stringResource(R.string.gender_other)
                                        else -> ""
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    // Birthday
                    Text(
                        text = stringResource(R.string.profile_label_birthday),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    var showDatePicker by remember { mutableStateOf(false) }
                    val datePickerState = rememberDatePickerState(
                        initialSelectedDateMillis = birthday.atStartOfDay(ZoneId.systemDefault())
                            .toInstant().toEpochMilli()
                    )

                    if (showDatePicker) {
                        DatePickerDialog(
                            onDismissRequest = { showDatePicker = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    datePickerState.selectedDateMillis?.let { millis ->
                                        onAction(
                                            SignUpAction.BirthdayChanged(
                                                Instant.ofEpochMilli(millis)
                                                    .atZone(ZoneId.systemDefault()).toLocalDate()
                                            )
                                        )
                                    }
                                    showDatePicker = false
                                }) { Text(stringResource(id = android.R.string.ok)) }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDatePicker = false }) {
                                    Text(stringResource(id = android.R.string.cancel))
                                }
                            }
                        ) { DatePicker(state = datePickerState) }
                    }
                    val birthdayInteractionSource = remember { MutableInteractionSource() }
                    val isBirthdayPressed by birthdayInteractionSource.collectIsPressedAsState()
                    LaunchedEffect(isBirthdayPressed) {
                        if (isBirthdayPressed) showDatePicker = true
                    }

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = birthday.toString(),
                        onValueChange = { },
                        readOnly = true,
                        interactionSource = birthdayInteractionSource,
                        placeholder = {
                            Text(
                                text = stringResource(R.string.placeholder_birthday),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    // Address
                    SignUpTextField(
                        label = stringResource(R.string.profile_label_address),
                        value = address,
                        onValueChange = { onAction(SignUpAction.AddressChanged(it)) },
                        placeholder = stringResource(R.string.placeholder_address),
                        leadingIcon = Icons.Default.Home,
                        error = addressError
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                    // Sign Up Button
                    LoadingButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("sign_up_submit_button")
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(8.dp),
                                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            ),
                        text = stringResource(R.string.sign_up),
                        onClick = { onAction(SignUpAction.SignUpClicked) },
                        isLoading = isSigningUp,
                        shape = RoundedCornerShape(8.dp),
                    )
                }
                // Visual Decoration
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(4.dp)
                            .align(Alignment.Center)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
            // Login Link (Outside the card)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.already_have_an_account),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.back_to_login),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.clickable { onBackClicked() }
                )
            }
        }
    }
}

@Composable
fun SignUpTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    trailingIcon: @Composable (() -> Unit)? = null,
    error: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            },
            trailingIcon = trailingIcon,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedBorderColor = MaterialTheme.colorScheme.primary
            ),
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            isError = error != null,
            singleLine = true
        )
        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}

@ThemePreviews
@Composable
private fun SignUpScreenPreview(
    @PreviewParameter(SignUpScreenPreviewParameterProvider::class)
    state: SignUpScreenState
) {
    MinaryTheme {
        SignUpContent(
            email = state.email,
            name = state.name,
            password = state.password,
            confirmPassword = state.confirmPassword,
            gender = state.gender,
            birthday = state.birthday,
            address = state.address,
            phoneNumber = state.phoneNumber,
            isSigningUp = state.isSigningUp
        )
    }
}
