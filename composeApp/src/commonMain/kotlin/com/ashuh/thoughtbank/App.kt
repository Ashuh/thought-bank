package com.ashuh.thoughtbank

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.ashuh.thoughtbank.authentication.AuthenticationScreen
import com.ashuh.thoughtbank.deposit.DepositScreen
import com.ashuh.thoughtbank.registration.RegisterUsernameScreen
import com.ashuh.thoughtbank.wallet.WalletScreen
import com.ashuh.thoughtbank.withdraw.WithdrawScreen
import moe.tlaster.precompose.PreComposeApp
import moe.tlaster.precompose.navigation.BackHandler
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.rememberNavigator
import moe.tlaster.precompose.navigation.transition.NavTransition

private enum class Route(val route: String) {
    AUTHENTICATION("/authentication"),
    REGISTER("/register"),
    HOME("/home"),
    DEPOSIT("/deposit"),
    WALLET("/wallet"),
}

@Composable
fun App() {
    PreComposeApp {
        MaterialTheme {
            Screen()
        }
    }
}

@Composable
private fun Screen() {
    val navigator = rememberNavigator()
    val selectedRouteState = remember { mutableStateOf(Route.AUTHENTICATION) }
    var selectedRoute by selectedRouteState
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        bottomBar = {
            if (selectedRoute != Route.AUTHENTICATION) {
                NavigationBar(selectedRouteState, navigator)
            }
        }
    ) { innerPadding ->
        NavHost(
            navigator = navigator,
            navTransition = NavTransition(
                createTransition = EnterTransition.None,
                destroyTransition = ExitTransition.None,
                pauseTransition = ExitTransition.None,
                resumeTransition = EnterTransition.None,
            ),
            initialRoute = Route.AUTHENTICATION.route
        ) {
            scene(
                route = Route.AUTHENTICATION.route,
            ) {
                BackHandler(true) {
                }

                AuthenticationScreen(Modifier.padding(innerPadding),
                    snackbarHostState,
                    onLogin = {
                        if (it) {
                            navigator.navigate(Route.HOME.route)
                            selectedRoute = Route.HOME
                        } else {
                            navigator.navigate(Route.REGISTER.route)
                            selectedRoute = Route.REGISTER
                        }
                })
            }

            scene(
                route = Route.REGISTER.route,
            ) {
                BackHandler(true) {
                }

                RegisterUsernameScreen(Modifier.padding(innerPadding),
                    snackbarHostState,
                    onSuccess = {
                        navigator.navigate(Route.HOME.route)
                        selectedRoute = Route.HOME
                    })
            }

            scene(
                route = Route.HOME.route,
            ) {
                BackHandler(true) {
                }
                WithdrawScreen(
                    modifier = Modifier.padding(innerPadding)
                )
            }

            scene(
                route = Route.DEPOSIT.route,
            ) {
                BackHandler(true) {
                }
                DepositScreen(Modifier.padding(innerPadding))
            }

            scene(
                route = Route.WALLET.route,
            ) {
                BackHandler(true) {
                }
                WalletScreen(Modifier.padding(innerPadding))
            }
        }
    }
}

@Composable
private fun NavigationBar(
    selectedItemState: MutableState<Route>,
    navigator: Navigator
) {
    var selectedItem by selectedItemState

    NavigationBar {
        NavigationBarItem(
            selected = selectedItem == Route.HOME,
            onClick = {
                selectedItem = Route.HOME
                navigator.navigate(Route.HOME.route)
            },
            label = { Text("Home") },
            icon = {
                Icon(
                    Icons.Rounded.Home,
                    contentDescription = null
                )
            }
        )

        NavigationBarItem(
            selected = selectedItem == Route.DEPOSIT,
            onClick = {
                selectedItem = Route.DEPOSIT
                navigator.navigate(Route.DEPOSIT.route)
            },
            label = { Text("Deposit") },
            icon = {
                Icon(
                    Icons.Rounded.Edit,
                    contentDescription = null
                )
            }
        )

        NavigationBarItem(
            selected = selectedItem == Route.WALLET,
            onClick = {
                selectedItem = Route.WALLET
                navigator.navigate(Route.WALLET.route)
            },
            label = { Text("Thought Wallet") },
            icon = {
                Icon(
                    Icons.Rounded.Wallet,
                    contentDescription = null
                )
            }
        )
    }
}
