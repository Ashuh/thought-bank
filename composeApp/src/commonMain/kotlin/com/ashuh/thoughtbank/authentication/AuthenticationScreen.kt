package com.ashuh.thoughtbank.authentication

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ashuh.thoughtbank.api.FirebaseAuthApi
import com.ashuh.thoughtbank.api.ThoughtBankApi
import com.ashuh.thoughtbank.repository.AuthenticationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun AuthenticationScreen(
    modifier: Modifier,
    snackbarHostState: SnackbarHostState,
    onLogin: (isUserRegistered: Boolean) -> Unit = {}
) {
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    val authenticationViewModel = remember { AuthenticationViewModel(coroutineScope) }
    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    if (authenticationViewModel.isSnackBarShowing) {
        LaunchedEffect(snackbarHostState) {
            val snackbarResult =
                snackbarHostState.showSnackbar(authenticationViewModel.snackBarMessage)
            authenticationViewModel.hideSnackBar()
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
                text = authenticationViewModel.title,
                fontSize = 28.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                enabled = !authenticationViewModel.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            AnimatedVisibility(
                visible = !authenticationViewModel.isSignIn,
                enter = fadeIn(initialAlpha = 0.4f),
                exit = fadeOut(animationSpec = tween(250))
            ) {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Display Name") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    enabled = !authenticationViewModel.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val passwordMismatch =
                !authenticationViewModel.isSignIn && password != confirmPassword

            OutlinedTextField(
                value = password,
                isError = passwordMismatch,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
                enabled = !authenticationViewModel.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            AnimatedVisibility(
                visible = !authenticationViewModel.isSignIn,
                enter = fadeIn(initialAlpha = 0.4f),
                exit = fadeOut(animationSpec = tween(250))
            ) {
                OutlinedTextField(
                    value = confirmPassword,
                    isError = passwordMismatch,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    enabled = !authenticationViewModel.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    when (authenticationViewModel.isSignIn) {
                        true -> {
                            coroutineScope.launch {
                                authenticationViewModel.login(email, password,
                                    onSuccess = {
                                        coroutineScope.launch {
                                            val isUserRegistered =
                                                authenticationViewModel.isUserRegistered(
                                                    AuthenticationRepository.userId!!
                                                )
                                            onLogin(isUserRegistered)
                                        }
                                    })
                            }
                        }

                        false -> {
                            coroutineScope.launch {
                                authenticationViewModel.signUp(email, password)
                            }
                        }
                    }
                },
                enabled = !authenticationViewModel.isLoading
                        && email.isNotEmpty()
                        && password.isNotEmpty()
                        && (authenticationViewModel.isSignIn || !passwordMismatch),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Next")
            }

            Spacer(modifier = Modifier.height(32.dp))

            ClickableText(
                text = AnnotatedString(authenticationViewModel.signInText),
                onClick = {
                    authenticationViewModel.toggleSignInLogIn()
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (authenticationViewModel.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

class AuthenticationViewModel(val scope: CoroutineScope) {

    var title: String by mutableStateOf("Login")
    var signInText: String by mutableStateOf("Create a new account")
    var isLoading: Boolean by mutableStateOf(false)
    var isSnackBarShowing: Boolean by mutableStateOf(false)
    var snackBarMessage: String by mutableStateOf("")
    var isSignIn: Boolean by mutableStateOf(true)

    fun toggleSignInLogIn() {
        isSignIn = !isSignIn
        if (isSignIn) {
            title = "Login"
            signInText = "Create a new account"
        } else {
            title = "Sign Up"
            signInText = "Already have an account? Log in"
        }
    }

    private fun showSnackBar(message: String) {
        isSnackBarShowing = true
        snackBarMessage = message
    }

    fun hideSnackBar() {
        isSnackBarShowing = false
        snackBarMessage = ""
    }

    suspend fun signUp(email: String, password: String) {
        try {
            isLoading = true
            val authResponse = FirebaseAuthApi.signUp(email, password)
            showSnackBar("Sign up successful")
        } catch (e: Exception) {
            showSnackBar(e.message.toString())
        } finally {
            isLoading = false
        }

    }

    suspend fun login(email: String, password: String, onSuccess: (Unit) -> Unit) {
        try {
            isLoading = true
            val authResponse = FirebaseAuthApi.signIn(email, password)
            AuthenticationRepository.userId = authResponse.localId
            onSuccess(Unit)
        } catch (e: Exception) {
            showSnackBar(e.message.toString())
        } finally {
            isLoading = false
        }
    }

    suspend fun isUserRegistered(userId: String): Boolean {
        return ThoughtBankApi.isUserRegistered(userId)
    }
}
