package kr.co.presentation.design

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kr.co.presentation.common.composable.LoadingButton
import kr.co.presentation.common.composable.LoadingIconButton
import kr.co.presentation.theme.MinaryTheme

@Composable
private fun PreviewSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        content()
        Spacer(modifier = Modifier.size(16.dp))
    }
}

@ThemePreviews
@Composable
fun ComponentPreviews() {
    MinaryTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                PreviewSection(title = "Buttons") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LoadingButton(
                            text = "Primary Button",
                            onClick = { },
                            isLoading = false
                        )
                        LoadingButton(
                            text = "Loading Button",
                            onClick = { },
                            isLoading = true
                        )
                        LoadingIconButton(
                            onClick = { },
                            isLoading = false
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        }
                    }
                }

                PreviewSection(title = "TextFields") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = "Text Field Content",
                            onValueChange = { },
                            label = { Text("Label") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                PreviewSection(title = "Cards") {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Card Title",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Card content goes here.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
