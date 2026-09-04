package com.pulse.messenger.ui.navigation

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pulse.messenger.domain.model.ProfileStage
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.pulse.messenger.ui.screens.auth.AuthEvent
import com.pulse.messenger.ui.screens.auth.AuthViewModel
import com.pulse.messenger.ui.screens.forgot.ForgotEmailScreen
import com.pulse.messenger.ui.screens.forgot.ForgotInboxScreen
import com.pulse.messenger.ui.screens.forgot.ResetPasswordScreen
import com.pulse.messenger.ui.screens.friends.FindFriendsScreen
import com.pulse.messenger.ui.screens.login.LoginEmailScreen
import com.pulse.messenger.ui.screens.login.LoginPasswordScreen
import com.pulse.messenger.ui.screens.login.LoginPhoneScreen
import com.pulse.messenger.ui.screens.conversation.ConversationScreen
import com.pulse.messenger.ui.screens.conversation.ConversationViewModel
import com.pulse.messenger.ui.screens.main.MainScreen
import com.pulse.messenger.ui.screens.main.NewChatStubScreen
import com.pulse.messenger.ui.screens.archived.ArchivedScreen
import com.pulse.messenger.ui.screens.archived.ArchivedViewModel
import com.pulse.messenger.ui.screens.folders.FoldersScreen
import com.pulse.messenger.ui.screens.folders.FoldersViewModel
import com.pulse.messenger.ui.screens.otp.OtpScreen
import com.pulse.messenger.ui.screens.permissions.PermissionsScreen
import com.pulse.messenger.ui.screens.profile.ProfileBioScreen
import com.pulse.messenger.ui.screens.profile.ProfileNameScreen
import com.pulse.messenger.ui.screens.profile.ProfilePhotoScreen
import com.pulse.messenger.ui.screens.profile.ProfileUsernameScreen
import com.pulse.messenger.ui.screens.search.GlobalSearchScreen
import com.pulse.messenger.ui.screens.search.SearchViewModel
import com.pulse.messenger.ui.screens.signup.SignupEmailScreen
import com.pulse.messenger.ui.screens.splash.SplashScreen
import com.pulse.messenger.ui.screens.splash.SplashViewModel
import com.pulse.messenger.ui.screens.success.AccountCreatedScreen
import com.pulse.messenger.ui.screens.success.ResetSuccessScreen
import com.pulse.messenger.ui.screens.welcome.WelcomeScreen
import com.pulse.messenger.ui.theme.PulseTheme
import com.pulse.messenger.ui.theme.PulseColors
import kotlinx.coroutines.launch

/**
 * App navigation graph + the auth event router.
 *
 * Motion follows PRD §4.5: fade + 24dp slide, 250ms, FastOutSlowIn.
 * The auth journey (S02-S17) shares one Activity-scoped [AuthViewModel]; its
 * one-shot [AuthEvent]s are mapped to destinations here - the ViewModel never
 * knows navigation routes (unidirectional data flow).
 */
@Composable
fun PulseApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val density = androidx.compose.ui.platform.LocalDensity.current
    val slidePx = with(density) { 24.dp.roundToPx() }
    val motionSpec = tween<androidx.compose.ui.unit.IntOffset>(
        durationMillis = 250,
        easing = FastOutSlowInEasing,
    )
    val fadeSpec = tween<Float>(durationMillis = 250, easing = FastOutSlowInEasing)

    val authViewModel = rememberAuthViewModel()
    val pulseColors = PulseTheme.colors

    fun goToLogin() {
        navController.navigate(PulseRoutes.LOGIN_EMAIL) {
            popUpTo(PulseRoutes.WELCOME)
        }
    }

    fun goToMain() {
        navController.navigate(PulseRoutes.MAIN) {
            popUpTo(PulseRoutes.WELCOME) { inclusive = true }
        }
    }

    // AuthEvent -> destination (single mapping point).
    LaunchedEffect(Unit) {
        authViewModel.events.collect { event ->
            when (event) {
                AuthEvent.OtpSent -> navController.navigate(PulseRoutes.OTP)
                AuthEvent.SignInComplete -> goToMain()
                AuthEvent.EmailVerifiedNext -> navController.navigate(PulseRoutes.SIGNUP_PASSWORD)
                AuthEvent.ForgotEmailSent -> navController.navigate(PulseRoutes.FORGOT_INBOX)
                AuthEvent.ResetComplete -> navController.navigate(PulseRoutes.SUCCESS_RESET)
                AuthEvent.SignupPasswordSaved -> navController.navigate(PulseRoutes.PROFILE_NAME)
                AuthEvent.GoogleNewUser -> navController.navigate(PulseRoutes.PROFILE_NAME)
                is AuthEvent.StageSaved -> when (event.stage) {
                    ProfileStage.Name -> navController.navigate(PulseRoutes.PROFILE_USERNAME)
                    ProfileStage.Username -> navController.navigate(PulseRoutes.PROFILE_PHOTO)
                    ProfileStage.Photo -> navController.navigate(PulseRoutes.PROFILE_BIO)
                    ProfileStage.Bio -> navController.navigate(PulseRoutes.PERMISSIONS)
                }
                AuthEvent.AccountCreated -> navController.navigate(PulseRoutes.SUCCESS_ACCOUNT) {
                    popUpTo(PulseRoutes.WELCOME) { inclusive = true }
                }
            }
        }
    }

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        authViewModel.snackbar.collect { snack ->
            snackbarHostState.showSnackbar(
                message = context.getString(snack.resId),
                duration = SnackbarDuration.Short,
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = PulseRoutes.SPLASH,
            modifier = Modifier.fillMaxSize(),
            enterTransition = {
                fadeIn(fadeSpec) + slideInHorizontally(motionSpec) { slidePx }
            },
            exitTransition = {
                fadeOut(fadeSpec) + slideOutHorizontally(motionSpec) { -slidePx }
            },
            popEnterTransition = {
                fadeIn(fadeSpec) + slideInHorizontally(motionSpec) { -slidePx }
            },
            popExitTransition = {
                fadeOut(fadeSpec) + slideOutHorizontally(motionSpec) { slidePx }
            },
        ) {
            composable(PulseRoutes.SPLASH) {
                val viewModel: SplashViewModel = hiltViewModel()
                SplashScreen(
                    viewModel = viewModel,
                    onFinished = { target ->
                        navController.navigate(target) {
                            popUpTo(PulseRoutes.SPLASH) { inclusive = true }
                        }
                    },
                )
            }

            /* ---------- S02 ---------- */
            composable(PulseRoutes.WELCOME) {
                WelcomeScreen(
                    vm = authViewModel,
                    onLogin = { navController.navigate(PulseRoutes.LOGIN_EMAIL) },
                    onCreateAccount = { navController.navigate(PulseRoutes.SIGNUP_EMAIL) },
                )
            }

            /* ---------- S03/S03b/S04 login ---------- */
            composable(PulseRoutes.LOGIN_EMAIL) {
                LoginEmailScreen(
                    vm = authViewModel,
                    onContinue = { navController.navigate(PulseRoutes.LOGIN_PASSWORD) },
                    onUsePhone = { navController.navigate(PulseRoutes.LOGIN_PHONE) },
                )
            }
            composable(PulseRoutes.LOGIN_PASSWORD) {
                LoginPasswordScreen(
                    vm = authViewModel,
                    onBackToEmail = { navController.popBackStack() },
                    onForgotPassword = { navController.navigate(PulseRoutes.FORGOT_EMAIL) },
                )
            }
            composable(PulseRoutes.LOGIN_PHONE) {
                LoginPhoneScreen(
                    vm = authViewModel,
                    onUseEmail = { navController.popBackStack() },
                )
            }

            /* ---------- S06/S11b OTP ---------- */
            composable(PulseRoutes.OTP) {
                OtpScreen(
                    vm = authViewModel,
                    onChangeIdentifier = { navController.popBackStack() },
                )
            }

            /* ---------- S07-S10 forgot/reset ---------- */
            composable(PulseRoutes.FORGOT_EMAIL) {
                ForgotEmailScreen(vm = authViewModel)
            }
            composable(PulseRoutes.FORGOT_INBOX) {
                ForgotInboxScreen(
                    vm = authViewModel,
                    onBackToLogin = { goToLogin() },
                )
            }
            composable(PulseRoutes.RESET_PASSWORD) {
                ResetPasswordScreen(vm = authViewModel)
            }
            composable(PulseRoutes.SUCCESS_RESET) {
                ResetSuccessScreen(onBackToLogin = { goToLogin() })
            }

            /* ---------- S11a/S11c sign-up ---------- */
            composable(PulseRoutes.SIGNUP_EMAIL) {
                SignupEmailScreen(
                    vm = authViewModel,
                    onLoginInstead = { goToLogin() },
                )
            }
            composable(PulseRoutes.SIGNUP_PASSWORD) {
                ResetPasswordScreen(vm = authViewModel, isSignUp = true)
            }

            /* ---------- S12-S17 profile setup + completion ---------- */
            composable(PulseRoutes.PROFILE_NAME) {
                ProfileNameScreen(
                    vm = authViewModel,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(PulseRoutes.PROFILE_USERNAME) {
                ProfileUsernameScreen(
                    vm = authViewModel,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(PulseRoutes.PROFILE_PHOTO) {
                ProfilePhotoScreen(
                    vm = authViewModel,
                    onSkip = { navController.navigate(PulseRoutes.PROFILE_BIO) },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(PulseRoutes.PROFILE_BIO) {
                ProfileBioScreen(
                    vm = authViewModel,
                    onSkip = { navController.navigate(PulseRoutes.PERMISSIONS) },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(PulseRoutes.PERMISSIONS) {
                PermissionsScreen(
                    vm = authViewModel,
                    onContinue = { navController.navigate(PulseRoutes.FIND_FRIENDS) },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(PulseRoutes.FIND_FRIENDS) {
                FindFriendsScreen(
                    vm = authViewModel,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(PulseRoutes.SUCCESS_ACCOUNT) {
                AccountCreatedScreen(onStartChatting = { goToMain() })
            }

            /* ---------- S18 ---------- */
            composable(PulseRoutes.MAIN) {
                MainScreen(
                    onOpenChat = { chatId -> navController.navigate(PulseRoutes.chatRoute(chatId)) },
                    onOpenSearch = { navController.navigate(PulseRoutes.SEARCH) },
                    onOpenArchived = { navController.navigate(PulseRoutes.ARCHIVED) },
                    onOpenFolders = { navController.navigate(PulseRoutes.FOLDERS) },
                    onNewChat = { navController.navigate(PulseRoutes.NEW_CHAT) },
                    onShowMessage = { message ->
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = message,
                                duration = SnackbarDuration.Short,
                            )
                        }
                    },
                )
            }

            /* ---------- S19 destinations ---------- */
            composable(PulseRoutes.SEARCH) {
                val viewModel: SearchViewModel = hiltViewModel()
                GlobalSearchScreen(
                    vm = viewModel,
                    onOpenChat = { chatId -> navController.navigate(PulseRoutes.chatRoute(chatId)) },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(PulseRoutes.ARCHIVED) {
                val viewModel: ArchivedViewModel = hiltViewModel()
                ArchivedScreen(
                    vm = viewModel,
                    onOpenChat = { chatId -> navController.navigate(PulseRoutes.chatRoute(chatId)) },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(PulseRoutes.FOLDERS) {
                val viewModel: FoldersViewModel = hiltViewModel()
                FoldersScreen(
                    vm = viewModel,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(PulseRoutes.NEW_CHAT) {
                NewChatStubScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = PulseRoutes.CHAT,
                arguments = listOf(navArgument("chatId") { type = NavType.StringType }),
            ) {
                val viewModel: ConversationViewModel = hiltViewModel()
                ConversationScreen(
                    vm = viewModel,
                    onBack = { navController.popBackStack() },
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = pulseColors.surface,
                contentColor = pulseColors.textPrimary,
            )
        }
    }
}

/** Activity-scoped ViewModel: one AuthViewModel instance across S02-S17. */
@Composable
private fun rememberAuthViewModel(): AuthViewModel {
    val context = LocalContext.current
    val activity = context as? Activity
        ?: error("AuthViewModel requires an Activity context")
    return hiltViewModel(viewModelStoreOwner = activity as ComponentActivity)
}
