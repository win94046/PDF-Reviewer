package com.yukai.pdfrevieweryukai.ui.navigation

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.yukai.pdfrevieweryukai.ui.screens.home.HomeScreen
import com.yukai.pdfrevieweryukai.ui.screens.pdfviewer.PdfViewerScreen

@Composable
fun PdfReviewerNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToPdfViewer = { encodedUri ->
                    navController.navigate(Screen.PdfViewer.createRoute(encodedUri))
                }
            )
        }
        
        composable(Screen.PdfViewer.route) { backStackEntry ->
            val encodedUri = backStackEntry.arguments?.getString("uri") ?: ""
            PdfViewerScreen(
                uri = encodedUri,
                onNavigateBack = {
//                    Log.d("PdfViewerViewModel", "onNavigateBack")
                    navController.popBackStack()
                }
            )
        }
    }
}