package com.ashuh.thoughtbank.registration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ashuh.thoughtbank.api.ThoughtBankApi
import com.ashuh.thoughtbank.repository.AuthenticationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun RegisterUsernameScreen(modifier: Modifier, snackbarHostState: SnackbarHostState, onSuccess: () -> Unit) {
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    val registerUsernameViewModel = remember { RegisterUsernameViewModel(coroutineScope) }
    var username by remember { mutableStateOf("") }

    if (registerUsernameViewModel.isSnackBarShowing) {
        LaunchedEffect(snackbarHostState) {
            val snackbarResult =
                snackbarHostState.showSnackbar(registerUsernameViewModel.snackBarMessage)
            registerUsernameViewModel.hideSnackBar()
        }
    }

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Select a Username",
                fontSize = 28.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                enabled = !registerUsernameViewModel.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        registerUsernameViewModel.registerUsername(
                            AuthenticationRepository.userId!!,
                            username,
                            onSuccess = {
                                onSuccess()
                            })
                    }
                },
                enabled = !registerUsernameViewModel.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Next")
            }
        }

        if (registerUsernameViewModel.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

class RegisterUsernameViewModel(val scope: CoroutineScope) {

    var isSnackBarShowing: Boolean by mutableStateOf(false)
    var snackBarMessage: String by mutableStateOf("")
    var isLoading: Boolean by mutableStateOf(false)

    fun registerUsername(userId: String, username: String, onSuccess: () -> Unit) {
        scope.launch {
            try {
                isLoading = true
                ThoughtBankApi.registerUsername(userId, username)
                onSuccess()
            } catch (e: Exception) {
                showSnackBar(e.message!!)
                return@launch
            } finally {
                isLoading = false
            }
        }
    }

    private fun showSnackBar(message: String) {
        isSnackBarShowing = true
        snackBarMessage = message
    }

    fun hideSnackBar() {
        isSnackBarShowing = false
    }
}
