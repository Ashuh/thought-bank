package com.ashuh.thoughtbank.deposit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ashuh.thoughtbank.repository.ThoughtsRepository
import kotlinx.coroutines.launch

private const val MAX_CHARS = 300

@Composable
fun DepositScreen(modifier: Modifier) {
    val scope = rememberCoroutineScope()
    val depositScreenViewModel = remember { DepositScreenViewModel() }
    val focusManager = LocalFocusManager.current
    val numRemainingChars by remember { mutableStateOf(MAX_CHARS) }

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Share your thought",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        val thoughtText = remember { mutableStateOf("") }
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            value = thoughtText.value,
            textStyle = MaterialTheme.typography.bodyLarge,
            onValueChange = {
                if (it.length <= MAX_CHARS) {
                    numRemainingChars - it.length
                    thoughtText.value = it
                }
            },
            isError = thoughtText.value.length >= MAX_CHARS,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                capitalization = KeyboardCapitalization.Sentences
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                }
            ),
            supportingText = {
                Text(
                    text = "${thoughtText.value.length} / $MAX_CHARS",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                )
            },
            enabled = depositScreenViewModel.isLoading.not()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                focusManager.clearFocus()
                scope.launch {
                    depositScreenViewModel.depositThought(thoughtText.value)
                }
            },
            modifier = Modifier.align(Alignment.End),
            enabled = thoughtText.value.isNotBlank() && depositScreenViewModel.isLoading.not()
        ) {
            Text(text = "Deposit")
        }
    }

    if (depositScreenViewModel.isLoading) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier.fillMaxSize()
        ) {
            CircularProgressIndicator()
        }
    }
}

private class DepositScreenViewModel {

    var isLoading: Boolean by mutableStateOf(false)

    suspend fun depositThought(thoughtContent: String) {
        isLoading = true
        ThoughtsRepository.depositThought(thoughtContent)
        isLoading = false
    }
}
