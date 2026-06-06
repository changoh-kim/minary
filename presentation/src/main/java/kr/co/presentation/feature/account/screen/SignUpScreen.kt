package kr.co.presentation.feature.account.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import kr.co.domain.feature.profile.model.Gender
import kr.co.presentation.R
import kr.co.presentation.common.composable.LoadingButton
import kr.co.presentation.common.extension.getString
import kr.co.presentation.feature.account.preview.provider.SignUpPreviewDataProvider
import kr.co.presentation.feature.account.viewmodel.SignUpAction
import kr.co.presentation.feature.account.viewmodel.SignUpScreenState
import kr.co.presentation.feature.account.viewmodel.SignUpSideEffect
import kr.co.presentation.feature.account.viewmodel.SignUpViewModel
import kr.co.presentation.theme.MinaryTheme
import kr.co.presentation.design.ThemePreviews
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun SignUpScreen(
    onSignUpSucceeded: () -> Unit,
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
        onAction = viewModel::handleAction
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
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(
                snackbarHostState,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                modifier = Modifier.padding(top = 36.dp),
                text = stringResource(R.string.create_an_account),
                style = MaterialTheme.typography.headlineMedium
            )

            // Email
            Text(
                modifier = Modifier.padding(top = 16.dp),
                text = stringResource(R.string.email),
                style = MaterialTheme.typography.labelLarge
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = email,
                    onValueChange = { value ->
                        onAction(SignUpAction.EmailChanged(value))
                    },
                    label = { Text(stringResource(R.string.user_email)) },
                    singleLine = true,
                    isError = emailError != null,
                    supportingText = {
                        if (emailError != null) {
                            Text(text = emailError)
                        }
                    }
                )
                LoadingButton(
                    text = stringResource(R.string.btn_check),
                    onClick = { onAction(SignUpAction.EmailCheckClicked) },
                    isLoading = isCheckingEmail,
                    modifier = Modifier.height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                )
            }
            if (isEmailAvailable != null) {
                Text(
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                    text = if (isEmailAvailable) {
                        stringResource(R.string.email_is_available)
                    } else {
                        stringResource(R.string.email_is_not_available)
                    },
                    color = if (isEmailAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Password
            Text(
                modifier = Modifier.padding(top = 16.dp),
                text = stringResource(R.string.password),
                style = MaterialTheme.typography.labelLarge
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = password,
                onValueChange = { value ->
                    onAction(SignUpAction.PasswordChanged(value))
                },
                label = { Text(stringResource(R.string.user_password)) },
                visualTransformation = PasswordVisualTransformation(),
                isError = passwordError != null,
                supportingText = {
                    if (passwordError != null) {
                        Text(text = passwordError)
                    }
                }
            )

            // Confirm Password
            Text(
                modifier = Modifier.padding(top = 16.dp),
                text = stringResource(R.string.confirm_password),
                style = MaterialTheme.typography.labelLarge
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = confirmPassword,
                onValueChange = { value ->
                    onAction(SignUpAction.ConfirmPasswordChanged(value))
                },
                label = { Text(stringResource(R.string.user_confirm_password)) },
                visualTransformation = PasswordVisualTransformation(),
                isError = confirmPasswordError != null,
                supportingText = {
                    if (confirmPasswordError != null) {
                        Text(text = confirmPasswordError)
                    }
                }
            )

            // User Name
            Text(
                modifier = Modifier.padding(top = 16.dp),
                text = stringResource(R.string.name),
                style = MaterialTheme.typography.labelLarge
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = name,
                onValueChange = { value ->
                    onAction(SignUpAction.NameChanged(value))
                },
                label = { Text(stringResource(R.string.user_name)) },
                isError = nameError != null,
                supportingText = {
                    if (nameError != null) {
                        Text(text = nameError)
                    }
                }
            )

            // Gender
            Text(
                modifier = Modifier.padding(top = 16.dp),
                text = stringResource(R.string.gender),
                style = MaterialTheme.typography.labelLarge
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
                            onClick = null // selectable handles it
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

            // Birthday
            Text(
                modifier = Modifier.padding(top = 16.dp),
                text = stringResource(R.string.birthday),
                style = MaterialTheme.typography.labelLarge
            )

            var showDatePicker by remember { mutableStateOf(false) }
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = birthday
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
                                onAction(SignUpAction.BirthdayChanged(date))
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
                value = birthday.toString(),
                onValueChange = { },
                readOnly = true,
                interactionSource = birthdayInteractionSource,
                label = { Text(stringResource(R.string.placeholder_birthday)) }
            )

            // Phone Number
            Text(
                modifier = Modifier.padding(top = 16.dp),
                text = stringResource(R.string.phone_number),
                style = MaterialTheme.typography.labelLarge
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = phoneNumber,
                onValueChange = { value ->
                    onAction(SignUpAction.PhoneNumberChanged(value))
                },
                label = { Text(stringResource(R.string.placeholder_phone)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = phoneNumberError != null,
                supportingText = {
                    if (phoneNumberError != null) {
                        Text(text = phoneNumberError)
                    }
                }
            )

            // Address
            Text(
                modifier = Modifier.padding(top = 16.dp),
                text = stringResource(R.string.address),
                style = MaterialTheme.typography.labelLarge
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = address,
                onValueChange = { value ->
                    onAction(SignUpAction.AddressChanged(value))
                },
                label = { Text(stringResource(R.string.placeholder_address)) },
                isError = addressError != null,
                supportingText = {
                    if (addressError != null) {
                        Text(text = addressError)
                    }
                }
            )

            // sign up button
            LoadingButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
                    .height(56.dp),
                text = stringResource(R.string.sign_up),
                onClick = { onAction(SignUpAction.SignUpClicked) },
                isLoading = isSigningUp,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@ThemePreviews
@Composable
private fun SignUpScreenPreview(
    @PreviewParameter(SignUpPreviewDataProvider::class)
    state: SignUpScreenState
) {
    SignUpContentPreview(state)
}

@Composable
fun SignUpContentPreview(
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
