package com.pulse.messenger.ui.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pulse.messenger.ui.screens.main.MainScreen
import com.pulse.messenger.ui.screens.splash.SplashScreen
import com.pulse.messenger.ui.screens.splash.SplashViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.math.roundToInt

/**
 * App navigation graph.
 *
 * Motion follows PRD §4.5: fade + 24dp slide, 250ms, FastOutSlowIn.
 * Graph today: Splash -> Main shell. Onboarding/auth (S02-S17) attach in M2;
 * conversation graph (S23+) in M4 - route constants live in [PulseRoutes].
 */
@Composable
fun PulseApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val density = LocalDensity.current
    val slidePx = with(density) { 24.dp.roundToPx() }
    val motionSpec = tween<androidx.compose.ui.unit.IntOffset>(
        durationMillis = 250,
        easing = FastOutSlowInEasing,
    )
    val fadeSpec = tween<Float>(durationMillis = 250, easing = FastOutSlowInEasing)

    NavHost(
        navController = navController,
        startDestination = PulseRoutes.SPLASH,
        modifier = modifier,
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
                onFinished = {
                    navController.navigate(PulseRoutes.MAIN) {
                        popUpTo(PulseRoutes.SPLASH) { inclusive = true }
                    }
                },
            )
        }
        composable(PulseRoutes.MAIN) {
            MainScreen()
        }
    }
}
