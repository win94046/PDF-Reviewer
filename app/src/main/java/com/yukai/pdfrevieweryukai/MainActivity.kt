package com.yukai.pdfrevieweryukai

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.yukai.pdfrevieweryukai.ui.navigation.PdfReviewerNavGraph
import com.yukai.pdfrevieweryukai.ui.navigation.Screen
import com.yukai.pdfrevieweryukai.ui.theme.MyApplicationTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.BLUE
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding(),
                    color = colorResource(id = R.color.gray_lighter)
                ) {
                    val navController = rememberNavController()
                    
                    // 處理從外部應用開啟 PDF 的情況
                    var startDestination by remember { mutableStateOf(Screen.Home.route) }
                    
                    LaunchedEffect(intent) {
                        handleIntent(intent) { uri ->
                            Log.d("PdfViewerViewModel", "handleIntent(intent) uri: ${uri}")
                            startDestination = Screen.PdfViewer.createRoute(Uri.encode(uri))
                        }
                    }
                    
                    PdfReviewerNavGraph(
                        navController = navController,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        
        // 處理新的 Intent（當 App 已經在執行時）
        handleIntent(intent) { uri ->
            // 這裡可以通過 NavController 導航到 PDF 閱讀器
            // 或者重新啟動 Activity
            finish()
            startActivity(Intent(this, MainActivity::class.java).apply {
                data = intent.data
                action = intent.action
            })
        }
    }
    
    private fun handleIntent(intent: Intent?, onPdfUriReceived: (String) -> Unit) {
        intent?.let {
            when (it.action) {
                Intent.ACTION_VIEW -> {
                    it.data?.let { uri ->
                        val mimeType = contentResolver.getType(uri)
                        if (mimeType == "application/pdf" || uri.toString().endsWith(".pdf", ignoreCase = true)) {
                            onPdfUriReceived(uri.toString())
                        }
                    }
                }
                else -> {
                    // Handle other actions if needed
                }
            }
        }
    }
}