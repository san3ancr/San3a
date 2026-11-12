package com.san3a.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.san3a.app.data.AuthRepository
import com.san3a.app.data.UserProfile
import com.san3a.app.data.UserRepository
import com.san3a.app.ui.screens.CraftsmanDetailScreen
import com.san3a.app.ui.screens.HomeScreen
import com.san3a.app.ui.screens.LoginScreen
import com.san3a.app.ui.screens.OtpScreen
import com.san3a.app.ui.screens.ProfileScreen
import com.san3a.app.ui.screens.SetupScreen
import com.san3a.app.ui.screens.SplashScreen
import com.san3a.app.ui.viewmodel.AuthStep
import com.san3a.app.ui.viewmodel.AuthViewModel
import com.san3a.app.ui.viewmodel.HomeViewModel
import com.san3a.app.ui.viewmodel.ProfileViewModel
import com.san3a.app.ui.viewmodel.SessionState
import com.san3a.app.ui.viewmodel.SessionViewModel
import com.san3a.app.ui.viewmodel.SetupViewModel

@Composable
fun AppRoot() {
    val sessionViewModel: SessionViewModel = viewModel()
    val state by sessionViewModel.state.collectAsStateWithLifecycle()

    when (val current = state) {
        SessionState.Loading -> SplashScreen()
        SessionState.LoggedOut -> AuthFlow()
        is SessionState.NeedsProfile -> SetupFlow(phone = current.phone)
        is SessionState.Ready -> MainFlow(
            profile = current.profile,
            onSignOut = sessionViewModel::signOut
        )
    }
}

@Composable
private fun AuthFlow() {
    val vm: AuthViewModel = viewModel()
    val step by vm.step.collectAsStateWithLifecycle()
    val phone by vm.phone.collectAsStateWithLifecycle()
    val code by vm.code.collectAsStateWithLifecycle()
    val busy by vm.busy.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    val activity = LocalContext.current.findActivity()

    when (step) {
        AuthStep.PHONE -> LoginScreen(
            phone = phone,
            busy = busy,
            error = error,
            onPhoneChange = vm::onPhoneChange,
            onSubmitPhone = { activity?.let { vm.sendCode(it) } },
            onGoogleSignIn = { activity?.let { vm.signInWithGoogle(it) } }
        )

        AuthStep.CODE -> OtpScreen(
            phone = phone,
            code = code,
            busy = busy,
            error = error,
            onCodeChange = vm::onCodeChange,
            onVerify = vm::verifyCode,
            onResend = { activity?.let { vm.sendCode(it) } },
            onChangeNumber = vm::backToPhone
        )
    }
}

@Composable
private fun SetupFlow(phone: String) {
    val vm: SetupViewModel = viewModel()
    val ui by vm.ui.collectAsStateWithLifecycle()
    SetupScreen(
        ui = ui,
        phone = phone,
        onRole = vm::setRole,
        onName = vm::onName,
        onPhone = vm::onPhone,
        onWhatsapp = vm::onWhatsapp,
        onCraft = vm::onCraft,
        onRegion = vm::onRegion,
        onCaptureLocation = vm::captureLocation,
        onSave = vm::save
    )
}

@Composable
private fun MainFlow(profile: UserProfile, onSignOut: () -> Unit) {
    val navController = rememberNavController()
    val profileViewModel: ProfileViewModel = viewModel()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showBottomBar = currentRoute != null && !currentRoute.startsWith(Routes.DETAIL)
    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == Routes.HOME,
                        onClick = {
                            if (currentRoute != Routes.HOME) {
                                navController.navigate(Routes.HOME) {
                                    popUpTo(Routes.HOME) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        },
                        icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                        label = { Text("الرئيسية") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.PROFILE,
                        onClick = {
                            if (currentRoute != Routes.PROFILE) {
                                navController.navigate(Routes.PROFILE) { launchSingleTop = true }
                            }
                        },
                        icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                        label = { Text("حسابي") }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.HOME) {
                val homeViewModel: HomeViewModel = viewModel()
                HomeScreen(
                    viewModel = homeViewModel,
                    onOpenCraftsman = { craftsman ->
                        navController.navigate("${Routes.DETAIL}/${craftsman.uid}")
                    }
                )
            }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    profile = profile,
                    onToggleAvailability = { profileViewModel.toggleAvailability(profile) },
                    onSignOut = onSignOut,
                    onDeleteAccount = { profileViewModel.deleteAccount() }
                )
            }
            composable("${Routes.DETAIL}/{uid}") { entry ->
                val uid = entry.arguments?.getString("uid").orEmpty()
                CraftsmanDetailRoute(
                    uid = uid,
                    currentUser = profile,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun CraftsmanDetailRoute(uid: String, currentUser: UserProfile, onBack: () -> Unit) {
    val repository = remember { UserRepository() }
    val profile by repository.observeProfile(uid)
        .collectAsStateWithLifecycle(initialValue = null)

    var timedOut by remember(uid) { mutableStateOf(false) }
    var retryKey by remember(uid) { mutableStateOf(0) }
    LaunchedEffect(uid, retryKey) {
        timedOut = false
        delay(8000)
        timedOut = true
    }

    val current = profile
    when {
        current != null -> CraftsmanDetailScreen(profile = current, currentUser = currentUser, onBack = onBack)
        timedOut -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("تعذّر تحميل ملف الحرفي", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                androidx.compose.material3.TextButton(onClick = { retryKey++ }) {
                    Text("إعادة المحاولة")
                }
            }
        }
        else -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

private object Routes {
    const val HOME = "home"
    const val PROFILE = "profile"
    const val DETAIL = "detail"
}
