package com.yukai.pdfrevieweryukai.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object PdfViewer : Screen("pdf_viewer/{uri}") {
        fun createRoute(uri: String): String = "pdf_viewer/$uri"
    }
}